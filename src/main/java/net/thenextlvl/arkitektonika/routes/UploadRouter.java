package net.thenextlvl.arkitektonika.routes;

import com.google.gson.JsonObject;
import net.thenextlvl.arkitektonika.Arkitektonika;
import net.thenextlvl.arkitektonika.model.Schematic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Spark;

import javax.servlet.MultipartConfigElement;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class UploadRouter {
    private static final Logger logger = LoggerFactory.getLogger(UploadRouter.class);

    public static void register() {
        Spark.options("/upload", (request, response) -> {
            response.header("Access-Control-Allow-Methods", "POST");
            response.status(204);
            return null;
        });
        Spark.before("/upload", (request, response) -> {
            var element = new MultipartConfigElement("/temp");
            request.raw().setAttribute("org.eclipse.jetty.multipartConfig", element);
        });
        Spark.post("/upload", UploadRouter::handleFileUpload);
    }

    private static Object handleFileUpload(Request request, Response response) throws Exception {
        var schematic = request.raw().getPart("schematic");
        var fileName = schematic != null ? schematic.getSubmittedFileName() : null;
        if (schematic == null || fileName == null) {
            response.status(400);
            return "Missing file";
        }

        // todo: validate nbt format

        try (var input = schematic.getInputStream()) {
            var downloadKey = Arkitektonika.DATA_STORAGE.generateDownloadKey();
            var deletionKey = Arkitektonika.DATA_STORAGE.generateDeletionKey();
            Arkitektonika.DATA_STORAGE.createSchematic(new Schematic(
                    downloadKey, deletionKey, fileName
            ));
            Files.copy(input, new File(Arkitektonika.SCHEMATIC_FOLDER, downloadKey).toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
            logger.info("Persisted file {} (downloadKey: {}, deletionKey: {})", fileName, downloadKey, deletionKey);
            var json = new JsonObject();
            json.addProperty("download_key", downloadKey);
            json.addProperty("delete_key", deletionKey);
            return json.toString();
        } catch (Exception e) {
            logger.error("Failed to persist data in table", e);
            response.status(500);
            return "Failed to persist data in table";
        }
    }
}
