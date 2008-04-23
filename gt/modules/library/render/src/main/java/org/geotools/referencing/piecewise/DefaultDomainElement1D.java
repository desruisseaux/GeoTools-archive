/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.referencing.piecewise;

import org.geotools.renderer.i18n.ErrorKeys;
import org.geotools.renderer.i18n.Errors;
import org.geotools.resources.Utilities;
import org.geotools.util.NumberRange;
import org.geotools.util.SimpleInternationalString;
import org.opengis.util.InternationalString;

/**
 * This class implements the {@link DomainElement1D} interface in order to provide
 * basic capabilities for {@link DomainElement1D} subclasses.
 * 
 * @author Simone Giannecchini, GeoSolutions.
 */
public class DefaultDomainElement1D implements DomainElement1D {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2520449231656622013L;

	/**
	 * Base implementation for the {@link Comparable#compareTo(Object)} method.
	 * This method will work only if the provided input object is a
	 * {@link DefaultDomainElement1D}.
	 * 
	 * <p>
	 * Two {@link DefaultDomainElement1D}s are compared by comparing their lower
	 * bounds in order to establish an order between them.
	 * 
	 * @return a negative integer, zero, or a positive integer as this object is
	 *         less than, equal to, or greater than the specified object.
	 * 
	 * @throws ClassCastException
	 *             if the specified object's type prevents it from being
	 *             compared to this Object.
	 * 
	 * @see Comparable#compareTo(Object)
	 * @todo we could improve this check for the specific case when the two minimums are equal
	 */
	public int compareTo(Object o) {
		if (o instanceof DefaultDomainElement1D)
			return new Double(inputMinimum).compareTo(new Double(
					((DefaultDomainElement1D) o).inputMinimum));
		throw new ClassCastException(Errors.format(ErrorKeys.ILLEGAL_ARGUMENT_$1, o));
	}

	/**
	 * Implementation of {@link Object#equals(Object)} for {@link DomainElement1D}s.
	 * 
	 * <p>
	 * Two {@link DefaultDomainElement1D}s are considered to be equal if they have
	 * the same inputr range and the same name.
	 * 
	 * @param obj
	 *            the reference object with which to compare.
	 * @return <code>true</code> if this object is the same as the obj
	 *         argument; <code>false</code> otherwise.
	 * @see Object#equals(Object)
	 */
	public boolean equals(final Object obj) {
		if (obj == this) {
			// Slight optimization
			return true;
		}
		if (obj != null && obj.getClass().equals(getClass())) {
			final DefaultDomainElement1D that = (DefaultDomainElement1D) obj;
			if (Double.doubleToRawLongBits(inputMinimum) == Double
					.doubleToRawLongBits(that.inputMinimum)
					&& Double.doubleToRawLongBits(inputMaximum) == Double
							.doubleToRawLongBits(that.inputMaximum)
					&& Utilities.equals(this.name, that.name)) {
				// paranoiac check
				if (this.inputRange != null && that.inputRange != null) {
					if (!Utilities.equals(this.inputRange, that.inputRange))
						return false;
					return true;
				}
				return true;
			}
		}
		return false;
	}

	/**
	 * @see DomainElement1D#contains(Number)
	 */
	public boolean contains(Number value) {
		return inputRange.contains(value);
	}

	/**
	 * @see DomainElement1D#contains(NumberRange)
	 */
	public boolean contains(NumberRange range) {
		return inputRange.contains(range);
	}

	/**
	 * @see DomainElement1D#contains(double)
	 */
	public boolean contains(double value) {
		// return (value <= inputMaximum && value >= inputMinimum)
		// || (Double.isNaN(value) && (Double.doubleToRawLongBits(value) ==
		// Double
		// .doubleToRawLongBits(inputMinimum)));
		return (value <= inputMaximum && value >= inputMinimum)
				|| (Double.doubleToRawLongBits(value) == Double
						.doubleToRawLongBits(inputMinimum));

	}

	/**
	 * The domain element name.
	 * 
	 * @uml.property name="name"
	 */
	private InternationalString name;

	/**
	 * The minimal sample value (inclusive). This domain element is made of all values
	 * in the range {@code    inputMinimum} to {@code    inputMaximum}
	 * inclusive.
	 * 
	 * @uml.property name="inputMinimum"
	 */
	private double inputMinimum;

	/**
	 * The maximal sample value (inclusive). This domain element is made of all values
	 * in the range {@code    inputMinimum} to {@code    inputMaximum}
	 * inclusive.
	 * 
	 * @uml.property name="inputMaximum"
	 */
	private double inputMaximum;

	/**
	 * The range of values {@code   [inputMinimum..maximum]} . May be computed
	 * only when first requested, or may be user-supplied .
	 * 
	 * @uml.property name="inputRange"
	 */
	private NumberRange inputRange;

	/**
	 * Is lower input bound infinite?
	 */
	private boolean inputMinimumInf;

	/**
	 * Is uper input bound infinite?
	 */
	private boolean inputMaximumInf;

	/**
	 * Is upper input bound NaN?
	 * 
	 * @uml.property name="inputMaximumNaN"
	 */
	private boolean inputMaximumNaN;

	/**
	 * Is lower input bound NaN?
	 * 
	 * @uml.property name="inputMinimumNaN"
	 */
	private boolean inputMinimumNaN;

