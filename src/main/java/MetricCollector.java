import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import oshi.SystemInfo;
import oshi.hardware.*;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;
import oshi.util.FormatUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MetricCollector {

    private static final Logger log = LogManager.getLogger(UI.class);
    private static int noOfCallsTogetPower = 0;
    private static int noOfCallsTogetCPU = 0;
    private static int noOfCallsTogetSensors = 0;
    private static int noOfCallsTogetMemory = 0;
    private static int noOfCallsTogetNetwork = 0;
    private static int noOfCallsTogetProcesses = 0;



    // Collects Power Info
    //      1 - charging
    //      0 - discharging
    // Returns powerStructure
    MetricCollectionStructures.powerStructure getPower(final long metricCollectedTime) {

        if (hasPowerTable()) {
            noOfCallsTogetPower++;

            // OSHI library objects
            SystemInfo si = new SystemInfo();
            HardwareAbstractionLayer hal = si.getHardware();

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

            System.out.println(String.format("%1$20s  %2$d", "getPower calls:", noOfCallsTogetPower)); //Sysout

            return pS;
        }

        return null;
    }

    // Collects CPU Info
    //
    //
    // Returns cpuStructure
    MetricCollectionStructures.cpuStructure getCPU(final long metricCollectedTime) {
        noOfCallsTogetCPU++;

        // OSHI library objects
        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();

        long[] prevTicks = hal.getProcessor().getSystemCpuLoadTicks();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            log.error("Failed when calculating ticks");
//            try {
//                throw new InterruptedException();
//            } catch (InterruptedException e1) {
//                e1.printStackTrace();
//            }
        }
        long[] ticks = hal.getProcessor().getSystemCpuLoadTicks();
        long user = ticks[CentralProcessor.TickType.USER.getIndex()] - prevTicks[CentralProcessor.TickType.USER.getIndex()];
        long nice = ticks[CentralProcessor.TickType.NICE.getIndex()] - prevTicks[CentralProcessor.TickType.NICE.getIndex()];
        long sys = ticks[CentralProcessor.TickType.SYSTEM.getIndex()] - prevTicks[CentralProcessor.TickType.SYSTEM.getIndex()];
        long idle = ticks[CentralProcessor.TickType.IDLE.getIndex()] - prevTicks[CentralProcessor.TickType.IDLE.getIndex()];
        long iowait = ticks[CentralProcessor.TickType.IOWAIT.getIndex()] - prevTicks[CentralProcessor.TickType.IOWAIT.getIndex()];
        long irq = ticks[CentralProcessor.TickType.IRQ.getIndex()] - prevTicks[CentralProcessor.TickType.IRQ.getIndex()];
        long softirq = ticks[CentralProcessor.TickType.SOFTIRQ.getIndex()] - prevTicks[CentralProcessor.TickType.SOFTIRQ.getIndex()];
        long steal = ticks[CentralProcessor.TickType.STEAL.getIndex()] - prevTicks[CentralProcessor.TickType.STEAL.getIndex()];
        long totalCpu = user + nice + sys + idle + iowait + irq + softirq + steal;

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

        System.out.println(String.format("%1$20s  %2$d", "getCPU calls:", noOfCallsTogetCPU)); //Sysout

        return cS;
    }

    // Collects Sensors Info
    //
    //
    // Returns sensorsStructure
    MetricCollectionStructures.sensorsStructure getSensors(final long metricCollectedTime) {
        noOfCallsTogetSensors++;

        // OSHI library objects
        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();

        MetricCollectionStructures.sensorsStructure sS= new MetricCollectionStructures.sensorsStructure();

        sS.setTimestamp(metricCollectedTime);
        sS.setCpuTemperature(hal.getSensors().getCpuTemperature());
        if (getCpuVoltage(hal) != 999.0) {
            sS.setCpuVoltage(hal.getSensors().getCpuVoltage());
        }
        if (getFans(hal) > 0) {
            sS.setFans(hal.getSensors().getFanSpeeds());
        }

        System.out.println(String.format("%1$20s  %2$d", "getSensors calls:", noOfCallsTogetSensors)); //Sysout

        return sS;
    }

    // Collects Memory Info
    //
    //
    // Returns memoryStructure
    MetricCollectionStructures.memoryStructure getMemory(final long metricCollectedTime) {
        noOfCallsTogetMemory++;

        // OSHI library objects
        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();

        MetricCollectionStructures.memoryStructure mS = new MetricCollectionStructures.memoryStructure();

        final long GIBI = 1L << 30;
        mS.setTimestamp(metricCollectedTime);
        double availableMemory = Math.round((((double)hal.getMemory().getAvailable()/GIBI)*10.0))/10.0;
        double totalMemory = Math.round((((double)hal.getMemory().getTotal()/GIBI)*10.0))/10.0;
        double usedMemory = totalMemory - availableMemory;
        mS.setUsedMemory(usedMemory);
        mS.setTotalMemory(totalMemory);

        System.out.println(String.format("%1$20s  %2$d", "getMemory calls:", noOfCallsTogetMemory)); //Sysout

        return mS;
    }

    // Collects Network Info
    //
    //
    // Returns networkStructure
    MetricCollectionStructures.networkStructure getNetwork(final long metricCollectedTime) {
        noOfCallsTogetNetwork++;

        // OSHI library objects
        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();

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

        System.out.println(String.format("%1$20s  %2$d", "getNetwork calls:", noOfCallsTogetNetwork)); //Sysout

        return nS;
    }

    // Collect Process Info
    //
    //
    // Returns processStructure
    MetricCollectionStructures.processStructure getProcess(final long metricCollectedTime) {
        noOfCallsTogetProcesses++;

        // OSHI library objects
        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();
        OperatingSystem os = si.getOperatingSystem();

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

        System.out.println(String.format("%1$20s  %2$d", "getProcess calls:", noOfCallsTogetProcesses)); //Sysout

        return pS;

    }

    // Return time remaining. Used by getPower method.
    //
    //
    // Returns a double representing how much time the battery has until it is drained.
    double getTimeRemaining(PowerSource[] powerSources) {
        return powerSources[0].getTimeRemaining();
    }


    // Return true if the PowerTable has been created
    //
    //
    // Returns a Boolean representing if the Power table has been created or not.
    Boolean hasPowerTable() {
        return Main.hasPowerSource;
    }


    // Return Cpu voltage
    //
    //
    // Returns a double representing the voltage running through the CPU
    double getCpuVoltage(HardwareAbstractionLayer hal) {
        return TableCreationChecks.getCpuVoltage(hal.getSensors());
    }


    // Return true if the PowerTable has been created
    //
    //
    // Returns a Boolean representing if the Power table has been created or not.
    double getFans(HardwareAbstractionLayer hal) {
        return TableCreationChecks.getFans(hal.getSensors());
    }


    // Used to fetch a start session time
    //
    //
    // Returns the current time as a long value
    long startSession() {
        return System.currentTimeMillis();
    }


    // Used to fetch a end session time
    //
    //
    // Returns the current time as a long value
    long endSession() {
        return System.currentTimeMillis();
    }
}