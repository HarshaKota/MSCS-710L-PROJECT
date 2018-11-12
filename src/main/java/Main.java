import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main {

    private static final Logger log = LogManager.getLogger(UI.class);

    static AtomicBoolean applicationOpen = new AtomicBoolean(true);

    private final static SystemInfo si = new SystemInfo();
    private final static HardwareAbstractionLayer hal = si.getHardware();
    private final static OperatingSystem os = si.getOperatingSystem();
    static  String databaseUrl = "jdbc:sqlite:MetricCollector.db";

    static boolean hasPowerSource = false;

    public static void main(String[] args) throws Exception {

        // Create the database
        final Database dbObject = new Database(databaseUrl);

        // Check if the session table is intact
        dbObject.checkSessionTable();

        // Start the UI
        UI ui = new UI();
        Thread uiThread = new Thread(ui);
        uiThread.start();

        // Record the session start time in the session table
        final long startSessionTime = MetricCollector.startSession();
        dbObject.insertStartSessionIntoSessionTable(startSessionTime);

        while (Main.applicationOpen.get()) {

            final long metricCollectedTime = MetricCollector.startSession();

            Callable<MetricCollectionStructures.powerStructure> power = new Callable<MetricCollectionStructures.powerStructure>() {
                @Override
                public MetricCollectionStructures.powerStructure call() {
                    return MetricCollector.getPower(metricCollectedTime, hal.getPowerSources());
                }
            };

            Callable<MetricCollectionStructures.cpuStructure> cpu = new Callable<MetricCollectionStructures.cpuStructure>() {
                @Override
                public MetricCollectionStructures.cpuStructure call() {
                    return MetricCollector.getCPU(metricCollectedTime, hal.getProcessor());
                }
            };

            Callable<MetricCollectionStructures.sensorsStructure> sensors = new Callable<MetricCollectionStructures.sensorsStructure>() {
                @Override
                public MetricCollectionStructures.sensorsStructure call() {
                    return MetricCollector.getSensors(metricCollectedTime, hal, hal.getSensors());
                }
            };

            Callable<MetricCollectionStructures.memoryStructure> memory = new Callable<MetricCollectionStructures.memoryStructure>() {
                @Override
                public MetricCollectionStructures.memoryStructure call() {
                    return MetricCollector.getMemory(metricCollectedTime, hal.getMemory());
                }
            };

            Callable<MetricCollectionStructures.networkStructure> network = new Callable<MetricCollectionStructures.networkStructure>() {
                @Override
                public MetricCollectionStructures.networkStructure call() {
                    return MetricCollector.getNetwork(metricCollectedTime, hal.getNetworkIFs());
                }
            };

            Callable<MetricCollectionStructures.processStructure> processes = new Callable<MetricCollectionStructures.processStructure>() {
                @Override
                public MetricCollectionStructures.processStructure call() {
                    return MetricCollector.getProcess(metricCollectedTime, os, hal.getMemory());
                }
            };

            ExecutorService service = Executors.newFixedThreadPool(6);

            Future<MetricCollectionStructures.powerStructure> powerFuture = service.submit(power);
            Future<MetricCollectionStructures.cpuStructure> cpuFuture = service.submit(cpu);
            Future<MetricCollectionStructures.sensorsStructure> sensorsFuture = service.submit(sensors);
            Future<MetricCollectionStructures.memoryStructure> memoryFuture = service.submit(memory);
            Future<MetricCollectionStructures.networkStructure> networkFuture = service.submit(network);
            Future<MetricCollectionStructures.processStructure> processesFuture = service.submit(processes);

            MetricCollectionStructures.powerStructure powerStructure;
            MetricCollectionStructures.cpuStructure cpuStructure;
            MetricCollectionStructures.sensorsStructure sensorsStructure;
            MetricCollectionStructures.memoryStructure memoryStructure;
            MetricCollectionStructures.networkStructure networkStructure;
            MetricCollectionStructures.processStructure processesStructure;

            try {
                powerStructure = powerFuture.get();
                cpuStructure = cpuFuture.get();
                sensorsStructure = sensorsFuture.get();
                memoryStructure = memoryFuture.get();
                networkStructure = networkFuture.get();
                processesStructure = processesFuture.get();

                dbObject.insertIntoPowerTable(powerStructure);
                dbObject.insertIntoCpuTable(cpuStructure);
                dbObject.insertIntoSensorsTable(sensorsStructure);
                dbObject.insertIntoMemoryTable(memoryStructure);
                dbObject.insertIntoNetworkTable(networkStructure);
                dbObject.insertIntoProcessTable(processesStructure);

                dbObject.commit();

                service.shutdown();

            } catch (InterruptedException e) {
                log.error("Future Interrupted " + e.getClass().getName() + ": " + e.getMessage());
            } catch (ExecutionException e) {
                log.error("Future Execution Failed" + e.getClass().getName() + ": " + e.getMessage());
            }

            System.out.println("----->Started to count down 5 seconds"); //Sysout
            Thread.sleep(5000);
            System.out.println("<-----Finished countdown of 5 seconds"); //Sysout
        }

        // Record the session end time in the session table
        final long endSessionTime = MetricCollector.endSession();
        dbObject.insertEndSessionIntoSessionTable(startSessionTime, endSessionTime);

        dbObject.closeDatabaseConnection();

        System.out.println("End of Main"); //Sysout
    }
}