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

    public static void main(String[] args) {

        // Create the database
        final Database dbObject = new Database();

        // Start the UI
        UI ui = new UI();
        Thread uiThread = new Thread(ui);
        uiThread.start();

        while (Main.applicationOpen.get()) {
            try {
                final SystemInfo si = new SystemInfo();
                final HardwareAbstractionLayer hal = si.getHardware();
                final OperatingSystem os = si.getOperatingSystem();

                Callable<MetricCollectionStructures.powerStructure> power = new Callable<MetricCollectionStructures.powerStructure>() {
                    @Override
                    public MetricCollectionStructures.powerStructure call() {
                        return MetricCollector.getPower(hal.getPowerSources());
                    }
                };

                Callable<MetricCollectionStructures.cpuStructure> cpu = new Callable<MetricCollectionStructures.cpuStructure>() {
                    @Override
                    public MetricCollectionStructures.cpuStructure call() {
                        return MetricCollector.getCPU(hal.getProcessor());
                    }
                };

                Callable<MetricCollectionStructures.sensorsStructure> sensors = new Callable<MetricCollectionStructures.sensorsStructure>() {
                    @Override
                    public MetricCollectionStructures.sensorsStructure call() {
                        return MetricCollector.getSensors(hal.getSensors());
                    }
                };

                Callable<MetricCollectionStructures.memoryStructure> memory = new Callable<MetricCollectionStructures.memoryStructure>() {
                    @Override
                    public MetricCollectionStructures.memoryStructure call() {
                        return MetricCollector.getMemory(hal.getMemory());
                    }
                };

                Callable<MetricCollectionStructures.networkStructure> network = new Callable<MetricCollectionStructures.networkStructure>() {
                    @Override
                    public MetricCollectionStructures.networkStructure call() {
                        return MetricCollector.getNetwork(hal.getNetworkIFs());
                    }
                };

                Callable<MetricCollectionStructures.processesStructure> processes = new Callable<MetricCollectionStructures.processesStructure>() {
                    @Override
                    public MetricCollectionStructures.processesStructure call() {
                        return MetricCollector.getProcesses(os, hal.getMemory());
                    }
                };

                ExecutorService service = Executors.newFixedThreadPool(6);

                Future<MetricCollectionStructures.powerStructure> powerFuture = service.submit(power);
                Future<MetricCollectionStructures.cpuStructure> cpuFuture = service.submit(cpu);
                Future<MetricCollectionStructures.sensorsStructure> sensorsFuture = service.submit(sensors);
                Future<MetricCollectionStructures.memoryStructure> memoryFuture = service.submit(memory);
                Future<MetricCollectionStructures.networkStructure> networkFuture = service.submit(network);
                Future<MetricCollectionStructures.processesStructure> processesFuture = service.submit(processes);

                MetricCollectionStructures.powerStructure powerStructure;
                MetricCollectionStructures.cpuStructure cpuStructure;
                MetricCollectionStructures.sensorsStructure sensorsStructure;
                MetricCollectionStructures.memoryStructure memoryStructure;
                MetricCollectionStructures.networkStructure networkStructure;
                MetricCollectionStructures.processesStructure processesStructure;

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

                    service.shutdown();

                } catch (InterruptedException e) {
                    log.error("Future Interrupted " + e.getClass().getName() + ": " + e.getMessage());
                } catch (ExecutionException e) {
                    log.error("Future Execution Failed" + e.getClass().getName() + ": " + e.getMessage());
                }

                System.out.println("----->Started to count down 5 seconds"); //Sysout
                Thread.sleep(5000);
                System.out.println("<-----Finished countdown of 5 seconds"); //Sysout
            } catch(Exception e){
                e.printStackTrace();
                log.error("Failed to Setup the latch and concurrent method calls");
            }

        }

        dbObject.closeDatabaseConnection();
        System.out.println("End of Main"); //Sysout
    }
}