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

import java.util.Set;

import org.opengis.geometry.complex.Complex;
import org.opengis.geometry.primitive.OrientablePrimitive;
import org.opengis.geometry.primitive.Primitive;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * 
 * OrientableSurface consists of a surface and an orientation inherited from
 * OrientablePrimitive. If the orientation is "+", then the OrientableSurface is
 * a Surface. If the orientation is "-", then the OrientableSurface is a
 * reference to a Surface with an upNormal that reverses the direction for this
 * OrientableSurface, the sense of "the top of the surface" (see 6.4.33.2).
 * 
 * OrientableSurface: {Orientation = "+" implies primitive = self};
 * {(Orientation = "-" and TransfiniteSet::contains(p : DirectPosition2D))
 * implies (primitive.upNormal(p) = - self.upNormal(p))};
 * 
 * @author Jackson Roehrig & Sanjay Jena
 */
public abstract class OrientableSurfaceImpl extends OrientablePrimitiveImpl
		implements OrientablePrimitive {

	/**
	 * Constructor
	 * 
	 * @param crs
	 * 
	 */
	protected OrientableSurfaceImpl(CoordinateReferenceSystem crs) {
		super(crs);
	}

	/**
	 * @param crs
	 * @param containedPrimitive
	 * @param containingPrimitive
	 * @param complex
	 */
	protected OrientableSurfaceImpl(CoordinateReferenceSystem crs,
			Set<Primitive> containedPrimitive,
			Set<Primitive> containingPrimitive, Set<Complex> complex) {
		super(crs, containedPrimitive, containingPrimitive, complex);
	}

	// /**
	// * Sets the primitive
	// * (influences the orientation directly).
	// * @param primitive
	// */
	// protected void setPrimitive(Primitive primitive) {
	// super.setPrimitive(primitive);
	// if (this.equals(primitive)) {
	// super.setOrientation(OrientablePrimitive.POSITIVE);
	// } else {
	// super.setOrientation(OrientablePrimitive.NEGATIVE);
	// }
	// }

	// /**
	// * Sets the Boundary of the Orientable Surface
	// * @param Boundary
	// */
	// protected void setBoundary(SurfaceBoundary boundary) {
	// this.m_boundary = boundary;
	// }
	//	

	// /**
	// * The operation "boundary" specializes the boundary operation defined at
	// Object with the appropriate return
	// * type for OrientableSurface. It shall return the set of circular
	// sequences of OrientableCurve that limit the
	// * globelExtent of this Surface. These curves shall be organized into one
	// circular sequence of curves for each
	// * boundary component of the Surface.
	// *
	// * OrientableSurface::boundary(): SurfaceBoundary;
	// *
	// * In cases where "exterior" boundary is not well defined, all the rings
	// of the SurfaceBoundary shall be listed as
	// * "interior".
	// *
	// * NOTE The concept of exterior boundary for a surface is really only
	// valid in a 2-dimensional plane. A bounded cylinder has
	// * two boundary components, neither of which can logically be classified
	// as its exterior. Thus, in 3 dimensions, there is no valid
	// * definition of exterior that covers all cases.
	// *
	// * @return SurfaceBoundary
	// */
	// public Boundary boundary() {
	// return null;
	// }

}
