package net.thenextlvl.arkitektonika.model;

import java.sql.Date;

public record Schematic(
        int id,
        String downloadKey,
        String deleteKey,
        String fileName,
        Date lastAccessed
) {
    public Schematic(String downloadKey, String deleteKey, String fileName) {
        this(0, downloadKey, deleteKey, fileName, new Date(System.currentTimeMillis()));
    }
}
