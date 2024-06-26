package net.thenextlvl.arkitektonika.routes;

import com.google.gson.JsonObject;
import core.nbt.NBTInputStream;
import net.thenextlvl.arkitektonika.Arkitektonika;
import net.thenextlvl.arkitektonika.model.Schematic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Spark;

import javax.servlet.MultipartConfigElement;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;

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
        try (var output = new ByteArrayOutputStream();
             var input = schematic.getInputStream()) {
            input.transferTo(output);
            return validate(response, output, fileName);
        } catch (Exception e) {
            logger.error("Failed to persist data in table", e);
            response.status(500);
            return "Failed to persist data in table";
        }
    }

    private static String validate(Response response, ByteArrayOutputStream output, String fileName) throws SQLException {
        var maxSchematicSize = Arkitektonika.CONFIG.maxSchematicSize();
        if (output.size() > maxSchematicSize) {
            logger.error("Invalid request due to file size: {} bytes (max {} bytes)", output.size(), maxSchematicSize);
            response.status(413);
            return "Submitted NBT file exceeds max size of " + maxSchematicSize + " bytes";
        }
        try (var nbt = new NBTInputStream(new ByteArrayInputStream(output.toByteArray()))) {
            nbt.readNamedTag();
            return persist(response, fileName, output);
        } catch (IOException e) {
            logger.error("Invalid request due to nbt content: {}", e.getMessage());
            response.status(400);
            return "File is not a valid NBT";
        }
    }

    private static String persist(Response response, String fileName, ByteArrayOutputStream output) throws SQLException, IOException {
        try (var input = new ByteArrayInputStream(output.toByteArray())) {
            var downloadKey = Arkitektonika.DATA_STORAGE.generateDownloadKey();
            var deletionKey = Arkitektonika.DATA_STORAGE.generateDeletionKey();
            var schematic = new Schematic(downloadKey, deletionKey, fileName);
            Arkitektonika.DATA_STORAGE.createSchematic(schematic);
            Files.copy(input, new File(Arkitektonika.SCHEMATIC_FOLDER, downloadKey).toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
            logger.info("Persisted file {} (downloadKey: {}, deletionKey: {})", fileName, downloadKey, deletionKey);
            response.status(200);
            var json = new JsonObject();
            json.addProperty("download_key", downloadKey);
            json.addProperty("delete_key", deletionKey);
            json.addProperty("expiration_date", schematic.expirationDate().getTime());
            return json.toString();
        }
    }
}
