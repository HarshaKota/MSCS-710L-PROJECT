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
}