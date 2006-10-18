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
    public String resolveSchemaLocation(XSDSchema xsdSchema, String namespaceURI,
        String schemaLocationURI) {
        if (schemaLocationURI == null) {
            return null;
        }

        //if no namespace given, assume default for the current schema
        if (((namespaceURI == null) || "".equals(namespaceURI)) && (xsdSchema != null)) {
            namespaceURI = xsdSchema.getTargetNamespace();
        }

        if ("http://www.opengis.net/gml".equals(namespaceURI)) {
            if (schemaLocationURI.endsWith("gml.xsd")) {
                return getClass().getResource("gml.xsd").toString();
            }
        }

        if ("http://www.opengis.net/gml".equals(namespaceURI)) {
            if (schemaLocationURI.endsWith("dynamicFeature.xsd")) {
                return getClass().getResource("dynamicFeature.xsd").toString();
            }
        }

        if ("http://www.opengis.net/gml".equals(namespaceURI)) {
            if (schemaLocationURI.endsWith("topology.xsd")) {
                return getClass().getResource("topology.xsd").toString();
            }
        }

        if ("http://www.opengis.net/gml".equals(namespaceURI)) {
            if (schemaLocationURI.endsWith("coverage.xsd")) {
                return getClass().getResource("coverage.xsd").toString();
            }
        }

        if ("http://www.opengis.net/gml".equals(namespaceURI)) {
            if (schemaLocationURI.endsWith("coordinateReferenceSystems.xsd")) {
                return getClass().getResource("coordinateReferenceSystems.xsd").toString();
            }
        }

        if ("http://www.opengis.net/gml".equals(namespaceURI)) {
            if (schemaLocationURI.endsWith("observation.xsd")) {
                return getClass().getResource("observation.xsd").toString();
            }
        }

        if ("http://www.opengis.net/gml".equals(namespaceURI)) {
            if (schemaLocationURI.endsWith("defaultStyle.xsd")) {
                return getClass().getResource("defaultStyle.xsd").toString();
            }
        }

        if ("http://www.opengis.net/gml".equals(namespaceURI)) {
            if (schemaLocationURI.endsWith("temporalReferenceSystems.xsd")) {
                return getClass().getResource("temporalReferenceSystems.xsd").toString();
            }
        }

        if ("http://www.opengis.net/gml".equals(namespaceURI)) {
            if (schemaLocationURI.endsWith("feature.xsd")) {
                return getClass().getResource("feature.xsd").toString();
            }
        }

        if ("http://www.opengis.net/gml".equals(namespaceURI)) {
            if (schemaLocationURI.endsWith("direction.xsd")) {
                return getClass().getResource("direction.xsd").toString();
            }
        }

        if ("http://www.opengis.net/gml".equals(namespaceURI)) {
            if (schemaLocationURI.endsWith("geometryComplexes.xsd")) {
                return getClass().getResource("geometryComplexes.xsd").toString();
            }
        }

        if ("http://www.opengis.net/gml".equals(namespaceURI)) {
            if (schemaLocationURI.endsWith("valueObjects.xsd")) {
                return getClass().getResource("valueObjects.xsd").toString();
            }
        }

        if ("http://www.opengis.net/gml".equals(namespaceURI)) {
            if (schemaLocationURI.endsWith("grids.xsd")) {
                return getClass().getResource("grids.xsd").toString();
            }
        }

        if ("http://www.opengis.net/gml".equals(namespaceURI)) {
            if (schemaLocationURI.endsWith("geometryAggregates.xsd")) {
                return getClass().getResource("geometryAggregates.xsd").toString();
            }
        }

        if ("http://www.opengis.net/gml".equals(namespaceURI)) {
            if (schemaLocationURI.endsWith("coordinateSystems.xsd")) {
                return getClass().getResource("coordinateSystems.xsd").toString();
            }
        }

        if ("http://www.opengis.net/gml".equals(namespaceURI)) {
            if (schemaLocationURI.endsWith("datums.xsd")) {
                return getClass().getResource("datums.xsd").toString();
            }
        }

        if ("http://www.opengis.net/gml".equals(namespaceURI)) {
            if (schemaLocationURI.endsWith("coordinateOperations.xsd")) {
                return getClass().getResource("coordinateOperations.xsd").toString();
            }
        }

        if ("http://www.opengis.net/gml".equals(namespaceURI)) {
            if (schemaLocationURI.endsWith("measures.xsd")) {
                return getClass().getResource("measures.xsd").toString();
            }
        }

        if ("http://www.opengis.net/gml".equals(namespaceURI)) {
            if (schemaLocationURI.endsWith("temporalTopology.xsd")) {
                return getClass().getResource("temporalTopology.xsd").toString();
            }
        }

        if ("http://www.opengis.net/gml".equals(namespaceURI)) {
            if (schemaLocationURI.endsWith("dictionary.xsd")) {
                return getClass().getResource("dictionary.xsd").toString();
            }
        }

        if ("http://www.opengis.net/gml".equals(namespaceURI)) {
            if (schemaLocationURI.endsWith("geometryBasic2d.xsd")) {
                return getClass().getResource("geometryBasic2d.xsd").toString();
            }
        }

        if ("http://www.opengis.net/gml".equals(namespaceURI)) {
            if (schemaLocationURI.endsWith("temporal.xsd")) {
                return getClass().getResource("temporal.xsd").toString();
            }
        }

        if ("http://www.opengis.net/gml".equals(namespaceURI)) {
            if (schemaLocationURI.endsWith("geometryBasic0d1d.xsd")) {
                return getClass().getResource("geometryBasic0d1d.xsd").toString();
            }
        }

        if ("http://www.opengis.net/gml".equals(namespaceURI)) {
            if (schemaLocationURI.endsWith("geometryPrimitives.xsd")) {
                return getClass().getResource("geometryPrimitives.xsd").toString();
            }
        }

        if ("http://www.opengis.net/gml".equals(namespaceURI)) {
            if (schemaLocationURI.endsWith("referenceSystems.xsd")) {
                return getClass().getResource("referenceSystems.xsd").toString();
            }
        }

        if ("http://www.opengis.net/gml".equals(namespaceURI)) {
            if (schemaLocationURI.endsWith("dataQuality.xsd")) {
                return getClass().getResource("dataQuality.xsd").toString();
            }
        }

        if ("http://www.opengis.net/gml".equals(namespaceURI)) {
            if (schemaLocationURI.endsWith("units.xsd")) {
                return getClass().getResource("units.xsd").toString();
            }
        }

        if ("http://www.opengis.net/gml".equals(namespaceURI)) {
            if (schemaLocationURI.endsWith("gmlBase.xsd")) {
                return getClass().getResource("gmlBase.xsd").toString();
            }
        }

        if ("http://www.opengis.net/gml".equals(namespaceURI)) {
            if (schemaLocationURI.endsWith("basicTypes.xsd")) {
                return getClass().getResource("basicTypes.xsd").toString();
            }
        }

        return null;
    }
}
