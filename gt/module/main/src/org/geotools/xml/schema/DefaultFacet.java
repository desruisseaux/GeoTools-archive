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
public class DefaultFacet implements Facet {
    private int type;
    private String value;

    private DefaultFacet() {
    }

    /**
     * Creates a new DefaultFacet object.
     *
     * @param type DOCUMENT ME!
     * @param value DOCUMENT ME!
     */
    public DefaultFacet(int type, String value) {
        this.type = type;
        this.value = value;
    }

    /**
     * @see org.geotools.xml.schema.Facet#getFacetType()
     */
    public int getFacetType() {
        return type;
    }

    /**
     * @see org.geotools.xml.schema.Facet#getValue()
     */
    public String getValue() {
        return value;
    }
}
