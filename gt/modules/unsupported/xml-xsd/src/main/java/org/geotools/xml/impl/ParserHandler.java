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
package org.geotools.xml.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDFactory;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.XSDSimpleTypeDefinition;
import org.eclipse.xsd.util.XSDSchemaLocationResolver;
import org.eclipse.xsd.util.XSDSchemaLocator;
import org.eclipse.xsd.util.XSDUtil;
import org.geotools.xml.BindingFactory;
import org.geotools.xml.Configuration;
import org.geotools.xml.Parser;
import org.geotools.xml.SchemaIndex;
import org.geotools.xml.Schemas;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.defaults.DefaultPicoContainer;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.NamespaceSupport;


/**
 *
 * The main sax event handler used for parsing the input document. This handler
 * maintains a stack of {@link Handler} objects. A handler is purshed onto the stack 
 * when a startElement event is processed, and popped off the stack when the corresponding 
 * endElement event is processed.
 *
 * @author Justin Deoliveira,Refractions Research Inc.,jdeolive@refractions.net
 *
 */
public class ParserHandler extends DefaultHandler {
    /** execution stack **/
    protected Stack handlers;

    /** namespace support **/
    NamespaceSupport namespaces;

    /** imported schemas **/
    XSDSchema[] schemas;

    /** index used to look up schema elements **/
    SchemaIndex index;

    /** handler factory **/
    HandlerFactory handlerFactory;

    /** binding loader */
    BindingLoader bindingLoader;
    
    /** bindign walker */
    BindingWalker bindingWalker;
    
    /**
     * binding factory
     */
    BindingFactory bindingFactory;
    
    /** the document handler **/
    DocumentHandler documentHandler;

    /** parser config **/
    Configuration config;

    /** context, container **/
    MutablePicoContainer context;

    /** logger **/
    Logger logger;
    
    /** flag to indicate if the parser should validate or not */
    boolean validating;
    
    /** list of "errors" that occur while parsing */
    List errors;

    public ParserHandler(Configuration config) {
        this.config = config;
        errors = new ArrayList();
        validating = false;
    }

    public Configuration getConfiguration() {
		return config;
	}
    
    public boolean isValidating() {
		return validating;
	}
    
    public void setValidating(boolean validating) {
		this.validating = validating;
	}
    
    public List getValidationErrors() {
    	return errors;
    }
    
    public HandlerFactory getHandlerFactory() {
        return handlerFactory;
    }

    public BindingLoader getBindingLoader() {
    	return bindingLoader;
    }

    public BindingWalker getBindingWalker() {
    	return bindingWalker;
    }
    
    public BindingFactory getBindingFactory() {
		return bindingFactory;
	}
    
    public XSDSchema[] getSchemas() {
        return schemas;
    }

    public SchemaIndex getSchemaIndex() {
        return index;
    }

    public Logger getLogger() {
        return logger;
    }

    public void startPrefixMapping(String prefix, String uri)
        throws SAXException {
        namespaces.declarePrefix(prefix, uri);
    }

    public void startDocument() throws SAXException {
        //perform teh configuration
        configure(config);

        //create the document handler + root context
        DocumentHandler docHandler = handlerFactory.createDocumentHandler( this );

        context = new DefaultPicoContainer();
        context = config.setupContext(context);

        docHandler.setContext(context);

        // create the stack and add handler for document element
        handlers = new Stack();
        handlers.push(docHandler);

        // get a logger from the context
        logger = (Logger) context.getComponentInstanceOfType(Logger.class);

        if (logger == null) {
            //create a default
            logger = Logger.getLogger("org.geotools.xml");
            context.registerComponentInstance(logger);
        }

        //setup the namespace support
        namespaces = new NamespaceSupport();
        context.registerComponentInstance(namespaces);
        context.registerComponentInstance( new NamespaceSupportWrapper( namespaces ) );
        
        //binding factory support
        bindingFactory = new BindingFactoryImpl( bindingLoader );
        context.registerComponentInstance( bindingFactory );
        
        //binding walker support
        context.registerComponentInstance( new BindingWalkerFactoryImpl( bindingLoader , context ) );
    }

