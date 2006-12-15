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

package org.geotools.geometry.iso.io;

import java.util.List;

import org.geotools.geometry.iso.coordinate.CurveSegmentImpl;
import org.geotools.geometry.iso.coordinate.PositionImpl;
import org.geotools.geometry.iso.coordinate.TriangleImpl;
import org.geotools.geometry.iso.primitive.CurveImpl;
import org.opengis.spatialschema.geometry.geometry.Position;
import org.opengis.spatialschema.geometry.geometry.Triangle;
import org.opengis.spatialschema.geometry.primitive.Curve;


/**
 * A CollectionFactory creates lists of different GM_Objects or Coordinates.
 * The original objective of this encapsulating is the possibility of persistence support,
 * that is that the returned lists are backed by a type of database to relieve the system
 * memory in cases of large geometries.
 * 
 * @author Sanjay Jena and Prof. Dr. Jackson Roehrig
 *
 */
public interface CollectionFactory {
	
	/**
	 * Creates a list of Curves
	 * 
	 * @return List
	 */
	List<CurveImpl> getCurveList();

	/**
	 * Creates a list of CurveSegments
	 * 
	 * @return List
	 */
	List<CurveSegmentImpl> getCurveSegmentList();

	
	/**
	 * Creates a list of Positions
	 * 
	 * @return List
	 */
	List<PositionImpl> getPositionList();

	/**
	 * Creates a list of Triangles
	 * 
	 * @return List
	 */
	List<TriangleImpl> createTriangleList();



}
