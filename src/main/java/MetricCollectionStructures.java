import java.util.ArrayList;

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
    }

    public static class cpuStructure {
        long timestamp;
        long uptime;
        double userLoad;
        double systemLoad;
        double idelLoad;
        ArrayList<Double> processorLoad = new ArrayList<>();

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

        public double getIdelLoad() {
            return idelLoad;
        }

        public void setIdelLoad(double idelLoad) {
            this.idelLoad = idelLoad;
        }

        public void setProcessorLoad(double processorLoad) {
            this.processorLoad.add(processorLoad);
        }

    }

    public static class sensorsStructure {
        long timestamp;
        double cpuTemperature;
        double cpuVoltage = 999.0;
        int[] fans = {};

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

        public void setFans(int[] fans) {
            this.fans = fans;
        }

        public int[] getFans() {
            return fans;
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
    }

}
