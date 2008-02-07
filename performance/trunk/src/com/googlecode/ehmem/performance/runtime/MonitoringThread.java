package com.googlecode.ehmem.performance.runtime;

/**
 * @author <A href="mailto:abashev at gmail dot com">Alexey Abashev</A>
 * @version $Id$
 */
public class MonitoringThread extends Thread {
    private int delay;

    public MonitoringThread(int delay) {
        super("Runtime monitoring thread");

        this.delay = delay;
    }

    /* (non-Javadoc)
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
        try {
            while (true) {
                RuntimeStatistics.memoryTimestamp();
//                RuntimeStatistics.cpuTimestamp();

                Thread.sleep(delay);
            }
        } catch (InterruptedException e) {
        }
    }
}
