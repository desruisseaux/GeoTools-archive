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
 * Binding object for the type http://www.opengis.net/gml:NullType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;simpleType name="NullType"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;Utility type for null elements.  The value may be selected from one of the enumerated tokens, or may be a URI in which case this should identify a resource which describes the reason for the null. &lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;union memberTypes="gml:NullEnumeration anyURI"/&gt;
 *  &lt;/simpleType&gt;
 *
 *          </code>
 *         </pre>
 * </p>
 *
 * @generated
 */
public class NullTypeBinding extends AbstractSimpleBinding {
    /**
     * @generated
     */
    public QName getTarget() {
        return GML.NULLTYPE;
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
