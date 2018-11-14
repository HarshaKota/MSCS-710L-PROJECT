import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;

import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Database {

    private Connection connection = null;
    private final Logger log = LogManager.getLogger(Database.class);
    private final SystemInfo si = new SystemInfo();
    private final HardwareAbstractionLayer hal = si.getHardware();


    public Database(String databaseUrl) throws Exception {

        String databaseClassName = "org.sqlite.JDBC";

        establishDatabaseConnection(databaseClassName, databaseUrl);

        createSensorsTable();

        createPowerTable();

        createCPUTable();

        createMemoryTable();

        createProcessInfoTable();

        createProcessTable();

        createNetworkTable();

        createSessionTable();
    }

    public Database() { }

    // Establish connection to the sqlite database/ Create if its doesn't exist
    void establishDatabaseConnection(String databaseClassName, String databaseUrl) throws Exception {
        try {
            Class.forName(databaseClassName);
            connection = DriverManager.getConnection(databaseUrl);
            connection.setAutoCommit(false);
        } catch (NullPointerException | SQLException | ClassNotFoundException e) {
            log.error("establishDatabaseConnection: Failed to connect to the database " + e.getClass().getName() + ": " + e.getMessage());
            throw new Exception(e);
        }
    }

    // Check if the session table is intact with both the startSession and endSession times
    void checkSessionTable() throws Exception {

        // Get the start and end times of the last session inserted into the session table
            String selectSql = "SELECT * FROM SESSION ORDER BY STARTSESSION DESC LIMIT 1";
            long startSessionTime = 0;
            long endSessionTime = 0;

        try {
            Statement checkSessionTableStatement = connection.createStatement();
            ResultSet rs = checkSessionTableStatement.executeQuery(selectSql);
            while(rs.next()) {
                startSessionTime = rs.getLong("STARTSESSION");
                endSessionTime = rs.getLong("ENDSESSION");
            }
            checkSessionTableStatement.close();
        } catch (Exception e) {
            log.error("Failed to fetch the last inserted session time " + e.getClass().getName() + ": " + e.getMessage());
            throw new NullPointerException("Failed to fetch the last inserted session time");
        }

        // Check and make sure the timestamp values are the same using 2 different table
        if (endSessionTime == 0) {

            long sensorLastTimestamp = 0;
            long memoryLastTimestamp = 0;

            String getLastTimeFromSensorsSql = "SELECT TIMESTAMP FROM SENSORS ORDER BY TIMESTAMP DESC LIMIT 1";
            String getLastTimeFromMemorySql = "SELECT TIMESTAMP FROM MEMORY ORDER BY TIMESTAMP DESC LIMIT 1";

            try {
                Statement getEndTimeFromSensorsStatement = connection.createStatement();
                ResultSet rs = getEndTimeFromSensorsStatement.executeQuery(getLastTimeFromSensorsSql);
                while(rs.next()) {
                    sensorLastTimestamp = rs.getLong("TIMESTAMP");
                }
                getEndTimeFromSensorsStatement.close();
            } catch (Exception e) {
                log.error("Failed to last timestamp from the sensors table " + e.getClass().getName() + ": " + e.getMessage());
                throw new Exception(e);
            }

            try {
                Statement getEndTimeFromMemoryStatement = connection.createStatement();
                ResultSet rs = getEndTimeFromMemoryStatement.executeQuery(getLastTimeFromMemorySql);
                while(rs.next()) {
                    memoryLastTimestamp = rs.getLong("TIMESTAMP");
                }
                getEndTimeFromMemoryStatement.close();
            } catch (Exception e) {
                log.error("Failed to last timestamp from the memory table " + e.getClass().getName() + ": " + e.getMessage());
                throw new Exception(e);
            }

            if (sensorLastTimestamp != memoryLastTimestamp) {
                log.fatal("The 2 tables - sensors and memory have different number of records in them.");
                throw new Exception("The 2 tables - sensors and memory have different number of records in them.");
            } else {
                String fixEndSessionTimeSql = "UPDATE SESSION SET ENDSESSION = ? WHERE STARTSESSION = ?";
                try {
                    PreparedStatement fixEndSesionTimeStatement = connection.prepareStatement(fixEndSessionTimeSql);

                    fixEndSesionTimeStatement.setLong(1, sensorLastTimestamp);
                    fixEndSesionTimeStatement.setLong(2, startSessionTime);

                    fixEndSesionTimeStatement.executeUpdate();
                    fixEndSesionTimeStatement.close();
                    connection.commit();
                } catch (Exception e) {
                    log.error("Failed to fix the endSession time in Session Table " + e.getClass().getName() + ": " + e.getMessage());
                    throw new Exception(e);
                }

            }
        }
    }

    // Create a Power Sources table
    void createPowerTable() throws Exception {

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
                throw new Exception(e);
            }
        } else {
            log.warn("There is no battery in this system. Power statistics will be unavailable");
        }
    }

    // Create a new Sensors table
    void createSensorsTable() throws Exception {

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
            throw new Exception(e);
        }
    }

    // Create a Memory table
    void createMemoryTable() throws Exception {

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
            throw new Exception(e);
        }
    }

    // Create a Processes table
    private void createProcessInfoTable() throws Exception {

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
            throw new Exception(e);
        }
    }

    // Create a ProcessorInfo table
    void createProcessTable() throws Exception {

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
            throw new Exception(e);
        }
    }

    // Create a CPU table
    void createCPUTable() throws Exception {

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
            throw new Exception(e);
        }
    }

    // Create a Network table
    void createNetworkTable() throws Exception {

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
            throw new Exception(e);
        }
    }

    // Create a Sessions table to hold each user session start and end time
    void createSessionTable() throws Exception {

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
            throw new Exception(e);
        }

    }

    // Insert values into Power Table
    void insertIntoPowerTable(MetricCollectionStructures.powerStructure pS) throws Exception {

        //Check if the received powerStructure is not empty
        if (pS.getTimestamp() == 0 || pS.getBatteryPercentage() == 0d) {
            log.error("Empty powerStructure used to insert into power table");
            throw new Exception("Empty powerStructure used to insert into power table");
        }

        if (Main.hasPowerSource) {
            try {
                Statement insertIntoPowerTableStatement = connection.createStatement();
                String sql =
                        "INSERT INTO POWER VALUES (" + pS.getTimestamp() + "," + pS.getPowerStatus() + "," + pS.getBatteryPercentage() + ")";
                insertIntoPowerTableStatement.executeUpdate(sql);
                insertIntoPowerTableStatement.close();
            } catch (Exception e) {
                log.error("Failed to insert into Power Table " + e.getClass().getName() + ": " + e.getMessage());
                throw new Exception(e);
            }
        }
    }

    // Insert values into CPU Table
    void insertIntoCpuTable(MetricCollectionStructures.cpuStructure cS) throws Exception {

        if (cS.getTimestamp() == 0 || cS.getUptime() == 0 || cS.getUserLoad() == 0d || cS.getSystemLoad() == 0d ||
                cS.getIdleLoad() == 0d)
        {
                log.error("Empty cpuStructure used to insert into CPU table ");
                throw new Exception("Empty cpuStructure used to insert into CPU table ");
        }

        if (cS.getProcessorLoad() == null) {
            log.error("Empty cpuStructure used to insert into CPU table: Empty getProcessorLoad ");
            throw new Exception("Empty cpuStructure used to insert into CPU table: Empty getProcessorLoad ");
        }

        if (cS.getProcessorLoad().size() != hal.getProcessor().getLogicalProcessorCount()) {
            log.error("No of processor loads are less than logicalProcessorCount ");
            throw new Exception("No of processor loads are less than logicalProcessorCount ");
        }

        StringBuilder processorData = new StringBuilder();

        for (int i = 0; i < cS.processorLoad.size() - 1; i++) {
            processorData.append(cS.processorLoad.get(i)).append(",");
        }
        processorData.append(cS.processorLoad.get(cS.processorLoad.size() - 1));

        try {
            Statement insertIntoCpuTableStatement = connection.createStatement();
            String sql =
                    "INSERT INTO CPU VALUES ("+cS.getTimestamp()+"," +cS.getUptime()+"," +cS.getUserLoad()+","
                            +cS.getSystemLoad()+"," +cS.getIdleLoad()+"," + processorData + ")";
            insertIntoCpuTableStatement.executeUpdate(sql);
            insertIntoCpuTableStatement.close();
        } catch (Exception e) {
            log.error("Failed to insert into CPU Table " + e.getClass().getName() + ": " + e.getMessage());
            throw new Exception(e);
        }
    }

    // Insert values into Sensors Table
    void insertIntoSensorsTable(MetricCollectionStructures.sensorsStructure sS) throws Exception {

        if (sS.getTimestamp() ==  0 || sS.getCpuTemperature() == 0d)
        {
            log.error("Empty sensorsStructure used to insert into Sensors table ");
            throw new Exception("Empty sensorsStructure used to insert into Sensors table ");
        }

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
            throw new Exception(e);
        }
    }

    // Insert values into Memory Table
    void insertIntoMemoryTable(MetricCollectionStructures.memoryStructure mS) throws Exception {

        if (mS.getTimestamp() ==  0 || mS.getUsedMemory() == 0d || mS.getTotalMemory() == 0d)
        {
            log.error("Empty memoryStructure used to insert into Memory table ");
            throw new Exception("Empty memoryStructure used to insert into Memory table ");
        }

        try {
            Statement insertIntoMemoryTableStatement = connection.createStatement();
            String sql =
                    "INSERT INTO MEMORY VALUES ("+mS.getTimestamp()+"," + mS.getUsedMemory()+","
                    + mS.getTotalMemory() + ")";
            insertIntoMemoryTableStatement.executeUpdate(sql);
            insertIntoMemoryTableStatement.close();
        } catch (Exception e) {
            log.error("Failed to insert into Memory Table " + e.getClass().getName() + ": " + e.getMessage());
            throw new Exception(e);
        }
    }

    // Insert values into Network Table
    void insertIntoNetworkTable(MetricCollectionStructures.networkStructure nS) throws Exception {

        if (nS.getTimestamp() ==  0 || nS.getPacketsReceived() == 0 || nS.getPacketsSent() == 0 ||
                nS.getSizeReceived().equals("") || nS.getSizeSent().equals(""))
        {
            log.error("Empty networkStructure used to insert into Network table ");
            throw new Exception("Empty networkStructure used to insert into Network table ");
        }

        try {
            Statement insertIntoNetworkTableStatement = connection.createStatement();
            String sql =
                    "INSERT INTO NETWORK VALUES ("+nS.getTimestamp()+"," +nS.getPacketsReceived()+","
                            + nS.getPacketsSent()+"," +"'"+nS.getSizeReceived()+"'"+"," +"'"+nS.getSizeSent()+"'" +")";
            insertIntoNetworkTableStatement.executeUpdate(sql);
            insertIntoNetworkTableStatement.close();
        } catch (Exception e) {
            log.error("Failed to insert into Network Table " + e.getClass().getName() + ": " + e.getMessage());
            throw new Exception(e);
        }
    }

    // Insert values into the Process and Processes tables
    void insertIntoProcessTable(MetricCollectionStructures.processStructure pS) throws Exception {

        if (pS.getTimestamp() ==  0 || pS.getNoOfThreads() == 0 || pS.getNoOfProcesses() == 0)
        {
            log.error("Empty processStructure used to insert into Process table ");
            throw new Exception("Empty processStructure used to insert into Process table ");
        }

        if (pS.getProcessesList() == null) {
            log.error("Empty processStructure used to insert into Processes table: processesList Null ");
            throw new Exception("Empty processStructure used to insert into Processes table: processesList Null ");
        }
        HashMap<String, List<Double>> processesList = pS.getProcessesList();
        for (Map.Entry<String, List<Double>> pair: ((Map<String, List<Double>>) processesList).entrySet()) {
            if (pair.getKey().equals("") || pair.getValue().get(0) == 0d || pair.getValue().get(1) == 0) {
                log.error("Empty processStructure used to insert into Processes table: Empty processesList ");
                throw new Exception("Empty processStructure used to insert into Processes table: Empty processesList ");
            }
        }

        try {
            Statement insertIntoProcessTableStatement = connection.createStatement();
            String sql =
                    "INSERT INTO PROCESS VALUES ("+ pS.getTimestamp()+"," +pS.getNoOfProcesses()+","
                            +pS.getNoOfThreads() +")";
            insertIntoProcessTableStatement.executeUpdate(sql);
            insertIntoProcessTableStatement.close();
        } catch (Exception e) {
            log.error("Failed to insert into Process Table " + e.getClass().getName() + ": " + e.getMessage());
            throw new Exception(e);
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
                throw new Exception(e);
            }
        }
    }

    // Insert the start session time into the session table
    void insertStartSessionIntoSessionTable(final long startSession) throws Exception {

        try {
            Statement insertStartSessionIntoSessionTableStatement = connection.createStatement();
            String sql =
                    "INSERT INTO SESSION VALUES ("+ startSession +",NULL" +")";
            insertStartSessionIntoSessionTableStatement.executeUpdate(sql);
            insertStartSessionIntoSessionTableStatement.close();
        } catch (Exception e) {
            log.error("Failed to insert Start Session Time into Session Table " + e.getClass().getName() + ": " + e.getMessage());
            throw new Exception(e);
        }
    }

    // Update the session end time of previously inserted startSession time
    void insertEndSessionIntoSessionTable(final long startSession, final long endSession) throws Exception {

        String sql = "UPDATE SESSION SET ENDSESSION = ? WHERE STARTSESSION = ?";

        try {

            PreparedStatement insertEndSessionIntoSessionTableStatement = connection.prepareStatement(sql);

            insertEndSessionIntoSessionTableStatement.setLong(1, endSession);
            insertEndSessionIntoSessionTableStatement.setLong(2,startSession);

            insertEndSessionIntoSessionTableStatement.executeUpdate();
            insertEndSessionIntoSessionTableStatement.close();
        } catch (Exception e) {
            log.error("Failed to update the endSession time in Session Table " + e.getClass().getName() + ": " + e.getMessage());
            throw new Exception(e);
        }
    }

    // Commit the metrics collected
    void commit() throws Exception {
        try {
            connection.commit();
        } catch (Exception e) {
            log.error("Failed to commit " + e.getClass().getName() + ": " + e.getMessage());
            throw new Exception(e);
        }
    }



    // Close the connection to the sqlite database
    void closeDatabaseConnection() throws Exception {
        try {
            connection.close();
        } catch (Exception e) {
            log.error("Failed to close database connection " + e.getClass().getName() + ": " + e.getMessage());
            throw new Exception(e);
        }
    }
}