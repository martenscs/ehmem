package com.danga.jmemcached;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author <A href="mailto:abashev at gmail dot com">Alexey Abashev</A>
 * @version $Id$
 */
public class PoolConfigurationWrapper {
    private Document document;
    private String prefix;

    public PoolConfigurationWrapper(Document document, String poolName) {
        this.document = document;
        this.prefix = "/memcached/pool[@name=\"" + poolName + "\"]/";
    }

    public String[] getServers() {
        NodeList nodes = (NodeList) evalute("servers/server/@host", XPathConstants.NODESET);

        if (nodes != null) {
            List<String> hosts = new ArrayList<String>(nodes.getLength());

            for (int i = 0; i < nodes.getLength(); i++) {
                hosts.add(nodes.item(i).getNodeValue());
            }

            return hosts.toArray(new String[nodes.getLength()]);
        }

        return new String[0];
    }

    public Integer[] getWeights() {
        NodeList nodes = (NodeList) evalute("servers/server/@weights", XPathConstants.NODESET);

        if (nodes != null) {
            List<String> weights = new ArrayList<String>(nodes.getLength());

            for (int i = 0; i < nodes.getLength(); i++) {
                weights.add(nodes.item(i).getNodeValue());
            }

            return weights.toArray(new Integer[nodes.getLength()]);
        }

        return new Integer[0];
    }

    public int getInitConnections() {
        String p = getParameter("initConnections");

        return Integer.parseInt(p);
    }

    public int getMaxConnections() {
        String p = getParameter("maxConnections");

        return Integer.parseInt(p);
    }

    public int getMinConnections() {
        String p = getParameter("minConnections");

        return Integer.parseInt(p);
    }

    public long getMaxIdle() {
        String p = getParameter("maxIdle");

        return Long.parseLong(p);
    }

    public long getMaintSleep() {
        String p = getParameter("maintSleep");

        return Long.parseLong(p);
    }

    public boolean getNagle() {
        String p = getParameter("nagle");

        return Boolean.parseBoolean(p);
    }

    public int getSocketTO() {
        String p = getParameter("socketTO");

        return Integer.parseInt(p);
    }

    public int getSocketConnectTO() {
        String p = getParameter("socketConnectTO");

        return Integer.parseInt(p);
    }

    /**
     * Laoding configuration parameter as string.
     * @param name
     * @return
     */
    private String getParameter(String name) {
        Node node = (Node) evalute("parameters/parameter[@name=\"" + name + "\"]/@value", XPathConstants.NODE);

        if (node != null) {
            return node.getNodeValue();
        } else {
            return null;
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
