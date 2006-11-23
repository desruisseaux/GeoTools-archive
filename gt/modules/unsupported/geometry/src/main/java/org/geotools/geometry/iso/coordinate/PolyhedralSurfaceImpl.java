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

import java.util.List;

import org.geotools.geometry.iso.FeatGeomFactoryImpl;
import org.geotools.geometry.iso.primitive.SurfaceBoundaryImpl;
import org.geotools.geometry.iso.primitive.SurfaceImpl;
import org.opengis.spatialschema.geometry.geometry.Polygon;
import org.opengis.spatialschema.geometry.geometry.PolyhedralSurface;

/**
 * 
 * A PolyhedralSurface (Figure 21) is a Surface composed of polygon surfaces
 * (Polygon) connected along their common boundary curves. This differs from
 * Surface only in the restriction on the types of surface patches acceptable.
 * 
 * @author Jackson Roehrig & Sanjay Jena
 */
public class PolyhedralSurfaceImpl extends SurfaceImpl implements
		PolyhedralSurface {

	/**
	 * The constructor for a PolyhedralSurface takes the facet Polygons and
	 * creates the necessary aggregate surface.
	 * 
	 * PolyhedralSurface::PolyhedralSurface(tiles[1..n]: Polygon ) :
	 * PolyhedralSurface
	 * 
	 * @param factory
	 * @param tiles
	 */
	public PolyhedralSurfaceImpl(FeatGeomFactoryImpl factory,
			List<Polygon> tiles) {
		super(factory, tiles);

	}

	/**
	 * @param factory
	 * @param boundary
	 */
	public PolyhedralSurfaceImpl(FeatGeomFactoryImpl factory,
			SurfaceBoundaryImpl boundary) {
		super(factory, boundary);
	}

	// /**
	// * Constructor without arguments Surface Polygons has to be setted later
	// * @param factory
	// */
	// public PolyhedralSurfaceImpl(GeometryFactoryImpl factory) {
	// super(factory);
	// }


}
