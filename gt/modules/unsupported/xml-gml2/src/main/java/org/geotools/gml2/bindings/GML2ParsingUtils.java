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

import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDParticle;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryCollection;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.DefaultFeatureTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeBuilder;
import org.geotools.referencing.CRS;
import org.geotools.util.Converters;
import org.geotools.xml.Binding;
import org.geotools.xml.BindingWalkerFactory;
import org.geotools.xml.Node;
import org.geotools.xml.Schemas;
import org.geotools.xml.impl.BindingWalker;


/**
 * Utility methods used by gml2 bindings when parsing.
 *
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 */
public class GML2ParsingUtils {
    /**
     * Turns a parse node instance into a geotools feature type.
     * <p>
     * For each child element and attribute of the node a geotools attribute
     * type is created. AttributeType#getName() is derived from the name of
     * the child element / attribute. Attribute#getType() is derived from the
     * class of the value of the child element / attribute.
     * </p>
     * <p>
     * Attribute types for the mandatory properties of any gml feature type
     * (description,name,boundedBy) are also created.
     * </p>
     * @param node The parse node / tree for the feature.
     *
     * @return A geotools feature type
     */
    public static FeatureType featureType(Node node) throws Exception {
        FeatureTypeBuilder ftBuilder = new DefaultFeatureTypeFactory();
        ftBuilder.setName(node.getComponent().getName());
        ftBuilder.setNamespace(new URI(node.getComponent().getNamespace()));

        //mandatory gml attributes
        if (!node.hasChild("description")) {
            ftBuilder.addType(AttributeTypeFactory.newAttributeType("description", String.class));
        }

        if (!node.hasChild("name")) {
            ftBuilder.addType(AttributeTypeFactory.newAttributeType("name", String.class));
        }

        if (!node.hasChild("boundedBy")) {
            ftBuilder.addType(AttributeTypeFactory.newAttributeType("boundedBy", Envelope.class));
        }

        //application schema defined attributes
        for (Iterator c = node.getChildren().iterator(); c.hasNext();) {
            Node child = (Node) c.next();
            String name = child.getComponent().getName();
            Object valu = child.getValue();

            ftBuilder.addType(AttributeTypeFactory.newAttributeType(name,
                    (valu != null) ? valu.getClass() : Object.class));
        }

        return ftBuilder.getFeatureType();
    }

    /**
     * Turns a xml type definition into a geotools feature type.
     *
     * @param type
     *            The xml schema tupe.
     *
     * @return The corresponding geotools feature type.
     */
    public static FeatureType featureType(XSDElementDeclaration element,
        BindingWalkerFactory bwFactory) throws Exception {
        FeatureTypeBuilder ftBuilder = new DefaultFeatureTypeFactory();
        ftBuilder.setName(element.getName());
        ftBuilder.setNamespace(new URI(element.getTargetNamespace()));

        // build the feaure type by walking through the elements of the
        // actual xml schema type
        List children = Schemas.getChildElementParticles(element.getType(), true);

        for (Iterator itr = children.iterator(); itr.hasNext();) {
            XSDParticle particle = (XSDParticle) itr.next();
            XSDElementDeclaration property = (XSDElementDeclaration) particle.getContent();

            if (property.isElementDeclarationReference()) {
                property = property.getResolvedElementDeclaration();
            }

            final ArrayList bindings = new ArrayList();
            BindingWalker.Visitor visitor = new BindingWalker.Visitor() {
                    public void visit(Binding binding) {
                        bindings.add(binding);
                    }
                };

            bwFactory.walk(property, visitor);

            if (bindings.isEmpty()) {
                // could not find a binding, use the defaults
                throw new RuntimeException("Could not find binding for " + property.getQName());
            }

            // get hte last binding in the chain to execute
            Binding last = ((Binding) bindings.get(bindings.size() - 1));
            Class theClass = last.getType();

            if (theClass == null) {
                throw new RuntimeException("binding declares null type: " + last.getTarget());
            }

            // get the attribute properties
            int min = particle.getMinOccurs();
            int max = particle.getMaxOccurs();

            // create the type
            AttributeType type = AttributeTypeFactory.newAttributeType(property.getName(),
                    theClass, true, null, null, null, min, max);
            ftBuilder.addType(type);
        }

        return ftBuilder.getFeatureType();
    }

    public static Feature feature(FeatureType fType, String fid, Node node)
        throws Exception {
        Object[] attributes = new Object[fType.getAttributeCount()];

        for (int i = 0; i < fType.getAttributeCount(); i++) {
            AttributeType attType = fType.getAttributeType(i);
            Object attValue = node.getChildValue(attType.getName());

            if ((attValue != null) && !attType.getType().isAssignableFrom(attValue.getClass())) {
                //type mismatch, to try convert
                Object converted = Converters.convert(attValue, attType.getType());

                if (converted != null) {
                    attValue = converted;
                }
            }

            attributes[i] = attValue;
        }

        //create the feature
        return fType.create(attributes, fid);
    }

    public static CoordinateReferenceSystem crs(Node node) {
        if (node.getAttribute("srsName") != null) {
            URI srs = (URI) node.getAttributeValue("srsName");

            //TODO: JD, this is a hack until GEOT-1136 has been resolved
            if ("http".equals(srs.getScheme()) && "www.opengis.net".equals(srs.getAuthority())
                    && "/gml/srs/epsg.xml".equals(srs.getPath()) && (srs.getFragment() != null)) {
                try {
                    return CRS.decode("EPSG:" + srs.getFragment());
                } catch (Exception e) {
                    //no nothing, will fail belows
                }
            }

            try {
                return CRS.decode(srs.toString());
            } catch (Exception e) {
                throw new RuntimeException("Could not create crs: " + srs, e);
            }
        }

        return null;
    }

    /**
     * Wraps the elements of a geometry collection in a normal collection.
     */
    public static Collection asCollection(GeometryCollection gc) {
        ArrayList members = new ArrayList();

        for (int i = 0; i < gc.getNumGeometries(); i++) {
            members.add(gc.getGeometryN(i));
        }

        return members;
    }
}
