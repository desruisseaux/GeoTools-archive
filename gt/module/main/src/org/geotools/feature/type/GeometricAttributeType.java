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
package org.geotools.feature.type;

import org.geotools.feature.DefaultAttributeType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.filter.Filter;
import org.geotools.referencing.crs.GeocentricCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.WKTReader;

/**
 * Provides ...TODO summary sentence
 * <p>
 * TODO Description
 * </p><p>
 * </p><p>
 * Example Use:<pre><code>
 * GeometryAttributeType x = new GeometryAttributeType( ... );
 * TODO code example
 * </code></pre>
 * </p>
 * @author Leprosy
 * @since 0.3
 * TODO: test wkt geometry parse.
 */
public class GeometricAttributeType extends DefaultAttributeType implements org.geotools.feature.GeometryAttributeType{
    /** CoordianteSystem used by this GeometryAttributeType */
    protected CoordinateReferenceSystem coordinateSystem;
    protected GeometryFactory geometryFactory;
    private Filter filter;

    public GeometricAttributeType(String name, Class type, boolean nillable, int min, int max,
        Object defaultValue, CoordinateReferenceSystem cs, Filter filter) {
        super(name, type, nillable,min,max, defaultValue);
        this.filter = filter;
        coordinateSystem = cs;
        geometryFactory = cs == null ? CSGeometryFactory.DEFAULT : new CSGeometryFactory(cs);
        
        /*
        coordinateSystem = (cs != null) ? cs : LocalCoordinateSystem.CARTESIAN;
        geometryFactory = (cs == LocalCoordinateSystem.CARTESIAN)
            ? CSGeometryFactory.DEFAULT : new CSGeometryFactory(cs);
         */
    }

   public GeometricAttributeType(String name, Class type, boolean nillable,
        Object defaultValue, CoordinateReferenceSystem cs,Filter filter) {
        this(name, type, nillable,1,1, defaultValue, cs,filter);
   }

    public GeometricAttributeType(GeometricAttributeType copy, CoordinateReferenceSystem override) {
        super(copy);
        coordinateSystem = copy.getCoordinateSystem();

        if (override != null) {
            coordinateSystem = override;
        }

        if (coordinateSystem == null) {
            coordinateSystem = GeocentricCRS.CARTESIAN;
        }
        geometryFactory = (coordinateSystem == GeocentricCRS.CARTESIAN)
            ? CSGeometryFactory.DEFAULT : new CSGeometryFactory(coordinateSystem);            
    }

    public CoordinateReferenceSystem getCoordinateSystem() {
        return coordinateSystem;
    }

    public GeometryFactory getGeometryFactory() {
        return geometryFactory;
    }

    public Object parse(Object value) throws IllegalArgumentException {
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
		throw new IllegalArgumentException("Could not parse the " + 
						   "string: " + wkt + 
						   " to well known text");
	    }
	}
        // consider wkb/gml support?
        throw new IllegalArgumentException(
            "AttributeGT.Geometric cannot parse " + value);
    }
    
    public Object duplicate(Object o) throws IllegalAttributeException {
        if (o == null)
            return o;
        if (o instanceof Geometry) {
            return ((Geometry)o).clone();
        }
        throw new IllegalAttributeException("Cannot duplicate " + o.getClass().getName());
    }

    /* (non-Javadoc)
     * @see org.geotools.feature.PrimativeAttributeType#getRestriction()
     */
    public Filter getRestriction() {
        return filter;
    }
}
/**
 * Helper class used to force CS information on JTS Geometry
 */
class CSGeometryFactory extends GeometryFactory {
    
    static public GeometryFactory DEFAULT = new GeometryFactory();    
    static public PrecisionModel DEFAULT_PRECISON_MODEL = new PrecisionModel();

    public CSGeometryFactory(CoordinateReferenceSystem cs) {
        super(toPrecisionModel(cs), toSRID(cs));
    }

    public GeometryCollection createGeometryCollection(Geometry[] geometries) {
        GeometryCollection gc = super.createGeometryCollection(geometries);

        // JTS14
        //gc.setUserData( cs );
        return gc;
    }

    public LinearRing createLinearRing(Coordinate[] coordinates) {
        LinearRing lr = super.createLinearRing(coordinates);

        // JTS14
        //gc.setUserData( cs );
        return lr;
    }

    //
    // And so on
    // Utility Functions
    private static int toSRID(CoordinateReferenceSystem cs) {
        if ((cs == null) || (cs == GeocentricCRS.CARTESIAN)) {
            return 0;
        }

        // not sure how to tell SRID from CoordinateSystem?
        return 0;
    }

    private static PrecisionModel toPrecisionModel(CoordinateReferenceSystem cs) {
        if ((cs == null) || (cs == GeocentricCRS.CARTESIAN)) {
            return DEFAULT_PRECISON_MODEL;
        }

        return DEFAULT_PRECISON_MODEL;
    }
}

