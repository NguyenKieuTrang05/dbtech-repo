package de.htwberlin.dbtech.aufgaben.ue02;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import de.htwberlin.dbtech.exceptions.CoolingSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.htwberlin.dbtech.exceptions.DataException;

/**
 * JDBC-Implementierung für das Kühlsystem.
 *
 * Diese Klasse stellt Methoden bereit, um auf die Datenbank zuzugreifen
 * und Proben (Samples) sowie Tabletts (Trays) zu verwalten.
 */
public class CoolingJdbc implements ICoolingJdbc {

    private static final Logger L = LoggerFactory.getLogger(CoolingJdbc.class);
    private Connection connection;

    @Override
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    /**
     * Liefert die aktive Datenbankverbindung zurück.
     *
     * Falls keine Verbindung gesetzt wurde, wird eine Exception geworfen.
     *
     * @return aktive Datenbankverbindung
     * @throws DataException wenn keine Verbindung gesetzt wurde
     */
    @SuppressWarnings("unused")
    private Connection useConnection() {
        // Prüfen, ob eine Verbindung gesetzt wurde
        if (connection == null) {
            throw new DataException("Connection not set");
        }
        return connection;
    }

    /**
     * Liest alle vorhandenen Probenarten (SampleKinds) aus der Tabelle SampleKind.
     *
     * Die Ergebnisse werden alphabetisch sortiert zurückgegeben.
     *
     * @return Liste aller Probenarten
     * @throws DataException bei einem Datenbankfehler
     */

    @Override
    public List<String> getSampleKinds() {
        PreparedStatement pStmt = null;
        ResultSet rs = null;
        List<String> sampleKind = null;

        try {
            String sql = "Select text from samplekind order by text asc";
            sampleKind = new LinkedList<String>();
            pStmt = useConnection().prepareStatement(sql);
            rs = pStmt.executeQuery();
            while (rs.next()) {
                sampleKind.add(rs.getString("text"));
            }
        }
        // SQL-Fehler in eigene Exception umwandeln
        catch (SQLException e) {
            throw new DataException(e);
        }
        return sampleKind;
    }

    /**
     * Sucht eine Probe (Sample) anhand ihrer ID.
     * Wird eine Probe (Sample) gefunden, so wird ein Sample-Objekt erstellt
     * und mit den Daten aus der Datenbank gefüllt.
     *
     * @param sampleId - der Primärschlüssel der gesuchten Probe (Sample)
     * @return das gefundene Sample-Objekt mit ihrer ID und Ablaufdatum
     * @throws CoolingSystemException falls keine Probe mit dieser ID existiert
     * @throws DataException bei einem Datenbankfehler
     */
    @Override
    public Sample findSampleById(Integer sampleId) {
        L.info("findSampleById: sampleId: " + sampleId);
        PreparedStatement pStmt = null;
        ResultSet rs = null;

        try {
            // Probe mit anhand des Primärschlüssels (SampleId) suchen
            String sql = "Select * from Sample where SampleID = ?";
            pStmt = useConnection().prepareStatement(sql);
            pStmt.setInt(1, sampleId);
            rs = pStmt.executeQuery();

            if (rs.next()) {
                // Wenn Probe vorhanden ist: Daten aus der gefundenen Probe in ein Sample-Objekt speichern
                Sample sample = new Sample();
                sample.setSampleId(rs.getInt("SampleID"));
                sample.setExpirationDate(rs.getDate("ExpirationDate").toLocalDate());

                return sample;
            } else {
                // Exception werfen, wenn eine Probe mit dieser ID nicht exisitiert
                throw new CoolingSystemException("Sample existiert nicht: " + sampleId);
            }

        }
        // SQL-Fehler in eigene Exception umwandeln
        catch (SQLException e) {
            throw new DataException(e);
        }

    }

