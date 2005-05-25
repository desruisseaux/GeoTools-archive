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
package org.geotools.xml.schema.impl;

import org.geotools.xml.schema.All;
import org.geotools.xml.schema.Element;

public class AllGT extends All {

    /*
    private Element[] elements;
    private String id;
    private int max;
    private int min;
    */
    public AllGT( Element[] elements ) {
        super( null, 1, 1, elements );
        /*
        this.elements = elements;        
        max = min = 1;
        */
    }

    public AllGT( String id, Element[] elements, int min, int max ) {
        super( id, min, max, elements );
        /*
        this.id = id;
        this.elements = elements;
        this.min = min;
        this.max = max;
        */
    }
    
    /* (non-Javadoc)
     * @see org.geotools.xml.schema.All#getElements()
     *
    public Element[] getElements() {
        return super.getElements();
    }*/

    /* (non-Javadoc)
     * @see org.geotools.xml.schema.All#getId()
     *
    public String getId() {
        return id;
    }*/

    /* (non-Javadoc)
     * @see org.geotools.xml.schema.ElementGrouping#getMaxOccurs()
     *
    public int getMaxOccurs() {
        return max;
    }*/

    /* (non-Javadoc)
     * @see org.geotools.xml.schema.ElementGrouping#getMinOccurs()
     *
    public int getMinOccurs() {
        return min;
    }*/

    /* (non-Javadoc)
     * @see org.geotools.xml.schema.ElementGrouping#getGrouping()
     *
    public int getGrouping() {
        return ALL;
    }*/

    /* (non-Javadoc)
     * @see org.geotools.xml.schema.ElementGrouping#findChildElement(java.lang.String)
     *
    public Element findChildElement( String name ) {
        if (elements != null) {
            for (int i = 0; i < elements.length; i++) {
                Element e = elements[i].findChildElement(name);

                if (e != null) {
                    return e;
                }
            }
        }

        return null;
    }*/

}
