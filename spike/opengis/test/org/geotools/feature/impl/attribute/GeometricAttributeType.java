/*
 *    uDig - User Friendly Desktop Internet GIS client
 *    http://udig.refractions.net
 *    (C) 2004, Refractions Research Inc.
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
 *
 */
package org.geotools.feature.impl.attribute;

import org.geotools.feature.impl.AttributeImpl;
import org.geotools.filter.Filter;
import org.geotools.referencing.crs.DefaultGeocentricCRS;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.GeometryType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.WKTReader;

/**
 * TODO: rename to GeometricAttribute
 * Provides ...TODO summary sentence
 * <p>
 * TODO Description
 * </p>
 * <p>
 * </p>
 * <p>
 * Example Use:
 * 
 * <pre><code>
 *     GeometryAttributeType x = new GeometryAttributeType( ... );
 *     TODO code example
 * </code></pre>
 * 
 * </p>
 * 
 * @author Leprosy
 * @since 0.3 TODO: test wkt geometry parse.
 */
public class GeometricAttributeType extends AttributeImpl implements
		GeometryAttribute {
	/** CoordianteSystem used by this GeometryAttributeType */
	protected CoordinateReferenceSystem coordinateSystem;

	/*
	 * protected GeometryFactory geometryFactory;
	 */

	public GeometricAttributeType(GeometryType type,
			CoordinateReferenceSystem cs, String id) {
		this(type, cs, null, id);
	}

	/**
	 * Relaxed constructor to support generic writing, checks that
	 * <code>type</code> is actually a <code>GeometryType</code>
	 * @param type
	 * @param cs
	 * @param content
	 * @param id
	 */
	public GeometricAttributeType(AttributeType type, Object content, String id) {
		super(id, type, content);
		if(!(type instanceof GeometryType)){
			throw new IllegalArgumentException("Expected GeometryType, got " + type);
		}
	}
	
	public GeometricAttributeType(GeometryType type,
			CoordinateReferenceSystem cs, Geometry content, String id) {
		super(id, type, content);
		coordinateSystem = cs;
		/*
		 * geometryFactory = cs == null ? CSGeometryFactory.DEFAULT : new
		 * CSGeometryFactory(cs);
		 */
		/*
		 * coordinateSystem = (cs != null) ? cs :
		 * LocalCoordinateSystem.CARTESIAN; geometryFactory = (cs ==
		 * LocalCoordinateSystem.CARTESIAN) ? CSGeometryFactory.DEFAULT : new
		 * CSGeometryFactory(cs);
		 */
	}

	/*
	 * public GeometricAttributeType(GeometricAttributeType copy,
	 * CoordinateReferenceSystem override) { super(copy); coordinateSystem =
	 * copy.getCoordinateSystem();
	 * 
	 * if (override != null) { coordinateSystem = override; }
	 * 
	 * if (coordinateSystem == null) { coordinateSystem =
	 * DefaultGeocentricCRS.CARTESIAN; } geometryFactory = (coordinateSystem ==
	 * DefaultGeocentricCRS.CARTESIAN) ? CSGeometryFactory.DEFAULT : new
	 * CSGeometryFactory(coordinateSystem); }
	 */

	public CoordinateReferenceSystem getCRS() {
		return coordinateSystem != null ? coordinateSystem
				: ((GeometryType) getType()).getCRS();
	}

	/*
	 * public GeometryFactory getGeometryFactory() { return geometryFactory; }
	 */

	protected Object parse(Object value) throws IllegalArgumentException {
		if (value == null) {
			return value;
		}

		if (value instanceof Geometry) {
			return value;
		}

		if (value instanceof String) {
			String wkt = (String) value;
			WKTReader reader = new WKTReader();
			try {
				return reader.read(wkt);
			} catch (com.vividsolutions.jts.io.ParseException pe) {
				throw new IllegalArgumentException("Could not parse the "
						+ "string: " + wkt + " to well known text");
			}
		}
		// consider wkb/gml support?
		throw new IllegalArgumentException(
				getClass().getName() + " cannot parse " + value);
	}

	public GeometryType getType() {
		return (GeometryType) super.getType();
	}

	public Geometry get() {
		return (Geometry) super.get();
	}

	public void set(Geometry geometry) {
		super.set(geometry);
	}

	/**
	 * Returns the non null envelope of this attribute. If the attribute's
	 * geometry is <code>null</code> the returned Envelope
	 * <code>isNull()</code> is true.
	 */
	public Envelope getBounds() {
		Envelope bounds = new Envelope();
		Geometry geom = get();
		if (geom != null) {
			bounds.expandToInclude(geom.getEnvelopeInternal());
		}
		return bounds;
	}
}
/**
 * Helper class used to force CS information on JTS Geometry
 */
/*
 * class CSGeometryFactory extends GeometryFactory {
 * 
 * static public GeometryFactory DEFAULT = new GeometryFactory(); static public
 * PrecisionModel DEFAULT_PRECISON_MODEL = new PrecisionModel();
 * 
 * public CSGeometryFactory(CoordinateReferenceSystem cs) {
 * super(toPrecisionModel(cs), toSRID(cs)); }
 * 
 * public GeometryCollection createGeometryCollection(Geometry[] geometries) {
 * GeometryCollection gc = super.createGeometryCollection(geometries); // JTS14
 * //gc.setUserData( cs ); return gc; }
 * 
 * public LinearRing createLinearRing(Coordinate[] coordinates) { LinearRing lr =
 * super.createLinearRing(coordinates); // JTS14 //gc.setUserData( cs ); return
 * lr; } // // And so on // Utility Functions private static int
 * toSRID(CoordinateReferenceSystem cs) { if ((cs == null) || (cs ==
 * DefaultGeocentricCRS.CARTESIAN)) { return 0; } // not sure how to tell SRID
 * from CoordinateSystem? return 0; }
 * 
 * private static PrecisionModel toPrecisionModel(CoordinateReferenceSystem cs) {
 * if ((cs == null) || (cs == DefaultGeocentricCRS.CARTESIAN)) { return
 * DEFAULT_PRECISON_MODEL; }
 * 
 * return DEFAULT_PRECISON_MODEL; } }
 */