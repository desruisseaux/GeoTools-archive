package org.geotools.xml.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDTypeDefinition;
import org.geotools.xml.Binding;
import org.geotools.xml.Schemas;
import org.picocontainer.MutablePicoContainer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Utility class to be used by bindings to encode an element.
 *
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class ElementEncoder {

	/**
	 * The factory used to load bindings.
	 */
	private BindingLoader bindingLoader;
	/**
	 * The binding context
	 */
	private MutablePicoContainer context;
	/**
	 * Logger 
	 */
	private Logger logger;
	
	public ElementEncoder ( BindingLoader bindingLoader, MutablePicoContainer context) {
		this.bindingLoader = bindingLoader;
		this.context = context;
	}
	
	/**
	 * Sets the logger for the encoder to use.
	 * @param logger
	 */
	public void setLogger( Logger logger ) {
		this.logger = logger;
	}
	
	/**
	 * Encodes a value corresponding to an element in a schema.
	 * 
	 * @param value The value to encode.
	 * @param element The declaration of the element corresponding to the value. 
	 * @param document The document used to create the encoded element.
	 * 
	 * @return The encoded value as an element.
	 */
	public Element encode( Object value, XSDElementDeclaration element, Document document ) {
		
		ElementEncodeExecutor executor = new ElementEncodeExecutor( value, element, document );
		new BindingWalker( bindingLoader, context ).walk( element, executor );
		
		return executor.getEncodedElement();
	}
	
	public void setContext(MutablePicoContainer context) {
		this.context = context;
	}
	
}
