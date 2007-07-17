/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006-2007, GeoTools Project Management Committee (PMC)
 *    (C) 2006       University of Applied Sciences K�ln (Fachhochschule K�ln)
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

import org.opengis.geometry.Boundary;
import org.opengis.geometry.aggregate.MultiPoint;
import org.opengis.geometry.primitive.Point;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * @author roehrig
 * 
 */
public class MultiPointImpl extends MultiPrimitiveImpl implements MultiPoint {

	/**
	 * Creates a MultiPoint by a set of Points.
	 * @param crs
	 * @param points Set of Points which shall be contained by the MultiPoint
	 */
	public MultiPointImpl(CoordinateReferenceSystem crs, Set<Point> points) {
		super(crs, points);
	}
	
	/* (non-Javadoc)
	 * @see org.geotools.geometry.featgeom.aggregate.MultiPrimitiveImpl#getBoundary()
	 */
	public Boundary getBoundary() {
		// Points don�t have a Boundary, so don�t have MultiPoints
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.geotools.geometry.featgeom.aggregate.MultiPrimitiveImpl#getElements()
	 */
	public Set<Point> getElements() {
		return (Set<Point>) super.elements;
	}

}
