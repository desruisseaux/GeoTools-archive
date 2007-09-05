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
import javax.xml.namespace.QName;
import org.geotools.filter.Expression;
import org.geotools.filter.FilterFactory;
import org.geotools.styling.ColorMapEntry;
import org.geotools.styling.StyleFactory;
import org.geotools.xml.*;


/**
 * Binding object for the element http://www.opengis.net/sld:ColorMapEntry.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;xsd:element name="ColorMapEntry"&gt;
 *      &lt;xsd:complexType&gt;
 *          &lt;xsd:attribute name="color" type="xsd:string" use="required"/&gt;
 *          &lt;xsd:attribute name="opacity" type="xsd:double"/&gt;
 *          &lt;xsd:attribute name="quantity" type="xsd:double"/&gt;
 *          &lt;xsd:attribute name="label" type="xsd:string"/&gt;
 *      &lt;/xsd:complexType&gt;
 *  &lt;/xsd:element&gt;
 *
 *          </code>
 *         </pre>
 * </p>
 *
 * @generated
 */
public class SLDColorMapEntryBinding extends AbstractComplexBinding {
    StyleFactory styleFactory;
    FilterFactory filterFactory;

    public SLDColorMapEntryBinding(StyleFactory styleFactory, FilterFactory filterFactory) {
        this.styleFactory = styleFactory;
        this.filterFactory = filterFactory;
    }

    /**
     * @generated
     */
    public QName getTarget() {
        return SLD.COLORMAPENTRY;
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
        return ColorMapEntry.class;
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
        ColorMapEntry entry = styleFactory.createColorMapEntry();

        Expression color = filterFactory.createLiteralExpression((String) node.getAttributeValue(
                    "color"));
        entry.setColor(color);

        if (node.getAttributeValue("opacity") != null) {
            Double opacity = (Double) node.getAttributeValue("opacity");
            entry.setOpacity(filterFactory.createLiteralExpression(opacity.doubleValue()));
        }

        if (node.getAttributeValue("quantity") != null) {
            Double quantity = (Double) node.getAttributeValue("quantity");
            entry.setQuantity(filterFactory.createLiteralExpression(quantity.doubleValue()));
        }

        if (node.getAttributeValue("label") != null) {
            String label = (String) node.getAttributeValue("label");
            entry.setLabel(label);
        }

        return entry;
    }
}
