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

        int noOfFans = TableCreationChecks.getFans(hal.getSensors());
        if (noOfFans >= 1) {
            createSensorsTable(noOfFans);
        }

        createMemoryTable();
        createProcessesTable();

        closeDatabaseConnection();
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
        } catch (Exception e) {
            log.error("Failed to create Power Table " + e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    // Create a new Sensors table
    private void createSensorsTable(int noOfFans){

        StringBuilder fanColumnStatement = new StringBuilder();
        String fanStatement0 = "FAN";
        String fanStatement1 = " REAL NOT NULL";

        for (int i=1; i<noOfFans; i++) {
            fanColumnStatement.append(fanStatement0).append(i).append(fanStatement1).append(",");
        }
        fanColumnStatement.append(fanStatement0).append(noOfFans).append(fanStatement1);

        try {
            Statement sensorsTableStatement = connection.createStatement();
            String sql =
                    "CREATE TABLE IF NOT EXISTS SENSORS " +
                            "(TIMESTAMP INTEGER PRIMARY KEY   NOT NULL," + fanColumnStatement +")";
            sensorsTableStatement.executeUpdate(sql);
            sensorsTableStatement.close();
        } catch (Exception e) {
            log.error("Failed to create Sensor Table " + e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    // Create a Memory table
    private void createMemoryTable(){

        try {
            Statement memoryTableStatement = connection.createStatement();
            String sql =
                    "CREATE TABLE IF NOT EXISTS MEMORY " +
                            "(TIMESTAMP INTEGER PRIMARY KEY   NOT NULL," +
                            "MEMORYUSED REAL                  NOT NULL," +
                            "SWAPUSED   REAL                  NOT NULL)";
            memoryTableStatement.executeUpdate(sql);
            memoryTableStatement.close();
        } catch (Exception e) {
            log.error("Failed to create Memory Table " + e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    // Create a Processes table
    private void createProcessesTable(){

        try {
            Statement processesTableStatement = connection.createStatement();
            String sql =
                    "CREATE TABLE IF NOT EXISTS PROCESSES " +
                            "(TIMESTAMP       INTEGER      NOT NULL," +
                            "PROCESSNAME      VARCHAR      NOT NULL," +
                            "CPUPERCENTAGE    REAL         NOT NULL," +
                            "MEMORYPERCENTAGE REAL         NOT NULL," +
                            "PRIMARY KEY(TIMESTAMP, PROCESSNAME))";
            processesTableStatement.executeUpdate(sql);
            processesTableStatement.close();
        } catch (Exception e) {
            log.error("Failed to create Processes Table " + e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    private void closeDatabaseConnection() {
        try {
            connection.close();
        } catch (SQLException e) {
            log.error("Failed to close database connection " + e.getClass().getName() + ": " + e.getMessage());
        }
    }
}