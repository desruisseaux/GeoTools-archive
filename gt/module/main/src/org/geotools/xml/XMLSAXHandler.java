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

import org.geotools.xml.handlers.DocumentHandler;
import org.geotools.xml.handlers.ElementHandlerFactory;
import org.geotools.xml.handlers.IgnoreHandler;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * XMLSAXHandler purpose.
 * 
 * <p>
 * This is a schema content handler. Code here has been modified from code
 * written by Ian Schneider.
 * </p>
 * 
 * <p>
 * This class contains one stack used to store part of the parse tree. The
 * ElementHandlers found on the stack have direct next handlers placed on the
 * stack. So here's the warning, be careful to read how you may be affecting
 * (or forgetting to affect) the stack.
 * </p>
 *
 * @author dzwiers, Refractions Research, Inc. http://www.refractions.net
 * @author $Author:$ (last modification)
 * @version $Id$
 *
 * @see XMLElementHandler
 */
public class XMLSAXHandler extends DefaultHandler {
    /**
     * the logger -- should be used for debugging (assuming there are bugs LOL)
     */
    protected final static Logger logger = Logger.getLogger(
            "net.refractions.xml.sax");

    // the stack of handlers
    private Stack handlers = new Stack();

    // hints
    private Map hints;
    private ElementHandlerFactory ehf = new ElementHandlerFactory(logger);

    // used to store prefix -> targetNamespace mapping until which time as the
    // schema uri is availiable (on the next startElement Call).
    private Map schemaProxy = new HashMap();

    // the base handler for the document
    private DocumentHandler document = null;

    // the Locator stores the current position in the parse
    // for end-user debug information
    private Locator locator;

    // the uri of the instance ducment, used to resolve relative URIs
    private URI instanceDocument;

    /**
     * <p>
     * This contructor is intended to create an XMLSAXHandler to be used when
     * parsing an XML instance document. The instance document's uri is also
     * be provided, as this will allow the parser to resolve relative uri's.
     * </p>
     *
     * @param intendedDocument
     * @param hints DOCUMENT ME!
     */
    public XMLSAXHandler(URI intendedDocument, Map hints) {
        instanceDocument = intendedDocument;
        this.hints = hints;
    }

    /**
     * Implementation of endDocument.
     *
     * @throws SAXException
     *
     * @see org.xml.sax.ContentHandler#endDocument()
     */
    public void endDocument() throws SAXException {
        document = ((DocumentHandler) handlers.pop());
    }

