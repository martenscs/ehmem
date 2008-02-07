package com.googlecode.ehmem.performance.runtime;

/**
 * @author <A href="mailto:abashev at gmail dot com">Alexey Abashev</A>
 * @version $Id$
 */
public class CPUTimestamp {
    private final long timestamp;
    private final int cpuload;

    public CPUTimestamp(long timestamp, int cpuload) {
        this.timestamp = timestamp;
        this.cpuload = cpuload;
    }

    /**
     * @return the timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * @return the cpuload
     */
    public int getCpuload() {
        return cpuload;
    }
}
