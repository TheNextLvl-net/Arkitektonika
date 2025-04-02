package net.thenextlvl.arkitektonika.routes;

import io.javalin.http.Context;
import net.thenextlvl.arkitektonika.Arkitektonika;
import org.jspecify.annotations.NullMarked;

import java.sql.Date;

@NullMarked
public class ExpirationRoute {
    private final Arkitektonika arkitektonika;

    public ExpirationRoute(Arkitektonika arkitektonika) {
        this.arkitektonika = arkitektonika;
    }

    public void register() {
        arkitektonika.javalin().get("/expiration/{key}", this::get);
        arkitektonika.javalin().put("/expiration/{key}/{expiration}", this::set);
        arkitektonika.javalin().options("/expiration/{key}", context -> {
            context.header("Access-Control-Allow-Methods", "GET, PUT");
            context.status(204);
        });
    }

    private void get(Context context) {
        context.future(() -> arkitektonika.schematicController()
                .getByKey(context.pathParam("key"))
                .thenAccept(optional -> optional.ifPresentOrElse(schematic -> {
                    context.result(String.valueOf(schematic.expirationDate().getTime()));
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

    private void set(Context context) {
        context.future(() -> arkitektonika.schematicController()
                .getByDeletionKey(context.pathParam("key"))
                .thenAccept(optional -> optional.ifPresentOrElse(schematic -> {
                    var expiration = context.pathParamAsClass("expiration", long.class).get();
                    schematic.expirationDate(new Date(expiration));
                    arkitektonika.dataController().updateExpiration(schematic);
                    context.result("Expiration updated");
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
