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
 * Binding object for the type http://www.opengis.net/gml:SphereType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;complexType name="SphereType"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;A sphere is a gridded surface given as a
 *     family of circles whose positions vary linearly along the
 *     axis of the sphere, and whise radius varies in proportions to
 *     the cosine function of the central angle. The horizontal
 *     circles resemble lines of constant latitude, and the vertical
 *     arcs resemble lines of constant longitude.
 *     NOTE! If the control points are sorted in terms of increasing
 *     longitude, and increasing latitude, the upNormal of a sphere
 *     is the outward normal.
 *     EXAMPLE If we take a gridded set of latitudes and longitudes
 *     in degrees,(u,v) such as
 *
 *          (-90,-180)  (-90,-90)  (-90,0)  (-90,  90) (-90, 180)
 *          (-45,-180)  (-45,-90)  (-45,0)  (-45,  90) (-45, 180)
 *          (  0,-180)  (  0,-90)  (  0,0)  (  0,  90) (  0, 180)
 *          ( 45,-180)  ( 45,-90)  ( 45,0)  ( 45, -90) ( 45, 180)
 *          ( 90,-180)  ( 90,-90)  ( 90,0)  ( 90, -90) ( 90, 180)
 *
 *     And map these points to 3D using the usual equations (where R
 *     is the radius of the required sphere).
 *
 *      z = R sin u
 *      x = (R cos u)(sin v)
 *      y = (R cos u)(cos v)
 *
 *     We have a sphere of Radius R, centred at (0,0), as a gridded
 *     surface. Notice that the entire first row and the entire last
 *     row of the control points map to a single point in each 3D
 *     Euclidean space, North and South poles respectively, and that
 *     each horizontal curve closes back on itself forming a
 *     geometric cycle. This gives us a metrically bounded (of finite
 *     size), topologically unbounded (not having a boundary, a
 *     cycle) surface.&lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;complexContent&gt;
 *          &lt;extension base="gml:AbstractGriddedSurfaceType"&gt;
 *              &lt;attribute fixed="circularArc3Points"
 *                  name="horizontalCurveType" type="gml:CurveInterpolationType"/&gt;
 *              &lt;attribute fixed="circularArc3Points"
 *                  name="verticalCurveType" type="gml:CurveInterpolationType"/&gt;
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
public class SphereTypeBinding extends AbstractComplexBinding {
    /**
     * @generated
     */
    public QName getTarget() {
        return GML.SPHERETYPE;
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
