package de.htwberlin.dbtech.aufgaben.ue03;

import java.sql.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDate;

import de.htwberlin.dbtech.exceptions.CoolingSystemException;
import de.htwberlin.dbtech.exceptions.DataException;

import de.htwberlin.dbtech.aufgaben.ue03.dao.SampleDao;
import de.htwberlin.dbtech.aufgaben.ue03.dao.SampleDaoImpl;
import de.htwberlin.dbtech.aufgaben.ue03.dao.TrayDao;
import de.htwberlin.dbtech.aufgaben.ue03.dao.TrayDaoImpl;
import de.htwberlin.dbtech.aufgaben.ue03.dao.PlaceDao;
import de.htwberlin.dbtech.aufgaben.ue03.dao.PlaceDaoImpl;

/**
 * Service-Klasse zur Verwaltung des Einlagerungsprozesses von Proben.
 * Die Klasse enthält die Geschäftslogik und verwendet DAO-Klassen
 * für den Datenbankzugriff.
 *
 * @author Kieu; Viona
 */

public class CoolingService implements ICoolingService {
    private static final Logger L = LoggerFactory.getLogger(CoolingService.class);
    private Connection connection;
    private SampleDao sampleDao;
    private TrayDao trayDao;
    private PlaceDao placeDao;

    /**
     * Setzt die Datenbankverbindung und initialisiert die benötigten DAO-Objekte.
     *
     * @param connection Verbindung zur Datenbank
     */

    @Override
    public void setConnection(Connection connection) {
        this.connection = connection;
        this.sampleDao = new SampleDaoImpl(connection);
        this.trayDao = new TrayDaoImpl(connection);
        this.placeDao = new PlaceDaoImpl(connection);
    }

    /**
     * Liefert die aktuelle Datenbankverbindung.
     *
     * @return Datenbankverbindung
     * @throws DataException falls keine Verbindung gesetzt wurde
     */

    @SuppressWarnings("unused")
    private Connection useConnection() {
        if (connection == null) {
            throw new DataException("Connection not set");
        }
        return connection;
    }

    @Override
    public void transferSample(Integer sampleId, Integer diameterInCM) {

        if (!sampleDao.isSampleIdExisting(sampleId))  {
           throw new CoolingSystemException("Sample id " + sampleId + " does not exist");
       }
        LocalDate sampleExpirationDate = sampleDao.getSampleExpirationDate(sampleId);
        Integer trayId = trayDao.findSuitableTray(sampleExpirationDate, diameterInCM);

       if (trayId == null) {
           trayId = trayDao.findEmptyTray(diameterInCM);

           if (trayId == null) {
               throw new CoolingSystemException("No suitable tray found");
           }
           trayDao.updateTrayExpirationDate(trayId, sampleExpirationDate.plusDays(30));
       }

        placeDao.updateSampleTray(sampleId, trayId);

    }

}


