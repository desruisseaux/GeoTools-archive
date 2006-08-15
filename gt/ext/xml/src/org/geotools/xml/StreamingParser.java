package org.geotools.xml;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.geotools.xml.impl.StreamingParserHandler;
import org.xml.sax.SAXException;

/**
 * Main interface to the streaming geotools xml parser. 
 * <p>
 * Performs the same task as {@link org.geotools.xml.Parser}, with the addition
 * that objects are streamed back to the client. The client must inform the 
 * parser what objects to stream back via an xpath expression. Consider the 
 * following example.
 * 
 * <pre>
 * 	<code>
 *  Configuration config = ... //some configuration
 *  String input = ... //some instance document
 *  String xpath = "//envelope";
 *  
 *  StreamingParser parser = new StreamingParser(config,input,xpath);
 *  Envelope e = null;
 *  while((e = (Envelope)parser.parse()) != null) {
 *  	//do something
 *  }
 *  </code>
 * </pre>
 * 
 * The xpath expression <code>//envelope</code> matches any element named 
 * <code>envelope</code> in the instance document. Whenever the parser has 
 * turned such an element into an object (an instance of Envelope), the object
 * is returned.
 * </p>
 * 
 * @author Justin Deoliveira, The Open Planning Project
 */
public class StreamingParser {

	private StreamingParserHandler handler;
	
	private SAXParser parser;
	private InputStream input;
	
	private Thread thread;
	
	/**
	 * Creates a new instance of the streaming parser.
	 * 
	 * @param configuration Object representing the configuration of the parser.
	 * @param input The path to the instance document to be parsed.
	 * @param xpath An xpath expression which dictates how the parser streams 
	 * objects back to the client.
	 * 
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws FileNotFoundException 
	 */
	public StreamingParser(Configuration configuration,String input,String xpath) 
		throws ParserConfigurationException, SAXException, FileNotFoundException {
		
		this( configuration, new BufferedInputStream( new FileInputStream( input ) ) , xpath );
	}
	
	/**
	 * Creates a new instance of the streaming parser.
	 * 
	 * @param configuration Object representing the configuration of the parser.
	 * @param input The input stream representing the instance document to be parsed.
	 * @param xpath An xpath expression which dictates how the parser streams 
	 * objects back to the client.
	 * 
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws FileNotFoundException 
	 */
	public StreamingParser( Configuration configuration, InputStream input, String xpath ) 
		throws ParserConfigurationException, SAXException {
		
		SAXParserFactory spf = SAXParserFactory.newInstance();
		spf.setNamespaceAware(true);
       
		parser = spf.newSAXParser();
		handler = new StreamingParserHandler(configuration,xpath);
		
		this.input = input;
	}
	
	
	/**
	 * Streams the parser to the next element in the instance document which 
	 * matches the xpath query specified in the contstructor. This method 
	 * returns null when there are no more objects to stream.
	 * 
	 * @return The next object in the stream, or null if no such object is 
	 * available.
	 */
	public Object parse()  {
		
		if (thread == null) {
			Runnable runnable = new Runnable() {
				public void run() {
					try {
						parser.parse(input,handler);
					} 
					catch (Exception e) {
						e.printStackTrace();
						throw new RuntimeException(e);
					}
				};
			};
			thread = new Thread(runnable);
			thread.start();
		}
		
		return handler.getBuffer().get();
		
	}
}
