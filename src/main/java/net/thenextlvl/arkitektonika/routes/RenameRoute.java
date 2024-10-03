package net.thenextlvl.arkitektonika.routes;

import io.javalin.http.Context;
import lombok.RequiredArgsConstructor;
import net.thenextlvl.arkitektonika.Arkitektonika;

@RequiredArgsConstructor
public class RenameRoute {
    private final Arkitektonika arkitektonika;

    public void register() {
        arkitektonika.javalin().put("/rename/{key}/{name}", this::rename);
        arkitektonika.javalin().options("/rename/{key}/{name}", context -> {
            context.header("Access-Control-Allow-Methods", "PUT");
            context.status(204);
        });
    }

    private void rename(Context context) {
        arkitektonika.schematicController()
                .getByDeletionKey(context.pathParam("key"))
                .thenAccept(optional -> optional.ifPresentOrElse(schematic -> {
                    schematic.name(context.pathParam("name"));
                    arkitektonika.dataController().renameSchematic(schematic);
                    context.result("File was renamed");
                    context.status(200);
                }, () -> {
                    context.result("File not found");
                    context.status(404);
                })).exceptionally(throwable -> {
                    context.result(throwable.getMessage());
                    context.status(500);
                    return null;
                });
    }
}
