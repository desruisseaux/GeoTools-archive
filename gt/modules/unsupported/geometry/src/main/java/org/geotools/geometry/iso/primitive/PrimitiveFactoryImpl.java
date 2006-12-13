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
import java.util.List;

import org.geotools.geometry.iso.DimensionModel;
import org.geotools.geometry.iso.FeatGeomFactoryImpl;
import org.geotools.geometry.iso.UnsupportedDimensionException;
import org.geotools.geometry.iso.coordinate.CoordinateFactoryImpl;
import org.geotools.geometry.iso.coordinate.DirectPositionImpl;
import org.geotools.geometry.iso.coordinate.EnvelopeImpl;
import org.geotools.geometry.iso.coordinate.LineStringImpl;
import org.geotools.geometry.iso.coordinate.PositionImpl;
import org.geotools.geometry.iso.coordinate.SurfacePatchImpl;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.spatialschema.geometry.Envelope;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;
import org.opengis.spatialschema.geometry.MismatchedReferenceSystemException;
import org.opengis.spatialschema.geometry.geometry.Position;
import org.opengis.spatialschema.geometry.primitive.Curve;
import org.opengis.spatialschema.geometry.primitive.CurveSegment;
import org.opengis.spatialschema.geometry.primitive.OrientableCurve;
import org.opengis.spatialschema.geometry.primitive.Point;
import org.opengis.spatialschema.geometry.primitive.PrimitiveFactory;
import org.opengis.spatialschema.geometry.primitive.Ring;
import org.opengis.spatialschema.geometry.primitive.SolidBoundary;
import org.opengis.spatialschema.geometry.primitive.SurfaceBoundary;
import org.opengis.spatialschema.geometry.primitive.SurfacePatch;

/**
 * @author Jackson Roehrig & Sanjay Jena
 * 
 */
public class PrimitiveFactoryImpl implements PrimitiveFactory {

	private FeatGeomFactoryImpl geometryFactory;

