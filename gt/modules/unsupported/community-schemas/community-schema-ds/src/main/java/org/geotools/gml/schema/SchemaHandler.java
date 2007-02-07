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
package org.geotools.gml.schema;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.feature.schema.DescriptorFactoryImpl;
import org.geotools.feature.type.TypeFactoryImpl;
import org.geotools.util.AttributeName;
import org.opengis.feature.schema.DescriptorFactory;
import org.opengis.feature.type.TypeFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.NamespaceSupport;


/**
 * DOCUMENT ME!
 *
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id$
 * @source $URL$
 * @since 2.3.x
  */
class SchemaHandler extends DefaultHandler {
    private static final Logger LOGGER = Logger.getLogger(SchemaHandler.class.getPackage()
                                                                             .getName());
    private AbstractParserHelper currentHelper = null;
    private List /*<AbstractParserHelper>*/ helperStack;
    private TypeFactory typeFactory;
    private DescriptorFactory descFactory;

    /** content of characters() appended here, cleared on startElement. */
    private StringBuffer characters;

    /** map of declared namespaces uri/prefix */
    private NamespaceSupport namespaces;
    private String targetNameSpaceUri;
    private Map groups;

    /**
     * Creates a new SchemaHandler object.
     */
    public SchemaHandler() {
        characters = new StringBuffer();
        namespaces = new NamespaceSupport();
        typeFactory = new TypeFactoryImpl();
        descFactory = new DescriptorFactoryImpl();
        groups = new HashMap();
    }

    /**
     * Creates a new SchemaHandler object.
     *
     * @param typeFactory DOCUMENT ME!
     * @param descFactory DOCUMENT ME!
     */
    public SchemaHandler(TypeFactory typeFactory, DescriptorFactory descFactory) {
        characters = new StringBuffer();
        namespaces = new NamespaceSupport();
        this.typeFactory = typeFactory;
        this.descFactory = descFactory;
        groups = new HashMap();
    }

    /**
     * DOCUMENT ME!
     *
     * @param groups DOCUMENT ME!
     */
    public void setGroups(Map groups) {
        this.groups = groups;
    }

