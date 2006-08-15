package org.geotools.xml;

import org.eclipse.xsd.XSDElementDeclaration;

/**
 * Represents an element in an instance document.
 * 
 * @author Justin Deoliveira,Refractions Research Inc.,jdeolive@refractions.net
 *
 */
public interface ElementInstance extends InstanceComponent {

	/**
	 * @return The declaration of the element in the schema.
	 */
	XSDElementDeclaration getElementDeclaration();
	
	/**
	 * @return The attributes of the element.
	 */
	AttributeInstance[] getAttributes();
	
	/**
	 * Sets the attributes of the element.
	 * 
	 * @param atts The new attributes.
	 */
	void setAttributes(AttributeInstance[] atts);
	
}
