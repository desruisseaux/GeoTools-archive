/*
 * This implementation of the OGC Feature Geometry Abstract Specification
 * (ISO 19107) is a project of the University of Applied Sciences Cologne
 * (Fachhochschule K�ln) in collaboration with GeoTools and GeoAPI.
 *
 * Copyright (C) 2006 University of Applied Sciences K�ln
 *                    (Fachhochschule K�ln) and GeoTools
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
 *     Institut f�r Technologie in den Tropen
 *     Fachhochschule K�ln
 *     Betzdorfer Strasse 2
 *     D-50679 K�ln
 *     Jackson.Roehrig@fh-koeln.de
 *
 *     Sanjay Dominik Jena
 *     san.jena@gmail.com
 *
 */

package org.geotools.geometry.iso.primitive;

import org.opengis.geometry.primitive.PrimitiveBoundary;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * 
 * PrimitiveBoundary The abstract class Primitive boundary is the root for the
 * various return types of the boundary operator for subtypes of Primitive.
 * Since points have no boundary, no special subclass is needed for their
 * boundary.
 * 
 * @author Jackson Roehrig & Sanjay Jena
 */
public abstract class PrimitiveBoundaryImpl extends BoundaryImpl implements
		PrimitiveBoundary {

	/**
	 * @param crs
	 */
	public PrimitiveBoundaryImpl(CoordinateReferenceSystem crs) {
		super(crs);
	}

}
