/*
 * Geotools2 - OpenSource mapping toolkit http://geotools.org (C) 2002, Geotools
 * Project Managment Committee (PMC) This library is free software; you can
 * redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; version 2.1 of
 * the License. This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details.
 */
package org.geotools.xml.schema;

import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotSupportedException;

/**
 * <p>
 * This is a convinience interface to help speed up the code, allowing for any
 * type definition to handled in a consistent manner, independant of whether
 * it's nested or not.
 * </p>
 * 
 * @see SimpleType
 * @see ComplexType
 * @author dzwiers www.refractions.net
 */
public interface Type {

    /**
     * <p>
     * This method is intended to receive the child elements in the form of
     * ElementValues (@see ElementValue). Recall that this is a pairing
     * containing a reference to the type and the actual value (do not call
     * .getValue on the types). This should return the real value (interpreted
     * value) for this element based on it's children. Remember, this is a XML
     * is a tree structure, so if you don't include some children, they are gone
     * forever (so be careful when ignoring data).
     * </p><p>
     * A SAXNotSupportedException should be thrown when the child's
     * [inherited, default implementation of
     * getValue(ElementValue[],Attributes)] method should be used.
     * </p>
     * 
     * @throws SAXNotSupportedException
     * 
     * @see ElementValue
     * 
     * @return Object
     */
    public Object getValue(Element element, ElementValue[] value,
            Attributes attrs, Map hints) throws SAXException, SAXNotSupportedException;

    /**
     * <p>
     * This returns the XML Schema declaration name of this type (both complex
     * and simple types have names ...)
     * </p>
     * 
     * @return
     */
    public String getName();

    /**
     * <p>
     * This is used for validation of an XML document, and represents the
     * targetNamespace of that this type resides in.
     * </p>
     * 
     * @return
     */
    public String getNamespace();

    /**
     * <p>
     * This is used to represent the heirarchy represented within an xml schema
     * document(s). This is particularily useful, as the parent will have the
     * first attempt to create a real (non Object[]) value of the element. For
     * more information see getValue.
     * </p>
     * 
     * @see Type#getValue(Element, ElementValue[], Attributes)
     * @return
     */
    public Type getParent();

    /**
     * <p>
     * This will return the intended Java Class for this element type. For
     * generic complex types this will be an object array. SimpleTypes will
     * match as they are parsed, and custom extensions will also return the
     * appropriate class value.
     * </p>
     * 
     * @return
     */
    public Class getInstanceType();
}