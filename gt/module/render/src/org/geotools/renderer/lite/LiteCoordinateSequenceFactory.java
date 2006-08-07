/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, Geotools Project Managment Committee (PMC)
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
package org.geotools.renderer.lite;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFactory;

/**
 * Connect JTS Geometry creation directly to an implementation of
 * coordinate sequence backed by a double array.
 * 
 * @author jeichar
 * @since 2.1.x
 * @source $URL$
 */
public final class LiteCoordinateSequenceFactory implements CoordinateSequenceFactory {

	/* (non-Javadoc)
	 * @see com.vividsolutions.jts.geom.CoordinateSequenceFactory#create(com.vividsolutions.jts.geom.Coordinate[])
	 */
	public CoordinateSequence create(Coordinate[] coordinates) {
		return new LiteCoordinateSequence(coordinates);
	}

	/* (non-Javadoc)
	 * @see com.vividsolutions.jts.geom.CoordinateSequenceFactory#create(com.vividsolutions.jts.geom.CoordinateSequence)
	 */
	public CoordinateSequence create(CoordinateSequence coordSeq) {
		return new LiteCoordinateSequence(coordSeq.toCoordinateArray());
	}

	/* (non-Javadoc)
	 * @see com.vividsolutions.jts.geom.CoordinateSequenceFactory#create(int, int)
	 */
	public CoordinateSequence create(int size, int dimension) {
		return new LiteCoordinateSequence(size, dimension);
	}

	/**
	 * @param points
	 * @return
	 */
	public CoordinateSequence create(double[] points) {
		return new LiteCoordinateSequence(points);
	}
}
