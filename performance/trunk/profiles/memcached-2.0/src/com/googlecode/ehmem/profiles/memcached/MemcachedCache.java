package com.googlecode.ehmem.profiles.memcached;

import java.util.HashMap;
import java.util.Map;

import net.sf.jsr107cache.CacheStatistics;

import com.danga.jmemcached.MemCachedClient;
import com.danga.jmemcached.SockIOPool;
import com.googlecode.ehmem.performance.jsr107.CacheAdapter;

/**
 * @author <A href="mailto:abashev at gmail dot com">Alexey Abashev</A>
 * @version $Id$
 */
public class MemcachedCache extends CacheAdapter {
    // create a static client as most installs only need
    // a single instance
    private MemCachedClient client = new MemCachedClient();
    private Statistics statistics = new Statistics();

    /* (non-Javadoc)
     * @see com.googlecode.ehmem.performance.jsr107.CacheAdapter#get(java.lang.Object)
     */
    @Override
    public Object get(Object key) {
        final Object object = client.get(key.toString());

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
        return client.add(key.toString(), value);
    }

    /* (non-Javadoc)
     * @see com.googlecode.ehmem.performance.jsr107.CacheAdapter#getCacheStatistics()
     */
    @Override
    public CacheStatistics getCacheStatistics() {
//        Map<String, Map<String, String>> stats = new HashMap<String, Map<String,String>>(client.stats());

        client.stats().entrySet().toString();
//
//        for (Entry<String, Map<String, String>> entry : stats.entrySet()) {
//            System.out.println(entry.getKey());
//
////            for (Entry<String, String> param : entry.getValue().entrySet()) {
////                System.out.println(" - " + param.getKey() + " = " + param.getValue());
////            }
//        }

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
