package de.htwberlin.dbtech.aufgaben.ue03.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import de.htwberlin.dbtech.exceptions.CoolingSystemException;
import de.htwberlin.dbtech.exceptions.DataException;

/**
 * Implementierung des Data Access Objects für die Tabelle Sample.
 *
 * @author Kieu, Viona
 */


public class SampleDaoImpl implements SampleDao {
    private Connection connection;

    /**
     * Erstellt ein Sample-DAO mit Datenbankverbindung.
     *
     * @param connection Verbindung zur Datenbank
     */

    public SampleDaoImpl(Connection connection) {
        this.connection = connection;
    }

    /**
     * Setzt die Datenbankverbindung.
     *
     * @param connection Verbindung zur Datenbank
     */

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
     * Prüft, ob eine Probe mit der angegebenen ID existiert.
     *
     * @param sampleId ID der Probe
     * @return true, wenn die Probe existiert
     */

    @Override
    public boolean isSampleIdExisting(Integer sampleId) {

        PreparedStatement pStmt = null;
        ResultSet rs = null;
        String sql = "Select count(sampleId) as Anzahl from Sample where sampleId = ?";
        try {

            pStmt = useConnection().prepareStatement(sql);
            pStmt.setInt(1, sampleId);
            rs = pStmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("Anzahl") > 0;
            } else {

                return false;
            }
        } catch (SQLException e) {
            throw new DataException(e);
        }
    }
    /**
     * Liest das Ablaufdatum einer Probe aus der Datenbank.
     *
     * @param sampleId ID der Probe
     * @return Ablaufdatum der Probe
     */

    @Override
    public LocalDate getSampleExpirationDate(Integer sampleId) {
        PreparedStatement pStmt = null;
        ResultSet rs = null;

        try {
            String sql = "Select expirationDate from Sample where sampleId = ?";
            pStmt = useConnection().prepareStatement(sql);
            pStmt.setInt(1, sampleId);
            rs = pStmt.executeQuery();

            if (rs.next()) {
                return rs.getDate("ExpirationDate").toLocalDate();
            } else {
                throw new CoolingSystemException("Sample id " + sampleId + " exisitiert nicht.");
            }
        } catch (SQLException e) {
            throw new DataException(e);}
    }
}