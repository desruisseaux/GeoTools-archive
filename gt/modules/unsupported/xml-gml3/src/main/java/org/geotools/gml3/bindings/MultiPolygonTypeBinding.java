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

import java.util.List;
import javax.xml.namespace.QName;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import org.geotools.xml.*;


/**
 * Binding object for the type http://www.opengis.net/gml:MultiPolygonType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;complexType name="MultiPolygonType"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;A MultiPolygon is defined by one or more Polygons, referenced through polygonMember elements. Deprecated with GML version 3.0. Use MultiSurfaceType instead.&lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;complexContent&gt;
 *          &lt;extension base="gml:AbstractGeometricAggregateType"&gt;
 *              &lt;sequence&gt;
 *                  &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:polygonMember"/&gt;
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
public class MultiPolygonTypeBinding extends AbstractComplexBinding {
    GeometryFactory gFactory;

    public MultiPolygonTypeBinding(GeometryFactory gFactory) {
        this.gFactory = gFactory;
    }

    /**
     * @generated
     */
    public QName getTarget() {
        return GML.MultiPolygonType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Class getType() {
        return MultiPolygon.class;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Object parse(ElementInstance instance, Node node, Object value)
        throws Exception {
        List polys = node.getChildValues(Polygon.class);

        return gFactory.createMultiPolygon((Polygon[]) polys.toArray(new Polygon[polys.size()]));
    }

    public Object getProperty(Object object, QName name)
        throws Exception {
        if (GML.polygonMember.equals(name)) {
            MultiPolygon multiPolygon = (MultiPolygon) object;
            Polygon[] members = new Polygon[multiPolygon.getNumGeometries()];

            for (int i = 0; i < members.length; i++) {
                members[i] = (Polygon) multiPolygon.getGeometryN(i);
            }

            return members;
        }

        return null;
    }
}
