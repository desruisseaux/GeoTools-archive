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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.geotools.feature.schema.DescriptorFactoryImpl;
import org.geotools.feature.type.TypeFactoryImpl;
import org.opengis.feature.schema.AttributeDescriptor;
import org.opengis.feature.schema.DescriptorFactory;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.TypeFactory;
import org.xml.sax.SAXException;


/**
 * Utility class to parse an XML Schema into a set of {@link
 * org.opengis.feature.type.AttributeType}.
 * 
 * <p>
 * Currently parsing capabilities are limited to a single file. Is the future
 * an eclipse emf based parser will be used, may be from the gtxml project,
 * and this limitation will be over.
 * </p>
 *
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id$
 */
public class SchemaParser {
    /** DOCUMENT ME!  */
    private static final Logger LOGGER = Logger.getLogger(SchemaParser.class.getPackage()
                                                                            .getName());

    
    private Map groups;
    
    /**
     * Type factory used as type repository for schema parsing, is 
     * initialized with basic gml types.
     * The factory is reused on each call to {@link #parse(URL)},
     * so it is possible to call parse() multiple times with different
     * schemas relying that the previously parsed types will be
     * found on this type registry.
     */
    private TypeFactory typeFactory;
    
    private DescriptorFactory descriptorFactory;
    
    /**
     * Creates a new SchemaParser object.
     */
    private SchemaParser()throws IOException {
        typeFactory = new TypeFactoryImpl();
        descriptorFactory = new DescriptorFactoryImpl();
        groups = new HashMap();
        loadGmlTypes();
    }


    /**
     * Initializes the type registry with the common gml types in
     * <code>basicTypes.xsd, gmlBase.xsd, dictionary.xsd</code>
     * <p>
     * Note by now I'm just parsing the gml schemas needed by the
     * Loughborough and Borehole types of XMML.
     * </p>
     * @throws IOException
     */
    private void loadGmlTypes()throws IOException{
    	loadSchema("schemas.opengis.net/gml/3.1.1/base/basicTypes.xsd");
    	loadSchema("schemas.opengis.net/gml/3.1.1/base/gmlBase.xsd");
    	loadSchema("schemas.opengis.net/gml/3.1.1/base/dictionary.xsd");    	
    	loadSchema("schemas.opengis.net/gml/3.1.1/base/valueObjects.xsd");    	
    	loadSchema("schemas.opengis.net/gml/3.1.1/base/coverage.xsd");    	
    	loadSchema("schemas.opengis.net/gml/3.1.1/base/measures.xsd");    	
    	loadSchema("schemas.opengis.net/gml/3.1.1/base/geometryBasic0d1d.xsd");    	
    }

    /**
     * Returns the type factory/registry used by
     * this parsed.
     * 
     * @return
     */
    public TypeFactory getTypeFactory(){
    	return typeFactory;
    }
    
    /**
     * 
     * @param location schema location path discoverable through
     * getClass().getResource()
     */
    private void loadSchema(String location)throws IOException{
		//load needed GML types directly from the gml schemas
		URL schemaLocation = getClass().getResource(location);
		parse(schemaLocation);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static SchemaParser newInstance()throws IOException {
        return new SchemaParser();
    }

    /**
     * Parses the schema referenced by <code>location</code> into
     * a set of {@link AttributeType} and {@link AttributeDescriptor}.
     *
     * @param location DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public void parse(URL location) throws IOException {
    	LOGGER.fine("about to parse " + location);
    	SchemaHandler handler = new SchemaHandler(typeFactory, descriptorFactory);
    	handler.setGroups(groups);
    	
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);

        SAXParser saxParser;

        try {
            saxParser = spf.newSAXParser();
        } catch (Exception e) {
            IOException ioe = new IOException("creating sax parser");
            ioe.initCause(e);
            throw ioe;
        }

        InputStream in = location.openStream();

        if (in == null) {
            throw new IOException("Can't open stream for " + location);
        }

        try {
            saxParser.parse(in, handler);
        } catch (SAXException e) {
            IOException ioe = new IOException("parsing gml schema " + location);
            ioe.initCause(e);
            throw ioe;
        }
    }

}
