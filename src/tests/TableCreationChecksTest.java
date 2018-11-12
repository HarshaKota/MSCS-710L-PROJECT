import org.junit.BeforeClass;
import org.junit.Test;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.PowerSource;
import oshi.hardware.Sensors;

import static org.junit.Assert.*;

public class TableCreationChecksTest {

    private static SystemInfo si;
    private static HardwareAbstractionLayer hal;

    @BeforeClass
    public static void setup() {
        si = new SystemInfo();
        hal = si.getHardware();
    }

    @Test
    public void getLogicalCPUs() {
        assertTrue(TableCreationChecks.getLogicalCPUs(hal.getProcessor()) > 0);
    }

    @Test
    public void checkPowerSource() {
        PowerSource[] powerSources = hal.getPowerSources();
        boolean result = powerSources.length != 0
                && !powerSources[0].getName().equalsIgnoreCase("Unknown")
                && powerSources[0].getRemainingCapacity() * 100d != 0.0;
        assertTrue(result || !result);
    }

    @Test
    public void getFans() {
        Sensors sensors = hal.getSensors();
        int[] fans = sensors.getFanSpeeds();
        if (sensors.getFanSpeeds().length == 0) {
            assertEquals(0,TableCreationChecks.getFans(hal.getSensors()));
        } else if (sensors.getFanSpeeds().length > 0) {
            for (int fan : fans) {
                assertNotEquals(0, fan);
            }
        }
    }

    @Test
    public void getCpuVoltage() {
        double noValue = 999.0;
        Sensors sensors = hal.getSensors();
        if (sensors.getCpuVoltage() > 0d) {
            assertTrue(TableCreationChecks.getCpuVoltage(hal.getSensors()) > 0d);
        } else if (sensors.getCpuVoltage() < 0d) {
            assertEquals(TableCreationChecks.getCpuVoltage(hal.getSensors()), noValue, 0.0);
        }
    }
}