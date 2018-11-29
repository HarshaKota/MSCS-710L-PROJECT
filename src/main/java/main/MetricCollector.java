package main;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import oshi.hardware.*;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;
import oshi.util.FormatUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * MetricCollector class provides methods to collect metric data for each of the metric
 * using the OSHI library methods
 */
public class MetricCollector {

    private static final Logger log = LogManager.getLogger(UI.class);
    private static int noOfCallsToGetPower = 0;
    private static int noOfCallsToGetCPU = 0;
    private static int noOfCallsToGetSensors = 0;
    private static int noOfCallsToGetMemory = 0;
    private static int noOfCallsToGetNetwork = 0;
    private static int noOfCallsToGetProcesses = 0;


    /**
     * Collects power metrics
     *
     * @param metricCollectedTime Time indicating when the metric was collected
     * @param hal OSHI Library HardwareAbstractionLayer object that provides methods to access hardware information
     * @return powerStructure
     */
    public MetricCollectionStructures.powerStructure getPower(final long metricCollectedTime, final HardwareAbstractionLayer hal) {

        if (hasPowerTable()) {
            noOfCallsToGetPower++;

            MetricCollectionStructures.powerStructure pS = new MetricCollectionStructures.powerStructure();

            pS.setTimestamp(metricCollectedTime);

            double timeRemaining =  getTimeRemaining(hal.getPowerSources());

            // -1d indicates charging
            if (timeRemaining < -1d) {
                pS.setPowerStatus(1);
            // 0d indicates discharging
            } else if (timeRemaining > 0d) {
                pS.setPowerStatus(0);
            }

            for (PowerSource pSource : hal.getPowerSources()) {
                pS.setBatteryPercentage(Math.round((pSource.getRemainingCapacity() * 100d) * 10.0) / 10.0);
            }

            return pS;
        }

        return null;
    }

    /**
     * Collects CPU metrics
     *
     * @param metricCollectedTime Time indicating when the metric was collected
     * @param hal OSHI Library HardwareAbstractionLayer object that provides methods to access hardware information
     * @return cpuStructure
     */
    public MetricCollectionStructures.cpuStructure getCPU(final long metricCollectedTime, final HardwareAbstractionLayer hal) {
        noOfCallsToGetCPU++;

        long[] prevTicks = hal.getProcessor().getSystemCpuLoadTicks();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            log.error("Failed when calculating ticks");
        }
        long[] ticks = hal.getProcessor().getSystemCpuLoadTicks();
        long user = ticks[CentralProcessor.TickType.USER.getIndex()] - prevTicks[CentralProcessor.TickType.USER.getIndex()];
        long nice = ticks[CentralProcessor.TickType.NICE.getIndex()] - prevTicks[CentralProcessor.TickType.NICE.getIndex()];
        long sys = ticks[CentralProcessor.TickType.SYSTEM.getIndex()] - prevTicks[CentralProcessor.TickType.SYSTEM.getIndex()];
        long idle = ticks[CentralProcessor.TickType.IDLE.getIndex()] - prevTicks[CentralProcessor.TickType.IDLE.getIndex()];
        long ioWait = ticks[CentralProcessor.TickType.IOWAIT.getIndex()] - prevTicks[CentralProcessor.TickType.IOWAIT.getIndex()];
        long irq = ticks[CentralProcessor.TickType.IRQ.getIndex()] - prevTicks[CentralProcessor.TickType.IRQ.getIndex()];
        long softirq = ticks[CentralProcessor.TickType.SOFTIRQ.getIndex()] - prevTicks[CentralProcessor.TickType.SOFTIRQ.getIndex()];
        long steal = ticks[CentralProcessor.TickType.STEAL.getIndex()] - prevTicks[CentralProcessor.TickType.STEAL.getIndex()];
        long totalCpu = user + nice + sys + idle + ioWait + irq + softirq + steal;

        MetricCollectionStructures.cpuStructure cS = new MetricCollectionStructures.cpuStructure();

