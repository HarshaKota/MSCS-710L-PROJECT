import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import oshi.SystemInfo;
import oshi.hardware.*;
import java.util.concurrent.CountDownLatch;

public class MetricCollector {

    private static final Logger log = LogManager.getLogger(UI.class);
    private static int noOfCallsTogetPower = 0;


    // Collects Power Info
    //      1 - charging
    //      0 - discharging
    // Returns powerStructure
    public static void getPower(CountDownLatch mainLatch, PowerSource[] powerSources) {
        noOfCallsTogetPower++;
        System.out.println(Thread.currentThread().getName()+" Status: "+Main.applicationOpen); //Sysout

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

        Database db = new Database();
        db.insertIntoPowerTable(pS);

        CountDownLatch latch = mainLatch;
        latch.countDown();
        System.out.println("Count: "+latch.getCount()+" getCPU calls: "+noOfCallsTogetPower); //Sysout
    }
}