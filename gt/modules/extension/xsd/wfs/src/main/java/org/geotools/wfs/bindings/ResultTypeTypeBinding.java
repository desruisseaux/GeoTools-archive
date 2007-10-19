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
package org.geotools.wfs.bindings;

import net.opengis.wfs.WfsFactory;
import javax.xml.namespace.QName;
import org.geotools.wfs.WFS;
import org.geotools.xml.*;


/**
 * Binding object for the type http://www.opengis.net/wfs:ResultTypeType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;xsd:simpleType name="ResultTypeType"&gt;
 *      &lt;xsd:restriction base="xsd:string"&gt;
 *          &lt;xsd:enumeration value="results"&gt;
 *              &lt;xsd:annotation&gt;
 *                  &lt;xsd:documentation&gt;
 *                    Indicates that a complete response should be generated
 *                    by the WFS.  That is, all response feature instances
 *                    should be returned to the client.
 *                 &lt;/xsd:documentation&gt;
 *              &lt;/xsd:annotation&gt;
 *          &lt;/xsd:enumeration&gt;
 *          &lt;xsd:enumeration value="hits"&gt;
 *              &lt;xsd:annotation&gt;
 *                  &lt;xsd:documentation&gt;
 *                    Indicates that an empty response should be generated with
 *                    the "numberOfFeatures" attribute set (i.e. no feature
 *                    instances should be returned).  In this manner a client may
 *                    determine the number of feature instances that a GetFeature
 *                    request will return without having to actually get the
 *                    entire result set back.
 *                 &lt;/xsd:documentation&gt;
 *              &lt;/xsd:annotation&gt;
 *          &lt;/xsd:enumeration&gt;
 *      &lt;/xsd:restriction&gt;
 *  &lt;/xsd:simpleType&gt;
 *
 *          </code>
 *         </pre>
 * </p>
 *
 * @generated
 */
public class ResultTypeTypeBinding extends AbstractSimpleBinding {
    public ResultTypeTypeBinding(WfsFactory factory) {
    }

    /**
     * @generated
     */
    public QName getTarget() {
        return WFS.ResultTypeType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Class getType() {
        return null;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Object parse(InstanceComponent instance, Object value)
        throws Exception {
        //TODO: implement and remove call to super
        return super.parse(instance, value);
    }
}
