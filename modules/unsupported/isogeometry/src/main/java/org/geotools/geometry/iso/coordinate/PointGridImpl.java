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


package org.geotools.geometry.iso.coordinate;

import java.util.ArrayList;

/**
 * Many of the geometric constructs in this International Standard require the
 * use of reference points which are organized into sequences or grids
 * (sequences of equal length sequences). PointArray::column[1..n] : Position
 * PointGrid::row[1..n] : PointArray
 * 
 * @author Jackson Roehrig & Sanjay Jena
 */
public class PointGridImpl {

	private ArrayList<PointArrayImpl> row = null;

	/**
	 * 
	 */
	public PointGridImpl() {
		this.row = new ArrayList<PointArrayImpl>();
	}

	/**
	 * @param pointArray
	 */
	public PointGridImpl(PointArrayImpl pointArray) {
		this.row = new ArrayList<PointArrayImpl>();
		this.row.add(pointArray);
	}

}