	/**
	 * Abstract domain element constructor.
	 * 
	 * <p>
	 * It builds up an {@link DefaultDomainElement1D} with the provided name and input
	 * range.
	 * 
	 * @param name
	 *            for this {@link DefaultDomainElement1D}.
	 * @param inputRange
	 *            for this {@link DefaultDomainElement1D}.
	 * @throws IllegalArgumentException
	 *             in case one of the input arguments is invalid.
	 */
	public DefaultDomainElement1D(final CharSequence name,
			final NumberRange inputRange) throws IllegalArgumentException {
		// /////////////////////////////////////////////////////////////////////
		//
		// Initial checks
		//
		// /////////////////////////////////////////////////////////////////////
		PiecewiseUtilities.ensureNonNull("name", name);
		PiecewiseUtilities.ensureNonNull("inputRange", inputRange);
		
		// /////////////////////////////////////////////////////////////////////
		//
		// Initialise fields
		//
		// /////////////////////////////////////////////////////////////////////
		this.name = SimpleInternationalString.wrap(name);
		this.inputRange = inputRange;
		Class type = inputRange.getElementClass();
		boolean minInc = inputRange.isMinIncluded();
		boolean maxInc = inputRange.isMaxIncluded();
		final double tempMin = inputRange.getMinimum();
		final double tempMax = inputRange.getMaximum();
		if (Double.isInfinite(tempMin)) {
			this.inputMinimum = tempMin;
			inputMinimumInf = true;
		} else
			this.inputMinimum = PiecewiseUtilities.doubleValue(type, inputRange
					.getMinValue(), minInc ? 0 : +1);
		if (Double.isInfinite(tempMax)) {
			this.inputMaximum = tempMax;
			inputMaximumInf = true;
		} else
			this.inputMaximum = PiecewiseUtilities.doubleValue(type, inputRange
					.getMaxValue(), maxInc ? 0 : -1);
		// /////////////////////////////////////////////////////////////////////
		//
		// Checks
		//
		// /////////////////////////////////////////////////////////////////////

		// //
		//
		// only one input values is NaN
		//
		// //
		inputMinimumNaN = Double.isNaN(inputMinimum);
		inputMaximumNaN = Double.isNaN(inputMaximum);
		if ((inputMinimumNaN && !inputMaximumNaN)
				|| (!inputMinimumNaN && inputMaximumNaN))
			throw new IllegalArgumentException(Errors.format(
					ErrorKeys.BAD_RANGE_$2, inputRange.getMinValue(),
					inputRange.getMaxValue()));

	}

	/**
	 * Returns a hash value for this domain element. This value need not remain
	 * consistent between different implementations of the same class.
	 * 
	 */
	public int hashCode() {
		return name.hashCode();
	}

	/**
	 * Getter method for this {@link DomainElement1D} 's name.
	 * 
	 * @return this {@link DefaultDomainElement1D} 's name.
	 * @uml.property name="name"
	 */
	public InternationalString getName() {
		return name;
	}
	
	/**
	 * Retrieves the upper bound of the range where this {@link DomainElement1D} is
	 * defined. 
	 * 
	 * <P>
	 * <strong>This is just a convenience method</strong>
	 * 
	 * @return the upper bound of the range where this {@link DomainElement1D} is
	 *         defined.
	 */
	public double getInputMaximum() {
		return inputMaximum;
	}

	/**
	 * Tells us if the upper bound of the range where this {@link DomainElement1D} is
	 * defined is an infinite number
	 * 
	 * <P>
	 * <strong>This is just a convenience method</strong>
	 * 
	 * @return <code>true</code> if the upper bound of the range where this
	 *         {@link DomainElement1D} is defined is infinite, <code>false</code>
	 *         otherwise.
	 */
	public boolean isInputMaximumInfinite() {
		return inputMaximumInf;
	}

	/**
	 * Tells us if the upper bound of the range where this {@link DomainElement1D} is
	 * defined is NaN.
	 * 
	 *  <P>
	 *  <strong>This is just a convenience method</strong>
	 * 
	 * @return <code>true</code> if the upper bound of the range where this
	 *         {@link DomainElement1D} is defined is NaN, <code>false</code>
	 *         otherwise.
	 */
	public boolean isInputMaximumNaN() {
		return inputMaximumNaN;
	}

	/**
	 * Retrieves the lower bound of the range where this {@link DomainElement1D} is
	 * defined. 
	 * 
	 * 
	 * <P>
	 * <strong>This is just a convenience method</strong>
	 * 
	 * @return the lower bound of the range where this {@link DomainElement1D} is
	 *         defined.
	 */
	public double getInputMinimum() {
		return inputMinimum;
	}

	/**
	 * Tells us if the lower bound of the range where this {@link DomainElement1D} is
	 * defined is an infinite number.
	 * 
	 *  <P>
	 *  <strong>This is just a convenience method</strong>
	 * 
	 * @return <code>true</code> if the lower bound of the range where this
	 *         {@link DomainElement1D} is defined is infinite, <code>false</code>
	 *         otherwise.
	 */
	public boolean isInputMinimumInfinite() {
		return inputMinimumInf;
	}

	/**
	 * This method retrieves the input range.
	 * 
	 * @return the input range.
	 * @uml.property name="inputRange"
	 */
	public NumberRange getRange() {
		return new NumberRange(inputRange);
	}

	/**
	 * Tells us if the lower bound of the range where this {@link DomainElement1D} is
	 * defined is NaN 
	 * 
	 * 
	 *  <P>
	 *  <strong>This is just a convenience method</strong>
	 * 
	 * @return <code>true</code> if the lower bound of the range where this
	 *         {@link DomainElement1D} is defined is NaN, <code>false</code>
	 *         otherwise.
	 */
	public boolean isInputMinimumNaN() {
		return inputMinimumNaN;
	}

	public String toString() {
		final StringBuffer buffer= new StringBuffer("Domain description:");
		buffer.append("\n").append("name=").append(name);
		buffer.append("\n").append("input range=").append(inputRange);
		return buffer.toString();
	}

}