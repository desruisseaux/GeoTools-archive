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

import org.geotools.geometry.iso.FeatGeomFactoryImpl;
import org.geotools.geometry.iso.coordinate.CurveSegmentImpl;
import org.geotools.geometry.iso.coordinate.DirectPositionImpl;
import org.geotools.geometry.iso.coordinate.EnvelopeImpl;
import org.geotools.geometry.iso.coordinate.LineSegmentImpl;
import org.geotools.geometry.iso.coordinate.LineStringImpl;
import org.geotools.geometry.iso.coordinate.PositionImpl;
import org.geotools.geometry.iso.io.GeometryToString;
import org.geotools.geometry.iso.operation.IsSimpleOp;
import org.geotools.geometry.iso.operation.Merger;
import org.geotools.geometry.iso.util.DoubleOperation;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.spatialschema.geometry.Envelope;
import org.opengis.spatialschema.geometry.complex.Complex;
import org.opengis.spatialschema.geometry.complex.CompositeCurve;
import org.opengis.spatialschema.geometry.geometry.LineSegment;
import org.opengis.spatialschema.geometry.geometry.LineString;
import org.opengis.spatialschema.geometry.geometry.ParamForPoint;
import org.opengis.spatialschema.geometry.geometry.Position;
import org.opengis.spatialschema.geometry.primitive.Curve;
import org.opengis.spatialschema.geometry.primitive.CurveBoundary;
import org.opengis.spatialschema.geometry.primitive.CurveSegment;
import org.opengis.spatialschema.geometry.primitive.OrientableCurve;
import org.opengis.spatialschema.geometry.primitive.OrientablePrimitive;
import org.opengis.spatialschema.geometry.primitive.Point;
import org.opengis.spatialschema.geometry.primitive.PrimitiveBoundary;

/**
 * Curve (Figure 11 of the ISO 19107 v5) is a descendent subtype of Primitive
 * through OrientablePrimitive. It is the basis for 1-dimensional geometry. A
 * curve is a continuous image of an open interval and so could be written as a
 * parameterized function such as c(t):(a, b) -> E^n where "t" is a real
 * parameter and E^n is Euclidean space of dimension n (usually 2 or 3, as
 * determined by the coordinate reference system). Any other parameterization
 * that results in the same image curve, traced in the same direction, such as
 * any linear shifts and positive scales such as e(t) = c(a + t(b-a)):(0,1) ->
 * E^n, is an equivalent representation of the same curve. For the sake of
 * simplicity, Curves should be parameterized by arc length, so that the
 * parameterization operation inherited from GenericCurve will be valid for
 * parameters between 0 and the length of the curve.
 * 
 * Curves are continuous, connected, and have a measurable length in terms of
 * the coordinate system. The orientation of the curve is determined by this
 * parameterization, and is consistent with the tangent function, which
 * approximates the derivative function of the parameterization and shall always
 * point in the "forward" direction. The parameterization of the reversal of the
 * curve defined by c(t):(a, b) -> E^n would be defined by a function of the
 * form s(t) = c(a + b - t):(a, b)?E^n.
 * 
 * A curve is composed of one or more curve segments. Each curve segment within
 * a curve may be defined using a different interpolation method. The curve
 * segments are connected to one another, with the end point of each segment
 * except the last being the start point of the next segment in the segment
 * list.
 * 
 * @version <A HREF="http://www.opengis.org/docs/01-101.pdf">Abstract
 *          Specification V5</A>
 * @author Jackson Roehrig & Sanjay Jena
 * 
 */
public class CurveImpl extends OrientableCurveImpl implements Curve {

