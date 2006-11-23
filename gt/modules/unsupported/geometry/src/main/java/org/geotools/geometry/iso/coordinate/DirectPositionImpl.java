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

import org.geotools.geometry.iso.FeatGeomFactoryImpl;
import org.geotools.geometry.iso.util.DoubleOperation;
import org.geotools.geometry.iso.util.algorithmND.AlgoPointND;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.spatialschema.geometry.DirectPosition;

/**
 * @author Jackson Roehrig & Sanjay Jena
 * 
 */
public class DirectPositionImpl implements DirectPosition {

	/**
	 * The attribute "coordinate" is a sequence of Numbers that hold the
	 * coordinate of this position in the specified reference system.
	 * DirectPosition2D::coordinate : Sequence<Number>
	 * 
	 * In order that this system will be based on an euclidic 2D system, the
	 * coordinates will be stored in explicit variables for each X- and
	 * Y-Coordinate
	 */
	private double[] coordinate;

	private FeatGeomFactoryImpl factory;

	/**
	 * Creates a direct Position initializing all ordiante values with 0.0
	 * 
	 * @param factory
	 */
	public DirectPositionImpl(FeatGeomFactoryImpl factory) {
		int n = factory.getCoordinateDimension();
		this.factory = factory;
		this.coordinate = new double[n];
		for (int i = 0; i < n; ++i)
			this.coordinate[i] = 0.0;
	}

	/**
	 * Creates a direct Position by using coordinates of another direct Position
	 * 
	 * @param factory
	 * @param coord
	 */
	public DirectPositionImpl(FeatGeomFactoryImpl factory, double[] coord) {
		this.factory = factory;
		this.coordinate = coord;
	}

	/**
	 * Creates a direct Position by using coordinates of another direct Position
	 * 
	 * @param factory
	 * @param p
	 */
	public DirectPositionImpl(FeatGeomFactoryImpl factory,
			final DirectPosition p) {
		this.factory = factory;
		// Comment by Sanjay
		// VORSICHT: Die folgende Codezeile verursachte, dass das selbe Objekt
		// (double Array) verwendet wurde; folglich wurde z.B. beim
		// Envelope Min und Max Position auf die selben Koordinaten zugegriffen.
		// this.coordinate=p.getCoordinates();
		// Bitte um kenntnisnahme und berücksichtigung in sourcen: Arrays müssen
		// explizit kopiert werden, nur elementare Datentypen werden automatisch
		// von Java neu erzeugt, alles andere sind nur Referenzen
		// TODO Das Klonen sollte in die Factory verlagert werden
		this.coordinate = p.getCoordinates().clone();
	}

	/**
	 * @param factory
	 * @param x
	 * @param y
	 * @param z
	 */
	public DirectPositionImpl(FeatGeomFactoryImpl factory, double x, double y,
			double z) {
		this.factory = factory;
		this.coordinate = new double[] { x, y, z };
	}

	/**
	 * @param factory
	 * @param x
	 * @param y
	 * @param z
	 * @param m
	 */
	public DirectPositionImpl(FeatGeomFactoryImpl factory, double x, double y,
			double z, double m) {
		this.factory = factory;
		this.coordinate = new double[] { x, y, z, m };
	}

