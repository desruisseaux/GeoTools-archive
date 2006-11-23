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

package org.geotools.geometry.iso.aggregate;

import java.util.Set;

import org.geotools.geometry.iso.FeatGeomFactoryImpl;
import org.opengis.spatialschema.geometry.aggregate.AggregateFactory;
import org.opengis.spatialschema.geometry.aggregate.MultiCurve;
import org.opengis.spatialschema.geometry.aggregate.MultiPoint;
import org.opengis.spatialschema.geometry.aggregate.MultiPrimitive;
import org.opengis.spatialschema.geometry.aggregate.MultiSurface;
import org.opengis.spatialschema.geometry.primitive.OrientableCurve;
import org.opengis.spatialschema.geometry.primitive.OrientableSurface;
import org.opengis.spatialschema.geometry.primitive.Point;
import org.opengis.spatialschema.geometry.primitive.Primitive;

/**
 * 
 */
public class AggregateFactoryImpl implements AggregateFactory {

	private FeatGeomFactoryImpl geometryFactory;

	/**
	 * @param geometryFactory
	 */
	public AggregateFactoryImpl(FeatGeomFactoryImpl geometryFactory) {
		this.geometryFactory = geometryFactory;
	}
	
	/**
	 * Creates a MultiPrimitive by a set of Primitives.
	 * @param points Set of Points which shall be contained by the MultiPoint
	 * @return
	 */
	public MultiPrimitive createMultiPrimitive(Set<Primitive> primitives) {
		return new MultiPrimitiveImpl(this.geometryFactory, primitives);
	}

	/**
	 * Creates a MultiPoint by a set of Points.
	 * @param points Set of Points which shall be contained by the MultiPoint
	 * @return
	 */
	public MultiPoint createMultiPoint(Set<Point> points) {
		return new MultiPointImpl(this.geometryFactory, points);
	}

	
	/**
	 * Creates a MultiCurve by a set of Curves.
	 * @param points Set of Points which shall be contained by the MultiCurve
	 * @return
	 */
	public MultiCurve createMultiCurve(Set<OrientableCurve> curves) {
		return new MultiCurveImpl(this.geometryFactory, curves);
	}

	/**
	 * Creates a MultiSurface by a set of Surfaces.
	 * @param points Set of Points which shall be contained by the MultiSurface
	 * @return
	 */
	public MultiSurface createMultiSurface(Set<OrientableSurface> surfaces) {
		return new MultiSurfaceImpl(this.geometryFactory, surfaces);
	}

	
}