	/**
	 * The association "segmentation" lists the CurveSegments of Curve, each of
	 * which defines the direct position of points along a portion of the curve.
	 * The order of the CurveSegments is the order in which they are used to
	 * trace the Curve.
	 * 
	 * Curve::segment [1..n] : Sequence<CurveSegment> _______________________
	 * CurveSegment::curve [0,1] : Reference<Curve>
	 * 
	 * For a particular parameter interval, the Curve and CurveSegment agree.
	 * 
	 * CurveSegment:
	 * 
	 * {curve.startParam() <= self.startParam()};
	 * 
	 * {curve.endParam() >= self.endParam()};
	 * 
	 * {self.startParam() < self.endParam()};
	 * 
	 * {s : Distance (startParam() <= s <= endParam()) implies
	 * 
	 * (curve.parameterization(s) = self.parameterization(s))}
	 * 
	 * 
	 * NOTE: In the standard, curve segments do not appear except in the context
	 * of a curve, and therefore the cardinality of the “curve” role in this
	 * association could be “1” which would preclude the use of curve segments
	 * except in this manner. While this would not affect this Standard, leaving
	 * the cardinality as “0..1” allows other standards based on this one to use
	 * curve segments in a more open-ended manner.
	 * 
	 * The field type AbstractSequentialList<CurveSegmentImpl> is implemented
	 * mainly outside of the feature geometry packages. The curve implementation
	 * iterates through segment, which could access its elements on the fly from
	 * any source. The segments may not be necessarily stored in the memory.
	 */
	private List<CurveSegment> curveSegments = null;

	private EnvelopeImpl envelope = null;

	/**
	 * Boundary of the Curve
	 */
	protected CurveBoundaryImpl boundary = null;

	/**
	 * The Curve constructor takes an abstract sequential list of CurveSegments
	 * with the appropriate end-to-start relationships and creates a Curve.
	 * 
	 * Curve::Curve(segment[1..n] : CurveSegment) : Curve
	 * 
	 * The start position of the first segment and the end position of the last
	 * segment must be associated to a PointImpl. If they are associated instead
	 * of that to a direct position, then this direct position will be used to
	 * construct a new Point.
	 * 
	 * @param factory
	 * @param segments
	 * @throws IllegalArgumentException,
	 *             if the array of CurveSegments is empty or does not fulfill
	 *             the requirements of the CurveSegments
	 */
	public CurveImpl(FeatGeomFactoryImpl factory,
			List<? extends CurveSegment> segments)
			throws IllegalArgumentException {
		super(factory);
		this.initialize(segments);
	}

	/**
	 * The Curve constructor takes an abstract sequential list of CurveSegments
	 * with the appropriate end-to-start relationships and creates a Curve.
	 * 
	 * Curve::Curve(segment[1..n] : CurveSegment) : Curve
	 * 
	 * The start position of the first segment and the end position of the last
	 * segment must be associated to a PointImpl. If they are associated instead
	 * of that to a direct position, then this direct position will be used to
	 * construct a new Point.
	 * 
	 * Contained primitives must be curve segments or points. If it is a curve
	 * 
	 * @param factory
	 * @param segments
	 * @param containedPrimitive
	 * @param containingPrimitive
	 * @param complex
	 * @throws IllegalArgumentException
	 */
	// TODO Selbe frage auch hier: brauchen wir diesen constructor?
	// public CurveImpl(FeatGeomFactoryImpl factory, List<CurveSegmentImpl>
	// segments,
	// Set<Primitive> containedPrimitive,
	// Set<Primitive> containingPrimitive, Set<Complex> complex)
	// throws IllegalArgumentException {
	// super(factory, containedPrimitive, containingPrimitive, complex);
	// this.initialize(segments);
	// }

	/**
	 * Initialize Curve attributes - Set segments - Calculate Envelope -
	 * Calculate Parametrisation
	 * 
	 * @param segments
	 */
	private void initialize(List<? extends CurveSegment> segments) {
		if ((segments == null) || segments.isEmpty())
			throw new IllegalArgumentException(
					"The list of CurveSegments ist empty."); //$NON-NLS-1$

		// set the segment
		this.curveSegments = (List<CurveSegment>) segments;

		// set the curve envelope
		Iterator<? extends CurveSegment> it = segments.iterator();
		CurveSegmentImpl cs0 = (CurveSegmentImpl) it.next();
		cs0.setCurve(this);
		this.envelope = new EnvelopeImpl(cs0.getEnvelope());

		PositionImpl p0 = cs0.getStartPosition();

		// Änderung durch Sanjay, da in bisheriger Version nicht der Fall
		// berücksichtigt wurde, dass nur 1 CurveSegment existiert
		CurveSegmentImpl cs1 = null;
		while (it.hasNext()) {
			cs1 = (CurveSegmentImpl) it.next();
			// set the segment / curve association
			cs1.setCurve(this);
			// expand the curve envelope
			this.envelope.expand(cs1.getEnvelope());
			if (!cs0.getEndPoint().equals(cs1.getStartPoint())) {
				throw new IllegalArgumentException(
						"Curvesegments are not continuous. Following curve segments are disjoint:" //$NON-NLS-1$
								+ cs0 + " and " + cs1); //$NON-NLS-1$
			}
			cs0 = cs1;
		}
		PositionImpl p1 = cs0.getEndPosition();

		Point pt0 = p0.getPoint();
		Point pt1 = p1.getPoint();
		if (pt0 == null) {
			pt0 = this.getGeometryFactory().getPrimitiveFactory().createPoint(
					p0);
		}
		if (pt1 == null) {
			pt1 = this.getGeometryFactory().getPrimitiveFactory().createPoint(
					p1);
		}
		// Calculate and Set Boundary
		this.boundary = this.calculateBoundary(pt0, pt1);
		/* Calculate Parametrisation */
		this.calculateParametrisation();

	}
	
