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
import org.opengis.filter.expression.Expression;
import org.opengis.filter.spatial.DWithin;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.Filters;
import org.geotools.xml.*;


/**
 * Binding object for the element http://www.opengis.net/ogc:DWithin.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;xsd:element name="DWithin" substitutionGroup="ogc:spatialOps" type="ogc:DistanceBufferType"/&gt;
 *
 *          </code>
 *         </pre>
 * </p>
 *
 * @generated
 */
public class DWithinBinding extends AbstractComplexBinding {
    FilterFactory filterfactory;

    public DWithinBinding(FilterFactory filterfactory) {
        this.filterfactory = filterfactory;
    }

    /**
     * @generated
     */
    public QName getTarget() {
        return OGC.DWITHIN;
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
        return DWithin.class;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Object parse(ElementInstance instance, Node node, Object value)
        throws Exception {
        //TODO: units
        Expression[] operands = (Expression[]) value;
        double distance = Filters.asDouble(operands[2]);

        return filterfactory.dwithin(operands[0], operands[1], distance, null);
    }
}
