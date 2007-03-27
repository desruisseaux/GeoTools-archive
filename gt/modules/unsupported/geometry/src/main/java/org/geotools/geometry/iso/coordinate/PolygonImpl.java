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


package org.geotools.geometry.iso.coordinate;

import java.util.List;

import org.geotools.geometry.iso.primitive.SurfaceBoundaryImpl;
import org.geotools.geometry.iso.primitive.SurfaceImpl;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.Envelope;
import org.opengis.geometry.coordinate.Polygon;

/**
 * @author Jackson Roehrig & Sanjay Jena
 * 
 * A Polygon (Figure 21) is a surface patch that is defined by a set of boundary
 * curves and an underlying surface to which these curves adhere. The default is
 * that the curves are coplanar and the polygon uses planar interpolation in its
 * interior.
 */
public class PolygonImpl extends SurfacePatchImpl implements Polygon {

	/**
	 * The attribute "boundary" stores the SurfaceBoundary that is the boundary
	 * of this Polygon.
	 * 
	 * Polygon::boundary : SurfaceBoundary
	 * 
	 * NOTE The boundary of a surface patch need not be in the same Complex as
	 * the containing Surface. The curves that are contained in the interior of
	 * the Surface (act as common boundary to 2 surface patches) are not part of
	 * any Complex in which the Surface is contained. They are purely
	 * constructive and would not play in any topological relation between
	 * Surface and Curve that defines the connectivity of the Complex.
	 * 
	 * IMPLEMENTATION ANNOTATION: The boundary will be realised and stored
	 * within the super class SurfacePatch
	 */

	/**
	 * The optional spanning surface provides a mechanism for spanning the
	 * interior of the polygon.
	 * 
	 * Polygon::spanningSurface [0,1] : Surface
	 * 
	 * NOTE The spanning surface should have no boundary components that
	 * intersect the boundary of the polygon, and there should be no ambiguity
	 * as to which portion of the surface is described by the bounding curves
	 * for the polygon. The most common spanning surface is an elevation model,
	 * which is not directly described in this standard, although Tins and
	 * gridded surfaces are often used in this role.
	 */
	// Spanning surface of the Polygon
	private SurfaceImpl spanningSurface = null;

	// Envelope of the Polygon
	private EnvelopeImpl envelope = null;

	// Array of Neighbours of the Polygon
	//private SurfacePatchImpl m_neighbours[] = null;

	/**
	 * Constructor This first variant of a constructor of Polygon creates a
	 * Polygon directly from a set of boundary curves (organized into a
	 * SurfaceBoundary) which shall be defined using coplanar Positions as
	 * controlPoints. Polygon::Polygon(boundary : SurfaceBondary) : Polygon
	 * 
	 * NOTE The meaning of exterior in the SurfaceBoundary is consistent with
	 * the plane of the constructed planar polygon.
	 * 
	 * @param boundary
	 */
	public PolygonImpl(SurfaceBoundaryImpl boundary) {
		this(boundary, null);
	}

	/**
	 * This second variant of a constructor of Polygon creates a Polygon lying
	 * on a spanning surface. There is no restriction of the types of
	 * interpolation used by the composite curves used in the SurfaceBoundary,
	 * but they must all be lie on the "spanningSurface" for the process to
	 * succeed. Polygon(boundary : SurfaceBondary, spanSurf : Surface) : Polygon
	 * 
	 * NOTE It is important that the boundary components be oriented properly
	 * for this to work. It is often the case that in bounded manifolds, such as
	 * the sphere, there is an ambiguity unless the orientation is properly
	 * used.
	 * 
	 * @param boundary
	 * @param spanSurf -
	 *            the Spanning Surface of the polygon
	 */
	public PolygonImpl(SurfaceBoundaryImpl boundary, SurfaceImpl spanSurf) {
		// The Constructor will not except a boundary which is NULL; an
		// exception will shall be thrown in the constructor of the super class
		// SurfacePatch
		// The boundary shall be build in lower classes like Triangle
		/* Call super constructor to store boundary */
		super(boundary);
		/* Set Spanning Surface of the Polygon */
		this.spanningSurface = spanSurf;
		/* Create Envelope of the Polygon */
		this.envelope = (EnvelopeImpl) this.createEnvelope();
	}

	/* (non-Javadoc)
	 * @see org.geotools.geometry.featgeom.coordinate.SurfacePatchImpl#getEnvelope()
	 */
	public Envelope getEnvelope() {
		return this.envelope;
	}

	/**
	 * Creates the Envelope for the Polygon
	 * 
	 * @return Envelope for the Polygon
	 */
	private Envelope createEnvelope() {
		/* Return Envelope of the given Surface Patch Boundary */
		return (this.getBoundary() != null) ? this.getBoundary().getEnvelope()
				: null;
	}

	// /**
	// * This method returns the neighbours of the SurfacePatch within the
	// relative Surface.
	// * The order and number of the returned SurfacePatch Array depends from
	// the type of SurfacePatch and has to be implemented individually.
	// * This method can be useful for example for calculating the boundary of a
	// surface, which is only defined by patches.
	// * @return Ordered Array of neighbours of the SurfacePatch; NULL if
	// neighbours not set
	// */
	// public SurfacePatchImpl[] getNeighbours() {
	// return this.m_neighbours;
	// }

	// /**
	// * Sets the neighbours of this SurfacePatch
	// * @param neighbourPatches
	// */
	// public void setNeighbours(SurfacePatchImpl[] neighbourPatches) {
	// this.m_neighbours = neighbourPatches;
	// }

//	/**
//	 * spanningSurface The optional spanning surface provides a mechanism for
//	 * spanning the interior of the polygon.
//	 * 
//	 * Polygon::spanningSurface [0,1] : Surface
//	 * 
//	 * NOTE The spanning surface should have no boundary components that
//	 * intersect the boundary of the polygon, and there should be no ambiguity
//	 * as to which portion of the surface is described by the bounding curves
//	 * for the polygon. The most common spanning surface is an elevation model,
//	 * which is not directly described in this standard, although Tins and
//	 * gridded surfaces are often used in this role.
//	 * 
//	 * @return SurfaceImpl
//	 */
//	public SurfaceImpl spanningSurface() {
//		return this.spanningSurface;
//	}

//	/**
//	 * @return double
//	 */
//	public double perimeter() {
//		return this.getBoundary().getLength();
//	}


	/* (non-Javadoc)
	 * @see org.opengis.geometry.coordinate.Polygon#getSpanningSurface()
	 */
	public List getSpanningSurface() {
		// TODO semantic JR, SJ
		// TODO implementation
		// TODO test
		// TODO documentation
		return null;
	}

	/* (non-Javadoc)
	 * @see org.opengis.geometry.coordinate.GenericSurface#getUpNormal(org.opengis.geometry.coordinate.DirectPosition)
	 */
	public double[] getUpNormal(DirectPosition point) {
		// TODO semantic JR, SJ
		// TODO implementation
		// TODO test
		// TODO documentation
		return null;
	}

	/* (non-Javadoc)
	 * @see org.opengis.geometry.coordinate.GenericSurface#getPerimeter()
	 */
	public double getPerimeter() {
		// TODO semantic JR, SJ
		// TODO implementation
		// TODO test
		// TODO documentation
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.opengis.geometry.coordinate.GenericSurface#getArea()
	 */
	public double getArea() {
		// TODO semantic JR, SJ
		// TODO implementation
		// TODO test
		// TODO documentation
		return 0;
	}

}
