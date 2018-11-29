package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * <h1>Store and Pass Data in structures</h1>
 * The MetricCollectionStructures class contains other classes. Each structure
 * stores metrics for a point in time for a specific metric. The act of storing data
 * in these structures allows us to pass data easily so we may avoid excessive calls
 * to the database.
 * <p>
 *
 * @author Harsha Kota, Christopher Byrnes, Bradley Lamitie
 * @version 1.0
 * @since   2018-11-29
 */
public class MetricCollectionStructures {

    /**
     * This class is used to store and access variables and data pertaining to
     * the power table metrics.
     */
    public static class powerStructure {

        long timestamp;
        int powerStatus;
        double batteryPercentage;

        // Sets

        /**
         * This method is used to get the timestamp
         * @return timestamp This is the time at which the metrics were collected
         */
        public long getTimestamp() {
            return timestamp;
        }

        /**
         * This method is used to set the timestamp
         * @param timestamp This is the time at which the metrics were collected
         */
        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        /**
         * This method is used to get the power status
         * NOTE: We use an int rather than a boolean because SQLite does not accept
         * bit or Boolean data types
         * @return int representing whether the battery is charging or not.
         */
        public int getPowerStatus() {
            return powerStatus;
        }

        /**
         * This method is used to set the power status
         * NOTE: We use an int rather than a boolean because SQLite does not accept
         * bit or Boolean data types
         * @param powerStatus represents whether the battery is charging or not.
         */
        public void setPowerStatus(int powerStatus) {
            this.powerStatus = powerStatus;
        }

        /**
         * This method is used to get the battery percentage
         * @return double representing the current percentage of power level.
         */
        public double getBatteryPercentage() {
            return batteryPercentage;
        }

        /**
         * This method is used to set the battery percentage
         * @param batteryPercentage represent the current percentage of power level.
         */
        public void setBatteryPercentage(double batteryPercentage) {
            this.batteryPercentage = batteryPercentage;
        }

        /**
         * This method is used to create a string that displays current values
         * of the structure
         * @see String
         */
        @Override
        public String toString() {
            return "powerStructure{" +
                    "timestamp=" + timestamp +
                    ", powerStatus=" + powerStatus +
                    ", batteryPercentage=" + batteryPercentage +
                    '}';
        }
    }

    /**
     * This class is used to store and access variables and data pertaining to
     * the cpu table metrics.
     */
    public static class cpuStructure {
        long timestamp;
        long uptime;
        double userLoad;
        double systemLoad;
        double idleLoad;
        ArrayList<Double> processorLoad;

        /**
         * This method is used to get the timestamp
         * @return timestamp This is the time at which the metrics were collected
         */
        public long getTimestamp() {
            return timestamp;
        }

        /**
         * This method is used to set the timestamp
         * @param timestamp This is the time at which the metrics were collected
         */
        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        /**
         * This method is used to get the time the system has been active
         * @return uptime This is the time the system has been active (since last shutdown)
         */
        public long getUptime() {
            return uptime;
        }

        /**
         * This method is used to set the time the system has been active
         * @param uptime This is the time the system has been active (since last shutdown)
         */
        public void setUptime(long uptime) {
            this.uptime = uptime;
        }

        /**
         * This method is used to get the userLoad
         * @return userLoad This is the value for userLoad on the CPU
         */
        public double getUserLoad() {
            return userLoad;
        }

        /**
         * This method is used to set the userLoad
         * @param userLoad This is the value for userLoad on the CPU
         */
        public void setUserLoad(double userLoad) {
            this.userLoad = userLoad;
        }

        /**
         * This method is used to get the systemLoad
         * @return systemLoad This is the value for systemLoad on the CPU
         */
        public double getSystemLoad() {
            return systemLoad;
        }

        /**
         * This method is used to set the systemLoad
         * @param systemLoad This is the value for systemLoad on the CPU
         */
        public void setSystemLoad(double systemLoad) {
            this.systemLoad = systemLoad;
        }

        /**
         * This method is used to get the idleLoad
         * @return idleLoad This is the value for idleLoad on the CPU
         */
        public double getIdleLoad() {
            return idleLoad;
        }

        /**
         * This method is used to set the idleLoad
         * @param idleLoad This is the value for idleLoad on the CPU
         */
        public void setIdleLoad(double idleLoad) {
            this.idleLoad = idleLoad;
        }

        /**
         * This method is used to set the processorLoad
         * @param processorLoad This is the list of processor loads on each logical processor
         */
        public void setProcessorLoad(ArrayList<Double> processorLoad) {
            this.processorLoad = processorLoad;
        }

