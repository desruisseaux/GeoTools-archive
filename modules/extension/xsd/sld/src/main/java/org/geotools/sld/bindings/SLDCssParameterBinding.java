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
package org.geotools.sld.bindings;

import org.picocontainer.MutablePicoContainer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.util.Iterator;
import javax.xml.namespace.QName;
import org.geotools.filter.Expression;
import org.geotools.filter.FilterFactory;
import org.geotools.sld.CssParameter;
import org.geotools.xml.*;


/**
 * Binding object for the element http://www.opengis.net/sld:CssParameter.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;xsd:element name="CssParameter"&gt;
 *      &lt;xsd:annotation&gt;
 *          &lt;xsd:documentation&gt;         A &quot;CssParameter&quot; refers to
 *              an SVG/CSS graphical-formatting         parameter.  The
 *              parameter is identified using the &quot;name&quot; attribute
 *              and the content of the element gives the SVG/CSS-coded
 *              value.       &lt;/xsd:documentation&gt;
 *      &lt;/xsd:annotation&gt;
 *      &lt;xsd:complexType mixed="true"&gt;
 *          &lt;xsd:complexContent mixed="true"&gt;
 *              &lt;xsd:extension base="sld:ParameterValueType"&gt;
 *                  &lt;xsd:attribute name="name" type="xsd:string" use="required"/&gt;
 *              &lt;/xsd:extension&gt;
 *          &lt;/xsd:complexContent&gt;
 *      &lt;/xsd:complexType&gt;
 *  &lt;/xsd:element&gt;
 *
 *          </code>
 *         </pre>
 * </p>
 *
 * @generated
 */
public class SLDCssParameterBinding extends AbstractComplexBinding {
    FilterFactory filterFactory;

    public SLDCssParameterBinding(FilterFactory filterFactory) {
        this.filterFactory = filterFactory;
    }

    /**
     * @generated
     */
    public QName getTarget() {
        return SLD.CSSPARAMETER;
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
        return CssParameter.class;
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
        CssParameter parameter = new CssParameter((String) node.getAttributeValue("name"));

        for (Iterator itr = node.getChildren().iterator(); itr.hasNext();) {
            Node child = (Node) itr.next();

            if (child.getValue() instanceof Expression) {
                parameter.getExpressions().add(child.getValue());
            }
        }

        String text = instance.getText();

        if ((text != null) && !"".equals(text)) {
            Expression exp = filterFactory.createLiteralExpression(text);

            if (exp != null) {
                parameter.getExpressions().add(exp);
            }
        }

        return parameter;
    }
}
