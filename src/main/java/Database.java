import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;

import java.sql.*;

public class Database {

    private Connection connection = null;
    private final Logger log = LogManager.getLogger(Database.class);
    private static SystemInfo si;
    private static HardwareAbstractionLayer hal;

    public Database() {
        si = new SystemInfo();
        hal = si.getHardware();

        establishDatabaseConnection();

        createSensorsTable();

        createPowerTable();

        createCPUTable();

        createMemoryTable();

        createProcessesTable();

        createProcessorInfoTable();

        createDiskTable();

        createNetworkTable();

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

        if(TableCreationChecks.checkPowerTable(hal.getPowerSources())) {

            try {
                Statement powerTableStatement = connection.createStatement();
                String sql =
                        "CREATE TABLE IF NOT EXISTS POWER " +
                                "(TIMESTAMP INTEGER PRIMARY KEY   NOT NULL," +
                                "POWERSTATUS INTEGER              NOT NULL," +
                                "BATTERYPERCENTAGE REAL           NOT NULL)";
                powerTableStatement.executeUpdate(sql);
                powerTableStatement.close();
            } catch (Exception e) {
                log.error("Failed to create Power Table " + e.getClass().getName() + ": " + e.getMessage());
                System.exit(0);
            }
        } else {
            log.warn("There is no battery in this system. Power statistics will be unavailable");
        }
    }

