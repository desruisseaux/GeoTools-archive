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

import org.eclipse.xsd.XSDAttributeDeclaration;
import org.eclipse.xsd.XSDElementDeclaration;
import org.geotools.xml.SchemaIndex;
import javax.xml.namespace.QName;


public class HandlerFactoryImpl implements HandlerFactory {
    ParserHandler parser;

    public HandlerFactoryImpl(ParserHandler parser) {
        this.parser = parser;
    }

    public DocumentHandler createDocumentHandler() {
        return new DocumentHandlerImpl(this);
    }

    public ElementHandler createElementHandler(QName qName, Handler parent) {
        SchemaIndex index = parser.getSchemaIndex();

        //look up the element in the schema
        XSDElementDeclaration element = index.getElementDeclaration(qName);

        if (element != null) {
            return createElementHandler(element, parent);
        }

        return null;
    }

    public ElementHandler createElementHandler(XSDElementDeclaration element,
        Handler parent) {
        return new ElementHandlerImpl(element, parent, parser);
    }

    public AttributeHandler createAttributeHandler(
        XSDAttributeDeclaration attribute, Handler parent) {
        return new AttributeHandlerImpl(attribute, parent, parser);
    }
}
