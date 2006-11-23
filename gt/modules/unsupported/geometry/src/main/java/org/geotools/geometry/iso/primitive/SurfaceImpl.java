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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.geotools.geometry.iso.FeatGeomFactoryImpl;
import org.geotools.geometry.iso.coordinate.EnvelopeImpl;
import org.geotools.geometry.iso.coordinate.SurfacePatchImpl;
import org.geotools.geometry.iso.io.GeometryToString;
import org.geotools.geometry.iso.operation.IsSimpleOp;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.spatialschema.geometry.Envelope;
import org.opengis.spatialschema.geometry.complex.Complex;
import org.opengis.spatialschema.geometry.complex.CompositeSurface;
import org.opengis.spatialschema.geometry.primitive.Surface;
import org.opengis.spatialschema.geometry.primitive.SurfaceBoundary;
import org.opengis.spatialschema.geometry.primitive.SurfacePatch;

/**
 * 
 * Surface (Figure 12) a subclass of Primitive and is the basis for
 * 2-dimensional geometry. Unorientable surfaces such as the Möbius band are not
 * allowed. The orientation of a surface chooses an "up" direction through the
 * choice of the upward normal, which, if the surface is not a cycle, is the
 * side of the surface from which the exterior boundary appears
 * counterclockwise. Reversal of the surface orientation reverses the curve
 * orientation of each boundary component, and interchanges the conceptual "up"
 * and "down" direction of the surface. If the surface is the boundary of a
 * solid, the "up" direction is usually outward. For closed surfaces, which have
 * no boundary, the up direction is that of the surface patches, which must be
 * consistent with one another. Its included SurfacePatches describe the
 * interior structure of a Surface
 * 
 * NOTE Other than the restriction on orientability, no other "validity"
 * condition is required for Surface.
 * 
 * @author Jackson Roehrig & Sanjay Jena
 */
public class SurfaceImpl extends OrientableSurfaceImpl implements Surface {

	/**
	 * The "Segmentation" association relates this Surface to a set of
	 * SurfacePatches that shall be joined together to form this Surface.
	 * Depending on the interpolation method, the set of patches may require
	 * significant additional structure. In general, the form of the patches
	 * shall be defined in the application schema.
	 * 
	 * Surface::patch [1..n] : SurfacePatch SurfacePatch::surface [0,1] :
	 * Reference<Surface>
	 * 
	 * If the Surface.coordinateDimension is 2, then the entire Surface is one
	 * logical patch defined by linear interpolation from the boundary.
	 * 
	 * NOTE In this standard, surface patches do not appear except in the
	 * context of a surface, and therefore the cardinality of the “surface” role
	 * in this association could be “1” which would preclude the use of surface
	 * patches except in this manner. While this would not affect this Standard,
	 * leaving the cardinality as “0..1” allows other standards based on this
	 * one to use surface patches in a more open-ended manner.
	 */
	private ArrayList<SurfacePatch> patch = null;

	private SurfaceBoundary boundary = null;

	private Envelope envelope;

	// /**
	// * Constructor without arguments
	// * Surface Patches have to be setted after
	// * @param factory
	// */
	// public SurfaceImpl(GeometryFactoryImpl factory) {
	// super(factory);
	// this.patch = null;
	// }

	/**
	 * Constructor The first version of the constructor for Surface takes a list
	 * of SurfacePatches with the appropriate side-toside relationships and
	 * creates a Surface.
	 * 
	 * Surface::Surface(patch[1..n] : SurfacePatch) : Surface
	 * 
	 * @param factory
	 * @param patch
	 */
	public SurfaceImpl(FeatGeomFactoryImpl factory,
			List<? extends SurfacePatch> patch) {
		super(factory, null, null, null);
		this.initializeSurface(patch, null);
	}

