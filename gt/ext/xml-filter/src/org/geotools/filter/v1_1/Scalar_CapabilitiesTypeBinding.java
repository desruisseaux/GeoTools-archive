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
 * Binding object for the type http://www.opengis.net/ogc:Scalar_CapabilitiesType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;xsd:complexType name="Scalar_CapabilitiesType"&gt;
 *      &lt;xsd:sequence&gt;
 *          &lt;xsd:element maxOccurs="1" minOccurs="0" ref="ogc:LogicalOperators"/&gt;
 *          &lt;xsd:element maxOccurs="1" minOccurs="0"
 *              name="ComparisonOperators" type="ogc:ComparisonOperatorsType"/&gt;
 *          &lt;xsd:element maxOccurs="1" minOccurs="0"
 *              name="ArithmeticOperators" type="ogc:ArithmeticOperatorsType"/&gt;
 *      &lt;/xsd:sequence&gt;
 *  &lt;/xsd:complexType&gt;
 *
 *          </code>
 *         </pre>
 * </p>
 *
 * @generated
 */
public class Scalar_CapabilitiesTypeBinding extends AbstractComplexBinding {
    FilterFactory filterfactory;

    public Scalar_CapabilitiesTypeBinding(FilterFactory filterfactory) {
        this.filterfactory = filterfactory;
    }

    /**
     * @generated
     */
    public QName getTarget() {
        return OGC.SCALAR_CAPABILITIESTYPE;
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
