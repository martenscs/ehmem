package net.sf.ehcache.management;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import net.sf.ehcache.CacheException;

/**
 * @author <A href="mailto:abashev at gmail dot com">Alexey Abashev</A>
 * @version $Id$
 */
public class CacheGraphics implements DynamicMBean {
    private CacheManager cacheManager;
    private ObjectName objectName;
    
    public CacheGraphics(CacheManager manager) {
        this.cacheManager = manager;
        
        try {
            objectName = new ObjectName("net.sf.ehcache:type=CacheGraphics,name=" + manager);
        } catch (MalformedObjectNameException e) {
            throw new CacheException("Unable to register cache graphics", e);
        } catch (NullPointerException e) {
            throw new CacheException("Unable to register cache graphics", e);
        }
    }
    
    /* (non-Javadoc)
     * @see javax.management.DynamicMBean#getAttribute(java.lang.String)
     */
    public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
        return "valie";
    }

    /* (non-Javadoc)
     * @see javax.management.DynamicMBean#getAttributes(java.lang.String[])
     */
    public AttributeList getAttributes(String[] attributes) {
        @SuppressWarnings("unchecked")
        List<Cache> caches = cacheManager.getCaches();
        Map<String, String> bars = new TreeMap<String, String>();
        
        for (Cache cache : caches) {
            final String bar = generateBar();
            
            bars.put(cache.getName(), bar);
        }

        AttributeList list = new AttributeList();
        
        for (Entry<String, String> entry : bars.entrySet()) {
            list.add(new Attribute(entry.getKey(), entry.getValue()));
        }
    
        return list;    
    }

    /* (non-Javadoc)
     * @see javax.management.DynamicMBean#getMBeanInfo()
     */
    
    public MBeanInfo getMBeanInfo() {
        String[] caches = cacheManager.getCacheNames();
        MBeanAttributeInfo[] attrs = new MBeanAttributeInfo[caches.length];
        
        for (int i = 0; i < caches.length; i++) {
            attrs[i] = new MBeanAttributeInfo(
                    caches[i],
                    "java.lang.String",
                    "Property " + caches[i],
                    true,
                    false,
                    false
            );
        }
        
        return new MBeanInfo(
                this.getClass().getName(),
                "Cache Graphics MBean",
                attrs,
                null, null, null
        ); 
    }

    /* (non-Javadoc)
     * @see javax.management.DynamicMBean#invoke(java.lang.String, java.lang.Object[], java.lang.String[])
     */
    public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException,
            ReflectionException {
        // Nothing todo - we are read-only
        return null;
    }

    /* (non-Javadoc)
     * @see javax.management.DynamicMBean#setAttribute(javax.management.Attribute)
     */
    public void setAttribute(
            Attribute attribute
    ) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
        // Nothing todo - we are read-only
    }

    /* (non-Javadoc)
     * @see javax.management.DynamicMBean#setAttributes(javax.management.AttributeList)
     */
    public AttributeList setAttributes(AttributeList attributes) {
        // Nothing todo - we are read-only
        return null;
    }

    public ObjectName getObjectName() {
        return objectName;
    }
    
    protected String generateBar() {
        return "|####_____|";
    }
}
