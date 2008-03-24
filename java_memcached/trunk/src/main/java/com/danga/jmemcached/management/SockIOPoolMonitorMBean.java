package com.danga.jmemcached.management;

/**
 * @author <A href="mailto:abashev at gmail dot com">Alexey Abashev</A>
 * @version $Id$
 */
public interface SockIOPoolMonitorMBean {
    int getInitConnections();

    int getMinConnections();

    int getMaxConnections();
}
