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
package org.geotools.xml;

import org.xml.sax.SAXException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.logging.Level;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


/**
 * SchemaFactory purpose.
 * 
 * <p>
 * This is the main entry point into the XSI parsing routines.
 * </p>
 * 
 * <p>
 * Example Use:
 * <pre><code>
 * 
 *  
 *    
 *     Object x = DocumentFactory.getInstance(new URI(&quot;MyInstanceDocumentURI&quot;);
 *     
 *   
 *  
 * </code></pre>
 * </p>
 *
 * @author dzwiers, Refractions Research, Inc. http://www.refractions.net
 * @author $Author:$ (last modification)
 * @version $Id$
 */
public class DocumentFactory {

	/**
	 * When this hint is contained and set to Boolean.FALSE, element ordering will not 
	 * be validated. This key may also affect data validation within the parse routines. 
	 * The inherent safety of the resulting objects is weekend by turning this param to false.
	 */
    public static final String VALIDATION_HINT = "DocumentFactory_VALIDATION_HINT";
    
    /**
     * <p>
     * calls getInstance(URI,Level) with Level.WARNING
     * </p>
     *
     * @param desiredDocument
     * @param hints May be null.
     *
     * @return Object
     *
     * @throws SAXException
     *
     * @see DocumentFactory#getInstance(URI, Map, Level)
     */
    public static Object getInstance(URI desiredDocument, Map hints)
        throws SAXException {
        return getInstance(desiredDocument, hints, Level.WARNING);
    }

    /**
     * <p>
     * Parses the instance data provided. This method assumes that the XML
     * document is fully described using XML Schemas. Failure to be fully
     * described as Schemas will result in errors, as opposed to a vid parse.
     * </p>
     *
     * @param desiredDocument
     * @param hints May be null.
     * @param level
     *
     * @return Object
     *
     * @throws SAXException
     */
    public static Object getInstance(URI desiredDocument, Map hints, Level level)
        throws SAXException {
        SAXParser parser = getParser();

        XMLSAXHandler xmlContentHandler = new XMLSAXHandler(desiredDocument,
                hints);
        XMLSAXHandler.setLogLevel(level);

        try {
            parser.parse(desiredDocument.toString(), xmlContentHandler);
        } catch (IOException e) {
            throw new SAXException(e);
        }

        return xmlContentHandler.getDocument();
    }

    /**
     * <p>
     * Parses the instance data provided. This method assumes that the XML
     * document is fully described using XML Schemas. Failure to be fully
     * described as Schemas will result in errors, as opposed to a vid parse.
     * </p>
     *
     * @param is
     * @param hints May be null.
     * @param level
     *
     * @return Object
     *
     * @throws SAXException
     */
    public static Object getInstance(InputStream is, Map hints, Level level)
        throws SAXException {
        SAXParser parser = getParser();

        XMLSAXHandler xmlContentHandler = new XMLSAXHandler(hints);
        XMLSAXHandler.setLogLevel(level);

        try {
            parser.parse(is, xmlContentHandler);
        } catch (IOException e) {
            throw new SAXException(e);
        }

        return xmlContentHandler.getDocument();
    }

    /*
     * convinience method to create an instance of a SAXParser if it is null.
     */
    private static SAXParser getParser() throws SAXException {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        spf.setValidating(false);

        try {
            return spf.newSAXParser();
        } catch (ParserConfigurationException e) {
            throw new SAXException(e);
        } catch (SAXException e) {
            throw new SAXException(e);
        }
    }
}
