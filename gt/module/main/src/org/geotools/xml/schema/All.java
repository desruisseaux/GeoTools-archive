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

import com.vividsolutions.xdo.xsi.Element;

/**
 * <p>
 * Instances of this interface are intended to represent the 'all' construct in
 * an XML Schema.
 * </p>
 *
 * @author dzwiers www.refractions.net
 *
 * @see Element
 */
public class All extends com.vividsolutions.xdo.xsi.All implements ElementGrouping {
    
    /** TODO: wtf */
    public All( String arg0, int arg1, int arg2, Element[] arg3 ) {
        super(arg0, arg1, arg2, arg3);
    }

    /**
     * <p>
     * The list of elements represented within this 'all' declaration. We
     * should not that as per the Schema definition of the 'all' declaration,
     * the return order in the array should not have an effect on the instance
     * document.
     * </p>
     *
     * @return
     */
    public Element[] getElements(){
        return super.getElements();
    }

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
     * Geotools ElementGrouping method.
     */
    public org.geotools.xml.schema.Element findChildElement( String name ) {
        return null; // inconvient to make convience method at this time
    }   
}
