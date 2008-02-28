package com.danga.jmemcached.hibernate;

import java.util.Collections;
import java.util.Map;

import org.hibernate.cache.Cache;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.Timestamper;

import com.danga.jmemcached.MemCachedClient;

/**
 * @author <A href="mailto:abashev at gmail dot com">Alexey Abashev</A>
 * @version $Id$
 */
class MemcachedCache implements Cache {
    private final MemCachedClient client;
    private final String regionName;
    
    private final String regionHash;
    
    public MemcachedCache(MemCachedClient client, String regionName) {
        this.client = client;
        this.regionName = regionName;
        this.regionHash = getHash(regionName.hashCode());
    }

    /* (non-Javadoc)
     * @see org.hibernate.cache.Cache#get(java.lang.Object)
     */
    public Object get(Object key) throws CacheException {
        return client.get(generateKey(key));
    }

    /* (non-Javadoc)
     * @see org.hibernate.cache.Cache#clear()
     */
    public void clear() throws CacheException {
    }

    /* (non-Javadoc)
     * @see org.hibernate.cache.Cache#destroy()
     */
    public void destroy() throws CacheException {
    }

    /* (non-Javadoc)
     * @see org.hibernate.cache.Cache#getElementCountInMemory()
     */
    public long getElementCountInMemory() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see org.hibernate.cache.Cache#getElementCountOnDisk()
     */
    public long getElementCountOnDisk() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see org.hibernate.cache.Cache#getRegionName()
     */
    public String getRegionName() {
        return regionName;
    }

    /* (non-Javadoc)
     * @see org.hibernate.cache.Cache#getSizeInMemory()
     */
    public long getSizeInMemory() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see org.hibernate.cache.Cache#getTimeout()
     */
    public int getTimeout() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see org.hibernate.cache.Cache#nextTimestamp()
     */
    public long nextTimestamp() {
        return Timestamper.next();
    }

    /* (non-Javadoc)
     * @see org.hibernate.cache.Cache#put(java.lang.Object, java.lang.Object)
     */
    public void put(Object key, Object value) throws CacheException {
        client.add(generateKey(key), value);
    }

    /* (non-Javadoc)
     * @see org.hibernate.cache.Cache#read(java.lang.Object)
     */
    public Object read(Object key) throws CacheException {
        return get(key);
    }

    /* (non-Javadoc)
     * @see org.hibernate.cache.Cache#remove(java.lang.Object)
     */
    public void remove(Object key) throws CacheException {
        client.delete(generateKey(key));
    }

    /* (non-Javadoc)
     * @see org.hibernate.cache.Cache#update(java.lang.Object, java.lang.Object)
     */
    public void update(Object key, Object value) throws CacheException {
        client.replace(generateKey(key), value);
    }

    /* (non-Javadoc)
     * @see org.hibernate.cache.Cache#toMap()
     */
    @SuppressWarnings("unchecked")
    public Map toMap() {
        return Collections.emptyMap();
    }

    /* (non-Javadoc)
     * @see org.hibernate.cache.Cache#unlock(java.lang.Object)
     */
    public void unlock(Object key) throws CacheException {
    }

    /* (non-Javadoc)
     * @see org.hibernate.cache.Cache#lock(java.lang.Object)
     */
    public void lock(Object key) throws CacheException {
    }
    
    /**
     * In current implementation we just emulating default toString 
     * @param object
     * @return
     */
    protected String generateKey(Object object) {
        String key = null;
        
        if (object != null) {
            key = regionHash + "-" + getHash(object.getClass().getName().hashCode()) + "-" + Integer.toHexString(object.hashCode());
        }

        return key;
    }
    
    private String getHash(int hashCode) {
        return Integer.toHexString(hashCode);
    }
}
