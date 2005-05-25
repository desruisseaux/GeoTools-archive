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

import java.net.URI;


/**
 * <p>
 * Instances of this interface are intended to represent the 'any' construct in
 * an XML Schema.
 * </p>
 * @
 *
 * @author dzwiers www.refractions.net
 */
public abstract class Any extends com.vividsolutions.xdo.xsi.Any implements ElementGrouping {
    
    public Any( URI arg0, String arg1, int arg2, int arg3 ) {
        super(arg0, arg1, arg2, arg3);
    }

    public static final URI ALL = null;
    /**
     * <p>
     * Returns the element declaration's id for this schema element.
     * </p>
     *
     * @return
     */
    public String getId(){
        return super.getId();
    }

    /**
     * @see org.geotools.xml.xsi.ElementGrouping#getMaxOccurs()
     */
    public int getMaxOccurs(){
        return super.getMaxOccurs();
    }

    /**
     * @see org.geotools.xml.xsi.ElementGrouping#getMinOccurs()
     */
    public int getMinOccurs(){
        return super.getMinOccurs();
    }

    /**
     * <p>
     * Returns the namespace attribute of the 'any' contruct that an instance
     * of this interface is representing within an XML Schema.
     * </p>
     *
     * @return
     */
    public URI getNamespace(){
        return super.getNamespace();
    }
    
    public Element findChildElement( String name ) {
        return null; // not convient to implement this convient method right now
    }
}
