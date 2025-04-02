package net.thenextlvl.arkitektonika.storage;

import net.thenextlvl.arkitektonika.model.Schematic;

import java.sql.SQLException;
import java.util.Optional;

public interface DataController {
    Optional<Schematic> getSchematicByDeletionKey(String key);

    Optional<Schematic> getSchematicByDownloadKey(String key);

    Optional<Schematic> getSchematicByKey(String key);

    String generateDeletionKey() throws SQLException;

    String generateDownloadKey() throws SQLException;

    boolean persistSchematic(Schematic schematic);

    boolean removeSchematic(String deletionKey);

    boolean renameSchematic(Schematic schematic);

    boolean updateExpiration(Schematic schematic);

    int pruneSchematics();
}