	/**
	 * @param geometryFactory
	 */
	public PrimitiveFactoryImpl(FeatGeomFactoryImpl geometryFactory) {
		this.geometryFactory = geometryFactory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.spatialschema.geometry.primitive.PrimitiveFactory#getCoordinateReferenceSystem()
	 */
	public CoordinateReferenceSystem getCoordinateReferenceSystem() {
		// TODO test
		// TODO documentation
		return this.geometryFactory.getCoordinateReferenceSystem();
	}

	/**
	 * Returns the Coordinate Dimension of the used Coordinate System
	 * (Sanjay)
	 * 
	 * @return dimension Coordinate Dimension used in this Factory
	 */
	public int getDimension() {
		//  Test OK
		return this.geometryFactory.getCoordinateDimension();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.spatialschema.geometry.primitive.PrimitiveFactory#createPoint(double[])
	 */
	public PointImpl createPoint(double[] coord) {
		// Test ok
		if (coord == null)
			throw new NullPointerException();
		if (coord.length != this.getDimension())
			throw new MismatchedDimensionException();
		// The coordinate array will be cloned in the CoordinateFactory
		return new PointImpl(this.geometryFactory, this.geometryFactory
				.getCoordinateFactory().createDirectPosition(coord));
	}

	/**
	 * Creates a Point by copying the coordinates of a given DirectPosition
	 * 
	 * @param dp DirectPosition, will be copied
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
		
		return new PointImpl(this.geometryFactory, dp.clone());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.spatialschema.geometry.primitive.PrimitiveFactory#createPoint(org.opengis.spatialschema.geometry.geometry.Position)
	 */
	public PointImpl createPoint(Position position)
			throws MismatchedReferenceSystemException,
			MismatchedDimensionException {
		// Test ok
		if (position == null)
			throw new IllegalArgumentException("Parameter position is null.");
		
		if (((PositionImpl) position).getCoordinateDimension()
				!= this.getDimension())
			throw new MismatchedDimensionException();
		
		// The create-Method called will clone the DP
		return createPoint((DirectPositionImpl) position.getPosition());
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
		return new CurveBoundaryImpl(this.geometryFactory,
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
		return new CurveBoundaryImpl(this.geometryFactory, p0, p1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.spatialschema.geometry.primitive.PrimitiveFactory#createCurve(java.util.List)
	 */
	public CurveImpl createCurve(List<CurveSegment> segments) {
		// test OK
		if (segments == null)
			throw new NullPointerException();
		
		// A curve will be created
		// - The curve will be set as parent curves for the Curve segments
		// - Start and end params for the CurveSegments will be set
		return new CurveImpl(this.geometryFactory, segments);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.spatialschema.geometry.primitive.PrimitiveFactory#createRing(java.util.List)
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
			// TODO JR: Zur Kenntnisnahme und Berücksichtigung in Sourcen: Für alle
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
		return new RingImpl(this.geometryFactory, orientableCurves);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.spatialschema.geometry.primitive.PrimitiveFactory#createSurfaceBoundary(org.opengis.spatialschema.geometry.primitive.Ring,
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
		return new SurfaceBoundaryImpl(this.geometryFactory, exterior,
				interiors);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.spatialschema.geometry.primitive.PrimitiveFactory#createSurface(java.util.List)
	 */
	public SurfaceImpl createSurface(List<SurfacePatch> surfacePatches)
			throws MismatchedReferenceSystemException,
			MismatchedDimensionException {
		
		// tested in /test/TestSurface.java
		// TODO SurfaceBoundary NOT calculated !!!

		// Create Surface
		SurfaceImpl rSurface = new SurfaceImpl(this.geometryFactory,
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
	 * @see org.opengis.spatialschema.geometry.primitive.PrimitiveFactory#createSurface(org.opengis.spatialschema.geometry.primitive.SurfaceBoundary)
	 */
	public SurfaceImpl createSurface(SurfaceBoundary boundary)
			throws MismatchedReferenceSystemException,
			MismatchedDimensionException {
		// Test ok
		// Creates a Surface without SurfacePatches
		return new SurfaceImpl(this.geometryFactory, boundary);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.spatialschema.geometry.primitive.PrimitiveFactory#createSolid(org.opengis.spatialschema.geometry.primitive.SolidBoundary)
	 */
	public SolidImpl createSolid(SolidBoundary boundary)
			throws MismatchedReferenceSystemException,
			MismatchedDimensionException {
		// TODO semantic SJ, JR
		// TODO implementation
		// TODO test
		// TODO documentation
		return new SolidImpl(this.geometryFactory, boundary);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.spatialschema.geometry.primitive.PrimitiveFactory#createPrimitive(org.opengis.spatialschema.geometry.Envelope)
	 */
	public PrimitiveImpl createPrimitive(Envelope envelope)
			throws MismatchedReferenceSystemException,
			MismatchedDimensionException {
		
		// Test ok
		
		DimensionModel dm = this.geometryFactory.getDimensionModel();
		if (dm.is2D() || dm.is2o5D()) {
			// 2D or 2.5D: Create a surface defined by the four corners of the envelope
			List<DirectPosition> positions = new ArrayList<DirectPosition>();
			try {
				positions.add(envelope.getLowerCorner());
				positions.add(((EnvelopeImpl)envelope).getNWCorner());
				positions.add(envelope.getUpperCorner());
				positions.add(((EnvelopeImpl)envelope).getSECorner());
				positions.add(envelope.getLowerCorner());
			} catch (UnsupportedDimensionException e) {
				throw new IllegalArgumentException("Inconsistent program error");
			}
			return this.createSurfaceByDirectPositions(positions);
			
		} else {
			// 3D
			// TODO Create Solid for the 3d Case
			assert false;
			return null;
		}
	}

	/**
	 * Creates a Ring conforming to the given DirectPositions.
	 * Helps to build Rings for SurfaceBoundaries.
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

		CoordinateFactoryImpl coordFactory = this.geometryFactory
				.getCoordinateFactory();

		// Create List of Position´s
		List<Position> positionList = coordFactory
				.createPositions(aDirectPositions);

		return this.createCurveByPositions(positionList);
	}

	/**
	 * Creates a curve bu Positions
	 * 
	 * @param aPositions
	 * @return Curve
	 */
	public CurveImpl createCurveByPositions(List<Position> aPositions) {
		CoordinateFactoryImpl coordFactory = this.geometryFactory
				.getCoordinateFactory();

		// Create List of CurveSegment´s (LineString´s)
		LineStringImpl lineString = coordFactory.createLineString(aPositions);
		List<CurveSegment> segments = new ArrayList<CurveSegment>();
		segments.add(lineString);

		// Create List of OrientableCurve´s (Curve´s)
		return this.createCurve(segments);
	}
	
	/**
	 * Creates a simple surface without holes by a list of DirectPositions
	 * 
	 * @param positions List of positions, the last positions must be equal to the first position
	 * @return a Surface defined by the given positions
	 */
	public SurfaceImpl createSurfaceByDirectPositions(List<DirectPosition> positions) {
		// Test ok
		Ring extRing = this.createRingByDirectPositions(positions);
		List<Ring> intRings = new ArrayList<Ring>();
		SurfaceBoundary sfb = this.createSurfaceBoundary(extRing, intRings);
		return this.createSurface(sfb);
	}


}
