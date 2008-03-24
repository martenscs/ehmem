package com.danga.jmemcached.management;

import java.lang.management.ManagementFactory;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.danga.jmemcached.MemCachedClient;

/**
 * @author <A href="mailto:abashev at gmail dot com">Alexey Abashev</A>
 * @version $Id$
 */
public class JMXHelper {
    private static final Log log = LogFactory.getLog(JMXHelper.class);

    private static final Collection<ObjectName> registeredClients = new HashSet<ObjectName>();
    private static final Map<String, ObjectName> registeredPools = new HashMap<String, ObjectName>();
    private static final Collection<ObjectName> registeredMonitors = new HashSet<ObjectName>();
    
    public static void registerClient(MemCachedClient client) {
        log.info("Registering new JMX client " + client);

        final MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
        final String poolName = client.getPool().getName();
        
        if (!registeredPools.containsKey(poolName)) {
            log.info("Registering new SockIO pool [" + poolName + "]");
            
            SockIOPoolMonitor monitor = new SockIOPoolMonitor(client.getPool());
            
            try {
                mbeanServer.registerMBean(monitor, monitor.getObjectName());
                
                registeredPools.put(poolName, monitor.getObjectName());
                
                for (String server : client.getPool().getServers()) {
                    PoolMonitor poolMonitor = new PoolMonitor(client.getPool(), server);
                    
                    mbeanServer.registerMBean(poolMonitor, poolMonitor.getObjectName());
                    
                    registeredMonitors.add(poolMonitor.getObjectName());
                }
            } catch (InstanceAlreadyExistsException e) {
                log.error("Unable to register mbean for pool - " + poolName, e);
            } catch (MBeanRegistrationException e) {
                log.error("Unable to register mbean for pool - " + poolName, e);
            } catch (NotCompliantMBeanException e) {
                log.error("Unable to register mbean for pool - " + poolName, e);
            }
        }
        
        try {
            ClientMonitor clientMonitor = new ClientMonitor(client);
            ObjectName name = clientMonitor.getObjectName();
            
            mbeanServer.registerMBean(clientMonitor, name);
            registeredClients.add(name);
        } catch (InstanceAlreadyExistsException e) {
            log.error("Unable to register mbean for client - " + client, e);
        } catch (MBeanRegistrationException e) {
            log.error("Unable to register mbean for client - " + client, e);
        } catch (NotCompliantMBeanException e) {
            log.error("Unable to register mbean for client - " + client, e);
        }
    }
    
    public static void unregisterClient(MemCachedClient client) {
        log.info("Unregistering new JMX client " + client);

        final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        
        for (ObjectName name : registeredMonitors) {
            try {
                server.unregisterMBean(name);
            } catch (InstanceNotFoundException e) {
                log.error("Unable to unregister mbean " + name, e);
            } catch (MBeanRegistrationException e) {
                log.error("Unable to unregister mbean " + name, e);
            }
        }

        for (ObjectName name : registeredPools.values()) {
            try {
                server.unregisterMBean(name);
            } catch (InstanceNotFoundException e) {
                log.error("Unable to unregister mbean " + name, e);
            } catch (MBeanRegistrationException e) {
                log.error("Unable to unregister mbean " + name, e);
            }
        }
        
        for (ObjectName name : registeredClients) {
            try {
                server.unregisterMBean(name);
            } catch (InstanceNotFoundException e) {
                log.error("Unable to unregister mbean " + name, e);
            } catch (MBeanRegistrationException e) {
                log.error("Unable to unregister mbean " + name, e);
            }
        }
    }
}
