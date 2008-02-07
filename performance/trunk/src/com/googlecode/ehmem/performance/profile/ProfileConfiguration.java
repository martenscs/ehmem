package com.googlecode.ehmem.performance.profile;

import java.util.List;

/**
 * @author <A href="mailto:abashev at gmail dot com">Alexey Abashev</A>
 * @version $Id$
 */
public class ProfileConfiguration {
    private String name;
    private List<CacheConfiguration> caches;

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the caches
     */
    public List<CacheConfiguration> getCaches() {
        return caches;
    }

    /**
     * @param caches the caches to set
     */
    public void setCaches(List<CacheConfiguration> caches) {
        this.caches = caches;
    }
}
