package org.geotools.xml.impl;

import javax.xml.namespace.QName;

import org.eclipse.xsd.XSDAttributeDeclaration;
import org.eclipse.xsd.XSDSchemaContent;
import org.geotools.xml.InstanceComponent;


public class AttributeHandlerImpl extends HandlerImpl 
	implements AttributeHandler {

	Handler parent;
	XSDAttributeDeclaration decl;
	ParserHandler parser;
	
	AttributeHandlerImpl(
		XSDAttributeDeclaration decl, Handler parent, ParserHandler parser
	) {
		this.decl = decl;
		this.parent = parent;
		this.parser = parser;
	}
	
	public XSDAttributeDeclaration getAttributeDeclaration() {
		return decl;
	}
	
	public XSDSchemaContent getSchemaContent() {
		return decl;
	}

	public InstanceComponent getComponent() {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getValue() {
		// TODO Auto-generated method stub
		return null;
	}

	public Handler getChildHandler(QName qName) {
		//attributes do not support children
		return null;
	}
	
	public Handler getParentHandler() {
		return parent;
	}
	
	public void handleAttribute(String value) {
		
	}
}
