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
public class SockIOPoolMonitor implements SockIOPoolMonitorMBean {
    private final Log log = LogFactory.getLog(SockIOPoolMonitor.class);

    private final SockIOPool pool;
    private final ObjectName objectName;
    
    public SockIOPoolMonitor(SockIOPool pool) {
        this.pool = pool;
        this.objectName = createObjectName();
    }

    /* (non-Javadoc)
     * @see com.danga.jmemcached.management.SockIOPoolMonitorMBean#getInitConnections()
     */
    public int getInitConnections() {
        return pool.getInitConn();
    }

    /* (non-Javadoc)
     * @see com.danga.jmemcached.management.SockIOPoolMonitorMBean#getMaxConnections()
     */
    public int getMaxConnections() {
        return pool.getMaxConn();
    }
    
    /* (non-Javadoc)
     * @see com.danga.jmemcached.management.SockIOPoolMonitorMBean#getMinConnections()
     */
    public int getMinConnections() {
        return pool.getMinConn();
    }

    public ObjectName getObjectName() {
        return objectName;
    }
    
    private ObjectName createObjectName() {
        ObjectName objectName = null;

        try {
            objectName = new ObjectName("com.danga.jmemcached:type=SockIOPool,name=" + pool.getName());
        } catch (MalformedObjectNameException e) {
            log.error("Unable to create object name for pool - " + pool.getName(), e);
        } catch (NullPointerException e) {
            log.error("Unable to create object name for pool - " + pool.getName(), e);
        }
        
        return objectName;
    }
}
