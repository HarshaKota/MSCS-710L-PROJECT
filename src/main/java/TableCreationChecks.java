import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import oshi.hardware.PowerSource;

import java.util.Arrays;

class TableCreationChecks {

    private static final Logger log = LogManager.getLogger(TableCreationChecks.class);

    // Check if the system has a battery
    static boolean getBatteryPercentage(PowerSource[] powerSources) {

        for (PowerSource pSource : powerSources) {
            if (powerSources.length < 0) {
                log.error(powerSources.length + " " + Arrays.toString(powerSources));
                return false;
            }
        }
        return true;
    }
}