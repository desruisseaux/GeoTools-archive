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
package org.geotools.xml;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;


import org.apache.xerces.jaxp.SAXParserFactoryImpl;
import org.apache.xerces.parsers.SAXParser;
import org.eclipse.xsd.XSDSchema;
import org.geotools.xml.impl.ParserHandler;
import org.geotools.xs.bindings.XS;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * Main interface to the geotools xml parser.
 *
 * <p>
 * <h3>Schema Resolution</h3>
 * See {@link org.geotools.xml.Configuration} javadocs for instructions on how
 * to customize schema resolution. This is often desirable in the case that
 * the instance document being parsed contains invalid uri's in schema imports
 * and includes.
 * </p>
 * @author Justin Deoliveira, The Open Planning Project
 */
public class Parser {
    /** sax handler which maintains the element stack */
    private ParserHandler handler;

    /** the sax parser driving the handler */
    private SAXParser parser;

    /** the instance document being parsed */
    private InputStream input;
    
    /**
     * Creats a new instance of the parser.
     *
     * @param configuration The parser configuration, bindings and context
     *
     */
    public Parser(Configuration configuration)  {
      
    	handler = new ParserHandler(configuration);
    }

    /**
     * Creates a new instance of the parser.
     *
     * @param configuration Object representing the configuration of the parser.
     * @param input A uri representing the instance document to be parsed.
     *
     * @throws ParserConfigurationException
     * @throws SAXException If a sax parser can not be created.
     * @throws URISyntaxException If <code>input</code> is not a valid uri.
     *
     * @deprecated use {@link #Parser(Configuration)} and {@link #parse(InputStream)}.
     */
    public Parser(Configuration configuration, String input)
        throws IOException, URISyntaxException {
        this(configuration,
            new BufferedInputStream(
                new FileInputStream(new File(new URI(input)))));
    }

    /**
     * Creates a new instance of the parser.
     *
     * @param configuration Object representing the configuration of the parser.
     * @param input The stream representing the instance document to be parsed.
     *
     * @deprecated use {@link #Parser(Configuration)} and {@link #parse(InputStream)}.
     */
    public Parser(Configuration configuration, InputStream input) {
        this(configuration);
        this.input = input;
    }

    /**
     * Signals the parser to parse the entire instance document. The object
     * returned from the parse is the object which has been bound to the root
     * element of the document. This method should only be called once for
     * a single instance document.
     *
     * @return The object representation of the root element of the document.
     *
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException 
     *
     * @deprecated use {@link #parse(InputStream)}
     */
    public Object parse() throws IOException, SAXException, ParserConfigurationException {
        return parse(input);
    }

    /**
     * Parses an instance documented defined by an input stream.
     * <p>
     * The object returned from the parse is the object which has been bound to the root
     * element of the document. This method should only be called once for a single instance document.
     * </p>
     * 
     * @return The object representation of the root element of the document.
     *
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException 
     */
    public Object parse(InputStream input) throws IOException, SAXException, ParserConfigurationException {
		return parse( new InputSource( input ) );
    }
    
    /**
     * Parses an instance documented defined by a sax input source.
     * <p>
     * The object returned from the parse is the object which has been bound to the root
     * element of the document. This method should only be called once for a single instance document.
     * </p>
     * 
     * @return The object representation of the root element of the document.
     *
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException 
     */
    public Object parse(InputSource source) throws IOException, SAXException, ParserConfigurationException {
    	parser = parser();
    	parser.setContentHandler( handler );
    	parser.setErrorHandler( handler );
    	
    	parser.parse( source );
    	
        return handler.getValue();
    }
    
    /**
     * Returns a list of any validation errors that occured while parsing.
     * 
     * @return A list of errors, or an empty list if none.
     */
    public List getValidationErrors() {
    	return handler.getValidationErrors();
    }
    
    /**
     * Returns the schema objects referenced by the instance document being
     * parsed. This method can only be called after a successful parse has
     * begun.
     *
     * @return The schema objects used to parse the document, or null if parsing
     * has not commenced.
     */
    public XSDSchema[] getSchemas() {
        if (handler != null) {
            return handler.getSchemas();
        }

        return null;
    }
    
    protected SAXParser parser() throws ParserConfigurationException, SAXException {
    	//JD: we use xerces directly here because jaxp does seem to allow use to 
    	// override all the namespaces to validate against
    	SAXParser parser = new SAXParser();
    	
    	//set the appropriate features
    	parser.setFeature("http://xml.org/sax/features/namespaces", true);
    	parser.setFeature("http://xml.org/sax/features/validation", true);
        parser.setFeature("http://apache.org/xml/features/validation/schema",
            true);
        parser.setFeature("http://apache.org/xml/features/validation/schema-full-checking",
            true);
        
		//set the schema sources of this configuration, and all dependent ones
		StringBuffer schemaLocation = new StringBuffer();
		for( Iterator d = handler.getConfiguration().allDependencies().iterator(); d.hasNext(); ) {
			Configuration dependency = (Configuration) d.next();
			
			//ignore xs namespace
			if ( XS.NAMESPACE.equals( dependency.getNamespaceURI() ) )
				continue;
		
			//seperate entries by space
			if ( schemaLocation.length() > 0 ) {
				schemaLocation.append( " " );
			}
			
			//add the entry
			schemaLocation.append( dependency.getNamespaceURI() );
			schemaLocation.append( " " );
			schemaLocation.append( dependency.getSchemaFileURL() );
		}
		
		//set hte property to map namespaces to schema locations
		parser.setProperty( 
			"http://apache.org/xml/properties/schema/external-schemaLocation", 
			schemaLocation.toString()
		);
		
		//set the default location
		parser.setProperty( 
			"http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation", 
			handler.getConfiguration().getSchemaFileURL()
		);
		
		return parser;
    }
    
    /**
     * Properties used to control the parser behaviour.
     * <p>
     * Parser properties are set in the configuration of a parser.
     * <pre>
     * Configuration configuration = new ....
     * configuration.getProperties().add( Parser.Properties.PARSE_UNKNOWN_ELEMENTS );
     * configuration.getProperties().add( Parser.Properties.PARSE_UNKNOWN_ATTRIBUTES );
     * </pre>
     * </p>
     * @author Justin Deoliveira, The Open Planning Project
     *
     */
    public static interface Properties {
    	
    	/**
    	 * If set, the parser will continue to parse when it finds an element 
    	 * and cannot determine its type.
    	 */
    	QName PARSE_UNKNOWN_ELEMENTS = 
    		new QName( "http://www.geotools.org", "parseUnknownElements" ); 
    	
    	/**
    	 * If set, the parser will continue to parse when it finds an attribute
    	 * and cannot determine its type.
    	 */
    	QName PARSE_UNKNOWN_ATTRIBUTES = 
    		new QName( "http://www.geotools.org", "parseUnknownAttributes" );
    	
    	/**
    	 * If set, the parser will ignore the schemaLocation attribute of an 
    	 * instance document.
    	 */
    	QName IGNORE_SCHEMA_LOCATION = 
    		new QName( "http://www.geotools.org", "ignoreSchemaLocation" );
    }
}
