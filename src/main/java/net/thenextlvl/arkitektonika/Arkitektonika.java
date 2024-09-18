package net.thenextlvl.arkitektonika;

import core.file.format.GsonFile;
import core.io.IO;
import io.javalin.Javalin;
import jakarta.servlet.MultipartConfigElement;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import net.thenextlvl.arkitektonika.config.Config;
import net.thenextlvl.arkitektonika.routes.*;
import net.thenextlvl.arkitektonika.storage.DataController;
import net.thenextlvl.arkitektonika.storage.SQLiteController;
import net.thenextlvl.arkitektonika.storage.SchematicController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Getter
@Accessors(fluent = true)
public class Arkitektonika {
    private static final Logger logger = LoggerFactory.getLogger(Arkitektonika.class);

    private final File dataFolder = new File("data");
    private final Config config = new GsonFile<>(IO.of(dataFolder, "config.json"), new Config(
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

    public static void main(String[] args) {
        new Arkitektonika().start();
    }

    @SneakyThrows
    private Arkitektonika() {
        this.dataController = new SQLiteController(dataFolder);
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
        new UploadRoute(this).register();
    }

    private void setupAccessControl() {
        javalin.before(context -> context.header("Access-Control-Allow-Origin", config.allowedOrigin()));
    }
}