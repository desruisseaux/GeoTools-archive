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
import java.util.List;
import javax.xml.namespace.QName;
import org.geotools.filter.Expression;
import org.geotools.sld.CssParameter;
import org.geotools.styling.Fill;
import org.geotools.styling.Graphic;
import org.geotools.styling.StyleFactory;
import org.geotools.xml.ComplexBinding;
import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;


/**
 * Binding object for the element http://www.opengis.net/sld:Fill.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;xsd:element name="Fill"&gt;
 *      &lt;xsd:annotation&gt;
 *          &lt;xsd:documentation&gt;         A &quot;Fill&quot; specifies the
 *              pattern for filling an area geometry.         The allowed
 *              CssParameters are: &quot;fill&quot; (color) and
 *              &quot;fill-opacity&quot;.       &lt;/xsd:documentation&gt;
 *      &lt;/xsd:annotation&gt;
 *      &lt;xsd:complexType&gt;
 *          &lt;xsd:sequence&gt;
 *              &lt;xsd:element ref="sld:GraphicFill" minOccurs="0"/&gt;
 *              &lt;xsd:element ref="sld:CssParameter" minOccurs="0" maxOccurs="unbounded"/&gt;
 *          &lt;/xsd:sequence&gt;
 *      &lt;/xsd:complexType&gt;
 *  &lt;/xsd:element&gt;
 *
 *          </code>
 *         </pre>
 * </p>
 *
 * @generated
 */
public class SLDFillBinding implements ComplexBinding {
    StyleFactory styleFactory;

    public SLDFillBinding(StyleFactory styleFactory) {
        this.styleFactory = styleFactory;
    }

    /**
     * @generated
     */
    public QName getTarget() {
        return SLD.FILL;
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
        return Fill.class;
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
        Expression color = null;
        Expression bgColor = null;
        Expression opacity = null;
        Graphic graphicFill = null;

        graphicFill = (Graphic) node.getChildValue("GraphicFill");

        List params = node.getChildValues("CssParameter");

        for (Iterator itr = params.iterator(); itr.hasNext();) {
            CssParameter param = (CssParameter) itr.next();

            if ("color".equals(param.getName())) {
                color = (Expression) param.getExpressions().get(0);
            }

            if ("background-color".equals(param.getName())) {
                bgColor = (Expression) param.getExpressions().get(0);
            }

            if ("opacity".equals(param.getName())) {
                opacity = (Expression) param.getExpressions().get(0);
            }
        }

        Fill fill = styleFactory.createFill(color);

        if (bgColor != null) {
            fill.setBackgroundColor(bgColor);
        }

        if (opacity != null) {
            fill.setOpacity(opacity);
        }

        return fill;
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
