package net.thenextlvl.arkitektonika.storage;

import net.thenextlvl.arkitektonika.model.Schematic;
import org.jspecify.annotations.NullMarked;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@NullMarked
public class SQLController implements DataController {
    private static final char[] CHARACTERS = "0123456789abcdef".toCharArray();
    private final Connection connection;

    public SQLController(Connection connection) throws SQLException {
        this.connection = connection;
        createAccountingTable();
    }

    @Override
    public Optional<Schematic> getSchematicByDeletionKey(String key) {
        try {
            return executeQuery(
                    "SELECT * FROM accounting WHERE delete_key = ? LIMIT 1",
                    this::transformRowToRecord, key
            );
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public Optional<Schematic> getSchematicByDownloadKey(String key) {
        try {
            return executeQuery(
                    "SELECT * FROM accounting WHERE download_key = ? LIMIT 1",
                    this::transformRowToRecord, key
            );
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public Optional<Schematic> getSchematicByKey(String key) {
        try {
            return executeQuery(
                    "SELECT * FROM accounting WHERE download_key = ? OR delete_key = ? LIMIT 1",
                    this::transformRowToRecord, key, key
            );
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public String generateDeletionKey() throws SQLException {
        return generateUniqueKey("SELECT expiration_date FROM accounting WHERE delete_key = ? LIMIT 1");
    }

    @Override
    public String generateDownloadKey() throws SQLException {
        return generateUniqueKey("SELECT expiration_date FROM accounting WHERE download_key = ? LIMIT 1");
    }

    @Override
    public boolean persistSchematic(Schematic schematic) {
        try {
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
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public boolean removeSchematic(String deletionKey) {
        try {
            return executeUpdate("DELETE FROM accounting WHERE delete_key = ?", deletionKey) != 0;
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public boolean renameSchematic(Schematic schematic) {
        try {
            return executeUpdate(
                    "UPDATE accounting SET name = ? WHERE delete_key = ?",
                    schematic.name(), schematic.deleteKey()
            ) != 0;
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public boolean updateExpiration(Schematic schematic) {
        try {
            return executeUpdate(
                    "UPDATE accounting SET expiration_date = ? WHERE delete_key = ?",
                    schematic.expirationDate(), schematic.deleteKey()
            ) != 0;
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public int pruneSchematics() {
        try {
            return executeUpdate(
                    "DELETE FROM accounting WHERE expiration_date <= ?",
                    new Date(System.currentTimeMillis())
            );
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
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
            key = randomString();
        } while (executeQuery(query, ResultSet::next, key));
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

    private static String randomString() {
        var random = ThreadLocalRandom.current();
        var builder = new StringBuilder(32);

        for (int i = 0; i < 32; i++) {
            builder.append(CHARACTERS[random.nextInt(CHARACTERS.length)]);
        }

        return builder.toString();
    }

    private interface ThrowingFunction<T, R> {
        R apply(T t) throws SQLException;

        static <T, R> ThrowingFunction<T, R> unchecked(ThrowingFunction<T, R> f) {
            return f;
        }
    }
}
