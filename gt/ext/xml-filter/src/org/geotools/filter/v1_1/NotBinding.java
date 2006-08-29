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
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.Not;
import javax.xml.namespace.QName;


/**
 * Binding object for the element http://www.opengis.net/ogc:Not.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;xsd:element name="Not" substitutionGroup="ogc:logicOps" type="ogc:UnaryLogicOpType"/&gt;
 *
 *          </code>
 *         </pre>
 * </p>
 *
 * @generated
 */
public class NotBinding extends AbstractComplexBinding {
    FilterFactory filterfactory;

    public NotBinding(FilterFactory filterfactory) {
        this.filterfactory = filterfactory;
    }

    /**
     * @generated
     */
    public QName getTarget() {
        return OGC.NOT;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Class getType() {
        return Not.class;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Object parse(ElementInstance instance, Node node, Object value)
        throws Exception {
        Filter filter = (Filter) node.getChildValue(Filter.class);

        return filterfactory.not(filter);
    }
}
