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
 * Binding object for the type http://www.opengis.net/gml:KnotTypesType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;simpleType name="KnotTypesType"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;Defines allowed values for the knots` type. Uniform knots implies that all knots are of multiplicity 1 and they differ by a positive constant from the preceding knot. Knots are quasi-uniform iff they are of multiplicity (degree + 1) at the ends, of multiplicity 1 elsewhere, and they differ by a positive constant from the preceding knot.&lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;restriction base="string"&gt;
 *          &lt;enumeration value="uniform"/&gt;
 *          &lt;enumeration value="quasiUniform"/&gt;
 *          &lt;enumeration value="piecewiseBezier"/&gt;
 *      &lt;/restriction&gt;
 *  &lt;/simpleType&gt;
 *
 *          </code>
 *         </pre>
 * </p>
 *
 * @generated
 */
public class KnotTypesTypeBinding extends AbstractSimpleBinding {
    /**
     * @generated
     */
    public QName getTarget() {
        return GML.KNOTTYPESTYPE;
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