    public void startElement(String uri, String localName, String qName,
        Attributes attributes) throws SAXException {
        if (schemas == null) {
            //root element, parse the schema
            //TODO: this processing is too loose, do some validation will ya!
            String[] locations = null;

            if ( context.getComponentInstance( Parser.Properties.IGNORE_SCHEMA_LOCATION ) != null ) {
            	//use the configuration
            	locations = new String[] {
            		config.getNamespaceURI(), config.getSchemaFileURL()	
            	};
            }
            else {
            	for (int i = 0; i < attributes.getLength(); i++) {
                    String name = attributes.getQName(i);

                    if (name.endsWith("schemaLocation")) {
                        //create an array of alternating namespace, location pairs
                        locations = attributes.getValue(i).split(" +");

                        break;
                    }
                }
            }
            

            //look up schema overrides
            XSDSchemaLocator[] locators = findSchemaLocators();
            XSDSchemaLocationResolver[] resolvers = findSchemaLocationResolvers();

            if ((locations != null) && (locations.length > 0)) {
                //parse each namespace location pair into schema objects
                schemas = new XSDSchema[locations.length / 2];

                for (int i = 0; i < locations.length; i += 2) {
                    String namespace = locations[i];
                    String location = locations[i + 1];

                    //first check for a location override
                    for (int j = 0; j < resolvers.length; j++) {
                        String override = resolvers[j].resolveSchemaLocation(null,
                                namespace, location);

                        if (override != null) {
                            location = override;

                            break;
                        }
                    }

                    //next check for schema override 
                    for (int j = 0; j < locators.length; j++) {
                        XSDSchema schema = locators[j].locateSchema(null,
                                namespace, location, null);

                        if (schema != null) {
                            schemas[i / 2] = schema;

                            break;
                        }
                    }

                    //if no schema override was found, parse location directly
                    if (schemas[i / 2] == null) {
                        try {
                            schemas[i / 2] = Schemas.parse(location, locators, resolvers);
                        } catch (Exception e) {
                            String msg = "Error parsing: " + location;
                            logger.warning(msg);
                            throw (SAXException) new SAXException(msg).initCause( e );
                        }
                    }
                }
            } else {
                //could not find a schemaLocation attribute, use the locators
                //look for schmea with locators
                for (int i = 0; i < locators.length; i++) {
                    XSDSchema schema = locators[i].locateSchema(null, uri,
                            null, null);

                    if (schema != null) {
                        schemas = new XSDSchema[] { schema };

                        break;
                    }
                }
            }

            if (schemas == null) {
                //crap out
                String msg = "Could not find a schemaLocation attribute or "
                    + "appropriate locator";
                throw new SAXException(msg);
            }

            index = new SchemaIndexImpl(schemas);
        }

        //set up a new namespace context
        namespaces.pushContext();

        //create a qName object from the string
        QName qualifiedName = new QName(uri, localName);

        //get the handler at top of the stack and lookup child
        
        //First ask teh parent handler for a child
        Handler parent = (Handler) handlers.peek();
        ElementHandler handler = (ElementHandler) parent.getChildHandler(qualifiedName);

        if ( handler == null ) {
        	//look for a global element
        	for ( int i = 0; i < schemas.length && handler == null; i++ ) {
        		XSDElementDeclaration element = 
        			Schemas.getElementDeclaration( schemas[ i ], qualifiedName );	
        		if ( element != null ) {
        			handler = handlerFactory.createElementHandler( element, parent, this );
        		}
        	}
        	
        }
        
        if ( handler == null ) {
        	//perform a lookup in the context for an element factory that create a child handler
        	List handlerFactories = context.getComponentInstancesOfType( HandlerFactory.class );
        	for ( Iterator hf = handlerFactories.iterator(); handler == null && hf.hasNext(); ) {
        		HandlerFactory handlerFactory = (HandlerFactory) hf.next();
        		handler = handlerFactory.createElementHandler( qualifiedName, parent, this );
        	}
        }
        
        if ( handler == null ) {
        	//check the parser flag, and just parse it anyways
        	if( context.getComponentInstance( Parser.Properties.PARSE_UNKNOWN_ELEMENTS ) != null) {
        		//create a mock element declaration
        		XSDElementDeclaration decl = XSDFactory.eINSTANCE.createXSDElementDeclaration();
        		decl.setName( qualifiedName.getLocalPart() );
        		decl.setTargetNamespace( qualifiedName.getNamespaceURI() );
        		
        		//set the type to be of string
        		XSDSimpleTypeDefinition type = (XSDSimpleTypeDefinition) 
        			XSDUtil.getSchemaForSchema( XSDUtil.SCHEMA_FOR_SCHEMA_URI_2001 )
        			.getSimpleTypeIdMap().get( "anyType" );
        		
        		decl.setTypeDefinition( type );
        		handler = new ElementHandlerImpl( decl, parent, this );
        	}
        }
        
        if (handler != null) {
        	//add the handler to teh list of children
        	parent.addChildHandler( handler );
        	
        	//signal the handler to start the element, and place it on the stack
            handler.startElement(qualifiedName, attributes);
            handlers.push(handler);
        } 
        else {
        	String msg = "Handler for " + qName + " could not be found.";
            throw new SAXException(msg);
        }	
    }

