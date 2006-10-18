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
package org.geotools.filter.v1_1;

import javax.xml.namespace.QName;
import org.opengis.filter.FilterFactory;
import org.geotools.xml.*;


/**
 * Binding object for the type http://www.opengis.net/ogc:SpatialOperatorNameType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;xsd:simpleType name="SpatialOperatorNameType"&gt;
 *      &lt;xsd:restriction base="xsd:string"&gt;
 *          &lt;xsd:enumeration value="BBOX"/&gt;
 *          &lt;xsd:enumeration value="Equals"/&gt;
 *          &lt;xsd:enumeration value="Disjoint"/&gt;
 *          &lt;xsd:enumeration value="Intersects"/&gt;
 *          &lt;xsd:enumeration value="Touches"/&gt;
 *          &lt;xsd:enumeration value="Crosses"/&gt;
 *          &lt;xsd:enumeration value="Within"/&gt;
 *          &lt;xsd:enumeration value="Contains"/&gt;
 *          &lt;xsd:enumeration value="Overlaps"/&gt;
 *          &lt;xsd:enumeration value="Beyond"/&gt;
 *          &lt;xsd:enumeration value="DWithin"/&gt;
 *      &lt;/xsd:restriction&gt;
 *  &lt;/xsd:simpleType&gt;
 *
 *          </code>
 *         </pre>
 * </p>
 *
 * @generated
 */
public class SpatialOperatorNameTypeBinding extends AbstractSimpleBinding {
    FilterFactory filterfactory;

    public SpatialOperatorNameTypeBinding(FilterFactory filterfactory) {
        this.filterfactory = filterfactory;
    }

    /**
     * @generated
     */
    public QName getTarget() {
        return OGC.SPATIALOPERATORNAMETYPE;
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
