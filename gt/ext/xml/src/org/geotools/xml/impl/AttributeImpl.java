package org.geotools.xml.impl;

import org.eclipse.xsd.XSDAttributeDeclaration;
import org.eclipse.xsd.XSDSchemaContent;
import org.eclipse.xsd.XSDTypeDefinition;
import org.geotools.xml.AttributeInstance;


public class AttributeImpl extends InstanceComponentImpl 
	implements AttributeInstance {

	XSDAttributeDeclaration decl;
	
	public AttributeImpl(XSDAttributeDeclaration decl) {
		this.decl = decl;
	}
	
	public XSDTypeDefinition getTypeDefinition() {
		return decl.getTypeDefinition();
	}
	
	public XSDAttributeDeclaration getAttributeDeclaration() {
		return decl;
	}

	public XSDSchemaContent getDeclaration() {
		return getAttributeDeclaration();
	}
}
