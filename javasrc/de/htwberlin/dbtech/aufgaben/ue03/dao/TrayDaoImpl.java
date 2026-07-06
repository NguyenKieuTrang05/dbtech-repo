package de.htwberlin.dbtech.aufgaben.ue03.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import de.htwberlin.dbtech.exceptions.DataException;

/**
 * Implementierung des Data Access Objects für die Tabelle Tray.
 *
 * @author Kieu, Viona
 */

public class TrayDaoImpl implements TrayDao {
    private Connection connection;

    /**
     * Erstellt ein Tray-DAO mit Datenbankverbindung.
     *
     * @param connection Verbindung zur Datenbank
     */

    public TrayDaoImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    /**
     * Liefert die aktuelle Datenbankverbindung.
     *
     * @return Datenbankverbindung
     */

    private Connection useConnection() {
        if (connection == null) {
            throw new DataException("Connection not set");
        }
        return connection;
    }
    /**
     * Ermittelt ein geeignetes Tray mit freiem Platz.
     *
     * @param expirationDate Ablaufdatum der Probe
     * @param diameterInCM Durchmesser der Probe
     * @return ID des passenden Trays oder null
     */

    @Override
    public Integer findSuitableTray(LocalDate expirationDate, Integer diameterInCM) {
        PreparedStatement pStmt = null;
        ResultSet rs = null;

        try {
            String sql =
                    "Select trayId " +
                            "from ( " +
                            "    Select " +
                            "        count(p.sampleId) as anzahl_proben, " +
                            "        t.trayId, " +
                            "        t.capacity, " +
                            "        t.expirationDate " +
                            "    from Place p " +
                            "    right join Tray t on p.trayId = t.trayId " +
                            "    where t.diameterInCM = ? " +
                            "    and t.expirationDate > ? " +
                            "    group by t.trayId, t.capacity, t.expirationDate " +
                            ") " +
                            "where capacity - anzahl_proben > 0 " +
                            "order by  expirationDate asc";

            pStmt = useConnection().prepareStatement(sql);
            pStmt.setInt(1, diameterInCM);
            pStmt.setDate(2, java.sql.Date.valueOf(expirationDate));
            rs = pStmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("trayId");
            }
            return null;
        } catch (SQLException e) {
            throw new DataException(e);
        }


    }
    /**
     * Sucht ein leeres Tray mit passendem Durchmesser.
     *
     * @param diameterInCM Durchmesser der Probe
     * @return ID des Trays oder null
     */

    @Override
    public Integer findEmptyTray(Integer diameterInCM) {
        PreparedStatement pStmt = null;
        ResultSet rs = null;

        try {
            String sql = "Select t.trayId from Tray t " +
                    "where t.diameterInCM = ? " +
                    "and not exists (select 1 from Place p where p.trayId = t.trayId) " +
                    "order by t.trayId asc ";
            pStmt = useConnection().prepareStatement(sql);
            pStmt.setInt(1, diameterInCM);
            rs = pStmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("trayId");
            }
            return null;
        } catch (SQLException e) {
            throw new DataException(e);
        }
    }
    /**
     * Aktualisiert das Ablaufdatum eines Trays.
     *
     * @param trayId ID des Trays
     * @param expirationDate neues Ablaufdatum
     */

    @Override
    public void updateTrayExpirationDate(Integer trayId, LocalDate expirationDate) {
        PreparedStatement pStmt = null;

        try {
            String sql = "Update Tray set expirationDate = ?  where trayId = ?";
            pStmt = useConnection().prepareStatement(sql);
            pStmt.setDate(1, java.sql.Date.valueOf(expirationDate));
            pStmt.setInt(2, trayId);
            pStmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataException(e);
        }
    }
}