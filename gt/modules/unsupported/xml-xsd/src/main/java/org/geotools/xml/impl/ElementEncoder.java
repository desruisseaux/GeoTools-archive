package org.geotools.xml.impl;

import org.eclipse.xsd.XSDElementDeclaration;
import org.picocontainer.MutablePicoContainer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Utility class to be used by bindings to encode an element.
 * <p>
 * Bindings should not instantiate this class directly, it should be declared as a dependency 
 * in the constructor.
 * </p>
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
	
	public ElementEncoder ( BindingLoader bindingLoader, MutablePicoContainer context ) {
		this.bindingLoader = bindingLoader;
		this.context = context;
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