    /**
     * Erstellt eine neue Probe.
     *
     * Zuerst wird geprüft, ob bereits eine Probe mit derselben SampleID existiert.
     * Danach wird geprüft, ob die angegebene Probenart existiert.
     * Gültigkeitsdauer der Probenart auslesen.
     * Ablaufdatum der Probe berechnen.
     * Anschließend wird die neue Probe in die Tabelle Sample eingefügt.
     *
     * @param sampleId  -   Primaerschuessel der Probe.
     * @param sampleKindId - Fremdschluessel auf die Probenart.
     * @throws CoolingSystemException wenn die Probe bereits existiert oder die Probenart nicht gefunden wird
     * @throws DataException bei einem Datenbankfehler
     */
    @Override
    public void createSample(Integer sampleId, Integer sampleKindId) {
        L.info("createSample: sampleId: " + sampleId + ", sampleKindId: " + sampleKindId);
        PreparedStatement pStmt = null;
        ResultSet rs = null;

        try {
            // Prüfen, ob eine Probe mit dieser ID bereits existiert
            String sql = "Select * from Sample where SampleID = ?";
            pStmt = useConnection().prepareStatement(sql);
            pStmt.setInt(1, sampleId);
            rs  = pStmt.executeQuery();

            // Probe bereits gefunden -> Exception werfen
            if (rs.next()) {
                throw new CoolingSystemException("Sample existiert bereits: " + sampleId);
            }
            // Gültigkeitstage der Probenart aus der Tabelle SampleKind lesen
            String sql2 = "Select ValidNoOfDays from SampleKind where SampleKindID = ?";
            pStmt = useConnection().prepareStatement(sql2);
            pStmt.setInt(1, sampleKindId);
            rs = pStmt.executeQuery();

            // Wenn die Probenart existiert -> Anzahl gültiger Tage speichern
            int validNoOfDays;
            if (rs.next()) {
                 validNoOfDays = rs.getInt("ValidNoOfDays");
            } else {
                throw new CoolingSystemException("SampleKind existiert nicht: " + sampleKindId);
            }
            // Ablaufdatum berechnen: heutiges Datum + Gültigkeitstage der Probe
            LocalDate expirationDate = LocalDate.now().plusDays(validNoOfDays);

            // Neue Probe in die Tabelle Sample einfügen
            String sql3 = "Insert into Sample (SampleID, SampleKindID, ExpirationDate) values (?, ?, ?)";
            pStmt = useConnection().prepareStatement(sql3);
            pStmt.setInt(1, sampleId);
            pStmt.setInt(2, sampleKindId);
            pStmt.setDate(3, java.sql.Date.valueOf(expirationDate));
            pStmt.executeUpdate();

        }
        // SQL-Fehler in eigene Exception umwandeln
        catch (SQLException e) {
            throw new DataException(e);
        }

    }
    /**
     * Leert ein Tablett vollständig und löscht alle zugehörigen Proben.
     *
     * Zuerst wird geprüft, ob das Tablett existiert.
     * Dann alle SampleIDs des Tabletts auslesen und speichern.
     * Anschließend werden alle Einträge aus der Tabelle Place für dieses Tablett gelöscht.
     * Zum Schluss werden die zugehörigen Proben aus der Tabelle Sample gelöscht.
     *
     * @param trayId - Primaerschuessel des Tabletts.
     * @throws CoolingSystemException - wenn das Tablett nicht existiert
     * @throws DataException bei einem Datenbankfehler
     */
    @Override
    public void clearTray(Integer trayId) {
        L.info("clearTray: trayId: " + trayId);
        PreparedStatement pStmt = null;
        ResultSet rs = null;

        try {
            // Prüfen ob Tray existiert
            String checkTraySql = "Select * from Tray where TrayID = ?";
            pStmt = useConnection().prepareStatement(checkTraySql);
            pStmt.setInt(1, trayId);
            rs = pStmt.executeQuery();

            // Wenn kein Tray gefunden wurde -> Exception werfen
            if (!rs.next()) {
                throw new CoolingSystemException("Tray existiert nicht");
            }

            // Alle vorhandenen SampleIDs des Tabletts speichern
            List<Integer> sampleIds = new ArrayList<>();
            String selectSampleIdsSql = "Select SampleID from Place where TrayID = ? and SampleID is not null";
            pStmt = useConnection().prepareStatement(selectSampleIdsSql);
            pStmt.setInt(1, trayId);
            rs = pStmt.executeQuery();
            // Alle gefundenen SampleIDs in die Liste einfügen
            while (rs.next()) {
                sampleIds.add(rs.getInt("SampleID"));
            }

            // Alle Place-Einträge des Tabletts löschen
            String deletePlaceSql = "Delete from Place where TrayID = ?";
            pStmt = useConnection().prepareStatement(deletePlaceSql);
            pStmt.setInt(1, trayId);
            pStmt.executeUpdate();

            // Zugehörige Samples löschen
            String deleteSampleSql = "Delete from Sample where SampleID = ?";
            for (Integer sampleId : sampleIds) {
                pStmt = useConnection().prepareStatement(deleteSampleSql);
                pStmt.setInt(1, sampleId);
                pStmt.executeUpdate();
            }

        }
        // SQL-Fehler in eigene Exception umwandeln
        catch (SQLException e) {
            throw new DataException(e);
        }

    }

    /***
     * liefert den nächsten Primärschlüssel als Tabelle
     *
     * @param columname - der Spaltename
     * @param tablename - der Tabellename
     * @return id - der nächste
     */

    public int getNextID(String columname, String tablename) {
        int id = 0;
        String sql = "Select max(" + columname + ")+1 from " + tablename;
        return id;
    }

}
