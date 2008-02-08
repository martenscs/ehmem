package com.googlecode.ehmem.profiles.memcached;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import net.sf.jsr107cache.CacheStatistics;
import net.spy.memcached.MemcachedClient;

import com.googlecode.ehmem.performance.jsr107.CacheAdapter;

/**
 * @author <A href="mailto:abashev at gmail dot com">Alexey Abashev</A>
 * @version $Id$
 */
public class FastMemcachedCache extends CacheAdapter {
    private static MemcachedClient client;

    private Statistics statistics = new Statistics();

    // set up connection pool once at class load
    static {
        try {
            client = new MemcachedClient(
                    Arrays.asList(
                            new InetSocketAddress("127.0.0.1", 11212),
                            new InetSocketAddress("192.168.0.1", 11211),
                            new InetSocketAddress("127.0.0.1", 11211)
                    )
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /* (non-Javadoc)
     * @see com.googlecode.ehmem.performance.jsr107.CacheAdapter#get(java.lang.Object)
     */
    @Override
    public Object get(Object key) {
     // Try to get a value, for up to 5 seconds, and cancel if it doesn't return
        Object object = null;
        Future<Object> f = client.asyncGet(key.toString());

        try {
            object = f.get(5, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            // Since we don't need this, go ahead and cancel the operation.  This
            // is not strictly necessary, but it'll save some work on the server.
            f.cancel(false);
            // Do other timeout related stuff
        } catch (InterruptedException e) {
            f.cancel(false);
        } catch (ExecutionException e) {
            f.cancel(false);
        }

        if (object == null) {
            statistics.misses++;
        } else {
            statistics.hits++;
        }

        return object;
    }

    /* (non-Javadoc)
     * @see com.googlecode.ehmem.performance.jsr107.CacheAdapter#put(java.lang.Object, java.lang.Object)
     */
    @Override
    public Object put(Object key, Object value) {
        return client.set(key.toString(), 120, value);
    }


    /* (non-Javadoc)
     * @see com.googlecode.ehmem.performance.jsr107.CacheAdapter#getCacheStatistics()
     */
    @Override
    public CacheStatistics getCacheStatistics() {
        System.out.println(client.getStats());

        return statistics;
    }

    private class Statistics implements CacheStatistics {
        int hits = 0;
        int misses = 0;

        @Override
        public int getCacheHits() {
            return hits;
        }

        @Override
        public int getCacheMisses() {
            return misses;
        }

        @Override
        public int getObjectCount() {
            return (-1);
        }

        @Override
        public void clearStatistics() {
        }

        @Override
        public int getStatisticsAccuracy() {
            return CacheStatistics.STATISTICS_ACCURACY_GUARANTEED;
        }
    }
}
