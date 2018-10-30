import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import oshi.hardware.CentralProcessor;
import oshi.hardware.PowerSource;
import oshi.hardware.Sensors;

import java.util.Arrays;

class TableCreationChecks {

    private static final Logger log = LogManager.getLogger(TableCreationChecks.class);

    // Check if the Power Table has any errors.
    static boolean checkPowerTable(PowerSource[] powerSources) {

        for (PowerSource pSource : powerSources) {
            if (powerSources.length < 0) {
                log.error(powerSources.length + " " + Arrays.toString(powerSources));
                return false;
            }
        }
        return true;
    }

    // Check if CPU Table has any errors
    static Boolean checkCPUTable(CentralProcessor processor) {

        int noOfLogicalCPUs = processor.getLogicalProcessorCount();

        if (noOfLogicalCPUs <= 0) {
            log.error(noOfLogicalCPUs);
            return false;
        }
        return true;
    }

    // Check if Sensor Table has any errors.
    static Boolean checkSensorsTable(Sensors sensors) {

        int fanArray[] = sensors.getFanSpeeds();

        if(fanArray.length <= 0){
            return false;
        }
        return true;
    }

    // Check if the system has fans
    static int getFans(Sensors sensors) {

        int fanArray[] = sensors.getFanSpeeds();

        return fanArray.length;
    }

    // Check how many Logical CPUs the system has
    static int getLogicalCPUs(CentralProcessor processor) {

        return processor.getLogicalProcessorCount();
    }
}