package com.googlecode.ehmem.performance.runtime;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author <A href="mailto:abashev at gmail dot com">Alexey Abashev</A>
 * @version $Id$
 */
public class RuntimeStatistics {
    private static final Runtime runtime = Runtime.getRuntime();

    private static List<MemoryTimestamp> memoryTimestamps = new ArrayList<MemoryTimestamp>();
    private static List<CPUTimestamp> cpuTimestamps = new ArrayList<CPUTimestamp>();

    private static AtomicLong numberOfObjects = new AtomicLong();

    public static void memoryTimestamp() {
        synchronized (memoryTimestamps) {
            MemoryTimestamp m = new MemoryTimestamp(
                    System.currentTimeMillis(),
                    runtime.maxMemory(),
                    runtime.totalMemory(),
                    runtime.freeMemory()
            );

            memoryTimestamps.add(m);
        }
    }

    public static List<MemoryTimestamp> getMemoryTimestamps() {
        return memoryTimestamps;
    }

    public static void cpuTimestamp() {
//        synchronized (cpuTimestamps) {
//            CPUTimestamp c = new CPUTimestamp(
//                    System.currentTimeMillis(),
//                    instance.getCpuUsage()
//            );
//
//            cpuTimestamps.add(c);
//        }
    }

    public static List<CPUTimestamp> getCPUTimestamps() {
        return cpuTimestamps;
    }

    public static void incrementNumberOfObjects() {
        numberOfObjects.incrementAndGet();
    }

    public static long getNumberOfObjects() {
        return numberOfObjects.longValue();
    }
}
