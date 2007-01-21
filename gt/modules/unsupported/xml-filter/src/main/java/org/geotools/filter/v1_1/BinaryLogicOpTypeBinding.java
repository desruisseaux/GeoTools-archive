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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.QName;
import org.opengis.filter.BinaryComparisonOperator;
import org.opengis.filter.BinaryLogicOperator;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.spatial.BinarySpatialOperator;
import org.geotools.xml.AbstractComplexBinding;
import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;


/**
 * Binding object for the type http://www.opengis.net/ogc:BinaryLogicOpType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;xsd:complexType name="BinaryLogicOpType"&gt;
 *      &lt;xsd:complexContent&gt;
 *          &lt;xsd:extension base="ogc:LogicOpsType"&gt;
 *              &lt;xsd:choice maxOccurs="unbounded" minOccurs="2"&gt;
 *                  &lt;xsd:element ref="ogc:comparisonOps"/&gt;
 *                  &lt;xsd:element ref="ogc:spatialOps"/&gt;
 *                  &lt;xsd:element ref="ogc:logicOps"/&gt;
 *              &lt;/xsd:choice&gt;
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
public class BinaryLogicOpTypeBinding extends AbstractComplexBinding {
    FilterFactory filterfactory;

    public BinaryLogicOpTypeBinding(FilterFactory filterfactory) {
        this.filterfactory = filterfactory;
    }

    /**
     * @generated
     */
    public QName getTarget() {
        return OGC.BinaryLogicOpType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Class getType() {
        return BinaryLogicOperator.class;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Object parse(ElementInstance instance, Node node, Object value)
        throws Exception {
        return null;
    }

    public Object getProperty(Object object, QName name)
        throws Exception {
        BinaryLogicOperator operator = (BinaryLogicOperator) object;

        if (OGC.comparisonOps.equals(name)) {
            List comparison = new ArrayList();

            for (Iterator f = operator.getChildren().iterator(); f.hasNext();) {
                Filter filter = (Filter) f.next();

                if (filter instanceof BinaryComparisonOperator) {
                    comparison.add(filter);
                }
            }

            if (!comparison.isEmpty()) {
                return comparison;
            }
        }

        if (OGC.spatialOps.equals(name)) {
            List spatial = new ArrayList();

            for (Iterator f = operator.getChildren().iterator(); f.hasNext();) {
                Filter filter = (Filter) f.next();

                if (filter instanceof BinarySpatialOperator) {
                    spatial.add(filter);
                }
            }

            if (!spatial.isEmpty()) {
                return spatial;
            }
        }

        if (OGC.logicOps.equals(name)) {
            List logic = new ArrayList();

            for (Iterator f = operator.getChildren().iterator(); f.hasNext();) {
                Filter filter = (Filter) f.next();

                if (filter instanceof BinaryLogicOperator) {
                    logic.add(filter);
                }
            }

            if (!logic.isEmpty()) {
                return logic;
            }
        }

        return null;
    }
}
