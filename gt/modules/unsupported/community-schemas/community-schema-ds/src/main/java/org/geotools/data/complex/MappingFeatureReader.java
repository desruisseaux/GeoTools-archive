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

package org.geotools.data.complex;

import java.io.IOException;
import java.util.NoSuchElementException;

import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;

/**
 * A FeatureReader that uses a
 * {@linkplain org.geotools.data.complex.FeatureTypeMapping} to perform Feature
 * fetching.
 * 
 * <p>
 * Note that the number of Features available from a MappingFeatureReader may
 * not match the number of features that resulted of executing the incoming
 * query over the surrogate FeatureSource. This will be the case when grouping
 * attributes has configured on the FeatureTypeMapping this reader is based on.
 * </p>
 * <p>
 * When a MappingFeatureReader is created, a delegated FeatureIterator will be
 * created based on the information provided by the FeatureTypeMapping object.
 * That delegate reader will be specialized in applying the appropiate mapping
 * stratagy based on wether grouping has to be performed or not.
 * </p>
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @see org.geotools.data.complex.DefaultMappingFeatureIterator
 * @see org.geotools.data.complex.GroupingFeatureIterator
 */
class MappingFeatureReader implements FeatureReader {

	private FeatureTypeMapping mapping;

	/**
	 * A strategy iterator that actually uses the FeatureTypeMapping to
	 * construct Features of the target FeatureType.
	 */
	private AbstractMappingFeatureIterator iterator;

	/**
	 * Creates a MappingFeatureReader to fetch all the available derived
	 * Features.
	 * 
	 * @param mapping
	 * @throws IOException
	 */
	public MappingFeatureReader(FeatureTypeMapping mapping) throws IOException {
		this(mapping, Query.ALL);
	}

	/**
	 * 
	 * @param mapping
	 * @param query
	 * @throws IOException
	 */
	public MappingFeatureReader(FeatureTypeMapping mapping, Query query)
			throws IOException {
		this.mapping = mapping;

		if (0 == mapping.getGroupByAttNames().size()) {
			this.iterator = new DefaultMappingFeatureIterator(mapping, query);
		} else {
			this.iterator = new GroupingFeatureIterator(mapping, query);
		}

	}

	/**
	 * @return the target FeatureType
	 */
	public FeatureType getFeatureType() {
		return (FeatureType) mapping.getTargetFeature().getType();
	}

	/**
	 * @return a Feature conformant to the target FeatureType by applying the
	 *         attribute mappings defined in the {@linkplain FeatureTypeMapping}
	 */
	public Feature next() throws IOException, IllegalAttributeException,
			NoSuchElementException {
		return (Feature) iterator.next();
	}

	/**
	 * @return wether there are more Features to be read
	 */
	public boolean hasNext() throws IOException {
		return iterator.hasNext();
	}

	/**
	 * Closes the underlying (source) FeatureReader
	 */
	public void close() throws IOException {
		this.iterator.close();
	}
}
