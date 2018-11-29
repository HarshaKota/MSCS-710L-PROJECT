package main;

import oshi.SystemInfo;
import oshi.hardware.*;
import oshi.software.os.FileSystem;
import oshi.software.os.NetworkParams;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;
import oshi.util.FormatUtil;

import java.util.ArrayList;
import java.util.Arrays;

public class staticMetricCollector {

    private final SystemInfo si = new SystemInfo();
    private final HardwareAbstractionLayer hal = si.getHardware();

    public ArrayList<String> getStaticSystemMetrics() {
        ArrayList<String> metricData = new ArrayList<>();
        ComputerSystem computerSystem = hal.getComputerSystem();

        metricData.add("manufacturer: " + computerSystem.getManufacturer());
        metricData.add("model: " + computerSystem.getModel());
        metricData.add("serialnumber: " + computerSystem.getSerialNumber());
        final Firmware firmware = computerSystem.getFirmware();
        metricData.add("firmware:");
        metricData.add("  manufacturer: " + firmware.getManufacturer());
        metricData.add("  name: " + firmware.getName());
        metricData.add("  description: " + firmware.getDescription());
        metricData.add("  version: " + firmware.getVersion());
        metricData.add("  release date: " + (firmware.getReleaseDate() == null ? "unknown"
                : firmware.getReleaseDate() == null ? "unknown" : firmware.getReleaseDate()));
        final Baseboard baseboard = computerSystem.getBaseboard();
        metricData.add("baseboard:");
        metricData.add("  manufacturer: " + baseboard.getManufacturer());
        metricData.add("  model: " + baseboard.getModel());
        metricData.add("  version: " + baseboard.getVersion());
        metricData.add("  serialnumber: " + baseboard.getSerialNumber());

        return metricData;
    }

    public ArrayList<String> getStaticProcessorMetrics() {
        ArrayList<String> metricData = new ArrayList<>();
        CentralProcessor processor = hal.getProcessor();

        metricData.add(processor.toString());
        metricData.add(" " + processor.getPhysicalPackageCount() + " physical CPU package(s)");
        metricData.add(" " + processor.getPhysicalProcessorCount() + " physical CPU core(s)");
        metricData.add(" " + processor.getLogicalProcessorCount() + " logical CPU(s)");

        metricData.add("Identifier: " + processor.getIdentifier());
        metricData.add("ProcessorID: " + processor.getProcessorID());

        return metricData;
    }

    public ArrayList<String> getStaticDiskMetrics() {
        ArrayList<String> metricData = new ArrayList<>();
        HWDiskStore[] diskStores = hal.getDiskStores();

        metricData.add("Disks:");
        for (HWDiskStore disk : diskStores) {
            boolean readwrite = disk.getReads() > 0 || disk.getWrites() > 0;
            metricData.add(String.format(" %s: (model: %s - S/N: %s) size: %s, reads: %s (%s),\n writes: %s (%s), transfer: %s ms%n",
                    disk.getName(), disk.getModel(), disk.getSerial(),
                    disk.getSize() > 0 ? FormatUtil.formatBytesDecimal(disk.getSize()) : "?",
                    readwrite ? disk.getReads() : "?", readwrite ? FormatUtil.formatBytes(disk.getReadBytes()) : "?",
                    readwrite ? disk.getWrites() : "?", readwrite ? FormatUtil.formatBytes(disk.getWriteBytes()) : "?",
                    readwrite ? disk.getTransferTime() : "?"));
            HWPartition[] partitions = disk.getPartitions();
            if (partitions == null) {
                continue;
            }
            for (HWPartition part : partitions) {
                metricData.add(String.format(" |-- %s: %s (%s) Maj:Min=%d:%d, size: %s%s%n", part.getIdentification(),
                        part.getName(), part.getType(), part.getMajor(), part.getMinor(),
                        FormatUtil.formatBytesDecimal(part.getSize()),
                        part.getMountPoint().isEmpty() ? "" : " @ " + part.getMountPoint()));
            }
        }

        return metricData;
    }

    public ArrayList<String> getStaticFileSystemMetrics() {
        ArrayList<String> metricData = new ArrayList<>();
        OperatingSystem os = si.getOperatingSystem();
        FileSystem fileSystem = os.getFileSystem();

        metricData.add("File System:");

        metricData.add(String.format(" File Descriptors: %d/%d%n", fileSystem.getOpenFileDescriptors(),
                fileSystem.getMaxFileDescriptors()));

        OSFileStore[] fsArray = fileSystem.getFileStores();
        for (OSFileStore fs : fsArray) {
            long usable = fs.getUsableSpace();
            long total = fs.getTotalSpace();
            metricData.add(String.format(
                    " %s (%s) [%s] %s of %s free (%.1f%%), %s of %s files free (%.1f%%) is %s "
                            + (fs.getLogicalVolume() != null && fs.getLogicalVolume().length() > 0 ? "[%s]" : "%s")
                            + " and is mounted at %s%n",
                    fs.getName(), fs.getDescription().isEmpty() ? "file system" : fs.getDescription(), fs.getType(),
                    FormatUtil.formatBytes(usable), FormatUtil.formatBytes(fs.getTotalSpace()), 100d * usable / total,
                    fs.getFreeInodes(), fs.getTotalInodes(), 100d * fs.getFreeInodes() / fs.getTotalInodes(),
                    fs.getVolume(), fs.getLogicalVolume(), fs.getMount()));
        }

        return metricData;
    }

    public ArrayList<String> getStaticNetworkMetrics() {
        ArrayList<String> metricData = new ArrayList<>();
        OperatingSystem os = si.getOperatingSystem();
        NetworkParams networkParams = os.getNetworkParams();

        metricData.add("Network parameters:");
        metricData.add(String.format(" Host name: %s%n", networkParams.getHostName()));
        metricData.add(String.format(" Domain name: %s%n", networkParams.getDomainName()));
        metricData.add(String.format(" DNS servers: %s%n", Arrays.toString(networkParams.getDnsServers())));
        metricData.add(String.format(" IPv4 Gateway: %s%n", networkParams.getIpv4DefaultGateway()));
        metricData.add(String.format(" IPv6 Gateway: %s%n", networkParams.getIpv6DefaultGateway()));

        return metricData;
    }
}
