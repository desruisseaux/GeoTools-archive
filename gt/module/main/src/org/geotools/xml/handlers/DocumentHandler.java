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
package org.geotools.xml.handlers;

import java.util.Map;

import org.geotools.xml.XMLElementHandler;
import org.geotools.xml.schema.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;


/**
 * <p>
 * Represents the start of an XML document ... serves up elements wrapped in
 * handlers for a specified schema.
 * </p>
 * @
 *
 * @author dzwiers www.refractions.net
 */
public class DocumentHandler extends XMLElementHandler {
    private XMLElementHandler xeh = null;
    private ElementHandlerFactory ehf;

    /**
     * Creates a new DocumentHandler object.
     *
     * @param ehf ElementHandlerFactory
     */
    public DocumentHandler(ElementHandlerFactory ehf) {
        this.ehf = ehf;
    }

    /**
     * 
     * @see org.geotools.xml.XMLElementHandler#getElement()
     */
    public Element getElement() {
        return null;
    }

    /**
     * 
     * @see org.geotools.xml.XMLElementHandler#endElement(java.lang.String, java.lang.String)
     */
    public void endElement(String namespaceURI, String localName, Map hints)
        throws SAXException {
    }

    /**
     * 
     * @see org.geotools.xml.XMLElementHandler#getHandler(java.lang.String, java.lang.String)
     */
    public XMLElementHandler getHandler(String namespaceURI, String localName)
        throws SAXException {
        if (xeh != null) {
            throw new SAXNotRecognizedException(
                "XML Documents may only have one top-level element");
        }

        xeh = ehf.createElementHandler(namespaceURI, localName);

        return xeh;
    }

    /**
     * 
     * @see org.geotools.xml.XMLElementHandler#startElement(java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(String namespaceURI, String localName,
        Attributes attr) throws SAXException {
    }

    /**
     * 
     * @see org.geotools.xml.XMLElementHandler#getValue()
     */
    public Object getValue() throws SAXException {
        return xeh.getValue();
    }

    /**
     * 
     * @see org.geotools.xml.XMLElementHandler#getName()
     */
    public String getName() {
        return "";
    }
}
