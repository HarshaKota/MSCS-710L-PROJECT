import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;

import java.sql.*;
import java.util.List;
import java.util.Map;

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

        createProcessInfoTable();

        createProcessTable();

        createNetworkTable();

        createSessionTable();
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

        if(TableCreationChecks.checkPowerSource(hal.getPowerSources())) {

            try {
                Statement powerTableStatement = connection.createStatement();
                String sql =
                        "CREATE TABLE IF NOT EXISTS POWER " +
                                "(TIMESTAMP         INTEGER PRIMARY KEY   NOT NULL," +
                                "POWERSTATUS        INTEGER               NOT NULL," +
                                "BATTERYPERCENTAGE  REAL                  NOT NULL)";
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
            String fanStatement0 = ",FAN";
            String fanStatement1 = " INTEGER NOT NULL";

            for (int i = 1; i < noOfFans; i++) {
                fanColumnStatement.append(fanStatement0).append(i).append(fanStatement1).append(",");
            }
            fanColumnStatement.append(fanStatement0).append(noOfFans).append(fanStatement1);
        }

        // Builds the cpu voltage column
        double cpuVoltage = TableCreationChecks.getCpuVoltage(hal.getSensors());
        StringBuilder cpuVoltageColumn = new StringBuilder();

        if (cpuVoltage != 999.0) {
            String cpuVoltageStatement0 = ",CPUVOLTAGE";
            String cpuVoltageStatement1 = " REAL NOT NULL";
            cpuVoltageColumn.append(cpuVoltageStatement0).append(cpuVoltageStatement1);
        }

        // Create the table
        try {
            Statement sensorsTableStatement = connection.createStatement();
            String sql =
                    "CREATE TABLE IF NOT EXISTS SENSORS " +
                            "(TIMESTAMP             INTEGER PRIMARY KEY   NOT NULL," +
                            "CPUTEMPERATURECELCIUS  REAL                  NOT NULL" +
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
                            "USEDMEMORY      REAL                  NOT NULL," +
                            "TOTALMEMORY     REAL                  NOT NULL)";
            memoryTableStatement.executeUpdate(sql);
            memoryTableStatement.close();
        } catch (Exception e) {
            log.error("Failed to create Memory Table " + e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    // Create a Processes table
    private void createProcessInfoTable(){

        try {
            Statement processesTableStatement = connection.createStatement();
            String sql =
                    "CREATE TABLE IF NOT EXISTS PROCESSINFO " +
                            "(TIMESTAMP         INTEGER      NOT NULL," +
                            "PROCESSNAME        VARCHAR      NOT NULL," +
                            "CPUPERCENTAGE      REAL         NOT NULL," +
                            "MEMORYPERCENTAGE   REAL         NOT NULL," +
                                               "PRIMARY KEY(TIMESTAMP, PROCESSNAME))";
            processesTableStatement.executeUpdate(sql);
            processesTableStatement.close();
        } catch (Exception e) {
            log.error("Failed to create ProcessInfo Table " + e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    // Create a ProcessorInfo table
    private void createProcessTable(){

        try {
            Statement processorInfoTableStatement = connection.createStatement();
            String sql =
                    "CREATE TABLE IF NOT EXISTS PROCESS " +
                            "(TIMESTAMP     INTEGER  PRIMARY KEY    NOT NULL," +
                            "NOOFPROCESSES  INTEGER                 NOT NULL," +
                            "NOOFTHREADS    INTEGER                 NOT NULL)" ;
            processorInfoTableStatement.executeUpdate(sql);
            processorInfoTableStatement.close();
        } catch (Exception e) {
            log.error("Failed to create Process Table " + e.getClass().getName() + ": " + e.getMessage());
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

    // Create a Network table
    private void createNetworkTable(){

        try {
            Statement networkTableStatement = connection.createStatement();
            String sql =
                    "CREATE TABLE IF NOT EXISTS NETWORK " +
                            "(TIMESTAMP              INTEGER  PRIMARY KEY     NOT NULL," +
                            "NOOFPACKETSRECEIVED     INTEGER                  NOT NULL," +
                            "NOOFPACKETSSENT         INTEGER                  NOT NULL," +
                            "SIZERECEIVED            TEXT                     NOT NULL," +
                            "SIZESENT                TEXT                     NOT NULL)";
            networkTableStatement.executeUpdate(sql);
            networkTableStatement.close();
        } catch (Exception e) {
            log.error("Failed to create Network Table " + e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    // Create a Sessions table to hold each user session start and end time
    private void createSessionTable() {

        try {
            Statement sessionTableStatement = connection.createStatement();
            String sql =
                    "CREATE TABLE IF NOT EXISTS SESSION " +
                            "(STARTSESSION  INTEGER  PRIMARY KEY      NOT NULL," +
                            "ENDSESSION     INTEGER                   NULL" +")";
            sessionTableStatement.executeUpdate(sql);
            sessionTableStatement.close();
        } catch (Exception e) {
            log.error("Failed to create Session Table " + e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }

    }

    // Insert values into Power Table
    void insertIntoPowerTable(MetricCollectionStructures.powerStructure pS){

        if (Main.hasPowerSource) {
            try {
                Statement insertIntoPowerTableStatement = connection.createStatement();
                String sql =
                        "INSERT INTO POWER VALUES (" + pS.getTimestamp() + "," + pS.getPowerStatus() + "," + pS.getBatteryPercentage() + ")";
                insertIntoPowerTableStatement.executeUpdate(sql);
                insertIntoPowerTableStatement.close();
            } catch (Exception e) {
                log.error("Failed to insert into Power Table " + e.getClass().getName() + ": " + e.getMessage());
                System.exit(0);
            }
        }
    }

    // Insert values into CPU Table
    void insertIntoCpuTable(MetricCollectionStructures.cpuStructure cS) {
        StringBuilder processorData = new StringBuilder();

        for (int i=0; i<cS.processorLoad.size()-1; i++) {
            processorData.append(cS.processorLoad.get(i)).append(",");
        }
        processorData.append(cS.processorLoad.get(cS.processorLoad.size()-1));

        try {
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

    // Insert values into Sensors Table
    void insertIntoSensorsTable(MetricCollectionStructures.sensorsStructure sS) {

        StringBuilder fans = new StringBuilder();
        if (sS.getFans().length > 0) {
            for (int i = 0; i < sS.getFans().length - 1; i++) {
                fans.append(sS.fans[i]).append(",");
            }
            fans.append(sS.fans[sS.getFans().length - 1]);
        }

        try {
            Statement insertIntoSensorsTableStatement = connection.createStatement();
            String sql =
                    "INSERT INTO SENSORS VALUES ("+sS.getTimestamp()+"," +sS.getCpuTemperature() +
                            (sS.getCpuVoltage()== 999.0 ? "" : ",") +
                            (sS.getCpuVoltage()==999.0 ? "" : (sS.getCpuVoltage())+",") +
                            fans + ")";
            insertIntoSensorsTableStatement.executeUpdate(sql);
            insertIntoSensorsTableStatement.close();
        } catch (Exception e) {
            log.error("Failed to insert into Sensors Table " + e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    // Insert values into Memory Table
    void insertIntoMemoryTable(MetricCollectionStructures.memoryStructure mS) {

        try {
            Statement insertIntoMemoryTableStatement = connection.createStatement();
            String sql =
                    "INSERT INTO MEMORY VALUES ("+mS.getTimestamp()+"," + mS.getUsedMemory()+","
                    + mS.getTotalMemory() + ")";
            insertIntoMemoryTableStatement.executeUpdate(sql);
            insertIntoMemoryTableStatement.close();
        } catch (Exception e) {
            log.error("Failed to insert into Memory Table " + e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    // Insert values into Network Table
    void insertIntoNetworkTable(MetricCollectionStructures.networkStructure nS) {

        try {
            Statement insertIntoNetworkTableStatement = connection.createStatement();
            String sql =
                    "INSERT INTO NETWORK VALUES ("+nS.getTimestamp()+"," +nS.getPacketsReceived()+","
                            + nS.getPacketsSent()+"," +"'"+nS.getSizeReceived()+"'"+"," +"'"+nS.getSizeSent()+"'" +")";
            insertIntoNetworkTableStatement.executeUpdate(sql);
            insertIntoNetworkTableStatement.close();
        } catch (Exception e) {
            log.error("Failed to insert into Network Table " + e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    // Insert values into the Process and Processes tables
    void insertIntoProcessTable(MetricCollectionStructures.processStructure pS) {

        try {
            Statement insertIntoProcessTableStatement = connection.createStatement();
            String sql =
                    "INSERT INTO PROCESS VALUES ("+ pS.getTimestamp()+"," +pS.getNoOfProcesses()+","
                            +pS.getNoOfThreads() +")";
            insertIntoProcessTableStatement.executeUpdate(sql);
            insertIntoProcessTableStatement.close();
        } catch (Exception e) {
            log.error("Failed to insert into Process Table " + e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }

        for (Map.Entry<String, List<Double>> processInfo: pS.processesList.entrySet()) {

            try {
                Statement insertIntoProcessInfoTableStatement = connection.createStatement();
                String sql =
                        "INSERT INTO PROCESSINFO VALUES (" + pS.getTimestamp() + "," + "'"+processInfo.getKey()+"'" +","
                                +processInfo.getValue().get(0)+"," +processInfo.getValue().get(1) +")";
                insertIntoProcessInfoTableStatement.executeUpdate(sql);
                insertIntoProcessInfoTableStatement.close();
            } catch (Exception e) {
                log.error("Failed to insert into ProcessInfo Table " + e.getClass().getName() + ": " + e.getMessage());
                System.exit(0);
            }
        }
    }

    // Insert the start session time into the session table
    void insertStartSessionIntoSessionTable(long startSession) {

        try {
            Statement insertStartSessionIntoSessionTableStatement = connection.createStatement();
            String sql =
                    "INSERT INTO SESSION VALUES ("+ startSession +",NULL" +")";
            insertStartSessionIntoSessionTableStatement.executeUpdate(sql);
            insertStartSessionIntoSessionTableStatement.close();
        } catch (Exception e) {
            log.error("Failed to insert Start Session Time into Session Table " + e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    // Update the session end time of previously inserted startSession time
    void insertEndSessionIntoSessionTable(long startSession, long endSession) {

        String sql = "UPDATE SESSION SET ENDSESSION = ? WHERE STARTSESSION = ?";

        try {

            PreparedStatement insertEndSessionIntoSessionTableStatement = connection.prepareStatement(sql);

            insertEndSessionIntoSessionTableStatement.setLong(1, endSession);
            insertEndSessionIntoSessionTableStatement.setLong(2,startSession);

            insertEndSessionIntoSessionTableStatement.executeUpdate();
            insertEndSessionIntoSessionTableStatement.close();
        } catch (Exception e) {
            log.error("Failed to update the endSession time in Session Table " + e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    // Close the connection to the sqlite database
    void closeDatabaseConnection() {
        try {
            connection.close();
        } catch (SQLException e) {
            log.error("Failed to close database connection " + e.getClass().getName() + ": " + e.getMessage());
        }
    }
}