        /**
         * This method is used to get the processorLoad
         * @return processorLoad This is the list of processor loads on each logical processor
         */
        ArrayList<Double> getProcessorLoad() { return processorLoad; }

        /**
         * This method is used to create a string that displays current values
         * of the structure
         * @see String
         */
        @Override
        public String toString() {
            return "cpuStructure{" +
                    "timestamp=" + timestamp +
                    ", uptime=" + uptime +
                    ", userLoad=" + userLoad +
                    ", systemLoad=" + systemLoad +
                    ", idleLoad=" + idleLoad +
                    ", processorLoad=" + processorLoad +
                    '}';
        }
    }

    /**
     * This class is used to store and access variables and data pertaining to
     * the sensors table metrics.
     */
    public static class sensorsStructure {
        long timestamp;
        double cpuTemperature = 999d;
        double cpuVoltage = 999d;
        ArrayList<Integer> fans;

        /**
         * This method is used to get the timestamp
         * @return timestamp This is the time at which the metrics were collected
         */
        public long getTimestamp() {
            return timestamp;
        }

        /**
         * This method is used to set the timestamp
         * @param timestamp This is the time at which the metrics were collected
         */
        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        /**
         * This method is used to get the temperature
         * @return cpuTemperature This is the temperature the computer is at
         */
        public double getCpuTemperature() {
            return cpuTemperature;
        }

        /**
         * This method is used to set the temperature
         * @param cpuTemperature This is the temperature the computer is at
         */
        public void setCpuTemperature(double cpuTemperature) {
            this.cpuTemperature = cpuTemperature;
        }

        /**
         * This method is used to get the CPU voltage
         * @return cpuVoltage This is the voltage the computer is at
         */
        public double getCpuVoltage() {
            return cpuVoltage;
        }

        /**
         * This method is used to set the CPU voltage
         * @param cpuVoltage This is the voltage the computer is at
         */
        public void setCpuVoltage(double cpuVoltage) {
            this.cpuVoltage = cpuVoltage;
        }

        /**
         * This method is used to get the fans
         * @return fans This is a list of the fan's speeds
         */
        public ArrayList<Integer> getFans() { return fans; }

        /**
         * This method is used to set the fans
         * @param fans This is a list of the fan's speeds
         */
        public void setFans(ArrayList<Integer> fans) { this.fans = fans; }

        /**
         * This method is used to create a string that displays current values
         * of the structure
         * @see String
         */
        @Override
        public String toString() {
            return "sensorsStructure{" +
                    "timestamp=" + timestamp +
                    ", cpuTemperature=" + cpuTemperature +
                    ", cpuVoltage=" + cpuVoltage +
                    ", fans=" + fans +
                    '}';
        }
    }

    /**
     * This class is used to store and access variables and data pertaining to
     * the memory table metrics.
     */
    public static class memoryStructure {
        long timestamp;
        double usedMemory;
        double totalMemory;

        /**
         * This method is used to get the timestamp
         * @return timestamp This is the time at which the metrics were collected
         */
        public long getTimestamp() {
            return timestamp;
        }

        /**
         * This method is used to set the timestamp
         * @param timestamp This is the time at which the metrics were collected
         */
        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        /**
         * This method is used to get the amount of RAM used
         * @return usedMemory This is the amount of RAM used
         */
        public double getUsedMemory() {
            return usedMemory;
        }

        /**
         * This method is used to set the amount of RAM used
         * @param usedMemory This is the amount of RAM used
         */
        public void setUsedMemory(double usedMemory) {
            this.usedMemory = usedMemory;
        }

        /**
         * This method is used to get the total amount of RAM available
         * @return totalMemory This is the total amount of RAM available
         */
        public double getTotalMemory() {
            return totalMemory;
        }

        /**
         * This method is used to set the total amount of RAM available
         * @param totalMemory This is the total amount of RAM available
         */
        public void setTotalMemory(double totalMemory) {
            this.totalMemory = totalMemory;
        }

        /**
         * This method is used to create a string that displays current values
         * of the structure
         * @see String
         */
        @Override
        public String toString() {
            return "memoryStructure{" +
                    "timestamp=" + timestamp +
                    ", usedMemory=" + usedMemory +
                    ", totalMemory=" + totalMemory +
                    '}';
        }
    }

    /**
     * This class is used to store and access variables and data pertaining to
     * the network table metrics.
     */
    public static class networkStructure {
        long timestamp;
        long packetsReceived;
        long packetsSent;
        String sizeReceived;
        String sizeSent;

        /**
         * This method is used to get the timestamp
         * @return timestamp This is the time at which the metrics were collected
         */
        public long getTimestamp() {
            return timestamp;
        }

