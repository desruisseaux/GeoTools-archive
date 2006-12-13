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

import org.geotools.geometry.iso.coordinate.DirectPositionImpl;
import org.geotools.geometry.iso.coordinate.LineStringImpl;
import org.geotools.geometry.iso.coordinate.PositionImpl;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.spatialschema.geometry.geometry.ParamForPoint;
import org.opengis.spatialschema.geometry.geometry.Position;
import org.opengis.spatialschema.geometry.primitive.Curve;
import org.opengis.spatialschema.geometry.primitive.CurveSegment;

/**
 * @author roehrig
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class CurveProxy extends OrientableCurveProxy implements Curve {

	/**
	 * @param curve
	 */
	protected CurveProxy(CurveImpl curve) {
		super(curve);
	}

	private CurveImpl proxy() {
		return (CurveImpl) this.getPrimitive();
	}

	public DirectPosition getStartPoint() {
		return this.proxy().getEndPoint();
	}

	public DirectPosition getEndPoint() {
		return this.proxy().getStartPoint();
	}

	public double[] getTangent(double distance) {
		return (this.proxy().getTangent(this.proxy().getEndParam() - distance));
	}

	public double getStartParam() {
		return this.proxy().getEndParam();
	}

	public double getEndParam() {
		return this.proxy().getStartParam();
	}

	public ParamForPoint getParamForPoint(DirectPosition p) {
		// TODO Auto-generated method stub
		return null;
	}

	public DirectPositionImpl forParam(double distance) {
		// TODO Auto-generated method stub
		return null;
	}

	public double getStartConstructiveParam() {
		return 0.0;
	}

	public double getEndConstructiveParam() {
		return 1.0;
	}

	public DirectPositionImpl constrParam(double cp) {
		// TODO Auto-generated method stub
		return null;
	}

	public double length(PositionImpl point1, PositionImpl point2) {
		return this.proxy().length(point1, point2);
	}

	public double length(double par1, double par2) {
		return this.proxy().length(par1, par2);
	}

	public double length() {
		return this.proxy().length();
	}

	public LineStringImpl asLineString() {
		/* Return reversed LineString representation of mate */
		return (LineStringImpl) this.proxy().asLineString().reverse();
	}

	public LineStringImpl asLineString(double spacing, double offset) {
		/* Return reversed LineString representation of mate */
		return (LineStringImpl) this.proxy().asLineString(spacing, offset)
				.reverse();
	}

	public List<CurveSegment> getSegments() {

		// TODO Auto-generated method stub
		return null;
	}

	public DirectPosition forConstructiveParam(double cp) {
		// TODO Auto-generated method stub
		return null;
	}

	public double length(Position point1, Position point2) {
		// TODO Auto-generated method stub
		return 0;
	}
}