	/**
	 * Constructor The second version, which is guaranteed to work always in 2D
	 * coordinate spaces, constructs a Surface by indicating its boundary as a
	 * collection of Curves organized into a SurfaceBoundary. In 3D coordinate
	 * spaces, this second version of the constructor shall require all of the
	 * defining boundary Curve instances to be coplanar (lie in a single plane)
	 * which will define the surface interior.
	 * 
	 * Surface::Surface(bdy : SurfaceBoundary) : Surface
	 * 
	 * @param factory
	 * 
	 * @param boundary
	 *            The SurfaceBoundary which defines the Surface
	 */
	public SurfaceImpl(FeatGeomFactoryImpl factory, SurfaceBoundary boundary) {

		super(factory, null, null, null);

		// Set Boundary
		this.boundary = boundary;

		// Set Envelope
		this.envelope = boundary.getEnvelope();

		// TODO Ist es wirklich notwendig, dass wir die SurfacePatches erzeugen?
		// Create Surface Patch on basis of the Boundary
		this.patch = new ArrayList<SurfacePatch>();
		this.patch.add(this.getGeometryFactory().getCoordinateFactory()
				.createPolygon(boundary, this));
	}

	/**
	 * Initializes the Surface:
	 * - Sets the surface patches
	 * - Sets the Boundary, or calculates it if doesn´t exist
	 * 
	 * @param patch
	 *            List of SurfacePatch´s
	 * @param surfaceBoundary
	 *            SurfaceBoundary; will be calculated if this parameter is NULL
	 */
	private void initializeSurface(List<? extends SurfacePatch> patch,
			SurfaceBoundaryImpl surfaceBoundary) {

		if (patch == null)
			throw new IllegalArgumentException("Empty array SurfacePatch."); //$NON-NLS-1$

		if (patch.isEmpty())
			throw new IllegalArgumentException("Empty array SurfacePatch."); //$NON-NLS-1$

		// TODO The continuity of the SurfacePatches should be verified here!
		
		/* Add patches to patch list */
		this.patch = new ArrayList<SurfacePatch>();
		for (SurfacePatch p : patch) {
			if (p != null)
				this.patch.add(p);
		}

		// Calculate the boundary for the SurfacePatches (only if no parameter
		// was given)
		if (surfaceBoundary == null) {
			// TODO JR. Das funktioniert noch nicht - Build the Boundary on
			// basis of the SurfacePatches
			// this.setBoundary(new
			// SurfaceBoundaryImpl((SurfacePatchImpl)patch));
		} else {
			this.boundary = surfaceBoundary;
		}

		// Build the envelope for the Surface on basis of the SurfacePatch
		// envelopes
		SurfacePatchImpl tFirstPatch = (SurfacePatchImpl) patch.get(0);
		//this.envelope = new EnvelopeImpl(tFirstPatch.getEnvelope());
		this.envelope = this.getGeometryFactory().getCoordinateFactory().createEnvelope(tFirstPatch.getEnvelope());
		for (SurfacePatch p : patch)
			((EnvelopeImpl) this.envelope).expand(((SurfacePatchImpl) p)
					.getEnvelope());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geotools.geometry.featgeom.primitive.PrimitiveImpl#getBoundary()
	 */
	public SurfaceBoundaryImpl getBoundary() {
		// ok
		// Return the Boundary of this surface
		return (SurfaceBoundaryImpl) boundary;
	}

	/**
	 * Sets the Boundary of the Surface
	 * 
	 * @param boundary
	 *            The boundary to set.
	 */
	public void setBoundary(SurfaceBoundaryImpl boundary) {
		this.boundary = boundary;
	}


	/**
	 * Sets the Surface Patches and Boundary for the Surface
	 * 
	 * @param surfacePatches -
	 *            ArrayList of Surface Patches, which represent the Surface
	 * @param surfaceBoundary -
	 *            Surface Boundary of the Surface
	 */
	protected void setPatches(List<? extends SurfacePatch> surfacePatches,
			SurfaceBoundaryImpl surfaceBoundary) {
		// TODO semantic JR
		// TODO implementation
		// TODO test
		// TODO documentation
		this.initializeSurface(surfacePatches, surfaceBoundary);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.spatialschema.geometry.primitive.Surface#getPatches()
	 */
	public ArrayList<SurfacePatch> getPatches() {
		return this.patch;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geotools.geometry.featgeom.root.GeometryImpl#getEnvelope()
	 */
	public Envelope getEnvelope() {
		// TODO documentation
		return this.envelope;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.stdss.fgeo.primitive.OrientablePrimitive#createMate()
	 */
	protected OrientablePrimitiveImpl createProxy() {
		// TODO semantic SJ, JR
		// TODO implementation
		// TODO test
		// TODO documentation
		return null;
	}

// Not used
//	/**
//	 * @param distance
//	 */
//	public void splitBoundary(double distance) {
//		this.getBoundary().split(distance);
//	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geotools.geometry.featgeom.root.GeometryImpl#clone()
	 */
	public SurfaceImpl clone() throws CloneNotSupportedException {
		// Test OK
		// Clone SurfaceBoundary and use it to create new Surface
		SurfaceBoundary newBoundary = (SurfaceBoundary) this.boundary.clone();
		return this.getGeometryFactory().getPrimitiveFactory().createSurface(newBoundary);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.spatialschema.geometry.primitive.OrientableSurface#getComposite()
	 */
	public CompositeSurface getComposite() {
		// TODO semantic SJ, JR
		// TODO implementation
		// TODO test
		// TODO documentation
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.spatialschema.geometry.root.Geometry#isSimple()
	 */
	public boolean isSimple() {
		// Test OK
		// Test simplicity by building a topological graph and testing for self-intersection
		// Is Simple, if the exterior ring and the interior rings does not have selfintersections
		// and the exterior ring and the interior rings don´t touch or intersect each other. 
		IsSimpleOp simpleOp = new IsSimpleOp();
		return simpleOp.isSimple(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.spatialschema.geometry.root.Geometry#isCycle()
	 */
	public boolean isCycle() {
		// TODO test
		// TODO semantic: A surface is a cycle if the exterior ring and the interior rings are closed? that is always the case.
		// Is that correct?!
		// Always return true, since open sets are not supported. The boundary of a Surface is always closed.
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.spatialschema.geometry.geometry.GenericSurface#getUpNormal(org.opengis.spatialschema.geometry.DirectPosition)
	 */
	public double[] getUpNormal(DirectPosition point) {
		// TODO semantic SJ, JR
		// TODO implementation
		// TODO test
		// TODO documentation
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.spatialschema.geometry.geometry.GenericSurface#getPerimeter()
	 */
	public double getPerimeter() {
		// TODO semantic SJ, JR
		// TODO implementation
		// TODO test
		// TODO documentation
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opengis.spatialschema.geometry.geometry.GenericSurface#getArea()
	 */
	public double getArea() {
		// TODO semantic SJ, JR
		// TODO implementation
		// TODO test
		// TODO documentation
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geotools.geometry.featgeom.root.GeometryImpl#getClosure()
	 */
	public Complex getClosure() {
		// TODO semantic SJ, JR
		// TODO implementation
		// TODO test
		// TODO documentation
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geotools.geometry.featgeom.root.GeometryImpl#getDimension(org.opengis.spatialschema.geometry.DirectPosition)
	 */
	public int getDimension(DirectPosition point) {
		// TODO semantic SJ, JR
		// TODO implementation
		// TODO test
		// TODO documentation
		return 2;
	}

	public String toString() {
		return GeometryToString.getString(this);
	}


	/**
	 * Returns a list of the rings which define the surface: First element is
	 * the exterior ring (island), the following elements, if exist, define the
	 * interior rings (holes)
	 * 
	 * @return List of RingImpl: First element is the exterior ring (island),
	 *         the following elements, if exist, define the interior rings
	 *         (holes)
	 */
	public List<RingImpl> getBoundaryRings() {

		List<RingImpl> rList = new ArrayList();
		rList.add((RingImpl) this.boundary.getExterior());
		Iterator tInteriorRings = this.boundary.getInteriors().iterator();

		while (tInteriorRings.hasNext()) {
			rList.add((RingImpl) tInteriorRings.next());
		}

		return rList;

	}

	@Override
	public DirectPosition getRepresentativePoint() {
		// TODO Auto-generated method stub
		return null;
	}

}
