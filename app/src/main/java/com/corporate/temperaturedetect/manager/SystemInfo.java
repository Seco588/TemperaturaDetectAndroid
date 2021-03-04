package com.corporate.temperaturedetect.manager;

import android.app.ActivityManager;
import android.content.Context;

import java.io.File;
import java.text.NumberFormat;

import static android.content.Context.ACTIVITY_SERVICE;

public class SystemInfo {
    private final long GB = 1073741824;
    private final long MB = 1048576;

    private Runtime runtime = Runtime.getRuntime();

    public String Info() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.OsInfo());
        sb.append(this.MemInfo());
        sb.append(this.DiskInfo());
        return sb.toString();
    }

    public String OSname() {
        return System.getProperty("os.name");
    }

    public String OSversion() {
        return System.getProperty("os.version");
    }

    public String OsArch() {
        return System.getProperty("os.arch");
    }

    public long totalMem() {
        long l = Runtime.getRuntime().totalMemory();
        return toMB(l);
    }

    public long maxMemory() {
        long l = Runtime.getRuntime().maxMemory();
        return toMB(l);
    }

    public long usedMem() {
        long l = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        return toMB(l);
    }

    public String MemInfo() {
        NumberFormat format = NumberFormat.getInstance();
        StringBuilder sb = new StringBuilder();
        long maxMemory = runtime.maxMemory();
        long allocatedMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        sb.append("\nFree memory: ");
        sb.append(toMB(freeMemory));
        sb.append(" MB");
        sb.append("\nAllocated memory:");
        sb.append(toMB(allocatedMemory));
        sb.append(" MB");
        sb.append("\nMax memory: ");
        sb.append(toMB(maxMemory));
        sb.append(" MB");
        sb.append("\nTotal free memory: ");
        long k = freeMemory + (maxMemory - allocatedMemory);
        sb.append(toMB(k));
        sb.append(" MB");
        sb.append("\nMemory used : ");
        long Memory_used = maxMemory - k;
        sb.append(toMB(Memory_used));
        sb.append(" MB");
        return sb.toString();

    }

    public String OsInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("\nOS: ");
        sb.append(this.OSname());
        sb.append("\nVersion: ");
        sb.append(this.OSversion());
        sb.append(": ");
        sb.append(this.OsArch());
        sb.append("\nAvailable processors (cores): ");
        sb.append(runtime.availableProcessors());
        return sb.toString();
    }

    public String DiskInfo() {
        /* Get a list of all filesystem roots on this system */
        File[] roots = File.listRoots();
        StringBuilder sb = new StringBuilder();

        /* For each filesystem root, print some info */
        for (File root : roots) {
            sb.append("\nFile system root: ");
            sb.append(root.getAbsolutePath());
            sb.append("\nTotal space (bytes): ");
            sb.append(root.getTotalSpace());
            sb.append("\nFree space (bytes): ");
            sb.append(root.getFreeSpace());
            sb.append("\nUsable space (bytes): ");
            sb.append(root.getUsableSpace());
        }
        return sb.toString();
    }

    public long toGB(long bytes) {
        return bytes / GB;
    }


    public long toMB(long bytes) {
        return bytes / MB;
    }

    //Ram Device
    public long freeRamMemorySizeDevice(Context context) {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        return mi.availMem / 1048576L;
    }

    public long usedRamMemoryDevice(Context context) {
        long free = freeRamMemorySizeDevice(context);
        long total = totalRamMemorySizeDevice(context);
        return total - free;
    }

    public long totalRamMemorySizeDevice(Context context) {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        return mi.totalMem / 1048576L;
    }


}
