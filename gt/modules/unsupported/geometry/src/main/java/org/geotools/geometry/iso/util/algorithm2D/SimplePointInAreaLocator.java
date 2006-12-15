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
/*
 * This class was copied from the JTS Topology Suite Version 1.7.2
 * of Vivid Solutions and modified and reused in this library under
 * the terms of GNU Lesser General Public Licence.
 * The original copyright of the Vivid Solutions JTS is stated as follows:
 *
 *------------------------------------------------------------------------
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
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
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 *------------------------------------------------------------------------
 */
package org.geotools.geometry.iso.util.algorithm2D;

import java.util.List;

import org.geotools.geometry.iso.primitive.RingImpl;
import org.geotools.geometry.iso.primitive.SurfaceImpl;
import org.geotools.geometry.iso.root.GeometryImpl;
import org.geotools.geometry.iso.topograph2D.Coordinate;
import org.geotools.geometry.iso.topograph2D.Location;
import org.geotools.geometry.iso.topograph2D.util.CoordinateArrays;
import org.opengis.spatialschema.geometry.Geometry;

/**
 * Computes whether a point lies in the interior of an area {@link Geometry}.
 * The algorithm used is only guaranteed to return correct results for points
 * which are <b>not</b> on the boundary of the Geometry.
 */
public class SimplePointInAreaLocator {

	/**
	 * locate is the main location function. It handles both single-element and
	 * multi-element Geometries. The algorithm for multi-element Geometries is
	 * more complex, since it has to take into account the boundaryDetermination
	 * rule
	 * 
	 * @param p
	 * @param geom
	 * @return
	 */
	public static int locate(Coordinate p, GeometryImpl geom) {
		// TODO auskommentiert; checken!
		// if (geom.isEmpty())
		// return Location.EXTERIOR;

		if (containsPoint(p, geom))
			return Location.INTERIOR;
		return Location.EXTERIOR;
	}

	private static boolean containsPoint(Coordinate p, GeometryImpl geom) {

		if (geom instanceof SurfaceImpl) {
			return containsPointInPolygon(p, (SurfaceImpl) geom);
		}
		// TODO auskommentiert; checken!
		// else if (geom instanceof GeometryCollection) {
		// Iterator geomi = new GeometryCollectionIterator(
		// (GeometryCollection) geom);
		// while (geomi.hasNext()) {
		// GeometryImpl g2 = (GeometryImpl) geomi.next();
		// if (g2 != geom)
		// if (containsPoint(p, g2))
		// return true;
		// }
		// }
		return false;

		// OLD CODE:
		// if (geom instanceof Polygon) {
		// return containsPointInPolygon(p, (Polygon) geom);
		// } else if (geom instanceof GeometryCollection) {
		// Iterator geomi = new GeometryCollectionIterator(
		// (GeometryCollection) geom);
		// while (geomi.hasNext()) {
		// Geometry g2 = (Geometry) geomi.next();
		// if (g2 != geom)
		// if (containsPoint(p, g2))
		// return true;
		// }
		// }
		// return false;

	}

	public static boolean containsPointInPolygon(Coordinate p,
			SurfaceImpl aSurface) {

		// TODO auskommentiert; checken!
		// if (poly.isEmpty())
		// return false;

		List<RingImpl> rings = aSurface.getBoundaryRings();
		RingImpl shell = rings.get(0);

		// The point lies in the ring defined by the coordinatearray
		// representation of the exterior ring?
		// if not, return false
		if (!CGAlgorithms.isPointInRing(p, CoordinateArrays
				.toCoordinateArray(shell.asDirectPositions()))) {
			return false;
		}

		// The point lies in the ring defined by the coordinatearray
		// representation of the exterior ring?
		for (int i = 1; i < rings.size(); i++) {
			RingImpl hole = (RingImpl) rings.get(i);
			// if so, return false
			if (CGAlgorithms.isPointInRing(p, CoordinateArrays
					.toCoordinateArray(hole.asDirectPositions()))) {
				return false;
			}
		}

		// the point lies inside the exterior ring, and outside the holes, so
		// its on the polygon
		return true;

		// OLD CODE:
		// if (poly.isEmpty())
		// return false;
		// LinearRing shell = (LinearRing) poly.getExteriorRing();
		// if (!CGAlgorithms.isPointInRing(p, shell.getCoordinates()))
		// return false;
		// // now test if the point lies in or on the holes
		// for (int i = 0; i < poly.getNumInteriorRing(); i++) {
		// LinearRing hole = (LinearRing) poly.getInteriorRingN(i);
		// if (CGAlgorithms.isPointInRing(p, hole.getCoordinates()))
		// return false;
		// }
		// return true;

	}

}
