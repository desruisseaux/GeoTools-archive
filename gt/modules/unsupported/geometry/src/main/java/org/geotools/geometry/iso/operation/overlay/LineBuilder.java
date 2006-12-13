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
import java.util.LinkedList;
import java.util.List;

import org.geotools.geometry.iso.FeatGeomFactoryImpl;
import org.geotools.geometry.iso.coordinate.CoordinateFactoryImpl;
import org.geotools.geometry.iso.primitive.CurveImpl;
import org.geotools.geometry.iso.primitive.PrimitiveFactoryImpl;
import org.geotools.geometry.iso.topograph2D.DirectedEdge;
import org.geotools.geometry.iso.topograph2D.DirectedEdgeStar;
import org.geotools.geometry.iso.topograph2D.Edge;
import org.geotools.geometry.iso.topograph2D.Label;
import org.geotools.geometry.iso.topograph2D.Node;
import org.geotools.geometry.iso.topograph2D.util.CoordinateArrays;
import org.geotools.geometry.iso.util.Assert;
import org.geotools.geometry.iso.util.algorithm2D.PointLocator;
import org.opengis.spatialschema.geometry.geometry.LineString;
import org.opengis.spatialschema.geometry.primitive.CurveSegment;
import org.opengis.spatialschema.geometry.primitive.OrientableCurve;


/**
 * Forms JTS LineStrings out of a the graph of {@link DirectedEdge}s created by
 * an {@link OverlayOp}.
 */
public class LineBuilder {
	private OverlayOp op;

	private FeatGeomFactoryImpl featGeomFactory;

	private CoordinateFactoryImpl coordinateFactory;

	private PrimitiveFactoryImpl primitiveFactory;

	private PointLocator ptLocator;

	private List lineEdgesList = new ArrayList();

	private List<OrientableCurve> resultLineList = new ArrayList<OrientableCurve>();

	public LineBuilder(OverlayOp op, FeatGeomFactoryImpl aFeatGeomFactory,
			PointLocator ptLocator) {
		this.op = op;
		this.featGeomFactory = aFeatGeomFactory;
		this.coordinateFactory = featGeomFactory.getCoordinateFactory();
		this.primitiveFactory = featGeomFactory.getPrimitiveFactory();
		this.ptLocator = ptLocator;
	}

	/**
	 * @return a list of the LineStrings in the result of the specified overlay
	 *         operation
	 */
	public List<OrientableCurve> build(int opCode) {
		this.findCoveredLineEdges();
		this.collectLines(opCode);
		// labelIsolatedLines(lineEdgesList);
		this.buildLines(opCode);
		return resultLineList;
	}

	/**
	 * Find and mark L edges which are "covered" by the result area (if any). L
	 * edges at nodes which also have A edges can be checked by checking their
	 * depth at that node. L edges at nodes which do not have A edges can be
	 * checked by doing a point-in-polygon test with the previously computed
	 * result areas.
	 */
	private void findCoveredLineEdges() {
		// first set covered for all L edges at nodes which have A edges too
		for (Iterator nodeit = op.getGraph().getNodes().iterator(); nodeit
				.hasNext();) {
			Node node = (Node) nodeit.next();
			// node.print(System.out);
			((DirectedEdgeStar) node.getEdges()).findCoveredLineEdges();
		}

		/**
		 * For all L edges which weren't handled by the above, use a
		 * point-in-poly test to determine whether they are covered
		 */
		for (Iterator it = op.getGraph().getEdgeEnds().iterator(); it.hasNext();) {
			DirectedEdge de = (DirectedEdge) it.next();
			Edge e = de.getEdge();
			if (de.isLineEdge() && !e.isCoveredSet()) {
				boolean isCovered = op.isCoveredByA(de.getCoordinate());
				e.setCovered(isCovered);
			}
		}
	}

	private void collectLines(int opCode) {
		for (Iterator it = op.getGraph().getEdgeEnds().iterator(); it.hasNext();) {
			DirectedEdge de = (DirectedEdge) it.next();
			collectLineEdge(de, opCode, lineEdgesList);
			collectBoundaryTouchEdge(de, opCode, lineEdgesList);
		}
	}

	/**
	 * Collect line edges which are in the result. Line edges are in the result
	 * if they are not part of an area boundary, if they are in the result of
	 * the overlay operation, and if they are not covered by a result area.
	 * 
	 * @param de
	 *            the directed edge to test
	 * @param opCode
	 *            the overlap operation
	 * @param edges
	 *            the list of included line edges
	 */
	private void collectLineEdge(DirectedEdge de, int opCode, List edges) {
		Label label = de.getLabel();
		Edge e = de.getEdge();
		// include L edges which are in the result
		if (de.isLineEdge()) {
			if (!de.isVisited() && OverlayOp.isResultOfOp(label, opCode)
					&& !e.isCovered()) {
				// Debug.println("de: " + de.getLabel());
				// Debug.println("edge: " + e.getLabel());

				edges.add(e);
				de.setVisitedEdge(true);
			}
		}
	}

	/**
	 * Collect edges from Area inputs which should be in the result but which
	 * have not been included in a result area. This happens ONLY:
	 * <ul>
	 * <li>during an intersection when the boundaries of two areas touch in a
	 * line segment
	 * <li> OR as a result of a dimensional collapse.
	 * </ul>
	 */
	private void collectBoundaryTouchEdge(DirectedEdge de, int opCode,
			List edges) {
		Label label = de.getLabel();
		if (de.isLineEdge())
			return; // only interested in area edges
		if (de.isVisited())
			return; // already processed
		if (de.isInteriorAreaEdge())
			return; // added to handle dimensional collapses
		if (de.getEdge().isInResult())
			return; // if the edge linework is already included, don't include
		// it again

		// sanity check for labelling of result edgerings
		Assert.isTrue(!(de.isInResult() || de.getSym().isInResult())
				|| !de.getEdge().isInResult());

		// include the linework if it's in the result of the operation
		if (OverlayOp.isResultOfOp(label, opCode)
				&& opCode == OverlayOp.INTERSECTION) {
			edges.add(de.getEdge());
			de.setVisitedEdge(true);
		}
	}

	private void buildLines(int opCode) {

		for (Iterator it = this.lineEdgesList.iterator(); it.hasNext();) {

			Edge e = (Edge) it.next();
			Label label = e.getLabel();

			LineString line = coordinateFactory.createLineString(
					CoordinateArrays.toPositionList(coordinateFactory, e
							.getCoordinates()), 0.0);
			LinkedList<CurveSegment> tLineStrings = new LinkedList<CurveSegment>();
			tLineStrings.add((CurveSegment) line);
			CurveImpl curve = primitiveFactory
					.createCurve((List<CurveSegment>) tLineStrings);
			resultLineList.add(curve);
			e.setInResult(true);

		}
	}

	private void labelIsolatedLines(List edgesList) {
		for (Iterator it = edgesList.iterator(); it.hasNext();) {
			Edge e = (Edge) it.next();
			Label label = e.getLabel();
			// n.print(System.out);
			if (e.isIsolated()) {
				if (label.isNull(0))
					labelIsolatedLine(e, 0);
				else
					labelIsolatedLine(e, 1);
			}
		}
	}

	/**
	 * Label an isolated node with its relationship to the target geometry.
	 */
	private void labelIsolatedLine(Edge e, int targetIndex) {
		int loc = ptLocator.locate(e.getCoordinate(), op
				.getArgGeometry(targetIndex));
		e.getLabel().setLocation(targetIndex, loc);
	}

}
