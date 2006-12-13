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



package org.geotools.geometry.iso.operation;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.geotools.geometry.iso.primitive.CurveImpl;
import org.geotools.geometry.iso.primitive.SurfaceImpl;
import org.geotools.geometry.iso.root.GeometryImpl;
import org.geotools.geometry.iso.topograph2D.Coordinate;
import org.geotools.geometry.iso.topograph2D.Edge;
import org.geotools.geometry.iso.topograph2D.EdgeIntersection;
import org.geotools.geometry.iso.topograph2D.GeometryGraph;
import org.geotools.geometry.iso.topograph2D.index.SegmentIntersector;
import org.geotools.geometry.iso.util.algorithm2D.LineIntersector;
import org.geotools.geometry.iso.util.algorithm2D.RobustLineIntersector;
import org.opengis.spatialschema.geometry.Geometry;

/**
 * Tests whether a <code>Geometry</code> is simple. In general, the SFS
 * specification of simplicity follows the rule:
 * <UL>
 * <LI> A Geometry is simple iff the only self-intersections are at boundary
 * points.
 * </UL>
 * Simplicity is defined for each {@link Geometry} subclass as follows:
 * <ul>
 * <li>Valid polygonal geometries are simple by definition, so
 * <code>isSimple</code> trivially returns true.
 * <li>Linear geometries are simple iff they do not self-intersect at points
 * other than boundary points.
 * <li>Zero-dimensional geometries (points) are simple iff they have no
 * repeated points.
 * <li>Empty <code>Geometry</code>s are always simple
 * <ul>
 */
public class IsSimpleOp {

	public IsSimpleOp() {
	}

	public boolean isSimple(CurveImpl geom) {
		return isSimpleLinearGeometry(geom);
	}

	public boolean isSimple(SurfaceImpl geom) {
		return isSimpleLinearGeometry(geom);
	}
	
	// public boolean isSimple(MultiLineString geom) {
	// return isSimpleLinearGeometry(geom);
	// }

	/**
	 * A MultiPoint is simple iff it has no repeated points
	 */
	// public boolean isSimple(MultiPoint mp) {
	// if (mp.isEmpty())
	// return true;
	// Set points = new TreeSet();
	// for (int i = 0; i < mp.getNumGeometries(); i++) {
	// Point pt = (Point) mp.getGeometryN(i);
	// Coordinate p = pt.getCoordinate();
	// if (points.contains(p))
	// return false;
	// points.add(p);
	// }
	// return true;
	// }

	/**
	 * Works for:
	 * - Curve
	 * - ...
	 * 
	 * @param geom
	 * @return
	 */
	private boolean isSimpleLinearGeometry(GeometryImpl geom) {

		// TODO auskommentiert; checken!
		// if (geom.isEmpty())
		// return true;

		GeometryGraph graph = new GeometryGraph(0, geom);
		LineIntersector li = new RobustLineIntersector();
		SegmentIntersector si = graph.computeSelfNodes(li, true);
		// if no self-intersection, must be simple
		
		// Primitives can be simple even if their boundary intersect
		// Complexes are not simple if their boundary intersect
		// TODO Bedeutung der attribute checken und entsprechend anpassen!
		if (!si.hasIntersection())
			return true;
		if (si.hasProperIntersection())
			return false;
		if (this.hasNonEndpointIntersection(graph))
			return false;
		if (this.hasClosedEndpointIntersection(graph))
			return false;
		return true;
	}

	/**
	 * For all edges, check if there are any intersections which are NOT at an
	 * endpoint. The Geometry is not simple if there are intersections not at
	 * endpoints.
	 */
	private boolean hasNonEndpointIntersection(GeometryGraph graph) {
		for (Iterator i = graph.getEdgeIterator(); i.hasNext();) {
			Edge e = (Edge) i.next();
			int maxSegmentIndex = e.getMaximumSegmentIndex();
			for (Iterator eiIt = e.getEdgeIntersectionList().iterator(); eiIt
					.hasNext();) {
				EdgeIntersection ei = (EdgeIntersection) eiIt.next();
				if (!ei.isEndPoint(maxSegmentIndex))
					return true;
			}
		}
		return false;
	}

	class EndpointInfo {
		Coordinate pt;

		boolean isClosed;

		int degree;

		EndpointInfo(Coordinate pt) {
			this.pt = pt;
			isClosed = false;
			degree = 0;
		}

		void addEndpoint(boolean isClosed) {
			degree++;
			this.isClosed |= isClosed;
		}
	}

	/**
	 * Test that no edge intersection is the endpoint of a closed line. To check
	 * this we compute the degree of each endpoint. The degree of endpoints of
	 * closed lines must be exactly 2.
	 */
	private boolean hasClosedEndpointIntersection(GeometryGraph graph) {
		Map endPoints = new TreeMap();
		for (Iterator i = graph.getEdgeIterator(); i.hasNext();) {
			Edge e = (Edge) i.next();
			int maxSegmentIndex = e.getMaximumSegmentIndex();
			boolean isClosed = e.isClosed();
			Coordinate p0 = e.getCoordinate(0);
			addEndpoint(endPoints, p0, isClosed);
			Coordinate p1 = e.getCoordinate(e.getNumPoints() - 1);
			addEndpoint(endPoints, p1, isClosed);
		}

		for (Iterator i = endPoints.values().iterator(); i.hasNext();) {
			EndpointInfo eiInfo = (EndpointInfo) i.next();
			if (eiInfo.isClosed && eiInfo.degree != 2)
				return true;
		}
		return false;
	}

	/**
	 * Add an endpoint to the map, creating an entry for it if none exists
	 */
	private void addEndpoint(Map endPoints, Coordinate p, boolean isClosed) {
		EndpointInfo eiInfo = (EndpointInfo) endPoints.get(p);
		if (eiInfo == null) {
			eiInfo = new EndpointInfo(p);
			endPoints.put(p, eiInfo);
		}
		eiInfo.addEndpoint(isClosed);
	}

}
