package main;

import oshi.hardware.CentralProcessor;
import oshi.hardware.PowerSource;
import oshi.hardware.Sensors;

/**
 * This class provides methods that will be used to
 * determine what tables/columns are needed when creating the tables
 */
public class TableCreationChecks {

    /**
     * Checks how many Logical CPUs the system has
     *
     * @param processor OSHI Library CentralProcessor object that provides methods to access processor metrics
     * @return Number of logical CPUS's as an int
     */
    public static int getLogicalCPUs(CentralProcessor processor) {

        return processor.getLogicalProcessorCount();
    }

    /**
     * Checks if the system has any power sources.
     *
     * @param powerSources OSHI Library PowerSources object that provides methods to access power metrics
     * @return Sets the Main class hasPowerSource variable with a boolean and returns it
     */
    public static boolean checkPowerSource(PowerSource[] powerSources) {

        Main.hasPowerSource = powerSources.length != 0
                && !powerSources[0].getName().equalsIgnoreCase("Unknown")
                && powerSources[0].getRemainingCapacity() * 100d != 0.0;

        return Main.hasPowerSource;
    }


    /**
     * Check if the system has fans
     *
     * @param sensors OSHI Library Sensors object that provides methods to access sensor metrics
     * @return Number of fans as an int
     */
    public static int getFans(Sensors sensors) {

        int fanArray[] = sensors.getFanSpeeds();

        for (int fans: fanArray) {
            if (fans == 0) {
                return 0;
            }
        }
        return fanArray.length;
    }

    /**
     * Checks the CPU Voltage of the system
     *
     * @param sensors OSHI Library Sensors object that provides methods to access sensor metrics
     * @return CPU Voltage as a double
     */
    public static double getCpuVoltage(Sensors sensors) {
        double noValue = 999.0;

        if (sensors.getCpuVoltage() > 0.0) {
            return sensors.getCpuVoltage();
        }

        return noValue;
    }
}