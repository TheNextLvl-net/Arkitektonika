package net.thenextlvl.arkitektonika.routes;

import io.javalin.http.Context;
import net.thenextlvl.arkitektonika.Arkitektonika;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class DownloadRoute {
    private final Arkitektonika arkitektonika;

    public DownloadRoute(Arkitektonika arkitektonika) {
        this.arkitektonika = arkitektonika;
    }

    public void register() {
        arkitektonika.javalin().get("/download/{key}", this::download);
        arkitektonika.javalin().options("/download/{key}", context -> {
            context.header("Access-Control-Allow-Methods", "HEAD, GET");
            context.status(204);
        });
    }

    private void download(Context context) {
        context.future(() -> arkitektonika.schematicController()
                .getByDownloadKey(context.pathParam("key"))
                .thenAccept(optional -> optional.ifPresentOrElse(schematic -> {
                    context.header("Content-Disposition", "attachment; filename=" + schematic.name());
                    context.result(schematic.data());
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
