package net.thenextlvl.arkitektonika.routes;

import net.thenextlvl.arkitektonika.Arkitektonika;
import net.thenextlvl.arkitektonika.model.Schematic;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Spark;

import java.io.File;
import java.sql.SQLException;

public class DeleteRouter {
    private static final Logger logger = LoggerFactory.getLogger(DeleteRouter.class);

    public static void register() {
        Spark.options("/delete/:key", (request, response) -> {
            response.header("Access-Control-Allow-Methods", "HEAD, GET");
            response.status(204);
            return null;
        });
        Spark.head("/delete/:key", (request, response) -> {
            if (fetchRecord(request, response) != null) response.status(200);
            return null;
        });
        Spark.get("/delete/:key", DeleteRouter::handleFileDelete);
    }

    private static @Nullable Object handleFileDelete(Request request, Response response) throws SQLException {
        var record = fetchRecord(request, response);
        if (record == null) return null;
        Arkitektonika.DATA_STORAGE.removeSchematic(record);
        var file = new File(Arkitektonika.SCHEMATIC_FOLDER, record.downloadKey());
        if (file.exists() && !file.delete()) {
            response.status(410);
            logger.error("Failed to delete file: {}", file);
            return "Failed to delete file " + record.fileName();
        }
        response.status(200);
        return "Successfully delete file " + record.fileName();
    }

    private static @Nullable Schematic fetchRecord(Request request, Response response) {
        Schematic record;
        try {
            record = Arkitektonika.DATA_STORAGE.getSchematicByDeletionKey(request.params(":key"));
        } catch (Exception e) {
            response.status(404);
            response.body("No record found for deletion key");
            return null;
        }
        return record;
    }
}
