package org.geotools.xml.impl;

import java.util.Iterator;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathContextFactory;
import org.apache.commons.jxpath.JXPathIntrospector;
import org.geotools.xml.Configuration;
import org.geotools.xml.impl.jxpath.ElementHandlerPropertyHandler;
import org.xml.sax.SAXException;

public class StreamingParserHandler extends ParserHandler {

	/** name of element to stream **/
	String xpath;
	/** stream buffer **/
	Buffer buffer;
	
	public StreamingParserHandler(Configuration config, String xpath) {
		super(config);
		
		//TODO: validate the xpath expression
		this.xpath = xpath;
		buffer = new Buffer();
	}
	
	protected void endElementInternal(ElementHandler handler) {
		super.endElementInternal(handler);
		
		//create an xpath context from the root element
		// TODO: cache the context, should work just the same
		JXPathIntrospector.registerDynamicClass(
			ElementHandlerImpl.class, ElementHandlerPropertyHandler.class
		);
		JXPathContext jxpContext = JXPathContextFactory.newInstance()
			.newContext(null,((DocumentHandler)handlers.firstElement()).getDocumentElementHandler());
		jxpContext.setLenient(true);
		
		Iterator itr = jxpContext.iterate(xpath);
		for (; itr.hasNext(); ) {
			Object obj = itr.next();
			if (handler.equals(obj)) {
				//throw value into buffer
				buffer.put(handler.getValue());
				
				//remove handler from parse tree
				if (handler.getParentHandler() instanceof ElementHandler) {
					ElementHandler parent = 
						(ElementHandler)handler.getParentHandler();
					parent.removeChildHandler(handler);
				}
	
				break;
			}
		}
	}
	
	public void endDocument() throws SAXException {
		super.endDocument();
		buffer.close();
	}
	
	public Buffer getBuffer() {
		return buffer;
	}
	

}
