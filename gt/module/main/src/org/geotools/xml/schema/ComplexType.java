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

import java.util.Map;

/**
 * <p>
 * This interface is intended to represent an XML Schema complexType. This
 * interface extends the generic XML schema type interface to represent datum
 * within nested elements.
 * </p>
 * @
 *
 * @author dzwiers www.refractions.net
 */
public interface ComplexType extends Type {
    
    /**
     * Returns true when the complexType should be considered abstract, as 
     * defined by the XML schema of which this complex type definition is a 
     * part.
     *
     * @return 
     */
    public boolean isAbstract();

    /**
     * This methos represents the potential 'anyAttribute' declaration's 
     * namespace attribute which may occur within a complex type definition.
     *
     * @return 
     */
    public String getAnyAttributeNameSpace();

    /**
     * The set of attributes required by this complex type declaration. As 
     * per the xml schema definition, there is not an implied order to the 
     * attributes. For performance reasons an implementor may wich to order 
     * the attributes from most common to least commonly used attributes.
     *
     * @return 
     */
    public Attribute[] getAttributes();

    /**
     * Specifies a mask which denotes which substitution mechanisms may be 
     * used for this complex type definition. 
     *
     * @see Schema#EXTENSION
     * @see Schema#RESTRICTION
     * @see Schema#ALL
     *
     * @return 
     */
    public int getBlock();

    /**
     * Returns the child element representing the structure of nested 
     * child nodes (if any are allowed).
     * 
     * @see ElementGrouping
     *
     * @return
     */
    public ElementGrouping getChild();

    /**
     * Convinience method used to search this type's children for the 
     * requested element by localName.
     *
     * @param name the element's localName to search for.
     *
     * @return 
     */
    public Element findChildElement(String name);

    /**
     * Specifies a mask which denotes which substitution mechanisms prohibited 
     * for use by child definitions of this complex type. 
     *
     * @see Schema#EXTENSION
     * @see Schema#RESTRICTION
     * @see Schema#ALL
     *
     * @return 
     */
    public int getFinal();

    /**
     * Returns the xml schema id of this complexType if one 
     * exists, null otherwise.
     *
     * @return 
     */
    public String getId();

    /**
     * Returns true if this complexType allows mixed content (Child elements 
     * and a String value). 
     *
     * @return 
     */
    public boolean isMixed();

    /**
     * This method is used to publish whether this complexType is at the root 
     * of an inheritance tree, or a leaf within an inheritance tree. This method
     * should return true when the complexType is not a root of an inheritance 
     * tree.
     *
     * @return 
     */
    public boolean isDerived();
    
    /**
     * This method is a directive to the parser whether to keep the data around in 
     * memory for post processing. Generally this should return True, except when 
     * streaming.
     * 
     * @return True, except when streaming the element.
     */
    public boolean cache(Element element, Map hints);
}
