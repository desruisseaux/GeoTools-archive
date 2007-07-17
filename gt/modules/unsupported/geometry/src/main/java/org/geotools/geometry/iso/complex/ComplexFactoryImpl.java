/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006-2007, GeoTools Project Management Committee (PMC)
 *    (C) 2006       University of Applied Sciences Köln (Fachhochschule Köln)
 *    
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */

package org.geotools.geometry.iso.complex;

import java.util.List;

import org.geotools.geometry.iso.primitive.PointImpl;
import org.opengis.geometry.complex.ComplexFactory;
import org.opengis.geometry.complex.CompositeCurve;
import org.opengis.geometry.complex.CompositePoint;
import org.opengis.geometry.complex.CompositeSurface;
import org.opengis.geometry.primitive.OrientableCurve;
import org.opengis.geometry.primitive.OrientableSurface;
import org.opengis.geometry.primitive.Point;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class ComplexFactoryImpl implements ComplexFactory {

	//private FeatGeomFactoryImpl geometryFactory;
	private CoordinateReferenceSystem crs;

	/**
	 */
	public ComplexFactoryImpl(CoordinateReferenceSystem crs) {
		this.crs = crs;
	}
	
	public CompositePoint createCompositePoint(Point generator) {
		return new CompositePointImpl(crs, (PointImpl) generator);
	}
	
	public CompositeCurve createCompositeCurve(List<OrientableCurve> generator) {
		return new CompositeCurveImpl(generator);
	}

	public CompositeSurface createCompositeSurface(List<OrientableSurface> generator) {
		return new CompositeSurfaceImpl(generator);
	}

	
}
