package net.thenextlvl.arkitektonika.model;

import lombok.*;
import lombok.experimental.Accessors;
import net.thenextlvl.arkitektonika.Arkitektonika;

import java.sql.Date;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@RequiredArgsConstructor
@Accessors(fluent = true)
public class Schematic {
    private final String downloadKey;
    private final String deleteKey;
    private String fileName;
    private Date expirationDate;

    public Schematic(String downloadKey, String deleteKey, String fileName) {
        this(downloadKey, deleteKey, fileName, new Date(
                System.currentTimeMillis() + Arkitektonika.CONFIG.prune()
        ));
    }
}
