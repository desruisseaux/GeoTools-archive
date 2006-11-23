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

package org.geotools.geometry.iso.complex;

import java.util.List;

import org.geotools.geometry.iso.FeatGeomFactoryImpl;
import org.geotools.geometry.iso.primitive.OrientableSurfaceImpl;
import org.geotools.geometry.iso.primitive.PointImpl;
import org.opengis.spatialschema.geometry.complex.ComplexFactory;
import org.opengis.spatialschema.geometry.complex.CompositeCurve;
import org.opengis.spatialschema.geometry.complex.CompositePoint;
import org.opengis.spatialschema.geometry.complex.CompositeSurface;
import org.opengis.spatialschema.geometry.primitive.OrientableCurve;
import org.opengis.spatialschema.geometry.primitive.OrientableSurface;
import org.opengis.spatialschema.geometry.primitive.Point;

public class ComplexFactoryImpl implements ComplexFactory {

	private FeatGeomFactoryImpl geometryFactory;

	/**
	 * @param geometryFactory
	 */
	public ComplexFactoryImpl(FeatGeomFactoryImpl geometryFactory) {
		this.geometryFactory = geometryFactory;
	}
	
	public CompositePoint createCompositePoint(Point generator) {
		return new CompositePointImpl(this.geometryFactory, (PointImpl) generator);
	}
	
	public CompositeCurve createCompositeCurve(List<OrientableCurve> generator) {
		return new CompositeCurveImpl(this.geometryFactory, (generator));
	}

	public CompositeSurface createCompositeSurface(List<OrientableSurface> generator) {
		return new CompositeSurfaceImpl(this.geometryFactory, (List<? extends OrientableSurfaceImpl>) generator);
	}

	
}
