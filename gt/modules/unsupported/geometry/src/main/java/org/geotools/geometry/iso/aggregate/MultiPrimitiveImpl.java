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

package org.geotools.geometry.iso.aggregate;

import java.util.Iterator;
import java.util.Set;

import org.geotools.geometry.iso.FeatGeomFactoryImpl;
import org.geotools.geometry.iso.coordinate.EnvelopeImpl;
import org.geotools.geometry.iso.io.GeometryToString;
import org.geotools.geometry.iso.root.GeometryImpl;
import org.opengis.spatialschema.geometry.Boundary;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.spatialschema.geometry.Envelope;
import org.opengis.spatialschema.geometry.aggregate.MultiPrimitive;
import org.opengis.spatialschema.geometry.complex.Complex;
import org.opengis.spatialschema.geometry.primitive.Primitive;
import org.opengis.spatialschema.geometry.Geometry;


public class MultiPrimitiveImpl extends AggregateImpl implements MultiPrimitive {

	/**
	 * Creates a MultiPrimitive by a set of Primitives.
	 * @param factory
	 * @param primitives Set of Primitives which shall be contained by the MultiPrimitive
	 */
	public MultiPrimitiveImpl(FeatGeomFactoryImpl factory, Set<? extends Primitive> primitives) {
		super(factory, primitives);
	}

	/* (non-Javadoc)
	 * @see org.geotools.geometry.featgeom.root.GeometryImpl#clone()
	 */
	public GeometryImpl clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.geotools.geometry.featgeom.root.GeometryImpl#getBoundary()
	 */
	public Boundary getBoundary() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.geotools.geometry.featgeom.root.GeometryImpl#getDimension(org.opengis.spatialschema.geometry.DirectPosition)
	 */
	public int getDimension(DirectPosition point) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.geotools.geometry.featgeom.root.GeometryImpl#getEnvelope()
	 */
	public Envelope getEnvelope() {
		EnvelopeImpl env = super.getGeometryFactory().getCoordinateFactory().createEnvelope(new double[] {0, 0});
		Iterator<? extends Geometry> elementIter = this.elements.iterator();
		while (elementIter.hasNext()) {
			env.add((EnvelopeImpl)((Primitive)elementIter.next()).getEnvelope());
		}
		return env;		
	}

	/* (non-Javadoc)
	 * @see org.opengis.spatialschema.geometry.aggregate.Aggregate#getElements()
	 */
	public Set getElements() {
		return super.elements;
	}

	/* (non-Javadoc)
	 * @see org.opengis.spatialschema.geometry.root.Geometry#isSimple()
	 */
	public boolean isSimple() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.opengis.spatialschema.geometry.root.Geometry#isCycle()
	 */
	public boolean isCycle() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.opengis.spatialschema.geometry.root.Geometry#getMaximalComplex()
	 */
	public Set<Complex> getMaximalComplex() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.geotools.geometry.featgeom.root.GeometryImpl#getRepresentativePoint()
	 */
	public DirectPosition getRepresentativePoint() {
		// Return the representative point of the first primitive in this aggregate
		Iterator elementIter = this.elements.iterator();
		return ((Geometry)elementIter.next()).getRepresentativePoint();
	}
	
	/**
	 * Overwrite toString method for WKT output
	 */
	public String toString() {
		return GeometryToString.getString(this);
	}

}
