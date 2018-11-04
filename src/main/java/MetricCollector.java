import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import oshi.SystemInfo;
import oshi.hardware.*;
import java.util.concurrent.CountDownLatch;

public class MetricCollector {

    private static final Logger log = LogManager.getLogger(UI.class);
    private static int noOfCallsTogetPower = 0;
    private static int noOfCallsTogetCPU = 0;


    // Collects Power Info
    //      1 - charging
    //      0 - discharging
    // Returns powerStructure
    public static void getPower(CountDownLatch mainLatch, PowerSource[] powerSources) {
        noOfCallsTogetPower++;

        // Set up the System Info and Hardware Info Objects
        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();

        MetricCollectionStructures.powerStructure pS = new MetricCollectionStructures.powerStructure();

        if (TableCreationChecks.checkPowerTable(hal.getPowerSources())) {
            pS.setTimestamp(System.currentTimeMillis());
            double timeRemaining = powerSources[0].getTimeRemaining();
            // -1d indicates charging
            if (timeRemaining < -1d) {
                pS.setPowerStatus(1);
            // 0d indicates discharging
            } else if (timeRemaining < 0d) {
                pS.setPowerStatus(0);
            }
            for (PowerSource pSource : powerSources) {
                pS.setBatteryPercentage(Math.round(pSource.getRemainingCapacity() * 100d));
            }
        }

        // Insert into powerTable
        Database db = new Database();
        db.insertIntoPowerTable(pS);

        CountDownLatch latch = mainLatch;
        latch.countDown();
        System.out.println("Count: "+latch.getCount()+" getPower calls: "+noOfCallsTogetPower); //Sysout
    }

    // Collects CPU Info
    //
    //
    // Returns cpuStructure
    public static void getCPU(CountDownLatch mainLatch, CentralProcessor processor) {
        noOfCallsTogetCPU++;

        // Set up the System Info and Hardware Info Objects
        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();

        long[] prevTicks = processor.getSystemCpuLoadTicks();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            log.error("Failed when calcualting ticks");
        }
        long[] ticks = processor.getSystemCpuLoadTicks();
        long user = ticks[CentralProcessor.TickType.USER.getIndex()] - prevTicks[CentralProcessor.TickType.USER.getIndex()];
        long nice = ticks[CentralProcessor.TickType.NICE.getIndex()] - prevTicks[CentralProcessor.TickType.NICE.getIndex()];
        long sys = ticks[CentralProcessor.TickType.SYSTEM.getIndex()] - prevTicks[CentralProcessor.TickType.SYSTEM.getIndex()];
        long idle = ticks[CentralProcessor.TickType.IDLE.getIndex()] - prevTicks[CentralProcessor.TickType.IDLE.getIndex()];
        long iowait = ticks[CentralProcessor.TickType.IOWAIT.getIndex()] - prevTicks[CentralProcessor.TickType.IOWAIT.getIndex()];
        long irq = ticks[CentralProcessor.TickType.IRQ.getIndex()] - prevTicks[CentralProcessor.TickType.IRQ.getIndex()];
        long softirq = ticks[CentralProcessor.TickType.SOFTIRQ.getIndex()] - prevTicks[CentralProcessor.TickType.SOFTIRQ.getIndex()];
        long steal = ticks[CentralProcessor.TickType.STEAL.getIndex()] - prevTicks[CentralProcessor.TickType.STEAL.getIndex()];
        long totalCpu = user + nice + sys + idle + iowait + irq + softirq + steal;

        MetricCollectionStructures.cpuStructure cS = new MetricCollectionStructures.cpuStructure();

        cS.setTimestamp(System.currentTimeMillis());
        cS.setUptime(processor.getSystemUptime());
        cS.setUserLoad((float) (100d * user / totalCpu));
        cS.setSystemLoad((float) (100d * sys / totalCpu));
        cS.setIdelLoad((float) (100d * idle / totalCpu));
        double[] load = processor.getProcessorCpuLoadBetweenTicks();
        for (double eachLoad : load) {
            cS.setProcessorLoad((float) (eachLoad * 100));
        }

        // Insert into the cpuTable
        Database db = new Database();
        db.insertIntoCpuTable(cS);

        CountDownLatch latch = mainLatch;
        latch.countDown();
        System.out.println("Count: "+latch.getCount()+" getCPU calls: "+noOfCallsTogetCPU); //Sysout


    }
}