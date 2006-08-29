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
 * Binding object for the type http://www.opengis.net/gml:ArcByBulgeType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;complexType name="ArcByBulgeType"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;An ArcByBulge is an arc string with only one arc unit, i.e. two control points and one bulge.&lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;complexContent&gt;
 *          &lt;restriction base="gml:ArcStringByBulgeType"&gt;
 *              &lt;sequence&gt;
 *                  &lt;choice&gt;
 *                      &lt;annotation&gt;
 *                          &lt;documentation&gt;GML supports two different ways to specify the control points of a curve segment.
 *  1. A sequence of "pos" (DirectPositionType) or "pointProperty" (PointPropertyType) elements. "pos" elements are control points that are only part of this curve segment, "pointProperty" elements contain a point that may be referenced from other geometry elements or reference another point defined outside of this curve segment (reuse of existing points).
 *  2. The "posList" element allows for a compact way to specifiy the coordinates of the control points, if all control points are in the same coordinate reference systems and belong to this curve segment only. The number of direct positions in the list must be two.&lt;/documentation&gt;
 *                      &lt;/annotation&gt;
 *                      &lt;choice maxOccurs="2" minOccurs="2"&gt;
 *                          &lt;element ref="gml:pos"/&gt;
 *                          &lt;element ref="gml:pointProperty"/&gt;
 *                          &lt;element ref="gml:pointRep"&gt;
 *                              &lt;annotation&gt;
 *                                  &lt;documentation&gt;Deprecated with GML version 3.1.0. Use "pointProperty" instead. Included for backwards compatibility with GML 3.0.0.&lt;/documentation&gt;
 *                              &lt;/annotation&gt;
 *                          &lt;/element&gt;
 *                      &lt;/choice&gt;
 *                      &lt;element ref="gml:posList"/&gt;
 *                      &lt;element ref="gml:coordinates"&gt;
 *                          &lt;annotation&gt;
 *                              &lt;documentation&gt;Deprecated with GML version 3.1.0. Use "posList" instead.&lt;/documentation&gt;
 *                          &lt;/annotation&gt;
 *                      &lt;/element&gt;
 *                  &lt;/choice&gt;
 *                  &lt;element name="bulge" type="double"&gt;
 *                      &lt;annotation&gt;
 *                          &lt;documentation&gt;The bulge controls the offset of each arc's midpoint. The "bulge" is the real number multiplier for the normal that determines the offset direction of the midpoint of each arc. The length of the bulge sequence is exactly 1 less than the length of the control point array, since a bulge is needed for each pair of adjacent points in the control point array. The bulge is not given by a distance, since it is simply a multiplier for the normal.
 *  The midpoint of the resulting arc is given by: midPoint = ((startPoint + endPoint)/2.0) + bulge*normal&lt;/documentation&gt;
 *                      &lt;/annotation&gt;
 *                  &lt;/element&gt;
 *                  &lt;element name="normal" type="gml:VectorType"&gt;
 *                      &lt;annotation&gt;
 *                          &lt;documentation&gt;The attribute "normal" is a vector normal (perpendicular) to the chord of the arc, the line joining the first and last
 *  point of the arc. In a 2D coordinate system, there are only two possible directions for the normal, and it is often given as a signed real, indicating its length, with a positive sign indicating a left turn angle from the chord line, and a negative sign indicating a right turn from the chord. In 3D, the normal determines the plane of the arc, along with the start and endPoint of the arc.
 *  The normal is usually a unit vector, but this is not absolutely necessary. If the normal is a zero vector, the geometric object becomes equivalent to the straight line between the two end points. The length of the normal sequence is exactly the same as for the bulge sequence, 1 less than the control point sequence length.&lt;/documentation&gt;
 *                      &lt;/annotation&gt;
 *                  &lt;/element&gt;
 *              &lt;/sequence&gt;
 *              &lt;attribute fixed="1" name="numArc" type="integer" use="optional"&gt;
 *                  &lt;annotation&gt;
 *                      &lt;documentation&gt;An arc is an arc string consiting of a single arc, the attribute is fixed to "1".&lt;/documentation&gt;
 *                  &lt;/annotation&gt;
 *              &lt;/attribute&gt;
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
public class ArcByBulgeTypeBinding extends AbstractComplexBinding {
    /**
     * @generated
     */
    public QName getTarget() {
        return GML.ARCBYBULGETYPE;
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
