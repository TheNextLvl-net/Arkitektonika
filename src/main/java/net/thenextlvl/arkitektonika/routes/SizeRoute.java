package net.thenextlvl.arkitektonika.routes;

import io.javalin.http.Context;
import lombok.RequiredArgsConstructor;
import net.thenextlvl.arkitektonika.Arkitektonika;

@RequiredArgsConstructor
public class SizeRoute {
    private final Arkitektonika arkitektonika;

    public void register() {
        arkitektonika.javalin().get("/size/{key}", this::get);
        arkitektonika.javalin().options("/size/{key}", context -> {
            context.header("Access-Control-Allow-Methods", "GET");
            context.status(204);
        });
    }

    private void get(Context context) {
        context.future(() -> arkitektonika.schematicController()
                .getByDownloadKey(context.pathParam("key"))
                .thenAccept(optional -> optional.ifPresentOrElse(schematic -> {
                    context.result(String.valueOf(schematic.data().length));
                    context.status(200);
                }, () -> {
                    context.result("File not found");
                    context.status(404);
                })).exceptionally(throwable -> {
                    context.result(throwable.getMessage());
                    context.status(500);
                    return null;
                }));
    }
}
