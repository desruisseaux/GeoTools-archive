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
 * Instances of this interface are intended to represent the 'any' construct in
 * an XML Schema.
 * </p>
 * @
 *
 * @author dzwiers www.refractions.net
 */
public interface Any extends ElementGrouping {
    /**
     * <p>
     * Returns the element declaration's id for this schema element.
     * </p>
     *
     * @return
     */
    public String getId();

    /**
     * @see org.geotools.xml.xsi.ElementGrouping#getMaxOccurs()
     */
    public int getMaxOccurs();

    /**
     * @see org.geotools.xml.xsi.ElementGrouping#getMinOccurs()
     */
    public int getMinOccurs();

    /**
     * <p>
     * Returns the namespace attribute of the 'any' contruct that an instance
     * of this interface is representing within an XML Schema.
     * </p>
     *
     * @return
     */
    public String getNamespace();
}
