import org.junit.BeforeClass;
import org.junit.Test;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.PowerSource;
import oshi.software.os.OperatingSystem;

import static org.junit.Assert.*;

public class MetricCollectorTest {

    private static SystemInfo si;
    private static HardwareAbstractionLayer hal;
    private static OperatingSystem os;
    private static long timestamp;

    @BeforeClass
    public static void setup() {
         si = new SystemInfo();
         hal = si.getHardware();
         os = si.getOperatingSystem();
         timestamp = System.currentTimeMillis();
    }

    @Test
    public void getPower_NoPower() {
        Main.hasPowerSource = false;
        MetricCollectionStructures.powerStructure powerS = MetricCollector.getPower(timestamp, hal.getPowerSources());
        assertNull(powerS);
    }

    @Test
    public void getPower_CheckGetTimeRemaining() {
        PowerSource[] psArr = si.getHardware().getPowerSources();
        double epsilon = 1E-6;
        for(PowerSource ps: psArr) {
            assertTrue(ps.getTimeRemaining() > 0 || Math.abs(ps.getTimeRemaining() - -1) < epsilon
                    || Math.abs(ps.getTimeRemaining() - -2) < epsilon);
        }

    }

    @Test
    public void getPower_CheckGetRemainingCapacity() {
        PowerSource[] psArr = si.getHardware().getPowerSources();
        for(PowerSource ps: psArr) {
            assertTrue(ps.getRemainingCapacity() >= 0 && ps.getRemainingCapacity() <= 1);
        }
    }

    @Test
    public void getPower_OnSuccess() {
        Main.hasPowerSource = true;
        MetricCollectionStructures.powerStructure powerS = MetricCollector.getPower(timestamp, hal.getPowerSources());
        assertNotNull(powerS);
        assertTrue(powerS.getTimestamp() > 0);
        assertTrue(powerS.getPowerStatus() == 0 || powerS.getPowerStatus() == 1);
        assertTrue(powerS.getBatteryPercentage() >= 0d || powerS.getBatteryPercentage() <= 100d);
    }

}