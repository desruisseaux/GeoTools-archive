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
package org.geotools.gpx.binding;

import java.math.BigDecimal;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import org.geotools.gpx.bean.ObjectFactory;
import org.geotools.gpx.bean.PtType;
import org.geotools.xml.*;


/**
 * Binding object for the type http://www.topografix.com/GPX/1/1:ptType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;xsd:complexType name="ptType"&gt;
 *      &lt;xsd:annotation&gt;
 *          &lt;xsd:documentation&gt;
 *           A geographic point with optional elevation and time.  Available for use by other schemas.
 *      &lt;/xsd:documentation&gt;
 *      &lt;/xsd:annotation&gt;
 *      &lt;xsd:sequence&gt;
 *          &lt;!-- elements must appear in this order --&gt;
 *          &lt;xsd:element minOccurs="0" name="ele" type="xsd:decimal"&gt;
 *              &lt;xsd:annotation&gt;
 *                  &lt;xsd:documentation&gt;
 *                  The elevation (in meters) of the point.
 *            &lt;/xsd:documentation&gt;
 *              &lt;/xsd:annotation&gt;
 *          &lt;/xsd:element&gt;
 *          &lt;xsd:element minOccurs="0" name="time" type="xsd:dateTime"&gt;
 *              &lt;xsd:annotation&gt;
 *                  &lt;xsd:documentation&gt;
 *                  The time that the point was recorded.
 *            &lt;/xsd:documentation&gt;
 *              &lt;/xsd:annotation&gt;
 *          &lt;/xsd:element&gt;
 *      &lt;/xsd:sequence&gt;
 *      &lt;xsd:attribute name="lat" type="latitudeType" use="required"&gt;
 *          &lt;xsd:annotation&gt;
 *              &lt;xsd:documentation&gt;
 *                  The latitude of the point.  Decimal degrees, WGS84 datum.
 *            &lt;/xsd:documentation&gt;
 *          &lt;/xsd:annotation&gt;
 *      &lt;/xsd:attribute&gt;
 *      &lt;xsd:attribute name="lon" type="longitudeType" use="required"&gt;
 *          &lt;xsd:annotation&gt;
 *              &lt;xsd:documentation&gt;
 *                  The latitude of the point.  Decimal degrees, WGS84 datum.
 *            &lt;/xsd:documentation&gt;
 *          &lt;/xsd:annotation&gt;
 *      &lt;/xsd:attribute&gt;
 *  &lt;/xsd:complexType&gt;
 *
 *          </code>
 *         </pre>
 * </p>
 *
 * @generated
 */
public class PtTypeBinding extends AbstractComplexBinding {
    ObjectFactory factory;

    public PtTypeBinding(ObjectFactory factory) {
        this.factory = factory;
    }

    /**
     * @generated
     */
    public QName getTarget() {
        return GPX.ptType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Class getType() {
        return PtType.class;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Object parse(ElementInstance instance, Node node, Object value)
        throws Exception {
        PtType point = factory.createPtType();

        point.setLon((BigDecimal) node.getAttribute("lon"));
        point.setLat((BigDecimal) node.getAttribute("lat"));
        point.setEle((BigDecimal) node.getChildValue("ele"));
        point.setTime((XMLGregorianCalendar) node.getChildValue("time"));

        return point;
    }
}
