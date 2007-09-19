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
package org.geotools.kml.bindings;

import java.util.Collection;
import javax.xml.namespace.QName;
import org.geotools.feature.DefaultFeatureBuilder;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.type.DefaultFeatureTypeBuilder;
import org.geotools.kml.KML;
import org.geotools.xml.AbstractComplexBinding;
import org.geotools.xml.Binding;
import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;


/**
 * Binding object for the type http://earth.google.com/kml/2.1:DocumentType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;complexType final="#all" name="DocumentType"&gt;
 *      &lt;complexContent&gt;
 *          &lt;extension base="kml:ContainerType"&gt;
 *              &lt;sequence&gt;
 *                  &lt;element maxOccurs="unbounded" minOccurs="0" ref="kml:Feature"/&gt;
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
public class DocumentTypeBinding extends AbstractComplexBinding {
    static final FeatureType featureType;

    static {
        DefaultFeatureTypeBuilder tb = new DefaultFeatureTypeBuilder();
        tb.init(FeatureTypeBinding.featureType);
        tb.setName("document");

        //&lt;element maxOccurs="unbounded" minOccurs="0" ref="kml:Feature"/&gt;
        tb.add("Feature", Collection.class);

        featureType = tb.buildFeatureType();
    }

    /**
     * @generated
     */
    public QName getTarget() {
        return KML.DocumentType;
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

    public int getExecutionMode() {
        return Binding.AFTER;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Object parse(ElementInstance instance, Node node, Object value)
        throws Exception {
        DefaultFeatureBuilder b = new DefaultFeatureBuilder();

        Feature feature = (Feature) value;
        b.init(feature);
        b.setType(featureType);

        //&lt;element maxOccurs="unbounded" minOccurs="0" ref="kml:Feature"/&gt;
        b.set("Feature", node.getChildValues(Feature.class));

        return b.buildFeature(feature.getID());
    }
}
