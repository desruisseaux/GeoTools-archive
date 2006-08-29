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
 * Binding object for the type http://www.opengis.net/gml:RingType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;complexType name="RingType"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;A Ring is used to represent a single connected component of a surface boundary. It consists of a sequence of curves connected in a cycle (an object whose boundary is empty).
 *  A Ring is structurally similar to a composite curve in that the endPoint of each curve in the sequence is the startPoint of the next curve in the Sequence. Since the sequence is circular, there is no exception to this rule. Each ring, like all boundaries, is a cycle and each ring is simple.
 *  NOTE: Even though each Ring is simple, the boundary need not be simple. The easiest case of this is where one of the interior rings of a surface is tangent to its exterior ring.&lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;complexContent&gt;
 *          &lt;extension base="gml:AbstractRingType"&gt;
 *              &lt;sequence&gt;
 *                  &lt;element maxOccurs="unbounded" ref="gml:curveMember"&gt;
 *                      &lt;annotation&gt;
 *                          &lt;documentation&gt;This element references or contains one curve in the composite curve. The curves are contiguous, the collection of curves is ordered.
 *  NOTE: This definition allows for a nested structure, i.e. a CompositeCurve may use, for example, another CompositeCurve as a curve member.&lt;/documentation&gt;
 *                      &lt;/annotation&gt;
 *                  &lt;/element&gt;
 *              &lt;/sequence&gt;
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
public class RingTypeBinding extends AbstractComplexBinding {
    /**
     * @generated
     */
    public QName getTarget() {
        return GML.RINGTYPE;
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
