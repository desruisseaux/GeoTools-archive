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
package org.geotools.wfs.bindings;

import net.opengis.wfs.AllSomeType;
import net.opengis.wfs.WfsFactory;
import javax.xml.namespace.QName;
import org.geotools.wfs.WFS;
import org.geotools.xml.*;


/**
 * Binding object for the type http://www.opengis.net/wfs:AllSomeType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;xsd:simpleType name="AllSomeType"&gt;
 *      &lt;xsd:restriction base="xsd:string"&gt;
 *          &lt;xsd:enumeration value="ALL"/&gt;
 *          &lt;xsd:enumeration value="SOME"/&gt;
 *      &lt;/xsd:restriction&gt;
 *  &lt;/xsd:simpleType&gt;
 *
 *          </code>
 *         </pre>
 * </p>
 *
 * @generated
 */
public class AllSomeTypeBinding extends AbstractSimpleBinding {
    public AllSomeTypeBinding(WfsFactory factory) {
    }

    /**
     * @generated
     */
    public QName getTarget() {
        return WFS.AllSomeType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Class getType() {
        return AllSomeType.class;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Object parse(InstanceComponent instance, Object value)
        throws Exception {
        //TODO: implement and remove call to super
        return super.parse(instance, value);
    }
}
