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
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import javax.xml.namespace.QName;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.identity.FeatureId;
import org.geotools.xml.*;


/**
 * Binding object for the type http://www.opengis.net/ogc:FeatureIdType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;xsd:complexType name="FeatureIdType"&gt;
 *      &lt;xsd:attribute name="fid" type="xsd:anyURI" use="required"/&gt;
 *  &lt;/xsd:complexType&gt;
 *
 *          </code>
 *         </pre>
 * </p>
 *
 * @generated
 */
public class OGCFeatureIdTypeBinding extends AbstractComplexBinding {
    private FilterFactory factory;

    public OGCFeatureIdTypeBinding(FilterFactory factory) {
        this.factory = factory;
    }

    /**
     * @generated
     */
    public QName getTarget() {
        return OGC.FeatureIdType;
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
        return FeatureId.class;
    }

    /**
     * <!-- begin-user-doc -->
     * This will be a good test of xs:anyURI
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
        URI fid = (URI) node.getAttribute("fid").getValue();

        return factory.featureId(fid.toString());
    }

    public Object getProperty(Object object, QName name)
        throws Exception {
        if ("fid".equals(name.getLocalPart())) {
            FeatureId featureId = (FeatureId) object;

            //&lt;xsd:attribute name="fid" type="xsd:anyURI" use="required"/&gt;
            if (featureId != null) {
                return new URI(featureId.getID());
            }
        }

        return null;
    }
}
