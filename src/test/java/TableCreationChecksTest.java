import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.PowerSource;
import oshi.hardware.Sensors;
import oshi.hardware.platform.windows.WindowsPowerSource;

import static org.junit.Assert.*;

public class TableCreationChecksTest {

    private static HardwareAbstractionLayer hal;

    @BeforeClass
    public static void setup() {
        SystemInfo si = new SystemInfo();
        hal = si.getHardware();
    }

    @Test
    public void getLogicalCPUs() {
        assertTrue(TableCreationChecks.getLogicalCPUs(hal.getProcessor()) > 0);
    }

    @Test
    public void checkPowerSource_InvalidPowerSource() {
        final TableCreationChecks testTableChecks = Mockito.spy(new TableCreationChecks());
        PowerSource[] powerSources = new PowerSource[0];
        TableCreationChecks.checkPowerSource(powerSources);
    }

    @Test
    public void checkPowerSource_ValidPowerSource() {
        final TableCreationChecks testTableChecks = Mockito.spy(new TableCreationChecks());
        PowerSource[] powerSources = new PowerSource[1];
        powerSources[0] = new WindowsPowerSource("TestBatteryName", 0.99d, 52.0d);
        TableCreationChecks.checkPowerSource(powerSources);
    }

    @Test
    public void getFans_ValidFanSpeeds() {
        final Sensors testSensors = Mockito.spy(hal.getSensors());
        final TableCreationChecks testTableChecks = Mockito.spy(new TableCreationChecks());
        int[] fanArray =new int[1];
        Mockito.when(testSensors.getFanSpeeds()).thenReturn(fanArray);
        TableCreationChecks.getFans(testSensors);
    }

    @Test
    public void getFans_InvalidFanSpeeds() {
        final Sensors testSensors = Mockito.spy(hal.getSensors());
        final TableCreationChecks testTableChecks = Mockito.spy(new TableCreationChecks());
        int[] fanArray =new int[1];
        fanArray[0] = 1;
        Mockito.when(testSensors.getFanSpeeds()).thenReturn(fanArray);
        TableCreationChecks.getFans(testSensors);
    }

    @Test
    public void getCpuVoltage_InvalidVoltage() {
        final Sensors testSensors = Mockito.spy(hal.getSensors());
        final TableCreationChecks testTableChecks = Mockito.spy(new TableCreationChecks());
        Mockito.when(testSensors.getCpuVoltage()).thenReturn(999.0d);
        TableCreationChecks.getCpuVoltage(testSensors);
    }

    @Test
    public void getCpuVoltage_ValidVoltage() {
        final Sensors testSensors = Mockito.spy(hal.getSensors());
        final TableCreationChecks testTableChecks = Mockito.spy(new TableCreationChecks());
        Mockito.when(testSensors.getCpuVoltage()).thenReturn(0.0d);
        TableCreationChecks.getCpuVoltage(testSensors);
    }
}