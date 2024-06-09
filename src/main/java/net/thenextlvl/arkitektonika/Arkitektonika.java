package net.thenextlvl.arkitektonika;

import core.file.format.GsonFile;
import core.io.IO;
import net.thenextlvl.arkitektonika.config.Config;
import net.thenextlvl.arkitektonika.routes.DeleteRouter;
import net.thenextlvl.arkitektonika.routes.DownloadRouter;
import net.thenextlvl.arkitektonika.routes.UploadRouter;
import net.thenextlvl.arkitektonika.storage.DataStorage;
import net.thenextlvl.arkitektonika.storage.DatabaseStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;

import java.io.File;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Arkitektonika {
    public static final File DATA_FOLDER = new File("data");
    public static final File SCHEMATIC_FOLDER = new File(DATA_FOLDER, "schematics");

    public static final Config CONFIG = new GsonFile<>(IO.of(DATA_FOLDER, "config.json"), new Config(
            3000, TimeUnit.MINUTES.toMillis(30), 1000000, "*",
            new Config.Limiter(TimeUnit.SECONDS.toMillis(60), 30, 500)
    )).validate().save().getRoot();

    public static final DataStorage DATA_STORAGE = new DatabaseStorage();
    private static final Logger logger = LoggerFactory.getLogger(Arkitektonika.class);

    static {
        //noinspection ResultOfMethodCallIgnored
        SCHEMATIC_FOLDER.mkdirs();
    }

    public static void main(String[] args) {
        try (var executorService = Executors.newSingleThreadScheduledExecutor()) {
            executorService.scheduleAtFixedRate(Arkitektonika::prune, 0, CONFIG.prune(), TimeUnit.MILLISECONDS);
            Spark.port(CONFIG.port());
            registerAccessControl();
            DeleteRouter.register();
            DownloadRouter.register();
            UploadRouter.register();
        }
    }

    private static void registerAccessControl() {
        Spark.before((request, response) -> response.header("Access-Control-Allow-Origin", CONFIG.allowedOrigin()));
    }

    public static void prune() {
        try {
            logger.info("Starting prune of old and unused schematics...");
            int outdated = outdatedPrune();
            int dangling = danglingPrune();
            int invalid = invalidPrune();
            logger.info("Pruned {} schematics ({} outdated, {} dangling, {} invalid)",
                    outdated + dangling + invalid, outdated, dangling, invalid);
        } catch (SQLException e) {
            logger.error("Failed to perform a prune", e);
        }
    }

    private static int invalidPrune() throws SQLException {
        var deletionCounter = 0;
        for (var record : DATA_STORAGE.getSchematics()) {
            if (new File(SCHEMATIC_FOLDER, record.downloadKey()).exists()) continue;
            DATA_STORAGE.removeSchematic(record);
            logger.info("Removed schematic {} because no file system entry was present", record.downloadKey());
            deletionCounter++;
        }
        return deletionCounter;
    }

    private static int danglingPrune() {
        var deletionCounter = 0;
        var files = SCHEMATIC_FOLDER.listFiles();
        if (files != null) for (var file : files) {
            try {
                DATA_STORAGE.getSchematicByDownloadKey(file.getName());
            } catch (SQLException e) {
                logger.debug("Deleting dangling file {}", file.getName());
                var success = file.delete();
                if (!success) logger.error("Failed to delete dangling file {}", file.getName());
                else deletionCounter++;
            }
        }
        return deletionCounter;
    }

    private static int outdatedPrune() throws SQLException {
        var deleted = DATA_STORAGE.removeSchematics();
        for (var record : deleted) {
            var success = new File(SCHEMATIC_FOLDER, record.downloadKey()).delete();
            if (!success) logger.error("Failed to delete file: {}", record.downloadKey());
        }
        return deleted.size();
    }
}