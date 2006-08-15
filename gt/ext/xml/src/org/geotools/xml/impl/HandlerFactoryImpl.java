package org.geotools.xml.impl;

import javax.xml.namespace.QName;

import org.eclipse.xsd.XSDAttributeDeclaration;
import org.eclipse.xsd.XSDElementDeclaration;
import org.geotools.xml.SchemaIndex;

public class HandlerFactoryImpl implements HandlerFactory {

	ParserHandler parser;
	public HandlerFactoryImpl(ParserHandler parser) {
		this.parser = parser;
	}
	
	public DocumentHandler createDocumentHandler() {
		return new DocumentHandlerImpl(this);
	}

	public ElementHandler createElementHandler(
		QName qName, Handler parent
	) {
		SchemaIndex index = parser.getSchemaIndex();
		
		//look up the element in the schema
		XSDElementDeclaration element = index.getElementDeclaration(qName);
		if (element != null) {
			return createElementHandler(element, parent);
		}
		
		return null;
	}

	public ElementHandler createElementHandler(
		XSDElementDeclaration element, Handler parent
	) {
		return new ElementHandlerImpl(element,parent,parser);
	}
	
	public AttributeHandler createAttributeHandler(
		XSDAttributeDeclaration attribute, Handler parent
	) {
		return new AttributeHandlerImpl(attribute,parent,parser);
	}
}
