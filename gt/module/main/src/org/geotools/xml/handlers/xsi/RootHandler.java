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
import org.geotools.xml.schema.Schema;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotSupportedException;
import java.net.URI;


/**
 * RootHandler purpose.
 * 
 * <p>
 * This is intended to bootstrap the schema parsing
 * </p>
 *
 * @author dzwiers, Refractions Research, Inc. http://www.refractions.net
 * @author $Author:$ (last modification)
 * @version $Id$
 */
public class RootHandler extends XSIElementHandler {
    /** 'root'  */
    public final static String LOCALNAME = "root";
    
    private SchemaHandler schema;
    private URI uri;

    /*
     * should not be called
     */
    private RootHandler() {
    }

    /**
     * Creates a new RootHandler object.
     *
     * @param uri 
     */
    public RootHandler(URI uri) {
        this.uri = uri;
    }

    /**
     * 
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return LOCALNAME.hashCode() * ((uri == null) ? 1 : uri.hashCode());
    }

    /**
     * 
     * @see org.geotools.xml.XSIElementHandler#getHandler(java.lang.String, java.lang.String)
     */
    public XSIElementHandler getHandler(String namespaceURI, String localName)
        throws SAXException {
        if (SchemaHandler.LOCALNAME.equalsIgnoreCase(localName)
                && SchemaHandler.namespaceURI.equalsIgnoreCase(namespaceURI)) {
            schema = new SchemaHandler();

            return schema;
        }

        return null;
    }

    /**
     * 
     * @see org.geotools.xml.XSIElementHandler#startElement(java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(String namespaceURI, String localName,
        Attributes attr) throws SAXException {
        throw new SAXNotSupportedException(
            "Should never have elements at the root level");
    }

    /**
     * 
     * @see org.geotools.xml.XSIElementHandler#getLocalName()
     */
    public String getLocalName() {
        return LOCALNAME;
    }

    /**
     * 
     * <p>
     * intended to be called after the parse, this generates a Schema
     * object from the schema which was parsed in.
     * </p>
     *
     * @return
     * @throws SAXException
     */
    public Schema getSchema() throws SAXException {
        Schema s = schema.compress(uri);

        return s;
    }

    /**
     * 
     * @see org.geotools.xml.XSIElementHandler#getHandlerType()
     */
    public int getHandlerType() {
        return DEFAULT;
    }

    /**
     * 
     * @see org.geotools.xml.XSIElementHandler#endElement(java.lang.String, java.lang.String)
     */
    public void endElement(String namespaceURI, String localName)
        throws SAXException {
    }
}