    public void characters(char[] ch, int start, int length)
        throws SAXException {
        //pull the handler from the top of stack
        ElementHandler handler = (ElementHandler) handlers.peek();
        handler.characters(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName)
        throws SAXException {
        //pop the last handler off of the stack
        ElementHandler handler = (ElementHandler) handlers.pop();
        
        //create a qName object from the string
        QName qualifiedName = new QName(uri, localName);
        
        handler.endElement( qualifiedName );

        endElementInternal(handler);

        //pop namespace context
        namespaces.popContext();
    }

    protected void endElementInternal(ElementHandler handler) {
        //do nothing
    }

    public void endDocument() throws SAXException {
        //only the document handler should be left on the stack
        documentHandler = (DocumentHandler) handlers.pop();

        synchronized (this) {
            notifyAll();
        }
    }

    public void warning(SAXParseException e) throws SAXException {
    	//errors.add( e );
    }
    
    public void error(SAXParseException e) throws SAXException {
    	logger.log( Level.WARNING, e.getMessage() );
    	errors.add( e );
    }
    
    public Object getValue() {
        return documentHandler.getValue();
    }

    protected void configure(Configuration config) {
        handlerFactory = new HandlerFactoryImpl();
        bindingLoader = new BindingLoader();
        bindingWalker = new BindingWalker( bindingLoader );

        //configure the strategy objects
        MutablePicoContainer container = bindingLoader.getContainer(); 
        container = config.setupBindings( container );
        bindingLoader.setContainer( container );
    }

    protected XSDSchemaLocator[] findSchemaLocators() {
    	List l = Schemas.getComponentInstancesOfType( context, XSDSchemaLocator.class );
    	//List l = context.getComponentInstancesOfType(XSDSchemaLocator.class);

        if ((l == null) || l.isEmpty()) {
            return new XSDSchemaLocator[] {  };
        }

        return (XSDSchemaLocator[]) l.toArray(new XSDSchemaLocator[l.size()]);
    }

    protected XSDSchemaLocationResolver[] findSchemaLocationResolvers() {
        //List l = context.getComponentInstancesOfType(XSDSchemaLocationResolver.class);
        List l = Schemas.getComponentInstancesOfType( context, XSDSchemaLocationResolver.class );
       
        if ((l == null) || l.isEmpty()) {
            return new XSDSchemaLocationResolver[] {  };
        }

        return (XSDSchemaLocationResolver[]) l.toArray(new XSDSchemaLocationResolver[l
            .size()]);
    }
}
