package net.thenextlvl.arkitektonika.model;

import org.jspecify.annotations.NullMarked;

import java.sql.Date;

@NullMarked
public class Schematic {
    private final String deleteKey;
    private final String downloadKey;
    private final byte[] data;
    private Date expirationDate;
    private String name;

    public Schematic(String deleteKey, String downloadKey, byte[] data, Date expirationDate, String name) {
        this.deleteKey = deleteKey;
        this.downloadKey = downloadKey;
        this.data = data;
        this.expirationDate = expirationDate;
        this.name = name;
    }

    public String deleteKey() {
        return deleteKey;
    }

    public String downloadKey() {
        return downloadKey;
    }

    public byte[] data() {
        return data;
    }

    public Date expirationDate() {
        return expirationDate;
    }

    public void expirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String name() {
        return name;
    }

    public void name(String name) {
        this.name = name;
    }
}