        cS.setTimestamp(metricCollectedTime);
        cS.setUptime(hal.getProcessor().getSystemUptime());
        cS.setUserLoad(Math.round((100d * user / totalCpu)*10.0)/10.0);
        cS.setSystemLoad(Math.round((100d * sys / totalCpu)*10.0)/10.0);
        cS.setIdleLoad(Math.round((100d * idle / totalCpu)*10.0)/10.0);
        double[] load = hal.getProcessor().getProcessorCpuLoadBetweenTicks();
        ArrayList<Double> individualProcessorLoad = new ArrayList<>();
        for (double eachLoad : load) {
            individualProcessorLoad.add(Math.round((eachLoad * 100)*10.0)/10.0);
        }
        cS.setProcessorLoad(individualProcessorLoad);

        return cS;
    }

    /**
     * Collects Sensor metrics
     *
     * @param metricCollectedTime Time indicating when the metric was collected
     * @param hal OSHI Library HardwareAbstractionLayer object that provides methods to access hardware information
     * @return sensorsStructure
     */
    public MetricCollectionStructures.sensorsStructure getSensors(final long metricCollectedTime, final HardwareAbstractionLayer hal) {
        noOfCallsToGetSensors++;

        MetricCollectionStructures.sensorsStructure sS = new MetricCollectionStructures.sensorsStructure();

        sS.setTimestamp(metricCollectedTime);
        sS.setCpuTemperature(hal.getSensors().getCpuTemperature());
        if (getCpuVoltage(hal) != 999.0) {
            sS.setCpuVoltage(hal.getSensors().getCpuVoltage());
        }

        ArrayList<Integer> fans = new ArrayList<>();
        int fansLength = getFans(hal);
        if (fansLength > 0) {
            for (int fan: hal.getSensors().getFanSpeeds()) {
                fans.add(fan);
            }
        }
        sS.setFans(fans);

        return sS;
    }

    /**
     * Collects Memory metrics
     *
     * @param metricCollectedTime Time indicating when the metric was collected
     * @param hal OSHI Library HardwareAbstractionLayer object that provides methods to access hardware information
     * @return memoryStructure
     */
    public MetricCollectionStructures.memoryStructure getMemory(final long metricCollectedTime, final HardwareAbstractionLayer hal) {
        noOfCallsToGetMemory++;

        MetricCollectionStructures.memoryStructure mS = new MetricCollectionStructures.memoryStructure();

        final long GIBI = 1L << 30;
        mS.setTimestamp(metricCollectedTime);
        double availableMemory = Math.round((((double)hal.getMemory().getAvailable()/GIBI)*10.0))/10.0;
        double totalMemory = Math.round((((double)hal.getMemory().getTotal()/GIBI)*10.0))/10.0;
        double usedMemory = totalMemory - availableMemory;
        mS.setUsedMemory(usedMemory);
        mS.setTotalMemory(totalMemory);

        return mS;
    }

    /**
     * Collects Network metrics
     *
     * @param metricCollectedTime Time indicating when the metric was collected
     * @param hal OSHI Library HardwareAbstractionLayer object that provides methods to access hardware information
     * @return networkStructure
     */
    public MetricCollectionStructures.networkStructure getNetwork(final long metricCollectedTime, final HardwareAbstractionLayer hal) {
        noOfCallsToGetNetwork++;

        MetricCollectionStructures.networkStructure nS = new MetricCollectionStructures.networkStructure();

        long packetsReceived = 0;
        long packetsSent = 0;
        String sizeReceived = "";
        String sizeSent = "";
        for (NetworkIF net: hal.getNetworkIFs()) {
            boolean hasData = net.getBytesRecv() > 0 || net.getBytesSent() > 0 || net.getPacketsRecv() > 0
                    || net.getPacketsSent() > 0;
            if(hasData) {
                if (packetsReceived < net.getPacketsRecv()) {
                    packetsReceived = net.getPacketsRecv();
                    sizeReceived = FormatUtil.formatBytes(net.getBytesRecv());
                }
                if (packetsSent < net.getPacketsSent()) {
                    packetsSent = net.getPacketsSent();
                    sizeSent = FormatUtil.formatBytes(net.getBytesSent());
                }
            }
        }
        nS.setTimestamp(metricCollectedTime);
        nS.setPacketsReceived(packetsReceived);
        nS.setPacketsSent(packetsSent);
        nS.setSizeReceived(sizeReceived);
        nS.setSizeSent(sizeSent);

        return nS;
    }

    /**
     * Collects Process metrics
     *
     * @param metricCollectedTime Time indicating when the metric was collected
     * @param hal OSHI Library HardwareAbstractionLayer object that provides methods to access hardware information
     * @param os OSHI Library OperatingSystem object that provides methods to access Operating System information
     * @return processStructure
     */
    public MetricCollectionStructures.processStructure getProcess(final long metricCollectedTime, final HardwareAbstractionLayer hal, final OperatingSystem os) {
        noOfCallsToGetProcesses++;

        MetricCollectionStructures.processStructure pS = new MetricCollectionStructures.processStructure();

        List<OSProcess> allProcesses = Arrays.asList(os.getProcesses(10, OperatingSystem.ProcessSort.MEMORY));

        pS.setTimestamp(metricCollectedTime);
        pS.setNoOfProcesses(os.getProcessCount());
        pS.setNoOfThreads(os.getThreadCount());

        HashMap<String, List<Double>> processesMap = new HashMap<>();

        for (int i = 0; i < allProcesses.size() && i < 10; i++) {
            OSProcess p = allProcesses.get(i);

            List<Double> isPresent = processesMap.get(p.getName());

            if (isPresent == null) {
                List<Double> cpuAndMemValues = new ArrayList<>();
                cpuAndMemValues.add(Math.round((100d * (p.getKernelTime() + p.getUserTime()) / p.getUpTime())*10.0)/10.0);
                cpuAndMemValues.add(Math.round((100d * p.getResidentSetSize() / hal.getMemory().getTotal())*10.0)/10.0);
                processesMap.put(p.getName(), cpuAndMemValues);
            } else {
                // In the list
                double previousVal1 = isPresent.get(0);
                double previousVal2 = isPresent.get(1);

                // Present rounded values
                double presentVal1 = Math.round((100d * (p.getKernelTime() + p.getUserTime()) / p.getUpTime())*10.0)/10.0;
                double presentVal2 = Math.round((100d * p.getResidentSetSize() / hal.getMemory().getTotal())*10.0)/10.0;

                // Getting the computed rounded values
                double newCpuValue = Math.round(((previousVal1+presentVal1)/2)*10.0)/10.0;
                double newMemValue = Math.round(((previousVal2+presentVal2)/2)*10.0)/10.0;

                isPresent.set(0, newCpuValue);
                isPresent.set(1, newMemValue);
            }

        }

        pS.setProcessesList(processesMap);

        return pS;

    }

    /**
     * Returns time remaining. Used by getPower method.
     *
     * @param powerSources OSHI Library PowerSources object that provides methods to access power metrics
     * @return A double representing how much time the battery has until it is drained.
     */
    public double getTimeRemaining(PowerSource[] powerSources) {
        return powerSources[0].getTimeRemaining();
    }


    /**
     * Check if the PowerTable has been created
     *
     * @return A Boolean representing if the Power table has been created or not.
     */
    public Boolean hasPowerTable() {
        return Main.hasPowerSource;
    }


    /**
     * Returns Cpu voltage
     *
     * @param hal OSHI Library HardwareAbstractionLayer object that provides methods to access hardware information
     * @return A double representing the voltage running through the CPU
     */
    public double getCpuVoltage(HardwareAbstractionLayer hal) {
        return TableCreationChecks.getCpuVoltage(hal.getSensors());
    }


    /**
     * Check if the PowerTable has been created
     *
     * @param hal OSHI Library HardwareAbstractionLayer object that provides methods to access hardware information
     * @return A Boolean representing if the Power table has been created or not.
     */
    public int getFans(HardwareAbstractionLayer hal) {
        return TableCreationChecks.getFans(hal.getSensors());
    }


    /**
     * Used to fetch a start session time
     *
     * @return The current time as a long value
     */
    public long startSession() {
        return System.currentTimeMillis();
    }


    /**
     * Used to fetch an end session time
     *
     * @return The current time as a long value
     */
    public long endSession() {
        return System.currentTimeMillis();
    }
}