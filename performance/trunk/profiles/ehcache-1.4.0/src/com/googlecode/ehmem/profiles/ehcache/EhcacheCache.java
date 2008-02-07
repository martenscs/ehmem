package com.googlecode.ehmem.profiles.ehcache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.Statistics;
import net.sf.jsr107cache.CacheStatistics;

import com.googlecode.ehmem.performance.jsr107.CacheAdapter;

/**
 * @author <A href="mailto:abashev at gmail dot com">Alexey Abashev</A>
 * @version $Id$
 */
public class EhcacheCache extends CacheAdapter {
    private Cache cache;

    public EhcacheCache(String name) {
        cache = CacheManager.create().getCache(name);

        if (cache == null) {
            throw new RuntimeException("Unable to find cache with name " + name);
        }
    }

    /* (non-Javadoc)
     * @see com.googlecode.ehmem.performance.jsr107.CacheAdapter#get(java.lang.Object)
     */
    @Override
    public Object get(Object key) {
        return cache.get(key);
    }

    /* (non-Javadoc)
     * @see com.googlecode.ehmem.performance.jsr107.CacheAdapter#put(java.lang.Object, java.lang.Object)
     */
    @Override
    public Object put(Object key, Object value) {
        cache.put(new Element(key, value));

        return null;
    }

    /* (non-Javadoc)
     * @see com.googlecode.ehmem.performance.jsr107.CacheAdapter#getCacheStatistics()
     */
    @Override
    public CacheStatistics getCacheStatistics() {
        return new StatWrapper(cache.getStatistics());
    }

    private class StatWrapper implements CacheStatistics {
        private Statistics stat;

        public StatWrapper(Statistics stat) {
            this.stat = stat;
        }

        @Override
        public int getCacheHits() {
            return (int) stat.getCacheHits();
        }

        @Override
        public int getCacheMisses() {
            return (int) stat.getCacheMisses();
        }

        @Override
        public int getObjectCount() {
            return (int) stat.getObjectCount();
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
