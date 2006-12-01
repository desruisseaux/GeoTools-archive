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

import java.util.Collections;
import java.util.List;

import javax.xml.namespace.QName;

import org.eclipse.xsd.XSDAttributeDeclaration;
import org.eclipse.xsd.XSDSchemaContent;
import org.geotools.xml.InstanceComponent;


public class AttributeHandlerImpl extends HandlerImpl
    implements AttributeHandler {
    Handler parent;
    XSDAttributeDeclaration decl;
    ParserHandler parser;

    AttributeHandlerImpl(XSDAttributeDeclaration decl, Handler parent,
        ParserHandler parser) {
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
    
    public void addChildHandler(Handler child) {
    	//attributes do not support children
    }
    
    public void removeChildHandler(Handler child) {
    	//attributes do not support children
    }

    public List getChildHandlers() {
    	//attributes do not support children
    	return Collections.EMPTY_LIST;
    }
    
    public Handler getParentHandler() {
        return parent;
    }

    public void handleAttribute(String value) {
    }
}
