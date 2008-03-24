package com.danga.jmemcached;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author <A href="mailto:abashev at gmail dot com">Alexey Abashev</A>
 * @version $Id$
 */
public class PoolConfigurationWrapper {
    public static final String INIT_CONNECTIONS = "initConnections";
    public static final String MAX_CONNECTIONS = "maxConnections";
    public static final String MIN_CONNECTIONS = "minConnections";
    public static final String MAX_IDLE = "maxIdle";

    public static final String NAGLE = "nagle";
    public static final String MAINT_SLEEP = "maintSleep";

    public static final String SOCKET_TIMEOUT = "socketTO";
    public static final String SOCKET_CONNECT_TIMEOUT = "socketConnectTO";

    private Document document;
    private String prefix;

    private Map<String, Integer> servers;
    
    public PoolConfigurationWrapper(Document document, String poolName) {
        this.document = document;
        this.prefix = "/memcached/pool[@name=\"" + poolName + "\"]/";
    }

    public synchronized String[] getServers() {
        if (servers == null) {
            loadServerList();
        }
        
        Set<String> keySet = servers.keySet();
        
        return keySet.toArray(new String[keySet.size()]);
    }

    public synchronized Integer[] getWeights() {
        if (servers == null) {
            loadServerList();
        }

        Set<String> keySet = servers.keySet();
        Integer[] weights = new Integer[keySet.size()];
        
        int i = 0;
        for (String key : keySet) {
            weights[i++] = servers.get(key); 
        }
        
        return weights;
    }

    public <T> T getParameter(String parameter, Class<T> type) {
        Node node = (Node) evalute("parameters/parameter[@name=\"" + parameter + "\"]/@value", XPathConstants.NODE);

        if (node != null) {
            if (Long.class.isAssignableFrom(type)) {
                return type.cast(Long.parseLong(node.getNodeValue()));
            } else if (Integer.class.isAssignableFrom(type)) {
                return type.cast(Integer.parseInt(node.getNodeValue()));
            } else if (String.class.isAssignableFrom(type)) {
                return type.cast(node.getNodeValue());
            } else if (Boolean.class.isAssignableFrom(type)) {
                return type.cast(Boolean.parseBoolean(node.getNodeValue()));
            }
        }

        return null;
    }

    public boolean hasParameter(String parameter) {
        Node node = (Node) evalute("parameters/parameter[@name=\"" + parameter + "\"]/@value", XPathConstants.NODE);

        return (node != null);
    }

    protected void loadServerList() {
        NodeList nodes = (NodeList) evalute("servers/server", XPathConstants.NODESET);

        if (nodes != null) {
            SortedMap<String, Integer> map = new TreeMap<String, Integer>();
            
            for (int i = 0; i < nodes.getLength(); i++) {
                String host = null;
                
                NamedNodeMap attributes = nodes.item(i).getAttributes();
                
                Node hostAttr = attributes.getNamedItem("host");
                
                if (hostAttr == null) {
                    throw new IllegalStateException("Unable to find host in <" + nodes.item(i).getNodeName() + " " + attributes + "/>");
                } else {
                    host = hostAttr.getNodeValue();
                }
                
                Node weightAttr = attributes.getNamedItem("weight");
                Integer weight = (weightAttr != null) ? new Integer(weightAttr.getNodeValue()) : 1;

                map.put(host, weight);
            }
            
            servers = Collections.unmodifiableSortedMap(map);
        }
    }
    
    private Object evalute(String path, QName resultType) {
        // TODO Fast implementation - must replace to caching xpaths
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();

        try {
            return xpath.evaluate(prefix + path, document, resultType);
        } catch (XPathExpressionException e) {
            throw new RuntimeException("Unable to evalute xpath [" + path + "]", e);
        }
    }
}
