package net.thenextlvl.arkitektonika.storage;

import org.jspecify.annotations.NullMarked;

import java.nio.file.Path;
import java.sql.DriverManager;
import java.sql.SQLException;

@NullMarked
public class SQLiteController extends SQLController {
    public SQLiteController(Path dataPath) throws SQLException {
        super(DriverManager.getConnection("jdbc:sqlite:" + dataPath.resolve("database.db")));
    }
}
