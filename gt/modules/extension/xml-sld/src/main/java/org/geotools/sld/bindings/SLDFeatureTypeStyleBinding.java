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
import java.util.List;
import javax.xml.namespace.QName;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Rule;
import org.geotools.styling.StyleFactory;
import org.geotools.xml.*;


/**
 * Binding object for the element http://www.opengis.net/sld:FeatureTypeStyle.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;xsd:element name="FeatureTypeStyle"&gt;
 *      &lt;xsd:annotation&gt;
 *          &lt;xsd:documentation&gt;       A FeatureTypeStyle contains styling
 *              information specific to one       feature type.  This is the
 *              SLD level that separates the &apos;layer&apos;
 *              handling from the &apos;feature&apos; handling.     &lt;/xsd:documentation&gt;
 *      &lt;/xsd:annotation&gt;
 *      &lt;xsd:complexType&gt;
 *          &lt;xsd:sequence&gt;
 *              &lt;xsd:element ref="sld:Name" minOccurs="0"/&gt;
 *              &lt;xsd:element ref="sld:Title" minOccurs="0"/&gt;
 *              &lt;xsd:element ref="sld:Abstract" minOccurs="0"/&gt;
 *              &lt;xsd:element ref="sld:FeatureTypeName" minOccurs="0"/&gt;
 *              &lt;xsd:element ref="sld:SemanticTypeIdentifier" minOccurs="0" maxOccurs="unbounded"/&gt;
 *              &lt;xsd:element ref="sld:Rule" maxOccurs="unbounded"/&gt;
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
public class SLDFeatureTypeStyleBinding implements ComplexBinding {
    StyleFactory styleFactory;

    public SLDFeatureTypeStyleBinding(StyleFactory styleFactory) {
        this.styleFactory = styleFactory;
    }

    /**
     * @generated
     */
    public QName getTarget() {
        return SLD.FEATURETYPESTYLE;
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
        return FeatureTypeStyle.class;
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
        FeatureTypeStyle featureTypeStyle = styleFactory.createFeatureTypeStyle();

        //&lt;xsd:element ref="sld:Name" minOccurs="0"/&gt;
        if (node.hasChild("Name")) {
            featureTypeStyle.setName((String) node.getChildValue("Name"));
        }

        //&lt;xsd:element ref="sld:Title" minOccurs="0"/&gt;
        if (node.hasChild("Title")) {
            featureTypeStyle.setTitle((String) node.getChildValue("Title"));
        }

        //&lt;xsd:element ref="sld:Abstract" minOccurs="0"/&gt;
        if (node.hasChild("Abstract")) {
            featureTypeStyle.setAbstract((String) node.getChildValue("Abstract"));
        }

        //&lt;xsd:element ref="sld:FeatureTypeName" minOccurs="0"/&gt;
        if (node.hasChild("FeatureTypeName")) {
            featureTypeStyle.setFeatureTypeName((String) node.getChildValue("FeatureTypeName"));
        }

        //&lt;xsd:element ref="sld:SemanticTypeIdentifier" minOccurs="0" maxOccurs="unbounded"/&gt;
        if (node.hasChild("SemanticTypeIdentifier")) {
            List ids = node.getChildValues("SemanticTypeIdentifier");
            featureTypeStyle.setSemanticTypeIdentifiers((String[]) ids.toArray(
                    new String[ids.size()]));
        }

        //&lt;xsd:element ref="sld:Rule" maxOccurs="unbounded"/&gt;
        if (node.hasChild("Rule")) {
            List rules = node.getChildValues("Rule");
            featureTypeStyle.setRules((Rule[]) rules.toArray(new Rule[rules.size()]));
        }

        return featureTypeStyle;
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
