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
     * Creates an empty xml document.
     */
    protected void setUp() throws Exception {
    	DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
   	 	docFactory.setNamespaceAware(true);
   	 
    	document = docFactory.newDocumentBuilder().newDocument();
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
    //protected abstract Element createRootElement(Document doc);

    /**
     * Template method for subclasses to register namespace mappings on the
     * root element of an instance document.
     * <p>
     * Namespace mappings should be set as follows:
     * <pre>
     * <code>
     *	root.setAttribute( "xmlns:gml", http://www.opengis.net/gml" );
     * </code>
     * </pre>
     * </p>
     * <p>
     * Subclasses of this method should register the default namespace, the 
     * default namesapce is the one returned by the configuration.
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
    	 
    	Element root = document.getDocumentElement();
    	if ( root == null ) {
    		throw new IllegalStateException( "Document has no root element" );
    	}

    	Configuration config = createConfiguration();
        
    	registerNamespaces(root);
    	
    	//default
    	root.setAttribute( 
			"xsi:schemaLocation", config.getNamespaceURI() + " " + config.getSchemaFileURL() 
		);
     
	    DOMParser parser = new DOMParser(config, document);

        return parser.parse();
    }
}
