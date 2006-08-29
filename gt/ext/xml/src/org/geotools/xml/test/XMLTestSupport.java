/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.xml.test;

import junit.framework.TestCase;
import org.geotools.xml.Configuration;
import org.geotools.xml.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilderFactory;


/**
 * Abstract base class for testing the parsing of instance documents.
 * <p>
 * Instance documents are created using a dom.
 * </p>
 *
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 */
public abstract class XMLTestSupport extends TestCase {
    /**
     * the instance document
     */
    protected Document document;

    /**
     *
     */
    protected void setUp() throws Exception {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory
            .newInstance();
        docFactory.setNamespaceAware(true);

        document = docFactory.newDocumentBuilder().newDocument();

        Element root = createRootElement(document);

        document.appendChild(root);

        registerNamespaces(root);
        registerSchemaLocation(root);
    }

    /**
     * Creates the root element for the document.
     * <p>
     * This method is called during setup.
     * </p>
     * @param doc Document used to build the root element.
     *
     * @return the root element of the instnace document.
     */
    protected abstract Element createRootElement(Document doc);

    /**
     * Template method for subclasses to register namespace mappings on the
     * root element of an instance document.
     * <p>
     * Namespace mappings should be set as follows:
     * <pre>
     * <code>
     *         root.setAttribute( "xmlns:gml", http://www.opengis.net/gml" );
     *         ....
     *  root.setAttribute( "xmlns", "http://www.opengis.net/sld" ); //default
     * </code>
     * </pre>
     * </p>
     * <p>
     * This method is intended to be extended or overiden. This implementation
     * registers the <code>xsi,http://www.w3.org/2001/XMLSchema-instance</code>
     * namespace.
     * </p>
     * @param root The root node of the instance document.
     *
     */
    protected void registerNamespaces(Element root) {
        root.setAttribute("xmlns:xsi",
            "http://www.w3.org/2001/XMLSchema-instance");
    }

    /**
     * Template method for subclasses to register schema locatoins on the root
     * element of an instance document.
     * <p>
     * <pre>
     * <code>
     *         root.setAttribute(
     *                 "xsi:schemaLocation",
     *                 "http://www.opengis.net/gml gml.xsd "http://www.opengis.net/sld" sld.xsd
     *         );
     * </code>
     * </pre>
     * </p>
     *
     * @param root The root node of the instance document.
     */
    protected abstract void registerSchemaLocation(Element root);

    /**
     * Tempalte method for subclasses to create the configuration to be used by
     * the parser.
     *
     * @return A parser configuration.
     */
    protected abstract Configuration createConfiguration();

    /**
     * Parses the built document.
     * <p>
     * This method should be called after building the entire document.
     *
     * </p>
     * @return
     * @throws Exception
     */
    protected Object parse() throws Exception {
        Configuration config = createConfiguration();

        DOMParser parser = new DOMParser(config, document);

        return parser.parse();
    }
}
