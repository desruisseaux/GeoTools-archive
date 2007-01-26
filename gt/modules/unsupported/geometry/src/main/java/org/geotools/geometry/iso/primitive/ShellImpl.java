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

import java.util.List;

import org.geotools.geometry.iso.FeatGeomFactoryImpl;
import org.geotools.geometry.iso.complex.CompositeSurfaceImpl;
import org.opengis.spatialschema.geometry.primitive.OrientableSurface;
import org.opengis.spatialschema.geometry.primitive.Shell;

/**
 * 
 * A Shell is used to represent a single connected component of a SolidBoundary.
 * It consists of a number of references to OrientableSurfaces connected in a
 * topological cycle (an object whose boundary is empty). Unlike a Ring, a
 * Shell's elements have no natural sort order. Like Rings, Shells are simple.
 * Shell: {isSimple() = TRUE}
 * 
 * @author Jackson Roehrig & Sanjay Jena
 */
public class ShellImpl extends CompositeSurfaceImpl implements Shell {

	/**
	 * @param factory
	 * @param generator
	 */
	public ShellImpl(FeatGeomFactoryImpl factory,
			List<OrientableSurface> generator) {
		super(factory, generator);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geotools.geometry.featgeom.complex.CompositeSurfaceImpl#isSimple()
	 */
	public boolean isSimple() {
		// Implementation ok
		// Shells are always simple
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geotools.geometry.featgeom.complex.CompositeSurfaceImpl#isCycle()
	 */
	public boolean isCycle() {
		// Implementation ok
		// Shells are always simple
		return true;
	}

}
