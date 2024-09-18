package net.thenextlvl.arkitektonika.routes;

import net.thenextlvl.arkitektonika.Arkitektonika;
import org.jetbrains.annotations.Nullable;
import spark.Request;
import spark.Response;
import spark.Spark;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

public class Base64Router {
    public static void register() {
        Spark.get("/base64/:key", Base64Router::get);
    }

    private static @Nullable Object get(Request request, Response response) throws IOException {
        var file = new File(Arkitektonika.SCHEMATIC_FOLDER, request.params(":key"));
        if (!file.exists()) return null;
        var data = Files.readAllBytes(file.toPath());
        response.status(200);
        return Base64.getEncoder().encodeToString(data);
    }
}
