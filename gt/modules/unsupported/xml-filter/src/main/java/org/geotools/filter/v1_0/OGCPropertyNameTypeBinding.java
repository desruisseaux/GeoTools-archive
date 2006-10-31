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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.namespace.QName;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.PropertyName;
import org.geotools.filter.Filters;
import org.geotools.xml.*;


/**
 * Binding object for the type http://www.opengis.net/ogc:PropertyNameType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;xsd:complexType name="PropertyNameType"&gt;
 *      &lt;xsd:complexContent mixed="true"&gt;
 *          &lt;xsd:extension base="ogc:ExpressionType"/&gt;
 *      &lt;/xsd:complexContent&gt;
 *  &lt;/xsd:complexType&gt;
 *
 *          </code>
 *         </pre>
 * </p>
 *
 * @generated
 */
public class OGCPropertyNameTypeBinding implements ComplexBinding {
    FilterFactory factory;

    public OGCPropertyNameTypeBinding(FilterFactory factory) {
        this.factory = factory;
    }

    /**
     * @generated
     */
    public QName getTarget() {
        return OGC.PropertyNameType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
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
        return PropertyName.class;
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
        Expression expression = (Expression) value;
        String xpath = Filters.asString(expression);

        //strip off namespace prefix.
        //Our feature model does not suppor tthe notion of namespace for attributes
        // and there is a lot of code that expects this to be a non-qualified name.
        // Also, the PropertyName interface doesn't support a namespace
        if ((xpath != null) && (xpath.indexOf(":") != -1)) {
            xpath = xpath.substring(xpath.indexOf(":") + 1);
        }

        return factory.property(xpath);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public void encode(Object object, Element element, Document document) {
        //TODO: implement
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Object getChild(Object object, QName name) {
        //TODO: implement
        return null;
    }
}
