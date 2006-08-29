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
 * Binding object for the type http://www.opengis.net/gml:ArrayAssociationType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;complexType name="ArrayAssociationType"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;A base for derived types used to specify complex types containing an array of objects, by unspecified UML association - either composition or aggregation.  An instance of this type contains elements representing Objects.
 *
 *  Ideally this type would be derived by extension of AssociationType.
 *  However, this leads to a non-deterministic content model, since both the base and the extension have minOccurs="0", and is thus prohibited in XML Schema.&lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;sequence&gt;
 *          &lt;element maxOccurs="unbounded" minOccurs="0" ref="gml:_Object"/&gt;
 *      &lt;/sequence&gt;
 *  &lt;/complexType&gt;
 *
 *          </code>
 *         </pre>
 * </p>
 *
 * @generated
 */
public class ArrayAssociationTypeBinding extends AbstractComplexBinding {
    /**
     * @generated
     */
    public QName getTarget() {
        return GML.ARRAYASSOCIATIONTYPE;
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
