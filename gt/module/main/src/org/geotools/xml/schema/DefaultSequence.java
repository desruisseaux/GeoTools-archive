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
package org.geotools.xml.schema;

/**
 * <p>
 * DOCUMENT ME!
 * </p>
 *
 * @author dzwiers
 */
public class DefaultSequence implements Sequence {
    private ElementGrouping[] children;
    private String id;
    private int min;
    private int max;

    private DefaultSequence() {
    }

    public DefaultSequence(ElementGrouping[] children) {
        this.children = children;
        min = max = 1;
    }

    public DefaultSequence(String id, ElementGrouping[] children, int min,
        int max) {
        this.children = children;
        this.min = min;
        this.max = max;
        this.id = id;
    }

    /**
     * @see org.geotools.xml.schema.Sequence#getChildren()
     */
    public ElementGrouping[] getChildren() {
        return children;
    }

    /**
     * @see org.geotools.xml.schema.Sequence#getId()
     */
    public String getId() {
        return id;
    }

    /**
     * @see org.geotools.xml.schema.ElementGrouping#getMaxOccurs()
     */
    public int getMaxOccurs() {
        return max;
    }

    /**
     * @see org.geotools.xml.schema.ElementGrouping#getMinOccurs()
     */
    public int getMinOccurs() {
        return min;
    }

    /**
     * @see org.geotools.xml.schema.ElementGrouping#getGrouping()
     */
    public int getGrouping() {
        return SEQUENCE;
    }

    /**
     * @see org.geotools.xml.schema.ElementGrouping#findChildElement(java.lang.String)
     */
    public Element findChildElement(String name) {
        if (children != null) {
            for (int i = 0; i < children.length; i++) {
                Element e = children[i].findChildElement(name);

                if (e != null) {
                    return e;
                }
            }
        }

        return null;
    }
}
