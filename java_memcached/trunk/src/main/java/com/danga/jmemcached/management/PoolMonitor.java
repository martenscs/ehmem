package com.danga.jmemcached.management;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.danga.jmemcached.SockIOPool;

/**
 * @author <A href="mailto:abashev at gmail dot com">Alexey Abashev</A>
 * @version $Id$
 */
public class PoolMonitor implements PoolMonitorMBean {
    private final Log log = LogFactory.getLog(PoolMonitor.class);
    
    private SockIOPool pool;
    private String host;
    
    private ObjectName objectName;
    
    public PoolMonitor(SockIOPool pool, String host) {
        this.pool = pool;
        this.host = host;
        this.objectName = createObjectName();
    }
    
    /* (non-Javadoc)
     * @see com.danga.jmemcached.management.PoolMonitorMBean#getAvailableSockets()
     */
    public int getAvailableSockets() {
        return pool.getAvailableSockets(host);
    }

    /* (non-Javadoc)
     * @see com.danga.jmemcached.management.PoolMonitorMBean#getBusySockets()
     */
    public int getBusySockets() {
        return pool.getBusySockets(host);
    }

    /* (non-Javadoc)
     * @see com.danga.jmemcached.management.PoolMonitorMBean#getDeadSockets()
     */
    public int getDeadSockets() {
        return pool.getDeadSockets(host);
    }

    /**
     * @return the objectName
     */
    public ObjectName getObjectName() {
        return objectName;
    }

    private ObjectName createObjectName() {
        ObjectName objectName = null;

        try {
            objectName = new ObjectName("com.danga.jmemcached:type=PoolMonitor,SockIOPool=" + pool.getName() + ",name=" + host.replace(':', '_'));
        } catch (MalformedObjectNameException e) {
            log.error("Unable to create object name for pool - " + pool.getName(), e);
        } catch (NullPointerException e) {
            log.error("Unable to create object name for pool - " + pool.getName(), e);
        }
        
        return objectName;
    }
}
