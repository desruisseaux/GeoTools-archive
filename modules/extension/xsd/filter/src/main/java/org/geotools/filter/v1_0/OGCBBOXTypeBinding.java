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
package org.geotools.filter.v1_0;

import org.picocontainer.MutablePicoContainer;
import javax.xml.namespace.QName;
import com.vividsolutions.jts.geom.Envelope;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.spatial.BBOX;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.gml2.GML;
import org.geotools.gml2.bindings.GML2EncodingUtils;
import org.geotools.xml.AbstractComplexBinding;
import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;


/**
 * Binding object for the type http://www.opengis.net/ogc:BBOXType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;xsd:complexType name="BBOXType"&gt;
 *      &lt;xsd:complexContent&gt;
 *          &lt;xsd:extension base="ogc:SpatialOpsType"&gt;
 *              &lt;xsd:sequence&gt;
 *                  &lt;xsd:element ref="ogc:PropertyName"/&gt;
 *                  &lt;xsd:element ref="gml:Box"/&gt;
 *              &lt;/xsd:sequence&gt;
 *          &lt;/xsd:extension&gt;
 *      &lt;/xsd:complexContent&gt;
 *  &lt;/xsd:complexType&gt;
 *
 *          </code>
 *         </pre>
 * </p>
 *
 * @generated
 */
public class OGCBBOXTypeBinding extends AbstractComplexBinding {
    private FilterFactory factory;
    private CoordinateReferenceSystem crs;

    public OGCBBOXTypeBinding() {
        //(JD) TODO: fix this. The reason we dont use constructor injection to get 
        // the factory is that pico does not do both setter + constructor injection
        // And since we support setter injection of a crs we just fall back on 
        // common factory finder... since there is actually only one filter factory
        // impl not a huge deal, but it woul dbe nice to be consistent
        factory = CommonFactoryFinder.getFilterFactory(null);
    }

    /**
     * @generated
     */
    public QName getTarget() {
        return OGC.BBOXType;
    }

    /**
     * Setter for crs.
     * <p>
     * This is used to allow containing entities (liek a wfs query) to provide
     * a coordinate reference system in the context.
     * </p>
     */
    public void setCRS(CoordinateReferenceSystem crs) {
        this.crs = crs;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Class getType() {
        return BBOX.class;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Object parse(ElementInstance instance, Node node, Object value)
        throws Exception {
        //TODO: crs
        PropertyName propertyName = (PropertyName) node.getChildValue(PropertyName.class);
        Envelope box = (Envelope) node.getChildValue(Envelope.class);
        Node srsNode = node.getChild(Envelope.class).getAttribute("srsName");
        String srs = (srsNode != null) ? srsNode.getValue().toString() : null;

        if ((srs == null) && (crs != null)) {
            srs = GML2EncodingUtils.crs(crs);
        }

        return factory.bbox(propertyName.getPropertyName(), box.getMinX(), box.getMinY(),
            box.getMaxX(), box.getMaxY(), srs);
    }

    public Object getProperty(Object object, QName name)
        throws Exception {
        BBOX box = (BBOX) object;

        //&lt;xsd:element ref="ogc:PropertyName"/&gt;
        if (OGC.PropertyName.equals(name)) {
            return factory.property(box.getPropertyName());
        }

        //&lt;xsd:element ref="gml:Box"/&gt;
        if (GML.Box.equals(name)) {
            return new Envelope(box.getMinX(), box.getMaxX(), box.getMinY(), box.getMaxY());
        }

        return null;
    }
}
