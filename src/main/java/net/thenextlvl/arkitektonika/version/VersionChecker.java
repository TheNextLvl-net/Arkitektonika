package net.thenextlvl.arkitektonika.version;

import core.version.SemanticVersion;
import core.version.github.GitHubVersionChecker;
import core.version.github.Release;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

@Getter
public class VersionChecker extends GitHubVersionChecker<SemanticVersion> {
    private static final Logger logger = LoggerFactory.getLogger(VersionChecker.class);

    private final SemanticVersion versionRunning = parseVersion(Objects.requireNonNullElse(
            getClass().getPackage().getImplementationVersion(), "0.0.0"
    ));

    public VersionChecker() {
        super("TheNextLvl-net", "Arkitektonika");
    }

    @Override
    public SemanticVersion parseVersion(String version) {
        return SemanticVersion.parse(version);
    }

    @Override
    public boolean isSupported(Release release) {
        return true;
    }

    public void checkVersion() {
        retrieveLatestSupportedVersion().thenAccept(version -> {
            if (version.equals(versionRunning)) {
                logger.info("You are running the latest version of Arkitektonika");
            } else if (version.compareTo(versionRunning) > 0) {
                logger.warn("An update for Arkitektonika is available");
                logger.warn("You are running version {}, the latest supported version is {}", versionRunning, version);
                logger.warn("Update at https://github.com/{}/{}", getOwner(), getRepository());
            } else logger.warn("You are running a snapshot version of Arkitektonika");
        }).exceptionally(throwable -> {
            logger.error("Version check failed", throwable);
            return null;
        });
    }
}
