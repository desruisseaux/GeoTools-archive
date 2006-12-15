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

	/* (non-Javadoc)
	 * @see org.geotools.geometry.featgeom.root.GeometryImpl#clone()
	 */
	public CompositePointImpl clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.geotools.geometry.featgeom.complex.ComplexImpl#createBoundary()
	 */
	public Set<Complex> createBoundary() {
		// Return null, because a point doesn´t have a boundary
		return null;
	}


	/* (non-Javadoc)
	 * @see org.geotools.geometry.featgeom.complex.CompositeImpl#getGeneratorClass()
	 */
	public Class getGeneratorClass() {
		return PointImpl.class;
	}

	/* (non-Javadoc)
	 * @see org.opengis.spatialschema.geometry.root.Geometry#isSimple()
	 */
	public boolean isSimple() {
		// a point is always simple
		return true;
	}

	/* (non-Javadoc)
	 * @see org.opengis.spatialschema.geometry.root.Geometry#isCycle()
	 */
	public boolean isCycle() {
		// A point is always a cicle
		return true;
	}

	/* (non-Javadoc)
	 * @see org.opengis.spatialschema.geometry.complex.Composite#getGenerators()
	 */
	public List getGenerators() {
		return this.elements;
	}

	/* (non-Javadoc)
	 * @see org.geotools.geometry.featgeom.root.GeometryImpl#getBoundary()
	 */
	public Boundary getBoundary() {
		// a point doesn´t have a boundary
		return null;
	}


	/* (non-Javadoc)
	 * @see org.geotools.geometry.featgeom.root.GeometryImpl#getDimension(org.opengis.spatialschema.geometry.DirectPosition)
	 */
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

	/* (non-Javadoc)
	 * @see org.geotools.geometry.featgeom.root.GeometryImpl#getRepresentativePoint()
	 */
	public DirectPosition getRepresentativePoint() {
		// Return the point which defines this CompositePoint
		return ((PointImpl)this.elements.get(0)).getPosition();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return GeometryToString.getString(this);
	}

}
