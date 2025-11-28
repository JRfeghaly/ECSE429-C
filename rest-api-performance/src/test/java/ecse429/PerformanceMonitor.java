package ecse429;

import java.io.*;
import java.util.*;

public class PerformanceMonitor {

    private static class Row {
        int scale;
        long timestamp;

        double addMs, updateMs, deleteMs;
        double addCPU, updateCPU, deleteCPU;
        double addMem, updateMem, deleteMem;
    }

    private final List<Row> rows = new ArrayList<>();

    // ---------- OS METRICS ----------
    private double readCPU() {
        try {
            Process p = Runtime.getRuntime().exec("ps -A -o %cpu");
            try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                br.readLine();
                double total = 0;
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty()) total += Double.parseDouble(line);
                }
                return total;
            }
        } catch (Exception e) {
            return -1.0;
        }
    }

    private double readFreeMemory() {
        try {
            Process p = Runtime.getRuntime().exec("vm_stat");
            try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                long freePages = 0;
                long pageSize = 4096;

                String line;
                while ((line = br.readLine()) != null) {
                    if (line.startsWith("Pages free")) {
                        freePages = Long.parseLong(line.replaceAll("[^0-9]", ""));
                    }
                    if (line.contains("page size of")) {
                        pageSize = Long.parseLong(line.replaceAll("[^0-9]", ""));
                    }
                }

                return (freePages * pageSize) / (1024.0 * 1024.0);
            }
        } catch (Exception e) {
            return -1.0;
        }
    }

    // ---------- RECORDING ----------
    private Row current;

    public void recordAdd(int scale, long addMs) {
        current = new Row();
        current.scale = scale;
        current.timestamp = System.currentTimeMillis();

        current.addMs = addMs;
        current.addCPU = readCPU();
        current.addMem = readFreeMemory();

        rows.add(current);
    }

    public void recordUpdate(long updateMs) {
        current.updateMs = updateMs;
        current.updateCPU = readCPU();
        current.updateMem = readFreeMemory();
    }

    public void recordDelete(long deleteMs) {
        current.deleteMs = deleteMs;
        current.deleteCPU = readCPU();
        current.deleteMem = readFreeMemory();
    }

    // ---------- CSV OUTPUT ----------
    public void writeCSV(String filename) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {

            pw.println(
                "scale,timestamp," +
                "add_ms,update_ms,delete_ms," +
                "add_cpu,update_cpu,delete_cpu," +
                "add_mem,update_mem,delete_mem"
            );

            for (Row r : rows) {
                pw.printf(
                    "%d,%d,%.4f,%.4f,%.4f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f%n",
                    r.scale, r.timestamp,

                    r.addMs, r.updateMs, r.deleteMs,

                    r.addCPU, r.updateCPU, r.deleteCPU,

                    r.addMem, r.updateMem, r.deleteMem
                );
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
