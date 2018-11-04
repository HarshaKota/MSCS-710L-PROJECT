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
}
