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
 * Instances of this interface are intended to represent XML Schema Elements.
 * </p>
 * @
 *
 * @author dzwiers www.refractions.net
 */
public abstract class Element extends com.vividsolutions.xdo.xsi.Element /** implements ElementGrouping */ {
    /**
     * <p>
     * Returns True when the instance of this XML Schema Element is abstract,
     * false otherwise
     * </p>
     *
     * @return
     */
    public boolean isAbstract(){
        return super.isAbstract();
    }

    /**
     * @see Schema#getBlockDefault()
     */
    public int getBlock(){
        return super.getBlock();
    }

    /**
     * <p>
     * This returns the default value for the Element as a String
     * </p>
     *
     * @return
     */
    public String getDefault(){
        return super.getDefault();
    }

    /**
     * @see Schema#getFinalDefault()
     */
    public int getFinal(){
        return super.getFinal();
    }

    /**
     * <p>
     * This returns the fixed value for the Element as a String
     * </p>
     *
     * @return
     */
    public String getFixed(){
        return super.getFixed();
    }

    /**
     * @see Schema#isElementFormDefault()
     */
    public boolean isForm(){
        return super.isForm();
    }

    /**
     * <p>
     * The Schema ID for this element definition.
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
     * Returns the element declaration's name in the Schema document, and
     * element name in the instance document.
     * </p>
     *
     * @return
     */
    public String getName(){
        return super.getName();
    }

    /**
     * DOCUMENT ME!
     *
     * @return
     */
    public URI getNamespace(){
        return super.getNamespace();
    }

    /**
     * <p>
     * Returns true when the element is nillable, false otherwise
     * </p>
     *
     * @return
     */
    public boolean isNillable(){
        return super.isNillable();
    }

    /**
     * <p>
     * This returns a reference to an element representing this element's
     * substitution group. This is of particular importance when resolving an
     * instance document's value.
     * </p>
     *
     * @return
     */
    public com.vividsolutions.xdo.xsi.Element getSubstitutionGroup(){
        return super.getSubstitutionGroup();
    }

    /**
     * <p>
     * Returns the declared type for this Element in the given Schema.
     * </p>
     *
     * @return
     *
     * @see Type
     */
    public com.vividsolutions.xdo.xsi.Type getType(){ // simpleType or complexType
        return super.getType();
    }
}
