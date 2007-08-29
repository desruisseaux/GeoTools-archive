/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2007, Geotools Project Managment Committee (PMC)
 *    (C) 2007, GeoSolutions S.A.S.
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
package org.geotools.parameter;

import java.awt.image.RenderedImage;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.media.jai.OperationDescriptor;
import javax.media.jai.RegistryElementDescriptor;
import javax.media.jai.registry.RenderedRegistryMode;

import org.geotools.referencing.AbstractIdentifiedObject;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterDescriptor;

/**
 * This class allows to decorate instances of
 * {@link ImagingParameterDescriptors} in order to change the behaviour of some
 * parameters for the JAI operations we describe, in order to make them
 * geospatial aware.
 * 
 * @author Simone Giannecchini, GeoSolutions.
 * @since 2.4.x
 * 
 */
public class ImagingParameterDescriptorsDecorator extends
		ImagingParameterDescriptors {

	/**
	 * 
	 */
	private static final long serialVersionUID = 85606397710901457L;

	/**
	 * This {@link Map} contains the descriptors for which we want to to change
	 * the behaviour.
	 */
	protected final Map replacedDescriptors;

	/**
	 * Constructs a parameter descriptor wrapping the specified JAI operation,
	 * including sources. The
	 * {@linkplain #getName name for this parameter group} will be inferred from
	 * the
	 * {@linkplain RegistryElementDescriptor#getName name of the supplied registry element}
	 * using the {@link #properties properties} method.
	 * 
	 * The <cite>source type map</cite> default to a (<code>{@linkplain RenderedImage}.class</code>,
	 * <code>{@linkplain GridCoverage}.class</code>) key-value pair and the
	 * <cite>registry mode</cite> default to
	 * {@link RenderedRegistryMode#MODE_NAME "rendered"}.
	 * 
	 * @param operation
	 *            The JAI's operation descriptor, usually as an instance of
	 *            {@link OperationDescriptor}.
	 */
	public ImagingParameterDescriptorsDecorator(
			final RegistryElementDescriptor operation,
			final Map replacedDescriptors) {
		super(properties(operation), operation, DEFAULT_SOURCE_TYPE_MAP,
				RenderedRegistryMode.MODE_NAME);

		// map contaning the replaced descriptors
		this.replacedDescriptors = new HashMap(replacedDescriptors);
	}

	/**
	 * Creates a new instance of parameter value group. A JAI
	 * {@link javax.media.jai.ParameterList} is created for holding parameter
	 * values, and wrapped into an {@link ImagingParameters} instance.
	 */
	public GeneralParameterValue createValue() {
		final int size = replacedDescriptors.size();
		final Map replacedValues = new HashMap(size);
		final Iterator it = replacedDescriptors.keySet().iterator();
		while (it.hasNext()) {
			final String key = (String) it.next();
			final ParameterDescriptor pd = (ParameterDescriptor) replacedDescriptors
					.get(key);
			replacedValues.put(key, pd.createValue());
		}
		return new ImagingParametersDecorator(this, replacedValues);
	}

	/**
	 * Compares the specified object with this parameter group for equality.
	 * 
	 * @param object
	 *            The object to compare to {@code this}.
	 * @param compareMetadata
	 *            {@code true} for performing a strict comparaison, or
	 *            {@code false} for comparing only properties relevant to
	 *            transformations.
	 * @return {@code true} if both objects are equal. XXX this is not rigorous
	 */
	public boolean equals(AbstractIdentifiedObject object,
			boolean compareMetadata) {
		boolean retVal = super.equals(object, compareMetadata);
		if (retVal) {
			final ImagingParameterDescriptorsDecorator that = (ImagingParameterDescriptorsDecorator) object;
			retVal &= (this.replacedDescriptors
					.equals(that.replacedDescriptors));
		}

		return retVal;
	}

	/**
	 * Returns a hash value for this parameter. This value doesn't need to be
	 * the same in past or future versions of this class.
	 */
	public int hashCode() {
		return super.hashCode() ^ replacedDescriptors.hashCode();
	}

}
