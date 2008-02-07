package com.googlecode.ehmem.profiles.hashmap;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.jsr107cache.CacheStatistics;

import com.googlecode.ehmem.performance.jsr107.CacheAdapter;

/**
 * @author <A href="mailto:abashev at gmail dot com">Alexey Abashev</A>
 * @version $Id$
 */
public class HashMapCache extends CacheAdapter {
    private Map<Object, Object> map = new ConcurrentHashMap<Object, Object>();
    private Statistics statistics = new Statistics();

    /* (non-Javadoc)
     * @see com.googlecode.ehmem.performance.jsr107.CacheAdapter#clear()
     */
    @Override
    public void clear() {
        map.clear();
    }

    /* (non-Javadoc)
     * @see com.googlecode.ehmem.performance.jsr107.CacheAdapter#containsKey(java.lang.Object)
     */
    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    /* (non-Javadoc)
     * @see com.googlecode.ehmem.performance.jsr107.CacheAdapter#containsValue(java.lang.Object)
     */
    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    /* (non-Javadoc)
     * @see com.googlecode.ehmem.performance.jsr107.CacheAdapter#get(java.lang.Object)
     */
    @Override
    public Object get(Object key) {
        Object value = map.get(key);

        if (value == null) {
            statistics.misses++;
        } else {
            statistics.hits++;
        }

        return value;
    }

    /* (non-Javadoc)
     * @see com.googlecode.ehmem.performance.jsr107.CacheAdapter#getCacheStatistics()
     */
    @Override
    public CacheStatistics getCacheStatistics() {
        return statistics;
    }

    /* (non-Javadoc)
     * @see com.googlecode.ehmem.performance.jsr107.CacheAdapter#put(java.lang.Object, java.lang.Object)
     */
    @Override
    public Object put(Object key, Object value) {
        return map.put(key, value);
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
            return map.size();
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
