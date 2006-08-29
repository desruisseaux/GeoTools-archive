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
package org.geotools.gml3.bindings;

import org.geotools.xml.*;
import javax.xml.namespace.QName;


/**
 * Binding object for the type http://www.opengis.net/gml:DegreesType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;complexType name="DegreesType"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;Integer number of degrees, plus the angle direction. This element can be used for geographic Latitude and Longitude. For Latitude, the XML attribute direction can take the values "N" or "S", meaning North or South of the equator. For Longitude, direction can take the values "E" or "W", meaning East or West of the prime meridian. This element can also be used for other angles. In that case, the direction can take the values "+" or "-" (of SignType), in the specified rotational direction from a specified reference direction.&lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;simpleContent&gt;
 *          &lt;extension base="gml:DegreeValueType"&gt;
 *              &lt;attribute name="direction"&gt;
 *                  &lt;simpleType&gt;
 *                      &lt;union&gt;
 *                          &lt;simpleType&gt;
 *                              &lt;restriction base="string"&gt;
 *                                  &lt;enumeration value="N"/&gt;
 *                                  &lt;enumeration value="E"/&gt;
 *                                  &lt;enumeration value="S"/&gt;
 *                                  &lt;enumeration value="W"/&gt;
 *                              &lt;/restriction&gt;
 *                          &lt;/simpleType&gt;
 *                          &lt;simpleType&gt;
 *                              &lt;restriction base="gml:SignType"/&gt;
 *                          &lt;/simpleType&gt;
 *                      &lt;/union&gt;
 *                  &lt;/simpleType&gt;
 *              &lt;/attribute&gt;
 *          &lt;/extension&gt;
 *      &lt;/simpleContent&gt;
 *  &lt;/complexType&gt;
 *
 *          </code>
 *         </pre>
 * </p>
 *
 * @generated
 */
public class DegreesTypeBinding extends AbstractComplexBinding {
    /**
     * @generated
     */
    public QName getTarget() {
        return GML.DEGREESTYPE;
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
    public Object parse(ElementInstance instance, Node node, Object value)
        throws Exception {
        //TODO: implement
        return null;
    }
}
