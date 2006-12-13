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


package org.geotools.geometry.iso.coordinate;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.geotools.geometry.iso.FeatGeomFactoryImpl;
import org.geotools.geometry.iso.primitive.CurveImpl;
import org.geotools.geometry.iso.util.DoubleOperation;
import org.geotools.geometry.iso.util.algorithmND.AlgoPointND;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.spatialschema.geometry.geometry.LineSegment;
import org.opengis.spatialschema.geometry.geometry.PointArray;
import org.opengis.spatialschema.geometry.geometry.Position;

/**
 * 
 * Many of the geometric constructs in this International Standard require the
 * use of reference points which are organized into sequences or grids
 * (sequences of equal length sequences). PointArray::column[1..n] : Position
 * PointGrid::row[1..n] : PointArray
 * 
 * The class name follows the ISO19107. It is a confusing name, since it
 * contains an array of positions and not of points. The positions themselves
 * contain either a direct position or a point.
 * 
 * @author Jackson Roehrig & Sanjay Jena
 */

public class PointArrayImpl implements PointArray {

	// The list of Positions by which the PointArray is defined
	private List<PositionImpl> column = null;

	/**
	 * Creates a new PointArray based on another PointArray. This constructor
	 * creates new Position objects.
	 * 
	 * @param aPointArray
	 */
	public PointArrayImpl(PointArrayImpl aPointArray) {

		if (aPointArray.isEmpty())
			throw new IllegalArgumentException("Parameter PointArray is empty. Cannot create empty PointArray.");

		// Position data will be cloned here
		
		//this.column = new ArrayList<PositionImpl>();
		this.column = this.getFeatGeomFactory().getListFactory().getPositionList();

//		int coordDim = aPointArray.getFirst().getCoordinateDimension();
//		CoordinateFactoryImpl coordFactory = FeatGeomFactoryImpl
//				.getDefaultCoordinateFactory(coordDim);

		// TODO JR: Zur kenntnisnahme:
		// Wie in deinem Vorschlag von unserem Telefonat am 04/10 hole ich die CoordFactory über ein DP
		CoordinateFactoryImpl coordFactory = this.getFeatGeomFactory().getCoordinateFactory();
		
		for (int i = 0; i < aPointArray.length(); i++) {
			this.column.add((PositionImpl) coordFactory
					.createPosition(aPointArray.get(i).getPosition()));
		}
	}

	/**
	 * Construct a new PointArray. This constructor does not create new position
	 * objects.
	 * 
	 * @param positions
	 * 
	 */
	public PointArrayImpl(List<PositionImpl> positions) {
		if (positions.size() == 0)
			throw new IllegalArgumentException("Parameter positions is empty. Cannot create empty PointArray.");
		
		this.column = positions;
	}
	
	
	// TODO JR: Zur kenntnisnahme:
	// Wie in deinem Vorschlag von unserem Telefonat am 04/10 hole ich die CoordFactory über ein DP
	/**
	 * Returns the Feature Geometry Factory Instance based on the reference of the DirectPositions of this PointArray
	 * @return Factory instance
	 */
	private FeatGeomFactoryImpl getFeatGeomFactory() {
		if (this.column.isEmpty()) {
			return null;
		}
		
		DirectPositionImpl tDP = this.column.get(0).getPosition();
		return tDP.getGeometryFactory();		
	}
	

	/**
	 * Returns the Point array as Set of Position
	 * 
	 * @return the positions
	 */
	public List<PositionImpl> getPointArray() {
		// ok
		return this.column;
	}

	/**
	 * Returns the coordiantes of the Position at index
	 * @param arg0
	 * @return double[]
	 */
	public double[] getCoordinate(int index) {
		// test ok
		
		PositionImpl pos = this.get(index);
		return pos.getPosition().getCoordinates();

		// Auskommentiert und geändert durch Sanjay am 21.08.2006
		// der komplette code hat nicht soviel sinn gemacht, wurde nicht getestet
		// Position nicht berücksichtigt wurde
		// OLD CODE:
		// return (obj instanceof PointImpl)
		// (PointImpl)obj).getPosition().getCoordinates();
		// ((PointImpl)obj).getPosition().getCoordinates():
		// (double[])obj;
	}