    /**
     * Implementation of startDocument.
     *
     * @throws SAXException
     *
     * @see org.xml.sax.ContentHandler#startDocument()
     */
    public void startDocument() throws SAXException {
        try {
            handlers.push(new DocumentHandler(ehf));
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Implementation of characters.
     *
     * @param ch
     * @param start
     * @param length
     *
     * @throws SAXException
     *
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    public void characters(char[] ch, int start, int length)
        throws SAXException {
        try {
            String text = String.copyValueOf(ch, start, length);

            if ((text != null) && !"".equals(text)) {
                ((XMLElementHandler) handlers.peek()).characters(text);
            }
        } catch (SAXException e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Implementation of endElement.
     *
     * @param namespaceURI
     * @param localName
     * @param qName
     *
     * @throws SAXException
     *
     * @see org.xml.sax.ContentHandler#endElement(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    public void endElement(String namespaceURI, String localName, String qName)
        throws SAXException {
        logger.info("END: " + qName);

        try {
            ((XMLElementHandler) handlers.pop()).endElement(namespaceURI,
                localName, hints);
        } catch (Exception e) {
            logger.warning(e.toString());
            logger.warning("Line " + locator.getLineNumber() + " Col "
                + locator.getColumnNumber());
            throw new SAXException(e);
        }
    }

    /**
     * Implementation of startElement.
     *
     * @param namespaceURI
     * @param localName
     * @param qName
     * @param atts
     *
     * @throws SAXException
     *
     * @see org.xml.sax.ContentHandler#startElement(java.lang.String,
     *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(String namespaceURI, String localName,
        String qName, Attributes atts) throws SAXException {
        logger.info("START: " + qName);

        if (schemaProxy.size() != 0) {
            logger.info("ADDING NAMESPACES: " + schemaProxy.size());

            String t = atts.getValue("http://www.w3.org/2001/XMLSchema-instance",
                    "schemaLocation");

            if ((t == null) || "".equals(t)) {
                t = atts.getValue("", "schemaLocation");
            }

            if (!((t == null) || "".equals(t))) {
                String[] targ2uri = t.split("\\s+");

                if (targ2uri != null) {
                    for (int i = 0; i < (targ2uri.length / 2); i++) {
                        String uri = targ2uri[(i * 2) + 1];
                        String targ = targ2uri[i * 2];
                        String prefix = (String) schemaProxy.get(targ);
                        URI targUri = instanceDocument.resolve(uri);
                        ehf.startPrefixMapping(prefix, targ, targUri);
                        schemaProxy.remove(targ);
                    }
                }
            }

            if (schemaProxy.size() != 0) {
                Iterator it = schemaProxy.keySet().iterator();

                while (it.hasNext()) {
                    String targ = (String) it.next();
                    String prefix = (String) schemaProxy.get(targ);
                    ehf.startPrefixMapping(prefix, targ);
//                    schemaProxy.remove(targ);
                    it.remove();
                }
            }
        }

        logger.finest("Moving on to finding the element handler");

        try {
            XMLElementHandler parent = ((XMLElementHandler) handlers.peek());
            logger.finest("Parent Node = " + parent.getClass().getName()
                + "  '" + parent.getName() + "'");

            //            logger.finest("Parent Node = "+parent.getClass().getName()+"
            // '"+parent.getName()+"' "+
            //                    (parent.getType()==null?"null":
            //                    ((((ComplexType)parent.getType()).getChild()==null)?"null":
            //                    ((((ComplexType)parent.getType()).getChild().getGrouping() ==
            // ElementGrouping.SEQUENCE)?
            //                            ((((Sequence)((ComplexType)parent.getType()).getChild()).getChildren()==null)?0:
            //                                ((Sequence)((ComplexType)parent.getType()).getChild()).getChildren().length)+"":"null"))));
            logger.finest("This Node = " + localName + " :: " + namespaceURI);

            XMLElementHandler eh = parent.getHandler(namespaceURI, localName,
                    hints);

            if (eh == null) {
                eh = new IgnoreHandler();
            }

            logger.finest("This Node = " + eh.getClass().getName());

            handlers.push(eh);
            eh.startElement(namespaceURI, localName, atts);
        } catch (Exception e) {
            logger.warning(e.toString());
            logger.warning("Line " + locator.getLineNumber() + " Col "
                + locator.getColumnNumber());
            throw new SAXException(e);
        }
    }

    /**
     * <p>
     * Used to set the logger level for all XMLSAXHandlers
     * </p>
     *
     * @param l
     */
    public static void setLogLevel(Level l) {
        logger.setLevel(l);
        XMLElementHandler.setLogLevel(l);
    }

    /**
     * getDocument purpose.
     * 
     * <p>
     * Completes the post-processing phase, and returns the value from the
     * parse ...
     * </p>
     *
     * @return
     *
     * @throws SAXException
     *
     * @see DocumentHandler#getValue()
     */
    public Object getDocument() throws SAXException {
        return document.getValue();
    }

    /**
     * Implementation of error.
     *
     * @param exception
     *
     * @throws SAXException
     *
     * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
     */
    public void error(SAXParseException exception) throws SAXException {
        logger.severe("ERROR " + exception.getMessage());
        logger.severe("col " + locator.getColumnNumber() + ", line "
            + locator.getLineNumber());
    }

    /**
     * Implementation of fatalError.
     *
     * @param exception
     *
     * @throws SAXException
     *
     * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
     */
    public void fatalError(SAXParseException exception)
        throws SAXException {
        logger.severe("FATAL " + exception.getMessage());
        logger.severe("col " + locator.getColumnNumber() + ", line "
            + locator.getLineNumber());
        throw exception;
    }

    /**
     * Implementation of warning.
     *
     * @param exception
     *
     * @throws SAXException
     *
     * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
     */
    public void warning(SAXParseException exception) throws SAXException {
        logger.warning("WARN " + exception.getMessage());
        logger.severe("col " + locator.getColumnNumber() + ", line "
            + locator.getLineNumber());
    }

    /**
     * Stores the locator for future error reporting
     *
     * @see org.xml.sax.ContentHandler#setDocumentLocator(org.xml.sax.Locator)
     */
    public void setDocumentLocator(Locator locator) {
        super.setDocumentLocator(locator);
        this.locator = locator;
    }

    /**
     * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
     */
    public void endPrefixMapping(String prefix) throws SAXException {
        ehf.endPrefixMapping(prefix);
    }

    /**
     * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String,
     *      java.lang.String)
     */
    public void startPrefixMapping(String prefix, String uri)
        throws SAXException {
        if ("http://www.w3.org/2001/XMLSchema-instance".equals(uri)) {
            return;
        }

        schemaProxy.put(uri, prefix);
    }
}
