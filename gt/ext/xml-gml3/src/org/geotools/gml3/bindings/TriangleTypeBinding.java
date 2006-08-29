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
 * Binding object for the type http://www.opengis.net/gml:TriangleType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;complexType name="TriangleType"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;Represents a triangle as a surface with an outer boundary consisting of a linear ring. Note that this is a polygon (subtype) with no inner boundaries. The number of points in the linear ring must be four.&lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;complexContent&gt;
 *          &lt;extension base="gml:AbstractSurfacePatchType"&gt;
 *              &lt;sequence&gt;
 *                  &lt;element ref="gml:exterior"&gt;
 *                      &lt;annotation&gt;
 *                          &lt;documentation&gt;Constraint: The Ring shall be a LinearRing and must form a triangle, the first and the last position must be co-incident.&lt;/documentation&gt;
 *                      &lt;/annotation&gt;
 *                  &lt;/element&gt;
 *              &lt;/sequence&gt;
 *              &lt;attribute fixed="planar" name="interpolation" type="gml:SurfaceInterpolationType"&gt;
 *                  &lt;annotation&gt;
 *                      &lt;documentation&gt;The attribute "interpolation" specifies the interpolation mechanism used for this surface patch. Currently only planar surface patches are defined in GML 3, the attribute is fixed to "planar", i.e. the interpolation method shall return points on a single plane. The boundary of the patch shall be contained within that plane.&lt;/documentation&gt;
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
public class TriangleTypeBinding extends AbstractComplexBinding {
    /**
     * @generated
     */
    public QName getTarget() {
        return GML.TRIANGLETYPE;
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
