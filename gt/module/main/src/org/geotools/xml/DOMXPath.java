/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package org.geotools.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.util.Iterator;
import java.util.List;


/**
 * XPath implementation that Works with org.w3c.dom.Node objects
 *
 * @author Jesse Eichar
 * @version $Revision: 1.9 $
 */
public class DOMXPath extends XPath {
    /**
     * Creates a new DOMXPath object.
     *
     * @param path string form of an XPath
     */
    public DOMXPath(String path) {
        super(path);
    }

    /**
     * Returns the value of the first TEXT_NODE.  The whitespace is deleted
     * from the value
     *
     * @param path The list of the nodes from the root (Not necessarily doc
     *        node) and the identified node.  Only the last node is actually
     *        used in the calculation.
     *
     * @return the value of the first TEXT_NODE.  The whitespace is deleted
     *         from the value
     */
    protected Object solve(List path) {
        Node node = (Node) path.get(path.size() - 1);

        for (node = node.getFirstChild(); node != null;
                node = node.getNextSibling())
            if (node.getNodeType() == Node.TEXT_NODE) {
                String value = (String) node.getNodeValue();
                value = value.replaceAll("\n+", " ");
                value = value.trim();

                return value;
            }

        return null;
    }

    /**
     * returns an iterator that iterates through all the o's children that are
     * Element objects
     *
     * @param o A Node element
     *
     * @return an iterator that iterates through all the o's children that are
     *         Element objects
     */
    protected Iterator getChildren(Object o) {
        final Node n = (Node) o;

        return new ElementIterator(n);
    }

    /**
     * Returns the node name of the node.  Uses getNodeName()
     *
     * @param o A Node object
     *
     * @return the node name of the node.  Uses getNodeName()
     */
    protected String getNodeName(Object o) {
        Node node = (Node) o;

        return node.getNodeName();
    }

    /**
     * @see org.geotools.xml.XPath#isLegalNode(java.lang.Object)
     */
    protected boolean isLegalNode(Object o, int operation, boolean isList) {
        if (o instanceof Node) {
            return true;
        }

        return false;
    }

    private class ElementIterator implements Iterator {
        Node next;
        NodeList children;
        int index = 0;

        ElementIterator(Node n) {
            if (n instanceof Document) {
                next = ((Document) n).getDocumentElement();
            } else {
                children = n.getChildNodes();
                next = findNext();
            }
        }

        public boolean hasNext() {
            return (next == null) ? false : true;
        }

        public Object next() {
            Node tmp = next;
            next = findNext();

            return tmp;
        }

        private Node findNext() {
            if (children == null) {
                return null;
            }

            for (int i = index + 1; i < children.getLength(); i++) {
                if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    index = i;

                    return children.item(i);
                }
            }

            return null;
        }

        public void remove() {
        }
    }
}
