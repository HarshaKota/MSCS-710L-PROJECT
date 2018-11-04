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
        float userLoad;
        float systemLoad;
        float idelLoad;
        ArrayList<Float> processorLoad = new ArrayList<>();

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

        public float getUserLoad() {
            return userLoad;
        }

        public void setUserLoad(float userLoad) {
            this.userLoad = userLoad;
        }

        public float getSystemLoad() {
            return systemLoad;
        }

        public void setSystemLoad(float systemLoad) {
            this.systemLoad = systemLoad;
        }

        public float getIdelLoad() {
            return idelLoad;
        }

        public void setIdelLoad(float idelLoad) {
            this.idelLoad = idelLoad;
        }

        public void setProcessorLoad(Float processorLoad) {
            this.processorLoad.add(processorLoad);
        }

    }
}
