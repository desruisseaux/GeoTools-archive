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

import java.net.URI;
import java.net.URISyntaxException;

import org.geotools.xml.XSIElementHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


/**
 * ImportHandler purpose.
 * 
 * <p>
 * Represents an 'import' element.
 * </p>
 *
 * @author dzwiers, Refractions Research, Inc. http://www.refractions.net
 * @author $Author:$ (last modification)
 * @version $Id$
 */
public class ImportHandler extends XSIElementHandler {
    /** 'import' */
    public final static String LOCALNAME = "import";
    private static int offset = 0;

    //    private String id;
    private URI namespace;
    private String schemaLocation;
    private int hashCodeOffset = getOffset();

    /*
     * helper method for hashCode()
     */
    private static int getOffset() {
        return offset++;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return (LOCALNAME.hashCode() * ((schemaLocation == null) ? 1
                                                                 : schemaLocation
        .hashCode())) + hashCodeOffset;
    }

    /**
     * @see org.geotools.xml.XSIElementHandler#getHandler(java.lang.String,
     *      java.lang.String)
     */
    public XSIElementHandler getHandler(String namespaceURI, String localName)
        throws SAXException {
        return null;
    }

    /**
     * @see org.geotools.xml.XSIElementHandler#startElement(java.lang.String,
     *      java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(String namespaceURI, String localName,
        Attributes atts) throws SAXException {
        //        id = atts.getValue("", "id");
        //
        //        if (id == null) {
        //            id = atts.getValue(namespaceURI, "id");
        //        }
        schemaLocation = atts.getValue("", "schemaLocation");

        if (schemaLocation == null) {
            schemaLocation = atts.getValue(namespaceURI, "schemaLocation");
        }

        String namespace = atts.getValue("", "namespace");

        if (namespace == null) {
            namespace = atts.getValue(namespaceURI, "namespace");
        }
        try {
            this.namespace = new URI(namespace);
        } catch (URISyntaxException e) {
            logger.warning(e.toString());
            throw new SAXException(e);
        }

        if (namespaceURI.equalsIgnoreCase(namespace)) {
            throw new SAXException(
                "You may not import a namespace with the same name as the current namespace");
        }
    }

    /**
     * @see org.geotools.xml.XSIElementHandler#getLocalName()
     */
    public String getLocalName() {
        return LOCALNAME;
    }

    /**
     * <p>
     * gets the namespace attribute
     * </p>
     *
     * @return
     */
    public URI getNamespace() {
        return namespace;
    }

    /**
     * <p>
     * gets the schemaLocation attribute
     * </p>
     *
     * @return
     */
    public String getSchemaLocation() {
        return schemaLocation;
    }

    /**
     * @see org.geotools.xml.XSIElementHandler#getHandlerType()
     */
    public int getHandlerType() {
        return DEFAULT;
    }

    /**
     * @see org.geotools.xml.XSIElementHandler#endElement(java.lang.String,
     *      java.lang.String)
     */
    public void endElement(String namespaceURI, String localName)
        throws SAXException {
    }
}
