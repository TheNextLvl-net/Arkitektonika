package net.thenextlvl.arkitektonika.config;

import com.google.gson.annotations.SerializedName;
import org.jspecify.annotations.NullMarked;

/**
 * <a href="https://github.com/IntellectualSites/Arkitektonika?tab=readme-ov-file#configuration">Original resource</a>
 *
 * @param port             on which port should the application bind
 * @param prune            defines how old records must be to be deleted by the prune script (in ms)
 * @param maxSchematicSize maximum size of schematic files to be accepted (in bytes)
 * @param allowedOrigin    this allows Cross-Origin requests from the specified origin (not documented)
 * @param limiter          refer to {@link Limiter}
 */
@NullMarked
public record Config(
        @SerializedName("port") int port,
        @SerializedName("prune") long prune,
        @SerializedName("maxSchematicSize") long maxSchematicSize,
        @SerializedName("allowedOrigin") String allowedOrigin,
        @SerializedName("limiter") Limiter limiter
) {
    /**
     * @param windowMs   the frame of the limiter (after what duration should the limit gets reset)
     * @param delayAfter After how many requests during windowMs should delayMs be applied
     * @param delayMs    How many ms should the request take longer.
     *                   Formula: currentRequestDelay = (currentRequestAmount - delayAfter) * delayMs
     */
    public record Limiter(
            @SerializedName("windowMs") long windowMs,
            @SerializedName("delayAfter") int delayAfter,
            @SerializedName("delayMs") long delayMs
    ) {
    }
}
