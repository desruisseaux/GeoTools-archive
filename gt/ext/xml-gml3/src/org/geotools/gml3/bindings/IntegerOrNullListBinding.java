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
 * Binding object for the type http://www.opengis.net/gml:integerOrNullList.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;simpleType name="integerOrNullList"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;XML List based on the union type defined above.  An element declared with this type contains a space-separated list of integer values with null values interspersed as needed&lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;list itemType="gml:integerOrNull"/&gt;
 *  &lt;/simpleType&gt;
 *
 *          </code>
 *         </pre>
 * </p>
 *
 * @generated
 */
public class IntegerOrNullListBinding extends AbstractSimpleBinding {
    /**
     * @generated
     */
    public QName getTarget() {
        return GML.INTEGERORNULLLIST;
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
