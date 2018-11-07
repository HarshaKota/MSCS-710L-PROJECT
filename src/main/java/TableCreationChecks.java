import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import oshi.hardware.CentralProcessor;
import oshi.hardware.PowerSource;
import oshi.hardware.Sensors;

import java.util.Arrays;

class TableCreationChecks {

    private static final Logger log = LogManager.getLogger(TableCreationChecks.class);

    /*                  CPU Table                   */

    // Check how many Logical CPUs the system has
    static int getLogicalCPUs(CentralProcessor processor) {

        return processor.getLogicalProcessorCount();
    }


    /*                  Power Table                   */

    // Check if the system has any power sources.
    static boolean checkPowerTable(PowerSource[] powerSources) {

        return powerSources.length != 0;
    }


    /*                  Sensors  Table                   */

    // Check if the system has fans
    static int getFans(Sensors sensors) {

        int fanArray[] = sensors.getFanSpeeds();

        for (int fans: fanArray) {
            if (fans == 0) {
                return 0;
            }
        }
        return fanArray.length;
    }

    static double getCpuVoltage(Sensors sensors) {
        double noValue = 999.0;

        if (sensors.getCpuVoltage() > 0.0) {
            return sensors.getCpuVoltage();
        }

        return noValue;
    }
}