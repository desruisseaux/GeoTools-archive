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
 * Binding object for the type http://www.opengis.net/gml:OffsetCurveType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;complexType name="OffsetCurveType"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;An offset curve is a curve at a constant
 *                   distance from the basis curve. They can be useful as a cheap
 *                   and simple alternative to constructing curves that are offsets
 *                   by definition.&lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;complexContent&gt;
 *          &lt;extension base="gml:AbstractCurveSegmentType"&gt;
 *              &lt;sequence&gt;
 *                  &lt;element name="offsetBase" type="gml:CurvePropertyType"&gt;
 *                      &lt;annotation&gt;
 *                          &lt;documentation&gt;offsetBase is a reference to thecurve from which this
 *                                                           curve is define        as an offset.&lt;/documentation&gt;
 *                      &lt;/annotation&gt;
 *                  &lt;/element&gt;
 *                  &lt;element name="distance" type="gml:LengthType"&gt;
 *                      &lt;annotation&gt;
 *                          &lt;documentation&gt;distance is the distance at which the
 *                                                           offset curve is generated from the basis curve. In 2D systems, positive distances
 *                                                           are to be to the left of the basis curve, and the negative distances are to be to the
 *                                                           right of the basis curve.&lt;/documentation&gt;
 *                      &lt;/annotation&gt;
 *                  &lt;/element&gt;
 *                  &lt;element minOccurs="0" name="refDirection" type="gml:VectorType"&gt;
 *                      &lt;annotation&gt;
 *                          &lt;documentation&gt;refDistance is used to define the vector
 *         direction of the offset curve from the basis curve. It can
 *         be omitted in the 2D case, where the distance can be
 *         positive or negative. In that case, distance defines left
 *         side (positive distance) or right side (negative distance)
 *         with respect to the tangent to the basis curve.
 *
 *         In 3D the basis curve shall have a well defined tangent
 *         direction for every point. The offset curve at any point
 *         in 3D, the basis curve shall have a well-defined tangent
 *         direction for every point. The offset curve at any point
 *         (parameter) on the basis curve c is in the direction
 *         -   -   -         -
 *         s = v x t  where  v = c.refDirection()
 *         and
 *         -
 *         t = c.tangent()
 *                                                      -
 *         For the offset direction to be well-defined, v shall not
 *         on any point of the curve be in the same, or opposite,
 *         direction as
 *         -
 *         t.
 *
 *         The default value of the refDirection shall be the local
 *         co-ordinate axis vector for elevation, which indicates up for
 *         the curve in a geographic sense.
 *
 *         NOTE! If the refDirection is the positive tangent to the
 *         local elevation axis ("points upward"), then the offset
 *         vector points to the left of the curve when viewed from
 *         above.&lt;/documentation&gt;
 *                      &lt;/annotation&gt;
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
public class OffsetCurveTypeBinding extends AbstractComplexBinding {
    /**
     * @generated
     */
    public QName getTarget() {
        return GML.OFFSETCURVETYPE;
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
