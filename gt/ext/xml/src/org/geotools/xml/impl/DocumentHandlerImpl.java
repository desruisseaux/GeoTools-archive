package org.geotools.xml.impl;

import javax.xml.namespace.QName;

import org.eclipse.xsd.XSDSchemaContent;
import org.geotools.xml.InstanceComponent;

public class DocumentHandlerImpl extends HandlerImpl
	implements DocumentHandler {

	/** factory used to create a handler for the root element **/
	HandlerFactory factory;
	/** handler for root element **/
	ElementHandler handler;
	
	public DocumentHandlerImpl(HandlerFactory factory) {
		this.factory = factory;
	}
	
	public XSDSchemaContent getSchemaContent() {
		return null;
	}

	public InstanceComponent getComponent() {
		return null;
	}
	
	public Object getValue() {
		//just return the root handler value
		if (handler != null)
			return handler.getValue();
		
		return null;
	}

	public Handler getChildHandler(QName qName) {
		handler = factory.createElementHandler(qName, this);
		return handler;
	}
	
	public Handler getParentHandler() {
		//always null, this is the root handler
		return null;
	}
	
	public ElementHandler getDocumentElementHandler() {
		return handler;
	}
}
