package com.googlecode.ehmem.performance.reports;

import net.sf.jsr107cache.CacheStatistics;

/**
 * @author <A href="mailto:abashev at gmail dot com">Alexey Abashev</A>
 * @version $Id$
 */
public class CacheReport {
    public String generateReport(String name, CacheStatistics stat) {
        System.out.println("Name: " + name);
        System.out.println("Object count: " + stat.getObjectCount());
        System.out.println("Hits: " + stat.getCacheHits());
        System.out.println("Misses: " + stat.getCacheMisses());

        return "";
    }
}
