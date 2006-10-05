/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, GeoTools Project Managment Committee (PMC)
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
 */
package org.geotools.xml;

import java.io.Serializable;
import java.net.URI;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.OperationNotSupportedException;

import org.geotools.xml.schema.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotSupportedException;


/**
 * XSIElementHandler purpose.
 * 
 * <p>
 * This abstract class is intended to act as both a definition of a generic
 * handler.
 * </p>
 *
 * @author dzwiers, Refractions Research, Inc. http://www.refractions.net
 * @author $Author:$ (last modification)
 * @source $URL$
 * @version $Id$
 */
public abstract class XMLElementHandler implements Serializable {
    /**
     * the logger -- should be used for debugging (assuming there are bugs LOL)
     */
    protected final static Logger logger = Logger.getLogger(
            "net.refractions.xml.element");

    /**
     * Creates a new XSIElementHandler object. Intended to limit creation to
     * the sub-packages
     */
    protected XMLElementHandler() {
        // do nothing
    }

    /**
     * This method throws a SAXNotSupportedException if it is called and not
     * overwritten. When overridding this method, you should be careful to
     * understand that it may be called more than once per element. Therefore
     * it would be advisable to log the text and handle the text's
     * interpretation at a later time (
     *
     * @param text
     *
     * @throws SAXException
     * @throws SAXNotSupportedException
     *
     * @see endElement(String,String)).
     */
    public void characters(String text) throws SAXException {
        throw new SAXNotSupportedException("Should overide this method.");
    }

    /**
     * handles SAX end Element events. This matches the end of the element
     * declaration in the document ... and responds to the event generated by
     * the SAX parser. This is an opportunity to complete some
     * post-processing.
     *
     * @param namespaceURI
     * @param localName
     * @param hints DOCUMENT ME!
     *
     * @throws SAXException
     * @throws OperationNotSupportedException
     *
     * @see SchemaContentHandler#endElement
     */
    public abstract void endElement(URI namespaceURI, String localName,
        Map hints) throws SAXException, OperationNotSupportedException;

    /**
     * handles SAX start Element events. This matches the start of the element
     * declaration in the document ... and responds to the event generated by
     * the SAX parser. This is an opportunity to complete some pre-processing.
     *
     * @param namespaceURI
     * @param localName
     * @param attr
     *
     * @throws SAXException
     *
     * @see SchemaContentHandler#startElement
     */
    public abstract void startElement(URI namespaceURI, String localName,
        Attributes attr) throws SAXException;

    /**
     * This will find an appropriate XMLElementHandler for the specified child
     * if appropriate. This method may return or throw an exception, depending
     * on the severity, if an error occurs. This method should be used to
     * complete a SAX parse of a document for which the Schema is known, and
     * parsed.
     *
     * @param namespaceURI
     * @param localName
     * @param hints DOCUMENT ME!
     *
     * @return XMLElementHandler, or null
     *
     * @throws SAXException
     */
    public abstract XMLElementHandler getHandler(URI namespaceURI,
        String localName, Map hints) throws SAXException;

    /**
     * This method will get the value of the element depending on it's type.
     *
     * @return Object (may be null)
     *
     * @throws SAXException
     *
     * @see Type#getValue
     */
    public abstract Object getValue() throws SAXException;

    /**
     * This returns the name of the element being represented by this handler.
     * This name matches the name specified in the Schema.
     *
     * @return The Name (may not be null)
     */
    public abstract String getName();

    /**
     * This returns the Element specified.
     *
     * @return Element (may not be null)
     */
    public abstract Element getElement();

    /**
     * <p>
     * Sets the logger level for all XMLElementHandlers.
     * </p>
     *
     * @param l
     */
    public static void setLogLevel(Level l) {
        logger.setLevel(l);
    }
}
