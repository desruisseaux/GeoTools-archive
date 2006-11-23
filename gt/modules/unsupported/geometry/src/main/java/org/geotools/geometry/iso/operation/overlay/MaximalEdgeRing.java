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



package org.geotools.geometry.iso.operation.overlay;

import java.util.ArrayList;
import java.util.List;

import org.geotools.geometry.iso.FeatGeomFactoryImpl;
import org.geotools.geometry.iso.topograph2D.DirectedEdge;
import org.geotools.geometry.iso.topograph2D.DirectedEdgeStar;
import org.geotools.geometry.iso.topograph2D.EdgeRing;
import org.geotools.geometry.iso.topograph2D.Node;
import org.geotools.geometry.iso.util.algorithm2D.CGAlgorithms;


/**
 * A ring of {@link edges} which may contain nodes of degree > 2. A
 * MaximalEdgeRing may represent two different spatial entities:
 * <ul>
 * <li>a single polygon possibly containing inversions (if the ring is oriented
 * CW)
 * <li>a single hole possibly containing exversions (if the ring is oriented
 * CCW)
 * </ul>
 * If the MaximalEdgeRing represents a polygon, the interior of the polygon is
 * strongly connected.
 * <p>
 * These are the form of rings used to define polygons under some spatial data
 * models. However, under the OGC SFS model, {@link MinimalEdgeRings} are
 * required. A MaximalEdgeRing can be converted to a list of MinimalEdgeRings
 * using the {@link #buildMinimalRings() } method.
 * 
 */
public class MaximalEdgeRing extends EdgeRing {

	public MaximalEdgeRing(DirectedEdge start,
			FeatGeomFactoryImpl geometryFactory, CGAlgorithms cga) {
		super(start, geometryFactory, cga);
	}

	public DirectedEdge getNext(DirectedEdge de) {
		return de.getNext();
	}

	public void setEdgeRing(DirectedEdge de, EdgeRing er) {
		de.setEdgeRing(er);
	}

	/**
	 * For all nodes in this EdgeRing, link the DirectedEdges at the node to
	 * form minimalEdgeRings
	 */
	public void linkDirectedEdgesForMinimalEdgeRings() {
		DirectedEdge de = startDe;
		do {
			Node node = de.getNode();
			((DirectedEdgeStar) node.getEdges()).linkMinimalDirectedEdges(this);
			de = de.getNext();
		} while (de != startDe);
	}

	public List buildMinimalRings() {
		List minEdgeRings = new ArrayList();
		DirectedEdge de = startDe;
		do {
			if (de.getMinEdgeRing() == null) {
				EdgeRing minEr = new MinimalEdgeRing(de,
						super.mFeatGeomFactory, cga);
				minEdgeRings.add(minEr);
			}
			de = de.getNext();
		} while (de != startDe);
		return minEdgeRings;
	}

}
