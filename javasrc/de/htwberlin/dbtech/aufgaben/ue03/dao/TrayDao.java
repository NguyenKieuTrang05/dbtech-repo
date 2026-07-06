package de.htwberlin.dbtech.aufgaben.ue03.dao;

import java.sql.Connection;
import java.time.LocalDate;

/**
 * Schnittstelle für den Datenbankzugriff auf die Tabelle Tray.
 *
 * @author Kieu, Viona
 */

public interface TrayDao {
    /**
     * Setzt die Datenbankverbindung.
     *
     * @param connection Verbindung zur Datenbank
     */
    void setConnection(Connection connection);;

    /**
     * Sucht ein geeignetes Tray mit freiem Platz.
     *
     * @param expirationDate Ablaufdatum der Probe
     * @param diameterInCM Durchmesser der Probe
     * @return ID des passenden Trays oder null
     */
    Integer findSuitableTray(LocalDate expirationDate, Integer diameterInCM);

    /**
     * Sucht ein leeres Tray mit passendem Durchmesser.
     *
     * @param diameterInCM Durchmesser der Probe
     * @return ID des Trays oder null
     */
    Integer findEmptyTray(Integer diameterInCM);

    /**
     * Aktualisiert das Ablaufdatum eines Trays.
     *
     * @param trayId ID des Trays
     * @param expirationDate neues Ablaufdatum
     */
    void updateTrayExpirationDate(Integer trayId, LocalDate expirationDate);
}