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

import javax.xml.namespace.QName;
import com.vividsolutions.jts.geom.Geometry;
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
 * Binding object for the type http://earth.google.com/kml/2.1:PlacemarkType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;complexType final="#all" name="PlacemarkType"&gt;
 *      &lt;complexContent&gt;
 *          &lt;extension base="kml:FeatureType"&gt;
 *              &lt;sequence&gt;
 *                  &lt;element minOccurs="0" ref="kml:Geometry"/&gt;
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
public class PlacemarkTypeBinding extends AbstractComplexBinding {
    static final FeatureType featureType;

    static {
        DefaultFeatureTypeBuilder tb = new DefaultFeatureTypeBuilder();

        //TODO: use inheiretance when our feature model works
        tb.init(FeatureTypeBinding.featureType);
        tb.setName("placemark");

        //&lt;element minOccurs="0" ref="kml:Geometry"/&gt;
        tb.add("Geometry", Geometry.class);

        featureType = tb.buildFeatureType();
    }

    /**
     * @generated
     */
    public QName getTarget() {
        return KML.PlacemarkType;
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

        //&lt;element minOccurs="0" ref="kml:Geometry"/&gt;
        b.set("Geometry", node.getChildValue(Geometry.class));

        return b.buildFeature(feature.getID());
    }
}
