package net.sf.ehcache.management;

import java.util.Arrays;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author <A href="mailto:abashev at gmail dot com">Alexey Abashev</A>
 * @version $Id$
 */
public class CacheGraphics implements DynamicMBean {
    private static final char STOP_CHAR = '|';
    private static final char FILL_CHAR = '#';
    private static final char EMPTY_CHAR = '_';
    private static final int BAR_LENGTH = 20;

    private static final String EMPTY = "empty";
    private static final String MAX_NOT_SPECIFIED = "Max is not specified";

    private final Log log = LogFactory.getLog(CacheGraphics.class);

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
        Cache cache = cacheManager.getCache(attribute);
        
        if (cache == null) {
            throw new AttributeNotFoundException();
        }
        
        return generateBar(
                cache.getStatistics().getObjectCount(),
                cache.getCacheConfiguration().getMaxElementsInMemory()
        );
    }

    /* (non-Javadoc)
     * @see javax.management.DynamicMBean#getAttributes(java.lang.String[])
     */
    public AttributeList getAttributes(String[] attributes) {
        AttributeList list = new AttributeList();
        
        for (String name : attributes) {
            try {
                list.add(new Attribute(name, getAttribute(name)));
            } catch (AttributeNotFoundException e) {
                log.error("Unable to load attribute value for \"" + name + "\"", e);
            } catch (MBeanException e) {
                log.error("Unable to load attribute value for \"" + name + "\"", e);
            } catch (ReflectionException e) {
                log.error("Unable to load attribute value for \"" + name + "\"", e);
            }
        }
    
        return list;    
    }

    /* (non-Javadoc)
     * @see javax.management.DynamicMBean#getMBeanInfo()
     */
    
    public MBeanInfo getMBeanInfo() {
        String[] caches = cacheManager.getCacheNames();
        
        Arrays.sort(caches);
        
        MBeanAttributeInfo[] attrs = new MBeanAttributeInfo[caches.length];
        
        for (int i = 0; i < caches.length; i++) {
            attrs[i] = new MBeanAttributeInfo(
                    caches[i],
                    "java.lang.String",
                    "Property " + caches[i],
                    true, false, false
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
    
    protected String generateBar(long amount, long max) {
        if (amount == 0) {
            return EMPTY;
        }

        if (max == 0) {
            return MAX_NOT_SPECIFIED;
        }

        int len = (int) ((amount * BAR_LENGTH) / max);  
        StringBuilder sb = new StringBuilder();
        
        sb.append(STOP_CHAR);
        
        for (int i = 0; i < BAR_LENGTH; i++) {
            sb.append((i < len) ? FILL_CHAR : EMPTY_CHAR);
        }
        
        sb.append(STOP_CHAR);
        
        // Calculate percent
        sb.append(String.format(" %3d%%", (int) ((amount * 100) / max)));
        
        return sb.toString();
    }
}
