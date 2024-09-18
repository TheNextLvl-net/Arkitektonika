package net.thenextlvl.arkitektonika.routes;

import io.javalin.http.Context;
import lombok.RequiredArgsConstructor;
import net.thenextlvl.arkitektonika.Arkitektonika;

@RequiredArgsConstructor
public class DeleteRoute {
    private final Arkitektonika arkitektonika;

    public void register() {
        arkitektonika.javalin().delete("/delete/{key}", this::delete);
        arkitektonika.javalin().get("/delete/{key}", this::delete);
        arkitektonika.javalin().options("/delete/{key}", context -> {
            context.header("Access-Control-Allow-Methods", "DELETE, GET");
            context.status(204);
        });
    }

    private void delete(Context context) {
        context.future(() -> arkitektonika.schematicController()
                .delete(context.pathParam("key"))
                .thenAccept(success -> {
                    context.result(success ? "File was deleted" : "File not found");
                    context.status(success ? 200 : 404);
                }).exceptionally(throwable -> {
                    context.result(throwable.getMessage());
                    context.status(500);
                    return null;
                }));
    }
}
