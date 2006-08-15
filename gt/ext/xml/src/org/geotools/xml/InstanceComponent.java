package org.geotools.xml;

import org.eclipse.xsd.XSDSchemaContent;
import org.eclipse.xsd.XSDTypeDefinition;

public interface InstanceComponent {
	
	/**
	 * @return The object containing the type definiton of the instance.
	 */
	XSDTypeDefinition getTypeDefinition();
	
	/**
	 * @return The feature describing the component instance.
	 */
	XSDSchemaContent getDeclaration();
	
	/**
	 * @return The namespace of the element;
	 */
	String getNamespace();
	
	/**
	 * Sets the namespace of the element.
	 * 
	 * @param namespace The new namespace.
	 */
	void setNamespace(String namespace);
	
	/**
	 * @return The name of the element.
	 */
	String getName();
	
	/**
	 * Sets the name of the element.
	 * 
	 * @param name The new name.
	 */
	void setName(String name);
	
	/**
	 * @return The text inside of the component, or the empty string if the 
	 * component does not contain any text.
	 */
	String getText();
	
	/**
	 * Sets the text of the element.
	 * 
	 * @param text The new text
	 */
	void setText(String text);
}
