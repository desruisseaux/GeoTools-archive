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
 * Binding object for the type http://www.opengis.net/gml:SurfaceInterpolationType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;simpleType name="SurfaceInterpolationType"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;SurfaceInterpolationType is a list of codes that may be used to identify the interpolation mechanisms specified by an
 *  application schema.&lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;restriction base="string"&gt;
 *          &lt;enumeration value="none"/&gt;
 *          &lt;enumeration value="planar"/&gt;
 *          &lt;enumeration value="spherical"/&gt;
 *          &lt;enumeration value="elliptical"/&gt;
 *          &lt;enumeration value="conic"/&gt;
 *          &lt;enumeration value="tin"/&gt;
 *          &lt;enumeration value="parametricCurve"/&gt;
 *          &lt;enumeration value="polynomialSpline"/&gt;
 *          &lt;enumeration value="rationalSpline"/&gt;
 *          &lt;enumeration value="triangulatedSpline"/&gt;
 *      &lt;/restriction&gt;
 *  &lt;/simpleType&gt;
 *
 *          </code>
 *         </pre>
 * </p>
 *
 * @generated
 */
public class SurfaceInterpolationTypeBinding extends AbstractSimpleBinding {
    /**
     * @generated
     */
    public QName getTarget() {
        return GML.SURFACEINTERPOLATIONTYPE;
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
    public Object parse(InstanceComponent instance, Object value)
        throws Exception {
        //TODO: implement
        return null;
    }
}
