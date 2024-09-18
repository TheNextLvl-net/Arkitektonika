package net.thenextlvl.arkitektonika.storage;

import core.util.StringUtil;
import lombok.SneakyThrows;
import net.thenextlvl.arkitektonika.model.Schematic;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class SQLController implements DataController {
    private static final char[] CHARACTERS = "0123456789abcdef".toCharArray();
    private final Connection connection;

    public SQLController(Connection connection) throws SQLException {
        this.connection = connection;
        createAccountingTable();
    }

    @Override
    @SneakyThrows
    public Optional<Schematic> getSchematicByDeletionKey(String key) {
        return executeQuery(
                "SELECT * FROM accounting WHERE delete_key = ? LIMIT 1",
                this::transformRowToRecord, key
        );
    }

    @Override
    @SneakyThrows
    public Optional<Schematic> getSchematicByDownloadKey(String key) {
        return executeQuery(
                "SELECT * FROM accounting WHERE download_key = ? LIMIT 1",
                this::transformRowToRecord, key
        );
    }

    @Override
    @SneakyThrows
    public Optional<Schematic> getSchematicByKey(String key) {
        return executeQuery(
                "SELECT * FROM accounting WHERE download_key = ? OR delete_key = ? LIMIT 1",
                this::transformRowToRecord, key, key
        );
    }

    @Override
    @SneakyThrows
    public String generateDeletionKey() {
        return generateUniqueKey("SELECT expiration_date FROM accounting WHERE delete_key = ? LIMIT 1");
    }

    @Override
    @SneakyThrows
    public String generateDownloadKey() {
        return generateUniqueKey("SELECT expiration_date FROM accounting WHERE download_key = ? LIMIT 1");
    }

    @Override
    @SneakyThrows
    public boolean persistSchematic(Schematic schematic) {
        return executeUpdate("""
                        INSERT INTO accounting (name, download_key, delete_key, expiration_date, data)
                        VALUES (?, ?, ?, ?, ?)
                        """,
                schematic.name(),
                schematic.downloadKey(),
                schematic.deleteKey(),
                schematic.expirationDate(),
                schematic.data()
        ) != 0;
    }

    @Override
    @SneakyThrows
    public boolean removeSchematic(String deletionKey) {
        return executeUpdate("DELETE FROM accounting WHERE delete_key = ?", deletionKey) != 0;
    }

    @Override
    @SneakyThrows
    public boolean renameSchematic(Schematic schematic) {
        return executeUpdate(
                "UPDATE accounting SET name = ? WHERE delete_key = ?",
                schematic.name(), schematic.deleteKey()
        ) != 0;
    }

    @Override
    @SneakyThrows
    public boolean updateExpiration(Schematic schematic) {
        return executeUpdate(
                "UPDATE accounting SET expiration_date = ? WHERE delete_key = ?",
                schematic.expirationDate(), schematic.deleteKey()
        ) != 0;
    }

    @SneakyThrows
    public int pruneSchematics() {
        return executeUpdate(
                "DELETE FROM accounting WHERE expiration_date <= ?",
                new Date(System.currentTimeMillis())
        );
    }

    private void createAccountingTable() throws SQLException {
        executeUpdate("""
                CREATE TABLE IF NOT EXISTS accounting (
                    download_key CHAR(32) NOT NULL UNIQUE,
                    delete_key CHAR(32) NOT NULL UNIQUE,
                    name CHAR(33) NOT NULL,
                    expiration_date INTEGER NOT NULL,
                    data BLOB NOT NULL
                )""");
    }

    private String generateUniqueKey(String query) throws SQLException {
        String key;
        do {
            key = StringUtil.random(CHARACTERS, 32);
        } while (Boolean.TRUE.equals(executeQuery(query, ResultSet::next, key)));
        return key;
    }

    private Optional<Schematic> transformRowToRecord(ResultSet resultSet) throws SQLException {
        return resultSet.next() ? Optional.of(readRow(resultSet)) : Optional.empty();
    }

    private Set<Schematic> transformRowsToRecords(ResultSet resultSet) throws SQLException {
        var schematics = new HashSet<Schematic>();
        while (resultSet.next()) schematics.add(readRow(resultSet));
        return schematics;
    }

    private Schematic readRow(ResultSet resultSet) throws SQLException {
        return new Schematic(
                resultSet.getString("delete_key"),
                resultSet.getString("download_key"),
                resultSet.getBytes("data"),
                resultSet.getDate("expiration_date"),
                resultSet.getString("name")
        );
    }

    private <T> T executeQuery(String query, ThrowingFunction<ResultSet, T> mapper, Object... parameters) throws SQLException {
        try (var preparedStatement = connection.prepareStatement(query)) {
            for (var i = 0; i < parameters.length; i++)
                preparedStatement.setObject(i + 1, parameters[i]);
            try (var resultSet = preparedStatement.executeQuery()) {
                return ThrowingFunction.unchecked(mapper).apply(resultSet);
            }
        }
    }

    private int executeUpdate(String query, Object... parameters) throws SQLException {
        try (var preparedStatement = connection.prepareStatement(query)) {
            for (var i = 0; i < parameters.length; i++)
                preparedStatement.setObject(i + 1, parameters[i]);
            return preparedStatement.executeUpdate();
        }
    }

    private interface ThrowingFunction<T, R> {
        R apply(T t) throws SQLException;

        static <T, R> ThrowingFunction<T, R> unchecked(ThrowingFunction<T, R> f) {
            return f;
        }
    }
}