    // Create a new Sensors table
    private void createSensorsTable(){

        // Builds the fan column
        int noOfFans = TableCreationChecks.getFans(hal.getSensors());
        StringBuilder fanColumnStatement = new StringBuilder();

        if (noOfFans > 0) {
            String fanStatement0 = "FAN";
            String fanStatement1 = " REAL NOT NULL";

            for (int i = 1; i < noOfFans; i++) {
                fanColumnStatement.append(fanStatement0).append(i).append(fanStatement1).append(",");
            }
            fanColumnStatement.append(fanStatement0).append(noOfFans).append(fanStatement1);
        }

        // Builds the cpu voltage column
        double cpuVoltage = TableCreationChecks.getCpuVoltage(hal.getSensors());
        StringBuilder cpuVoltageColumn = new StringBuilder();

        if (cpuVoltage > 0.0) {
            String cpuVoltageStatement0 = "CPUVOLTAGE";
            String cpuVoltageStatement1 = " REAL NOT NULL";
            cpuVoltageColumn.append(cpuVoltageStatement0).append(cpuVoltageStatement1);
        }

        // Create the table
        try {
            Statement sensorsTableStatement = connection.createStatement();
            String sql =
                    "CREATE TABLE IF NOT EXISTS SENSORS " +
                            "(TIMESTAMP             INTEGER PRIMARY KEY   NOT NULL," +
                            "CPUTEMPERATURECELCIUS  REAL                  NOT NULL," +
                            cpuVoltageColumn +
                            fanColumnStatement +")";
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
                            "(TIMESTAMP      INTEGER PRIMARY KEY   NOT NULL," +
                            "AVAILABLEMEMORY REAL                  NOT NULL," +
                            "TOTALMEMORY     REAL                  NOT NULL)";
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

    // Create a ProcessorInfo table
    private void createProcessorInfoTable(){

        try {
            Statement processorInfoTableStatement = connection.createStatement();
            String sql =
                    "CREATE TABLE IF NOT EXISTS PROCESSORINFO " +
                            "(TIMESTAMP     INTEGER  PRIMARY KEY    NOT NULL," +
                            "NOOFPROCESSES  INTEGER                 NOT NULL," +
                            "NOOFTHREADS    INTEGER                 NOT NULL)" ;
            processorInfoTableStatement.executeUpdate(sql);
            processorInfoTableStatement.close();
        } catch (Exception e) {
            log.error("Failed to create ProcessorInfo Table " + e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    // Create a CPU table
    private void createCPUTable(){

        int noOfLogicalCPUs = TableCreationChecks.getLogicalCPUs(hal.getProcessor());

        StringBuilder processorLoadColumnStatement = new StringBuilder();
        String processorLoadColumnStatement0 = "PROCESSOR";
        String processorLoadColumnStatement1 = "LOAD REAL NOT NULL";

        for (int i=1; i<noOfLogicalCPUs; i++) {
            processorLoadColumnStatement.append(processorLoadColumnStatement0).append(i).append(processorLoadColumnStatement1).append(",");
        }
        processorLoadColumnStatement.append(processorLoadColumnStatement0).append(noOfLogicalCPUs).append(processorLoadColumnStatement1);

        try {
            Statement cpuTableStatement = connection.createStatement();
            String sql =
                    "CREATE TABLE IF NOT EXISTS CPU " +
                            "(TIMESTAMP     INTEGER  PRIMARY KEY    NOT NULL," +
                            "UPTIME         INTEGER                 NOT NULL," +
                            "USERLOAD       REAL                    NOT NULL," +
                            "SYSTEMLOAD     REAL                    NOT NULL," +
                            "IDLELOAD       REAL                    NOT NULL," +
                            processorLoadColumnStatement + ")";
            cpuTableStatement.executeUpdate(sql);
            cpuTableStatement.close();
        } catch (Exception e) {
            log.error("Failed to create CPU Table " + e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    // Create a Disk table
    private void createDiskTable(){

        try {
            Statement diskTableStatement = connection.createStatement();
            String sql =
                    "CREATE TABLE IF NOT EXISTS DISK " +
                            "(TIMESTAMP         INTEGER      NOT NULL," +
                            "AVAILABLEDISKSPACE REAL         NOT NULL," +
                            "TOTALDISKSPACE     REAL         NOT NULL)";
            diskTableStatement.executeUpdate(sql);
            diskTableStatement.close();
        } catch (Exception e) {
            log.error("Failed to create Disk Table " + e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    // Create a Network table
    private void createNetworkTable(){

        try {
            Statement networkTableStatement = connection.createStatement();
            String sql =
                    "CREATE TABLE IF NOT EXISTS NETWORK " +
                            "(TIMESTAMP              INTEGER      NOT NULL," +
                            "NOOFPACKETSRECEIVED     REAL         NOT NULL," +
                            "NOOFPACKETSTRANSMITTED  REAL         NOT NULL," +
                            "SIZERECEIVED            REAL         NOT NULL," +
                            "SIZETRANSMITTED         REAL         NOT NULL)";
            networkTableStatement.executeUpdate(sql);
            networkTableStatement.close();
        } catch (Exception e) {
            log.error("Failed to create Network Table " + e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    // Insert values into Power Table
    public void insertIntoPowerTable(MetricCollectionStructures.powerStructure pS){
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:MetricCollector.db");
            Statement insertIntoPowerTableStatement = connection.createStatement();
            String sql =
                    "INSERT INTO POWER VALUES ("+pS.getTimestamp()+"," +pS.getPowerStatus()+"," +pS.getBatteryPercentage() + ")";
            insertIntoPowerTableStatement.executeUpdate(sql);
            insertIntoPowerTableStatement.close();
        } catch (Exception e) {
            log.error("Failed to insert into Power Table " + e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    // Insert values into CPU Table
    public void insertIntoCpuTable(MetricCollectionStructures.cpuStructure cS) {
        StringBuilder processorData = new StringBuilder();

        for (int i=0; i<cS.processorLoad.size()-1; i++) {
            processorData.append(cS.processorLoad.get(i)).append(",");
        }
        processorData.append(cS.processorLoad.get(cS.processorLoad.size()-1));

        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:MetricCollector.db");
            Statement insertIntoCpuTableStatement = connection.createStatement();
            String sql =
                    "INSERT INTO CPU VALUES ("+cS.getTimestamp()+"," +cS.getUptime()+"," +cS.getUserLoad()+","
                            +cS.getSystemLoad()+"," +cS.getIdelLoad()+"," + processorData + ")";
            insertIntoCpuTableStatement.executeUpdate(sql);
            insertIntoCpuTableStatement.close();
        } catch (Exception e) {
            log.error("Failed to insert into CPU Table " + e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    // Close the connection to the sqlite database
    private void closeDatabaseConnection() {
        try {
            connection.close();
        } catch (SQLException e) {
            log.error("Failed to close database connection " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

}