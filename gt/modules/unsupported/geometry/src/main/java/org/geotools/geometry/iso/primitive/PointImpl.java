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

import org.geotools.geometry.iso.FeatGeomFactoryImpl;
import org.geotools.geometry.iso.coordinate.DirectPositionImpl;
import org.geotools.geometry.iso.io.GeometryToString;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.spatialschema.geometry.Envelope;
import org.opengis.spatialschema.geometry.TransfiniteSet;
import org.opengis.spatialschema.geometry.complex.Complex;
import org.opengis.spatialschema.geometry.geometry.Position;
import org.opengis.spatialschema.geometry.primitive.Bearing;
import org.opengis.spatialschema.geometry.primitive.OrientablePrimitive;
import org.opengis.spatialschema.geometry.primitive.Point;
import org.opengis.spatialschema.geometry.primitive.PrimitiveBoundary;

/**
 * 
 * Point is the basic data type for a geometric object consisting of one and
 * only one point.
 * 
 * @author Jackson Roehrig & Sanjay Jena
 */

public class PointImpl extends PrimitiveImpl implements Point {

	/**
	 * The attribute "position" is the DirectPosition of this Point.
	 * 
	 * Point::position : DirectPosition
	 * 
	 * Point is the only subclass of Primitive that cannot use Positions to
	 * represent its defining geometry. A Position is either a DirectPostion or
	 * a reference to a Point (from which a DirectPosition2D may be obtained).
	 * 
	 * By not allowing Point to use this technique, infinitely recursive
	 * references are prevented. Applications may choose another mechanism to
	 * prevent this logical problem.
	 * 
	 * NOTE In most cases, the state of a Point is fully determined by its
	 * position attribute. The only exception to this is if the Point has been
	 * subclassed to provide additional non-geometric information such as
	 * symbology.
	 */
	private DirectPositionImpl position = null;

	/**
	 * The constructor PointImpl creates a Point at a given position, backed by the DirectPosition
	 * PointImpl::PointImpl(position : DirectPositionImpl) : PointImpl
	 * 
	 * @param factory
	 * @param dp
	 */
	public PointImpl(FeatGeomFactoryImpl factory, final DirectPositionImpl dp) {
		super(factory, null, null, null);
		// Cloning of the DP is done in the factory class
		this.position = dp;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geotools.geometry.featgeom.root.GeometryImpl#clone()
	 */
	public PointImpl clone() throws CloneNotSupportedException {
		return this.getGeometryFactory().getPrimitiveFactory().createPoint(
				this.getPosition().getCoordinates());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.spatialschema.geometry.primitive.Point#getPosition()
	 */
	public DirectPositionImpl getPosition() {
		return this.position;
	}

	/**
	 * Sets the value of position
	 * 
	 * @param p
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.spatialschema.geometry.primitive.Point#setPosition(org.opengis.spatialschema.geometry.DirectPosition)
	 */
	public void setPosition(DirectPosition p) {
		this.position = this.getGeometryFactory().getCoordinateFactory()
				.createDirectPosition(p);
	}

	/**
	 * Overwrite toString method for WKT output
	 */
	public String toString() {
		return GeometryToString.getString(this);
	}

	/**
	 * The operation "bearing" returns the bearing, as a unit vector, of the
	 * tangent (at this Point) to the curve between this Point and a passed
	 * Position.
	 * 
	 * Point::bearing(toPoint : Position) : Bearing
	 * 
	 * The choice of the curve type for defining the bearing is dependent on the
	 * SC_CRS in which this Point is defined. For example, in the Mercator
	 * projection, the curve is the rhumb line. In 3D, geocentric coordinate
	 * system, the curve may be the geodesic joining the two points along the
	 * surface of the geoid or ellipsoid in use. Implementations that support
	 * this function shall specify the nature of the curve to be used.
	 * 
	 * NOTE The type "Vector" is a common data type defined in ISO TS 19103.
	 * 
	 * @param toPoint
	 * @return Bearing
	 * 
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.spatialschema.geometry.primitive.Point#getBearing(org.opengis.spatialschema.geometry.geometry.Position)
	 */
	public Bearing getBearing(Position toPoint) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * The method <code>getDimension</code> returns the inherent dimension of
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
	 *            a <code>DirectPosition</code> value
	 * @return the value 0 as <code>int</code>
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geotools.geometry.featgeom.root.GeometryImpl#getDimension(org.opengis.spatialschema.geometry.DirectPosition)
	 */
	public int getDimension(DirectPosition point) {
		return 0;
	}

	/**
	 * The method <code>getEnvelope</code> returns the minimum bounding box
	 * for this Object. There are cases for which the min and max positions
	 * would be outside the domain of validity of the object's coordinate
	 * reference system. This method is included here only as an interface, as
	 * applications may choose to implement in different manners.
	 * 
	 * @return an <code>Envelope</code> with zero width and length
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geotools.geometry.featgeom.root.GeometryImpl#getEnvelope()
	 */
	public Envelope getEnvelope() {
		/* Return envelope only consisting of this point */
		return this.getGeometryFactory().getCoordinateFactory().createEnvelope(
				this.position.getCoordinates());
	}

	/**
	 * The operation "getBoundary" is a specialization of the boundary operation
	 * at Object, and returns a NULL value indication an empty set.
	 * 
	 * Point::boundary() : NULL
	 * 
	 * @return null
	 */
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geotools.geometry.featgeom.primitive.PrimitiveImpl#getBoundary()
	 */
	public PrimitiveBoundary getBoundary() {
		// a Point does not have a Boundary
		return null;
	}



	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geotools.geometry.featgeom.root.GeometryImpl#getRepresentativePoint()
	 */
	public DirectPosition getRepresentativePoint() {
		return this.position;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geotools.geometry.featgeom.root.GeometryImpl#getClosure()
	 */
	public Complex getClosure() {
		// TODO Semantics JR
		// TODO Test
		// Return a CompositePoint which contains this point
		return this.getGeometryFactory().getComplexFactory().createCompositePoint(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.spatialschema.geometry.root.Geometry#isSimple()
	 */
	public boolean isSimple() {
		// A Point is always simple
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.spatialschema.geometry.root.Geometry#isCycle()
	 */
	public boolean isCycle() {
		// A Point is always a cicle, because its boundary is always NULL
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geotools.geometry.featgeom.root.GeometryImpl#getCentroid()
	 */
	public DirectPosition getCentroid() {
		// Return this point, which is its own centroid
		return this.position;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.spatialschema.geometry.primitive.Primitive#getProxy()
	 */
	public OrientablePrimitive[] getProxy() {
		// TODO ok?!
		return null;
	}
	

	/* (non-Javadoc)
	 * @see org.geotools.geometry.featgeom.root.GeometryImpl#equals(org.opengis.spatialschema.geometry.TransfiniteSet)
	 */
	public boolean equals(TransfiniteSet pointSet) {
		// Overwrite the equals method for performance reason
		// In that way, we do not need to build a topological graph to compare this trivial coordinates

		// If the parameter is not a Point, call the equals method from GM_Object
		if (!(pointSet instanceof PointImpl))
			return super.equals(pointSet);
		return this.getPosition().equals(((PointImpl)pointSet).getPosition());
	}

}