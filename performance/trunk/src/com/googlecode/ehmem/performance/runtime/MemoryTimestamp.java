package com.googlecode.ehmem.performance.runtime;

/**
 * @author <A href="mailto:abashev at gmail dot com">Alexey Abashev</A>
 * @version $Id$
 */
public class MemoryTimestamp {
    private final long timestamp;
    private final long free;
    private final long total;
    private final long max;

    public MemoryTimestamp(long timestamp, long max, long total, long free) {
        this.timestamp = timestamp;
        this.max = max;
        this.total = total;
        this.free = free;
    }

    /**
     * @return the timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * @return the free
     */
    public long getFree() {
        return free;
    }

    /**
     * @return the total
     */
    public long getTotal() {
        return total;
    }

    /**
     * @return the max
     */
    public long getMax() {
        return max;
    }
}
