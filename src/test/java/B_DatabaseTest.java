import main.*;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static org.mockito.BDDMockito.given;

@SuppressWarnings("ALL")
public class B_DatabaseTest {

    private static SystemInfo si;

    @BeforeClass
    public static void setup() {
        si = new SystemInfo();
    }

    @Test(expected = Exception.class)
    public void establishDatabaseConnection_Null() throws Exception {
        Database dbObj = new Database();
        dbObj.establishDatabaseConnection(null);
    }

    @Test
    public void establishDatabaseConnection_NotNull() throws Exception {
        main.Database dbObj = new main.Database();
        String databaseUrl = "jdbc:sqlite:main.MetricCollector.db";
        dbObj.establishDatabaseConnection(databaseUrl);
        dbObj.closeDatabaseConnection();
    }

    @Test(expected = Exception.class)
    public void checkSessionTable_Null() throws Exception {
        Database dbObj = new Database();
        dbObj.checkSessionTable();
    }

    @Test
    public void checkSessionTable_NotNull() {
        try {
            final Database testDatabase = Mockito.spy(new Database());
            testDatabase.establishDatabaseConnection( "jdbc:sqlite:main.MetricCollector.db");
            long val = new Random().nextLong();
            testDatabase.insertStartSessionIntoSessionTable(val);
            testDatabase.insertEndSessionIntoSessionTable(val,5);
            testDatabase.checkSessionTable();
            testDatabase.closeDatabaseConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test(expected = Exception.class)
    public void powerMetrics_nullTimes() throws Exception {
        final Database dbObj = Mockito.spy(new Database());
        dbObj.getPowerMetrics(null,null);
    }

    @Test(expected = Exception.class)
    public void powerMetrics_failedToRetreive() throws Exception {
        final Database dbObj = Mockito.spy(new Database());
        given(dbObj.getPowerMetrics(1L,1L)).willThrow(new Exception());
    }

    @Test(expected = Exception.class)
    public void cpuMetrics_nullTimes() throws Exception {
        final Database dbObj = Mockito.spy(new Database());
        dbObj.getCpuMetrics(null,null,"");
    }

    @Test(expected = Exception.class)
    public void cpuMetrics_failedToRetreive() throws Exception {
        final Database dbObj = Mockito.spy(new Database());
        given(dbObj.getCpuMetrics(1L,1L,"")).willThrow(new Exception());
    }

    @Test(expected = Exception.class)
    public void memoryMetrics_nullTimes() throws Exception {
        final Database dbObj = Mockito.spy(new Database());
        dbObj.getMemoryMetrics(null,null,"");
    }

    @Test(expected = Exception.class)
    public void memoryMetrics_failedToRetreive() throws Exception {
        final Database dbObj = Mockito.spy(new Database());
        given(dbObj.getMemoryMetrics(1L,1L,"")).willThrow(new Exception());
    }

    @Test(expected = Exception.class)
    public void networkMetrics_nullTimes() throws Exception {
        final Database dbObj = Mockito.spy(new Database());
        dbObj.getNetworkMetrics(null,null,"");
    }

    @Test(expected = Exception.class)
    public void networkMetrics_failedToRetreive() throws Exception {
        final Database dbObj = Mockito.spy(new Database());
        given(dbObj.getNetworkMetrics(1L,1L,"")).willThrow(new Exception());
    }

    @Test(expected = Exception.class)
    public void processMetrics_nullTimes() throws Exception {
        final Database dbObj = Mockito.spy(new Database());
        dbObj.getProcessMetrics(null,null,"");
    }

    @Test(expected = Exception.class)
    public void processMetrics_failedToRetreive() throws Exception {
        final Database dbObj = Mockito.spy(new Database());
        given(dbObj.getProcessMetrics(1L,1L,"")).willThrow(new Exception());
    }

    @Test(expected = Exception.class)
    public void sensorMetrics_nullTimes() throws Exception {
        final Database dbObj = Mockito.spy(new Database());
        dbObj.getSensorsMetrics(null,null,"");
    }

    @Test(expected = Exception.class)
    public void sensorMetrics_failedToRetreive() throws Exception {
        final Database dbObj = Mockito.spy(new Database());
        given(dbObj.getSensorsMetrics(1L,1L,"")).willThrow(new Exception());
    }

    @Test(expected = Exception.class)
    public void processInfoMetrics_failedToRetreive() throws Exception {
        final Database dbObj = Mockito.spy(new Database());
        given(dbObj.getProcessinfoMetrics(1L,1L,"","")).willThrow(new Exception());
    }

    @Test(expected = Exception.class)
    public void createPowerTable_Successful() throws Exception {
        Database dbObj = new Database();
        dbObj.createPowerTable();
    }

    @Test(expected = Exception.class)
    public void createSensorsTable_Successful() throws Exception {
        Database dbObj = new Database();
        dbObj.createSensorsTable();
    }

    @Test(expected = Exception.class)
    public void createMemoryTable_Successful() throws Exception {
        Database dbObj = new Database();
        dbObj.createMemoryTable();
    }

    @Test(expected = Exception.class)
    public void createProcessTable_Successful() throws Exception {
        Database dbObj = new Database();
        dbObj.createProcessTable();
        dbObj.closeDatabaseConnection();
    }

    @Test(expected = Exception.class)
    public void createCPUTable_Successful() throws Exception {
        Database dbObj = new Database();
        dbObj.createCPUTable();
    }

    @Test(expected = Exception.class)
    public void createNetworkTable_Successful() throws Exception {
        Database dbObj = new Database();
        dbObj.createNetworkTable();
    }

    @Test(expected = Exception.class)
    public void createSessionTable_Successful() throws Exception {
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
        Database dbObj = new Database();
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
    public void insertIntoCpuTable_EmptyStructure() throws Exception {
        Database dbObj = new Database();
        MetricCollectionStructures.cpuStructure cpuStructure = new MetricCollectionStructures.cpuStructure();
        cpuStructure.setIdleLoad(-1d);
        dbObj.insertIntoCpuTable(cpuStructure);
    }

    @Test(expected = Exception.class)
    public void insertIntoCpuTable_EmptyInputs1() throws Exception {
        Database dbObj = new Database();
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
        Database dbObj = new Database();
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
        Database dbObj = new Database();
        dbObj.insertIntoCpuTable(cpuStructure);
    }

    @Test(expected = Exception.class)
    public void insertIntoSensorsTable_NoConnection() throws Exception {
        MetricCollectionStructures.sensorsStructure sensorsStructure = new MetricCollectionStructures.sensorsStructure();
        sensorsStructure.setTimestamp(System.currentTimeMillis());
        sensorsStructure.setCpuTemperature(10d);
        sensorsStructure.setCpuVoltage(10d);
        sensorsStructure.setFans(new ArrayList<>());
        Database dbObj = new Database();
        dbObj.insertIntoSensorsTable(sensorsStructure);
    }

    @Test(expected = Exception.class)
    public void insertIntoSensorsTable_NoFans() throws Exception {
        MetricCollectionStructures.sensorsStructure sensorsStructure = new MetricCollectionStructures.sensorsStructure();
        sensorsStructure.setTimestamp(System.currentTimeMillis());
        sensorsStructure.setCpuTemperature(10d);
        sensorsStructure.setCpuVoltage(10d);
        sensorsStructure.setFans(new ArrayList<>());
        Database dbObj = new Database();
        dbObj.insertIntoSensorsTable(sensorsStructure);
    }
    @Test(expected = Exception.class)
    public void insertIntoSensorsTable_HasFan() throws Exception {
        MetricCollectionStructures.sensorsStructure sensorsStructure = new MetricCollectionStructures.sensorsStructure();
        sensorsStructure.setTimestamp(System.currentTimeMillis());
        sensorsStructure.setCpuTemperature(10d);
        sensorsStructure.setCpuVoltage(10d);
        ArrayList testFans = new ArrayList<Integer>();
        testFans.add(0,52);
        sensorsStructure.setFans(testFans);
        Database dbObj = new Database();
        dbObj.insertIntoSensorsTable(sensorsStructure);
    }

    @Test(expected = Exception.class)
    public void insertIntoSensorsTable_HasMultipleFans() throws Exception {
        MetricCollectionStructures.sensorsStructure sensorsStructure = new MetricCollectionStructures.sensorsStructure();
        sensorsStructure.setTimestamp(System.currentTimeMillis());
        sensorsStructure.setCpuTemperature(10d);
        sensorsStructure.setCpuVoltage(10d);
        ArrayList testFans = new ArrayList<Integer>();
        testFans.add(0,52);
        testFans.add(1,56);
        sensorsStructure.setFans(testFans);
        Database dbObj = new Database();
        dbObj.insertIntoSensorsTable(sensorsStructure);
    }

    @Test(expected = Exception.class)
    public void insertIntoSensorsTable_EmptyInputs() throws Exception {
        Database dbObj = new Database();
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
        Database dbObj = new Database();
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
        Database dbObj = new Database();
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
        Database dbObj = new Database();
        dbObj.insertIntoProcessTable(new MetricCollectionStructures.processStructure());
    }

    @Test(expected = Exception.class)
    public void insertIntoProcessTable_EmptyInputs2() throws Exception {
        MetricCollectionStructures.processStructure processStructure = new MetricCollectionStructures.processStructure();
        processStructure.setTimestamp(System.currentTimeMillis());
        processStructure.setNoOfThreads(10);
        processStructure.setNoOfProcesses(10);
        Database dbObj = new Database();
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
        Database dbObj = new Database();
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