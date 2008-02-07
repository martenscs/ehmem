package com.googlecode.ehmem.profiles.memcached;

import org.apache.commons.logging.LogFactory;

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

    private static final SockIOPool pool;

    // set up connection pool once at class load
    static {

        // server list and weights
        String[] servers = {
                "192.168.0.1:11211",
                "127.0.0.1:11211"
        };

        Integer[] weights = {1, 1};

        // grab an instance of our connection pool
        pool = SockIOPool.getInstance();

        // set the servers and the weights
        pool.setServers( servers );
        pool.setWeights( weights );

        // set some basic pool settings
        // 5 initial, 5 min, and 250 max conns
        // and set the max idle time for a conn
        // to 6 hours
        pool.setInitConn( 5 );
        pool.setMinConn( 5 );
        pool.setMaxConn( 250 );
        pool.setMaxIdle( 1000 * 60 * 60 * 6 );

        // set the sleep for the maint thread
        // it will wake up every x seconds and
        // maintain the pool size
        pool.setMaintSleep( 30 );

        // set some TCP settings
        // disable nagle
        // set the read timeout to 3 secs
        // and don't set a connect timeout
        pool.setNagle( false );
        pool.setSocketTO( 3000 );
        pool.setSocketConnectTO( 0 );

        // initialize the connection pool
        pool.initialize();
    }

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
        System.out.println(client.stats().toString());

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
