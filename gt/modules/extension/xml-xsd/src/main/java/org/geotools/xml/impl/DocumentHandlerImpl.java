/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.xml.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.xsd.XSDSchemaContent;
import org.geotools.xml.InstanceComponent;
import javax.xml.namespace.QName;


public class DocumentHandlerImpl extends HandlerImpl implements DocumentHandler {
    /** factory used to create a handler for the root element **/
    HandlerFactory factory;

    /** handler for root element **/
    ElementHandler handler;

    /** the parser */
    ParserHandler parser;
    
    public DocumentHandlerImpl(HandlerFactory factory, ParserHandler parser ) {
        this.factory = factory;
        this.parser = parser;
    }

    public XSDSchemaContent getSchemaContent() {
        return null;
    }

    public InstanceComponent getComponent() {
        return null;
    }

    public Object getValue() {
        //just return the root handler value
        if (handler != null) {
            return handler.getValue();
        }

        return null;
    }

    public Handler getChildHandler(QName qName) {
        handler = factory.createElementHandler(qName, this, parser );

        return handler;
    }
    
    public List getChildHandlers() {
    	if ( handler == null ) {
    		return Collections.EMPTY_LIST;
    	}
    	
    	ArrayList list = new ArrayList();
    	list.add( handler );
    	
    	return list;
    }
    

    public void addChildHandler(Handler child) {
    	//do nothing
    }
    
    public void removeChildHandler(Handler child) {
    	//do nothing
    }
    
    public Handler getParentHandler() {
        //always null, this is the root handler
        return null;
    }

    public ElementHandler getDocumentElementHandler() {
        return handler;
    }
}
