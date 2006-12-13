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

import org.geotools.geometry.iso.coordinate.DirectPositionImpl;
import org.geotools.geometry.iso.coordinate.LineStringImpl;
import org.geotools.geometry.iso.coordinate.PositionImpl;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.spatialschema.geometry.complex.CompositeCurve;
import org.opengis.spatialschema.geometry.primitive.OrientableCurve;

/**
 * @author roehrig
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class OrientableCurveProxy extends OrientablePrimitiveProxy implements
		OrientableCurve {

	/**
	 * @param curve
	 */
	protected OrientableCurveProxy(CurveImpl curve) {
		super(curve);
	}

	private CurveImpl curve() {
		return (CurveImpl) getPrimitive();
	}

	/**
	 * @return
	 */
	public DirectPosition getStartPoint() {
		return this.curve().getEndPoint();
	}

	/**
	 * @return
	 */
	public DirectPosition getEndPoint() {
		return this.curve().getStartPoint();
	}

	/**
	 * @param distance
	 * @return
	 */
	public double[] getTangent(double distance) {
		// TODO Auto-generated method stub
		return null;
	}

	public double getStartParam() {
		return this.curve().getEndParam();
	}

	public double getEndParam() {
		return this.curve().getStartParam();
	}

	public Object[] paramForPoint(DirectPositionImpl p) {
		// TODO Auto-generated method stub
		return null;
	}

	public DirectPositionImpl param(double distance) {
		// TODO Auto-generated method stub
		return null;
	}

	public double startConstrParam() {
		return this.curve().getEndConstructiveParam();
	}

	public double endConstrParam() {
		return this.curve().getStartConstructiveParam();
	}

	public DirectPositionImpl constrParam(double cp) {
		// TODO Auto-generated method stub
		return null;
	}

	public double length(PositionImpl point1, PositionImpl point2) {
		return this.curve().length(point1, point2);
	}

	public double length(double par1, double par2) {
		return this.curve().length(par1, par2);
	}

	public double length() {
		return this.curve().length();
	}

	public LineStringImpl asLineString(double spacing, double offset) {
		return this.curve().asLineString(spacing, offset);
	}

	public LineStringImpl asLineString() {
		return this.curve().asLineString();
	}

	public CompositeCurve getComposite() {
		// TODO Auto-generated method stub
		return null;
	}

}
