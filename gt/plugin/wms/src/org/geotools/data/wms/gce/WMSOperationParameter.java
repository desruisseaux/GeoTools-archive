/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004, Geotools Project Managment Committee (PMC)
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
 *
 */
package org.geotools.data.wms.gce;

import org.opengis.metadata.Identifier;

import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.OperationParameter;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.units.Unit;


/**
 * @author rgould
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WMSOperationParameter implements OperationParameter {
    Identifier[] identifiers;
    int maxOccurs;
    int minOccurs;
    String name;
    String remarks;
    Object defaultValue;
    Set validValues;
    List availableLayers;
    Class valueClass = WMSParameterValue.class;

    /* (non-Javadoc)
     * @see org.opengis.parameter.OperationParameter#getValueClass()
     */
    public Class getValueClass() {
        return valueClass;
    }

    /* (non-Javadoc)
     * @see org.opengis.parameter.OperationParameter#getValidValues()
     */
    public Set getValidValues() {
        return validValues;
    }

    /* (non-Javadoc)
     * @see org.opengis.parameter.OperationParameter#getDefaultValue()
     */
    public Object getDefaultValue() {
        return defaultValue;
    }

    /* (non-Javadoc)
     * @see org.opengis.parameter.OperationParameter#getMinimumValue()
     */
    public Comparable getMinimumValue() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.opengis.parameter.OperationParameter#getMaximumValue()
     */
    public Comparable getMaximumValue() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.opengis.parameter.OperationParameter#getUnit()
     */
    public Unit getUnit() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.opengis.parameter.GeneralOperationParameter#createValue()
     */
    public GeneralParameterValue createValue() {
        return new WMSParameterValue(null, this);
    }

    /* (non-Javadoc)
     * @see org.opengis.parameter.GeneralOperationParameter#getMinimumOccurs()
     */
    public int getMinimumOccurs() {
        return minOccurs;
    }

    /* (non-Javadoc)
     * @see org.opengis.parameter.GeneralOperationParameter#getMaximumOccurs()
     */
    public int getMaximumOccurs() {
        return maxOccurs;
    }

    /* (non-Javadoc)
     * @see org.opengis.referencing.IdentifiedObject#getName(java.util.Locale)
     */
    public String getName(Locale arg0) {
        return name;
    }

    /* (non-Javadoc)
     * @see org.opengis.referencing.IdentifiedObject#getIdentifiers()
     */
    public Identifier[] getIdentifiers() {
        return identifiers;
    }

    /* (non-Javadoc)
     * @see org.opengis.referencing.IdentifiedObject#getRemarks(java.util.Locale)
     */
    public String getRemarks(Locale arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.opengis.referencing.IdentifiedObject#toWKT()
     */
    public String toWKT() throws UnsupportedOperationException {
        // TODO Auto-generated method stub
        return null;
    }

    public List getAvailableLayers() {
        return availableLayers;
    }
}