	/**
	 * Calculates the Boundary for the curve
	 * 
	 * @param start
	 * @param end
	 * @return CurveBoundary
	 */
	private CurveBoundaryImpl calculateBoundary(Point start, Point end) {
		
		// Return null if start point and end point are equal.
		// This is a design decision made by us, since the Abstract
		// Specification does not give any restrictions in that point.
		if (start.equals(end))
			return null;
		else
		// Return the CurveBoundary defined by the start and end point of this curve
			return this.getGeometryFactory().getPrimitiveFactory().createCurveBoundary(start, end);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geotools.geometry.featgeom.root.GeometryImpl#clone()
	 */
	public CurveImpl clone() throws CloneNotSupportedException {
		// Test OK
		// Create a new Curve by cloning the direct positions which define the control points of this curve 
		List<DirectPositionImpl> dPList = this.asDirectPositions();
		List<DirectPosition> newDPList = new ArrayList<DirectPosition>();
		for (int i=0; i<dPList.size(); i++) {
			newDPList.add(dPList.get(i).clone());
		}
		return (CurveImpl) this.getGeometryFactory().getPrimitiveFactory().createCurveByDirectPositions(newDPList);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geotools.geometry.featgeom.primitive.OrientablePrimitiveImpl#createProxy()
	 */
	protected OrientablePrimitive createProxy() {
		// TODO semantic SJ, JR
		// TODO implementation
		// TODO test
		// TODO documentation
		return new CurveProxy(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geotools.geometry.featgeom.primitive.PrimitiveImpl#getBoundary()
	 */
	public CurveBoundary getBoundary() {
		// Returns the boundary of this Curve, which is of type CurveBoundary
		return this.boundary;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geotools.geometry.featgeom.root.GeometryImpl#getEnvelope()
	 */
	public Envelope getEnvelope() {
		// Return the envelope of this Curve
		return this.envelope;
	}

	/**
	 * @param distance
	 */
	public void split(double distance) {
		for (CurveSegment curveSegment : this.curveSegments) {
			((CurveSegmentImpl) curveSegment).split(distance);
		}
	}

	/**
	 * @param dist
	 * @return CurveSegmentImpl
	 */
	protected CurveSegmentImpl getSegmentAt(double dist) {
		// if ( this.orientation() ==
		// OrientablePrimitive.Orientation.NEGATIVE) dist = this.length -
		// dist;
		if (this.curveSegments == null)
			return null;
		double length = 0.0;
		for (CurveSegment curveSegment : this.curveSegments) {
			length = DoubleOperation.add(length, ((CurveSegmentImpl) curveSegment).length());
			//length += ((CurveSegmentImpl) curveSegment).length();
			if (dist < length)
				return (CurveSegmentImpl) curveSegment;
		}
		return (CurveSegmentImpl) this.curveSegments.get(this.curveSegments
				.size() - 1);
	}

	/**
	 * The operations "startPoint" shall return the DirectPositions of the first
	 * point, respectively on the GenericCurve. This differs from the boundary
	 * operator in Primitive, since it returns only the values of these two
	 * points, not representative objects. GenericCurve::startPoint() :
	 * DirectPosition2D
	 * 
	 * @return an <code>DirectPosition2D</code> value
	 */
	public DirectPosition getStartPoint() {
		// Return first Point of this curve
		return this.curveSegments.get(0).getStartPoint();
	}

	/**
	 * The operations "endPoint" shall return the DirectPositions of the last
	 * point, respectively on the GenericCurve. This differs from the boundary
	 * operator in Primitive, since it returns only the values of these two
	 * points, not representative objects. GenericCurve::startPoint() :
	 * DirectPosition2D
	 * 
	 * @return an <code>DirectPosition2D</code> value
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.spatialschema.geometry.geometry.GenericCurve#getEndPoint()
	 */
	public DirectPosition getEndPoint() {
		/* Return End Point of last CurveSegment */
		return this.curveSegments.get(this.curveSegments.size() - 1)
				.getEndPoint();
	}

	/**
	 * The operation "paramForPoint" shall return the parameter for this
	 * GenericCurve at the passed DirectPosition. If the DirectPosition is not
	 * on the curve, the nearest point on the curve shall be used.
	 * 
	 * GenericCurve::paramForPoint(p : DirectPosition2D) : Set<Distance>,
	 * DirectPosition2D
	 * 
	 * The DirectPosition closest is the actual value for the "p" used, that is,
	 * it shall be the point on the GenericCurve closest to the coordinate
	 * passed in as "p". The return set will contain only one distance, unless
	 * the curve is not simple. If there is more than one DirectPosition on the
	 * GenericCurve at the same minimal distance from the passed "p", the return
	 * value may be an arbitrary choice of one of the possible answers.
	 * 
	 * @param p
	 *            an <code>DirectPosition2D</code> value
	 * @return Array of parameters for the Position
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.spatialschema.geometry.geometry.GenericCurve#getParamForPoint(org.opengis.spatialschema.geometry.DirectPosition)
	 */
	public ParamForPoint getParamForPoint(DirectPosition p) {
		// TODO semantic SJ, JR
		// TODO implementation
		// TODO test
		// TODO documentation
		/* Initialise paramForPoints with set of first Segment */
		ParamForPoint paramForPoints = this.curveSegments.get(0)
				.getParamForPoint(p);

		double minDistanceSquare = ((DirectPositionImpl) p)
				.distanceSquare(paramForPoints.getPosition());
		double actDistanceSquare = 0.0;

		/* Loop all other segments and check if the distance of them is smaller */
		for (int i = 1; i < this.curveSegments.size(); i++) {
			ParamForPoint paramForPoints1 = this.curveSegments.get(i)
					.getParamForPoint(p);
			actDistanceSquare = ((DirectPositionImpl) p)
					.distanceSquare(paramForPoints1.getPosition());
			if (actDistanceSquare <= minDistanceSquare) {
				// TODO
				// /* If other params are closer, clear list of params */
				// if (actDistanceSquare < minDistanceSquare) {
				// ((ParamForPoint)paramForPoints).clear();
				// }
				// /* Add new minimal distances to list of params */
				// for (int j = 0; j < obj.length; j++) {
				// paramForPoints.add((Double) obj[j]);
				// }
				// minDistanceSquare = actDistanceSquare;
			}
		}

		return paramForPoints;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.spatialschema.geometry.geometry.GenericCurve#forParam(double)
	 */
	public DirectPosition forParam(double distance) {
		// Test ok - valid for n dimensional space

		if (distance < this.getStartParam() || distance > this.getEndParam())
			throw new IllegalArgumentException("Distance parameter not in parametrisation range."); //$NON-NLS-1$

		int index = 0;
		while (index < this.curveSegments.size()
				&& distance > this.curveSegments.get(index).getEndParam()) {
			index++;
		}

		/* Return DirectPosition using the same function of the Curve Segment */
		return this.curveSegments.get(index).forParam(distance);
	}

	/**
	 * The operation "constrParam" shall be an alternate representation of the
	 * curve as the continuous image of a real number interval without the
	 * restriction that the parameter represents the arc length of the curve,
	 * nor restrictions between a Curve and its component CurveSegments. The
	 * most common use of this operation is to expose the constructive equations
	 * of the underlying curve, especially useful when that curve is used to
	 * construct a parametric surface.
	 * 
	 * GenericCurve::constrParam(cp : Real) : DirectPosition2D
	 * 
	 * @param cp
	 *            a <code>double</code> value
	 * @return an <code>DirectPosition2D</code> value
	 */
	public DirectPosition constrParam(double cp) {
		return this.forParam(DoubleOperation.mult(cp, this.length()));
	}


	/* (non-Javadoc)
	 * @see org.opengis.spatialschema.geometry.geometry.GenericCurve#length(double, double)
	 */
	public double length(double par1, double par2) {
		if (par1 < 0 || par2 > this.length())
			throw new IllegalArgumentException(
					"Parameter out of parametrisation range."); //$NON-NLS-1$

		/* Return difference of the two parameters */
		return DoubleOperation.subtract(par2, par1);
	}

	/**
	 * This third form of the operation length returns length(0.0,length)
	 * 
	 * @return length
	 */
	public double length() {
		/* Return absolute difference between End and Start Param */
		return Math.abs(DoubleOperation.subtract(this.getEndParam(), this.getStartParam()));
	}

	/**
	 * The function "asLineString" constructs a line string (sequence of line
	 * segments) where the control points (ends of the segments) lie on this
	 * curve. If "maxSpacing" is given (not zero), then the distance between
	 * control points along the generated curve is not more than "maxSpacing".
	 * If "maxOffset" is given (not zero), the distance between generated curve
	 * at any point and the original curve is be more than the "maxOffset". If
	 * both parameters are set, then both criteria are met. If the original
	 * control points of the curve lie on the curve, then they are included in
	 * the returned LineString's controlPoints. If both parameters are set to
	 * zero, then the line string returned is constructed from the control
	 * points of the original curve.
	 * 
	 * GenericCurve::asLineString(spacing : Distance = 0, offset : Distance = 0) :
	 * LineString
	 * 
	 * NOTE This function is useful in creating linear approximations of the
	 * curve for simple actions such as display. It is often referred to as a
	 * "stroked curve". For this purpose, the "maxOffset" version is useful in
	 * maintaining a minimal representation of the curve appropriate for the
	 * display device being targeted. This function is also useful in preparing
	 * to transform a curve from one coordinate reference system to another by
	 * transforming its control points. In this case, the "maxSpacing" version
	 * is more appropriate. Allowing both parameters to default to zero does not
	 * seem to have any useful geographic nor geometric interpretation unless
	 * further information is known about how the curves were constructed.
	 * 
	 * @return an <code>LineString</code> value
	 */
	public List<LineSegment> asLineSegments() {
		/* Schleife ueber alle CurveSegments */
		ArrayList<LineSegment> mergedSegments = new ArrayList<LineSegment>();
		for (CurveSegment curveSegment : this.curveSegments) {
			LineString lineString = curveSegment.asLineString(0, 0);
			List<LineSegment> lineSegments = lineString.asLineSegments();
			for (LineSegment lineSegment : lineSegments)
				mergedSegments.add(lineSegment);
		}
		return mergedSegments;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.spatialschema.geometry.geometry.GenericCurve#asLineString(double,
	 *      double)
	 */
	public LineStringImpl asLineString(double spacing, double offset) {
		// TODO semantic SJ, JR
		// TODO implementation
		// TODO test
		// TODO documentation
		if (this.curveSegments.isEmpty())
			return null;
		CurveSegment seg = this.curveSegments.get(0);
		LineStringImpl ls = (LineStringImpl) seg.asLineString(spacing, offset);
		// TODO Wirft fehler
		// UEber FActory instanzieren!
		LineStringImpl result = new LineStringImpl(ls);
		for (int i = 1; i < this.curveSegments.size(); ++i) {
			seg = this.curveSegments.get(i);
			ls = (LineStringImpl) seg.asLineString(spacing, offset);
			result = result.merge(ls);
		}
		/* Set StartParam for new LineString */
		result.setStartParam(this.getStartParam());
		/* Set EndParam for new LineString */
		result.setEndParam(this.getEndParam());
		return result;
	}

	/**
	 * @return LineStringImpl
	 */
	public LineStringImpl asLineString() {
		return this.asLineString(0.0, 0.0);
	}

	/**
	 * The method <code>dimension</code> returns the inherent dimension of
	 * this Object, which is less than or equal to the coordinate dimension. The
	 * dimension of a collection of geometric objects is the largest dimension
	 * of any of its pieces. Points are 0-dimensional, curves are 1-dimensional,
	 * surfaces are 2-dimensional, and solids are 3-dimensional. Locally, the
	 * dimension of a geometric object at a point is the dimension of a local
	 * neighborhood of the point - that is the dimension of any coordinate
	 * neighborhood of the point. Dimension is unambiguously defined only for
	 * DirectPositions interior to this Object. If the passed DirectPosition2D
	 * is NULL, then the method returns the largest possible dimension for any
	 * DirectPosition2D in this Object.
	 * 
	 * @param point
	 *            a <code>DirectPosition2D</code> value
	 * @return an <code>int</code> value
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geotools.geometry.featgeom.root.GeometryImpl#getDimension(org.opengis.spatialschema.geometry.DirectPosition)
	 */
	public int getDimension(DirectPosition point) {
		// TODO semantic SJ, JR
		// TODO implementation
		// TODO test
		// TODO documentation
		if (point == null)
			return 1;
		return Math.min(point.getDimension(), 1);
	}

	/**
	 * Sets the Parametization values for the Curvesegments Start- and End Param
	 * Constructed Start- and End Param
	 */
	private void calculateParametrisation() {
		if (this.curveSegments == null || this.curveSegments.size() == 0)
			throw new IllegalArgumentException("Segment array not set."); //$NON-NLS-1$

		double tmpLineLength = 0;
		double totalValue = 0;
		for (int i = 0; i < this.curveSegments.size(); i++) {
			
			// Get length of the LineString (before updating the StartParam of the CurveSegment)
			tmpLineLength = ((LineStringImpl) (this.curveSegments.get(i)))
					.length();
			// Set Start Param
			((LineStringImpl) this.curveSegments.get(i))
					.setStartParam(totalValue);
			// Add length of LineString to total length
			totalValue = DoubleOperation.add(totalValue, tmpLineLength);
			//totalValue += tmpLineLength;
			// Set End Param
			((LineStringImpl) this.curveSegments.get(i))
					.setEndParam(totalValue);
		}

//		LineStringImpl tmpLineString = null;
//		for (int i = 0; i < this.curveSegments.size(); i++) {
//			tmpLineString = (LineStringImpl) this.curveSegments.get(i);
//			// JR gelöscht
//		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geotools.geometry.featgeom.root.GeometryImpl#getRepresentativePoint()
	 */
	public DirectPosition getRepresentativePoint() {
		// Use start point of this curve as representative point
		return this.getStartPoint();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.spatialschema.geometry.root.Geometry#isSimple()
	 */
	public boolean isSimple() {
		// Test ok
		// Test simplicity by building a topological graph and testing for self-intersection
		IsSimpleOp simpleOp = new IsSimpleOp();
		return simpleOp.isSimple(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.spatialschema.geometry.primitive.Curve#getSegments()
	 */
	public List<CurveSegment> getSegments() {
		// ok
		// Return the CurveSegments that define this curve
		return this.curveSegments;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.spatialschema.geometry.primitive.OrientableCurve#getComposite()
	 */
	public CompositeCurve getComposite() {
		// TODO semantic SJ, JR
		// TODO implementation
		// TODO test
		// TODO documentation
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.spatialschema.geometry.geometry.GenericCurve#getTangent(double)
	 */
	public double[] getTangent(double distance) {
		// Test OK - valid for n dimensional space
		
		if (distance < this.getStartParam() || distance > this.getEndParam())
			throw new IllegalArgumentException("Distance parameter not in parametrisation range."); //$NON-NLS-1$

		// Loop all Segments (LineStrings) until the the LineString, which
		// contains position at distance, is found
		int i = 0;
		while (this.curveSegments.get(i).getEndParam() < distance
				&& i < curveSegments.size()) {
			i++;
		}
		/* Delegate work to according LineString */
		return this.curveSegments.get(i).getTangent(distance);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.spatialschema.geometry.geometry.GenericCurve#getStartParam()
	 */
	public double getStartParam() {
		// Test ok
		/* The StartParam for a Curve shall always be 0. */
		return 0.0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.spatialschema.geometry.geometry.GenericCurve#getEndParam()
	 */
	public double getEndParam() {
		// Test ok
		/* Return EndParam of last Curve Segment */
		return this.curveSegments.get(this.curveSegments.size() - 1)
				.getEndParam();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.spatialschema.geometry.geometry.GenericCurve#getStartConstructiveParam()
	 */
	public double getStartConstructiveParam() {
		// Test ok
		/* The StartConstrParam for a Curve shall always be 0. */
		return 0.0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.spatialschema.geometry.geometry.GenericCurve#getEndConstructiveParam()
	 */
	public double getEndConstructiveParam() {
		// Test ok
		/* The EndConstrParam for a Curve shall always be 1. */
		return 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.spatialschema.geometry.geometry.GenericCurve#forConstructiveParam(double)
	 */
	public DirectPosition forConstructiveParam(double cp) {
		// Test ok - valid for n dimensional space

		// Return the Position at param (cp * lengthOfLineString)
		double par = DoubleOperation.mult(cp, this.length());
		return this.forParam(par);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.spatialschema.geometry.geometry.GenericCurve#length(org.opengis.spatialschema.geometry.geometry.Position,
	 *      org.opengis.spatialschema.geometry.geometry.Position)
	 */
	public double length(Position point1, Position point2) {
		// TODO semantic SJ, JR
		// TODO implementation
		// TODO test
		// TODO documentation

		/* Default-Values */
		if (point1 == null && point2 == null)
			return this.length();
		// if (point1 == null)
		// point1 = new PositionImpl(this.getStartPoint());
		// if (point2 == null)
		// point2 = new PositionImpl(this.getEndPoint());

		/* Get all Params for closest points to startposition point1 */
		ParamForPoint startParams = this.getParamForPoint(point1.getPosition());
		/* Get all Params for closest points to endposition point2 */
		ParamForPoint endParams = this.getParamForPoint(point2.getPosition());

		/*
		 * Compare the distances between each found startParam and endParam and
		 * choose the smallest one
		 */
		double minDistance = Math.abs(DoubleOperation.subtract(startParams.getDistance(), endParams.getDistance()));

		// double actDistance = 0.0;
		// for (int i = 1; i < startParams.length; i++) {
		// for (int j = 1; i < endParams.length; j++) {
		// actDistance = Math.abs((Double) this.getStartParams[0]
		// - (Double) endParams[0]);
		// if (actDistance < minDistance) {
		// minDistance = actDistance;


		return minDistance;
	}


	/**
	 * Returns the DirectPositions which define the control points of this Curve
	 * 
	 * @return
	 */
	public List<DirectPositionImpl> asDirectPositions() {

		List<DirectPositionImpl> rList = new ArrayList<DirectPositionImpl>();

		CurveSegment tSegment = null;

		// Iterate all CurveSegments (= LineStrings)
		for (int i = 0; i < this.curveSegments.size(); i++) {
			tSegment = this.curveSegments.get(i);

			// TODO: This version only handles the CurveSegment type LineString
			LineStringImpl tLineString = (LineStringImpl) tSegment;

			Iterator<LineSegment> tLineSegmentIter = tLineString
					.asLineSegments().iterator();
			while (tLineSegmentIter.hasNext()) {
				LineSegment tLineSegment = tLineSegmentIter.next();
				// Add new Coordinate, which is the start point of the actual
				// LineSegment
				rList.add((DirectPositionImpl) tLineSegment.getStartPoint());
			}

		}
		// Add new Coordinate, which is the end point of the last curveSegment
		rList.add((DirectPositionImpl) tSegment.getEndPoint());

		return rList;
	}


	
	/**
	 * Constructs a new Curve by merging this Curve with another Curve
	 * The two input curves will not be modified.
	 * There will be no more references to positions or lists of the input curves, all values are copied.
	 * 
	 * @param other
	 * @return
	 */
	public CurveImpl merge(CurveImpl other) {
		// Test ok
		Merger merger = new Merger(this.getGeometryFactory());
		return merger.merge(this, other);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return GeometryToString.getString(this);
	}


}
