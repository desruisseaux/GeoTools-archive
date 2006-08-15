package org.geotools.xml.impl;


import javax.xml.namespace.QName;

import org.eclipse.xsd.XSDSchemaContent;
import org.geotools.xml.InstanceComponent;
import org.picocontainer.MutablePicoContainer;

/**
 * Class implementing this interface serve has handlers for content of an 
 * instance document as it is parsed.
 *
 * <p>
 * A handler is repsonsible for parsing and validating content. Upon a 
 * successful parse and validation, the handler must return the "parsed" 
 * content from a call to {@link #getValue}.
 * </p>
 * 
 * <p>
 * A handler corresponds to a specific component in a schema. Processing is 
 * delegated to the handler when an instance of the component is encountered in
 * an instance document.
 * </p>
 * 
 * @author Justin Deoliveira,Refractions Research Inc.,jdeolive@refractions.net
 *
 */
public interface Handler {

	/**
	 * @return The entity of the schema that corresponds to the handler.
	 */
	XSDSchemaContent getSchemaContent();
	
	/**
	 * @return The instance of the schema content that is currently being 
	 * handled.
	 */
	InstanceComponent getComponent();
	
	/**
	 * @return A value which corresponds to an instance of the entity of the 
	 * handler.  
	 */
	Object getValue();
	
	/**
	 * @return The context or container in which the instance is to be parsed in.
	 * 
	 */
	MutablePicoContainer getContext();
	
	/**
	 * @param context The context in which the the instance is to be parsed in.
	 */
	void setContext(MutablePicoContainer context);
	
	/**
	 * Returns a handler for a component in the schema which is a child of 
	 * this component. 
	 * <p>
	 * This method will return null in two situations:
	 * <ol>
	 * 	<li>The schema component being handled does not support children (for 
	 * example, an attribute).</li>
	 * 	<li>A child with the specified qName could not be found.
	 * </ol>
	 * 
	 * @param qName The qualified name of the schema component.
	 * 
	 * @return A new handler, or null if one cannot be created.
	 */
	Handler getChildHandler(QName qName);
	
	/**
	 * @return The parent handler.
	 * 
	 * @see Handler#getChildHandler(QName, SchemaBuilder)
	 */
	Handler getParentHandler();
}