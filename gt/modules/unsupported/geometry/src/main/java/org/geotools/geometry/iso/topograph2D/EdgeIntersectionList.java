/*
 * This implementation of the OGC Feature Geometry Abstract Specification
 * (ISO 19107) is a project of the University of Applied Sciences Cologne
 * (Fachhochschule K�ln) in collaboration with GeoTools and GeoAPI.
 *
 * Copyright (C) 2006 University of Applied Sciences K�ln
 *                    (Fachhochschule K�ln) and GeoTools
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
 *     Institut f�r Technologie in den Tropen
 *     Fachhochschule K�ln
 *     Betzdorfer Strasse 2
 *     D-50679 K�ln
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



package org.geotools.geometry.iso.topograph2D;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


/**
 * A list of edge intersections along an {@link Edge}. Implements splitting an
 * edge with intersections into multiple resultant edges.
 */
public class EdgeIntersectionList {
	
	// a Map <EdgeIntersection, EdgeIntersection>
	private Map nodeMap = new TreeMap();

	Edge edge; // the parent edge

	public EdgeIntersectionList(Edge edge) {
		this.edge = edge;
	}

	/**
	 * Adds an intersection into the list, if it isn't already there. The input
	 * segmentIndex and dist are expected to be normalized.
	 * 
	 * @return the EdgeIntersection found or added
	 */
	public EdgeIntersection add(Coordinate intPt, int segmentIndex, double dist) {
		EdgeIntersection eiNew = new EdgeIntersection(intPt, segmentIndex, dist);
		EdgeIntersection ei = (EdgeIntersection) nodeMap.get(eiNew);
		if (ei != null) {
			return ei;
		}
		nodeMap.put(eiNew, eiNew);
		return eiNew;
	}

	/**
	 * Returns an iterator of {@link EdgeIntersection}s
	 * 
	 * @return an Iterator of EdgeIntersections
	 */
	public Iterator iterator() {
		return nodeMap.values().iterator();
	}

	/**
	 * Tests if the given point is an edge intersection
	 * 
	 * @param pt
	 *            the point to test
	 * @return true if the point is an intersection
	 */
	public boolean isIntersection(Coordinate pt) {
		for (Iterator it = iterator(); it.hasNext();) {
			EdgeIntersection ei = (EdgeIntersection) it.next();
			if (ei.coord.equals(pt))
				return true;
		}
		return false;
	}

	/**
	 * Adds entries for the first and last points of the edge to the list
	 */
	public void addEndpoints() {
		int maxSegIndex = edge.pts.length - 1;
		add(edge.pts[0], 0, 0.0);
		add(edge.pts[maxSegIndex], maxSegIndex, 0.0);
	}

	/**
	 * Creates new edges for all the edges that the intersections in this list
	 * split the parent edge into. Adds the edges to the input list (this is so
	 * a single list can be used to accumulate all split edges for a Geometry).
	 * 
	 * @param edgeList
	 *            a list of EdgeIntersections
	 */
	public void addSplitEdges(List edgeList) {
		// ensure that the list has entries for the first and last point of the
		// edge
		// e.g., add the end points of the geometry this edgeIntersectionList
		addEndpoints();

		// Iterator of the Edge Intersections
		Iterator it = iterator();
		// there should always be at least two entries in the list
		// SJ TODO The EdgeIntersection are ordered, that�s making the edge
		// generation easier - but how comes? where is the list of
		// edgeIntersections generated in ordered sequence?
		EdgeIntersection eiPrev = (EdgeIntersection) it.next();
		while (it.hasNext()) {
			EdgeIntersection ei = (EdgeIntersection) it.next();
			// Create new Edge from Previous EdgeIntersection to the actual
			// EdgeIntersection; apply the same label as the edge which inhibts
			// the EdgeIntersections
			Edge newEdge = createSplitEdge(eiPrev, ei);
			edgeList.add(newEdge);

			eiPrev = ei;
		}
	}

	/**
	 * Create a new "split edge" with the section of points between (and
	 * including) the two intersections. The label for the new edge is the same
	 * as the label for the parent edge.
	 */
	Edge createSplitEdge(EdgeIntersection ei0, EdgeIntersection ei1) {
		// Debug.print("\ncreateSplitEdge"); Debug.print(ei0); Debug.print(ei1);
		int npts = ei1.segmentIndex - ei0.segmentIndex + 2;

		Coordinate lastSegStartPt = edge.pts[ei1.segmentIndex];
		// if the last intersection point is not equal to the its segment start
		// pt,
		// add it to the points list as well.
		// (This check is needed because the distance metric is not totally
		// reliable!)
		// The check for point equality is 2D only - Z values are ignored
		boolean useIntPt1 = ei1.dist > 0.0
				|| !ei1.coord.equals2D(lastSegStartPt);
		if (!useIntPt1) {
			npts--;
		}

		Coordinate[] pts = new Coordinate[npts];
		int ipt = 0;
		pts[ipt++] = new Coordinate(ei0.coord);
		for (int i = ei0.segmentIndex + 1; i <= ei1.segmentIndex; i++) {
			pts[ipt++] = edge.pts[i];
		}
		if (useIntPt1)
			pts[ipt] = ei1.coord;
		return new Edge(pts, new Label(edge.label));
	}

	public void print(PrintStream out) {
		out.println("Intersections:");
		for (Iterator it = iterator(); it.hasNext();) {
			EdgeIntersection ei = (EdgeIntersection) it.next();
			ei.print(out);
		}
	}
}
