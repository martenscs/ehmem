package com.googlecode.ehmem.performance;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheManager;
import net.sf.jsr107cache.CacheStatistics;

import com.googlecode.ehmem.performance.profile.CacheConfiguration;
import com.googlecode.ehmem.performance.profile.ProfileConfiguration;
import com.googlecode.ehmem.performance.profile.ProfileLoader;
import com.googlecode.ehmem.performance.reports.CacheReport;
import com.googlecode.ehmem.performance.reports.MemoryReport;
import com.googlecode.ehmem.performance.runtime.MonitoringThread;
import com.googlecode.ehmem.performance.runtime.RuntimeStatistics;

/**
 * @author <A href="mailto:abashev at gmail dot com">Alexey Abashev</A>
 * @version $Id$
 */
public class CacheMeter {
    private final int producers;
    private final int consumers;
    private final String cacheName;

    private ExecutorService consumerPool;
    private ExecutorService producerPool;

    public CacheMeter(String cacheName, int producers, int consumers) {
        this.cacheName = cacheName;
        this.producers = producers;
        this.consumers = consumers;
    }

    public void init() {
        consumerPool = Executors.newFixedThreadPool(consumers);
        producerPool = Executors.newFixedThreadPool(producers);
    }

    public void start() {
        for (int i = 0; i < producers; i++) {
            producerPool.execute(new CacheProducer(cacheName));
        }

        for (int i = 0; i < consumers; i++) {
            consumerPool.execute(new CacheConsumer(cacheName));
        }
    }

    public void stop() {
        consumerPool.shutdownNow();
        producerPool.shutdownNow();
    }

    public static void main(String[] args) throws Exception {
        ProfileConfiguration config = ProfileLoader.loadProfile(args[0]);

        System.out.println("Name: " + config.getName());

        int producers = Integer.parseInt(args[1]);
        int consumers = Integer.parseInt(args[2]);
        long delay = Long.parseLong(args[3]) * 1000;

        System.out.println("Producers: " + producers + " Consumers: " + consumers);

        Collection<CacheMeter> meters = new ArrayList<CacheMeter>(config.getCaches().size());

        for (CacheConfiguration c : config.getCaches()) {
            CacheMeter meter = new CacheMeter(c.getName(), producers, consumers);
            Class<Cache> providerClass = (Class<Cache>) Class.forName(c.getClazz());
            Constructor<Cache> constructor = null;
            Cache cache;

            try {
                constructor = providerClass.getConstructor(String.class);
            } catch (Exception e) {
            }

            if (constructor != null) {
                cache = constructor.newInstance(c.getName());
            } else {
                cache = providerClass.newInstance();
            }

            CacheManager.getInstance().registerCache(c.getName(), cache);

            meter.init();

            meters.add(meter);
        }

        MonitoringThread monitor = new MonitoringThread(2000);

        Thread.sleep(3000);
        System.gc(); System.gc(); System.gc();
        Thread.sleep(3000);

        monitor.start();

        for (CacheMeter meter : meters) {
            meter.start();
        }

        Thread.sleep(delay);

        for (CacheMeter meter : meters) {
            meter.stop();
        }

        System.out.println("Number of objects: " + RuntimeStatistics.getNumberOfObjects());

        final CacheReport cacheReport = new CacheReport();

        for (CacheConfiguration c : config.getCaches()) {
            Cache cache = CacheManager.getInstance().getCache(c.getName());
            CacheStatistics stat = cache.getCacheStatistics();

            System.out.println(cacheReport.generateReport(c.getName(), stat));
        }

        final MemoryReport memoryReport = new MemoryReport();

        System.out.println(memoryReport.generateReport(RuntimeStatistics.getMemoryTimestamps()));

        System.exit(0);
    }
}
