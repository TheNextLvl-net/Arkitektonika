package net.thenextlvl.arkitektonika.routes;

import com.google.gson.JsonObject;
import io.javalin.http.Context;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Part;
import net.thenextlvl.arkitektonika.Arkitektonika;
import net.thenextlvl.arkitektonika.model.Schematic;
import net.thenextlvl.nbt.NBTInputStream;
import org.jspecify.annotations.NullMarked;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

@NullMarked
public class UploadRoute {
    private final Arkitektonika arkitektonika;

    public UploadRoute(Arkitektonika arkitektonika) {
        this.arkitektonika = arkitektonika;
    }

    public void register() {
        arkitektonika.javalin().post("/upload", this::upload);
        arkitektonika.javalin().options("/upload", context -> {
            context.header("Access-Control-Allow-Methods", "POST");
            context.status(204);
        });
    }

    private void upload(Context context) {
        context.future(() -> CompletableFuture.supplyAsync(() -> {
            try {
                var request = context.req();
                request.setAttribute("org.eclipse.jetty.multipartConfig", arkitektonika.multipartConfig());
                var part = request.getPart("schematic");
                var maxSize = arkitektonika.config().maxSchematicSize();
                if (part.getSize() > maxSize) {
                    context.result("File exceeds max size of " + maxSize + " bytes");
                    context.status(413);
                    return null;
                } else try (var nbt = NBTInputStream.create(part.getInputStream())) {
                    var ignored = nbt.readTag();
                    return part;
                } catch (IOException e) {
                    context.result(e.getMessage());
                    context.status(400);
                    return null;
                }
            } catch (ServletException | IOException e) {
                context.result(e.getMessage());
                context.status(500);
                return null;
            }
        }).thenCompose(part -> persist(part).thenAccept(schematic -> {
            var json = new JsonObject();
            json.addProperty("download_key", schematic.downloadKey());
            json.addProperty("delete_key", schematic.deleteKey());
            json.addProperty("expiration_date", schematic.expirationDate().getTime());
            context.header("Content-Type", "application/json");
            context.result(json.toString());
            context.status(200);
        })).exceptionally(throwable -> {
            context.result(throwable.getMessage());
            context.status(500);
            return null;
        }));
    }

    private CompletableFuture<Schematic> persist(Part part) {
        return CompletableFuture.supplyAsync(() -> {
            try (var data = part.getInputStream()) {
                var downloadKey = arkitektonika.dataController().generateDownloadKey();
                var deletionKey = arkitektonika.dataController().generateDeletionKey();
                var schematic = new Schematic(downloadKey, deletionKey, data.readAllBytes(),
                        new Date(System.currentTimeMillis() + arkitektonika.config().prune()),
                        part.getSubmittedFileName());
                arkitektonika.dataController().persistSchematic(schematic);
                return schematic;
            } catch (SQLException | IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        });
    }
}
