package com.danga.jmemcached.ehcache;

import java.io.IOException;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.Status;
import net.sf.ehcache.store.Store;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.danga.jmemcached.MemCachedClient;

/**
 * @author <A href="mailto:abashev at gmail dot com">Alexey Abashev</A>
 * @version $Id$
 */
public class MemcachedStore implements Store {
    private static final Log log = LogFactory.getLog(MemcachedStore.class);

    private Status status;
    private final MemCachedClient client;

    public MemcachedStore(Ehcache cache) {
        this.status = Status.STATUS_UNINITIALISED;

        this.client = new MemCachedClient();

        this.status = Status.STATUS_ALIVE;
    }

    /* (non-Javadoc)
     * @see net.sf.ehcache.store.Store#backedUp()
     */
    public boolean backedUp() {
        log.warn("'Backed Up' for memcached is not supported");

        return false;
    }

    /* (non-Javadoc)
     * @see net.sf.ehcache.store.Store#containsKey(java.lang.Object)
     */
    public boolean containsKey(Object key) {
        return client.keyExists(key.toString());
    }

    /* (non-Javadoc)
     * @see net.sf.ehcache.store.Store#dispose()
     */
    public void dispose() {

    }

    /* (non-Javadoc)
     * @see net.sf.ehcache.store.Store#expireElements()
     */
    public void expireElements() {
        log.warn("'Expire elements' for memcached is not supported");
    }

    /* (non-Javadoc)
     * @see net.sf.ehcache.store.Store#flush()
     */
    public void flush() throws IOException {
        log.warn("'Flush' for memcached is not supported");
    }

    /* (non-Javadoc)
     * @see net.sf.ehcache.store.Store#get(java.lang.Object)
     */
    public Element get(Object key) {
        if (log.isTraceEnabled()) {
            log.trace("Get element with key [" + key.toString() + "]");
        }
        
        return (Element) client.get(generateKey(key));
    }

    /* (non-Javadoc)
     * @see net.sf.ehcache.store.Store#getKeyArray()
     */
    public Object[] getKeyArray() {
        log.error("'Get keys' for memcached is not supported");

        return new Object[0];
    }

    /* (non-Javadoc)
     * @see net.sf.ehcache.store.Store#getQuiet(java.lang.Object)
     */
    public Element getQuiet(Object key) {
        if (log.isTraceEnabled()) {
            log.trace("Quietly get element with key [" + key + "]");
        }

        return (Element) client.get(generateKey(key));
    }

    /* (non-Javadoc)
     * @see net.sf.ehcache.store.Store#getSize()
     */
    public int getSize() {
        log.error("'Get size' for memcached is not supported");

        return 0;
    }

    /* (non-Javadoc)
     * @see net.sf.ehcache.store.Store#getStatus()
     */
    public Status getStatus() {
        return status;
    }

    /* (non-Javadoc)
     * @see net.sf.ehcache.store.Store#put(net.sf.ehcache.Element)
     */
    public void put(Element element) throws CacheException {
        Object key = element.getKey();

        if (log.isTraceEnabled()) {
            log.trace("Put in cache value [" + element + "] with key [" + key + "]");
        }
        
        client.add(generateKey(key), element);
    }

    /* (non-Javadoc)
     * @see net.sf.ehcache.store.Store#remove(java.lang.Object)
     */
    public Element remove(Object key) {
        if (log.isTraceEnabled()) {
            log.trace("Removing key [" + key + "] from cache");
        }

        client.delete(generateKey(key));

        return null;
    }

    /* (non-Javadoc)
     * @see net.sf.ehcache.store.Store#removeAll()
     */
    public void removeAll() throws CacheException {
        log.error("'Remove All' for memcached is not supported");

        throw new UnsupportedOperationException("Remove all is not supported");
    }
    
    /**
     * In current implementation we just emulating default toString 
     * @param object
     * @return
     */
    protected String generateKey(Object object) {
        String key = null;
        
        if (object != null) {
            key = object.getClass().getName() + "@" + Integer.toHexString(object.hashCode());
        }

        return key;
    }
}
