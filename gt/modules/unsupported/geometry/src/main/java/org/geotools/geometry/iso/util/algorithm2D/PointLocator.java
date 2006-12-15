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

import org.geotools.geometry.iso.primitive.CurveImpl;
import org.geotools.geometry.iso.primitive.RingImpl;
import org.geotools.geometry.iso.primitive.SurfaceImpl;
import org.geotools.geometry.iso.root.GeometryImpl;
import org.geotools.geometry.iso.topograph2D.Coordinate;
import org.geotools.geometry.iso.topograph2D.GeometryGraph;
import org.geotools.geometry.iso.topograph2D.Location;
import org.geotools.geometry.iso.topograph2D.util.CoordinateArrays;
import org.opengis.spatialschema.geometry.Geometry;

/**
 * Computes the topological relationship ({@link Location}) of a single point
 * to a {@link Geometry}. The algorithm obeys the SFS Boundary Determination
 * Rule to determine whether the point lies on the boundary or not.
 * <p>
 * Notes:
 * <ul>
 * <li>{@link LinearRing}s do not enclose any area - points inside the ring
 * are still in the EXTERIOR of the ring.
 * </ul>
 * Instances of this class are not reentrant.
 * 
 */
public class PointLocator {
	private boolean isIn; // true if the point lies in or on any Geometry

	// element

	private int numBoundaries; // the number of sub-elements whose boundaries

	// the point lies in

	public PointLocator() {
	}

	/**
	 * Convenience method to test a point for intersection with a Geometry
	 * 
	 * @param p
	 *            the coordinate to test
	 * @param geom
	 *            the Geometry to test
	 * @return <code>true</code> if the point is in the interior or boundary
	 *         of the Geometry
	 */
	public boolean intersects(Coordinate p, GeometryImpl geom) {
		return locate(p, geom) != Location.EXTERIOR;
	}

	/**
	 * Computes the topological relationship ({@link Location}) of a single
	 * point to a Geometry. It handles both single-element and multi-element
	 * Geometries. The algorithm for multi-part Geometries takes into account
	 * the SFS Boundary Determination Rule.
	 * 
	 * @return the {@link Location} of the point relative to the input Geometry
	 */
	public int locate(Coordinate p, GeometryImpl geom) {

		// TODO auskommentiert; checken!
		// if (geom.isEmpty())
		// return Location.EXTERIOR;

		if (geom instanceof CurveImpl) {
			return locate(p, (CurveImpl) geom);
		} else if (geom instanceof SurfaceImpl) {
			return locate(p, (SurfaceImpl) geom);
		}

		isIn = false;
		numBoundaries = 0;
		computeLocation(p, geom);
		if (GeometryGraph.isInBoundary(numBoundaries))
			return Location.BOUNDARY;
		if (numBoundaries > 0 || isIn)
			return Location.INTERIOR;
		return Location.EXTERIOR;
	}

	private void computeLocation(Coordinate p, GeometryImpl geom) {
		if (geom instanceof CurveImpl) {
			updateLocationInfo(locate(p, (CurveImpl) geom));
		} else if (geom instanceof SurfaceImpl) {
			updateLocationInfo(locate(p, (SurfaceImpl) geom));
		}
		// else if (geom instanceof MultiLineString) {
		// MultiLineString ml = (MultiLineString) geom;
		// for (int i = 0; i < ml.getNumGeometries(); i++) {
		// LineString l = (LineString) ml.getGeometryN(i);
		// updateLocationInfo(locate(p, l));
		// }
		// } else if (geom instanceof MultiPolygon) {
		// MultiPolygon mpoly = (MultiPolygon) geom;
		// for (int i = 0; i < mpoly.getNumGeometries(); i++) {
		// Polygon poly = (Polygon) mpoly.getGeometryN(i);
		// updateLocationInfo(locate(p, poly));
		// }
		// } else if (geom instanceof GeometryCollection) {
		// Iterator geomi = new GeometryCollectionIterator(
		// (GeometryCollection) geom);
		// while (geomi.hasNext()) {
		// GeometryImpl g2 = (GeometryImpl) geomi.next();
		// if (g2 != geom)
		// computeLocation(p, g2);
		// }
		// }
	}

	private void updateLocationInfo(int loc) {
		if (loc == Location.INTERIOR)
			isIn = true;
		if (loc == Location.BOUNDARY)
			numBoundaries++;
	}

	private int locate(Coordinate p, CurveImpl curve) {
		Coordinate[] pt = CoordinateArrays.toCoordinateArray(curve
				.asDirectPositions());

		// Annahme: curve.isClosed() = curve.isCycle() ??
		// if (!curve.isClosed()) {
		if (!curve.getStartPoint().equals(curve.getEndPoint())) {
			if (p.equals(pt[0]) || p.equals(pt[pt.length - 1])) {
				return Location.BOUNDARY;
			}
		}
		if (CGAlgorithms.isOnLine(p, pt))
			return Location.INTERIOR;
		return Location.EXTERIOR;
	}

	private int locateInPolygonRing(Coordinate p, RingImpl ring) {
		// can this test be folded into isPointInRing ?
		Coordinate[] coord = CoordinateArrays.toCoordinateArray(ring
				.asDirectPositions());
		if (CGAlgorithms.isOnLine(p, coord)) {
			return Location.BOUNDARY;
		}
		if (CGAlgorithms.isPointInRing(p, coord))
			return Location.INTERIOR;
		return Location.EXTERIOR;
	}

	private int locate(Coordinate p, SurfaceImpl aSurface) {

		// if (poly.isEmpty())
		// return Location.EXTERIOR;
		List<RingImpl> rings = aSurface.getBoundaryRings();
		RingImpl shell = rings.get(0);

		int shellLoc = locateInPolygonRing(p, shell);
		if (shellLoc == Location.EXTERIOR)
			return Location.EXTERIOR;
		if (shellLoc == Location.BOUNDARY)
			return Location.BOUNDARY;
		// now test if the point lies in or on the holes
		for (int i = 1; i < rings.size(); i++) {
			RingImpl hole = (RingImpl) rings.get(i);
			int holeLoc = locateInPolygonRing(p, hole);
			if (holeLoc == Location.INTERIOR)
				return Location.EXTERIOR;
			if (holeLoc == Location.BOUNDARY)
				return Location.BOUNDARY;
		}
		return Location.INTERIOR;

		// OLD CODE:
		// if (poly.isEmpty())
		// return Location.EXTERIOR;
		// LinearRing shell = (LinearRing) poly.getExteriorRing();
		//
		// int shellLoc = locateInPolygonRing(p, shell);
		// if (shellLoc == Location.EXTERIOR)
		// return Location.EXTERIOR;
		// if (shellLoc == Location.BOUNDARY)
		// return Location.BOUNDARY;
		// // now test if the point lies in or on the holes
		// for (int i = 0; i < poly.getNumInteriorRing(); i++) {
		// LinearRing hole = (LinearRing) poly.getInteriorRingN(i);
		// int holeLoc = locateInPolygonRing(p, hole);
		// if (holeLoc == Location.INTERIOR)
		// return Location.EXTERIOR;
		// if (holeLoc == Location.BOUNDARY)
		// return Location.BOUNDARY;
		// }
		// return Location.INTERIOR;

	}
}
