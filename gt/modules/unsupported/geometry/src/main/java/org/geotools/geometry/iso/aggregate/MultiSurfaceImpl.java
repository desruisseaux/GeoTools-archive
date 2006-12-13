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

import java.util.Set;

import org.geotools.geometry.iso.FeatGeomFactoryImpl;
import org.opengis.spatialschema.geometry.Boundary;
import org.opengis.spatialschema.geometry.aggregate.MultiSurface;
import org.opengis.spatialschema.geometry.primitive.OrientableSurface;

public class MultiSurfaceImpl extends MultiPrimitiveImpl implements MultiSurface {

	
	/**
	 * Creates a MultiSurface by a set of Curves.
	 * @param factory
	 * @param surfaces Set of Surfaces which shall be contained by the MultiSurface
	 */
	public MultiSurfaceImpl(FeatGeomFactoryImpl factory, Set<OrientableSurface> surfaces) {
		super(factory, surfaces);
	}

	/* (non-Javadoc)
	 * @see org.opengis.spatialschema.geometry.aggregate.MultiSurface#getArea()
	 */
	public double getArea() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	/* (non-Javadoc)
	 * @see org.geotools.geometry.featgeom.aggregate.MultiPrimitiveImpl#getBoundary()
	 */
	public Boundary getBoundary() {
		// TODO Auto-generated method stub
		// We shall return a set of Rings, but in which boundary object?!
		return null;
	}
	

	/* (non-Javadoc)
	 * @see org.geotools.geometry.featgeom.aggregate.MultiPrimitiveImpl#getElements()
	 */
	public Set<OrientableSurface> getElements() {
		return (Set<OrientableSurface>) super.elements;
	}

}
