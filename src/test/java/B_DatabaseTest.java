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

    private static SystemInfo si= new SystemInfo();;
    static String databaseUrl = "jdbc:sqlite:MetricCollector.db";

    @BeforeClass
    static public void establishDatabaseConnection_NotNull() throws Exception {
        main.Database dbObj = new main.Database();
        dbObj.establishDatabaseConnection(databaseUrl);
    }

    @Test
    public void checkSessionTable_NotNull() throws Exception {
        final Database testDatabase = Mockito.spy(new Database());
        long val = new Random().nextLong();
        testDatabase.establishDatabaseConnection(databaseUrl);
        testDatabase.insertStartSessionIntoSessionTable(val);
        testDatabase.insertEndSessionIntoSessionTable(val,5);
        testDatabase.checkSessionTable();
    }

    @Test
    public void createPowerTable_Successful() throws Exception {
        Database dbObj = new Database();
        dbObj.createPowerTable();
    }

    @Test
    public void createSensorsTable_Successful() throws Exception {
        Database dbObj = new Database();
        dbObj.createSensorsTable();
    }

    @Test
    public void createMemoryTable_Successful() throws Exception {
        Database dbObj = new Database();
        dbObj.createMemoryTable();
    }

    @Test
    public void createProcessTable_Successful() throws Exception {
        Database dbObj = new Database();
        dbObj.createProcessTable();
    }

    @Test
    public void createCPUTable_Successful() throws Exception {
        Database dbObj = new Database();
        dbObj.createCPUTable();
    }

    @Test
    public void createNetworkTable_Successful() throws Exception {
        Database dbObj = new Database();
        dbObj.createNetworkTable();
    }

    @Test
    public void createSessionTable_Successful() throws Exception {
        Database dbObj = new Database();
        dbObj.createSessionTable();
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

}