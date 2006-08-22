package org.geotools.xml;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.xsd.XSDSchema;
import org.geotools.xml.impl.ParserHandler;
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
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	public Parser( Configuration configuration ) throws ParserConfigurationException, SAXException {
	
		SAXParserFactory spf = SAXParserFactory.newInstance();
		spf.setNamespaceAware(true);
        
		parser = spf.newSAXParser();
		
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
		throws ParserConfigurationException, SAXException, IOException, URISyntaxException {
		
		this(
			configuration, new BufferedInputStream(
				new FileInputStream( new File( new URI( input ) ) )
			)
		);
	}
	
	/**
	 * Creates a new instance of the parser.
	 * 
	 * @param configuration Object representing the configuration of the parser.
	 * @param input The stream representing the instance document to be parsed.
	 * 
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * 
	 * @deprecated use {@link #Parser(Configuration)} and {@link #parse(InputStream)}.
	 */
	public Parser( Configuration configuration, InputStream input )
		throws ParserConfigurationException, SAXException  {
		
		this( configuration );
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
	 * 
	 * @deprecated use {@link #parse(InputStream)}
	 */
	public Object parse() throws IOException, SAXException {
		return parse( input );
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
	 */
	public Object parse( InputStream input ) throws IOException, SAXException {
		parser.parse( input, handler );
		return handler.getValue();
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
		if (handler != null)
			return handler.getSchemas();
		
		return null;
	}
}