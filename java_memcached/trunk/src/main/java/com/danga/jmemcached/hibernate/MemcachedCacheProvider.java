package com.danga.jmemcached.hibernate;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.cache.Cache;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.CacheProvider;
import org.hibernate.cache.Timestamper;

import com.danga.jmemcached.MemCachedClient;

/**
 * @author <A href="mailto:abashev at gmail dot com">Alexey Abashev</A>
 * @version $Id$
 */
public class MemcachedCacheProvider implements CacheProvider {
    private static final Log log = LogFactory.getLog(MemcachedCacheProvider.class);

    private MemCachedClient client;

    /* (non-Javadoc)
     * @see org.hibernate.cache.CacheProvider#start(java.util.Properties)
     */
    public void start(Properties properties) throws CacheException {
        log.info("Start new memcached provider");
        
        client = new MemCachedClient();
    }

    /* (non-Javadoc)
     * @see org.hibernate.cache.CacheProvider#buildCache(java.lang.String, java.util.Properties)
     */
    public Cache buildCache(String regionName, Properties properties) throws CacheException {
        return new MemcachedCache(client, regionName);
    }

    /* (non-Javadoc)
     * @see org.hibernate.cache.CacheProvider#stop()
     */
    public void stop() {
        log.info("Stop memcached provider");
        
        if ((client != null) && (client.getPool() != null)) {
            client.getPool().shutDown();
        }
    }

    /* (non-Javadoc)
     * @see org.hibernate.cache.CacheProvider#isMinimalPutsEnabledByDefault()
     */
    public boolean isMinimalPutsEnabledByDefault() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.hibernate.cache.CacheProvider#nextTimestamp()
     */
    public long nextTimestamp() {
        return Timestamper.next();
    }
}
