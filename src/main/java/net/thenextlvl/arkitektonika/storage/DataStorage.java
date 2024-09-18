package net.thenextlvl.arkitektonika.storage;

import net.thenextlvl.arkitektonika.model.Schematic;

import java.sql.SQLException;
import java.util.List;

public interface DataStorage {
    /**
     * Get all schematics
     *
     * @return a list of schematics
     * @throws SQLException thrown if something goes wrong
     */
    List<Schematic> getSchematics() throws SQLException;

    /**
     * Get the schematic corresponding to the given download key
     *
     * @param downloadKey the download key
     * @return the corresponding schematic
     * @throws SQLException thrown if something goes wrong
     */
    Schematic getSchematicByDownloadKey(String downloadKey) throws SQLException;

    /**
     * Get the schematic corresponding to the given deletion key
     *
     * @param deletionKey the deletion key
     * @return the corresponding schematic
     * @throws SQLException thrown if something goes wrong
     */
    Schematic getSchematicByDeletionKey(String deletionKey) throws SQLException;

    /**
     * Remove a schematic
     *
     * @throws SQLException thrown if something goes wrong
     */
    void removeSchematic(Schematic schematic) throws SQLException;

    /**
     * Store a schematic
     *
     * @throws SQLException thrown if something goes wrong
     */
    void createSchematic(Schematic schematic) throws SQLException;

    /**
     * Rename a schematic
     *
     * @throws SQLException thrown if something goes wrong
     */
    void renameSchematic(Schematic schematic) throws SQLException;

    /**
     * Updates the expiration date of the given schematic.
     *
     * @param schematic the schematic whose expiration date is to be updated
     * @throws SQLException if a database access error occurs
     */
    void updateExpiration(Schematic schematic) throws SQLException;

    /**
     * Remove all schematics where {@link Schematic#expirationDate()} has passed
     *
     * @return a list of schematics that where removed
     * @throws SQLException thrown if something goes wrong
     */
    List<Schematic> removeSchematics() throws SQLException;

    /**
     * Generate a new unique download key
     *
     * @return the download key
     * @throws SQLException thrown if something goes wrong
     */
    String generateDownloadKey() throws SQLException;

    /**
     * Generate a new unique deletion key
     *
     * @return the deletion key
     * @throws SQLException thrown if something goes wrong
     */
    String generateDeletionKey() throws SQLException;
}
