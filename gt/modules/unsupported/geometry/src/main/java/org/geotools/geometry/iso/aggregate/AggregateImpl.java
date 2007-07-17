/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006-2007, GeoTools Project Management Committee (PMC)
 *    (C) 2006       University of Applied Sciences Köln (Fachhochschule Köln)
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

package org.geotools.geometry.iso.aggregate;

import java.util.Set;

import org.geotools.geometry.iso.root.GeometryImpl;
import org.opengis.geometry.aggregate.Aggregate;
import org.opengis.geometry.Geometry;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * @author roehrig
 * @param <T>
 * 
 */
public abstract class AggregateImpl extends GeometryImpl implements Aggregate {
	
	
	protected Set<? extends Geometry> elements = null;

	/**
	 * @param crs
	 */
	public AggregateImpl(CoordinateReferenceSystem crs, Set<? extends Geometry> elements) {
		super(crs);
		this.elements = elements;
	}

}
