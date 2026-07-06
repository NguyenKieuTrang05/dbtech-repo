package de.htwberlin.dbtech.aufgaben.ue03.dao;

import java.sql.Connection;

/**
 * Schnittstelle für den Datenbankzugriff auf die Tabelle Place.
 *
 * @author Kieu, Viona
 */

public interface PlaceDao {

    /**
     * Setzt die Datenbankverbindung.
     *
     * @param connection Verbindung zur Datenbank
     */
    void setConnection(Connection connection);

    /**
     * Ermittelt die kleinste freie Platznummer eines Trays.
     *
     * @param trayId ID des Trays
     * @return Nummer des freien Platzes
     */
    Integer findSmallestFreePlaceNo(Integer trayId);

    /**
     * Fügt eine Probe auf dem kleinsten freien Platz eines Trays ein.
     *
     * @param sampleId ID der Probe
     * @param trayId ID des Trays
     */
    void updateSampleTray(Integer sampleId, Integer trayId);
}