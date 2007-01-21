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
package org.geotools.filter.v1_0;

import org.picocontainer.MutablePicoContainer;
import javax.xml.namespace.QName;
import org.opengis.filter.BinaryLogicOperator;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
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
public class OGCBinaryLogicOpTypeBinding extends AbstractComplexBinding {
    private FilterFactory factory;

    public OGCBinaryLogicOpTypeBinding(FilterFactory factory) {
        this.factory = factory;
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
    public int getExecutionMode() {
        return OVERRIDE;
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
    public void initialize(ElementInstance instance, Node node, MutablePicoContainer context) {
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Object parse(ElementInstance instance, Node node, Object value)
        throws Exception {
        //TODO: replace with element bindings
        Filter f1 = (Filter) node.getChildValue(0);
        Filter f2 = (Filter) node.getChildValue(1);

        String name = instance.getName();

        //		<xsd:element name="And" substitutionGroup="ogc:logicOps" type="ogc:BinaryLogicOpType"/>
        if ("And".equals(name)) {
            return factory.and(f1, f2);
        }
        //		<xsd:element name="Or" substitutionGroup="ogc:logicOps" type="ogc:BinaryLogicOpType"/>
        else if ("Or".equals(name)) {
            return factory.or(f1, f2);
        } else {
            throw new IllegalStateException(name);
        }
    }
}
