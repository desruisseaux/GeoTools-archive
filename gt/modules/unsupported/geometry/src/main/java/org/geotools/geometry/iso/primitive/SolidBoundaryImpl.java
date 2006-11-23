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
import java.util.Set;

import org.geotools.geometry.iso.FeatGeomFactoryImpl;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.spatialschema.geometry.Envelope;
import org.opengis.spatialschema.geometry.complex.Complex;
import org.opengis.spatialschema.geometry.primitive.Shell;
import org.opengis.spatialschema.geometry.primitive.SolidBoundary;

/**
 * The boundary of Solids shall be represented as SolidBoundary.
 * 
 * @author Jackson Roehrig & Sanjay Jena
 * 
 */
public class SolidBoundaryImpl extends PrimitiveBoundaryImpl implements
		SolidBoundary {

	/**
	 * SolidBoundaries are similar to SurfaceBoundaries. In normal 3-dimensional
	 * Euclidean space, one shell is distinguished as the exterior. In the more
	 * general case, this is not always possible.
	 * 
	 * SolidBoundary::exterior[0,1] : Shell; SolidBoundary::interior[0..n] :
	 * Shell;
	 * 
	 * NOTE An alternative use of solids with no external shell would be to
	 * define "complements" of finite solids. These infinite solids would have
	 * only interior boundaries. If this standard is extended to 4D Euclidean
	 * space, or if 3D compact manifolds are used (probably not in geographic
	 * information), then other examples of bounded solids without exterior
	 * boundaries are possible.
	 */
	private ShellImpl exterior = null;

	private ArrayList interior = null; /* ArrayList of Shell */

	/**
	 * @param factory
	 */
	public SolidBoundaryImpl(FeatGeomFactoryImpl factory) {
		super(factory);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Set<Complex> createBoundary() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SolidBoundaryImpl clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return null;
	}

	public Shell getExterior() {
		// TODO Auto-generated method stub
		return null;
	}

	public Shell[] getInteriors() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isSimple() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Complex getClosure() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param point
	 * @return 3
	 * 
	 */
	@Override
	public int getDimension(DirectPosition point) {
		// TODO Dimension of a SolidBoundary is 2 or 3??
		return 2;
	}

	@Override
	public Envelope getEnvelope() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DirectPosition getRepresentativePoint() {
		// TODO Auto-generated method stub
		return null;
	}

}
