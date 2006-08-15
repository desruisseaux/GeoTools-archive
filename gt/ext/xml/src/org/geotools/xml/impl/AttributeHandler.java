package org.geotools.xml.impl;

import org.eclipse.xsd.XSDAttributeDeclaration;

/**
 * Classes implementing this interace serve as handlers for attributes in an 
 * instance document as it is parsed.
 * 
* <p>
 * An attribute handler corresponds to a specific attribute in a schema. 
 * </p>
 * 
 * @author Justin Deoliveira,Refractions Research Inc.,jdeolive@refractions.net
 */
public interface AttributeHandler extends Handler {

	/**
	 * @return The schema declaration of the attribute being handled.
	 */
	XSDAttributeDeclaration getAttributeDeclaration();
	
	/**
	 * Sets the attribute instance being handled by the handler.
	 * 
	 * @param value The value of the attribute from an instance document.
	 */
	void handleAttribute(String value);
}
