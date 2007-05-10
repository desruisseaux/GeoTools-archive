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

import org.opengis.geometry.Boundary;
import org.opengis.geometry.aggregate.MultiCurve;
import org.opengis.geometry.primitive.OrientableCurve;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class MultiCurveImpl extends MultiPrimitiveImpl implements MultiCurve {

	/**
	 * Creates a MultiCurve by a set of Curves.
	 * @param crs
	 * @param curves Set of Curves which shall be contained by the MultiCurve
	 */
	public MultiCurveImpl(CoordinateReferenceSystem crs, Set<OrientableCurve> curves) {
		super(crs, curves);
	}

	/* (non-Javadoc)
	 * @see org.opengis.geometry.coordinate.aggregate.MultiCurve#length()
	 */
	public double length() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	/* (non-Javadoc)
	 * @see org.geotools.geometry.featgeom.aggregate.MultiPrimitiveImpl#getBoundary()
	 */
	public Boundary getBoundary() {
		// TODO Auto-generated method stub
		// We shall return a set of points, but in which boundary object?!
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.geotools.geometry.featgeom.aggregate.MultiPrimitiveImpl#getElements()
	 */
	public Set<OrientableCurve> getElements() {
		return (Set<OrientableCurve>) super.elements;
	}

}
