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
}