import org.junit.BeforeClass;
import org.junit.Test;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;

import static org.junit.Assert.*;

public class DatabaseTest {

    private static SystemInfo si;
    private static HardwareAbstractionLayer hal;
    private static OperatingSystem os;

    @BeforeClass
    public static void setup() {
        si = new SystemInfo();
        hal = si.getHardware();
        os = si.getOperatingSystem();
    }

    @Test
    public void establishDatabaseConnection_Null()  {
        Database dbObj = new Database("Test");
        dbObj.establishDatabaseConnection(null, null);
    }


}