	/**
	 * Gets the position at index
	 * 
	 * @param arg0
	 * @return PositionImpl
	 */
	public PositionImpl get(int index) {
		// test ok
		return (PositionImpl) this.column.get(index);
	}

	/**
	 * Returns the first element
	 * 
	 * @return Position
	 */
	public PositionImpl getFirst() {
		// ok
		return (PositionImpl) this.column.get(0);
	}

	/**
	 * Returns the last element
	 * 
	 * @return Position
	 */
	public PositionImpl getLast() {
		// ok
		return (PositionImpl) this.column.get(this.column.size() - 1);
	}

//	/**
//	 * @param index
//	 * @param position
//	 */
//	public void setPosition(int index, PositionImpl position) {
//		assert ((index < this.column.size()) && (position != null));
//		this.column.set(index, position);
//	}

//	/**
//	 * @param positions
//	 * @param startPosition
//	 */
//	public void set(List<PositionImpl> positions, int startPosition) {
//		this.set(positions, startPosition, positions.size());
//	}
//
//	/**
//	 * @param positions
//	 * @param startPosition
//	 * @param count
//	 */
//	private void set(List<PositionImpl> positions, int startPosition, int count) {
//
//		assert (startPosition >= 0);
//		if ((startPosition + count) > positions.size())
//			count = (positions.size() - startPosition);
//		for (int i = 0; i < count; ++i) {
//			this.column.add(positions.get(i + startPosition));
//		}
//	}
	
	
//	/**
//	 * Extends the point array with a list of positions
//	 * 
//	 * @param positions to be added at the end of the point array
//	 */
//	public void addLast(List<PositionImpl> positions) {
//		this.column.addAll(positions);
//	}
//	
//	/**
//	 * Inserts a list of positions in beginning of the point array
//	 * 
//	 * @param positions to be added in the start of the point array
//	 */
//	public void addFirst(List<PositionImpl> positions) {
//		LinkedList newColumn = new LinkedList(positions);
//		newColumn.addAll(this.column);
//		this.column = newColumn;
//	}
	

