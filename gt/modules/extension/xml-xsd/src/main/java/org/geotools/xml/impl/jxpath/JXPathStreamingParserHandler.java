package org.geotools.xml.impl.jxpath;

import java.util.Iterator;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathContextFactory;
import org.apache.commons.jxpath.JXPathIntrospector;
import org.geotools.xml.Configuration;
import org.geotools.xml.impl.DocumentHandler;
import org.geotools.xml.impl.ElementHandler;
import org.geotools.xml.impl.ElementHandlerImpl;
import org.geotools.xml.impl.StreamingParserHandler;

public class JXPathStreamingParserHandler extends StreamingParserHandler {

	 /** xpath to stream **/
    String xpath;
    
    public JXPathStreamingParserHandler( Configuration config, String xpath ) {
    	super( config );
    	
    	this.xpath = xpath;
    }

    protected boolean stream(ElementHandler handler) {
    	//create an xpath context from the root element
        // TODO: cache the context, should work just the same
        JXPathIntrospector.registerDynamicClass(ElementHandlerImpl.class,
            ElementHandlerPropertyHandler.class);

        ElementHandler rootHandler = 
        	((DocumentHandler) handlers.firstElement()).getDocumentElementHandler();
        
        JXPathContext jxpContext = 
        	JXPathContextFactory.newInstance().newContext(null,rootHandler);
                
        jxpContext.setLenient(true);

        Iterator itr = jxpContext.iterate(xpath);

        for (; itr.hasNext();) {
            Object obj = itr.next();
            if (handler.equals(obj)) {
            	return true;
            }
        }
    
    	return false;
    }
    
}
