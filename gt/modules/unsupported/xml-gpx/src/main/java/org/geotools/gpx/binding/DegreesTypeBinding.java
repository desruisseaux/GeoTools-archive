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
package org.geotools.gpx.binding;

import java.math.BigDecimal;
import javax.xml.namespace.QName;
import org.geotools.gpx.bean.ObjectFactory;
import org.geotools.xml.*;


/**
 * Binding object for the type http://www.topografix.com/GPX/1/1:degreesType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;xsd:simpleType name="degreesType"&gt;
 *      &lt;xsd:annotation&gt;
 *          &lt;xsd:documentation&gt;
 *                  Used for bearing, heading, course.  Units are decimal degrees, true (not magnetic).
 *            &lt;/xsd:documentation&gt;
 *      &lt;/xsd:annotation&gt;
 *      &lt;xsd:restriction base="xsd:decimal"&gt;
 *          &lt;xsd:minInclusive value="0.0"/&gt;
 *          &lt;xsd:maxExclusive value="360.0"/&gt;
 *      &lt;/xsd:restriction&gt;
 *  &lt;/xsd:simpleType&gt;
 *
 *          </code>
 *         </pre>
 * </p>
 *
 * @generated
 */
public class DegreesTypeBinding extends AbstractSimpleBinding {
    ObjectFactory factory;

    public DegreesTypeBinding(ObjectFactory factory) {
        this.factory = factory;
    }

    /**
     * @generated
     */
    public QName getTarget() {
        return GPX.degreesType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Class getType() {
        return BigDecimal.class;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Object parse(InstanceComponent instance, Object value)
        throws Exception {
        BigDecimal deg = (BigDecimal) value;

        if ((deg.doubleValue() < 0) || (deg.doubleValue() >= 360)) {
            throw new IllegalArgumentException("degree value out of bounds [0..360): " + value);
        }

        return deg;
    }
}
