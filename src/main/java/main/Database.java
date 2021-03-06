package main;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;

import java.sql.*;
import java.text.DecimalFormat;
import java.util.*;

/**
 * <h1>Create and Retrieve Data From the Database</h1>
 * The Database Class creates the database in which we
 * store metric information. This class also can be used
 * access and retrieve data from the database.
 * <p>
 *
 * @author Harsha Kota, Christopher Byrnes, Bradley Lamitie
 * @version 1.0
 * @since   2018-11-29
 */
public class Database {

    private static Connection connection = null;
    private final Logger log = LogManager.getLogger(Database.class);
    private final SystemInfo si = new SystemInfo();
    private final HardwareAbstractionLayer hal = si.getHardware();
    public static ArrayList<String> tablesAvailable;

    public Database() { }

    /**
     * This method is used to create each of the database tables
     */
    void createTables() throws Exception {
        createSensorsTable();

        createPowerTable();

        createCPUTable();

        createMemoryTable();

        createProcessInfoTable();

        createProcessTable();

        createNetworkTable();

        createSessionTable();

        getTablesAvailable();
    }

    /**
     * This method is used to establish a connection between the application
     * and the sqlite database. If one does not exist already, we will create
     * one.
     * @param databaseUrl This is the url used to develop the connection
     * @throws Exception On failure to connect to the database
     */
    public void establishDatabaseConnection(String databaseUrl) throws Exception {
        try {
            String databaseClassName = "org.sqlite.JDBC";
            Class.forName(databaseClassName);
            connection = DriverManager.getConnection(databaseUrl);
            connection.setAutoCommit(false);
        } catch (NullPointerException | SQLException | ClassNotFoundException e) {
            log.error("establishDatabaseConnection: Failed to connect to the database " + e.getClass().getName() + ": " + e.getMessage());
            throw new Exception("establishDatabaseConnection: Failed to connect to the database " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * This method ensures the session table is intact and the timestamp value is correct.
     * We ensure the integrity of the session timestamp by retrieving the last timestamp
     * and cross referencing the sensors and memory table to ensure that they match.
     * @throws NullPointerException On failure to retrieve the last inserted session
     * @throws Exception On failure to retrieve the last session timestamp from the Sensors Table
     * @throws Exception On failure to retrieve the last session timestamp from the Memory Table
     * @throws Exception On failure to fix the last session timestamp
     */
    public void checkSessionTable() throws Exception {

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
            log.error("checkSessionTable: Failed to fetch the last inserted session time " + e.getClass().getName() + ": " + e.getMessage());
            throw new NullPointerException("checkSessionTable: Failed to fetch the last inserted session time");
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
                log.error("checkSessionTable: Failed to retrieve last timestamp from the sensors table " + e.getClass().getName() + ": " + e.getMessage());
                throw new Exception("checkSessionTable: Failed to retrieve last timestamp from the sensors table " + e.getClass().getName() + ": " + e.getMessage());
            }

            try {
                Statement getEndTimeFromMemoryStatement = connection.createStatement();
                ResultSet rs = getEndTimeFromMemoryStatement.executeQuery(getLastTimeFromMemorySql);
                while(rs.next()) {
                    memoryLastTimestamp = rs.getLong("TIMESTAMP");
                }
                getEndTimeFromMemoryStatement.close();
            } catch (Exception e) {
                log.error("checkSessionTable: Failed to retrieve last timestamp from the memory table " + e.getClass().getName() + ": " + e.getMessage());
                throw new Exception("checkSessionTable: Failed to retrieve last timestamp from the memory table " + e.getClass().getName() + ": " + e.getMessage());
            }

            if (sensorLastTimestamp != memoryLastTimestamp) {
                log.fatal("checkSessionTable: The 2 tables - sensors and memory have different number of records in them.");
                throw new Exception("checkSessionTable: The 2 tables - sensors and memory have different number of records in them.");
            } else {
                String fixEndSessionTimeSql = "UPDATE SESSION SET ENDSESSION = ? WHERE STARTSESSION = ?";
                try {
                    PreparedStatement fixEndSessionTimeStatement = connection.prepareStatement(fixEndSessionTimeSql);

                    fixEndSessionTimeStatement.setLong(1, sensorLastTimestamp);
                    fixEndSessionTimeStatement.setLong(2, startSessionTime);

                    fixEndSessionTimeStatement.executeUpdate();
                    fixEndSessionTimeStatement.close();
                    connection.commit();
                } catch (Exception e) {
                    log.error("checkSessionTable: Failed to fix the endSession time in Session Table " + e.getClass().getName() + ": " + e.getMessage());
                    throw new Exception("checkSessionTable: Failed to fix the endSession time in Session Table " + e.getClass().getName() + ": " + e.getMessage());
                }

            }
        }
    }

    /**
     * This method is used to create the Power table. However,
     * if the user does not have a battery to retrieve metrics
     * the power table is not created.
     * @throws Exception On failure to create the Power Table
     */
    public void createPowerTable() throws Exception {

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
                log.error("createPowerTable: Failed to create Power Table " + e.getClass().getName() + ": " + e.getMessage());
                throw new Exception(e);
            }
        } else {
            log.warn("createPowerTable: There is no battery in this system. Power statistics will be unavailable");
        }
    }

    /**
     * This method is used to create the Sensors table. However,
     * if the user does not have fans or valid cpu voltage it will
     * exclude those when creating the table.
     * @throws Exception On failure to create the Sessions Table
     */
    public void createSensorsTable() throws Exception {

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
            log.error("createSensorsTable: Failed to create Sensor Table " + e.getClass().getName() + ": " + e.getMessage());
            throw new Exception("createSensorsTable: Failed to create Sensor Table " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * This method is used to create the Memory table.
     * @throws Exception On failure to create the Memory Table
     */
    public void createMemoryTable() throws Exception {

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
            log.error("createMemoryTable: Failed to create Memory Table " + e.getClass().getName() + ": " + e.getMessage());
            throw new Exception("createMemoryTable: Failed to create Memory Table " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * This method is used to create the ProcessInfo table.
     * @throws Exception On failure to create the ProcessInfo Table
     */
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
            log.error("createProcessInfoTable: Failed to create ProcessInfo Table " + e.getClass().getName() + ": " + e.getMessage());
            throw new Exception("createProcessInfoTable: Failed to create ProcessInfo Table " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * This method is used to create the Process table.
     * @throws Exception On failure to create the Process Table
     */
    public void createProcessTable() throws Exception {

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
            log.error("createProcessTable: Failed to create Process Table " + e.getClass().getName() + ": " + e.getMessage());
            throw new Exception("createProcessTable: Failed to create Process Table " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * This method is used to create the CPU table. Additionally,
     * this method creates a database column for each logical CPU
     * @throws Exception On failure to create the CPU Table
     */
    public void createCPUTable() throws Exception {

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
            log.error("createCPUTable: Failed to create CPU Table " + e.getClass().getName() + ": " + e.getMessage());
            throw new Exception("createCPUTable: Failed to create CPU Table " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * This method is used to create the Network table.
     * @throws Exception On failure to create the Network Table
     */
    public void createNetworkTable() throws Exception {

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
            log.error("createNetworkTable: Failed to create Network Table " + e.getClass().getName() + ": " + e.getMessage());
            throw new Exception("createNetworkTable: Failed to create Network Table " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * This method is used to create the Session table.
     * @throws Exception On failure to create the Session Table
     */
    // Create a Sessions table to hold each user session start and end time
    public void createSessionTable() throws Exception {

        try {
            Statement sessionTableStatement = connection.createStatement();
            String sql =
                    "CREATE TABLE IF NOT EXISTS SESSION " +
                            "(STARTSESSION  INTEGER  PRIMARY KEY      NOT NULL," +
                            "ENDSESSION     INTEGER                   NULL" +")";
            sessionTableStatement.executeUpdate(sql);
            sessionTableStatement.close();
        } catch (Exception e) {
            log.error("createSessionTable: Failed to create Session Table " + e.getClass().getName() + ": " + e.getMessage());
            throw new Exception("createSessionTable: Failed to create Session Table " + e.getClass().getName() + ": " + e.getMessage());
        }

    }

    /**
     * This method is used to insert metrics data into the power table.
     * @param pS a structure used to store and pass Power metrics between methods and classes
     * @throws Exception On powerStructure lacking valid metrics
     * @throws Exception On failure to insert into the Power Table
     */
    // Insert values into Power Table
    public void insertIntoPowerTable(MetricCollectionStructures.powerStructure pS) throws Exception {

        if (pS != null) {
            //Check if the received powerStructure is not empty
            if (pS.getTimestamp() == 0 || pS.getBatteryPercentage() == 0d) {
                log.error("insertIntoPowerTable: Empty powerStructure used to insert into power table");
                throw new Exception("insertIntoPowerTable: Empty powerStructure used to insert into power table");
            }

            if (Main.hasPowerSource) {
                try {
                    Statement insertIntoPowerTableStatement = connection.createStatement();
                    String sql =
                            "INSERT INTO POWER VALUES (" + pS.getTimestamp() + "," + pS.getPowerStatus() + "," + pS.getBatteryPercentage() + ")";
                    insertIntoPowerTableStatement.executeUpdate(sql);
                    insertIntoPowerTableStatement.close();
                } catch (Exception e) {
                    log.error("insertIntoPowerTable: Failed to insert into Power Table " + e.getClass().getName() + ": " + e.getMessage());
                    throw new Exception("insertIntoPowerTable: Failed to insert into Power Table " + e.getClass().getName() + ": " + e.getMessage());
                }
            }
        }
    }

    /**
     * This method is used to insert metrics data into the Cpu table. The insert statement is
     * derived based on how many logical processors the user has.
     * @param cS a structure used to store and pass Cpu metrics between methods and classes
     * @throws Exception On passing in an empty cpuStructure
     * @throws Exception On passing in an empty cpuStructure with empty processorLoad
     * @throws Exception On passing in a cpuStructure with inaccurate number of logical Processors
     * @throws Exception On failure to insert metrics into Cpu table
     */
    public void insertIntoCpuTable(MetricCollectionStructures.cpuStructure cS) throws Exception {

        if (cS.getTimestamp() < 0 || cS.getUptime() < 0 || cS.getUserLoad() < 0d || cS.getSystemLoad() < 0d ||
                cS.getIdleLoad() < 0d)
        {
                log.error("insertIntoCpuTable: Empty cpuStructure used to insert into CPU table ");
                throw new Exception("insertIntoCpuTable: Empty cpuStructure used to insert into CPU table ");
        }

        if (cS.getProcessorLoad() == null) {
            log.error("insertIntoCpuTable: Empty cpuStructure used to insert into CPU table: Empty getProcessorLoad ");
            throw new Exception("insertIntoCpuTable: Empty cpuStructure used to insert into CPU table: Empty getProcessorLoad ");
        }

        if (cS.getProcessorLoad().size() != hal.getProcessor().getLogicalProcessorCount()) {
            log.error("insertIntoCpuTable: No of processor loads are less than logicalProcessorCount ");
            throw new Exception("insertIntoCpuTable: No of processor loads are less than logicalProcessorCount ");
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
            log.error("insertIntoCpuTable: Failed to insert into CPU Table " + e.getClass().getName() + ": " + e.getMessage());
            throw new Exception("insertIntoCpuTable: Failed to insert into CPU Table " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * This method is used to insert metrics data into the Sensors table. The insert statement is
     * derived based on how many fans the user has, as well as if cpu voltage is available.
     * @param sS a structure used to store and pass sensor metrics between methods and classes
     * @throws Exception On passing in an empty sensorsStructure
     * @throws Exception On failure to insert metrics into Sensors table
     */
    public void insertIntoSensorsTable(MetricCollectionStructures.sensorsStructure sS) throws Exception {

        if (sS.getTimestamp() ==  0)
        {
            log.error("insertIntoSensorsTable: Empty sensorsStructure used to insert into Sensors table ");
            throw new Exception("insertIntoSensorsTable: Empty sensorsStructure used to insert into Sensors table ");
        }

        StringBuilder fans = new StringBuilder();
        if(sS.getFans().size() > 0) {
            int commas = sS.getFans().size() - 1;
            for (int i = 0; i < sS.getFans().size(); i++) {
                if (commas > 0) {
                    fans.append(sS.fans.get(i)).append(",");
                }
                fans.append(sS.fans.get(i));
            }
        }

        try {
            Statement insertIntoSensorsTableStatement = connection.createStatement();
            String sql =
                    "INSERT INTO SENSORS VALUES ("+sS.getTimestamp()+"," +sS.getCpuTemperature() +
                            (sS.getCpuVoltage()== 999.0 ? "" : ",") +
                            (sS.getCpuVoltage()==999.0 ? "" : (sS.getCpuVoltage())) +
                            (fans.length() <= 0 ? "" : ",") +
                            fans + ")";
            insertIntoSensorsTableStatement.executeUpdate(sql);
            insertIntoSensorsTableStatement.close();
        } catch (Exception e) {
            log.error("insertIntoSensorsTable: Failed to insert into Sensors Table " + e.getClass().getName() + ": " + e.getMessage());
            throw new Exception("insertIntoSensorsTable: Failed to insert into Sensors Table " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * This method is used to insert metrics data into the Memory table.
     * @param mS a structure used to store and pass Memory metrics between methods and classes
     * @throws Exception On passing in an empty memoryStructure
     * @throws Exception On failure to insert metrics into Memory table
     */
    public void insertIntoMemoryTable(MetricCollectionStructures.memoryStructure mS) throws Exception {

        if (mS.getTimestamp() ==  0 || mS.getUsedMemory() == 0d || mS.getTotalMemory() == 0d)
        {
            log.error("insertIntoMemoryTable: Empty memoryStructure used to insert into Memory table ");
            throw new Exception("insertIntoMemoryTable: Empty memoryStructure used to insert into Memory table ");
        }

        try {
            Statement insertIntoMemoryTableStatement = connection.createStatement();
            String sql =
                    "INSERT INTO MEMORY VALUES ("+mS.getTimestamp()+"," + mS.getUsedMemory()+","
                    + mS.getTotalMemory() + ")";
            insertIntoMemoryTableStatement.executeUpdate(sql);
            insertIntoMemoryTableStatement.close();
        } catch (Exception e) {
            log.error("insertIntoMemoryTable: Failed to insert into Memory Table " + e.getClass().getName() + ": " + e.getMessage());
            throw new Exception("insertIntoMemoryTable: Failed to insert into Memory Table " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * This method is used to insert metrics data into the Network table.
     * @param nS a structure used to store and pass network metrics between methods and classes
     * @throws Exception On passing in an empty networkStructure
     * @throws Exception On failure to insert metrics into Network table
     */
    // Insert values into Network Table
    public void insertIntoNetworkTable(MetricCollectionStructures.networkStructure nS) throws Exception {

        if (nS.getTimestamp() ==  0 || nS.getPacketsReceived() == 0 || nS.getPacketsSent() == 0 ||
                nS.getSizeReceived().equals("") || nS.getSizeSent().equals(""))
        {
            log.error("insertIntoNetworkTable: Empty networkStructure used to insert into Network table ");
            throw new Exception("insertIntoNetworkTable: Empty networkStructure used to insert into Network table ");
        }

        try {
            Statement insertIntoNetworkTableStatement = connection.createStatement();
            String sql =
                    "INSERT INTO NETWORK VALUES ("+nS.getTimestamp()+"," +nS.getPacketsReceived()+","
                            + nS.getPacketsSent()+"," +"'"+nS.getSizeReceived()+"'"+"," +"'"+nS.getSizeSent()+"'" +")";
            insertIntoNetworkTableStatement.executeUpdate(sql);
            insertIntoNetworkTableStatement.close();
        } catch (Exception e) {
            log.error("insertIntoNetworkTable: Failed to insert into Network Table " + e.getClass().getName() + ": " + e.getMessage());
            throw new Exception("insertIntoNetworkTable: Failed to insert into Network Table " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * This method is used to insert metrics data into the Process and ProcessInfo tables.
     * @param pS a structure used to store and pass sensor metrics between methods and classes
     * @throws Exception On passing in an empty sensorsStructure
     * @throws Exception On passing in an empty sensorsStructure with null processesList
     * @throws Exception On passing in an empty sensorsStructure with an empty processesList
     * @throws Exception On failure to insert metrics into Process table
     * @throws Exception On failure to insert metrics into ProcessInfo table
     */
    public void insertIntoProcessTable(MetricCollectionStructures.processStructure pS) throws Exception {

        if (pS.getTimestamp() ==  0 || pS.getNoOfThreads() == 0 || pS.getNoOfProcesses() == 0)
        {
            log.error("insertIntoProcessTable: Empty processStructure used to insert into Process table ");
            throw new Exception("insertIntoProcessTable: Empty processStructure used to insert into Process table ");
        }

        if (pS.getProcessesList() == null) {
            log.error("insertIntoProcessTable: Empty processStructure used to insert into Processes table: processesList Null ");
            throw new Exception("insertIntoProcessTable: Empty processStructure used to insert into Processes table: processesList Null ");
        }
        HashMap<String, List<Double>> processesList = pS.getProcessesList();
        for (Map.Entry<String, List<Double>> pair: ((Map<String, List<Double>>) processesList).entrySet()) {
            if (pair.getKey().equals("") || pair.getValue().get(0) == null || pair.getValue().get(1) == null) {
                log.error("insertIntoProcessTable: Empty processStructure used to insert into Processes table: Empty processesList ");
                throw new Exception("insertIntoProcessTable: Empty processStructure used to insert into Processes table: Empty processesList ");
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
            log.error("insertIntoProcessTable: Failed to insert into Process Table " + e.getClass().getName() + ": " + e.getMessage());
            throw new Exception("insertIntoProcessTable: Failed to insert into Process Table " + e.getClass().getName() + ": " + e.getMessage());
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
                log.error("insertIntoProcessTable: Failed to insert into ProcessInfo Table " + e.getClass().getName() + ": " + e.getMessage());
                throw new Exception("insertIntoProcessTable: Failed to insert into ProcessInfo Table " + e.getClass().getName() + ": " + e.getMessage());
            }
        }
    }

    /**
     * This method is used to insert the timestamp representing the start of a session.
     * @param startSession a long representing the time the session started in milliseconds
     * @throws Exception On failure to insert start session time into the Session table
     */
    public void insertStartSessionIntoSessionTable(final long startSession) throws Exception {

        try {
            Statement insertStartSessionIntoSessionTableStatement = connection.createStatement();
            String sql =
                    "INSERT INTO SESSION VALUES ("+ startSession +",NULL" +")";
            insertStartSessionIntoSessionTableStatement.executeUpdate(sql);
            insertStartSessionIntoSessionTableStatement.close();
        } catch (Exception e) {
            log.error("insertStartSessionIntoSessionTable: Failed to insert Start Session Time into Session Table " + e.getClass().getName() + ": " + e.getMessage());
            throw new Exception("insertStartSessionIntoSessionTable: Failed to insert Start Session Time into Session Table " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * This method is used to insert the end timestamp representing the start and end of a session.
     * This is accomplished by finding the matching session in the database and adding the end session
     * time to the table.
     * @param startSession a long representing the time the session started in milliseconds
     * @param endSession a long representing the time the session ended in milliseconds
     * @throws Exception On failure to update session table with end session time into the Session table
     */
    public void insertEndSessionIntoSessionTable(final long startSession, final long endSession) throws Exception {

        String sql = "UPDATE SESSION SET ENDSESSION = ? WHERE STARTSESSION = ?";

        try {

            PreparedStatement insertEndSessionIntoSessionTableStatement = connection.prepareStatement(sql);

            insertEndSessionIntoSessionTableStatement.setLong(1, endSession);
            insertEndSessionIntoSessionTableStatement.setLong(2,startSession);

            insertEndSessionIntoSessionTableStatement.executeUpdate();
            insertEndSessionIntoSessionTableStatement.close();
        } catch (Exception e) {
            log.error("insertEndSessionIntoSessionTable: Failed to update the endSession time in Session Table " + e.getClass().getName() + ": " + e.getMessage());
            throw new Exception("insertEndSessionIntoSessionTable: Failed to update the endSession time in Session Table " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * This method is used to make all queries executed on the database in the time since the last rollback.
     * This method also releases locks on the database.
     * @throws Exception On failure to commit the queries executed to the database
     */
    public void commit() throws Exception {
        try {
            connection.commit();
        } catch (Exception e) {
            log.error("commit: Failed to commit " + e.getClass().getName() + ": " + e.getMessage());
            throw new Exception("commit: Failed to commit " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * This method is used to close the connection to the database.
     * @throws Exception On failure to close the connection to the database
     */
    public void closeDatabaseConnection() throws Exception {
        try {
            connection.close();
        } catch (Exception e) {
            log.error("closeDatabaseConnection: Failed to close database connection " + e.getClass().getName() + ": " + e.getMessage());
            throw new Exception("closeDatabaseConnection: Failed to close database connection " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * This method is used to retrieve what tables are available to the user. This method is used
     * to populate the main dropdown box in the UI.
     * @throws Exception On failure to retrieve tables
     */
    private void getTablesAvailable() throws Exception {
        tablesAvailable = new ArrayList<>();

        try {
            ResultSet rs = connection.getMetaData().getTables(null,null,null,null);
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");
                if (tableName.equalsIgnoreCase("session")) continue;
                tablesAvailable.add(tableName);
            }
        } catch (SQLException e) {
            log.error("getTables: Failed to get table names " + e.getClass().getName() + ": " + e.getMessage());
            throw new Exception("getTables: Failed to get table names " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * This method is used to retrieve the sessions and sort them by time. This method is used to
     * create the list of available metrics in the UI
     * @return A linked hash map containing the start and end times associated with the session
     * @throws Exception On failure to retrieve the sessions from the table.
     */
    public LinkedHashMap<Long, Long> getSessions() throws Exception {
        LinkedHashMap<Long,Long> sessions = new LinkedHashMap<>();
        try {
            Statement getSessionsStatement = connection.createStatement();
            String sql =
                    "SELECT * FROM SESSION ORDER BY STARTSESSION DESC";
            ResultSet rs = getSessionsStatement.executeQuery(sql);
            while (rs.next()) {
                Long startSession = rs.getLong("STARTSESSION");
                long endSession = rs.getLong("ENDSESSION");
                if (endSession == 0) continue;
                sessions.put(startSession, endSession);
            }
            getSessionsStatement.close();
        } catch (Exception e) {
            log.error("getSessions: Failed to get sessions from session table " + e.getClass().getName() + ": " + e.getMessage());
            throw new Exception("getSessions: Failed to get sessions from session table " + e.getClass().getName() + ": " + e.getMessage());
        }

        return sessions;
    }

    /**
     * This method is used to retrieve data from the power table for use in the UI
     * @return A linked hash map holding the timeStamp, and the value of the column
     * @param startTime This is the start time of the session we are retrieving for
     * @param endTime This is the end time of the session we are retrieving for
     * @throws Exception On failure to retrieve power metrics from the table
     * @throws Exception On start and end time being null
     */
    public LinkedHashMap<Long, Double> getPowerMetrics(Long startTime, Long endTime) throws Exception {

        LinkedHashMap<Long, Double> powerMetrics = new LinkedHashMap<>();

        if (startTime != null && endTime != null) {
            String sql = "SELECT * FROM POWER WHERE TIMESTAMP >= ? AND TIMESTAMP <= ? ORDER BY TIMESTAMP ASC";
            try {
                PreparedStatement getPowerMetricsStatement = connection.prepareStatement(sql);
                getPowerMetricsStatement.setLong(1, startTime);
                getPowerMetricsStatement.setLong(2, endTime);
                ResultSet rs = getPowerMetricsStatement.executeQuery();
                while (rs.next()) {
                    Long timestamp = rs.getLong("TIMESTAMP");
                    Double batteryPercentage = rs.getDouble("BATTERYPERCENTAGE");
                    //Double.parseDouble(new DecimalFormat("#0.#").format(rs.getDouble(columnName)))
                    powerMetrics.put(timestamp, batteryPercentage);
                }
                getPowerMetricsStatement.close();
            } catch (Exception e) {
                log.error("getPowerMetrics: Failed to get power metrics from power table " + e.getClass().getName() + ": " + e.getMessage());
                throw new Exception("getPowerMetrics: Failed to get power metrics from power table " + e.getClass().getName() + ": " + e.getMessage());
            }
        } else {
            log.error("getPowerMetrics: startTime and endTime are null ");
            throw new Exception("getPowerMetrics: startTime and endTime are null ");
        }

        return powerMetrics;
    }

    /**
     * This method is used to retrieve the names of columns present in the Cpu table.
     * @return An array list containing the names of each column in the cpu table
     * @throws Exception On failure to retrieve cpu columns
     */
    public ArrayList<String> getCpuColumns() throws Exception {
        ArrayList<String> cpuColumns = new ArrayList<>();

        String sql = "SELECT * FROM CPU";
        try {
            PreparedStatement getCpuTableColumnsStatement = connection.prepareStatement(sql);
            ResultSet rs = getCpuTableColumnsStatement.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            for (int i=1; i<=columnCount; i++) {
                cpuColumns.add(metaData.getColumnName(i));
            }
            cpuColumns.remove(0);
            getCpuTableColumnsStatement.close();
        } catch (SQLException e) {
            log.error("getCpuColumns: Failed to get cpu columns " + e.getClass().getName() + ": " + e.getMessage());
            throw new Exception("getCpuColumns: Failed to get cpu columns " + e.getClass().getName() + ": " + e.getMessage());
        }

        return cpuColumns;
    }

    /**
     * This method is used to retrieve data from the cpu table for use in the UI
     * @return A linked hash map holding the timeStamp, and the value of the column
     * @param startTime This is the start time of the session we are retrieving for
     * @param endTime This is the end time of the session we are retrieving for
     * @param columnName This is the name of the metric we are retrieving
     * @throws Exception On failure to retrieve cpu metrics from the table
     * @throws Exception On start and end time being null
     */
    public LinkedHashMap<Long, Double> getCpuMetrics(Long startTime, Long endTime, String columnName) throws Exception {
        LinkedHashMap<Long, Double> cpuMetrics = new LinkedHashMap<>();

        if (startTime != null && endTime != null) {
            String sql = "SELECT * FROM CPU WHERE TIMESTAMP >= ? AND TIMESTAMP <= ? ORDER BY TIMESTAMP ASC";
            try {
                PreparedStatement getCpuMetricsStatement = connection.prepareStatement(sql);
                getCpuMetricsStatement.setLong(1, startTime);
                getCpuMetricsStatement.setLong(2, endTime);
                ResultSet rs = getCpuMetricsStatement.executeQuery();
                while (rs.next()) {
                    Long timestamp = rs.getLong("TIMESTAMP");
                    Double columnValue = Double.parseDouble(new DecimalFormat("#0.#").format(rs.getDouble(columnName)));
                    cpuMetrics.put(timestamp, columnValue);
                }
                getCpuMetricsStatement.close();
            } catch (Exception e) {
                log.error("getCpuMetrics: Failed to get cpu metrics from cpu table " + e.getClass().getName() + ": " + e.getMessage());
                throw new Exception("getCpuMetrics: Failed to get cpu metrics from cpu table " + e.getClass().getName() + ": " + e.getMessage());
            }
        } else {
            log.error("getCpuMetrics: startTime and endTime are null ");
            throw new Exception("getCpuMetrics: startTime and endTime are null ");
        }

        return cpuMetrics;
    }

    /**
     * This method is used to retrieve the names of columns present in the Memory table.
     * @return An array list containing the names of each column in the memory table
     * @throws Exception On failure to retrieve memory columns
     */
    public ArrayList<String> getMemoryColumns() throws Exception {
        ArrayList<String> memoryColumns = new ArrayList<>();

        String sql = "SELECT * FROM MEMORY";
        try {
            PreparedStatement getMemoryTableColumnsStatement = connection.prepareStatement(sql);
            ResultSet rs = getMemoryTableColumnsStatement.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            for (int i=1; i<=columnCount; i++) {
                memoryColumns.add(metaData.getColumnName(i));
            }
            memoryColumns.remove(0);
            getMemoryTableColumnsStatement.close();
        } catch (SQLException e) {
            log.error("getMemoryColumns: Failed to get memory columns " + e.getClass().getName() + ": " + e.getMessage());
            throw new Exception("getMemoryColumns: Failed to get memory columns " + e.getClass().getName() + ": " + e.getMessage());
        }

        return memoryColumns;
    }

    /**
     * This method is used to retrieve the total memory from the memory table for use in the UI
     * @return A double value representing the total memory
     * @throws Exception On failure to retrieve total memory from the table
     */
    public Double getTotalMemory() throws Exception {
        Double totalMemory = null;

        String sql = "SELECT MAX(TIMESTAMP), TOTALMEMORY FROM MEMORY";

        try {
            PreparedStatement getTotalMemoryStatement = connection.prepareStatement(sql);
            ResultSet rs = getTotalMemoryStatement.executeQuery();
            while (rs.next()) {
                totalMemory = Double.parseDouble(new DecimalFormat("#0.#").format(rs.getDouble("TOTALMEMORY")));
            }
            getTotalMemoryStatement.close();
        } catch (SQLException e) {
            log.error("getTotalMemory: Failed to get total memory " + e.getClass().getName() + ": " + e.getMessage());
            throw new Exception("getTotalMemory: Failed to get total memory " + e.getClass().getName() + ": " + e.getMessage());
        }

        return totalMemory;
    }

    /**
     * This method is used to retrieve data from the memory table for use in the UI
     * @return A linked hash map holding the timeStamp, and the value of the column
     * @param startTime This is the start time of the session we are retrieving for
     * @param endTime This is the end time of the session we are retrieving for
     * @param columnName This is the name of the metric we are retrieving
     * @throws Exception On failure to retrieve memory metrics from the table
     * @throws Exception On start and end time being null
     */
    public LinkedHashMap<Long, Double> getMemoryMetrics(Long startTime, Long endTime, String columnName) throws Exception {
        LinkedHashMap<Long, Double> memoryMetrics = new LinkedHashMap<>();

        if (startTime != null && endTime != null) {
            String sql = "SELECT * FROM MEMORY WHERE TIMESTAMP >= ? AND TIMESTAMP <= ? ORDER BY TIMESTAMP ASC";
            try {
                PreparedStatement getMemoryMetricsStatement = connection.prepareStatement(sql);
                getMemoryMetricsStatement.setLong(1, startTime);
                getMemoryMetricsStatement.setLong(2, endTime);
                ResultSet rs = getMemoryMetricsStatement.executeQuery();
                while (rs.next()) {
                    Long timestamp = rs.getLong("TIMESTAMP");
                    Double columnValue = Double.parseDouble(new DecimalFormat("#0.#").format(rs.getDouble(columnName)));
                    memoryMetrics.put(timestamp, columnValue);
                }
                getMemoryMetricsStatement.close();
            } catch (Exception e) {
                log.error("getMemoryMetrics: Failed to get memory metrics from memory table " + e.getClass().getName() + ": " + e.getMessage());
                throw new Exception("getMemoryMetrics: Failed to get memory metrics from memory table " + e.getClass().getName() + ": " + e.getMessage());
            }
        } else {
            log.error("getMemoryMetrics: startTime and endTime are null ");
            throw new Exception("getMemoryMetrics: startTime and endTime are null ");
        }

        return memoryMetrics;
    }

    /**
     * This method is used to retrieve the names of columns present in the Network table.
     * @return An array list containing the names of each column in the network table
     * @throws Exception On failure to retrieve network columns
     */
    public ArrayList<String> getNetworkColumns() throws Exception {
        ArrayList<String> networkColumns = new ArrayList<>();

        String sql = "SELECT * FROM NETWORK";
        try {
            PreparedStatement getNetworkTableColumnsStatement = connection.prepareStatement(sql);
            ResultSet rs = getNetworkTableColumnsStatement.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            for (int i=1; i<=columnCount; i++) {
                networkColumns.add(metaData.getColumnName(i));
            }
            networkColumns.remove(0);
            getNetworkTableColumnsStatement.close();
        } catch (SQLException e) {
            log.error("getNetworkColumns: Failed to get network columns " + e.getClass().getName() + ": " + e.getMessage());
            throw new Exception("getNetworkColumns: Failed to get network columns " + e.getClass().getName() + ": " + e.getMessage());
        }

        return networkColumns;
    }

    /**
     * This method is used to retrieve data from the network table for use in the UI
     * @return A linked hash map holding the timeStamp, and the value of the column
     * @param startTime This is the start time of the session we are retrieving for
     * @param endTime This is the end time of the session we are retrieving for
     * @param columnName This is the name of the metric we are retrieving
     * @throws Exception On failure to retrieve network metrics from the table
     * @throws Exception On start and end time being null
     */
    public LinkedHashMap<Long, Double> getNetworkMetrics(Long startTime, Long endTime, String columnName) throws Exception {
        LinkedHashMap<Long, Double> networkMetrics = new LinkedHashMap<>();

        if (startTime != null && endTime != null) {
            String sql = "SELECT * FROM NETWORK WHERE TIMESTAMP >= ? AND TIMESTAMP <= ? ORDER BY TIMESTAMP ASC";
            try {
                PreparedStatement getNetworkMetricsStatement = connection.prepareStatement(sql);
                getNetworkMetricsStatement.setLong(1, startTime);
                getNetworkMetricsStatement.setLong(2, endTime);
                ResultSet rs = getNetworkMetricsStatement.executeQuery();
                while (rs.next()) {
                    Long timestamp = rs.getLong("TIMESTAMP");
                    String columnValue = rs.getString(columnName);
                    if (columnName.equalsIgnoreCase("sizereceived") || columnName.equalsIgnoreCase("sizesent")) {
                        Double decimalValue = Double.parseDouble(
                                new DecimalFormat("#0.#").format(
                                        Double.valueOf(columnValue.split(" ")[0])));
                        String sizeIndicator = columnValue.split(" ")[1];
                        Double actualValue = Double.parseDouble(
                                new DecimalFormat("#0.#").format(
                                        decimalValue * (sizeIndicator.equalsIgnoreCase("gib") ? 1024 : 1)));
                        networkMetrics.put(timestamp, actualValue);
                    } else {
                        Double actualValue = Double.parseDouble(new DecimalFormat("#0.#").format(rs.getDouble(columnName)));
                        networkMetrics.put(timestamp, actualValue);
                    }
                }
                getNetworkMetricsStatement.close();
            } catch (Exception e) {
                log.error("getNetworkMetrics: Failed to get network metrics from network table " + e.getClass().getName() + ": " + e.getMessage());
                throw new Exception("getMemoryMetrics: Failed to get network metrics from network table " + e.getClass().getName() + ": " + e.getMessage());
            }
        } else {
            log.error("getNetworkMetrics: startTime and endTime are null ");
            throw new Exception("getNetworkMetrics: startTime and endTime are null ");
        }

        return networkMetrics;
    }

    /**
     * This method is used to retrieve the names of columns present in the Process table.
     * @return An array list containing the names of each column in the process table
     * @throws Exception On failure to retrieve process columns
     */
    public ArrayList<String> getProcessColumns() throws Exception {
        ArrayList<String> networkColumns = new ArrayList<>();

        String sql = "SELECT * FROM PROCESS";
        try {
            PreparedStatement getProcessTableColumnsStatement = connection.prepareStatement(sql);
            ResultSet rs = getProcessTableColumnsStatement.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            for (int i=1; i<=columnCount; i++) {
                networkColumns.add(metaData.getColumnName(i));
            }
            networkColumns.remove(0);
            getProcessTableColumnsStatement.close();
        } catch (SQLException e) {
            log.error("getProcessColumns: Failed to get process columns " + e.getClass().getName() + ": " + e.getMessage());
            throw new Exception("getProcessColumns: Failed to get process columns " + e.getClass().getName() + ": " + e.getMessage());
        }

        return networkColumns;
    }

    /**
     * This method is used to retrieve data from the process table for use in the UI
     * @return A linked hash map holding the timeStamp, and the value of the column
     * @param startTime This is the start time of the session we are retrieving for
     * @param endTime This is the end time of the session we are retrieving for
     * @param columnName This is the name of the metric we are retrieving
     * @throws Exception On failure to retrieve process metrics from the table
     * @throws Exception On start and end time being null
     */
    public LinkedHashMap<Long, Long> getProcessMetrics(Long startTime, Long endTime, String columnName) throws Exception {
        LinkedHashMap<Long, Long> processMetrics = new LinkedHashMap<>();

        if (startTime != null && endTime != null) {
            String sql = "SELECT * FROM PROCESS WHERE TIMESTAMP >= ? AND TIMESTAMP <= ? ORDER BY TIMESTAMP ASC";
            try {
                PreparedStatement getProcessMetricsStatement = connection.prepareStatement(sql);
                getProcessMetricsStatement.setLong(1, startTime);
                getProcessMetricsStatement.setLong(2, endTime);
                ResultSet rs = getProcessMetricsStatement.executeQuery();
                while (rs.next()) {
                    Long timestamp = rs.getLong("TIMESTAMP");
                    Long columnValue = rs.getLong(columnName);
                    processMetrics.put(timestamp, columnValue);
                }
                getProcessMetricsStatement.close();
            } catch (Exception e) {
                log.error("getProcessMetrics: Failed to get process metrics from process table " + e.getClass().getName() + ": " + e.getMessage());
                throw new Exception("getProcessMetrics: Failed to get process metrics from process table " + e.getClass().getName() + ": " + e.getMessage());
            }
        } else {
            log.error("getProcessMetrics: startTime and endTime are null ");
            throw new Exception("getProcessMetrics: startTime and endTime are null ");
        }

        return processMetrics;
    }

    /**
     * This method is used to retrieve the names of columns present in the Sensors table.
     * @return An array list containing the names of each column in the sensors table
     * @throws Exception On failure to retrieve sensors columns
     */
    public ArrayList<String> getSensorsColumns() throws Exception {
        ArrayList<String> sensorsColumns = new ArrayList<>();

        String sql = "SELECT * FROM SENSORS";
        try {
            PreparedStatement getSensorsTableColumnsStatement = connection.prepareStatement(sql);
            ResultSet rs = getSensorsTableColumnsStatement.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            for (int i=1; i<=columnCount; i++) {
                sensorsColumns.add(metaData.getColumnName(i));
            }
            sensorsColumns.remove(0);
            getSensorsTableColumnsStatement.close();
        } catch (SQLException e) {
            log.error("getSensorsColumns: Failed to get sensors columns " + e.getClass().getName() + ": " + e.getMessage());
            throw new Exception("getSensorsColumns: Failed to get sensors columns " + e.getClass().getName() + ": " + e.getMessage());
        }

        return sensorsColumns;
    }

    /**
     * This method is used to retrieve data from the sensors table for use in the UI
     * @return A linked hash map holding the timeStamp, and the value of the column
     * @param startTime This is the start time of the session we are retrieving for
     * @param endTime This is the end time of the session we are retrieving for
     * @param columnName This is the name of the metric we are retrieving
     * @throws Exception On failure to retrieve sensors metrics from the table
     * @throws Exception On start and end time being null
     */
    public LinkedHashMap<Long, Double> getSensorsMetrics(Long startTime, Long endTime, String columnName) throws Exception {
        LinkedHashMap<Long, Double> sensorsMetrics = new LinkedHashMap<>();

        if (startTime != null && endTime != null) {
            String sql = "SELECT * FROM SENSORS WHERE TIMESTAMP >= ? AND TIMESTAMP <= ? ORDER BY TIMESTAMP ASC";
            try {
                PreparedStatement getSensorsMetricsStatement = connection.prepareStatement(sql);
                getSensorsMetricsStatement.setLong(1, startTime);
                getSensorsMetricsStatement.setLong(2, endTime);
                ResultSet rs = getSensorsMetricsStatement.executeQuery();
                while (rs.next()) {
                    Long timestamp = rs.getLong("TIMESTAMP");
                    Double columnValue = Double.parseDouble(new DecimalFormat("#0.#").format(rs.getDouble(columnName)));
                    sensorsMetrics.put(timestamp, columnValue);
                }
                getSensorsMetricsStatement.close();
            } catch (Exception e) {
                log.error("getSensorsMetrics: Failed to get sensors metrics from sensors table " + e.getClass().getName() + ": " + e.getMessage());
                throw new Exception("getSensorsMetrics: Failed to get sensors metrics from sensors table " + e.getClass().getName() + ": " + e.getMessage());
            }
        } else {
            log.error("getSensorsMetrics: startTime and endTime are null ");
            throw new Exception("getSensorsMetrics: startTime and endTime are null ");
        }

        return sensorsMetrics;
    }

    /**
     * This method is used to retrieve the names of columns present in the ProcessInfo table.
     * @return An array list containing the names of each column in the ProcessInfo table
     * @throws Exception On failure to retrieve ProcessInfo columns
     */
    public ArrayList<String> getProcessinfoColumns() throws Exception {
        ArrayList<String> processinfoColumns = new ArrayList<>();

        String sql = "SELECT * FROM PROCESSINFO";
        try {
            PreparedStatement getProcessinfoTableColumnsStatement = connection.prepareStatement(sql);
            ResultSet rs = getProcessinfoTableColumnsStatement.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            for (int i=1; i<=columnCount; i++) {
                processinfoColumns.add(metaData.getColumnName(i));
            }
            processinfoColumns.remove(0);
            processinfoColumns.remove(0);
            getProcessinfoTableColumnsStatement.close();
        } catch (SQLException e) {
            log.error("getProcessinfoColumns: Failed to get processinfo columns " + e.getClass().getName() + ": " + e.getMessage());
            throw new Exception("getProcessinfoColumns: Failed to get processinfo columns " + e.getClass().getName() + ": " + e.getMessage());
        }

        return processinfoColumns;
    }

    /**
     * This method is used to retrieve the list of processes running on the system.
     * @return An array list containing names of processes present in the ProcessInfo table.
     * @throws Exception On failure to retrieve the processes' names.
     */
    public ArrayList<String> getProcessesNames(Long startTime, Long endTime) throws Exception {
        ArrayList<String> processesNames = new ArrayList<>();

        String sql = "SELECT DISTINCT PROCESSNAME FROM PROCESSINFO WHERE TIMESTAMP >= ? AND TIMESTAMP <= ? ORDER BY TIMESTAMP ASC";
        try {
            PreparedStatement getProcessesNamesStatement = connection.prepareStatement(sql);
            getProcessesNamesStatement.setLong(1, startTime);
            getProcessesNamesStatement.setLong(2, endTime);
            ResultSet rs = getProcessesNamesStatement.executeQuery();
            while (rs.next()) {
                String processName = rs.getString("PROCESSNAME");
                processesNames.add(processName);
            }
            getProcessesNamesStatement.close();
        } catch (SQLException e) {
            log.error("getProcessesNames: Failed to get processes names " + e.getClass().getName() + ": " + e.getMessage());
            throw new Exception("getProcessesNames: Failed to get processes names " + e.getClass().getName() + ": " + e.getMessage());
        }

        return processesNames;
    }

    /**
     * This method is used to retrieve data from the processInfo table about a single process for use in the UI
     * @return A linked hash map holding the timeStamp, and the value of the column
     * @param startTime This is the start time of the session we are retrieving for
     * @param endTime This is the end time of the session we are retrieving for
     * @param columnName This is the name of the metric we are retrieving
     * @param processName This is the name of a particular process
     * @throws Exception On failure to retrieve processInfo metrics from the table
     * @throws Exception On start and end time being null
     */
    public LinkedHashMap<Long, Double> getProcessinfoMetrics(Long startTime, Long endTime, String columnName, String processName) throws Exception {
        LinkedHashMap<Long, Double> processinfoMetrics = new LinkedHashMap<>();
        if (startTime != null && endTime != null) {
            String sql = "SELECT * FROM PROCESSINFO WHERE TIMESTAMP >= ? AND TIMESTAMP <= ? AND PROCESSNAME = ? ORDER BY TIMESTAMP ASC";
            try {
                PreparedStatement getProcessinfoMetricsStatement = connection.prepareStatement(sql);
                getProcessinfoMetricsStatement.setLong(1, startTime);
                getProcessinfoMetricsStatement.setLong(2, endTime);
                getProcessinfoMetricsStatement.setString(3, processName);
                ResultSet rs = getProcessinfoMetricsStatement.executeQuery();
                while (rs.next()) {
                    Long timestamp = rs.getLong("TIMESTAMP");
                    Double columnValue = Double.parseDouble(new DecimalFormat("#0.#").format(rs.getDouble(columnName)));
                    processinfoMetrics.put(timestamp, columnValue);
                }
                getProcessinfoMetricsStatement.close();
            } catch (Exception e) {
                log.error("getProcessinfoMetrics: Failed to get processinfo metrics from processinfo table " + e.getClass().getName() + ": " + e.getMessage());
                throw new Exception("getProcessinfoMetrics: Failed to get processinfo metrics from processinfo table " + e.getClass().getName() + ": " + e.getMessage());
            }
        } else {
            log.error("getProcessinfoMetrics: startTime and endTime are null ");
            throw new Exception("getProcessinfoMetrics: startTime and endTime are null ");
        }
        return processinfoMetrics;
    }

}