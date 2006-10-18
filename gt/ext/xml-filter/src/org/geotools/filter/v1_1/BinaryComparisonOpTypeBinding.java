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
import org.opengis.filter.expression.Expression;
import org.geotools.xml.*;


/**
 * Binding object for the type http://www.opengis.net/ogc:BinaryComparisonOpType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;xsd:complexType name="BinaryComparisonOpType"&gt;
 *      &lt;xsd:complexContent&gt;
 *          &lt;xsd:extension base="ogc:ComparisonOpsType"&gt;
 *              &lt;xsd:sequence&gt;
 *                  &lt;xsd:element maxOccurs="2" minOccurs="2" ref="ogc:expression"/&gt;
 *              &lt;/xsd:sequence&gt;
 *              &lt;xsd:attribute default="true" name="matchCase"
 *                  type="xsd:boolean" use="optional"/&gt;
 *          &lt;/xsd:extension&gt;
 *      &lt;/xsd:complexContent&gt;
 *  &lt;/xsd:complexType&gt;
 *
 *          </code>
 *         </pre>
 * </p>
 *
 * @generated
 */
public class BinaryComparisonOpTypeBinding extends AbstractComplexBinding {
    FilterFactory filterfactory;

    public BinaryComparisonOpTypeBinding(FilterFactory filterfactory) {
        this.filterfactory = filterfactory;
    }

    /**
     * @generated
     */
    public QName getTarget() {
        return OGC.BINARYCOMPARISONOPTYPE;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Class getType() {
        return Expression[].class;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Object parse(ElementInstance instance, Node node, Object value)
        throws Exception {
        Expression e1 = (Expression) node.getChildValue(0);
        Expression e2 = (Expression) node.getChildValue(1);

        return new Expression[] { e1, e2 };
    }
}
