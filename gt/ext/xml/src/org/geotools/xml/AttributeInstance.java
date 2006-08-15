package org.geotools.xml;

import org.eclipse.xsd.XSDAttributeDeclaration;

/**
 * Represents an attribute in an instance document.
 * 
 * @author Justin Deoliveira,Refractions Research Inc.,jdeolive@refractions.net
 *
 */
public interface AttributeInstance extends InstanceComponent {

	/**
	 * @return The declaration of the element from its schema.
	 */
	XSDAttributeDeclaration getAttributeDeclaration();
}
