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
import javax.xml.namespace.QName;
import com.vividsolutions.jts.geom.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.gml2.FeatureTypeCache;
import org.geotools.xml.AbstractComplexBinding;
import org.geotools.xml.BindingWalkerFactory;
import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;


/**
 * Binding object for the type http://www.opengis.net/gml:AbstractFeatureType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;complexType name="AbstractFeatureType" abstract="true"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;         An abstract feature provides a set of
 *              common properties. A concrete          feature type must
 *              derive from this type and specify additional
 *              properties in an application schema. A feature may
 *              optionally          possess an identifying attribute
 *              (&apos;fid&apos;).       &lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;sequence&gt;
 *          &lt;element ref="gml:description" minOccurs="0"/&gt;
 *          &lt;element ref="gml:name" minOccurs="0"/&gt;
 *          &lt;element ref="gml:boundedBy" minOccurs="0"/&gt;
 *          &lt;!-- additional properties must be specified in an application schema --&gt;
 *      &lt;/sequence&gt;
 *      &lt;attribute name="fid" type="ID" use="optional"/&gt;
 *  &lt;/complexType&gt;
 *
 *          </code>
 *         </pre>
 * </p>
 *
 * @generated
 */
public class GMLAbstractFeatureTypeBinding extends AbstractComplexBinding {
    /** Cache of feature types */
    FeatureTypeCache ftCache;

    /** factory for loading bindings */
    BindingWalkerFactory bwFactory;

    public GMLAbstractFeatureTypeBinding(FeatureTypeCache ftCache, BindingWalkerFactory bwFactory) {
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
            fType = GML2ParsingUtils.featureType(decl, bwFactory);
            ftCache.put(fType);
        }

        //fid
        String fid = (String) node.getAttributeValue("fid");

        //create feature
        return GML2ParsingUtils.feature(fType, fid, node);
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

        if (feature.getFeatureType().getAttributeType(name.getLocalPart()) != null) {
            return feature.getAttribute(name.getLocalPart());
        }

        return null;
    }
}
