package org.geotools.xml;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;

import org.geotools.xml.impl.ParserHandler;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Parses a DOM (Document Object Model) using the geotools xml binding system.
 *  
 * @author Justin Deoliveira, The Open Planning Project 
 *
 */
public class DOMParser {

	Configuration configuration;
	Document document;
	
	ParserHandler handler;
	
	/**
	 * Creates a new instance of the parser.
	 * 
	 * @param configuration Object representing the configuration of the parser.
	 * @param document An xml document.
	 * 
	 */
	public DOMParser(Configuration configuration, Document document) {
		this.configuration = configuration;
		this.document = document;
	}
	
	/**
	 * Parses the supplied DOM returning a single object respresenting the 
	 * result of the parse.
	 * 
	 * @return The object representation of the root element of the document.
	 * 
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	public Object parse() throws ParserConfigurationException, SAXException{
		//Prepare the DOM source
	    Source source = new DOMSource(document);
	    
	    // Create the handler to handle the SAX events
	    handler = new ParserHandler(configuration);
	    
	    try {
	        // Prepare the result
	        SAXResult result = new SAXResult(handler);
	    
	        TransformerFactory xformerFactory = TransformerFactory.newInstance();
	        
	        // Create a transformer
	        Transformer xformer = xformerFactory.newTransformer();
	        
	        // Traverse the DOM tree
	        xformer.transform(source, result);
	    } 
	    catch (TransformerConfigurationException e) {
	    	new ParserConfigurationException().initCause(e);
	    } 
	    catch (TransformerException e) {
	    	throw new SAXException(e);
	    }
	
	    return handler.getValue();
	}
}
