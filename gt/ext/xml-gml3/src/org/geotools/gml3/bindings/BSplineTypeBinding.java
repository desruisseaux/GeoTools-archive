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
 * Binding object for the type http://www.opengis.net/gml:BSplineType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;complexType name="BSplineType"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;A B-Spline is a piecewise parametric polynomial or rational curve described in terms of control points and basis functions. Knots are breakpoints on the curve that connect its pieces. They are given as a non-decreasing sequence of real numbers. If the weights in the knots are equal then it is a polynomial spline. The degree is the algebraic degree of the basis functions.&lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;complexContent&gt;
 *          &lt;extension base="gml:AbstractCurveSegmentType"&gt;
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
 *                  &lt;element maxOccurs="unbounded" minOccurs="2" name="knot" type="gml:KnotPropertyType"&gt;
 *                      &lt;annotation&gt;
 *                          &lt;documentation&gt;The property "knot" shall be the sequence of distinct knots used to define the spline basis functions.&lt;/documentation&gt;
 *                      &lt;/annotation&gt;
 *                  &lt;/element&gt;
 *              &lt;/sequence&gt;
 *              &lt;attribute default="polynomialSpline" name="interpolation" type="gml:CurveInterpolationType"&gt;
 *                  &lt;annotation&gt;
 *                      &lt;documentation&gt;The attribute "interpolation" specifies the curve interpolation mechanism used for this segment. This mechanism
 *  uses the control points and control parameters to determine the position of this curve segment. For a BSpline the interpolation can be either "polynomialSpline" or "rationalSpline", default is "polynomialSpline".&lt;/documentation&gt;
 *                  &lt;/annotation&gt;
 *              &lt;/attribute&gt;
 *              &lt;attribute name="isPolynomial" type="boolean" use="optional"&gt;
 *                  &lt;annotation&gt;
 *                      &lt;documentation&gt;The attribute isPolynomial is set to true if this is a polynomial spline.&lt;/documentation&gt;
 *                  &lt;/annotation&gt;
 *              &lt;/attribute&gt;
 *              &lt;attribute name="knotType" type="gml:KnotTypesType" use="optional"&gt;
 *                  &lt;annotation&gt;
 *                      &lt;documentation&gt;The attribute "knotType" gives the type of knot distribution used in defining this spline. This is for information only
 *  and is set according to the different construction-functions.&lt;/documentation&gt;
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
public class BSplineTypeBinding extends AbstractComplexBinding {
    /**
     * @generated
     */
    public QName getTarget() {
        return GML.BSPLINETYPE;
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
