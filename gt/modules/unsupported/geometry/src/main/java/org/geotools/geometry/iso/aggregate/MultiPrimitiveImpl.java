/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006-2007, GeoTools Project Management Committee (PMC)
 *    (C) 2006       University of Applied Sciences Köln (Fachhochschule Köln)
 *    
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */

package org.geotools.geometry.iso.aggregate;

import java.util.Iterator;
import java.util.Set;

import org.geotools.geometry.iso.coordinate.DirectPositionImpl;
import org.geotools.geometry.iso.coordinate.EnvelopeImpl;
import org.geotools.geometry.iso.io.GeometryToString;
import org.geotools.geometry.iso.primitive.PrimitiveImpl;
import org.geotools.geometry.iso.root.GeometryImpl;
import org.opengis.geometry.Boundary;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.Envelope;
import org.opengis.geometry.aggregate.MultiPrimitive;
import org.opengis.geometry.complex.Complex;
import org.opengis.geometry.primitive.Primitive;
import org.opengis.geometry.Geometry;
import org.opengis.referencing.crs.CoordinateReferenceSystem;


public class MultiPrimitiveImpl extends AggregateImpl implements MultiPrimitive {

	/**
	 * Creates a MultiPrimitive by a set of Primitives.
	 * @param crs
	 * @param primitives Set of Primitives which shall be contained by the MultiPrimitive
	 */
	public MultiPrimitiveImpl(CoordinateReferenceSystem crs, Set<? extends Primitive> primitives) {
		super(crs, primitives);
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
		
		// union the various primitives together and return the boundary of that
		Boundary boundary = null;
		Iterator iterator = this.elements.iterator();
		while (iterator.hasNext()) {
			PrimitiveImpl p = (PrimitiveImpl) iterator.next();
			if (boundary == null) {
				boundary = p.getBoundary();
			}
			else {
				if (p.getBoundary() != null) {
					boundary.union(p.getBoundary());
				}
			}
		}
		
		return boundary;
		
		
		//return null;
	}

	/* (non-Javadoc)
	 * @see org.geotools.geometry.featgeom.root.GeometryImpl#getDimension(org.opengis.geometry.coordinate.DirectPosition)
	 */
	public int getDimension(DirectPosition point) {
		if (point != null) {
			return point.getDimension();
		}
		else {
			// return the largest dimension of all the contained elements in this collection
			int maxD = 0;
			Set<Primitive> elem = this.getElements();
			Iterator<Primitive> iterator = elem.iterator();
			while (iterator.hasNext()) {
				Geometry prim = iterator.next();
				int D = prim.getDimension(null);
				if (D > maxD) maxD = D;
			}
			return maxD;
		}
	}

	/* (non-Javadoc)
	 * @see org.geotools.geometry.featgeom.root.GeometryImpl#getEnvelope()
	 */
	public Envelope getEnvelope() {
		EnvelopeImpl env = new EnvelopeImpl(new DirectPositionImpl( getCoordinateReferenceSystem(), (new double[] {Double.NaN, Double.NaN})) );
		Iterator<? extends Geometry> elementIter = this.elements.iterator();
		while (elementIter.hasNext()) {
			env.add((EnvelopeImpl)((Primitive)elementIter.next()).getEnvelope());
		}
		return env;		
	}

	/* (non-Javadoc)
	 * @see org.opengis.geometry.coordinate.aggregate.Aggregate#getElements()
	 */
	public Set getElements() {
		return super.elements;
	}

	/* (non-Javadoc)
	 * @see org.opengis.geometry.coordinate.root.Geometry#isSimple()
	 */
	public boolean isSimple() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.opengis.geometry.coordinate.root.Geometry#getMaximalComplex()
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
