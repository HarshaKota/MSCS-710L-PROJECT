import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

class MetricCollectionStructures {

    public static class powerStructure {

        long timestamp;
        int powerStatus;
        double batteryPercentage;

        // Sets
        void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        void setPowerStatus(int powerStatus) {
            this.powerStatus = powerStatus;
        }

        void setBatteryPercentage(double batteryPercentage) {
            this.batteryPercentage = batteryPercentage;
        }

        // Gets
        int getPowerStatus() {
            return powerStatus;
        }

        double getBatteryPercentage() {
            return batteryPercentage;
        }

        long getTimestamp() {
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
        double idelLoad;
        ArrayList<Double> processorLoad = new ArrayList<>();

        long getTimestamp() {
            return timestamp;
        }

        void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        long getUptime() {
            return uptime;
        }

        void setUptime(long uptime) {
            this.uptime = uptime;
        }

        double getUserLoad() {
            return userLoad;
        }

        void setUserLoad(double userLoad) {
            this.userLoad = userLoad;
        }

        double getSystemLoad() {
            return systemLoad;
        }

        void setSystemLoad(double systemLoad) {
            this.systemLoad = systemLoad;
        }

        double getIdelLoad() {
            return idelLoad;
        }

        void setIdelLoad(double idelLoad) {
            this.idelLoad = idelLoad;
        }

        void setProcessorLoad(ArrayList<Double> processorLoad) {
            this.processorLoad = processorLoad;
        }

        @Override
        public String toString() {
            return "cpuStructure{" +
                    "timestamp=" + timestamp +
                    ", uptime=" + uptime +
                    ", userLoad=" + userLoad +
                    ", systemLoad=" + systemLoad +
                    ", idelLoad=" + idelLoad +
                    ", processorLoad=" + processorLoad +
                    '}';
        }
    }

    public static class sensorsStructure {
        long timestamp;
        double cpuTemperature;
        double cpuVoltage = 999d;
        int[] fans = {};

        long getTimestamp() {
            return timestamp;
        }

        void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        double getCpuTemperature() {
            return cpuTemperature;
        }

        void setCpuTemperature(double cpuTemperature) {
            this.cpuTemperature = cpuTemperature;
        }

        double getCpuVoltage() {
            return cpuVoltage;
        }

        void setCpuVoltage(double cpuVoltage) {
            this.cpuVoltage = cpuVoltage;
        }

        void setFans(int[] fans) {
            this.fans = fans;
        }

        int[] getFans() {
            return fans;
        }

        @Override
        public String toString() {
            return "sensorsStructure{" +
                    "timestamp=" + timestamp +
                    ", cpuTemperature=" + cpuTemperature +
                    ", cpuVoltage=" + cpuVoltage +
                    ", fans=" + Arrays.toString(fans) +
                    '}';
        }
    }

    public static class memoryStructure {
        long timestamp;
        double usedMemory;
        double totalMemory;

        long getTimestamp() {
            return timestamp;
        }

        void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        double getUsedMemory() {
            return usedMemory;
        }

        void setUsedMemory(double usedMemory) {
            this.usedMemory = usedMemory;
        }

        double getTotalMemory() {
            return totalMemory;
        }

        void setTotalMemory(double totalMemory) {
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

        long getTimestamp() {
            return timestamp;
        }

        void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        long getPacketsReceived() {
            return packetsReceived;
        }

        void setPacketsReceived(long packetsReceived) {

            this.packetsReceived = packetsReceived;
        }

        long getPacketsSent() {
            return packetsSent;
        }

        void setPacketsSent(long packetsSent) {
            this.packetsSent = packetsSent;
        }

        String getSizeReceived() {
            return sizeReceived;
        }

        void setSizeReceived(String sizeReceived) {
            this.sizeReceived = sizeReceived;
        }

        String getSizeSent() {
            return sizeSent;
        }

        void setSizeSent(String sizeSent) {
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

    static class processStructure {
        long timestamp;
        long noOfProcesses;
        long noOfThreads;
        HashMap<String, List<Double>> processesList;

        long getTimestamp() {
            return timestamp;
        }

        void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        double getNoOfProcesses() {
            return noOfProcesses;
        }

        void setNoOfProcesses(long noOfProcesses) {
            this.noOfProcesses = noOfProcesses;
        }

        double getNoOfThreads() {
            return noOfThreads;
        }

        void setNoOfThreads(long noOfThreads) {
            this.noOfThreads = noOfThreads;
        }

        void setProcessesList(HashMap<String, List<Double>> processesList) {
            this.processesList = processesList;
        }
    }
}
