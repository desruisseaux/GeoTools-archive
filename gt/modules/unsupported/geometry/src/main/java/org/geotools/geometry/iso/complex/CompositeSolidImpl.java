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

import java.util.List;
import java.util.Set;

import org.geotools.geometry.iso.FeatGeomFactoryImpl;
import org.geotools.geometry.iso.coordinate.DirectPositionImpl;
import org.geotools.geometry.iso.primitive.CurveImpl;
import org.geotools.geometry.iso.primitive.SolidImpl;
import org.opengis.spatialschema.geometry.Boundary;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.spatialschema.geometry.Envelope;
import org.opengis.spatialschema.geometry.complex.Complex;

/**
 * A CompositeSolid (Figure 30) shall be a Complex with all the geometric
 * properties of a solid. Essentially, a composite solid is a set of solids that
 * join in pairs on common boundary surfaces to form a single solid.
 * 
 * @author Jackson Roehrig & Sanjay Jena
 * 
 */
public class CompositeSolidImpl extends CompositeImpl<SolidImpl> {

	/**
	 * The association role Composition::generator associates this
	 * CompositeSolid to the primitive Solids in its generating set, that is,
	 * the solids that form the core of this complex. CompositeSolid::generator :
	 * Set<Solid>
	 * 
	 * NOTE To get a full representation of the elements in the Complex, the
	 * Surfaces, Curves and Points on the boundary of the generator set if
	 * Solids would have to be added to the generator list.
	 * 
	 * The Solids generators will be passed through the super constructor and
	 * saved in the element ArrayList of the according Complex
	 * 
	 * @param factory
	 * 
	 * @param generator
	 */
	public CompositeSolidImpl(FeatGeomFactoryImpl factory,
			List<CurveImpl> generator) {
		super(factory, generator);
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
		return 3;
	}

	@Override
	public Class getGeneratorClass() {
		return CompositeSolidImpl.class;
	}

	@Override
	public Set<Complex> createBoundary() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompositeSolidImpl clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isSimple() {
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
	public int getDimension(DirectPosition point) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Envelope getEnvelope() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DirectPosition getRepresentativePoint() {
		// TODO Auto-generated method stub
		return null;
	}

}
