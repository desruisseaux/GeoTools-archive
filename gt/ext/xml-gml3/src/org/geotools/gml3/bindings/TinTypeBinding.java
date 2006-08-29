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

import org.geotools.xml.*;
import javax.xml.namespace.QName;


/**
 * Binding object for the type http://www.opengis.net/gml:TinType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;complexType name="TinType"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;A tin is a triangulated surface that uses
 *     the Delauny algorithm or a similar algorithm complemented with
 *     consideration of breaklines, stoplines, and maximum length of
 *     triangle sides. These networks satisfy the Delauny's criterion
 *     away from the modifications: Fore each triangle in the
 *     network, the circle passing through its vertices does not
 *     contain, in its interior, the vertex of any other triangle.&lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;complexContent&gt;
 *          &lt;extension base="gml:TriangulatedSurfaceType"&gt;
 *              &lt;sequence&gt;
 *                  &lt;element maxOccurs="unbounded" minOccurs="0"
 *                      name="stopLines" type="gml:LineStringSegmentArrayPropertyType"&gt;
 *                      &lt;annotation&gt;
 *                          &lt;documentation&gt;Stoplines are lines where the local
 *         continuity or regularity of the surface is questionable.
 *         In the area of these pathologies, triangles intersecting
 *         a stopline shall be removed from the tin surface, leaving
 *         holes in the surface. If coincidence occurs on surface
 *         boundary triangles, the result shall be a change of the
 *         surface boundary. Stoplines contains all these
 *         pathological segments as a set of line strings.&lt;/documentation&gt;
 *                      &lt;/annotation&gt;
 *                  &lt;/element&gt;
 *                  &lt;element maxOccurs="unbounded" minOccurs="0"
 *                      name="breakLines" type="gml:LineStringSegmentArrayPropertyType"&gt;
 *                      &lt;annotation&gt;
 *                          &lt;documentation&gt;Breaklines are lines of a critical
 *         nature to the shape of the surface, representing local
 *         ridges, or depressions (such as drainage lines) in the
 *         surface. As such their constituent segments must be
 *         included in the tin eve if doing so
 *         violates the Delauny criterion. Break lines contains these
 *         critical segments as a set of line strings.&lt;/documentation&gt;
 *                      &lt;/annotation&gt;
 *                  &lt;/element&gt;
 *                  &lt;element name="maxLength" type="gml:LengthType"&gt;
 *                      &lt;annotation&gt;
 *                          &lt;documentation&gt;Areas of the surface where data is not
 *         sufficiently dense to assure reasonable calculation shall be
 *         removed by adding a retention criterion for triangles based
 *         on the length of their sides. For many triangle sides
 *         exceeding maximum length, the adjacent triangles to that
 *         triangle side shall be removed from the surface.&lt;/documentation&gt;
 *                      &lt;/annotation&gt;
 *                  &lt;/element&gt;
 *                  &lt;element name="controlPoint"&gt;
 *                      &lt;annotation&gt;
 *                          &lt;documentation&gt;The corners of the triangles in the TIN
 *    are often referred to as pots. ControlPoint shall contain a
 *    set of the GM_Position used as posts for this TIN. Since each
 *    TIN contains triangles, there must be at least 3 posts. The
 *         order in which these points are given does not affect the
 *         surface that is represented. Application schemas may add
 *         information based on ordering of control points to facilitate
 *         the reconstruction of the TIN from the control points.&lt;/documentation&gt;
 *                      &lt;/annotation&gt;
 *                      &lt;complexType&gt;
 *                          &lt;choice&gt;
 *                              &lt;element ref="gml:posList"/&gt;
 *                              &lt;group maxOccurs="unbounded" minOccurs="3" ref="gml:geometricPositionGroup"/&gt;
 *                          &lt;/choice&gt;
 *                      &lt;/complexType&gt;
 *                  &lt;/element&gt;
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
public class TinTypeBinding extends AbstractComplexBinding {
    /**
     * @generated
     */
    public QName getTarget() {
        return GML.TINTYPE;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Class getType() {
        return null;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Object parse(ElementInstance instance, Node node, Object value)
        throws Exception {
        //TODO: implement
        return null;
    }
}
