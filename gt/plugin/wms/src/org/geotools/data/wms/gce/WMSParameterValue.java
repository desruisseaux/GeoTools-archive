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

import java.net.URI;

import javax.units.Unit;

import org.geotools.parameter.Parameter;
import org.geotools.parameter.DefaultParameterDescriptor;
import org.opengis.parameter.InvalidParameterTypeException;
import org.opengis.parameter.InvalidParameterValueException;


/**
 * @author rgould
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 * @source $URL$
 */
public class WMSParameterValue extends Parameter {
    private Object value;
    private DefaultParameterDescriptor descriptor;

    /**
     * @param value
     * @param descriptor
     */
    public WMSParameterValue(Object value, DefaultParameterDescriptor descriptor) {
        super(descriptor);
        this.value = value;
        this.descriptor = descriptor;
    }

    /* (non-Javadoc)
     * @see org.opengis.parameter.ParameterValue#getUnit()
     */
    public Unit getUnit() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.opengis.parameter.ParameterValue#doubleValue(javax.units.Unit)
     */
    public double doubleValue(Unit arg0) throws InvalidParameterTypeException {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see org.opengis.parameter.ParameterValue#doubleValue()
     */
    public double doubleValue() throws InvalidParameterTypeException {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see org.opengis.parameter.ParameterValue#intValue()
     */
    public int intValue() throws InvalidParameterTypeException {
        // TODO Auto-generated method stub
        return 0;
    }

    public boolean booleanValue() throws InvalidParameterTypeException {
        return ((Boolean) value).booleanValue();
    }

    /* (non-Javadoc)
     * @see org.opengis.parameter.ParameterValue#stringValue()
     */
    public String stringValue() throws InvalidParameterTypeException {
        // TODO Auto-generated method stub
        return (String) value;
    }

    /* (non-Javadoc)
     * @see org.opengis.parameter.ParameterValue#doubleValueList(javax.units.Unit)
     */
    public double[] doubleValueList(Unit arg0)
        throws InvalidParameterTypeException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.opengis.parameter.ParameterValue#doubleValueList()
     */
    public double[] doubleValueList() throws InvalidParameterTypeException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.opengis.parameter.ParameterValue#intValueList()
     */
    public int[] intValueList() throws InvalidParameterTypeException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.opengis.parameter.ParameterValue#valueFile()
     */
    public URI valueFile() throws InvalidParameterTypeException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.opengis.parameter.ParameterValue#getValue()
     */
    public Object getValue() {
        // TODO Auto-generated method stub
        return value;
    }

    /* (non-Javadoc)
     * @see org.opengis.parameter.ParameterValue#setValue(double[], javax.units.Unit)
     */
    public void setValue(double[] arg0, Unit arg1)
        throws InvalidParameterValueException {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see org.opengis.parameter.ParameterValue#setValue(double, javax.units.Unit)
     */
    public void setValue(double arg0, Unit arg1)
        throws InvalidParameterValueException {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see org.opengis.parameter.ParameterValue#setValue(double)
     */
    public void setValue(double arg0) throws InvalidParameterValueException {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see org.opengis.parameter.ParameterValue#setValue(int)
     */
    public void setValue(int arg0) throws InvalidParameterValueException {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see org.opengis.parameter.ParameterValue#setValue(boolean)
     */
    public void setValue(boolean arg0) throws InvalidParameterValueException {
        value = new Boolean(arg0);
    }

    /* (non-Javadoc)
     * @see org.opengis.parameter.ParameterValue#setValue(java.lang.Object)
     */
    public void setValue(Object arg0) throws InvalidParameterValueException {
        value = arg0;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    public Object clone() {
        // TODO Auto-generated method stub
        return new WMSParameterValue(new String((String) value), descriptor);
    }
}
