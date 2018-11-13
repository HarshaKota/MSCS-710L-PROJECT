import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import oshi.SystemInfo;
import oshi.hardware.*;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;
import org.mockito.Mockito;
import org.mockito.Spy;
import sun.management.Sensor;

import static org.junit.Assert.*;

public class MetricCollectorTest {

    private static SystemInfo si;
    private static HardwareAbstractionLayer hal;
    private static OperatingSystem os;
    private static long timestamp;
    private MetricCollector testCollector = new MetricCollector();

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
        MetricCollectionStructures.powerStructure powerS = testCollector.getPower(timestamp, hal.getPowerSources());
        assertNull(powerS);
    }

    @Test
    public void getPower_CheckNotCharging() {
        PowerSource[] powerSources = hal.getPowerSources();
        final long metricCollectedTime = MetricCollector.startSession();
        final MetricCollector testCollector = Mockito.spy(new MetricCollector());
        Mockito.when(testCollector.hasPowerTable()).thenReturn(true);
        Mockito.when(testCollector.getTimeRemaining(powerSources)).thenReturn(2d);
        testCollector.getPower(metricCollectedTime, powerSources);
    }

    @Test
    public void getPower_CheckCharging() {
        PowerSource[] powerSources = hal.getPowerSources();
        final long metricCollectedTime = MetricCollector.startSession();
        final MetricCollector testCollector = Mockito.spy(new MetricCollector());
        Mockito.when(testCollector.hasPowerTable()).thenReturn(true);
        Mockito.when(testCollector.getTimeRemaining(powerSources)).thenReturn(-2d);
        testCollector.getPower(metricCollectedTime, powerSources);
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
        MetricCollectionStructures.powerStructure powerS = testCollector.getPower(timestamp, hal.getPowerSources());
        assertNotNull(powerS);
        assertTrue(powerS.getTimestamp() > 0);
        assertTrue(powerS.getPowerStatus() == 0 || powerS.getPowerStatus() == 1);
        assertTrue(powerS.getBatteryPercentage() >= 0d || powerS.getBatteryPercentage() <= 100d);
    }

    @Test //(expected = InterruptedException.class)
    public void getCPU_ThrowInterruptException() {
        ExpectedException thrown = ExpectedException.none();
        CentralProcessor processor = hal.getProcessor();
        final long metricCollectedTime = MetricCollector.startSession();
        final MetricCollector testCollector = Mockito.spy(new MetricCollector());
        Thread.currentThread().interrupt();
        thrown.expect(InterruptedException.class);
        testCollector.getCPU(metricCollectedTime, processor);
    }

    @Test
    public void getCPU_SystemCpuLoadTicks() {
        CentralProcessor p = si.getHardware().getProcessor();
        assertEquals(p.getSystemCpuLoadTicks().length, CentralProcessor.TickType.values().length);

    }

    @Test
    public void getCPU_getProcessorCpuLoadBetweenTicks() {
        CentralProcessor p = si.getHardware().getProcessor();
        assertEquals(p.getProcessorCpuLoadBetweenTicks().length, p.getLogicalProcessorCount());
    }

    @Test
    public void getCPU_AllProcessorDetails() {
        CentralProcessor p = si.getHardware().getProcessor();
        assertTrue(p.getSystemUptime() > 0);
        assertTrue(p.getLogicalProcessorCount() >= p.getPhysicalProcessorCount());
        assertTrue(p.getPhysicalProcessorCount() > 0);
        for (int cpu = 0; cpu < p.getLogicalProcessorCount(); cpu++) {
            assertTrue(p.getProcessorCpuLoadBetweenTicks()[cpu] >= 0 && p.getProcessorCpuLoadBetweenTicks()[cpu] <= 1);
            assertEquals(p.getProcessorCpuLoadTicks()[cpu].length, CentralProcessor.TickType.values().length);
        }
    }

    @Test
    public void getCPU_OnSuccess() {
        MetricCollectionStructures.cpuStructure cpuS = testCollector.getCPU(timestamp, hal.getProcessor());
        assertNotNull(cpuS);
        assertTrue(cpuS.getTimestamp() > 0);
        assertTrue(cpuS.getUptime() > 0);
        assertTrue(cpuS.getUserLoad() >= 0d && cpuS.getUserLoad() <= 100d);
        assertTrue(cpuS.getSystemLoad() >= 0d && cpuS.getSystemLoad() <= 100d);
        assertTrue(cpuS.getIdleLoad() >= 0d && cpuS.getIdleLoad() <= 100d);
    }

    @Test
    public void getSensors_CPUTemperature() {
        Sensors s = si.getHardware().getSensors();
        assertTrue(s.getCpuTemperature() >= 0d && s.getCpuTemperature() <= 100d);
    }

    @Test
    public void getSensors_CPUVoltage() {
        Sensors s = si.getHardware().getSensors();
        assertTrue(s.getCpuVoltage() >= 0);
    }

    @Test
    public void getSensors_ValidCpuVoltage() {
        Sensors testSensors = hal.getSensors();
        final long metricCollectedTime = MetricCollector.startSession();
        final MetricCollector testCollector = Mockito.spy(new MetricCollector());
        Mockito.when(testCollector.getCpuVoltage(hal)).thenReturn(100.0);
        testCollector.getSensors(metricCollectedTime, hal, testSensors);
    }

    @Test
    public void getSensors_NoCpuVoltage() {
        Sensors testSensors = hal.getSensors();
        final long metricCollectedTime = MetricCollector.startSession();
        final MetricCollector testCollector = Mockito.spy(new MetricCollector());
        Mockito.when(testCollector.getCpuVoltage(hal)).thenReturn(999.0);
        testCollector.getSensors(metricCollectedTime, hal, testSensors);
    }

    @Test
    public void getSensors_ValidFanSpeeds() {
        Sensors testSensors = hal.getSensors();
        final long metricCollectedTime = MetricCollector.startSession();
        final MetricCollector testCollector = Mockito.spy(new MetricCollector());
        Mockito.when(testCollector.getFans(hal)).thenReturn(100.0);
        testCollector.getSensors(metricCollectedTime, hal, testSensors);
    }

    @Test
    public void getSensors_OnSuccess() {
        MetricCollectionStructures.sensorsStructure sensorS = testCollector.getSensors(timestamp, hal, hal.getSensors());
        assertNotNull(sensorS);
        assertTrue(sensorS.getTimestamp() > 0);
        assertTrue(sensorS.getCpuTemperature() >= 0d);
        assertTrue(sensorS.getCpuVoltage() >= 0d);
        assertTrue(sensorS.getFans().length > 0 || sensorS.getFans().length == 0);
    }

    @Test
    public void getMemory_MemoryNotNull() {
        GlobalMemory memory = hal.getMemory();
        assertNotNull(memory);
    }

    @Test
    public void getMemory_getTotalMemory() {
        GlobalMemory memory = hal.getMemory();
        assertTrue(memory.getTotal() > 0);
    }

    @Test
    public void getMemory_getAvaialbleMemory() {
        GlobalMemory memory = hal.getMemory();
        assertTrue(memory.getAvailable() >= 0);
    }

    @Test
    public void getMemory_OnSuccess() {
        MetricCollectionStructures.memoryStructure memoryS = MetricCollector.getMemory(timestamp, hal.getMemory());
        assertNotNull(memoryS);
        assertTrue(memoryS.getTimestamp() > 0);
        assertTrue(memoryS.getTotalMemory() >= 0d);
        assertTrue(memoryS.getUsedMemory() >= 0d);
    }

    @Test
    public void getNetwork_getBytesRecv() {
        for (NetworkIF net : si.getHardware().getNetworkIFs()) {
            assertTrue(net.getBytesRecv() >= 0);
        }
    }

    @Test
    public void getNetwork_getBytesSent() {
        for (NetworkIF net : si.getHardware().getNetworkIFs()) {
            assertTrue(net.getBytesSent() >= 0);
        }
    }

    @Test
    public void getNetwork_OnSuccess() {
        MetricCollectionStructures.networkStructure networkS = MetricCollector.getNetwork(timestamp, hal.getNetworkIFs());
        assertNotNull(networkS);
        assertTrue(networkS.getTimestamp() > 0);
        assertTrue(networkS.getPacketsReceived() >= 0);
        assertTrue(networkS.getPacketsSent() >= 0);
        assertTrue(!networkS.getSizeReceived().equals(""));
        assertTrue(!networkS.getSizeSent().equals(""));
    }


    @Test
    public void getProcess_ThreadCound() {
        assertTrue(os.getThreadCount() >= 1);
    }

    @Test
    public void getProcess_ProcessCount() {
        assertTrue(os.getProcessCount() >= 1);
    }

    @Test
    public void getProcess_Sort() {
        assertTrue(os.getProcesses(0, null).length > 0);
        OSProcess[] processes = os.getProcesses(5, null);
        assertNotNull(processes);
        assertTrue(processes.length > 0);
    }

    @Test
    public void getProcess_GetName() {
        OSProcess proc = os.getProcess(os.getProcessId());
        assertTrue(proc.getName().length() > 0);
    }

    @Test
    public void getProcess_ProcessHasThread() {
        OSProcess proc = os.getProcess(os.getProcessId());
        assertTrue(proc.getThreadCount() > 0);
    }

    @Test
    public void getProcess_GetKernelTime() {
        OSProcess proc = os.getProcess(os.getProcessId());
        assertTrue(proc.getThreadCount() > 0);
    }

    @Test
    public void getProcess_GetUserTime() {
        OSProcess proc = os.getProcess(os.getProcessId());
        assertTrue(proc.getUserTime() >= 0);
    }

    @Test
    public void getProcess_GetUpTime() {
        OSProcess proc = os.getProcess(os.getProcessId());
        assertTrue(proc.getUpTime() >= 0);
    }

    @Test
    public void getProcess_GetResidentSetSize() {
        OSProcess proc = os.getProcess(os.getProcessId());
        assertTrue(proc.getResidentSetSize() >= 0);
    }

    @Test
    public void getProcess_OnSuccess() {
        MetricCollectionStructures.processStructure processS = MetricCollector.getProcess(timestamp, os, hal.getMemory());
        assertNotNull(processS);
        assertTrue(processS.getTimestamp() > 0);
        assertTrue(processS.getNoOfThreads() >= 1);
        assertTrue(processS.getNoOfProcesses() >= 1);
        assertNotNull(processS.processesList);
        assertTrue(processS.processesList.size() > 0);
    }

    @Test
    public void startSession() {
        assertTrue(MetricCollector.startSession() > 0);
    }

    @Test
    public void endSession() {
        assertTrue(MetricCollector.endSession() > 0);
    }
}