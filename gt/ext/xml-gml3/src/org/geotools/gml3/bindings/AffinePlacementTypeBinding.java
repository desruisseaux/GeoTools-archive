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
 * Binding object for the type http://www.opengis.net/gml:AffinePlacementType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;complexType name="AffinePlacementType"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;A placement takes a standard geometric
 *     construction and places it in geographic space. It defines a
 *     transformation from a constructive parameter space to the
 *     co-ordinate space of the co-ordinate reference system being used.
 *     Parameter spaces in formulae in this International Standard are
 *     given as (u, v) in 2D and(u, v, w) in 3D. Co-ordinate reference
 *     systems positions are given in formulae, in this International
 *     Standard, by either (x, y) in 2D, or (x, y, z) in 3D.
 *
 *     Affine placements are defined by linear transformations from
 *     parameter space to the target co-ordiante space. 2-dimensional
 *     Cartesian parameter space,(u,v) transforms into 3-dimensional co-
 *     ordinate reference systems,(x,y,z) by using an affine
 *     transformation,(u,v)-&gt;(x,y,z) which is defined :
 *
 *          x        ux vx          x0
 *                           u
 *          y =        uy vy   + y0
 *                           v
 *          x        uz vz        z0
 *
 *     Then, given this equation, the location element of the
 *     AffinePlacement is the direct position (x0, y0, z0), which is the
 *     target position of the origin in (u, v). The two reference
 *     directions (ux, uy, uz) and (vx, vy, vz) are the target
 *     directions of the unit vectors at the origin in (u, v).&lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;sequence&gt;
 *          &lt;element name="location" type="gml:DirectPositionType"&gt;
 *              &lt;annotation&gt;
 *                  &lt;documentation&gt;The location property gives
 *       the target of the parameter space origin. This is the vector
 *      (x0, y0, z0) in the formulae above.&lt;/documentation&gt;
 *              &lt;/annotation&gt;
 *          &lt;/element&gt;
 *          &lt;element maxOccurs="unbounded" name="refDirection" type="gml:VectorType"&gt;
 *              &lt;annotation&gt;
 *                  &lt;documentation&gt;The attribute refDirection gives the
 *  target directions for the co-ordinate basis vectors of the
 *  parameter space. These are the columns of the matrix in the
 *  formulae given above. The number of directions given shall be
 *  inDimension. The dimension of the directions shall be
 *  outDimension.&lt;/documentation&gt;
 *              &lt;/annotation&gt;
 *          &lt;/element&gt;
 *          &lt;element name="inDimension" type="positiveInteger"&gt;
 *              &lt;annotation&gt;
 *                  &lt;documentation&gt;Dimension of the constructive parameter
 *       space.&lt;/documentation&gt;
 *              &lt;/annotation&gt;
 *          &lt;/element&gt;
 *          &lt;element name="outDimension" type="positiveInteger"&gt;
 *              &lt;annotation&gt;
 *                  &lt;documentation&gt;Dimension of the co-ordinate space.&lt;/documentation&gt;
 *              &lt;/annotation&gt;
 *          &lt;/element&gt;
 *      &lt;/sequence&gt;
 *  &lt;/complexType&gt;
 *
 *          </code>
 *         </pre>
 * </p>
 *
 * @generated
 */
public class AffinePlacementTypeBinding extends AbstractComplexBinding {
    /**
     * @generated
     */
    public QName getTarget() {
        return GML.AFFINEPLACEMENTTYPE;
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
