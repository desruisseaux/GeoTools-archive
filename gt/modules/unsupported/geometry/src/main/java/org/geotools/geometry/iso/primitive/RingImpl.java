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


package org.geotools.geometry.iso.primitive;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.geotools.geometry.iso.FeatGeomFactoryImpl;
import org.geotools.geometry.iso.complex.CompositeCurveImpl;
import org.geotools.geometry.iso.coordinate.DirectPositionImpl;
import org.geotools.geometry.iso.coordinate.LineSegmentImpl;
import org.geotools.geometry.iso.coordinate.LineStringImpl;
import org.geotools.geometry.iso.io.GeometryToString;
import org.geotools.geometry.iso.operation.IsSimpleOp;
import org.opengis.spatialschema.geometry.complex.Complex;
import org.opengis.spatialschema.geometry.primitive.Curve;
import org.opengis.spatialschema.geometry.primitive.CurveBoundary;
import org.opengis.spatialschema.geometry.primitive.CurveSegment;
import org.opengis.spatialschema.geometry.primitive.OrientableCurve;
import org.opengis.spatialschema.geometry.primitive.Primitive;
import org.opengis.spatialschema.geometry.primitive.Ring;

/**
 * 
 * A Ring is used to represent a single connected component of a
 * SurfaceBoundary. It consists of a number of references to OrientableCurves
 * connected in a cycle (an object whose boundary is empty). A Ring is
 * structurally similar to a CompositeCurve in that the endPoint of each
 * OrientableCurve in the sequence is the startPoint of the next OrientableCurve
 * in the Sequence. Since the sequence is circular, there is no exception to
 * this rule. Each ring, like all boundaries is a cycle and each ring is simple.
 * 
 * Ring: {isSimple() = TRUE}
 * 
 * Even though each Ring is simple, the boundary need not be simple. The easiest
 * case of this is where one of the interior rings of a surface is tangent to
 * its exterior ring. Implementations may enforce stronger restrictions on the
 * interaction of boundary elements.
 * 
 * @author Jackson Roehrig & Sanjay Jena
 */
public class RingImpl extends CompositeCurveImpl implements Ring {

	private SurfaceBoundaryImpl surfaceBoundary;

	/**
	 * Creates a Ring
	 * 
	 * @param factory
	 * @param generator
	 */
	public RingImpl(FeatGeomFactoryImpl factory, List<OrientableCurve> generator) {
		super(factory, generator);
		this.checkConsistency(generator);
	}

	/**
	 * Check consisty of the given curve list:
	 * - Continuity
	 * - Simplicity
	 * - Closeness
	 */
	private void checkConsistency(List<OrientableCurve> aGenerator) {
		CurveImpl oc0 = (CurveImpl) aGenerator.get(0).getPrimitive();
		CurveImpl oc1 = (CurveImpl) aGenerator.get(aGenerator.size() - 1)
				.getPrimitive();

		// Check Closeness
		if (!oc0.getStartPoint().equals(oc1.getEndPoint()))
			throw new IllegalArgumentException("Start point of first element has to be at the same position as end point of last element"); //$NON-NLS-1$

		// Check Continuity and merge all curves into a new curve
		CurveImpl newCurve = oc0;
		for (int i=1; i<aGenerator.size(); i++) {
			CurveImpl nextCurve = (CurveImpl) aGenerator.get(i);
			if (! newCurve.getEndPoint().equals(nextCurve.getStartPoint()))
				throw new IllegalArgumentException("The curve segments are not continuous"); //$NON-NLS-1$
			newCurve.merge((CurveImpl) aGenerator.get(i));

		}

		// Check Simplicity
		IsSimpleOp isSimple = new IsSimpleOp();
		if (! isSimple.isSimple(newCurve))
			throw new IllegalArgumentException("The curve segments are not simple, but intersect"); //$NON-NLS-1$

	}

	/* (non-Javadoc)
	 * @see org.geotools.geometry.featgeom.complex.CompositeCurveImpl#clone()
	 */
	public RingImpl clone() throws CloneNotSupportedException {
		// Test OK
		Iterator<Primitive> elementIter = this.getElements().iterator();
		List<OrientableCurve> newElements = new ArrayList<OrientableCurve>();
		while (elementIter.hasNext()) {
			newElements.add((Curve) elementIter.next().clone());
		}
		return this.getGeometryFactory().getPrimitiveFactory().createRing(newElements);
	}

	
	/* (non-Javadoc)
	 * @see org.geotools.geometry.featgeom.complex.CompositeCurveImpl#getBoundary()
	 */
	public CurveBoundary getBoundary() {
		// A Ring does not have a Boundary since it´s start and end points are equal.
		return null;
	}

	/* (non-Javadoc)
	 * @see org.geotools.geometry.featgeom.complex.CompositeCurveImpl#createBoundary()
	 */
	public Set<Complex> createBoundary() {
		// overwrites the boundary definition of CompositeCurve
		// returns null, cause a Ring does not have a boundary
		return null;
	}

	/**
	 * @return Returns the surfaceBoundary.
	 */
	public SurfaceBoundaryImpl getSurfaceBoundary() {
		return surfaceBoundary;
	}

	/**
	 * @param surfaceBoundary
	 *            The surfaceBoundary to set.
	 */
	public void setSurfaceBoundary(SurfaceBoundaryImpl surfaceBoundary) {
		this.surfaceBoundary = surfaceBoundary;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geotools.geometry.featgeom.complex.CompositeCurveImpl#isSimple()
	 */
	public boolean isSimple() {
		// Implementation ok
		// A Ring is always simple
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geotools.geometry.featgeom.complex.CompositeCurveImpl#isCycle()
	 */
	public boolean isCycle() {
		// Implementation ok
		// A Ring is always a cycle
		return true;
	}

	/**
	 * @return
	 */
	public List<DirectPositionImpl> asDirectPositions() {

		List<DirectPositionImpl> rList = new ArrayList<DirectPositionImpl>();

		// Iterate all Curves
		for (int i = 0; i < this.elements.size(); i++) {

			CurveImpl tCurve = (CurveImpl) this.elements.get(i);
			Iterator<CurveSegment> tCurveSegmentIter = tCurve.getSegments()
					.iterator();
			CurveSegment tSegment = null;

			// Iterate all CurveSegments (= LineStrings)
			while (tCurveSegmentIter.hasNext()) {
				tSegment = tCurveSegmentIter.next();

				// TODO: This version only handles the CurveSegment type
				// LineString
				LineStringImpl tLineString = (LineStringImpl) tSegment;

				Iterator<LineSegmentImpl> tLineSegmentIter = tLineString
						.asLineSegments().iterator();
				while (tLineSegmentIter.hasNext()) {
					LineSegmentImpl tLineSegment = tLineSegmentIter.next();
					// Add new Coordinate, which is the start point of the
					// actual LineSegment
					rList.add(tLineSegment.getStartPoint());
				}

			}
			// Add new Coordinate, which is the end point of the last
			// curveSegment
			rList.add((DirectPositionImpl) tSegment.getEndPoint());
		}

		return rList;
	}

	/* (non-Javadoc)
	 * @see org.geotools.geometry.featgeom.complex.CompositeCurveImpl#toString()
	 */
	public String toString() {
		return GeometryToString.getString(this);
	}


}
