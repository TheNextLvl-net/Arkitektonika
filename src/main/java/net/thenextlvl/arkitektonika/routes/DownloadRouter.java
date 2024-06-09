package net.thenextlvl.arkitektonika.routes;

import net.thenextlvl.arkitektonika.Arkitektonika;
import net.thenextlvl.arkitektonika.model.Schematic;
import org.jetbrains.annotations.Nullable;
import spark.Request;
import spark.Response;
import spark.Spark;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class DownloadRouter {
    public static void register() {
        Spark.options("/download/:key", (request, response) -> {
            response.header("Access-Control-Allow-Methods", "HEAD, GET");
            response.status(200);
            return null;
        });
        Spark.head("/download/:key", (request, response) -> {
            var record = fetchRecord(request, response);
            if (record == null) return null;
            var file = new File(Arkitektonika.SCHEMATIC_FOLDER, record.downloadKey());
            if (file.exists()) response.status(200);
            return null;
        });
        Spark.get("/download/:key", DownloadRouter::handleFileDownload);
    }

    private static @Nullable Object handleFileDownload(Request request, Response response) throws IOException {
        var record = fetchRecord(request, response);
        if (record == null) return null;
        var file = new File(Arkitektonika.SCHEMATIC_FOLDER, record.downloadKey());
        if (!file.exists()) return null;
        var data = Files.readAllBytes(file.toPath());
        response.header("Content-Disposition", "attachment; filename=" + record.fileName());
        response.status(200);
        return data;
    }

    private static @Nullable Schematic fetchRecord(Request request, Response response) {
        try {
            return Arkitektonika.DATA_STORAGE.getSchematicByDownloadKey(request.params(":key"));
        } catch (Exception e) {
            response.status(404);
            response.body("No record found for deletion key");
            return null;
        }
    }
}
