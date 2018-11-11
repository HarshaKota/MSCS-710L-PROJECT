import org.junit.BeforeClass;
import org.junit.Test;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DatabaseTest {

    private static SystemInfo si;
    private static String databaseUrl;

    @BeforeClass
    public static void setup() {
        si = new SystemInfo();
        databaseUrl = "jdbc:sqlite:TestMetricCollector.db";
    }

    @Test(expected = Exception.class)
    public void establishDatabaseConnection_Null() throws Exception {
        Database dbObj = new Database();
        dbObj.establishDatabaseConnection(null, null);
    }

    @Test(expected = Exception.class)
    public void checkSessionTable_Null() throws Exception {
        Database dbObj = new Database();
        dbObj.checkSessionTable();
    }

    @Test(expected = Exception.class)
    public void createPowerTable() throws Exception {
        Database dbObj = new Database();
        dbObj.createPowerTable();
    }

    @Test(expected = Exception.class)
    public void createSensorsTable() throws Exception {
        Database dbObj = new Database();
        dbObj.createSensorsTable();
    }

    @Test(expected = Exception.class)
    public void createMemoryTable() throws Exception {
        Database dbObj = new Database();
        dbObj.createMemoryTable();
    }

    @Test(expected = Exception.class)
    public void createProcessTable() throws Exception {
        Database dbObj = new Database();
        dbObj.createProcessTable();
    }

    @Test(expected = Exception.class)
    public void createCPUTable() throws Exception {
        Database dbObj = new Database();
        dbObj.createCPUTable();
    }

    @Test(expected = Exception.class)
    public void createNetworkTable() throws Exception {
        Database dbObj = new Database();
        dbObj.createNetworkTable();
    }

    @Test(expected = Exception.class)
    public void createSessionTable() throws Exception {
        Database dbObj = new Database();
        dbObj.createSessionTable();
    }

    @Test(expected = Exception.class)
    public void insertIntoPowerTable_NoConnection() throws Exception {
        Main.hasPowerSource = true;
        MetricCollectionStructures.powerStructure powerStructure = new MetricCollectionStructures.powerStructure();
        powerStructure.setTimestamp(System.currentTimeMillis());
        powerStructure.setPowerStatus(0);
        powerStructure.setBatteryPercentage(100d);
        Database dbObj = new Database();
        dbObj.insertIntoPowerTable(powerStructure);
    }

    @Test(expected = Exception.class)
    public void insertIntoPowerTable_EmptyInputs() throws Exception {
        Main.hasPowerSource = true;
        Database dbObj = new Database(databaseUrl);
        dbObj.insertIntoPowerTable(new MetricCollectionStructures.powerStructure());
    }

    @Test(expected = Exception.class)
    public void insertIntoCpuTable_NoConnection() throws Exception {
        MetricCollectionStructures.cpuStructure cpuStructure = new MetricCollectionStructures.cpuStructure();
        cpuStructure.setTimestamp(System.currentTimeMillis());
        cpuStructure.setUptime(100L);
        cpuStructure.setUserLoad(100d);
        cpuStructure.setSystemLoad(100d);
        cpuStructure.setIdleLoad(100d);
        CentralProcessor p = si.getHardware().getProcessor();
        ArrayList<Double> processorLoad = new ArrayList<>();
        for (int i=0; i<p.getLogicalProcessorCount(); i++)
            processorLoad.add(10d);
        cpuStructure.setProcessorLoad(processorLoad);
        Database dbObj = new Database();
        dbObj.insertIntoCpuTable(cpuStructure);
    }

    @Test(expected = Exception.class)
    public void insertIntoCpuTable_EmptyInputs1() throws Exception {
        Database dbObj = new Database(databaseUrl);
        dbObj.insertIntoCpuTable(new MetricCollectionStructures.cpuStructure());
    }

    @Test(expected = Exception.class)
    public void insertIntoCpuTable_EmptyInputs2() throws Exception {
        MetricCollectionStructures.cpuStructure cpuStructure = new MetricCollectionStructures.cpuStructure();
        cpuStructure.setTimestamp(System.currentTimeMillis());
        cpuStructure.setUptime(100L);
        cpuStructure.setUserLoad(100d);
        cpuStructure.setSystemLoad(100d);
        cpuStructure.setIdleLoad(100d);
        Database dbObj = new Database(databaseUrl);
        dbObj.insertIntoCpuTable(cpuStructure);
    }

    @Test(expected = Exception.class)
    public void insertIntoCpuTable_EmptyInputs3() throws Exception {
        MetricCollectionStructures.cpuStructure cpuStructure = new MetricCollectionStructures.cpuStructure();
        cpuStructure.setTimestamp(System.currentTimeMillis());
        cpuStructure.setUptime(100L);
        cpuStructure.setUserLoad(100d);
        cpuStructure.setSystemLoad(100d);
        cpuStructure.setIdleLoad(100d);
        CentralProcessor p = si.getHardware().getProcessor();
        ArrayList<Double> processorLoad = new ArrayList<>();
        for (int i=0; i<p.getLogicalProcessorCount(); i++)
            processorLoad.add(10d);
        processorLoad.remove(0);
        cpuStructure.setProcessorLoad(processorLoad);
        Database dbObj = new Database(databaseUrl);
        dbObj.insertIntoCpuTable(cpuStructure);
    }

    @Test(expected = Exception.class)
    public void insertIntoSensorsTable_NoConnection() throws Exception {
        MetricCollectionStructures.sensorsStructure sensorsStructure = new MetricCollectionStructures.sensorsStructure();
        sensorsStructure.setTimestamp(System.currentTimeMillis());
        sensorsStructure.setCpuTemperature(10d);
        sensorsStructure.setCpuVoltage(10d);
        sensorsStructure.setFans(new int[0]);
        Database dbObj = new Database();
        dbObj.insertIntoSensorsTable(sensorsStructure);
    }

    @Test(expected = Exception.class)
    public void insertIntoSensorsTable_EmptyInputs() throws Exception {
        Database dbObj = new Database(databaseUrl);
        dbObj.insertIntoSensorsTable(new MetricCollectionStructures.sensorsStructure());
    }

    @Test(expected = Exception.class)
    public void insertIntoMemoryTable_NoConnection() throws Exception {
        MetricCollectionStructures.memoryStructure memoryStructure = new MetricCollectionStructures.memoryStructure();
        memoryStructure.setTimestamp(System.currentTimeMillis());
        memoryStructure.setUsedMemory(10d);
        memoryStructure.setTotalMemory(10d);
        Database dbObj = new Database();
        dbObj.insertIntoMemoryTable(memoryStructure);
    }

    @Test(expected = Exception.class)
    public void insertIntoMemoryTable_EmptyInputs() throws Exception {
        Database dbObj = new Database(databaseUrl);
        dbObj.insertIntoMemoryTable(new MetricCollectionStructures.memoryStructure());
    }

    @Test(expected = Exception.class)
    public void insertIntoNetworkTable_NoConnection() throws Exception {
        MetricCollectionStructures.networkStructure networkStructure = new MetricCollectionStructures.networkStructure();
        networkStructure.setTimestamp(System.currentTimeMillis());
        networkStructure.setPacketsReceived(10);
        networkStructure.setPacketsSent(10);
        networkStructure.setSizeReceived("5 Gb");
        networkStructure.setSizeSent("5 Gb");
        Database dbObj = new Database();
        dbObj.insertIntoNetworkTable(networkStructure);
    }

    @Test(expected = Exception.class)
    public void insertIntoNetworkTable_EmptyInputs() throws Exception {
        Database dbObj = new Database(databaseUrl);
        dbObj.insertIntoNetworkTable(new MetricCollectionStructures.networkStructure());
    }

    @Test(expected = Exception.class)
    public void insertIntoProcessTable_NoConnection() throws Exception {
        MetricCollectionStructures.processStructure processStructure = new MetricCollectionStructures.processStructure();
        processStructure.setTimestamp(System.currentTimeMillis());
        processStructure.setNoOfThreads(10);
        processStructure.setNoOfProcesses(10);
        HashMap<String, List<Double>> processList = new HashMap<>();
        List<Double> processesList = new ArrayList<>();
        processesList.add(10d);
        processesList.add(10d);
        processList.put("Process1", processesList);
        processStructure.setProcessesList(processList);
        Database dbObj = new Database();
        dbObj.insertIntoProcessTable(processStructure);
    }

    @Test(expected = Exception.class)
    public void insertIntoProcessTable_EmptyInputs1() throws Exception {
        Database dbObj = new Database(databaseUrl);
        dbObj.insertIntoProcessTable(new MetricCollectionStructures.processStructure());
    }

    @Test(expected = Exception.class)
    public void insertIntoProcessTable_EmptyInputs2() throws Exception {
        MetricCollectionStructures.processStructure processStructure = new MetricCollectionStructures.processStructure();
        processStructure.setTimestamp(System.currentTimeMillis());
        processStructure.setNoOfThreads(10);
        processStructure.setNoOfProcesses(10);
        Database dbObj = new Database(databaseUrl);
        dbObj.insertIntoProcessTable(processStructure);
    }

    @Test(expected = Exception.class)
    public void insertIntoProcessTable_EmptyInputs3() throws Exception {
        MetricCollectionStructures.processStructure processStructure = new MetricCollectionStructures.processStructure();
        processStructure.setTimestamp(System.currentTimeMillis());
        processStructure.setNoOfThreads(10);
        processStructure.setNoOfProcesses(10);
        HashMap<String, List<Double>> processList = new HashMap<>();
        List<Double> processesList = new ArrayList<>();
        processesList.add(10d);
        processesList.add(10d);
        processList.put("", processesList);
        processStructure.setProcessesList(processList);
        Database dbObj = new Database(databaseUrl);
        dbObj.insertIntoProcessTable(processStructure);
    }

    @Test(expected = Exception.class)
    public void insertStartSessionIntoSessionTable_NoConnection() throws Exception {
        Database dbObj = new Database();
        dbObj.insertStartSessionIntoSessionTable(System.currentTimeMillis());
    }

    @Test(expected = Exception.class)
    public void insertEndSessionIntoSessionTable_NoConnection() throws Exception {
        Database dbObj = new Database();
        dbObj.insertEndSessionIntoSessionTable(System.currentTimeMillis(), System.currentTimeMillis());
    }

    @Test(expected = Exception.class)
    public void commit_NoConnection() throws Exception {
        Database dbObj = new Database();
        dbObj.commit();
    }
}