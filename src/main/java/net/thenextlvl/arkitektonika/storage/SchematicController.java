package net.thenextlvl.arkitektonika.storage;

import net.thenextlvl.arkitektonika.Arkitektonika;
import net.thenextlvl.arkitektonika.model.Schematic;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class SchematicController {
    private final Arkitektonika arkitektonika;

    public SchematicController(Arkitektonika arkitektonika) {
        this.arkitektonika = arkitektonika;
    }

    public CompletableFuture<Boolean> delete(String deletionKey) {
        return CompletableFuture.supplyAsync(() -> arkitektonika.dataController().removeSchematic(deletionKey));
    }

    public CompletableFuture<Integer> prune() {
        return CompletableFuture.supplyAsync(() -> arkitektonika.dataController().pruneSchematics());
    }

    public CompletableFuture<Optional<Schematic>> getByDeletionKey(String key) {
        return CompletableFuture.supplyAsync(() -> arkitektonika.dataController().getSchematicByDeletionKey(key));
    }

    public CompletableFuture<Optional<Schematic>> getByDownloadKey(String key) {
        return CompletableFuture.supplyAsync(() -> arkitektonika.dataController().getSchematicByDownloadKey(key));
    }

    public CompletableFuture<Optional<Schematic>> getByKey(String key) {
        return CompletableFuture.supplyAsync(() -> arkitektonika.dataController().getSchematicByKey(key));
    }
}
