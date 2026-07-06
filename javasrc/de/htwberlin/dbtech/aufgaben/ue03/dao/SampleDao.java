package de.htwberlin.dbtech.aufgaben.ue03.dao;

import java.sql.Connection;
import java.time.LocalDate;

/**
 * Schnittstelle für den Datenbankzugriff auf die Tabelle Sample.
 *
 * @author Kieu, Viona
 */
public interface SampleDao {

    /**
     * Setzt die Datenbankverbindung.
     *
     * @param connection Verbindung zur Datenbank
     */
    void setConnection(Connection connection);

    /**
     * Prüft, ob eine Probe mit der angegebenen ID existiert.
     *
     * @param sampleId ID der Probe
     * @return true, wenn die Probe existiert, sonst false
     */
    boolean isSampleIdExisting(Integer sampleId);

    /**
     * Liefert das Ablaufdatum einer Probe.
     *
     * @param sampleId ID der Probe
     * @return Ablaufdatum der Probe
     */
    LocalDate getSampleExpirationDate(Integer sampleId);
}