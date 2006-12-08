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
package org.geotools.filter.v1_1;

import javax.xml.namespace.QName;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.PropertyName;
import org.geotools.xml.*;


/**
 * Binding object for the type http://www.opengis.net/ogc:BinarySpatialOpType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;xsd:complexType name="BinarySpatialOpType"&gt;
 *      &lt;xsd:complexContent&gt;
 *          &lt;xsd:extension base="ogc:SpatialOpsType"&gt;
 *              &lt;xsd:sequence&gt;
 *                  &lt;xsd:element ref="ogc:PropertyName"/&gt;
 *                  &lt;xsd:choice&gt;
 *                      &lt;xsd:element ref="gml:_Geometry"/&gt;
 *                      &lt;xsd:element ref="gml:Envelope"/&gt;
 *                  &lt;/xsd:choice&gt;
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
public class BinarySpatialOpTypeBinding extends AbstractComplexBinding {
    FilterFactory filterfactory;
    GeometryFactory geometryFactory;

    public BinarySpatialOpTypeBinding(FilterFactory filterfactory, GeometryFactory geometryFactory) {
        this.filterfactory = filterfactory;
        this.geometryFactory = geometryFactory;
    }

    /**
     * @generated
     */
    public QName getTarget() {
        return OGC.BINARYSPATIALOPTYPE;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Class getType() {
        return Expression[].class;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Object parse(ElementInstance instance, Node node, Object value)
        throws Exception {
        PropertyName name = (PropertyName) node.getChildValue(PropertyName.class);
        Expression spatial = null;

        if (node.hasChild(Geometry.class)) {
            spatial = filterfactory.literal(node.getChildValue(Geometry.class));
        } else if (node.hasChild(Envelope.class)) {
            //JD: creating an envelope here would break a lot of our code, for instance alot of 
            // code that encodes a filter into sql will choke on this
            Envelope envelope = (Envelope) node.getChildValue(Envelope.class);
            Polygon polygon = geometryFactory.createPolygon(geometryFactory.createLinearRing(
                        new Coordinate[] {
                            new Coordinate(envelope.getMinX(), envelope.getMinY()),
                            new Coordinate(envelope.getMaxX(), envelope.getMinY()),
                            new Coordinate(envelope.getMaxX(), envelope.getMaxY()),
                            new Coordinate(envelope.getMinX(), envelope.getMaxY()),
                            new Coordinate(envelope.getMinX(), envelope.getMinY())
                        }), null);

            spatial = filterfactory.literal(polygon);
        }

        return new Expression[] { name, spatial };
    }
}
