package net.thenextlvl.arkitektonika.storage;

import core.util.StringUtil;
import net.thenextlvl.arkitektonika.Arkitektonika;
import net.thenextlvl.arkitektonika.model.Schematic;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DatabaseStorage implements DataStorage {
    private static final char[] CHARACTERS = "0123456789abcdef".toCharArray();
    private final Connection connection;

    public DatabaseStorage() {
        try {
            var url = "jdbc:sqlite:" + new File(Arkitektonika.DATA_FOLDER, "database.db");
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection(url);
            migrate();
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to initialize Database", e);
        }
    }

    @Override
    public List<Schematic> getSchematics() throws SQLException {
        return executeQuery("SELECT * FROM accounting", this::transformRowsToRecords);
    }

    @Override
    public Schematic getSchematicByDeletionKey(String deletionKey) throws SQLException {
        return executeQuery(
                "SELECT * FROM accounting WHERE delete_key = ? LIMIT 1",
                this::transformRowToRecord,
                deletionKey
        );
    }

    @Override
    public Schematic getSchematicByDownloadKey(String downloadKey) throws SQLException {
        return executeQuery(
                "SELECT * FROM accounting WHERE download_key = ? LIMIT 1",
                this::transformRowToRecord,
                downloadKey
        );
    }

    @Override
    public void removeSchematic(Schematic schematic) throws SQLException {
        executeUpdate("DELETE FROM accounting WHERE delete_key = ?", schematic.deleteKey());
    }

    @Override
    public void createSchematic(Schematic schematic) throws SQLException {
        executeUpdate(
                "INSERT INTO accounting (filename, download_key, delete_key, expiration_date) VALUES (?, ?, ?, ?)",
                schematic.fileName(),
                schematic.downloadKey(),
                schematic.deleteKey(),
                schematic.expirationDate()
        );
    }

    @Override
    public void renameSchematic(Schematic schematic) throws SQLException {
        executeUpdate(
                "UPDATE accounting SET filename = ? WHERE delete_key = ?",
                schematic.fileName(), schematic.deleteKey()
        );
    }

    @Override
    public List<Schematic> removeSchematics() throws SQLException {
        var schematics = executeQuery(
                "SELECT * FROM accounting WHERE expiration_date <= ?",
                this::transformRowsToRecords,
                new Date(System.currentTimeMillis())
        );
        for (var schematic : schematics) removeSchematic(schematic);
        return schematics;
    }

    @Override
    public String generateDeletionKey() throws SQLException {
        return generateUniqueKey("SELECT expiration_date FROM accounting WHERE delete_key = ? LIMIT 1");
    }

    @Override
    public String generateDownloadKey() throws SQLException {
        return generateUniqueKey("SELECT expiration_date FROM accounting WHERE download_key = ? LIMIT 1");
    }

    private String generateUniqueKey(String query) throws SQLException {
        String key;
        do {
            key = StringUtil.random(CHARACTERS, 32);
        } while (executeQuery(query, ResultSet::next, key));
        return key;
    }

    private void migrate() throws SQLException {
        executeUpdate("""
                CREATE TABLE IF NOT EXISTS accounting (
                    download_key CHAR(32) NOT NULL UNIQUE,
                    delete_key CHAR(32) NOT NULL UNIQUE,
                    filename CHAR(33) NOT NULL,
                    expiration_date INTEGER NOT NULL
                )""");
    }

    private Schematic transformRowToRecord(ResultSet resultSet) throws SQLException {
        return new Schematic(
                Objects.requireNonNull(resultSet.getString("download_key")),
                Objects.requireNonNull(resultSet.getString("delete_key")),
                Objects.requireNonNull(resultSet.getString("filename")),
                Objects.requireNonNull(resultSet.getDate("expiration_date"))
        );
    }

    private List<Schematic> transformRowsToRecords(ResultSet resultSet) throws SQLException {
        var schematics = new ArrayList<Schematic>();
        while (resultSet.next()) {
            schematics.add(transformRowToRecord(resultSet));
        }
        return schematics;
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

    private void executeUpdate(String query, Object... parameters) throws SQLException {
        try (var preparedStatement = connection.prepareStatement(query)) {
            for (var i = 0; i < parameters.length; i++)
                preparedStatement.setObject(i + 1, parameters[i]);
            preparedStatement.executeUpdate();
        }
    }

    private interface ThrowingFunction<T, R> {
        R apply(T t) throws SQLException;

        static <T, R> ThrowingFunction<T, R> unchecked(ThrowingFunction<T, R> f) {
            return f;
        }
    }
}
