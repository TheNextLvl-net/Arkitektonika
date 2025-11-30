package net.thenextlvl.arkitektonika.version;

import net.thenextlvl.version.SemanticVersion;
import net.thenextlvl.version.github.GitHubVersionChecker;
import net.thenextlvl.version.github.Release;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

@NullMarked
public class VersionChecker extends GitHubVersionChecker<SemanticVersion> {
    private static final Logger logger = LoggerFactory.getLogger(VersionChecker.class);

    private final SemanticVersion versionRunning = parseVersion(Objects.requireNonNullElse(
            getClass().getPackage().getImplementationVersion(), "0.0.0"
    ));

    public VersionChecker() {
        super("TheNextLvl-net", "Arkitektonika");
    }

    @Override
    public SemanticVersion getVersionRunning() {
        return versionRunning;
    }

    @Override
    public SemanticVersion parseVersion(String version) {
        return SemanticVersion.parse(version.startsWith("v") ? version.substring(1) : version);
    }

    @Override
    public boolean isSupported(Release release) {
        return true;
    }

    public void checkVersion() {
        if (versionRunning.toString().equals("0.0.0"))
            logger.info("You are running a development version of Arkitektonika");
        else retrieveLatestVersion().thenAccept(this::printVersionInfo).exceptionally(throwable -> {
            logger.warn("There are no public releases for this plugin yet");
            return null;
        });
    }

    private void printVersionInfo(SemanticVersion version) {
        if (version.equals(versionRunning)) {
            logger.info("You are running the latest version of Arkitektonika");
        } else if (version.compareTo(versionRunning) > 0) {
            logger.warn("An update for Arkitektonika is available");
            logger.warn("You are running version {}, the latest version is {}", versionRunning, version);
            logger.warn("Update at https://github.com/{}/{}", getOwner(), getRepository());
            logger.warn("Do not test in production and always make backups before updating");
        } else logger.warn("You are running a snapshot version of Arkitektonika");
    }
}