    /**
     * DOCUMENT ME!
     *
     * @param name DOCUMENT ME!
     * @param publicId DOCUMENT ME!
     * @param systemId DOCUMENT ME!
     *
     * @throws SAXException DOCUMENT ME!
     */
    public void notationDecl(String name, String publicId, String systemId)
        throws SAXException {
        LOGGER.info("processingInstruction: " + name + ", " + publicId + ", "
            + systemId);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws SAXException DOCUMENT ME!
     */
    public void startDocument() throws SAXException {
        LOGGER.finest("startDocument");
        helperStack = new LinkedList /*<AbstractParserHelper>*/();
    }

    /**
     * Receive notification of the start of a Namespace mapping.
     * 
     * <p>
     * By default, do nothing. Application writers may override this method in
     * a subclass to take specific actions at the start of each Namespace
     * prefix scope (such as storing the prefix mapping).
     * </p>
     *
     * @param prefix The Namespace prefix being declared.
     * @param uri The Namespace URI mapped to the prefix.
     *
     * @exception SAXException Any SAX exception, possibly wrapping another
     *            exception.
     *
     * @see org.xml.sax.ContentHandler#startPrefixMapping
     */
    public void startPrefixMapping(String prefix, String uri)
        throws SAXException {
        LOGGER.finest("startPrefixMapping(" + prefix + ", " + uri + ")");
        namespaces.declarePrefix(prefix, uri);
    }

    /**
     * DOCUMENT ME!
     *
     * @param uri DOCUMENT ME!
     * @param localName DOCUMENT ME!
     * @param qName DOCUMENT ME!
     * @param attributes DOCUMENT ME!
     *
     * @throws SAXException DOCUMENT ME!
     */
    public void startElement(String uri, String localName, String qName,
        Attributes attributes) throws SAXException {
        LOGGER.finest("startElement(" + uri + ", " + localName + ", " + qName
            + ", " + attributes + ")");

        characters.setLength(0);

        String prefix = (String) namespaces.getPrefix(uri);
        AttributeName elemName;

        /*
           if (prefix == null) {
                   throw new SAXException("namespace uri was not registered: " + uri);
           }
         */
        elemName = new AttributeName(prefix, uri, localName);

        boolean alreadyInitialized = true;

        if (currentHelper == null) {
            push(new SchemaParserHelper());
            alreadyInitialized = false;
        }

        AbstractParserHelper helper = this.currentHelper.getHelper(elemName);
        helper.setNamespaces(this.targetNameSpaceUri, this.namespaces);
        helper.setFactories(typeFactory, descFactory);
        helper.init(elemName, attributes);
        helper.setGroups(groups);

        currentHelper.startElement(elemName, attributes);

        if (!alreadyInitialized) {
            SchemaHelper schemaAtts = (SchemaHelper) helper;
            this.targetNameSpaceUri = schemaAtts.getTargetNamespaceUri();
        }

        push(helper);
    }

    /**
     * DOCUMENT ME!
     *
     * @param uri DOCUMENT ME!
     * @param localName DOCUMENT ME!
     * @param qName DOCUMENT ME!
     *
     * @throws SAXException DOCUMENT ME!
     */
    public void endElement(String uri, String localName, String qName)
        throws SAXException {
        LOGGER.entering(getClass().getName(), "endElement",
            new Object[] { uri, localName, qName });

        AttributeName elemName = new AttributeName(uri, localName);
        Object product = null;

        try {
            product = currentHelper.getProduct(elemName);
        } catch (RuntimeException e) {
            LOGGER.log(Level.SEVERE,
                "can't continue parsing: " + e.getMessage(), e);
            throw e;
        }

        pop();

        currentHelper.addSubproduct(elemName, product);
    }

    private void push(AbstractParserHelper helper) {
        LOGGER.finer("push: -> " + helper);
        helperStack.add(0, helper);
        this.currentHelper = helper;
    }

    private void pop() {
        AbstractParserHelper removed = (AbstractParserHelper) helperStack.remove(0);
        LOGGER.finer("pop:  <- " + removed);
        currentHelper = (AbstractParserHelper) helperStack.get(0);
    }

    /**
     * DOCUMENT ME!
     *
     * @param ch DOCUMENT ME!
     * @param start DOCUMENT ME!
     * @param length DOCUMENT ME!
     *
     * @throws SAXException DOCUMENT ME!
     */
    public void characters(char[] ch, int start, int length)
        throws SAXException {
        characters.append(ch, start, length);
    }

    /**
     * Receive notification of the end of a Namespace mapping.
     * 
     * <p>
     * By default, do nothing. Application writers may override this method in
     * a subclass to take specific actions at the end of each prefix mapping.
     * </p>
     *
     * @param prefix The Namespace prefix being declared.
     *
     * @exception SAXException Any SAX exception, possibly wrapping another
     *            exception.
     *
     * @see org.xml.sax.ContentHandler#endPrefixMapping
     */
    public void endPrefixMapping(String prefix) throws SAXException {
        LOGGER.finest("endPrefixMapping(" + prefix + ")");

        // LOGGER.info("endPrefixMapping: " + prefix);
    }

    /**
     * At the end of the document, the helper stack should have got empty and
     * the current helper being the SchemaHelper, so this handlers
     * parsedContent is set to the product of {@linkPlain SchemaHelper}
     *
     * @throws SAXException DOCUMENT ME!
     */
    public void endDocument() throws SAXException {
        LOGGER.finest("endDocument");
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public TypeFactory getTypeFactory() {
        return typeFactory;
    }

    /**
     * DOCUMENT ME!
     *
     * @param typeFactory DOCUMENT ME!
     */
    public void setTypeFactory(TypeFactory typeFactory) {
        this.typeFactory = typeFactory;
    }
}
