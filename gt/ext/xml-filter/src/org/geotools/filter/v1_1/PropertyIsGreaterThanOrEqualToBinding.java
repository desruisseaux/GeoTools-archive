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

import org.geotools.xml.*;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.PropertyIsGreaterThanOrEqualTo;
import org.opengis.filter.expression.Expression;
import javax.xml.namespace.QName;


/**
 * Binding object for the element http://www.opengis.net/ogc:PropertyIsGreaterThanOrEqualTo.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;xsd:element name="PropertyIsGreaterThanOrEqualTo"
 *      substitutionGroup="ogc:comparisonOps" type="ogc:BinaryComparisonOpType"/&gt;
 *
 *          </code>
 *         </pre>
 * </p>
 *
 * @generated
 */
public class PropertyIsGreaterThanOrEqualToBinding
    extends AbstractComplexBinding {
    FilterFactory filterfactory;

    public PropertyIsGreaterThanOrEqualToBinding(FilterFactory filterfactory) {
        this.filterfactory = filterfactory;
    }

    /**
     * @generated
     */
    public QName getTarget() {
        return OGC.PROPERTYISGREATERTHANOREQUALTO;
    }

    public int getExecutionMode() {
        return AFTER;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Class getType() {
        return PropertyIsGreaterThanOrEqualTo.class;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Object parse(ElementInstance instance, Node node, Object value)
        throws Exception {
        Expression[] operands = (Expression[]) value;

        return filterfactory.greaterOrEqual(operands[0], operands[1]);
    }
}
