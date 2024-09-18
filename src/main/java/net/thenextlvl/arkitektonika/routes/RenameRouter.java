package net.thenextlvl.arkitektonika.routes;

import net.thenextlvl.arkitektonika.Arkitektonika;
import net.thenextlvl.arkitektonika.model.Schematic;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Spark;

import java.sql.SQLException;

public class RenameRouter {
    private static final Logger logger = LoggerFactory.getLogger(RenameRouter.class);

    public static void register() {
        Spark.options("/rename/:key/:name", (request, response) -> {
            response.header("Access-Control-Allow-Methods", "PUT");
            response.status(204);
            return null;
        });
        Spark.put("/rename/:key/:name", RenameRouter::handleFileRename);
    }

    private static @Nullable Object handleFileRename(Request request, Response response) throws SQLException {
        var record = fetchRecord(request, response);
        if (record == null) return null;
        record.fileName(request.params(":name"));
        Arkitektonika.DATA_STORAGE.renameSchematic(record);
        response.status(200);
        logger.info("Renamed file {}", record.fileName());
        return "Successfully renamed file " + record.fileName();
    }

    private static @Nullable Schematic fetchRecord(Request request, Response response) {
        try {
            return Arkitektonika.DATA_STORAGE.getSchematicByDeletionKey(request.params(":key"));
        } catch (Exception e) {
            response.status(404);
            response.body("No record found for deletion key");
            return null;
        }
    }
}
