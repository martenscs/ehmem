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

    private final Ehcache cache;
    private Status status;
    private final MemCachedClient client;

    public MemcachedStore(Ehcache cache) {
        this.status = Status.STATUS_UNINITIALISED;

        this.cache = cache;
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
        return (Element) client.get(key.toString());
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
        return (Element) client.get(key.toString());
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
        client.add(element.getKey().toString(), element);
    }

    /* (non-Javadoc)
     * @see net.sf.ehcache.store.Store#remove(java.lang.Object)
     */
    public Element remove(Object key) {
        client.delete(key.toString());

        return null;
    }

    /* (non-Javadoc)
     * @see net.sf.ehcache.store.Store#removeAll()
     */
    public void removeAll() throws CacheException {
        log.error("'Remove All' for memcached is not supported");

        throw new UnsupportedOperationException("Remove all is not supported");
    }
}
