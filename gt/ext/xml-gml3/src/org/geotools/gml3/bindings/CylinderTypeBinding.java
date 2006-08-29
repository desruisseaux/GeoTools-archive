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
 * Binding object for the type http://www.opengis.net/gml:CylinderType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;complexType name="CylinderType"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;A cylinder is a gridded surface given as a
 *     family of circles whose positions vary along a set of parallel
 *     lines, keeping the cross sectional horizontal curves of a
 *     constant shape.
 *     NOTE! Given the same working assumptions as in the previous
 *     note, a Cylinder can be given by two circles, giving us the
 *     control points of the form ((P1, P2, P3),(P4, P5, P6)).&lt;/documentation&gt;
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
public class CylinderTypeBinding extends AbstractComplexBinding {
    /**
     * @generated
     */
    public QName getTarget() {
        return GML.CYLINDERTYPE;
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
