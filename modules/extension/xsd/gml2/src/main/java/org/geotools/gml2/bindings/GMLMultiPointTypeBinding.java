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

import javax.xml.namespace.QName;

import org.geotools.gml2.GML;
import org.geotools.xml.AbstractComplexBinding;
import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;

import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;


/**
 * Binding object for the type http://www.opengis.net/gml:MultiPointType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;complexType name="MultiPointType"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;         A MultiPoint is defined by one or more
 *              Points, referenced through          pointMember elements.       &lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;complexContent&gt;
 *          &lt;restriction base="gml:GeometryCollectionType"&gt;
 *              &lt;sequence&gt;
 *                  &lt;element ref="gml:pointMember" maxOccurs="unbounded"/&gt;
 *              &lt;/sequence&gt;
 *              &lt;attribute name="gid" type="ID" use="optional"/&gt;
 *              &lt;attribute name="srsName" type="anyURI" use="required"/&gt;
 *          &lt;/restriction&gt;
 *      &lt;/complexContent&gt;
 *  &lt;/complexType&gt;
 *
 *          </code>
 *         </pre>
 * </p>
 *
 * @generated
 */
public class GMLMultiPointTypeBinding extends AbstractComplexBinding {
    GeometryFactory gFactory;

    public GMLMultiPointTypeBinding(GeometryFactory gFactory) {
        this.gFactory = gFactory;
    }

    /**
     * @generated
     */
    public QName getTarget() {
        return GML.MultiPointType;
    }

    public int getExecutionMode() {
        return AFTER;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Class getType() {
        return MultiPoint.class;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Object parse(ElementInstance instance, Node node, Object value)
        throws Exception {
        GeometryCollection gc = (GeometryCollection) value;
        Point[] points = new Point[gc.getNumGeometries()];

        for (int i = 0; i < gc.getNumGeometries(); i++) {
            points[i] = (Point) gc.getGeometryN(i);
        }

        MultiPoint multiPoint = gFactory.createMultiPoint(points);
        multiPoint.setUserData(gc.getUserData());

        return multiPoint;
    }

    public Object getProperty(Object object, QName name)
        throws Exception {
        if (GML.pointMember.equals(name)) {
            return GML2ParsingUtils.asCollection((MultiPoint) object);
        }

        return null;
    }
}
