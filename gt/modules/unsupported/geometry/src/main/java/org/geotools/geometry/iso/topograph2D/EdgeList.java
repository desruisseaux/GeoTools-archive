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



package org.geotools.geometry.iso.topograph2D;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.geotools.geometry.iso.index.SpatialIndex;
import org.geotools.geometry.iso.index.quadtree.Quadtree;

/**
 * A EdgeList is a list of Edges. It supports locating edges that are pointwise
 * equals to a target edge.
 */
public class EdgeList {
	
	private List edges = new ArrayList();

	/**
	 * An index of the edges, for fast lookup.
	 * 
	 * a Quadtree is used, because this index needs to be dynamic (e.g. allow
	 * insertions after queries). An alternative would be to use an ordered set
	 * based on the values of the edge coordinates
	 * 
	 */
	private SpatialIndex index = new Quadtree();

	public EdgeList() {
	}

	/**
	 * Insert an edge unless it is already in the list
	 */
	public void add(Edge e) {
		edges.add(e);
		index.insert(e.getEnvelope(), e);
	}

	public void addAll(Collection edgeColl) {
		for (Iterator i = edgeColl.iterator(); i.hasNext();) {
			add((Edge) i.next());
		}
	}

	public List getEdges() {
		return edges;
	}

	// <FIX> fast lookup for edges
	/**
	 * If there is an edge equal to e already in the list, return it. Otherwise
	 * return null.
	 * 
	 * @return equal edge, if there is one already in the list null otherwise
	 */
	public Edge findEqualEdge(Edge e) {
		Collection testEdges = index.query(e.getEnvelope());

		for (Iterator i = testEdges.iterator(); i.hasNext();) {
			Edge testEdge = (Edge) i.next();
			if (testEdge.equals(e))
				return testEdge;
		}
		return null;
	}

	public Iterator iterator() {
		return edges.iterator();
	}

	public Edge get(int i) {
		return (Edge) edges.get(i);
	}

	/**
	 * If the edge e is already in the list, return its index.
	 * 
	 * @return index, if e is already in the list -1 otherwise
	 */
	public int findEdgeIndex(Edge e) {
		for (int i = 0; i < edges.size(); i++) {
			if (((Edge) edges.get(i)).equals(e))
				return i;
		}
		return -1;
	}

	public void print(PrintStream out) {
		out.print("MULTILINESTRING ( ");
		for (int j = 0; j < edges.size(); j++) {
			Edge e = (Edge) edges.get(j);
			if (j > 0)
				out.print(",");
			out.print("(");
			Coordinate[] pts = e.getCoordinates();
			for (int i = 0; i < pts.length; i++) {
				if (i > 0)
					out.print(",");
				out.print(pts[i].x + " " + pts[i].y);
			}
			out.println(")");
		}
		out.print(")  ");
	}

}
