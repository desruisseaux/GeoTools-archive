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



package org.geotools.geometry.iso.operation.overlay;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.geotools.geometry.iso.coordinate.DirectPositionImpl;
import org.geotools.geometry.iso.primitive.PointImpl;
import org.geotools.geometry.iso.topograph2D.Coordinate;
import org.geotools.geometry.iso.topograph2D.Label;
import org.geotools.geometry.iso.topograph2D.Node;
import org.geotools.geometry.iso.util.algorithm2D.PointLocator;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

// import java.util.*;
// import com.vividsolutions.jts.geom.*;
// import com.vividsolutions.jts.algorithm.*;
// import com.vividsolutions.jts.geomgraph.*;

/**
 * Constructs {@link org.opengis.geometry.primitive.Point}s from the nodes of an overlay graph.
 * 
 * @version 1.7.2
 */
public class PointBuilder {
	private OverlayOp op;

	//private FeatGeomFactoryImpl featGeomFactory;
	private CoordinateReferenceSystem crs;

	//private PrimitiveFactoryImpl primitiveFactory;

	private List resultPointList = new ArrayList();

	public PointBuilder(OverlayOp op, CoordinateReferenceSystem crs,
			PointLocator ptLocator) {
		this.op = op;
		this.crs = crs;
		//this.primitiveFactory = featGeomFactory.getPrimitiveFactory();
		// ptLocator is never used in this class
	}

	/**
	 * Computes the Point geometries which will appear in the result, given the
	 * specified overlay operation.
	 * 
	 * @return a list of the Points objects in the result
	 */
	public List build(int opCode) {
		extractNonCoveredResultNodes(opCode);
		/**
		 * It can happen that connected result nodes are still covered by result
		 * geometries, so must perform this filter. (For instance, this can
		 * happen during topology collapse).
		 */
		return resultPointList;
	}

	/**
	 * Determines nodes which are in the result, and creates {@link Point}s for
	 * them.
	 * 
	 * This method determines nodes which are candidates for the result via
	 * their labelling and their graph topology.
	 * 
	 * @param opCode
	 *            the overlay operation
	 */
	private void extractNonCoveredResultNodes(int opCode) {
		// testing only
		// if (true) return resultNodeList;

		for (Iterator nodeit = op.getGraph().getNodes().iterator(); nodeit
				.hasNext();) {
			Node n = (Node) nodeit.next();

			// filter out nodes which are known to be in the result
			if (n.isInResult())
				continue;
			// if an incident edge is in the result, then the node coordinate is
			// included already
			if (n.isIncidentEdgeInResult())
				continue;
			if (n.getEdges().getDegree() == 0
					|| opCode == OverlayOp.INTERSECTION) {

				/**
				 * For nodes on edges, only INTERSECTION can result in edge
				 * nodes being included even if none of their incident edges are
				 * included
				 */
				Label label = n.getLabel();
				if (OverlayOp.isResultOfOp(label, opCode)) {
					filterCoveredNodeToPoint(n);
				}
			}
		}
		// System.out.println("connectedResultNodes collected = " +
		// connectedResultNodes.size());
	}

	/**
	 * Converts non-covered nodes to Point objects and adds them to the result.
	 * 
	 * A node is covered if it is contained in another element Geometry with
	 * higher dimension (e.g. a node point might be contained in a polygon, in
	 * which case the point can be eliminated from the result).
	 * 
	 * @param n
	 *            the node to test
	 */
	private void filterCoveredNodeToPoint(Node n) {
		Coordinate coord = n.getCoordinate();
		if (!op.isCoveredByLA(coord)) {
			PointImpl pt = new PointImpl(new DirectPositionImpl(crs, coord.getCoordinates()));
			//PointImpl pt = primitiveFactory.createPoint(coord.getCoordinates());
			resultPointList.add(pt);
		}
	}
}
