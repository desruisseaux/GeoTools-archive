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

import org.eclipse.xsd.XSDComplexTypeDefinition;
import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDTypeDefinition;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.QName;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFactory;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.DefaultFeatureTypeFactory;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeBuilder;
import org.geotools.referencing.CRS;
import org.geotools.xml.Binding;
import org.geotools.xml.BindingFactory;
import org.geotools.xml.BindingWalkerFactory;
import org.geotools.xml.Node;
import org.geotools.xml.Schemas;
import org.geotools.xml.impl.BindingWalker;


/**
 * Utility class for gml3 parsing.
 *
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 */
public class GML3ParsingUtils {
    /**
     * Turns a xml type definition into a geotools feature type.
     * @param type The xml schema tupe.
     *
     * @return The corresponding geotools feature type.
     */
    public static FeatureType featureType(XSDElementDeclaration element,
        BindingWalkerFactory bwFactory) throws Exception {
        FeatureTypeBuilder ftBuilder = new DefaultFeatureTypeFactory();
        ftBuilder.setName(element.getName());
        ftBuilder.setNamespace(new URI(element.getTargetNamespace()));

        //build the feaure type by walking through the elements of the 
        // actual xml schema type
        List children = Schemas.getChildElementDeclarations(element);

        for (Iterator itr = children.iterator(); itr.hasNext();) {
            XSDElementDeclaration property = (XSDElementDeclaration) itr.next();

            //ignore the attributes provided by gml, change this for new feature model
            //            if (GML.NAMESPACE.equals(property.getTargetNamespace())) {
            //                if ("boundedBy".equals(property.getName())) {
            //                    continue;
            //                }
            //
            //                if ("location".equals(property.getName())) {
            //                    continue;
            //                }
            //
            //                if ("name".equals(property.getName())) {
            //                    continue;
            //                }
            //
            //                if ("description".equals(property.getName())) {
            //                    continue;
            //                }
            //
            //                if ("metaDataProperty".equals(property.getName())) {
            //                    continue;
            //                }
            //            }
            final ArrayList bindings = new ArrayList();
            BindingWalker.Visitor visitor = new BindingWalker.Visitor() {
                    public void visit(Binding binding) {
                        bindings.add(binding);
                    }
                };

            bwFactory.createBindingWalker().walk(property, visitor);

            if (bindings.isEmpty()) {
                //could not find a binding, use the defaults
                throw new RuntimeException("Could not find binding for " + property.getQName());
            }

            Class theClass = ((Binding) bindings.get(0)).getType();

            //call method with most parameter
            ftBuilder.addType(AttributeTypeFactory.newAttributeType(property.getName(), theClass));
        }

        return ftBuilder.getFeatureType();
    }

    static CoordinateReferenceSystem crs(Node node) {
        if (node.getAttribute("srsName") != null) {
            URI srs = (URI) node.getAttributeValue("srsName");

            try {
                return CRS.decode(srs.toString());
            } catch (Exception e) {
                //TODO: log this
                return null;
            }
        }

        return null;
    }

    static LineString lineString(Node node, GeometryFactory gf, CoordinateSequenceFactory csf) {
        return line(node, gf, csf, false);
    }

    static LinearRing linearRing(Node node, GeometryFactory gf, CoordinateSequenceFactory csf) {
        return (LinearRing) line(node, gf, csf, true);
    }

    static LineString line(Node node, GeometryFactory gf, CoordinateSequenceFactory csf,
        boolean ring) {
        if (node.hasChild(DirectPosition.class)) {
            List dps = node.getChildValues(DirectPosition.class);
            DirectPosition dp = (DirectPosition) dps.get(0);

            CoordinateSequence seq = csf.create(dps.size(), dp.getDimension());

            for (int i = 0; i < dps.size(); i++) {
                dp = (DirectPosition) dps.get(i);

                for (int j = 0; j < dp.getDimension(); j++) {
                    seq.setOrdinate(i, j, dp.getOrdinate(j));
                }
            }

            return ring ? gf.createLinearRing(seq) : gf.createLineString(seq);
        }

        if (node.hasChild(Point.class)) {
            List points = node.getChildValues(Point.class);
            Coordinate[] coordinates = new Coordinate[points.size()];

            for (int i = 0; i < points.size(); i++) {
                coordinates[i] = ((Point) points.get(0)).getCoordinate();
            }

            return ring ? gf.createLinearRing(coordinates) : gf.createLineString(coordinates);
        }

        if (node.hasChild(Coordinate.class)) {
            List list = node.getChildValues(Coordinate.class);
            Coordinate[] coordinates = (Coordinate[]) list.toArray(new Coordinate[list.size()]);

            return ring ? gf.createLinearRing(coordinates) : gf.createLineString(coordinates);
        }

        if (node.hasChild(DirectPosition[].class)) {
            DirectPosition[] dps = (DirectPosition[]) node.getChildValue(DirectPosition[].class);

            CoordinateSequence seq = null;

            if (dps.length == 0) {
                seq = csf.create(0, 0);
            } else {
                seq = csf.create(dps.length, dps[0].getDimension());

                for (int i = 0; i < dps.length; i++) {
                    DirectPosition dp = (DirectPosition) dps[i];

                    for (int j = 0; j < dp.getDimension(); j++) {
                        seq.setOrdinate(i, j, dp.getOrdinate(j));
                    }
                }
            }

            return ring ? gf.createLinearRing(seq) : gf.createLineString(seq);
        }

        if (node.hasChild(CoordinateSequence.class)) {
            CoordinateSequence seq = (CoordinateSequence) node.getChildValue(CoordinateSequence.class);

            return ring ? gf.createLinearRing(seq) : gf.createLineString(seq);
        }

        return null;
    }
}
