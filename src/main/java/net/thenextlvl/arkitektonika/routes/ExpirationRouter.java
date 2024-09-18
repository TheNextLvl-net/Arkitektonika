package net.thenextlvl.arkitektonika.routes;

import net.thenextlvl.arkitektonika.Arkitektonika;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Spark;

import java.sql.Date;
import java.sql.SQLException;

public class ExpirationRouter {
    private static final Logger logger = LoggerFactory.getLogger(ExpirationRouter.class);

    public static void register() {
        Spark.get("/expiration/:key", ExpirationRouter::get);
        Spark.put("/expiration/:key/:expiration", ExpirationRouter::set);
    }

    private static @Nullable Object get(Request request, Response response) throws SQLException {
        try {
            var record = Arkitektonika.DATA_STORAGE.getSchematicByDownloadKey(request.params(":key"));
            response.status(200);
            return record.expirationDate().getTime();
        } catch (NullPointerException e) {
            response.status(404);
            return null;
        }
    }

    private static @Nullable Object set(Request request, Response response) throws SQLException {
        try {
            var record = Arkitektonika.DATA_STORAGE.getSchematicByDeletionKey(request.params(":key"));
            var expiration = Long.parseLong(request.params(":expiration"));
            record.expirationDate(new Date(expiration));
            Arkitektonika.DATA_STORAGE.updateExpiration(record);
            response.status(200);
            logger.info("Updated expiration of {} to {}", record.fileName(), record.expirationDate());
            return "Updated expiration of " + record.fileName() + " to " + record.expirationDate();
        } catch (NumberFormatException e) {
            response.status(400);
            return null;
        } catch (NullPointerException e) {
            response.status(404);
            return null;
        }
    }
}
