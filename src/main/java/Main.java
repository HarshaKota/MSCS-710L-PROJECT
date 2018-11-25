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

    static  String databaseUrl = "jdbc:sqlite:MetricCollector.db";

    static boolean hasPowerSource = false;
    static int collectionInterval = 5000;

    private static SystemInfo si;
    private static HardwareAbstractionLayer hal;
    private static OperatingSystem os;

    public static void main(String[] args) throws Exception {

        //Initialize OSHI Objects
        si = new SystemInfo();
        hal = si.getHardware();
        os = si.getOperatingSystem();

        // Create the database
        final Database dbObject = new Database();
        dbObject.establishDatabaseConnection(databaseUrl);
        dbObject.createTables();

        // Check if the session table is intact
        dbObject.checkSessionTable();

        // Start the UI
        UI ui = new UI();
        Thread uiThread = new Thread(ui);
        uiThread.start();

        // Get an instance of the metric collector
        final MetricCollector metricCollector = new MetricCollector();

        // Record the session start time in the session table
        final long startSessionTime = metricCollector.startSession();
        dbObject.insertStartSessionIntoSessionTable(startSessionTime);

        while (Main.applicationOpen.get()) {

            final long metricCollectedTime = metricCollector.startSession();

            Callable<MetricCollectionStructures.powerStructure> power = () -> metricCollector.getPower(metricCollectedTime, hal);

            Callable<MetricCollectionStructures.cpuStructure> cpu = () -> metricCollector.getCPU(metricCollectedTime, hal);

            Callable<MetricCollectionStructures.sensorsStructure> sensors = () -> metricCollector.getSensors(metricCollectedTime, hal);

            Callable<MetricCollectionStructures.memoryStructure> memory = () -> metricCollector.getMemory(metricCollectedTime, hal);

            Callable<MetricCollectionStructures.networkStructure> network = () -> metricCollector.getNetwork(metricCollectedTime, hal);

            Callable<MetricCollectionStructures.processStructure> processes = () -> metricCollector.getProcess(metricCollectedTime, hal, os);

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
            Thread.sleep(collectionInterval);
            System.out.println("<-----Finished countdown of 5 seconds"); //Sysout
        }

        // Record the session end time in the session table
        final long endSessionTime = metricCollector.endSession();
        dbObject.insertEndSessionIntoSessionTable(startSessionTime, endSessionTime);

        dbObject.closeDatabaseConnection();

        System.out.println("End of Main"); //Sysout
    }
}