        /**
         * This method is used to set the timestamp
         * @param timestamp This is the time at which the metrics were collected
         */
        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        /**
         * This method is used to get a a value representing how many packets the user has received
         * @return packetsReceived This is a value representing how many packets the user has received
         */
        public long getPacketsReceived() {
            return packetsReceived;
        }

        /**
         * This method is used to set a a value representing how many packets the user has received
         * @param packetsReceived This is a value representing how many packets the user has received
         */
        public void setPacketsReceived(long packetsReceived) {
            this.packetsReceived = packetsReceived;
        }

        /**
         * This method is used to get a a value representing how many packets the user has sent
         * @return packetsSent This is a value representing how many packets the user has sent
         */
        public long getPacketsSent() {
            return packetsSent;
        }

        /**
         * This method is used to set a a value representing how many packets the user has sent
         * @param packetsSent This is a value representing how many packets the user has sent
         */
        public void setPacketsSent(long packetsSent) {
            this.packetsSent = packetsSent;
        }

        /**
         * This method is used to get a a value representing how much data the user has received
         * @return sizeReceived This is a value representing how much data the user has received
         */
        public String getSizeReceived() {
            return sizeReceived;
        }

        /**
         * This method is used to set a a value representing how much data the user has received
         * @param sizeReceived This is a value representing how much data the user has received
         */
        public void setSizeReceived(String sizeReceived) {
            this.sizeReceived = sizeReceived;
        }

        /**
         * This method is used to get a a value representing how much data the user has sent
         * @return sizeSent This is a value representing how much data the user has sent
         */
        public String getSizeSent() {
            return sizeSent;
        }

        /**
         * This method is used to set a a value representing how much data the user has sent
         * @param sizeSent This is a value representing how much data the user has sent
         */
        public void setSizeSent(String sizeSent) {
            this.sizeSent = sizeSent;
        }

        /**
         * This method is used to create a string that displays current values
         * of the structure
         * @see String
         */
        @Override
        public String toString() {
            return "networkStructure{" +
                    "timestamp=" + timestamp +
                    ", packetsReceived=" + packetsReceived +
                    ", packetsSent=" + packetsSent +
                    ", sizeReceived='" + sizeReceived + '\'' +
                    ", sizeSent='" + sizeSent + '\'' +
                    '}';
        }
    }

    /**
     * This class is used to store and access variables and data pertaining to
     * the process table metrics.
     */
    public static class processStructure {
        long timestamp;
        long noOfProcesses;
        long noOfThreads;
        public HashMap<String, List<Double>> processesList;

        /**
         * This method is used to get the timestamp
         * @return timestamp This is the time at which the metrics were collected
         */
        public long getTimestamp() {
            return timestamp;
        }

        /**
         * This method is used to set the timestamp
         * @param timestamp This is the time at which the metrics were collected
         */
        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        /**
         * This method is used to get the number of processes running
         * @return noOfProcesses This is the number of processes running on the system
         */
        public double getNoOfProcesses() {
            return noOfProcesses;
        }

        /**
         * This method is used to set the number of processes running
         * @param noOfProcesses This is the number of processes running on the system
         */
        public void setNoOfProcesses(long noOfProcesses) {
            this.noOfProcesses = noOfProcesses;
        }

        /**
         * This method is used to get the number of threads running
         * @return noOfThreads This is the number of threads running on the system
         */
        public double getNoOfThreads() {
            return noOfThreads;
        }

        /**
         * This method is used to set the number of threads running
         * @param noOfThreads This is the number of threads running on the system
         */
        public void setNoOfThreads(long noOfThreads) {
            this.noOfThreads = noOfThreads;
        }

        /**
         * This method is used to get a list of processes running on a system
         * @return processesList This is the list of processes by name and includes RAM
         * usage and CPU usage in percentage
         */
        HashMap<String, List<Double>> getProcessesList() { return processesList; }

        /**
         * This method is used to set a list of processes running on a system
         * @param processesList This is the list of processes by name and includes RAM
         * usage and CPU usage in percentage
         */
        public void setProcessesList(HashMap<String, List<Double>> processesList) {
            this.processesList = processesList;
        }

        /**
         * This method is used to create a string that displays current values
         * of the structure
         * @see String
         */
        @Override
        public String toString() {
            return "processStructure{" +
                    "timestamp=" + timestamp +
                    ", noOfProcesses=" + noOfProcesses +
                    ", noOfThreads=" + noOfThreads +
                    ", processesList=" + processesList +
                    '}';
        }
    }
}
