package net.thenextlvl.arkitektonika;

import core.file.formats.GsonFile;
import io.javalin.Javalin;
import jakarta.servlet.MultipartConfigElement;
import net.thenextlvl.arkitektonika.config.Config;
import net.thenextlvl.arkitektonika.routes.Base64Route;
import net.thenextlvl.arkitektonika.routes.DeleteRoute;
import net.thenextlvl.arkitektonika.routes.DownloadRoute;
import net.thenextlvl.arkitektonika.routes.ExpirationRoute;
import net.thenextlvl.arkitektonika.routes.RenameRoute;
import net.thenextlvl.arkitektonika.routes.SizeRoute;
import net.thenextlvl.arkitektonika.routes.UploadRoute;
import net.thenextlvl.arkitektonika.storage.DataController;
import net.thenextlvl.arkitektonika.storage.SQLiteController;
import net.thenextlvl.arkitektonika.storage.SchematicController;
import net.thenextlvl.arkitektonika.version.VersionChecker;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@NullMarked
public class Arkitektonika {
    private static final Logger logger = LoggerFactory.getLogger(Arkitektonika.class);

    private final Path dataPath = Path.of("data");
    private final Config config = new GsonFile<>(dataPath.resolve("config.json"), new Config(
            3000, TimeUnit.MINUTES.toMillis(30), 1000000, "*",
            new Config.Limiter(TimeUnit.SECONDS.toMillis(60), 30, 500)
    )).validate().save().getRoot();

    private final DataController dataController;
    private final SchematicController schematicController = new SchematicController(this);

    private final MultipartConfigElement multipartConfig = new MultipartConfigElement(
            System.getProperty("java.io.tmpdir"),
            config().maxSchematicSize() * 1024, // in KB
            1024 * 1024 * 1024, // 1GB
            10 * 1024 * 1024 // 10MB
    );

    private final Javalin javalin = Javalin.create(config -> config.showJavalinBanner = false);

    public static void main() throws SQLException {
        new Arkitektonika().start();
    }

    private Arkitektonika() throws SQLException {
        this.dataController = new SQLiteController(dataPath);
        new VersionChecker().checkVersion();
        setupAccessControl();
        registerRoutes();
    }

    private void start() {
        try (var executor = Executors.newSingleThreadScheduledExecutor()) {
            executor.scheduleAtFixedRate(this::prune, 0, 1, TimeUnit.MINUTES);
            javalin.start(Integer.getInteger("port", config.port()));
        }
    }

    private void prune() {
        var start = System.currentTimeMillis();
        logger.info("Starting pruning expired schematics...");
        schematicController.prune().thenAccept(amount -> {
            var time = (System.currentTimeMillis() - start) / 1000d;
            logger.info("Pruned {} schematics in {}s", amount, time);
        }).exceptionally(throwable -> {
            logger.error("Failed to prune schematics", throwable);
            return null;
        });
    }

    private void registerRoutes() {
        new Base64Route(this).register();
        new DeleteRoute(this).register();
        new DownloadRoute(this).register();
        new ExpirationRoute(this).register();
        new RenameRoute(this).register();
        new SizeRoute(this).register();
        new UploadRoute(this).register();
    }

    private void setupAccessControl() {
        javalin.before(context -> context.header("Access-Control-Allow-Origin", config.allowedOrigin()));
    }

    public Config config() {
        return config;
    }

    public DataController dataController() {
        return dataController;
    }

    public SchematicController schematicController() {
        return schematicController;
    }

    public MultipartConfigElement multipartConfig() {
        return multipartConfig;
    }

    public Javalin javalin() {
        return javalin;
    }
}