	/**
	 * @return boolean
	 */
	public boolean isEmpty() {
		// Implementation OK
		return this.column.isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.spatialschema.geometry.geometry.PointArray#length()
	 */
	public int length() {
		// Implementation OK
		return this.column.size();
	}

//	/**
//	 * 
//	 */
//	public void reverse() {
//		Collections.reverse(this.column);
//	}

	/**
	 * Creates the absolute length over all points in point array
	 * 
	 * @return absolute length over all points in point array
	 */
	public double getDistanceSum() {
		// Test OK - Methode korrigiert
		double dist = 0.0;
		double[] c0 = this.getCoordinate(0);
		for (int i = 1; i < this.length(); i++) {
			double[] c1 = this.getCoordinate(i);
			dist = DoubleOperation.add(dist, AlgoPointND.getDistance(c0, c1));
			//dist += AlgoPointND.getDistanceSquare(c0, c1);
			c0 = c1;
		}
		//return Math.sqrt(dist);
		return dist;
	}

	/**
	 * Creates an envelope for all points in point array
	 * 
	 * @return envelope for all points in point array
	 */
	public EnvelopeImpl getEnvelope() {

		double[] c0 = getCoordinate(0);
		
		EnvelopeImpl env = this.getFeatGeomFactory().getCoordinateFactory().createEnvelope(c0);
		
		for (int i = 1, n = length(); i < n; i++) {
			double[] c1 = getCoordinate(i);
			env.expand(c1);
		}
		return env;
	}

	/**
	 * Removes the first occurrence of this position from the PointArray
	 * @param p
	 * @return boolean TRUE, if the Remove was successful
	 */
	public boolean removePosition(Position p) {
		// test ok
		return this.column.remove(p);
	}

	public String toString() {
		String rString = ""; //$NON-NLS-1$
		for (int i = 0; i < this.column.size(); i++) {
			rString += this.column.get(i) + ", ";
		}
		return rString;
	}

	/* (non-Javadoc)
	 * @see org.opengis.spatialschema.geometry.geometry.PointArray#getCoordinateReferenceSystem()
	 */
	public CoordinateReferenceSystem getCoordinateReferenceSystem() {
		return this.getFeatGeomFactory().getCoordinateReferenceSystem();
	}

	/* (non-Javadoc)
	 * @see org.opengis.spatialschema.geometry.geometry.PointArray#get(int, org.opengis.spatialschema.geometry.DirectPosition)
	 */
	public DirectPositionImpl get(int col, DirectPosition dest)
			throws IndexOutOfBoundsException {
		// Test ok (SJ)

		PositionImpl pos = this.column.get(col);

		double[] coords = pos.getPosition().getCoordinates();
		
		if (dest != null) {
			// Set coordinates in existing DP
			((DirectPositionImpl)dest).setCoordinate(coords);
		} else {
			// Create new DP with coordinates
			dest = this.getFeatGeomFactory().getCoordinateFactory().createDirectPosition(coords);
		}
		
		return (DirectPositionImpl) dest;
	}

	/* (non-Javadoc)
	 * @see org.opengis.spatialschema.geometry.geometry.PointArray#set(int, org.opengis.spatialschema.geometry.DirectPosition)
	 */
	public void set(int column, DirectPosition position)
			throws IndexOutOfBoundsException, UnsupportedOperationException {
		// Test ok
		// Set copy of the coordinates of the given DirectPosition
		this.set(column, position.getCoordinates().clone());
	}

	/**
	 * Sets the Coordinates of the Position at index in the PointArray
	 * @param index
	 * @param coord
	 */
	public void set(int index, double[] coord) {
		// TODO test
		// Manipulate the coordinates at the Position entry at the index
		PositionImpl pos = this.column.get(index);
		pos.getPosition().setCoordinate(coord);
	}


	/* (non-Javadoc)
	 * @see org.opengis.spatialschema.geometry.geometry.PointArray#positions()
	 */
	public List positions() {
		// Test ok
		return this.column;
	}

	/**
	 * @param minSpacing
	 */
	public void merge(double minSpacing) {
		// TODO Test
		// TODO Documentation
		minSpacing *= minSpacing;
		double[] c0 = getCoordinate(0);
		for (int i = 1, n = length(); i < n; i++) {
			double[] c1 = getCoordinate(i);
			while (AlgoPointND.getDistanceSquare(c0, c1) < minSpacing) {
				this.remove(i);
				n--;
				c1 = getCoordinate(i);
			}
			c0 = c1;
		}
	}

	/**
	 * This method splits the sequence of positions according to a maximum
	 * distance. After splitting the distance between two positions will be
	 * maxSpacing or less. The length and shape of the LineString will not be
	 * changed.
	 * 
	 * @param maxSpacing
	 */
	public void split(double maxSpacing) {
		// TODO Test
		// TODO Documentation

		double[] c0 = getCoordinate(0);
		for (int i = 1, n = length(); i < n; i++) {
			double[] c1 = getCoordinate(i);
			double[][] newCoords = AlgoPointND.split(c0, c1, maxSpacing);
			if (newCoords != null) {
				for (int j = 0; i < newCoords.length; j++, i++, n++) {
					this.set(i, newCoords[j]);
				}
			}
			c0 = c1;
		}
	}

	/**
	 * Removes the Position at index
	 * 
	 * @param index
	 * @return PositionImpl
	 */
	public PositionImpl remove(int index) {
		// Test ok
		return this.column.remove(index);
	}

	/**
	 * Creates a LineSegment connecting the Positins p0 and p1, whereas p0 is
	 * the position at arg0 and p1 is the position at (arg0+1). If (arg0+1) ==
	 * size() then returns the LineSegment connecting the last position with the
	 * first one
	 * 
	 * @return List<LineSegmentImpl>
	 */
	public List<? extends LineSegment> getLineSegments(CurveImpl parentCurve) {
		// test ok (SJ)
		return new LineSegmentsSequence(this, parentCurve);
	}
	
    /**
     * Reverses the orientation of the parameterizations of the control points of this PointArray.
     */
	public void reverse() {
		Collections.reverse(this.column);
	}

	/**
	 * Class to support on-the-fly generation of LineSegments
	 * 
	 * @author roehrig
	 * 
	 */
	public class LineSegmentsSequence extends AbstractList<LineSegmentImpl> {

		private PointArrayImpl pointArray;

		private int index;

		private double length;
		
		private CurveImpl parentCurve = null;


		/**
		 * Create a Line Segment sequence by a pointarray and a parent curve
		 * 
		 * @param pointArray
		 * @param aParentCurve
		 */
		public LineSegmentsSequence(PointArrayImpl pointArray, CurveImpl aParentCurve) {
			this.pointArray = pointArray;
			this.index = 0;
			this.length = 0.0;
			this.parentCurve = aParentCurve;
		}

		/* (non-Javadoc)
		 * @see java.util.AbstractList#get(int)
		 */
		public LineSegmentImpl get(int arg0) {
			double[] p0 = this.getStartCoordinate(arg0);
			double[] p1 = this.getEndCoordinate(arg0);

			// Calculate start param for this line segment
			double startParam = 0.0;
			for (int i=1; i<=arg0; i++) {
				startParam = DoubleOperation.add(startParam, AlgoPointND.getDistance(this.getStartCoordinate(i-1), this.getEndCoordinate(i-1)));
				//startParam += AlgoPointND.getDistance(this.getStartCoordinate(i-1), this.getEndCoordinate(i-1));
			}
			
			LineSegmentImpl rSeg = this.pointArray.getFeatGeomFactory().getCoordinateFactory().createLineSegment(p0, p1, startParam);

			rSeg.setCurve(this.parentCurve);

			return rSeg;
		}

		/**
		 * @param arg0
		 * @param dp
		 * @return DirectPositionImpl
		 */
		public DirectPositionImpl getStartDirectPositionCoordinate(int arg0,
				DirectPosition dp) {
			return this.pointArray.get(arg0, dp);
		}

		/**
		 * @param arg0
		 * @param dp
		 * @return DirectPositionImpl
		 */
		public DirectPositionImpl getEndDirectPositionCoordinate(int arg0,
				DirectPosition dp) {
			return this.pointArray.get(arg0 + 1, dp);
		}

		/**
		 * @param arg0
		 * @return double[]
		 */
		public double[] getStartCoordinate(int arg0) {
			return this.pointArray.getCoordinate(arg0);
		}

		/**
		 * @param arg0
		 * @return double[]
		 */
		public double[] getEndCoordinate(int arg0) {
			return this.pointArray.getCoordinate(arg0 + 1);
		}

		public int size() {
			return this.pointArray.length() - 1;
		}

		/**
		 * @return boolean
		 */
		public boolean hasNext() {
			return this.index < (this.pointArray.length() - 2);
		}

		/**
		 * @return LineSegmentImpl
		 */
		public LineSegmentImpl next() {
			double[] p0 = this.getStartCoordinate(this.index);
			double[] p1 = this.getEndCoordinate(this.index);

//			LineSegmentImpl ls = FeatGeomFactoryImpl
//					.getDefaultCoordinateFactory(p0.length).createLineSegment(
//							p0, p1, this.length);
			LineSegmentImpl ls = this.pointArray.getFeatGeomFactory().getCoordinateFactory().createLineSegment(p0, p1, this.length);

			this.length = DoubleOperation.add(this.length, AlgoPointND.getDistance(p0, p1));
			//this.length += AlgoPointND.getDistance(p0, p1);
			
			return ls;
		}

	}

	/* (non-Javadoc)
	 * @see org.opengis.spatialschema.geometry.geometry.PointArray#getDimension()
	 */
	public int getDimension() {
		return this.getFeatGeomFactory().getCoordinateDimension();
	}



}
