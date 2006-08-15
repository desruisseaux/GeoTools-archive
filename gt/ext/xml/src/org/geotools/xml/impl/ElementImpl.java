package org.geotools.xml.impl;


import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDSchemaContent;
import org.eclipse.xsd.XSDTypeDefinition;
import org.geotools.xml.AttributeInstance;
import org.geotools.xml.ElementInstance;

public class ElementImpl extends InstanceComponentImpl 
	implements ElementInstance {

	/** declaration **/
	XSDElementDeclaration declaration;
	/** attributes **/
	AttributeInstance[] atts;
	
	
	public ElementImpl(XSDElementDeclaration declaration) {
		this.declaration = declaration;
	}
	
	public XSDTypeDefinition getTypeDefinition() {
		return declaration.getTypeDefinition();
	}
	
	public XSDSchemaContent getDeclaration() {
		return getDeclaration();
	}
	
	public XSDElementDeclaration getElementDeclaration() {
		return declaration;
	}
	
	public AttributeInstance[] getAttributes() {
		return atts;
	}

	public void setAttributes(AttributeInstance[] atts) {
		this.atts = atts;
	}
}
