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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.geotools.geometry.iso.coordinate.DirectPositionImpl;
import org.geotools.geometry.iso.coordinate.LineSegmentImpl;
import org.geotools.geometry.iso.coordinate.LineStringImpl;
import org.geotools.geometry.iso.coordinate.PointArrayImpl;
import org.geotools.geometry.iso.coordinate.PositionImpl;
import org.geotools.geometry.iso.coordinate.SurfacePatchImpl;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.Envelope;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.geometry.MismatchedReferenceSystemException;
import org.opengis.geometry.PositionFactory;
import org.opengis.geometry.coordinate.LineSegment;
import org.opengis.geometry.coordinate.Position;
import org.opengis.geometry.primitive.Curve;
import org.opengis.geometry.primitive.CurveSegment;
import org.opengis.geometry.primitive.OrientableCurve;
import org.opengis.geometry.primitive.Point;
import org.opengis.geometry.primitive.PrimitiveFactory;
import org.opengis.geometry.primitive.Ring;
import org.opengis.geometry.primitive.SolidBoundary;
import org.opengis.geometry.primitive.SurfaceBoundary;
import org.opengis.geometry.primitive.SurfacePatch;

/**
 * @author Jackson Roehrig & Sanjay Jena
 * 
 */
public class PrimitiveFactoryImpl implements PrimitiveFactory {

	// Factories should no longer need to know about each other or about any
	// geometryFactory
	// private FeatGeomFactoryImpl xgeometryFactory;
	private CoordinateReferenceSystem crs;
	private PositionFactory positionFactory;

