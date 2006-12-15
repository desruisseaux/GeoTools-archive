/*
 * This implementation of the OGC Feature Geometry Abstract Specification
 * (ISO 19107) is a project of the University of Applied Sciences Cologne
 * (Fachhochschule Köln) in collaboration with GeoTools and GeoAPI.
 *
 * Copyright (C) 2006 University of Applied Sciences Köln
 *                    (Fachhochschule Köln) and GeoTools
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * For more information, contact:
 *
 *     Prof. Dr. Jackson Roehrig
 *     Institut für Technologie in den Tropen
 *     Fachhochschule Köln
 *     Betzdorfer Strasse 2
 *     D-50679 Köln
 *     Jackson.Roehrig@fh-koeln.de
 *
 *     Sanjay Dominik Jena
 *     san.jena@gmail.com
 *
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
