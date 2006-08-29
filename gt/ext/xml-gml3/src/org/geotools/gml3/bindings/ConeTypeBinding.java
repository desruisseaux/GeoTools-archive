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
 * Binding object for the type http://www.opengis.net/gml:ConeType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;complexType name="ConeType"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;A cone is a gridded surface given as a
 *     family of conic sections whose control points vary linearly.
 *     NOTE! A 5-point ellipse with all defining positions identical
 *     is a point. Thus, a truncated elliptical cone can be given as a
 *     2x5 set of control points
 *     ((P1, P1, P1, P1, P1), (P2, P3, P4, P5, P6)). P1 is the apex
 *     of the cone. P2, P3,P4, P5 and P6 are any five distinct points
 *     around the base ellipse of the cone. If the horizontal curves
 *     are circles as opposed to ellipses, the a circular cone can
 *     be constructed using ((P1, P1, P1),(P2, P3, P4)). The apex most
 *     not coinside with the other plane.&lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;complexContent&gt;
 *          &lt;extension base="gml:AbstractGriddedSurfaceType"&gt;
 *              &lt;attribute fixed="circularArc3Points"
 *                  name="horizontalCurveType" type="gml:CurveInterpolationType"/&gt;
 *              &lt;attribute fixed="linear" name="verticalCurveType" type="gml:CurveInterpolationType"/&gt;
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
public class ConeTypeBinding extends AbstractComplexBinding {
    /**
     * @generated
     */
    public QName getTarget() {
        return GML.CONETYPE;
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
