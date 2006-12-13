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


package org.geotools.geometry.iso.coordinate;

import java.util.ArrayList;
import java.util.List;

import org.geotools.geometry.iso.FeatGeomFactoryImpl;
import org.geotools.geometry.iso.primitive.SurfaceBoundaryImpl;
import org.opengis.spatialschema.geometry.geometry.Polygon;
import org.opengis.spatialschema.geometry.geometry.Triangle;
import org.opengis.spatialschema.geometry.geometry.TriangulatedSurface;

/**
 * 
 * A TriangulatedSurface (Figure 21) is a PolyhedralSurface that is composed
 * only of triangles (Triangle). There is no restriction on how the
 * triangulation is derived.
 * 
 * @author Jackson Roehrig & Sanjay Jena
 */
public class TriangulatedSurfaceImpl extends PolyhedralSurfaceImpl implements
		TriangulatedSurface {

	/**
	 * Constructor
	 * 
	 * @param factory
	 * @param triangles
	 */
	public TriangulatedSurfaceImpl(FeatGeomFactoryImpl factory,
			List<Polygon> triangles) {
		super(factory, triangles);
	}

	/**
	 * Constructor without arguments Triangle Patches have to be setted after
	 * 
	 * @param factory
	 */
	public TriangulatedSurfaceImpl(FeatGeomFactoryImpl factory) {
		super(factory, (SurfaceBoundaryImpl) null);
	}

	/**
	 * @param factory
	 * @param boundary
	 */
	public TriangulatedSurfaceImpl(FeatGeomFactoryImpl factory,
			SurfaceBoundaryImpl boundary) {
		super(factory, boundary);
	}

	/**
	 * Sets the Triangles for the Triangulated Surface
	 * 
	 * @param triangles
	 * @param surfaceBoundary
	 */
	public void setTriangles(ArrayList<TriangleImpl> triangles,
			SurfaceBoundaryImpl surfaceBoundary) {
		super.setPatches(triangles, surfaceBoundary);
		// JR eingefügt und aus den TIN Konstruktoren entfernt
		for (TriangleImpl triangle : triangles) {
			triangle.setAssociatedSurface(this);
		}
	}
	
    /* (non-Javadoc)
     * @see org.geotools.geometry.iso.primitive.SurfaceImpl#getPatches()
     */
    public List<Triangle> getPatches() {
    	return (List<Triangle>) this.patch;
    }

	
	
}
