package net.thenextlvl.arkitektonika.routes;

import com.google.gson.JsonObject;
import core.nbt.NBTInputStream;
import io.javalin.http.Context;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Part;
import lombok.RequiredArgsConstructor;
import net.thenextlvl.arkitektonika.Arkitektonika;
import net.thenextlvl.arkitektonika.model.Schematic;

import java.io.IOException;
import java.sql.Date;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class UploadRoute {
    private final Arkitektonika arkitektonika;

    public void register() {
        arkitektonika.javalin().post("/upload", this::upload);
        arkitektonika.javalin().options("/upload", context -> {
            context.header("Access-Control-Allow-Methods", "POST");
            context.status(204);
        });
    }

    private void upload(Context context) {
        context.future(() -> CompletableFuture.runAsync(() -> {
            try {
                var request = context.req();
                var part = request.getPart("schematic");
                var maxSize = arkitektonika.config().maxSchematicSize();
                if (part.getSize() > maxSize) {
                    context.result("File exceeds max size of " + maxSize + " bytes");
                    context.status(413);
                } else try (var nbt = new NBTInputStream(part.getInputStream())) {
                    nbt.readNamedTag();
                    persist(part).thenAccept(schematic -> {
                        var json = new JsonObject();
                        json.addProperty("download_key", schematic.downloadKey());
                        json.addProperty("delete_key", schematic.deleteKey());
                        json.addProperty("expiration_date", schematic.expirationDate().getTime());
                        context.header("Content-Type", "application/json");
                        context.result(json.toString());
                        context.status(200);
                    }).exceptionally(throwable -> {
                        context.result(throwable.getMessage());
                        context.status(500);
                        return null;
                    });
                } catch (IOException e) {
                    context.result(e.getMessage());
                    context.status(400);
                }
            } catch (ServletException | IOException e) {
                context.result(e.getMessage());
                context.status(500);
            }
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
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        });
    }
}
