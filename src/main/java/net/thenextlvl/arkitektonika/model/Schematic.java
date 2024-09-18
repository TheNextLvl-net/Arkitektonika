package net.thenextlvl.arkitektonika.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.sql.Date;

@Getter
@Setter
@AllArgsConstructor
@Accessors(fluent = true, chain = false)
public class Schematic {
    private final String deleteKey;
    private final String downloadKey;
    private final byte[] data;
    private Date expirationDate;
    private String name;
}
