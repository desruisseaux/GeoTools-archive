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
package org.geotools.xml.handlers.xsi;

import org.geotools.xml.XSIElementHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;


/**
 * FacetHandler purpose.
 * 
 * <p>
 * Abstract class representing common Facet abilites + attributes.
 * </p>
 *
 * @author dzwiers, Refractions Research, Inc. http://www.refractions.net
 * @author $Author:$ (last modification)
 * @version $Id$
 */
public abstract class FacetHandler extends XSIElementHandler {
    /** ENUMERATION  */
    public static final int ENUMERATION = 1;

    /** FRACTIONDIGITS  */
    public static final int FRACTIONDIGITS = 2;

    /** LENGTH  */
    public static final int LENGTH = 4;

    /** MAXEXCLUSIVE  */
    public static final int MAXEXCLUSIVE = 8;

    /** MAXINCLUSIVE  */
    public static final int MAXINCLUSIVE = 16;

    /** MAXLENGTH  */
    public static final int MAXLENGTH = 32;

    /** MINEXCLUSIVE  */
    public static final int MINEXCLUSIVE = 64;

    /** MININCLUSIVE  */
    public static final int MININCLUSIVE = 128;

    /** MINLENGTH  */
    public static final int MINLENGTH = 264;

    /** PATTERN  */
    public static final int PATTERN = 512;

    /** TOTALDIGITS  */
    public static final int TOTALDIGITS = 1024;
    
    private String value;

    /**
     * 
     * @see org.geotools.xml.XSIElementHandler#endElement(java.lang.String, java.lang.String)
     */
    public void endElement(String namespaceURI, String localName)
        throws SAXException {
    }

    /**
     * 
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return getHandlerType() * ((value == null)
        ? 1 : value.hashCode());
    }

    /**
     * 
     * <p>
     * Return the int mask for the facet type.
     * </p>
     *
     * @return
     */
    public abstract int getType();

    /**
     * 
     * @see org.geotools.xml.XSIElementHandler#getHandlerType()
     */
    public int getHandlerType() {
        return FACET;
    }

    /**
     * 
     * @see org.geotools.xml.XSIElementHandler#getHandler(java.lang.String, java.lang.String)
     */
    public XSIElementHandler getHandler(String namespaceURI, String localName)
        throws SAXException {
        throw new SAXNotRecognizedException(
            "Facets are not allowed to have sub-elements");
    }

    /**
     * 
     * @see org.geotools.xml.XSIElementHandler#startElement(java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(String namespaceURI, String localName,
        Attributes atts) throws SAXException {
//        id = atts.getValue("", "id");
//
//        if (id == null) {
//            id = atts.getValue(namespaceURI, "id");
//        }

        value = atts.getValue("", "value");

        if (value == null) {
            value = atts.getValue(namespaceURI, "value");
        }
//
//        String fixed = atts.getValue("", "fixed");
//
//        if (fixed == null) {
//            fixed = atts.getValue(namespaceURI, "fixed");
//        }
//
//        if (!((fixed == null) || "".equalsIgnoreCase(fixed))) {
//            this.fixed = Boolean.getBoolean(fixed);
//        }
    }

    /**
     * 
     * <p>
     * Returns the Facet Value
     * </p>
     *
     * @return
     */
    public String getValue() {
        return value;
    }
}
