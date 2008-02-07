package com.googlecode.ehmem.performance;

import java.util.Random;

import com.googlecode.ehmem.performance.object.Keyed;
import com.googlecode.ehmem.performance.object.ObjectFactory;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheManager;

/**
 * @author <A href="mailto:abashev at gmail dot com">Alexey Abashev</A>
 * @version $Id$
 */
public class CacheProducer implements Runnable  {
    private final String cacheName;

    public CacheProducer(String cacheName) {
        this.cacheName = cacheName;
    }

    @Override
    public void run() {
        Cache cache = CacheManager.getInstance().getCache(cacheName);
        Random random = new Random();

        try {
            while (true) {
                Thread.sleep((long) (random.nextFloat() * 10));

                Keyed element = ObjectFactory.create(cacheName);

                cache.put(element.getKey(), element);
            }
        } catch (InterruptedException e) {
        }
    }
}
