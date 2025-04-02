package net.thenextlvl.arkitektonika.storage;

import org.jspecify.annotations.NullMarked;

import java.io.File;
import java.sql.DriverManager;
import java.sql.SQLException;

@NullMarked
public class SQLiteController extends SQLController {
    public SQLiteController(File dataFolder) throws SQLException {
        super(DriverManager.getConnection("jdbc:sqlite:" + new File(dataFolder, "database.db")));
    }
}
