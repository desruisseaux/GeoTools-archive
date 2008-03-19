/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.bind;

import org.w3c.dom.Node;

import java.util.Collections;
import java.util.Map;
import java.io.IOException;

public abstract class JAXBContext {
    public static final String JAXB_CONTEXT_FACTORY = 
        "javax.xml.bind.context.factory";
    protected JAXBContext() {}
    public static JAXBContext newInstance( String contextPath ) 
        throws JAXBException {
            
        //return newInstance( contextPath, JAXBContext.class.getClassLoader() );
        return newInstance( contextPath, Thread.currentThread().getContextClassLoader() );
    }
    
	public static JAXBContext newInstance( String contextPath, ClassLoader classLoader ) throws JAXBException {

        return newInstance(contextPath,classLoader,Collections.<String,Object>emptyMap());
    }

    public static JAXBContext newInstance( String contextPath, ClassLoader classLoader, Map<String,?>  properties  )
        throws JAXBException {

        return ContextFinder.find(
                        /* The default property name according to the JAXB spec */
                        JAXB_CONTEXT_FACTORY,

                        /* the context path supplied by the client app */
                        contextPath,

                        /* class loader to be used */
                        classLoader,
                        properties );
    }
    
    public static JAXBContext newInstance( Class... classesToBeBound )
        throws JAXBException {

        return newInstance(classesToBeBound,Collections.<String,Object>emptyMap());
    }

    public static JAXBContext newInstance( Class[] classesToBeBound, Map<String,?> properties )
        throws JAXBException {

        for( int i=classesToBeBound.length-1; i>=0; i-- )
            if(classesToBeBound[i]==null)
                throw new IllegalArgumentException();

        return ContextFinder.find(classesToBeBound,properties);
    }

    public abstract Unmarshaller createUnmarshaller() throws JAXBException;
    
    public abstract Marshaller createMarshaller() throws JAXBException;

    public <T> Binder<T> createBinder(Class<T> domType) {
		throw new UnsupportedOperationException();
	}

    public Binder<Node> createBinder() {
        return createBinder(Node.class);
    }

}
