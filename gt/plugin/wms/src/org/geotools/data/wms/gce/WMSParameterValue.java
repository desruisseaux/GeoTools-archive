/*
 * Created on Jul 21, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wms.gce;

import java.net.URL;

import javax.units.Unit;

import org.opengis.parameter.GeneralOperationParameter;
import org.opengis.parameter.InvalidParameterTypeException;
import org.opengis.parameter.InvalidParameterValueException;
import org.opengis.parameter.ParameterValue;

/**
 * @author rgould
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WMSParameterValue implements ParameterValue {

	private Object value;
	private GeneralOperationParameter descriptor;
	
	/**
	 * @param value
	 * @param descriptor
	 */
	public WMSParameterValue(Object value, GeneralOperationParameter descriptor) {

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

	/* (non-Javadoc)
	 * @see org.opengis.parameter.ParameterValue#booleanValue()
	 */
	public boolean booleanValue() throws InvalidParameterTypeException {
		// TODO Auto-generated method stub
		return false;
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
	public URL valueFile() throws InvalidParameterTypeException {
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
		// TODO Auto-generated method stub

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

	/* (non-Javadoc)
	 * @see org.opengis.parameter.GeneralParameterValue#getDescriptor()
	 */
	public GeneralOperationParameter getDescriptor() {
		return descriptor;
	}

}
