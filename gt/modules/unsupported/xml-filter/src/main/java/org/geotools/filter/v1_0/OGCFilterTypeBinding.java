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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.xml.namespace.QName;
import org.opengis.filter.FeatureId;
import org.opengis.filter.FilterFactory2;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.xml.ComplexBinding;
import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;


/**
 * Binding object for the type http://www.opengis.net/ogc:FilterType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;xsd:complexType name="FilterType"&gt;
 *      &lt;xsd:choice&gt;
 *          &lt;xsd:element ref="ogc:spatialOps"/&gt;
 *          &lt;xsd:element ref="ogc:comparisonOps"/&gt;
 *          &lt;xsd:element ref="ogc:logicOps"/&gt;
 *          &lt;xsd:element maxOccurs="unbounded" ref="ogc:FeatureId"/&gt;
 *      &lt;/xsd:choice&gt;
 *  &lt;/xsd:complexType&gt;
 *
 *          </code>
 *         </pre>
 * </p>
 *
 * @generated
 */
public class OGCFilterTypeBinding implements ComplexBinding {
    FilterFactory factory;

    public OGCFilterTypeBinding(FilterFactory factory) {
        this.factory = factory;
    }

    /**
     * @generated
     */
    public QName getTarget() {
        return OGC.FilterType;
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
        return Filter.class;
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
     * Surprised we actually have something to do: namely collapse multiple fid filters using AND
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Object parse(ElementInstance instance, Node node, Object value)
        throws Exception {
        if (node.getChildren().size() == 1) {
            return node.getChildValue(0); // return directly ...
        }

        // we must be a mess of FidFilters, concatenate
        Set fids = new HashSet();

        for (Iterator i = node.getChildren().iterator(); i.hasNext();) {
            Node child = (Node) i.next();
            FeatureId fid = (FeatureId) child.getValue();

            if ((fid != null) && (fid.getIDs() != null)) {
                fids.addAll(fid.getIDs());
            }
        }

        return factory.featureId(fids);
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
