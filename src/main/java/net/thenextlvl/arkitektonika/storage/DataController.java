package net.thenextlvl.arkitektonika.storage;

import net.thenextlvl.arkitektonika.model.Schematic;

import java.util.Optional;

public interface DataController {
    Optional<Schematic> getSchematicByDeletionKey(String key);

    Optional<Schematic> getSchematicByDownloadKey(String key);

    Optional<Schematic> getSchematicByKey(String key);

    String generateDeletionKey();

    String generateDownloadKey();

    boolean persistSchematic(Schematic schematic);

    boolean removeSchematic(String deletionKey);

    boolean renameSchematic(Schematic schematic);

    boolean updateExpiration(Schematic schematic);

    int pruneSchematics();
}
