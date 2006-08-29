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
 * Binding object for the type http://www.opengis.net/gml:BezierType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;complexType name="BezierType"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;Bezier curves are polynomial splines that use Bezier or Bernstein polynomials for interpolation purposes. It is a special case of the B-Spline curve with two knots.&lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;complexContent&gt;
 *          &lt;restriction base="gml:BSplineType"&gt;
 *              &lt;sequence&gt;
 *                  &lt;choice&gt;
 *                      &lt;annotation&gt;
 *                          &lt;documentation&gt;GML supports two different ways to specify the control points of a curve segment.
 *  1. A sequence of "pos" (DirectPositionType) or "pointProperty" (PointPropertyType) elements. "pos" elements are control points that are only part of this curve segment, "pointProperty" elements contain a point that may be referenced from other geometry elements or reference another point defined outside of this curve segment (reuse of existing points).
 *  2. The "posList" element allows for a compact way to specifiy the coordinates of the control points, if all control points are in the same coordinate reference systems and belong to this curve segment only.&lt;/documentation&gt;
 *                      &lt;/annotation&gt;
 *                      &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
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
 *                  &lt;element name="degree" type="nonNegativeInteger"&gt;
 *                      &lt;annotation&gt;
 *                          &lt;documentation&gt;The attribute "degree" shall be the degree of the polynomial used for interpolation in this spline.&lt;/documentation&gt;
 *                      &lt;/annotation&gt;
 *                  &lt;/element&gt;
 *                  &lt;element maxOccurs="2" minOccurs="2" name="knot" type="gml:KnotPropertyType"&gt;
 *                      &lt;annotation&gt;
 *                          &lt;documentation&gt;The property "knot" shall be the sequence of distinct knots used to define the spline basis functions.&lt;/documentation&gt;
 *                      &lt;/annotation&gt;
 *                  &lt;/element&gt;
 *              &lt;/sequence&gt;
 *              &lt;attribute fixed="polynomialSpline" name="interpolation" type="gml:CurveInterpolationType"&gt;
 *                  &lt;annotation&gt;
 *                      &lt;documentation&gt;The attribute "interpolation" specifies the curve interpolation mechanism used for this segment. This mechanism
 *  uses the control points and control parameters to determine the position of this curve segment. For a Bezier the interpolation is fixed as "polynomialSpline".&lt;/documentation&gt;
 *                  &lt;/annotation&gt;
 *              &lt;/attribute&gt;
 *              &lt;attribute fixed="true" name="isPolynomial" type="boolean"&gt;
 *                  &lt;annotation&gt;
 *                      &lt;documentation&gt;The attribute isPolynomial is set to true as this is a polynomial spline.&lt;/documentation&gt;
 *                  &lt;/annotation&gt;
 *              &lt;/attribute&gt;
 *              &lt;attribute name="knotType" type="gml:KnotTypesType" use="prohibited"&gt;
 *                  &lt;annotation&gt;
 *                      &lt;documentation&gt;The property "knotType" is not relevant for Bezier curve segments.&lt;/documentation&gt;
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
public class BezierTypeBinding extends AbstractComplexBinding {
    /**
     * @generated
     */
    public QName getTarget() {
        return GML.BEZIERTYPE;
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
