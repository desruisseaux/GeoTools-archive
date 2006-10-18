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
package org.geotools.gml3.bindings;

import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.util.XSDSchemaLocationResolver;
import org.eclipse.xsd.util.XSDSchemaLocator;
import java.io.IOException;
import org.geotools.gml3.bindings.smil.SMIL20SchemaLocationResolver;
import org.geotools.xlink.bindings.XLINKSchemaLocationResolver;
import org.geotools.xml.Schemas;


public class GMLSchemaLocator implements XSDSchemaLocator {
    public XSDSchema locateSchema(XSDSchema schema, String namespaceURI,
        String rawSchemaLocationURI, String resolvedSchemaLocationURI) {
        if (GML.NAMESPACE.equals(namespaceURI)) {
            String location = getClass().getResource("gml.xsd").toString();

            XSDSchemaLocationResolver[] locators = new XSDSchemaLocationResolver[] {
                    new XLINKSchemaLocationResolver(), new SMIL20SchemaLocationResolver(),
                    new GMLSchemaLocationResolver()
                };

            try {
                return Schemas.parse(location, null, locators);
            } catch (IOException e) {
                //TODO:  log this
            }
        }

        return null;
    }
}
