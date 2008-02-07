package com.googlecode.ehmem.performance.reports;

import java.util.List;

import com.googlecode.ehmem.performance.runtime.MemoryTimestamp;

/**
 * @author <A href="mailto:abashev at gmail dot com">Alexey Abashev</A>
 * @version $Id$
 */
public class MemoryReport {
    public String generateReport(List<MemoryTimestamp> timestamps) {
        long startTime = timestamps.get(0).getTimestamp();
        long startMemory = timestamps.get(0).getFree();

        for (MemoryTimestamp t : timestamps) {
            System.out.printf("%d - %d%n", t.getTimestamp() - startTime, t.getFree() - startMemory);
        }

        return "";
    }
}
