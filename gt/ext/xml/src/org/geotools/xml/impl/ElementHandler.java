package org.geotools.xml.impl;

import java.util.List;

import javax.xml.namespace.QName;

import org.eclipse.xsd.XSDElementDeclaration;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Classes implementing this interface serve has handlers for elements in an 
 * instance document as it is parsed. The element handler interface is a subset of the {@link
 * org.xml.sax.ContentHandler} interface. 
 *
 * <p>The methods <code>startElement, characters, and endElement</code> are called in 
 * sequence as they are for normal sax content handlers. 
 * </p>
 * 
 * <p>
 * An element handler corresponds to a specific element in a schema. A handler
 * must return a child handler for each valid child element of its corresponding
 * element.
 * </p>
 * 
 * @see org.xml.sax.ContentHandler
 *  
 * @author Justin Deoliveira,Refractions Research Inc.,jdeolive@refractions.net
 *
 */
public interface ElementHandler extends Handler {

	void startElement(QName qName, Attributes attributes) throws SAXException;
		
	void characters(char[] ch, int start, int length) throws SAXException;
		
	void endElement(String uri, String localName, String qName) 
		throws SAXException;
	
	XSDElementDeclaration getElementDeclaration();
	
	List getChildHandlers();
	
	void removeChildHandler(Handler child);
	
}
