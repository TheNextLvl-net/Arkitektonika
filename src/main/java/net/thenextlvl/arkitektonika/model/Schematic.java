package net.thenextlvl.arkitektonika.model;

import java.sql.Date;

public record Schematic(
        String downloadKey,
        String deleteKey,
        String fileName,
        Date lastAccessed
) {
    public Schematic(String downloadKey, String deleteKey, String fileName) {
        this(downloadKey, deleteKey, fileName, new Date(System.currentTimeMillis()));
    }
}
