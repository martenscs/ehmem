package com.googlecode.ehmem.performance;

import java.util.Random;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheManager;

import com.googlecode.ehmem.performance.object.ObjectFactory;

/**
 * @author <A href="mailto:abashev at gmail dot com">Alexey Abashev</A>
 * @version $Id$
 */
public class CacheConsumer implements Runnable {
    private final String cacheName;

    public CacheConsumer(String cacheName) {
        this.cacheName = cacheName;
    }

    @Override
    public void run() {
        Cache cache = CacheManager.getInstance().getCache(cacheName);
        Random random = new Random();

        try {
            while (true) {
                Thread.sleep((long) (random.nextFloat() * 5));

                long maxKey = ObjectFactory.getMaxKey(cacheName);
                long key = (long) (maxKey * random.nextFloat());

                Object value = cache.get(String.valueOf(key));

                if (value != null) {
                    if (!value.getClass().getName().equals(cacheName)) {
                        new RuntimeException("Wrong object in cache");
                    }
                }
            }
        } catch (InterruptedException e) {
        }
    }
}
