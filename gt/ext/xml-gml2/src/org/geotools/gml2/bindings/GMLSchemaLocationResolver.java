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
package org.geotools.gml2.bindings;

import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.util.XSDSchemaLocationResolver;


/**
 *
 * @generated
 */
public class GMLSchemaLocationResolver implements XSDSchemaLocationResolver {
    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     *        @generated modifiable
     */
    public String resolveSchemaLocation(XSDSchema xsdSchema,
        String namespaceURI, String schemaLocationURI) {
        if (schemaLocationURI == null) {
            return null;
        }

        //if no namespace given, assume default for the current schema
        if (((namespaceURI == null) || "".equals(namespaceURI))
                && (xsdSchema != null)) {
            namespaceURI = xsdSchema.getTargetNamespace();
        }

        if ("http://www.opengis.net/gml".equals(namespaceURI)
                && (schemaLocationURI != null)) {
            if (schemaLocationURI.endsWith("feature.xsd")) {
                return getClass().getResource("feature.xsd").toString();
            }
        }

        if ("http://www.opengis.net/gml".equals(namespaceURI)
                && (schemaLocationURI != null)) {
            if (schemaLocationURI.endsWith("geometry.xsd")) {
                return getClass().getResource("geometry.xsd").toString();
            }
        }

        if ("http://www.w3.org/1999/xlink".equals(namespaceURI)
                && (schemaLocationURI != null)) {
            if (schemaLocationURI.endsWith("xlinks.xsd")) {
                return getClass().getResource("xlinks.xsd").toString();
            }
        }

        return null;
    }
}
