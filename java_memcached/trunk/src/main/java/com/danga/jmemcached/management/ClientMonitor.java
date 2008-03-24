package com.danga.jmemcached.management;

import java.util.Map;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.danga.jmemcached.MemCachedClient;

/**
 * @author <A href="mailto:abashev at gmail dot com">Alexey Abashev</A>
 * @version $Id$
 */
public class ClientMonitor implements ClientMonitorMBean {
    private final Log log = LogFactory.getLog(ClientMonitor.class);

    private final MemCachedClient client;
    private final ObjectName objectName;
    
    public ClientMonitor(MemCachedClient client) {
        this.client = client;
        this.objectName = createObjectName();
    }
    
    /* (non-Javadoc)
     * @see com.danga.jmemcached.management.ClientMonitorMBean#flushAll()
     */
    public void flushAll() {
        client.flushAll();
    }
    
    /* (non-Javadoc)
     * @see com.danga.jmemcached.management.ClientMonitorMBean#getStatistics()
     */
    public Map<String, Object> stats() {
        return client.stats();
    }

    public ObjectName getObjectName() {
        return objectName;
    }
    
    private ObjectName createObjectName() {
        ObjectName objectName = null;

        try {
            objectName = new ObjectName("com.danga.jmemcached:type=MemcachedClient,MemcachedClient=" + client + ",name=");
        } catch (MalformedObjectNameException e) {
            log.error("Unable to create object name for client - " + client, e);
        } catch (NullPointerException e) {
            log.error("Unable to create object name for client - " + client, e);
        }
        
        return objectName;
    }

}
