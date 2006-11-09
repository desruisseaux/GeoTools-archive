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
package org.geotools.gml2.bindings;

import org.picocontainer.MutablePicoContainer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.math.BigDecimal;
import javax.xml.namespace.QName;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFactory;
import org.geotools.xml.*;
import org.geotools.xml.AbstractComplexBinding;
import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;


/**
 * Binding object for the type http://www.opengis.net/gml:CoordType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;complexType name="CoordType"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;         Represents a coordinate tuple in one,
 *              two, or three dimensions.       &lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;sequence&gt;
 *          &lt;element name="X" type="decimal"/&gt;
 *          &lt;element name="Y" type="decimal" minOccurs="0"/&gt;
 *          &lt;element name="Z" type="decimal" minOccurs="0"/&gt;
 *      &lt;/sequence&gt;
 *  &lt;/complexType&gt;
 *
 *          </code>
 *         </pre>
 * </p>
 *
 * @generated
 */
public class GMLCoordTypeBinding extends AbstractComplexBinding {
    CoordinateSequenceFactory csFactory;

    public GMLCoordTypeBinding(CoordinateSequenceFactory csFactory) {
        this.csFactory = csFactory;
    }

    /**
     * @generated
     */
    public QName getTarget() {
        return GML.COORDTYPE;
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
        return null;
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
     * Returns a coordinate sequence with a single coordinate in it.
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Object parse(ElementInstance instance, Node node, Object value)
        throws Exception {
        int dimension = 1;
        double x;
        double y;
        double z;
        x = y = z = Double.NaN;

        x = ((BigDecimal) node.getChild("X").getValue()).doubleValue();

        if (!node.getChildren("Y").isEmpty()) {
            dimension++;
            y = ((BigDecimal) node.getChild("Y").getValue()).doubleValue();
        }

        if (!node.getChildren("Z").isEmpty()) {
            dimension++;
            z = ((BigDecimal) node.getChild("Z").getValue()).doubleValue();
        }

        //create a coordinate sequence with a single coordinate in it
        CoordinateSequence seq = csFactory.create(1, dimension);
        seq.setOrdinate(0, CoordinateSequence.X, x);

        if (y != Double.NaN) {
            seq.setOrdinate(0, CoordinateSequence.Y, y);
        }

        if (z != Double.NaN) {
            seq.setOrdinate(0, CoordinateSequence.Z, z);
        }

        return seq;
    }
}
