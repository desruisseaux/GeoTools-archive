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

import org.eclipse.xsd.XSDSchemaContent;
import org.geotools.xml.InstanceComponent;
import javax.xml.namespace.QName;


public class DocumentHandlerImpl extends HandlerImpl implements DocumentHandler {
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
        if (handler != null) {
            return handler.getValue();
        }

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
