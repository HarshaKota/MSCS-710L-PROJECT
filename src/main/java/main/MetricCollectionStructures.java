package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * MetricCollectionStructures class provides structured classes for each metric
 * such that objects of these classes can be passed around when populated
 */
public class MetricCollectionStructures {

    public static class powerStructure {

        long timestamp;
        int powerStatus;
        double batteryPercentage;

        // Sets
        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public void setPowerStatus(int powerStatus) {
            this.powerStatus = powerStatus;
        }

        public void setBatteryPercentage(double batteryPercentage) {
            this.batteryPercentage = batteryPercentage;
        }

        // Gets
        public int getPowerStatus() {
            return powerStatus;
        }

        public double getBatteryPercentage() {
            return batteryPercentage;
        }

        public long getTimestamp() {
            return timestamp;
        }

        @Override
        public String toString() {
            return "powerStructure{" +
                    "timestamp=" + timestamp +
                    ", powerStatus=" + powerStatus +
                    ", batteryPercentage=" + batteryPercentage +
                    '}';
        }
    }

    public static class cpuStructure {
        long timestamp;
        long uptime;
        double userLoad;
        double systemLoad;
        double idleLoad;
        ArrayList<Double> processorLoad;

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public long getUptime() {
            return uptime;
        }

        public void setUptime(long uptime) {
            this.uptime = uptime;
        }

        public double getUserLoad() {
            return userLoad;
        }

        public void setUserLoad(double userLoad) {
            this.userLoad = userLoad;
        }

        public double getSystemLoad() {
            return systemLoad;
        }

        public void setSystemLoad(double systemLoad) {
            this.systemLoad = systemLoad;
        }

        public double getIdleLoad() {
            return idleLoad;
        }

        public void setIdleLoad(double idleLoad) {
            this.idleLoad = idleLoad;
        }

        public void setProcessorLoad(ArrayList<Double> processorLoad) {
            this.processorLoad = processorLoad;
        }

        ArrayList<Double> getProcessorLoad() { return processorLoad; }

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

    public static class sensorsStructure {
        long timestamp;
        double cpuTemperature = 999d;
        double cpuVoltage = 999d;
        ArrayList<Integer> fans;

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public double getCpuTemperature() {
            return cpuTemperature;
        }

        public void setCpuTemperature(double cpuTemperature) {
            this.cpuTemperature = cpuTemperature;
        }

        public double getCpuVoltage() {
            return cpuVoltage;
        }

        public void setCpuVoltage(double cpuVoltage) {
            this.cpuVoltage = cpuVoltage;
        }

        public ArrayList<Integer> getFans() { return fans; }

        public void setFans(ArrayList<Integer> fans) { this.fans = fans; }

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

    public static class memoryStructure {
        long timestamp;
        double usedMemory;
        double totalMemory;

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public double getUsedMemory() {
            return usedMemory;
        }

        public void setUsedMemory(double usedMemory) {
            this.usedMemory = usedMemory;
        }

        public double getTotalMemory() {
            return totalMemory;
        }

        public void setTotalMemory(double totalMemory) {
            this.totalMemory = totalMemory;
        }

        @Override
        public String toString() {
            return "memoryStructure{" +
                    "timestamp=" + timestamp +
                    ", usedMemory=" + usedMemory +
                    ", totalMemory=" + totalMemory +
                    '}';
        }
    }

    public static class networkStructure {
        long timestamp;
        long packetsReceived;
        long packetsSent;
        String sizeReceived;
        String sizeSent;

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public long getPacketsReceived() {
            return packetsReceived;
        }

        public void setPacketsReceived(long packetsReceived) {

            this.packetsReceived = packetsReceived;
        }

        public long getPacketsSent() {
            return packetsSent;
        }

        public void setPacketsSent(long packetsSent) {
            this.packetsSent = packetsSent;
        }

        public String getSizeReceived() {
            return sizeReceived;
        }

        public void setSizeReceived(String sizeReceived) {
            this.sizeReceived = sizeReceived;
        }

        public String getSizeSent() {
            return sizeSent;
        }

        public void setSizeSent(String sizeSent) {
            this.sizeSent = sizeSent;
        }

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

    public static class processStructure {
        long timestamp;
        long noOfProcesses;
        long noOfThreads;
        public HashMap<String, List<Double>> processesList;

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public double getNoOfProcesses() {
            return noOfProcesses;
        }

        public void setNoOfProcesses(long noOfProcesses) {
            this.noOfProcesses = noOfProcesses;
        }

        public double getNoOfThreads() {
            return noOfThreads;
        }

        public void setNoOfThreads(long noOfThreads) {
            this.noOfThreads = noOfThreads;
        }

        public void setProcessesList(HashMap<String, List<Double>> processesList) {
            this.processesList = processesList;
        }

        HashMap<String, List<Double>> getProcessesList() { return processesList; }

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
