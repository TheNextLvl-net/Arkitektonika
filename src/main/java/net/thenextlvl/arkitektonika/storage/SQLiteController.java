package net.thenextlvl.arkitektonika.storage;

import java.io.File;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteController extends SQLController {
    public SQLiteController(File dataFolder) throws SQLException {
        super(DriverManager.getConnection("jdbc:sqlite:" + new File(dataFolder, "database.db")));
    }
}
