package net.thenextlvl.arkitektonika.routes;

import io.javalin.http.Context;
import net.thenextlvl.arkitektonika.Arkitektonika;

import java.util.Base64;

public class Base64Route {
    private final Arkitektonika arkitektonika;

    public Base64Route(Arkitektonika arkitektonika) {
        this.arkitektonika = arkitektonika;
    }

    public void register() {
        arkitektonika.javalin().get("/base64/{key}", this::get);
        arkitektonika.javalin().options("/base64/{key}", context -> {
            context.header("Access-Control-Allow-Methods", "GET");
            context.status(204);
        });
    }

    private void get(Context context) {
        context.future(() -> arkitektonika.schematicController()
                .getByDownloadKey(context.pathParam("key"))
                .thenAccept(optional -> optional.ifPresentOrElse(schematic -> {
                    context.result(Base64.getEncoder().encodeToString(schematic.data()));
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
