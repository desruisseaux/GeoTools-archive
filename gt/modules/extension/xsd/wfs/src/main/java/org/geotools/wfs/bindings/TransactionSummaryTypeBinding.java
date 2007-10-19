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

import net.opengis.wfs.WfsFactory;
import javax.xml.namespace.QName;
import org.geotools.wfs.WFS;
import org.geotools.xml.*;


/**
 * Binding object for the type http://www.opengis.net/wfs:TransactionSummaryType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;xsd:complexType name="TransactionSummaryType"&gt;
 *      &lt;xsd:annotation&gt;
 *          &lt;xsd:documentation xml:lang="en"&gt;
 *              Reports the total number of features affected by some kind
 *              of write action (i.e, insert, update, delete).
 *           &lt;/xsd:documentation&gt;
 *      &lt;/xsd:annotation&gt;
 *      &lt;xsd:sequence&gt;
 *          &lt;xsd:element minOccurs="0" name="totalInserted" type="xsd:nonNegativeInteger"/&gt;
 *          &lt;xsd:element minOccurs="0" name="totalUpdated" type="xsd:nonNegativeInteger"/&gt;
 *          &lt;xsd:element minOccurs="0" name="totalDeleted" type="xsd:nonNegativeInteger"/&gt;
 *      &lt;/xsd:sequence&gt;
 *  &lt;/xsd:complexType&gt;
 *
 *          </code>
 *         </pre>
 * </p>
 *
 * @generated
 */
public class TransactionSummaryTypeBinding extends AbstractComplexEMFBinding {
    public TransactionSummaryTypeBinding(WfsFactory factory) {
        super(factory);
    }

    /**
     * @generated
     */
    public QName getTarget() {
        return WFS.TransactionSummaryType;
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
        //TODO: implement and remove call to super
        return super.parse(instance, node, value);
    }
}
