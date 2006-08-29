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
 * Binding object for the type http://www.opengis.net/gml:ArcByCenterPointType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;complexType name="ArcByCenterPointType"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;This variant of the arc requires that the points on the arc have to be computed instead of storing the coordinates directly. The control point is the center point of the arc plus the radius and the bearing at start and end. This represenation can be used only in 2D.&lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;complexContent&gt;
 *          &lt;extension base="gml:AbstractCurveSegmentType"&gt;
 *              &lt;sequence&gt;
 *                  &lt;choice&gt;
 *                      &lt;annotation&gt;
 *                          &lt;documentation&gt;GML supports two different ways to specify the control points of a curve segment.
 *  1. A "pos" (DirectPositionType) or "pointProperty" (PointPropertyType) element. The "pos" element contains a center point that is only part of this curve segment, a "pointProperty" element contains a point that may be referenced from other geometry elements or reference another point defined outside of this curve segment (reuse of existing points).
 *  2. The "posList" element can be used to specifiy the coordinates of the center point, too. The number of direct positions in the list must be one.&lt;/documentation&gt;
 *                      &lt;/annotation&gt;
 *                      &lt;choice&gt;
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
 *                  &lt;element name="radius" type="gml:LengthType"&gt;
 *                      &lt;annotation&gt;
 *                          &lt;documentation&gt;The radius of the arc.&lt;/documentation&gt;
 *                      &lt;/annotation&gt;
 *                  &lt;/element&gt;
 *                  &lt;element minOccurs="0" name="startAngle" type="gml:AngleType"&gt;
 *                      &lt;annotation&gt;
 *                          &lt;documentation&gt;The bearing of the arc at the start.&lt;/documentation&gt;
 *                      &lt;/annotation&gt;
 *                  &lt;/element&gt;
 *                  &lt;element minOccurs="0" name="endAngle" type="gml:AngleType"&gt;
 *                      &lt;annotation&gt;
 *                          &lt;documentation&gt;The bearing of the arc at the end.&lt;/documentation&gt;
 *                      &lt;/annotation&gt;
 *                  &lt;/element&gt;
 *              &lt;/sequence&gt;
 *              &lt;attribute fixed="circularArcCenterPointWithRadius"
 *                  name="interpolation" type="gml:CurveInterpolationType"&gt;
 *                  &lt;annotation&gt;
 *                      &lt;documentation&gt;The attribute "interpolation" specifies the curve interpolation mechanism used for this segment. This mechanism
 *  uses the control points and control parameters to determine the position of this curve segment. For an ArcByCenterPoint the interpolation is fixed as "circularArcCenterPointWithRadius".&lt;/documentation&gt;
 *                  &lt;/annotation&gt;
 *              &lt;/attribute&gt;
 *              &lt;attribute fixed="1" name="numArc" type="integer" use="required"&gt;
 *                  &lt;annotation&gt;
 *                      &lt;documentation&gt;Since this type describes always a single arc, the attribute is fixed to "1".&lt;/documentation&gt;
 *                  &lt;/annotation&gt;
 *              &lt;/attribute&gt;
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
public class ArcByCenterPointTypeBinding extends AbstractComplexBinding {
    /**
     * @generated
     */
    public QName getTarget() {
        return GML.ARCBYCENTERPOINTTYPE;
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
