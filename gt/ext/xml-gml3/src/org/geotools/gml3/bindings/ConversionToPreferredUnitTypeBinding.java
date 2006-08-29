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
 * Binding object for the type http://www.opengis.net/gml:ConversionToPreferredUnitType.
 *
 * <p>
 *        <pre>
 *         <code>
 *  &lt;complexType name="ConversionToPreferredUnitType"&gt;
 *      &lt;annotation&gt;
 *          &lt;documentation&gt;Relation of a unit to the preferred unit for this quantity type, specified by an arithmetic conversion (scaling and/or offset). A preferred unit is either a base unit or a derived unit selected for all units of one quantity type. The mandatory attribute "uom" shall reference the preferred unit that this conversion applies to. The conversion is specified by one of two alternative elements: "factor" or "formula".&lt;/documentation&gt;
 *      &lt;/annotation&gt;
 *      &lt;complexContent&gt;
 *          &lt;extension base="gml:UnitOfMeasureType"&gt;
 *              &lt;choice&gt;
 *                  &lt;element name="factor" type="double"&gt;
 *                      &lt;annotation&gt;
 *                          &lt;documentation&gt;Specification of the scale factor by which a value using this unit of measure can be multiplied to obtain the corresponding value using the preferred unit of measure.&lt;/documentation&gt;
 *                      &lt;/annotation&gt;
 *                  &lt;/element&gt;
 *                  &lt;element name="formula" type="gml:FormulaType"&gt;
 *                      &lt;annotation&gt;
 *                          &lt;documentation&gt;Specification of the formula by which a value using this unit of measure can be converted to obtain the corresponding value using the preferred unit of measure.&lt;/documentation&gt;
 *                      &lt;/annotation&gt;
 *                  &lt;/element&gt;
 *              &lt;/choice&gt;
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
public class ConversionToPreferredUnitTypeBinding extends AbstractComplexBinding {
    /**
     * @generated
     */
    public QName getTarget() {
        return GML.CONVERSIONTOPREFERREDUNITTYPE;
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
