package de.htwberlin.dbtech.aufgaben.ue03.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import de.htwberlin.dbtech.exceptions.CoolingSystemException;
import de.htwberlin.dbtech.exceptions.DataException;

/**
 * Implementierung des Data Access Objects für die Tabelle Place.
 *
 * @author Kieu,Viona
 */

public class PlaceDaoImpl implements PlaceDao {
    private Connection connection;

    /**
     * Erstellt ein Place-DAO mit Datenbankverbindung.
     *
     * @param connection Verbindung zur Datenbank
     */

    public PlaceDaoImpl(Connection connection) {
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
     * Ermittelt die kleinste freie Platznummer eines Trays.
     *
     * @param trayId ID des Trays
     * @return Nummer des freien Platzes
     */

    @Override
    public Integer findSmallestFreePlaceNo(Integer trayId) {
        PreparedStatement pStmt = null;
        ResultSet rs = null;

        try {
            String sql = "Select capacity from Tray where trayId = ?";
            pStmt = useConnection().prepareStatement(sql);
            pStmt.setInt(1, trayId);
            rs = pStmt.executeQuery();

            if (!rs.next()) {

                throw new CoolingSystemException("Tray not found");

            }
            int capacity = rs.getInt("capacity");

            for (int placeNo = 1; placeNo <= capacity; placeNo++) {

                sql = "Select count(*) as Anzahl from Place where trayId = ? and placeNo = ?";
                pStmt = useConnection().prepareStatement(sql);
                pStmt.setInt(1, trayId);
                pStmt.setInt(2, placeNo);
                rs = pStmt.executeQuery();

                if (rs.next() && rs.getInt("Anzahl") == 0) {
                    return placeNo;

                }
            }
            throw new CoolingSystemException("No free place found");
        } catch (SQLException e) {
            throw new DataException(e);
        }
    }
    /**
     * Fügt eine Probe an der kleinsten freien Position eines Trays ein.
     *
     * @param sampleId ID der Probe
     * @param trayId ID des Trays
     */

    @Override
    public void updateSampleTray(Integer sampleId, Integer trayId) {
        PreparedStatement pStmt = null;

        try {
            Integer placeNo = findSmallestFreePlaceNo(trayId);
            String sql = "Insert into Place (sampleId, trayId, placeNo) values (?, ?, ?)";
            pStmt = useConnection().prepareStatement(sql);
            pStmt.setInt(1, sampleId);
            pStmt.setInt(2, trayId);
            pStmt.setInt(3, placeNo);

            pStmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataException(e);
        }
    }
}