/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.filter.function;

import org.geotools.feature.Feature;

public abstract class RangedClassificationFunction extends
		ClassificationFunction {

	abstract public String getName();

	abstract public Object evaluate(Feature feature);

	/**
	 * Returns the lower bound value for the bin.
	 * @param index
	 * @return
	 */
	abstract public Object getMin(int index);
	
	/**
	 * Returns the upper bound value for the bin.
	 * @param index
	 * @return
	 */
	abstract public Object getMax(int index);
	
}
