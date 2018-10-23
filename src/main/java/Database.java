import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;

import java.sql.*;

public class Database {

    private Connection connection = null;
    private final Logger log = LogManager.getLogger(Database.class);

    public Database() {
        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();

        establishDatabaseConnection();

        if (TableCreationChecks.getBatteryPercentage(hal.getPowerSources())) {
            createPowerTable();
        }
    }

    // Establish connection to the sqlite database/ Create if its doesn't exist
    private void establishDatabaseConnection(){
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:MetricCollector.db");
        } catch (Exception e) {
            log.error("Failed to connect to the database " + e.getClass().getName() + ": " + e.getMessage());
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    // Create a Power Sources table
    private void createPowerTable(){

        try {
            Statement powerTableStatement = connection.createStatement();
            String sql =
                    "CREATE TABLE IF NOT EXISTS POWER " +
                            "(TIMESTAMP INTEGER PRIMARY KEY   NOT NULL," +
                            "BATTERYPERCENTAGE INTEGER        NOT NULL)";
            powerTableStatement.executeUpdate(sql);
            powerTableStatement.close();
            connection.close();
        } catch (Exception e) {
            log.error("Failed to create Power Table " + e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }


}