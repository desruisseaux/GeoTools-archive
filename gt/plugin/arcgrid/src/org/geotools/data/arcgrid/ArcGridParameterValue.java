/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
package org.geotools.data.arcgrid;

import org.opengis.parameter.GeneralOperationParameter;
import org.opengis.parameter.InvalidParameterTypeException;
import org.opengis.parameter.InvalidParameterValueException;
import org.opengis.parameter.ParameterValue;
import java.net.URL;
import javax.units.Unit;

/**
 * The Arc Grid Format parameters are simple so there is only one
 * ArcGridParameterValue class that acts as all the  parameter value classes
 *
 * @author jeichar
 */
public class ArcGridParameterValue implements ParameterValue {
	boolean value;
	GeneralOperationParameter descriptor;

	ArcGridParameterValue(
		boolean value,
		GeneralOperationParameter descriptor) {
		this.value = value;
		this.descriptor = descriptor;
	}

	/**
	 * @see org.opengis.parameter.ParameterValue#getUnit()
	 */
	public Unit getUnit() {
		return null;
	}

	/**
	 * @see org.opengis.parameter.ParameterValue#doubleValue()
	 */
	public double doubleValue() throws InvalidParameterTypeException {
		throw new InvalidParameterTypeException(
			"Not Supported by ArcGridParameterValue",
			null);
	}

	/**
	 * @see org.opengis.parameter.ParameterValue#intValue()
	 */
	public int intValue() throws InvalidParameterTypeException {
		throw new InvalidParameterTypeException(
			"Not Supported by ArcGridParameterValue",
			null);
	}

	/**
	 * @see org.opengis.parameter.ParameterValue#booleanValue()
	 */
	public boolean booleanValue() throws InvalidParameterTypeException {
		return value;
	}

	/**
	 * @see org.opengis.parameter.ParameterValue#stringValue()
	 */
	public String stringValue() throws InvalidParameterTypeException {
		throw new InvalidParameterTypeException(
			"Not Supported by ArcGridParameterValue",
			null);
	}

	/**
	 * @see org.opengis.parameter.ParameterValue#doubleValueList()
	 */
	public double[] doubleValueList() throws InvalidParameterTypeException {
		throw new InvalidParameterTypeException(
			"Not Supported by ArcGridParameterValue",
			null);
	}

	/**
	 * @see org.opengis.parameter.ParameterValue#intValueList()
	 */
	public int[] intValueList() throws InvalidParameterTypeException {
		throw new InvalidParameterTypeException(
			"Not Supported by ArcGridParameterValue",
			null);
	}

	/**
	 * @see org.opengis.parameter.ParameterValue#valueFile()
	 */
	public URL valueFile() throws InvalidParameterTypeException {
		throw new InvalidParameterTypeException(
			"Not Supported by ArcGridParameterValue",
			null);
	}

	/**
	 * @see org.opengis.parameter.ParameterValue#getValue()
	 */
	public Object getValue() {
		return Boolean.valueOf(value);
	}

	/**
	 * @see org.opengis.parameter.ParameterValue#setValue(double)
	 */
	public void setValue(double arg0) throws InvalidParameterValueException {
		throw new InvalidParameterTypeException(
			"Not Supported by ArcGridParameterValue",
			null);
	}

	/**
	 * @see org.opengis.parameter.ParameterValue#setValue(int)
	 */
	public void setValue(int arg0) throws InvalidParameterValueException {
		throw new InvalidParameterTypeException(
			"Not Supported by ArcGridParameterValue",
			null);
	}

	/**
	 * @see org.opengis.parameter.ParameterValue#setValue(boolean)
	 */
	public void setValue(boolean arg0) throws InvalidParameterValueException {
		value = arg0;
	}

	/**
	 * @see org.opengis.parameter.ParameterValue#setValue(java.lang.Object)
	 */
	public void setValue(Object arg0) throws InvalidParameterValueException {
		value = ((Boolean) arg0).booleanValue();
	}

	/**
	 * @see org.opengis.parameter.ParameterValue#setUnit(javax.units.Unit)
	 */
	public void setUnit(Unit arg0) throws InvalidParameterTypeException {
		throw new InvalidParameterTypeException(
			"Not Supported by ArcGridParameterValue",
			null);
	}

	/**
	 * @see org.opengis.parameter.GeneralParameterValue#getDescriptor()
	 */
	public GeneralOperationParameter getDescriptor() {
		return descriptor;
	}

	public Object clone() {
		return new ArcGridParameterValue(value, descriptor);
	}

	/* (non-Javadoc)
	 * @see org.opengis.parameter.ParameterValue#doubleValue(javax.units.Unit)
	 */
	public double doubleValue(Unit arg0) throws InvalidParameterTypeException {
		throw new InvalidParameterTypeException(
			"Not Supported by ArcGridParameterValue",
			null);
	}

	/* (non-Javadoc)
	 * @see org.opengis.parameter.ParameterValue#doubleValueList(javax.units.Unit)
	 */
	public double[] doubleValueList(Unit arg0)
		throws InvalidParameterTypeException {
		throw new InvalidParameterTypeException(
			"Not Supported by ArcGridParameterValue",
			null);
	}

	/* (non-Javadoc)
	 * @see org.opengis.parameter.ParameterValue#setValue(double[], javax.units.Unit)
	 */
	public void setValue(double[] arg0, Unit arg1)
		throws InvalidParameterValueException {
		throw new InvalidParameterTypeException(
			"Not Supported by ArcGridParameterValue",
			null);
	}

	/* (non-Javadoc)
	 * @see org.opengis.parameter.ParameterValue#setValue(double, javax.units.Unit)
	 */
	public void setValue(double arg0, Unit arg1)
		throws InvalidParameterValueException {
		throw new InvalidParameterTypeException(
			"Not Supported by ArcGridParameterValue",
			null);
	}
}
