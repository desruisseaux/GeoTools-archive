package org.geotools.xml;

import javax.xml.namespace.QName;

/**
 * Creates the binding for a qualified name.
 * <p>
 * An instance of this factory is placed in the context and available to 
 * bindings via constructor injection.
 * </p>
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public interface BindingFactory {

	/**
	 * Creates the binding from a qualified name.
	 * <p>
	 * Example usage.
	 * <pre>
	 * //Load the binding for xs int 
	 * QName name = new QName( "http://www.w3.org/2001/XMLSchema", "int" );
	 * Binding binding = bindingFactory.createBinding( name ); 
	 * </pre>
	 * </p>
	 * 
	 * @param name The qualified name of a schema type, element, or attribute.
	 * 
	 * @return The binding for <code>name</code>, or <code>null</code>.
	 */
	Binding createBinding( QName name );
}
