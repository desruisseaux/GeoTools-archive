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

import javax.xml.namespace.QName;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.geotools.geometry.DirectPosition1D;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.xml.*;


/**
 * Binding object for the type http://www.opengis.net/gml:DirectPositionListType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;complexType name="DirectPositionListType"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;DirectPositionList instances hold the coordinates for a sequence of direct positions within the same coordinate
 *                          reference system (CRS).&lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;simpleContent&gt;
 *          &lt;extension base="gml:doubleList"&gt;
 *              &lt;attributeGroup ref="gml:SRSReferenceGroup"/&gt;
 *              &lt;attribute name="count" type="positiveInteger" use="optional"&gt;
 *                  &lt;annotation&gt;
 *                      &lt;documentation&gt;"count" allows to specify the number of direct positions in the list. If the attribute count is present then
 *                                                  the attribute srsDimension shall be present, too.&lt;/documentation&gt;
 *                  &lt;/annotation&gt;
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
public class DirectPositionListTypeBinding extends AbstractComplexBinding {
    /**
     * @generated
     */
    public QName getTarget() {
        return GML.DirectPositionListType;
    }

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
        return DirectPosition[].class;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    public Object parse(ElementInstance instance, Node node, Object value)
        throws Exception {
        CoordinateReferenceSystem crs = GML3ParsingUtils.crs(node);

        double[] values = (double[]) value;
        Integer count = (Integer) node.getAttributeValue("count");

        if (count == null) {
            //assume 2 dimensional
            count = new Integer(values.length / 2);
        }

        if (count.intValue() == 0) {
            return new DirectPosition[] {  };
        }

        int dim = values.length / count.intValue();

        if ((dim < 1) || (dim > 2)) {
            throw new IllegalArgumentException("dimension must be 1 or 2");
        }

        DirectPosition[] dps = new DirectPosition[count.intValue()];

        if (dim == 1) {
            for (int i = 0; i < count.intValue(); i++) {
                dps[i] = new DirectPosition1D(crs);
                dps[i].setOrdinate(0, values[i]);
            }
        } else {
            int j = 0;

            for (int i = 0; i < count.intValue(); i++) {
                dps[i] = new DirectPosition2D(crs);
                dps[i].setOrdinate(0, values[j++]);
                dps[i].setOrdinate(1, values[j++]);
            }
        }

        return dps;
    }
}
