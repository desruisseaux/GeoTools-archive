package org.geotools.xml.impl.jxpath;

import java.util.Iterator;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathContextFactory;
import org.apache.commons.jxpath.JXPathIntrospector;
import org.geotools.xml.Configuration;
import org.geotools.xml.Node;
import org.geotools.xml.impl.DocumentHandler;
import org.geotools.xml.impl.ElementHandler;
import org.geotools.xml.impl.ElementHandlerImpl;
import org.geotools.xml.impl.NodeImpl;
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
//        JXPathIntrospector.registerDynamicClass(ElementHandlerImpl.class,
//            ElementHandlerPropertyHandler.class);
        JXPathIntrospector.registerDynamicClass( NodeImpl.class, NodePropertyHandler.class );

//        ElementHandler rootHandler = 
//        	((DocumentHandler) handlers.firstElement()).getDocumentElementHandler();
        
        Node root = ((DocumentHandler)handlers.firstElement()).getParseNode();
        JXPathContext jxpContext = 
        	JXPathContextFactory.newInstance().newContext(null,root);
                
        jxpContext.setLenient(true);

        Iterator itr = jxpContext.iterate(xpath);

        for (; itr.hasNext();) {
            Object obj = itr.next();
            if (handler.getParseNode().equals(obj)) {
            	return true;
            }
        }
    
    	return false;
    }
    
}
