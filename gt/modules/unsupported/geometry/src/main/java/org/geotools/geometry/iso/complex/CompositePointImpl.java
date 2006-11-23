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


package org.geotools.geometry.iso.complex;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.geotools.geometry.iso.FeatGeomFactoryImpl;
import org.geotools.geometry.iso.coordinate.DirectPositionImpl;
import org.geotools.geometry.iso.coordinate.EnvelopeImpl;
import org.geotools.geometry.iso.io.GeometryToString;
import org.geotools.geometry.iso.primitive.BoundaryImpl;
import org.geotools.geometry.iso.primitive.PointImpl;
import org.opengis.spatialschema.geometry.Boundary;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.spatialschema.geometry.Envelope;
import org.opengis.spatialschema.geometry.complex.Complex;
import org.opengis.spatialschema.geometry.complex.CompositePoint;

/**
 * A separate class for composite point, CompositePoint (Figure 27) is included
 * for completeness. It is a Complex containing one and only one Point.
 * 
 * @author Jackson Roehrig & Sanjay Jena
 */
public class CompositePointImpl extends CompositeImpl<PointImpl> implements CompositePoint {

	/**
	 * The association role Composition::generator associates this Composite
	 * Point to the single primitive in this complex. CompositePoint::generator
	 * [1] : Point
	 * 
	 * The generator is realised by the element ArrayList of the super class
	 * Complex and will be passed through the super constructor
	 * 
	 * @param factory
	 * @param generator
	 */
	public CompositePointImpl(FeatGeomFactoryImpl factory, PointImpl generator) {
		/* Call super constructor; elements will be set later */
		super(factory);
		List<PointImpl> list = new ArrayList<PointImpl>();
		list.add(generator);
		this.setElements(list);
	}

	@Override
	public CompositePointImpl clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.geotools.geometry.featgeom.complex.ComplexImpl#createBoundary()
	 */
	@Override
	public Set<Complex> createBoundary() {
		// Return null, because a point doesn´t have a boundary
		return null;
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
	public int dimension(@SuppressWarnings("unused")
	final DirectPositionImpl point) {
		return 0;
	}

	/**
	 * The method <code>envelope</code> returns the minimum bounding box for
	 * this Object. There are cases for which the min and max positions would be
	 * outside the domain of validity of the object's coordinate reference
	 * system. This method is included here only as an interface, as
	 * applications may choose to implement in different manners.
	 * 
	 * In this case the Envelope is the bounding box for the point of the
	 * CompositePoint.
	 * 
	 * @return a <code>GeoRectangle2D</code> value
	 */
	public EnvelopeImpl envelope() {
		PointImpl p = (PointImpl) ((List<PointImpl>) this.getElements()).get(0);
		return p.getGeometryFactory().getCoordinateFactory().createEnvelope(p.getEnvelope());
		
		//DirectPositionImpl p = ((PointImpl) ((List<PointImpl>) this.getElements()).get(0)).getPosition();
		//return new EnvelopeImpl(p);
	}

	/**
	 * The method "boundary" returns a finite set of Objects containing all of
	 * the direct positions on the boundary of this Object. These object
	 * collections have further internal structure where appropriate, and are
	 * represented as subclasses of the datatype Boundary that is a subtype of
	 * Complex. The finite set of Objects returned is in the same coordinate
	 * reference system as this Object. If the Object is in a Complex, then the
	 * boundary Objects returned is in the same Complex. If the Object is not in
	 * any Complex, then the boundary Objects returned may have been constructed
	 * in response to the method. The organization of the set returned is
	 * dependent on the type of Object. Each of the subclasses of Object
	 * specifies the organization of its boundary set more completely. The
	 * elements of a boundary are smaller in dimension than the original
	 * element. Constrain: all objects in the boundary are of at least 1
	 * dimension smaller than the original Object: boundary select(dimension) <=
	 * self.dimension - 1
	 * 
	 * In this case the function returns NULL. The instance of the
	 * CompositePoint contains one Point, which boundary is an empty set (such
	 * as the Boundary of Point).
	 * 
	 * @return a <code>Boundary</code> value
	 */
	public BoundaryImpl boundary() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.stdss.fgeo.complex.Composite#getGeneratorType()
	 */
	public Class getGeneratorClass() {
		return PointImpl.class;
	}

	public boolean isSimple() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isCycle() {
		// TODO Auto-generated method stub
		return false;
	}

	public List getGenerators() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boundary getBoundary() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Complex getClosure() {
		// Return this CompositePoint - TODO is that right?
		return this;
	}

	@Override
	public int getDimension(DirectPosition point) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.geotools.geometry.featgeom.root.GeometryImpl#getEnvelope()
	 */
	public Envelope getEnvelope() {
		// OK
		return this.elements.get(0).getEnvelope();
	}

	@Override
	public DirectPosition getRepresentativePoint() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return GeometryToString.getString(this);
	}

}
