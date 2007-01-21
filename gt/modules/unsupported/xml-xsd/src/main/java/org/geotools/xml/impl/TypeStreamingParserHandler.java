package org.geotools.xml.impl;

import org.geotools.xml.Configuration;


public class TypeStreamingParserHandler extends StreamingParserHandler {

	Class type;
	
	public TypeStreamingParserHandler(Configuration config, Class type) {
		super(config);
		this.type = type;
	}
	
	protected boolean stream(ElementHandler handler) {
		return handler.getParseNode().getValue() != null && 
		 	type.isAssignableFrom( handler.getParseNode().getValue().getClass() );
	}
	

}
