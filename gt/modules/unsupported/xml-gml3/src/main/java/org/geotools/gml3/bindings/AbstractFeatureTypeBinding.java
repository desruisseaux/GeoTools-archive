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

import org.eclipse.xsd.XSDElementDeclaration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.util.Iterator;
import javax.xml.namespace.QName;
import com.vividsolutions.jts.geom.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.gml2.FeatureTypeCache;
import org.geotools.xml.AbstractComplexBinding;
import org.geotools.xml.BindingFactory;
import org.geotools.xml.BindingWalkerFactory;
import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;


/**
 * Binding object for the type http://www.opengis.net/gml:AbstractFeatureType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;complexType abstract="true" name="AbstractFeatureType"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;An abstract feature provides a set of common properties, including id, metaDataProperty, name and description inherited from AbstractGMLType, plus boundedBy.    A concrete feature type must derive from this type and specify additional  properties in an application schema. A feature must possess an identifying attribute ('id' - 'fid' has been deprecated).&lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;complexContent&gt;
 *          &lt;extension base="gml:AbstractGMLType"&gt;
 *              &lt;sequence&gt;
 *                  &lt;element minOccurs="0" ref="gml:boundedBy"/&gt;
 *                  &lt;element minOccurs="0" ref="gml:location"&gt;
 *                      &lt;annotation&gt;
 *                          &lt;appinfo&gt;deprecated&lt;/appinfo&gt;
 *                          &lt;documentation&gt;deprecated in GML version 3.1&lt;/documentation&gt;
 *                      &lt;/annotation&gt;
 *                  &lt;/element&gt;
 *                  &lt;!-- additional properties must be specified in an application schema --&gt;
 *              &lt;/sequence&gt;
 *          &lt;/extension&gt;
 *      &lt;/complexContent&gt;
 *  &lt;/complexType&gt;
 *
 *          </code>
 *         </pre>
 * </p>
 *
 * @generated
 */
public class AbstractFeatureTypeBinding extends AbstractComplexBinding {
    FeatureTypeCache ftCache;
    BindingWalkerFactory bwFactory;

    public AbstractFeatureTypeBinding(FeatureTypeCache ftCache, BindingWalkerFactory bwFactory) {
        this.ftCache = ftCache;
        this.bwFactory = bwFactory;
    }

    /**
     * @generated
     */
    public QName getTarget() {
        return GML.AbstractFeatureType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Class getType() {
        return Feature.class;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Object parse(ElementInstance instance, Node node, Object value)
        throws Exception {
        //get the definition of the element
        XSDElementDeclaration decl = instance.getElementDeclaration();

        //look for a feature type in the cache
        FeatureType fType = ftCache.get(decl.getName());

        if (fType == null) {
            fType = GML3ParsingUtils.featureType(decl, bwFactory);

            //            FeatureTypeBuilder ftBuilder = new DefaultFeatureTypeFactory();
            //            ftBuilder.setName(decl.getName());
            //            ftBuilder.setNamespace(new URI(decl.getTargetNamespace()));
            //
            //            //build the feaure type by walking through the elements of the 
            //            // actual xml schema type
            //            List children = Schemas.getChildElementDeclarations(decl);
            //
            //            for (Iterator itr = children.iterator(); itr.hasNext();) {
            //                XSDElementDeclaration property = (XSDElementDeclaration) itr.next();
            //
            //                //ignore the attributes provided by gml, change this for new feature model
            //                if (GML.NAMESPACE.equals(property.getTargetNamespace())) {
            //                    if ("boundedBy".equals(property.getName())) {
            //                        continue;
            //                    }
            //
            //                    if ("location".equals(property.getName())) {
            //                        continue;
            //                    }
            //
            //                    if ("name".equals(property.getName())) {
            //                        continue;
            //                    }
            //
            //                    if ("description".equals(property.getName())) {
            //                        continue;
            //                    }
            //
            //                    if ("metaDataProperty".equals(property.getName())) {
            //                        continue;
            //                    }
            //                }
            //
            //                XSDTypeDefinition type = property.getType();
            //
            //                QName qName = new QName(type.getTargetNamespace(), type.getName());
            //
            //                Binding binding = bindingFactory.createBinding(qName);
            //
            //                if (binding == null) {
            //                    throw new RuntimeException("Could not find binding for " + qName);
            //                }
            //
            //                Class theClass = binding.getType();
            //
            //                if (theClass == null) {
            //                    throw new RuntimeException("Could not find class for " + qName);
            //                }
            //
            //                //call method with most parameter
            //                ftBuilder.addType(AttributeTypeFactory.newAttributeType(property.getName(), theClass));
            //            }

            //fType = ftBuilder.getFeatureType();
            ftCache.put(fType);
        }

        //TODO: this could pick up wrong thing, node api needs to be 
        // namespace aware
        String fid = (String) node.getAttributeValue("id");

        //create the feature
        Feature f = fType.create(new Object[fType.getAttributeCount()], fid);

        for (Iterator itr = node.getChildren().iterator(); itr.hasNext();) {
            Node child = (Node) itr.next();
            f.setAttribute(child.getComponent().getName(), child.getValue());
        }

        return f;
    }

    public Element encode(Object object, Document document, Element value)
        throws Exception {
        Feature feature = (Feature) object;
        FeatureType featureType = feature.getFeatureType();

        String namespace = featureType.getNamespace().toString();
        String typeName = featureType.getTypeName();

        Element encoding = document.createElementNS(namespace, typeName);
        encoding.setAttributeNS(GML.NAMESPACE, "id", feature.getID());

        return encoding;
    }

    public Object getProperty(Object object, QName name)
        throws Exception {
        Feature feature = (Feature) object;

        if (GML.name.equals(name)) {
            return feature.getAttribute("name");
        }

        if (GML.description.equals(name)) {
            return feature.getAttribute("description");
        }

        if (GML.boundedBy.equals(name)) {
            Envelope bounds = feature.getBounds();

            if (bounds instanceof ReferencedEnvelope) {
                return bounds;
            }

            CoordinateReferenceSystem crs = (feature.getFeatureType().getDefaultGeometry() != null)
                ? feature.getFeatureType().getDefaultGeometry().getCoordinateSystem() : null;

            return new ReferencedEnvelope(bounds, crs);
        }

        return null;
    }
}
