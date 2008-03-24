package com.danga.jmemcached.management;

import java.util.Map;

/**
 * @author <A href="mailto:abashev at gmail dot com">Alexey Abashev</A>
 * @version $Id$
 */
public interface ClientMonitorMBean {
    void flushAll();
    
    Map<String, Object> stats(); 
}
