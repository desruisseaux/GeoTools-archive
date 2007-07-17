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

package org.geotools.geometry.iso.aggregate;

import java.util.Set;

import org.opengis.geometry.aggregate.AggregateFactory;
import org.opengis.geometry.aggregate.MultiCurve;
import org.opengis.geometry.aggregate.MultiPoint;
import org.opengis.geometry.aggregate.MultiPrimitive;
import org.opengis.geometry.aggregate.MultiSurface;
import org.opengis.geometry.primitive.OrientableCurve;
import org.opengis.geometry.primitive.OrientableSurface;
import org.opengis.geometry.primitive.Point;
import org.opengis.geometry.primitive.Primitive;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * 
 */
public class AggregateFactoryImpl implements AggregateFactory {

	//private FeatGeomFactoryImpl geometryFactory;
	private CoordinateReferenceSystem crs;

	/**
	 * @param crs
	 */
	public AggregateFactoryImpl(CoordinateReferenceSystem crs) {
		this.crs = crs;
	}
	
	/**
	 * Creates a MultiPrimitive by a set of Primitives.
	 * @param points Set of Points which shall be contained by the MultiPoint
	 * @return
	 */
	public MultiPrimitive createMultiPrimitive(Set<Primitive> primitives) {
		return new MultiPrimitiveImpl(crs, primitives);
	}

	/**
	 * Creates a MultiPoint by a set of Points.
	 * @param points Set of Points which shall be contained by the MultiPoint
	 * @return
	 */
	public MultiPoint createMultiPoint(Set<Point> points) {
		return new MultiPointImpl(crs, points);
	}

	
	/**
	 * Creates a MultiCurve by a set of Curves.
	 * @param points Set of Points which shall be contained by the MultiCurve
	 * @return
	 */
	public MultiCurve createMultiCurve(Set<OrientableCurve> curves) {
		return new MultiCurveImpl(crs, curves);
	}

	/**
	 * Creates a MultiSurface by a set of Surfaces.
	 * @param points Set of Points which shall be contained by the MultiSurface
	 * @return
	 */
	public MultiSurface createMultiSurface(Set<OrientableSurface> surfaces) {
		return new MultiSurfaceImpl(crs, surfaces);
	}

	
}
