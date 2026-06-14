package de.htwberlin.dbtech.aufgaben.ue03;

import java.sql.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import de.htwberlin.dbtech.exceptions.CoolingSystemException;
import de.htwberlin.dbtech.exceptions.DataException;


public class CoolingService implements ICoolingService {
    private static final Logger L = LoggerFactory.getLogger(CoolingService.class);
    private Connection connection;

    @Override
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    @SuppressWarnings("unused")
    private Connection useConnection() {
        if (connection == null) {
            throw new DataException("Connection not set");
        }
        return connection;
    }

    @Override
    public void transferSample(Integer sampleId, Integer diameterInCM) {

       if (!isSampleIdExisting(sampleId)) {
           throw new CoolingSystemException("Sample id " + sampleId + " does not exist");
       }
       LocalDate sampleExpirationDate = getSampleExpirationDate(sampleId);
       Integer trayId = findSuitableTray(sampleExpirationDate,diameterInCM);

       if (trayId == null) {
           trayId = findEmptyTray(diameterInCM);

           if (trayId == null) {
               throw new CoolingSystemException("No suitable tray found");
           }
           updateTrayExpirationDate(trayId, sampleExpirationDate.plusDays(30));
       }

       updateSampleTray(sampleId, trayId);

    }
    /**
     * pr�ft, ob die Probe in der DB existiert
     *
     * @param sampleId
     *            - der Primaerschluessel der Probe
     * @return true - Probe existiert | false - Probe existiert nicht
     *
     * @author Kieu
     * **/
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
    private LocalDate getSampleExpirationDate(Integer sampleId) {
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

    private Integer findSuitableTray(LocalDate expirationDate, Integer diameterInCM) {
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
    private Integer findEmptyTray(Integer diameterInCM) {
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

    private void updateTrayExpirationDate(Integer trayId, LocalDate expirationDate) {
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
    private Integer findSmallestFreePlaceNo(Integer trayId) {
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
    private void updateSampleTray(Integer sampleId, Integer trayId) {
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


