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

import java.io.Serializable;

import org.geotools.util.NumberRange;
import org.opengis.util.InternationalString;

/**
 * A {@link DomainElement1D} can be seen as a monodimensional range of values with its own label. 
 * 
 * <p>
 * All {@link DomainElement1D}D <strong>must</strong> have a human readable name.
 * <p>
 * All {@code DomainElement1D} objects are immutable and thread-safe.
 * 
 * @author Simone Giannecchini, GeoSolutions
 * 
 */
public interface DomainElement1D extends Serializable, Comparable/*<Object>*/ {

	/**
	 * Returns the domain element name.
	 */
	public InternationalString getName();

	/**
	 * Compares the specified object with this domain element for equality.
	 */
	public boolean equals(final Object object);

	/**
	 * Compares the specified object with this domain element.
	 */
	public int compareTo(Object o);

	/**
	 * Provides access to the input {@link NumberRange} for this
	 * {@link DomainElement1D}.
	 * 
	 * @return the range where this {@link DomainElement1D} is defined.
	 */
	public NumberRange getRange();


	/**
	 * This methods can be used to check whether or not a given value belongs to
	 * {@link DomainElement1D}.
	 * 
	 * @param value
	 *            to check for the inclusion.
	 * @return <code>true</code> if the value belongs to this {@link DomainElement1D},
	 *         <code>false</code> otherwise.
	 */
	public boolean contains(final double value);

	/**
	 * This methods can be used to check whether or not a given value belongs to
	 * {@link DomainElement1D}.
	 * 
	 * @param value
	 *            to check for the inclusion.
	 * @return <code>true</code> if the value belongs to this {@link DomainElement1D},
	 *         <code>false</code> otherwise.
	 */
	public boolean contains(final Number value);

	/**
	 * This methods can be used to check whether or not a given
	 * {@link NumberRange} belongs to {@link DomainElement1D}.
	 * 
	 * @param value
	 *            to check for the inclusion.
	 * @return <code>true</code> if the {@link NumberRange} belongs to this
	 *         {@link DomainElement1D}, <code>false</code> otherwise.
	 */
	public boolean contains(final NumberRange range);

}