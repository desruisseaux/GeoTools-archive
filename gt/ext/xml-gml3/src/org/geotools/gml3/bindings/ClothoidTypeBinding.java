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
 * Binding object for the type http://www.opengis.net/gml:ClothoidType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;complexType name="ClothoidType"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;A clothoid, or Cornu's spiral, is plane
 *     curve whose curvature is a fixed function of its length.
 *     In suitably chosen co-ordinates it is given by Fresnel's
 *     integrals.
 *
 *      x(t) = 0-integral-t cos(AT*T/2)dT
 *
 *      y(t) = 0-integral-t sin(AT*T/2)dT
 *
 *     This geometry is mainly used as a transition curve between
 *     curves of type straight line to circular arc or circular arc
 *     to circular arc. With this curve type it is possible to
 *     achieve a C2-continous transition between the above mentioned
 *     curve types. One formula for the Clothoid is A*A = R*t where
 *     A is constant, R is the varying radius of curvature along the
 *     the curve and t is the length along and given in the Fresnel
 *     integrals.&lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;complexContent&gt;
 *          &lt;extension base="gml:AbstractCurveSegmentType"&gt;
 *              &lt;sequence&gt;
 *                  &lt;element name="refLocation"&gt;
 *                      &lt;complexType&gt;
 *                          &lt;sequence&gt;
 *                              &lt;element ref="gml:AffinePlacement"&gt;
 *                                  &lt;annotation&gt;
 *                                      &lt;documentation&gt;The "refLocation" is an affine mapping
 *            that places  the curve defined by the Fresnel Integrals
 *            into the co-ordinate reference system of this object.&lt;/documentation&gt;
 *                                  &lt;/annotation&gt;
 *                              &lt;/element&gt;
 *                          &lt;/sequence&gt;
 *                      &lt;/complexType&gt;
 *                  &lt;/element&gt;
 *                  &lt;element name="scaleFactor" type="decimal"&gt;
 *                      &lt;annotation&gt;
 *                          &lt;documentation&gt;The element gives the value for the
 *         constant in the Fresnel's integrals.&lt;/documentation&gt;
 *                      &lt;/annotation&gt;
 *                  &lt;/element&gt;
 *                  &lt;element name="startParameter" type="double"&gt;
 *                      &lt;annotation&gt;
 *                          &lt;documentation&gt;The startParameter is the arc length
 *         distance from the inflection point that will be the start
 *         point for this curve segment. This shall be lower limit
 *         used in the Fresnel integral and is the value of the
 *         constructive parameter of this curve segment at its start
 *         point. The startParameter can either be positive or
 *         negative.
 *         NOTE! If 0.0 (zero), lies between the startParameter and
 *         the endParameter of the clothoid, then the curve goes
 *         through the clothoid's inflection point, and the direction
 *         of its radius of curvature, given by the second
 *         derivative vector, changes sides with respect to the
 *         tangent vector. The term length distance for the&lt;/documentation&gt;
 *                      &lt;/annotation&gt;
 *                  &lt;/element&gt;
 *                  &lt;element name="endParameter" type="double"&gt;
 *                      &lt;annotation&gt;
 *                          &lt;documentation&gt;The endParameter is the arc length
 *         distance from the inflection point that will be the end
 *         point for this curve segment. This shall be upper limit
 *         used in the Fresnel integral and is the value of the
 *         constructive parameter of this curve segment at its
 *         start point. The startParameter can either be positive
 *         or negative.&lt;/documentation&gt;
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
public class ClothoidTypeBinding extends AbstractComplexBinding {
    /**
     * @generated
     */
    public QName getTarget() {
        return GML.CLOTHOIDTYPE;
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
