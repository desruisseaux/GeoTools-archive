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
 * Binding object for the type http://www.opengis.net/gml:GeodesicStringType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;complexType name="GeodesicStringType"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;A GeodesicString consists of sequence of
 *     geodesic segments. The type essentially combines a sequence of
 *     Geodesic into a single object.
 *     The GeodesicString is computed from two or more positions and an
 *     interpolation using geodesics defined from the geoid (or
 *     ellipsoid) of the co-ordinate reference system being used.&lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;complexContent&gt;
 *          &lt;extension base="gml:AbstractCurveSegmentType"&gt;
 *              &lt;choice&gt;
 *                  &lt;element ref="gml:posList"/&gt;
 *                  &lt;group maxOccurs="unbounded" minOccurs="2" ref="gml:geometricPositionGroup"/&gt;
 *              &lt;/choice&gt;
 *              &lt;attribute fixed="geodesic" name="interpolation" type="gml:CurveInterpolationType"&gt;
 *                  &lt;annotation&gt;
 *                      &lt;documentation&gt;The attribute "interpolation" specifies the
 *       curve interpolation mechanism used for this segment. This
 *       mechanism uses the control points and control parameters to
 *       determine the position of this curve segment. For an
 *       GeodesicString the interpolation is fixed as "geodesic".&lt;/documentation&gt;
 *                  &lt;/annotation&gt;
 *              &lt;/attribute&gt;
 *          &lt;/extension&gt;
 *      &lt;/complexContent&gt;
 *  &lt;/complexType&gt;
 *
 *          </code>
 *         </pre>
 * </p>
 *
 * @generated
 */
public class GeodesicStringTypeBinding extends AbstractComplexBinding {
    /**
     * @generated
     */
    public QName getTarget() {
        return GML.GEODESICSTRINGTYPE;
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
