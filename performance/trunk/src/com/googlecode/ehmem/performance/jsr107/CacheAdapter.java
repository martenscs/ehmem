package com.googlecode.ehmem.performance.jsr107;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheEntry;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheListener;
import net.sf.jsr107cache.CacheStatistics;

/**
 * A little adapter for throwing exception for all not implemented methods.
 *
 * @author <A href="mailto:abashev at gmail dot com">Alexey Abashev</A>
 * @version $Id$
 */
public class CacheAdapter implements Cache {
    /* (non-Javadoc)
     * @see net.sf.jsr107cache.Cache#addListener(net.sf.jsr107cache.CacheListener)
     */
    @Override
    public void addListener(CacheListener arg0) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /* (non-Javadoc)
     * @see net.sf.jsr107cache.Cache#clear()
     */
    @Override
    public void clear() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /* (non-Javadoc)
     * @see net.sf.jsr107cache.Cache#containsKey(java.lang.Object)
     */
    @Override
    public boolean containsKey(Object arg0) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /* (non-Javadoc)
     * @see net.sf.jsr107cache.Cache#containsValue(java.lang.Object)
     */
    @Override
    public boolean containsValue(Object arg0) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /* (non-Javadoc)
     * @see net.sf.jsr107cache.Cache#entrySet()
     */
    @Override
    public Set entrySet() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /* (non-Javadoc)
     * @see net.sf.jsr107cache.Cache#evict()
     */
    @Override
    public void evict() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /* (non-Javadoc)
     * @see net.sf.jsr107cache.Cache#get(java.lang.Object)
     */
    @Override
    public Object get(Object arg0) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /* (non-Javadoc)
     * @see net.sf.jsr107cache.Cache#getAll(java.util.Collection)
     */
    @Override
    public Map getAll(Collection arg0) throws CacheException {
        throw new UnsupportedOperationException("Not implemented");
    }

    /* (non-Javadoc)
     * @see net.sf.jsr107cache.Cache#getCacheEntry(java.lang.Object)
     */
    @Override
    public CacheEntry getCacheEntry(Object arg0) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /* (non-Javadoc)
     * @see net.sf.jsr107cache.Cache#getCacheStatistics()
     */
    @Override
    public CacheStatistics getCacheStatistics() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /* (non-Javadoc)
     * @see net.sf.jsr107cache.Cache#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /* (non-Javadoc)
     * @see net.sf.jsr107cache.Cache#keySet()
     */
    @Override
    public Set keySet() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /* (non-Javadoc)
     * @see net.sf.jsr107cache.Cache#load(java.lang.Object)
     */
    @Override
    public void load(Object arg0) throws CacheException {
        throw new UnsupportedOperationException("Not implemented");
    }

    /* (non-Javadoc)
     * @see net.sf.jsr107cache.Cache#loadAll(java.util.Collection)
     */
    @Override
    public void loadAll(Collection arg0) throws CacheException {
        throw new UnsupportedOperationException("Not implemented");
    }

    /* (non-Javadoc)
     * @see net.sf.jsr107cache.Cache#peek(java.lang.Object)
     */
    @Override
    public Object peek(Object arg0) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /* (non-Javadoc)
     * @see net.sf.jsr107cache.Cache#put(java.lang.Object, java.lang.Object)
     */
    @Override
    public Object put(Object arg0, Object arg1) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /* (non-Javadoc)
     * @see net.sf.jsr107cache.Cache#putAll(java.util.Map)
     */
    @Override
    public void putAll(Map arg0) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /* (non-Javadoc)
     * @see net.sf.jsr107cache.Cache#remove(java.lang.Object)
     */
    @Override
    public Object remove(Object arg0) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /* (non-Javadoc)
     * @see net.sf.jsr107cache.Cache#removeListener(net.sf.jsr107cache.CacheListener)
     */
    @Override
    public void removeListener(CacheListener arg0) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /* (non-Javadoc)
     * @see net.sf.jsr107cache.Cache#size()
     */
    @Override
    public int size() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /* (non-Javadoc)
     * @see net.sf.jsr107cache.Cache#values()
     */
    @Override
    public Collection values() {
        throw new UnsupportedOperationException("Not implemented");
    }
}