	/**
	 * @param crs
	 */
	public PrimitiveFactoryImpl(CoordinateReferenceSystem crs, PositionFactory positionFactory) {
		this.crs = crs;
		this.positionFactory = positionFactory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.geometry.primitive.PrimitiveFactory#getCoordinateReferenceSystem()
	 */
	public CoordinateReferenceSystem getCoordinateReferenceSystem() {
		// TODO test
		// TODO documentation
		return this.crs;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.geometry.primitive.PrimitiveFactory#getCoordinateReferenceSystem()
	 */
	public PositionFactory getPositionFactory() {
		// TODO test
		// TODO documentation
		return this.positionFactory;
	}

	/**
	 * Returns the Coordinate Dimension of the used Coordinate System (Sanjay)
	 * 
	 * @return dimension Coordinate Dimension used in this Factory
	 */
	public int getDimension() {
		// Test OK
		return this.crs.getCoordinateSystem().getDimension();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.geometry.primitive.PrimitiveFactory#createPoint(double[])
	 */
	public PointImpl createPoint(double[] coord) {
		// Test ok
		if (coord == null)
			throw new NullPointerException();
		if (coord.length != this.getDimension())
			throw new MismatchedDimensionException();

		return new PointImpl(positionFactory.createDirectPosition(coord));
	}

	/**
	 * Creates a Point by copying the coordinates of a given DirectPosition
	 * 
	 * @param dp
	 *            DirectPosition, will be copied
	 * @return PointImpl
	 */
	public PointImpl createPoint(DirectPositionImpl dp) {
		if (dp == null)
			throw new NullPointerException();
		// Test ok
		// Compare Dimension (which is the Coordinate Dimension) of the
		// DirectPosition with the CoordinateDimension of the current Coordinate
		// System in Euclidian Space
		if (dp.getDimension() != this.getDimension())
			throw new MismatchedDimensionException();

		return new PointImpl(dp.clone());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.geometry.primitive.PrimitiveFactory#createPoint(org.opengis.geometry.coordinate.Position)
	 */
	public PointImpl createPoint(Position position)
			throws MismatchedReferenceSystemException,
			MismatchedDimensionException {
		// Test ok
		if (position == null) {
			throw new IllegalArgumentException("Parameter position is null.");
		}
		if (position.getPosition().getDimension() != this.getDimension()) {
			throw new MismatchedDimensionException();
		}
		DirectPositionImpl copy = (DirectPositionImpl) positionFactory.createDirectPosition(position.getPosition().getCoordinates());
		return new PointImpl(copy);
	}

	/**
	 * Creates a CurveBoundary
	 * 
	 * @param dp0
	 * @param dp1
	 * @return CurveBoundaryImpl
	 */
	public CurveBoundaryImpl createCurveBoundary(DirectPosition dp0,
			DirectPosition dp1) {
		// Test OK (Sanjay)
		if (dp0 == null || dp1 == null)
			throw new NullPointerException(
					"One or both of the parameters is NULL");
		return new CurveBoundaryImpl(getCoordinateReferenceSystem(),
				createPoint((DirectPositionImpl) dp0),
				createPoint((DirectPositionImpl) dp1));
	}

	/**
	 * Creates a CurveBoundary
	 * 
	 * @param p0
	 * @param p1
	 * @return CurveBoundaryImpl
	 */
	public CurveBoundaryImpl createCurveBoundary(Point p0, Point p1) {
		if (p0 == null || p1 == null)
			throw new NullPointerException(
					"One or both of the parameters is NULL");
		return new CurveBoundaryImpl(getCoordinateReferenceSystem(), p0, p1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.geometry.primitive.PrimitiveFactory#createCurve(java.util.List)
	 */
	public CurveImpl createCurve(List<CurveSegment> segments) {
		// test OK
		if (segments == null)
			throw new NullPointerException();

		// A curve will be created
		// - The curve will be set as parent curves for the Curve segments
		// - Start and end params for the CurveSegments will be set
		return new CurveImpl(getCoordinateReferenceSystem(), segments);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.geometry.primitive.PrimitiveFactory#createRing(java.util.List)
	 */
	public RingImpl createRing(List<OrientableCurve> orientableCurves)
			throws MismatchedReferenceSystemException,
			MismatchedDimensionException {
		/**
		 * Creates a Ring from triple Array of DirectPositions (Array of arrays,
		 * which each represent a future Curve. Each array contain an array of
		 * positions, which each represent a future lineString)
		 */
		// TODO semantic JR
		// test OK
		for (OrientableCurve orientableCurve : orientableCurves) {
			// Comment by Sanjay
			// TODO JR: Zur Kenntnisnahme und Berücksichtigung in Sourcen: Für
			// alle
			// Primitives gilt, dass getDimension die Dimension des Objektes,
			// und getCoordinateDimension die Dimension des Koordinatensystems,
			// in welchem das Objekt instanziert wurde, wiedergibt
			// if (this.getDimension() != orientableCurve.getDimension(null)) {
			if (this.getDimension() != orientableCurve.getCoordinateDimension()) {
				throw new MismatchedDimensionException();
			}
			if (this.getCoordinateReferenceSystem() != orientableCurve
					.getCoordinateReferenceSystem()) {
				throw new MismatchedReferenceSystemException();
			}
		}
		return new RingImpl(orientableCurves);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.geometry.primitive.PrimitiveFactory#createSurfaceBoundary(org.opengis.geometry.primitive.Ring,
	 *      java.util.List)
	 */
	public SurfaceBoundaryImpl createSurfaceBoundary(Ring exterior,
			List<Ring> interiors) throws MismatchedReferenceSystemException,
			MismatchedDimensionException {
		// Test ok

		if (interiors == null && exterior == null)
			throw new NullPointerException();
		if (exterior != null) {
			if (this.getDimension() != exterior.getCoordinateDimension()) {
				throw new MismatchedDimensionException();
			}
			if (this.getCoordinateReferenceSystem() != exterior
					.getCoordinateReferenceSystem()) {
				throw new MismatchedReferenceSystemException();
			}
		}
		if (interiors != null) {
			for (Ring ring : interiors) {
				if (ring != null) {
					if (this.getDimension() != ring.getCoordinateDimension()) {
						throw new MismatchedDimensionException();
					}
					if (this.getCoordinateReferenceSystem() != ring
							.getCoordinateReferenceSystem()) {
						throw new MismatchedReferenceSystemException();
					}
				}
			}
		}
		return new SurfaceBoundaryImpl(getCoordinateReferenceSystem(),
				exterior, interiors);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.geometry.primitive.PrimitiveFactory#createSurface(java.util.List)
	 */
	public SurfaceImpl createSurface(List<SurfacePatch> surfacePatches)
			throws MismatchedReferenceSystemException,
			MismatchedDimensionException {

		// tested in /test/TestSurface.java
		// TODO SurfaceBoundary NOT calculated !!!

		// Create Surface
		SurfaceImpl rSurface = new SurfaceImpl(getCoordinateReferenceSystem(),
				surfacePatches);
		// Set reference to the generated Surface for each SurfacePatch
		for (int i = 0; i < surfacePatches.size(); i++) {
			SurfacePatchImpl actPatch = (SurfacePatchImpl) surfacePatches
					.get(i);
			actPatch.setSurface(rSurface);
		}

		return rSurface;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.geometry.primitive.PrimitiveFactory#createSurface(org.opengis.geometry.primitive.SurfaceBoundary)
	 */
	public SurfaceImpl createSurface(SurfaceBoundary boundary)
			throws MismatchedReferenceSystemException,
			MismatchedDimensionException {
		// Test ok
		// Creates a Surface without SurfacePatches
		return new SurfaceImpl(boundary);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.geometry.primitive.PrimitiveFactory#createSolid(org.opengis.geometry.primitive.SolidBoundary)
	 */
	public SolidImpl createSolid(SolidBoundary boundary)
			throws MismatchedReferenceSystemException,
			MismatchedDimensionException {
		// TODO semantic SJ, JR
		// TODO implementation
		// TODO test
		// TODO documentation
		return new SolidImpl(boundary);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.geometry.primitive.PrimitiveFactory#createPrimitive(org.opengis.geometry.coordinate.Envelope)
	 */
//	public PrimitiveImpl createPrimitive_try2(Envelope envelope)
//			throws MismatchedReferenceSystemException,
//			MismatchedDimensionException {
//
//		List<DirectPosition> positions = new ArrayList<DirectPosition>();
//
//		for (int d = 0; d < crs.getCoordinateSystem().getDimension(); d++) {
//			CoordinateSystemAxis axis = crs.getCoordinateSystem().getAxis(d);
//			AxisDirection direction = axis.getDirection();
//			double min, max;
//			if (direction == AxisDirection.OTHER) {
//				// we are going to "skip" min/max calculation on OTHER
//				// used for 2.5 D stuff
//				min = Double.NaN;
//				max = Double.NaN;
//			} else {
//				// figure out min & max from envelope
//				min = envelope.getMinimum(d);
//				max = envelope.getMaximum(d);
//			}
//			if (positions.isEmpty()) {
//				DirectPositionImpl min1 = (DirectPositionImpl) positionFactory.createDirectPosition(null);//new DirectPositionImpl(crs);
//				min1.setOrdinate(d, min);
//				DirectPositionImpl max1 = (DirectPositionImpl) positionFactory.createDirectPosition(null);//new DirectPositionImpl(crs);
//				max1.setOrdinate(d, max);
//
//				positions.add(min1);
//				positions.add(max1);
//			} else {
//				// update min in place
//				for (DirectPosition minN : positions) {
//					minN.setOrdinate(d, min);
//				}
//				// copy and update max
//				List<DirectPosition> copy = new ArrayList<DirectPosition>();
//				for (DirectPosition position : positions) {
//					DirectPositionImpl maxN = (DirectPositionImpl) positionFactory.createDirectPosition(position.getCoordinates()); //new DirectPositionImpl(position);
//					maxN.setOrdinate(d, max);
//				}
//				positions.addAll(copy);
//			}
//		}
//
//		return this.createSurfaceByDirectPositions(positions);
//	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.geometry.primitive.PrimitiveFactory#createPrimitive(org.opengis.geometry.coordinate.Envelope)
	 */
	public PrimitiveImpl createPrimitive(Envelope bounds)
			throws MismatchedReferenceSystemException,
			MismatchedDimensionException {
		//final int D = crs.getCoordinateSystem().getDimension();
		
		LineSegment segment = processBoundsToSegment(bounds);		
		return processSegmentToPrimitive( bounds, segment, 1 );		
	}
	
	private PrimitiveImpl processSegmentToPrimitive(Envelope bounds, LineSegment segment, int dimension) {
		//int D = crs.getCoordinateSystem().getDimension();
		CoordinateSystemAxis axis = crs.getCoordinateSystem().getAxis( dimension );
		
		if( axis.getDirection() == AxisDirection.OTHER ){
			return processSegmentToPrimitive( bounds, segment, dimension+1 );
		}
		RingImpl ring = processBoundsToRing( bounds, segment, dimension );
		return processRingToPrimitive( bounds, ring, dimension+1 );				
	}

	private PrimitiveImpl processRingToPrimitive(Envelope bounds, RingImpl ring, int dimension) {
		int D = crs.getCoordinateSystem().getDimension();
		if( dimension == D ){ // create Surface from ring and return			
			SurfaceBoundary boundary = new SurfaceBoundaryImpl( crs, ring, Collections.EMPTY_LIST );
			return new SurfaceImpl( boundary );
		}		
		CoordinateSystemAxis axis = crs.getCoordinateSystem().getAxis( dimension );
		if( axis.getDirection() == AxisDirection.OTHER ){
			return processRingToPrimitive( bounds, ring, dimension+1 );
		}
		return processRingToVolumne( bounds, ring, dimension+1 );
	}

	private PrimitiveImpl processRingToVolumne(Envelope bounds, RingImpl ring, int i) {
		// go into a volume
		throw new UnsupportedOperationException("Not yet 3D");
	}

	/**
	 * This is the first and easy step ...
	 * @param bounds
	 * @param dimension
	 */
	public LineSegment processBoundsToSegment( Envelope bounds ){
		final int D=0;
		CoordinateReferenceSystem crs = bounds.getCoordinateReferenceSystem();
		CoordinateSystemAxis axis = crs.getCoordinateSystem().getAxis( D );
		
		DirectPosition positionA = positionFactory.createDirectPosition(null); //new DirectPositionImpl(crs);
		DirectPosition positionB = positionFactory.createDirectPosition(null); //new DirectPositionImpl(crs);		
		if( axis.getDirection() != AxisDirection.OTHER ){
			positionA.setOrdinate(D, bounds.getMinimum(D) );
			positionB.setOrdinate(D, bounds.getMaximum(D) );
		}		
		PointArrayImpl array = new PointArrayImpl(crs );
		array.add( positionA );
		array.add( positionB );
		
		return new LineSegmentImpl( array, 0.0 );
	}
	/**
	 * This is pass #2 ...
	 * @return
	 */
	public RingImpl processBoundsToRing( Envelope bounds, LineSegment segment, final int D ){
		DirectPosition one = positionFactory.createDirectPosition(segment.getStartPoint().getCoordinates()); //new DirectPositionImpl( segment.getStartPoint() );
		one.setOrdinate( D, bounds.getMinimum(D) );
		
		DirectPosition two = positionFactory.createDirectPosition(segment.getEndPoint().getCoordinates()); //new DirectPositionImpl( segment.getEndPoint() );
		two.setOrdinate( D, bounds.getMinimum(D) );
		
		DirectPosition three = positionFactory.createDirectPosition(two.getCoordinates()); //new DirectPositionImpl( two );
		three.setOrdinate( D, bounds.getMaximum(D) );
		
		DirectPosition four = positionFactory.createDirectPosition(one.getCoordinates()); //new DirectPositionImpl( one );
		four.setOrdinate( D, bounds.getMaximum(D) );
		
		LineSegment edge1 = new LineSegmentImpl( one, two, 0.0 );
		LineSegment edge2 = new LineSegmentImpl( two, three, 0.0 );
		LineSegment edge3 = new LineSegmentImpl( three, four, 0.0 );
		LineSegment edge4 = new LineSegmentImpl( four, one, 0.0 );
		
		List<OrientableCurve> edges = new ArrayList<OrientableCurve>();
		edges.add( new CurveImpl( edge1 ));
		edges.add( new CurveImpl( edge2 ));
		edges.add( new CurveImpl( edge3 ));
		edges.add( new CurveImpl( edge4 ));
		return new RingImpl( edges );
	}
		
	/**
	 * Creates a Ring conforming to the given DirectPositions. Helps to build
	 * Rings for SurfaceBoundaries.
	 * 
	 * @param directPositions
	 * @return a Ring
	 */
	public Ring createRingByDirectPositions(List<DirectPosition> directPositions) {
		// Test ok

		// Create List of OrientableCurve´s (Curve´s)
		OrientableCurve curve = this
				.createCurveByDirectPositions(directPositions);
		List<OrientableCurve> orientableCurves = new ArrayList<OrientableCurve>();
		orientableCurves.add(curve);

		return this.createRing(orientableCurves);
	}

	/**
	 * Creates a Ring conforming to the given Positions
	 * 
	 * @param aPositions
	 * @return
	 */
	public Ring createRingByPositions(List<Position> aPositions) {

		// Create List of OrientableCurve´s (Curve´s)
		OrientableCurve curve = this.createCurveByPositions(aPositions);
		List<OrientableCurve> orientableCurves = new ArrayList<OrientableCurve>();
		orientableCurves.add(curve);

		return this.createRing(orientableCurves);

	}

	/**
	 * Creates a Curve conforming to the given DirectPositions Tested by Sanjay -
	 * 
	 * @param directPositions
	 * @return a Ring
	 */
	public Curve createCurveByDirectPositions(
			List<DirectPosition> aDirectPositions) {
		// Test ok

		// GeometryFactoryImpl coordFactory =
		// this.geometryFactory.getGeometryFactoryImpl();

		// Create List of Position´s
		List<Position> positionList = createPositions(aDirectPositions);
		// List<Position> positionList =
		// coordFactory.createPositions(aDirectPositions);

		// Create List of CurveSegment´s (LineString´s)
		LineStringImpl lineString = new LineStringImpl(new PointArrayImpl(
				positionList), 0.0);
		// LineStringImpl lineString =
		// coordFactory.createLineString(aPositions);
		List<CurveSegment> segments = new ArrayList<CurveSegment>();
		segments.add(lineString);
		
		// Create List of OrientableCurve´s (Curve´s)
		return this.createCurve(segments);
	}

	/**
	 * Converts a List of DirectPosition objects to a List of Position objects
	 * 
	 * @param aDirectPositions
	 *            List of DirectPosition objects
	 * @return List of Position objects
	 */
	public List<Position> createPositions(List<DirectPosition> aDirectPositions) {

		List<Position> rPositions = new LinkedList<Position>();
		for (int i = 0; i < aDirectPositions.size(); i++) {
			rPositions.add(new PositionImpl(aDirectPositions.get(i)));
		}

		return rPositions;
	}

	/**
	 * Creates a curve bu Positions
	 * 
	 * @param aPositions
	 * @return Curve
	 */
	public CurveImpl createCurveByPositions(List<Position> aPositions) {
		// GeometryFactoryImpl coordFactory =
		// this.geometryFactory.getGeometryFactoryImpl();

		// Create List of CurveSegment´s (LineString´s)
		LineStringImpl lineString = new LineStringImpl(new PointArrayImpl(
				aPositions), 0.0);
		// LineStringImpl lineString =
		// coordFactory.createLineString(aPositions);
		List<CurveSegment> segments = new ArrayList<CurveSegment>();
		segments.add(lineString);

		// Create List of OrientableCurve´s (Curve´s)
		return this.createCurve(segments);
	}

	/**
	 * Creates a simple surface without holes by a list of DirectPositions
	 * 
	 * @param positions
	 *            List of positions, the last positions must be equal to the
	 *            first position
	 * @return a Surface defined by the given positions
	 */
	public SurfaceImpl createSurfaceByDirectPositions(
			List<DirectPosition> positions) {
		// Test ok
		Ring extRing = this.createRingByDirectPositions(positions);
		List<Ring> intRings = new ArrayList<Ring>();
		SurfaceBoundary sfb = this.createSurfaceBoundary(extRing, intRings);
		return this.createSurface(sfb);
	}

}
