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
import org.geotools.geometry.iso.complex.ComplexImpl;
import org.opengis.spatialschema.geometry.Boundary;

/**
 * 
 * The abstract root data type for all the data types used to represent the
 * boundary of geometric objects is Boundary (Figure 7). Any subclass of Object
 * will use a subclass of Boundary to represent its boundary through the
 * operation Object::boundary. By the nature of geometry, boundary objects are
 * cycles. Boundary: {isCycle() = TRUE}
 * 
 * @author Jackson Roehrig & Sanjay Jena
 */
public abstract class BoundaryImpl extends ComplexImpl implements Boundary {

	/**
	 * @param factory
	 */
	public BoundaryImpl(FeatGeomFactoryImpl factory) {
		super(factory);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.spatialschema.geometry.root.Geometry#isCycle()
	 */
	public boolean isCycle() {
		// implementation ok
		// Boundaries are always a cycle, because their boundary is empty
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.geotools.geometry.featgeom.root.GeometryImpl#getBoundary()
	 */
	public Boundary getBoundary() {
		// Wie telefonisch besprochen 4.Okt.2006 geben wir hier NULL zurueck
		// A boundary does not have a boundary. Thus, the Boundary of a Boundary is NULL.
		return null;
	}	
	
	
}