	/**
	 * The Feature Geometry Factory can be accessed through this getter-method.
	 * Hereby Coordinate classes get possibility to get the factory´s instance.
	 * TODO I don´t know if this is a clear design, but it works for now. (SJ)
	 * @return GeometryFactoryImpl
	 */
	public FeatGeomFactoryImpl getGeometryFactory() {
		return this.factory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.spatialschema.geometry.DirectPosition#getDimension()
	 */
	public int getDimension() {
		// TODO semantic JR
		return this.factory.getCoordinateDimension();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.spatialschema.geometry.DirectPosition#getCoordinates()
	 */
	public double[] getCoordinates() {
		// TODO semantic JR
		return this.coordinate;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.spatialschema.geometry.DirectPosition#getOrdinate(int)
	 */
	public double getOrdinate(int dimension) throws IndexOutOfBoundsException {
		// TODO semantic JR
		return this.coordinate[dimension];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.spatialschema.geometry.DirectPosition#setOrdinate(int,
	 *      double)
	 */
	public void setOrdinate(int dimension, double value)
			throws IndexOutOfBoundsException {
		// TODO semantic JR
		// TODO documentation
		if (dimension >= this.coordinate.length || dimension < 0)
			throw new IndexOutOfBoundsException("Index out of coordinate range"); //$NON-NLS-1$
		this.coordinate[dimension] = value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.spatialschema.geometry.DirectPosition#getCoordinateReferenceSystem()
	 */
	public CoordinateReferenceSystem getCoordinateReferenceSystem() {
		// TODO semantic JR
		// TODO implementation Is the CRS correct/existent?
		// TODO test
		// TODO documentation
		return this.factory.getCoordinateReferenceSystem();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.spatialschema.geometry.DirectPosition#clone()
	 */
	public DirectPositionImpl clone() {
		// TODO semantic JR
		// Cloning the double array (in parameter) is important!

		// Return new DirectPosition by cloning the Coordiante array of double which define the position
		return new DirectPositionImpl(this.factory, this.coordinate.clone());
	}

	/**
	 * @param coord
	 */
	public void setCoordinate(double[] coord) {
		if (coord.length != this.getDimension())
			throw new IllegalArgumentException("Index out of coordinate range"); //$NON-NLS-1$
		this.coordinate = coord;
	}

	/**
	 * Returns the x value of the coordinate represented by this DirectPosition
	 * @return x
	 */
	public double getX() {
		return this.coordinate[0];
	}

	/**
	 * Returns the y value of the coordinate represented by this DirectPosition
	 * @return y
	 */
	public double getY() {
		return this.coordinate[1];
	}

	/**
	 * Returns the z value of the coordinate represented by this DirectPosition
	 * @return z
	 */
	public double getZ() {
		return (this.getDimension() > 2) ? this.coordinate[2] : Double.NaN;
	}

	/**
	 * Sets the x value of the coordinate represented by this DirectPosition
	 * 
	 * @param x
	 */
	public void setX(double x) {
		this.coordinate[0] = x;
	}

	/**
	 * Sets the y value of the coordinate represented by this DirectPosition
	 * 
	 * @param y
	 */
	public void setY(double y) {
		this.coordinate[1] = y;
	}

	/**
	 * Sets the z value of the coordinate represented by this DirectPosition
	 * 
	 * @param z
	 */
	public void setZ(double z) {
		if (this.getDimension() > 2)
			this.coordinate[2] = z;
	}

	/**
	 * Compares coodinates of DirectPosition Implementation Note: Parameter has
	 * to be of Type DirectPosition (not DirectPositionImpl), so that the equals
	 * method is found for DirectPosition´s and DirectPositionImpl´s
	 * 
	 * @param p
	 *            DirectPosition
	 * @return TRUE, if the two DirectPositions describe the same point in the
	 *         Euclidian Space
	 */

	// Sanjay: The method was replaced by the equals(Object) method below,
	// because it was not recognized in all cases
	// public boolean equals(DirectPosition p) {
	// return this.equals(p, 0);
	// }
	// TODO JR: nach zur kenntnisnahme und zustimmung bitte obiges kommentar loeschen
	public boolean equals(Object o) {
		if (o instanceof DirectPosition || o instanceof DirectPositionImpl)
			return this.equals((DirectPosition) o, 0);
		else
			return false;
	}

	/**
	 * Compares coodinates of Direct Positions and allows a tolerance value in
	 * the comparison Implementation Note: Parameter has to be of Type
	 * DirectPosition (not DirectPositionImpl), so that the equals method is
	 * found for DirectPosition´s and DirectPositionImpl´s
	 * 
	 * @param p
	 *            Direct Position to compare with
	 * @param tol Epsilon tolerance value
	 * @return TRUE, if coordinates accord concording to the tolerance value, FALSE if they dont.
	 */
	public boolean equals(DirectPosition p, double tol) {
		if (p.getDimension() != this.getDimension())
			return false;
		double coord[] = p.getCoordinates();
		for (int i = 0; i < this.coordinate.length; ++i) {
			if (Math.abs(DoubleOperation.subtract(coord[i], this.coordinate[i])) > tol)
				return false;
		}
		return true;
	}

	
	public String toString() {
		double coord[] = this.getCoordinates();
		String str = "(" + Double.toString(coord[0]);
		for (int i = 1; i < coord.length; ++i) {
			str += " " + Double.toString(coord[i]);
		}
		return str + ")";
	}

	/**
	 * Calculates the distance to another direct position
	 * 
	 * @param p
	 *            direct Position
	 * @return Distance
	 */
	public double distance(DirectPosition p) {
		return AlgoPointND.getDistance(this.coordinate, p.getCoordinates());
	}

	/**
	 * Calculates the square of the distance to another direct position
	 * 
	 * @param p
	 *            another direct Position
	 * @return Distance
	 */
	public double distanceSquare(DirectPosition p) {
		return AlgoPointND.getDistanceSquare(this.coordinate, p
				.getCoordinates());
	}
	
	
	

// Auskommentiert, da ungenutzt und nicht getestet
//	/**
//	 * Adds a DirectPosition to the position
//	 * 
//	 * @param p
//	 *            DirectPosition to add
//	 * @return new Position
//	 */
//	public DirectPositionImpl add(DirectPosition p) {
//		return new DirectPositionImpl(this.factory, AlgoPointND.add(
//				this.coordinate, p.getCoordinates()));
//	}

//	/**
//	 * @param factor
//	 * @return DirectPositionImpl
//	 */
//	public DirectPositionImpl add(double factor) {
//		return new DirectPositionImpl(this.factory, AlgoPointND.add(
//				this.coordinate, factor));
//	}

//	 Auskommentiert, da ungenutzt und nicht getestet
//	/**
//	 * Subtracts a direct position from the position
//	 * 
//	 * @param p
//	 * @return new Position
//	 */
//	public DirectPositionImpl subtract(DirectPositionImpl p) {
//		return new DirectPositionImpl(this.factory, AlgoPointND.subtract(
//				this.coordinate, p.coordinate));
//	}

// Auskommentiert, da die Methode auf einer nicht-robusten Methode scale-Methode von AlgoPointND basiert.
// Die Methode liefert daher teilweise falsche Ergebnisse. S. JUNIT Test (SJ)
//	 TODO 1) benoetigen wir die methode. wenn ja:
//        2) algorithmus als robuste version implementieren oder sind interne rundungsfehler ok?
//	/**
//	 * @param factor
//	 * @return DirectPositionImpl
//	 */
//	public DirectPositionImpl scale(double factor) {
//		return new DirectPositionImpl(this.factory, AlgoPointND.scale(
//				this.coordinate, factor));
//	}


// Auch diese Methode beruht auf einem Algorithmus, welcher wahrscheinlich nicht robust ist.
// TODO 1) benoetigen wir die methode. wenn ja:
//      2) algorithmus als robuste version implementieren oder sind interne rundungsfehler ok?
//	/**
//	 * @return GM_DirectPosition
//	 */
//	public DirectPositionImpl normalize() {
//		return new DirectPositionImpl(this.factory, AlgoPointND
//				.normalize(this.coordinate.clone()));
//	}

// Diese methode hat meiner meinung nach nichts in dieser klasse zu suchen (SJ)
// das interpolieren von einer straight line gehoert nicht zur aufgabe einer directPosition
// eher zu lineSegment. ich habe die vorhandene methode dort (in LineSegment) deswegen entsprechend angepasst.
// 
//	/**
//	 * 
//	 * @param p0
//	 * @param p1
//	 * @param r
//	 * @return DirectPositionImpl
//	 */
//	public static DirectPositionImpl evaluate(DirectPositionImpl p0,
//			DirectPositionImpl p1, double r) {
//		// TODO Documentation
//		// TODO Test
//		return new DirectPositionImpl(p0.factory, AlgoPointND.evaluate(
//				p0.coordinate, p1.coordinate, r));
//	}

//	 Diese methode hat meiner meinung nach nichts in dieser klasse zu suchen (SJ)
//	 das interpolieren von einer straight line gehoert nicht zur aufgabe einer directPosition
//	 eher zu lineSegment. ich habe die vorhandene methode dort (in LineSegment) deswegen entsprechend angepasst.
//	/**
//	 * @param p0
//	 * @param p1
//	 * @param eval
//	 * @return DirectPositionImpl
//	 */
//	public static DirectPositionImpl evaluate(DirectPositionImpl p0,
//			DirectPositionImpl p1, DirectPositionImpl eval) {
//		return new DirectPositionImpl(p0.factory, AlgoPointND.evaluate(
//				p0.coordinate, p1.coordinate, eval.coordinate));
//	}

	
// Nicht genutzt, nicht getestet
//	/**
//	 * Returns the length (Distance between origin and position)
//	 * 
//	 * @return Length
//	 */
//	public double length() {
//		return AlgoPointND.getDistanceToOrigin(this.coordinate);
//	}

//	 Nicht genutzt, nicht getestet
//	/**
//	 * @return double
//	 */
//	public double lengthSquare() {
//		return AlgoPointND.getDistanceToOriginSquare(this.coordinate);
//	}
	
	

	// TODO JR: Wenn du nichts dagegen hast, den folgenden teil bitte rausloeschen. solche methoden werden in robuster form in jts angeboten.
	
	// public double cross2D(DirectPositionImpl dp){
	// // corresponds to the 2*area of two vectors
	// return this.getX() * dp.getY() - this.getY() * dp.getX();
	// }
	//
	// public boolean intersectWithHorizontalLineFromRight2D(DirectPositionImpl
	// p0, DirectPositionImpl p1){
	// // returns true when a horizontal line passing at ME:
	// // 1) intersects the line with origin p0 and and p1 and
	// // 2) when ME is on the right side of the line
	// double x0 = p0.getX(); // line endpoint 2D coords
	// double y0 = p0.getY(); // line endpoint 2D coords
	// double x1 = p1.getX(); // line endpoint 2D coords
	// double y1 = p1.getY(); // line endpoint 2D coords
	// double xa = x0; // swap coordinates
	// double ya = y0; // swap coordinates
	// double xb = x1; // swap coordinates
	// double yb = y1; // swap coordinates
	// double max_x = Math.max(x0, x1); // maximum x coordinate
	// double min_x = Math.min(x0, x1); // minimum x coordinate
	// double max_y = Math.max(y0, y1); // maximum y coordinate
	// double min_y = Math.min(y0, y1); // minimum y coordinate
	//
	// // the horizontal line does not intersect the line to the
	// // left of location pt if:
	// // [1] if line is horizontal
	// if ( y0 == y1 ) return false;
	// // (2) if the y coordinate of point is outside the range
	// // max_y and min_y (but not including min_y)
	// if ( ((this.getY() < min_y) || (this.getY() >= max_y)) ) return false;
	// // (3) if given line is vertical and y coordinate of point is
	// // smaller than that of line
	// if ( ((x0 == x1) && (this.getX() < x0)) ) return false;
	// // (4) if inclined line is located to the right of given point
	// // (first reduce the problem to a case where yb >ya, always)
	// if ( (x0 != x1) ) {
	// if ( !(((x1 > x0) && (y1 > y0)) || ((x1 < x0) && (y1 > y0))) ) {
	// xa = x1;
	// ya = y1;
	// xb = x0;
	// yb = y0;
	// }
	// if ( (((this.getY() - ya) * (xb - xa)) > ((this.getX() - xa) * (yb -
	// ya))) ) {
	// return false;
	// }
	// }
	// // if we get here that is because the horizontal line passing
	// // at the location this intersects the given line to the left of the pt
	// return true;
	// }
	//
	// public double getAngle2D(DirectPositionImpl p1){
	// // * p1
	// // /
	// // /
	// // /
	// // *------>*
	// // (0,0) this
	// double angle = Math.atan2(p1.getY(), p1.getX()) - Math.atan2(this.getY(),
	// this.getX());
	// if ( angle < 0.0 ) angle = angle + 2 * Math.PI;
	// if ( angle > (2 * Math.PI) ) angle = angle - 2 * Math.PI;
	// return angle;
	// }
	//
	// public double minAngle2D(DirectPositionImpl p1, DirectPositionImpl p2){
	// double ang0 = ((DirectPositionImpl)
	// p1.subtract(this)).getAngle2D((DirectPositionImpl)p2.subtract(this));
	// double ang1 =
	// ((DirectPositionImpl)p2.subtract(p1)).getAngle2D((DirectPositionImpl)this.subtract(p1));
	// return Math.min(Math.min(ang0, ang1), Math.min(ang0, Math.PI - ang0 -
	// ang1));
	// }
	//    
	// public String toString() {
	// String str = Double.toString(this.coordinate[0]);
	// for (int i=1; i<this.getDimension(); ++i) {
	// str += "| " + Double.toString(this.coordinate[i]);
	// }
	// return "[DirectPosition: " + str + "]";
	// }
	//
	// /**
	// * Builds the scalar product
	// * @param p
	// * @return Scalar product
	// */
	// public double scalar(DirectPositionImpl p) {
	// double result = 0.0;
	// double pCoord[] = p.getCoordinates();
	// for (int i=0; i<this.getDimension(); ++i) {
	// result += this.coordinate[i] * pCoord[i];
	// }
	// return result;
	// }
	//
	// public static Object cross(DirectPositionImpl p0, DirectPositionImpl p1){
	// int n = Math.min(p0.getDimension(),p1.getDimension());
	// if (n==2) {
	// // corresponds to the 2*area of two vectors
	// double p0Coord[] = p0.getCoordinates();
	// double p1Coord[] = p1.getCoordinates();
	// return (Double)p0Coord[0] * p1Coord[1] - p0Coord[1] * p1Coord[0];
	// } else if (n==3) {
	// // TODO
	// assert false;
	// DirectPositionImpl result = null;
	// return null;
	// } else {
	// assert false;
	// return null;
	// }
	// }
}
