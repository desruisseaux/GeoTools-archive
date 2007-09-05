package org.geotools.xml.impl;

import javax.xml.namespace.QName;

import org.geotools.xml.Configuration;

public class ElementNameStreamingParserHandler extends StreamingParserHandler {

	/** 
	 * The name of elements to stream
	 */
	QName name;
	
	public ElementNameStreamingParserHandler(Configuration config, QName name) {
		super(config);
		
		this.name = name;
	}
	
	protected boolean stream(ElementHandler handler) {
		return name.getNamespaceURI().equals( handler.getComponent().getNamespace() ) 
			&& name.getLocalPart().equals( handler.getComponent().getName() );
	}
}
