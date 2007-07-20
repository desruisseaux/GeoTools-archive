package org.geotools.feature.simple;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;


import org.geotools.feature.type.SchemaImpl;
import org.geotools.feature.type.TypeFactoryImpl;
import org.geotools.feature.type.TypeName;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.TypeFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Schema containing a set of "simple" types. 
 * <p>
 * These types represent a good choice for default java bindings, for data
 * sources that do not have specific or complicated needs. As such these
 * types are made available as static final constants to be inlined in code
 * where needed.
 * </p>
 * When would you not use this class?
 * <ul>
 * <li><b>For specific mappings:</b> Create a custom Schema when working with GML or where specific XML Schema
 *    mappings are useful to track.
 * <li><b>For restricted basic types:</b> Create a custom Schema when working with a Data Source that has different
 *    needs for "basic" types. Shapefile for example needs a length restriction
 *    on its Text type and cannot make use of STRING as provided here.
 * </ul>
 * 
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 */
public class SimpleSchema extends SchemaImpl {
   
	//internal factory
	private static TypeFactory factory = new TypeFactoryImpl();
	
	/** simple namespace */
    public static final String NAMESPACE = "http://www.geotools.org/simple";
   
    //
    // Builtin Java Types
    //
    /** BOOLEAN to Boolean.class */        
    public static final AttributeType BOOLEAN = factory.createAttributeType(
		new TypeName(NAMESPACE,"boolean"), Boolean.class, false, false,
		Collections.EMPTY_SET, (AttributeType) null, null 
	);
    /** STRING to String.class */ 
    public static final AttributeType STRING = factory.createAttributeType(
        new TypeName(NAMESPACE,"string"), String.class, false,
        false,Collections.EMPTY_SET, (AttributeType) null, null
    );
    //
    // Numerics
    //
    /** NUMBER to Number.class */    
    public static final AttributeType NUMBER = factory.createAttributeType(
        new TypeName(NAMESPACE,"number"), Number.class, false,
        false,Collections.EMPTY_SET, (AttributeType) null, null
    );
    /**
     * INTEGER to java Integer.class
     */    
    public static final AttributeType INTEGER = factory.createAttributeType(
        new TypeName(NAMESPACE,"integer"), Integer.class, false,
        false,Collections.EMPTY_SET,NUMBER, null
    );
    /**
     * FLOAT to java Float.class
     */      
    public static final AttributeType FLOAT = factory.createAttributeType(
        new TypeName(NAMESPACE,"float"), Float.class, false,
        false,Collections.EMPTY_SET,NUMBER, null
    );
    /** DOUBLE to Double.class */
    public static final AttributeType DOUBLE = factory.createAttributeType(
        new TypeName(NAMESPACE,"double"), Double.class, false,
        false,Collections.EMPTY_SET,NUMBER, null
    );
    /** LONG to Long.class */
    public static final AttributeType LONG = factory.createAttributeType(
        new TypeName(NAMESPACE,"long"), Long.class, false,
        false,Collections.EMPTY_SET,NUMBER, null
    );
    /** SHORT to Short.class */
    public static final AttributeType SHORT = factory.createAttributeType(
        new TypeName(NAMESPACE,"short"), Short.class, false,
        false,Collections.EMPTY_SET,NUMBER, null
    );
    /** BYTE to Byte.class */
    public static final AttributeType BYTE = factory.createAttributeType(
        new TypeName(NAMESPACE,"byte"), Byte.class, false,
        false,Collections.EMPTY_SET,NUMBER, null
    );

    //
    // TEMPORAL
    //
    /** DATE to Date.class */
    public static final AttributeType DATE = factory.createAttributeType(
        new TypeName(NAMESPACE,"date"), Date.class, false,
        false,Collections.EMPTY_SET, (AttributeType) null, null
    );
    /** TIME to Time.class */
    public static final AttributeType TIME = factory.createAttributeType(
        new TypeName(NAMESPACE,"time"), Time.class, false,
        false,Collections.EMPTY_SET, (AttributeType) null, null
    );
    /**
     * DATETIME to Calendar.class.
     * <p>
     * Data and a Time like a timestamp.
     */    
    public static final AttributeType DATETIME = factory.createAttributeType(
        new TypeName(NAMESPACE,"datetime"), Timestamp.class, false,
        false,Collections.EMPTY_SET, (AttributeType) null, null
    );
    
    //
    // Geomtries
    //
    /** Geometry to Geometry.class */
    public static final GeometryType GEOMETRY = factory.createGeometryType(
        new TypeName(NAMESPACE,"geometry"), Geometry.class, null, false, false, 
        Collections.EMPTY_SET, (AttributeType) null, null
    );
    /** POINT (extends GEOMETRY) binds to Point.class */    
    public static final GeometryType POINT = factory.createGeometryType(
        new TypeName(NAMESPACE,"point"), Point.class, null, false, false, 
        Collections.EMPTY_SET, GEOMETRY, null
    );
    /** LINESTRING (extends GEOMETRY) binds to LineString.class */        
    public static final GeometryType LINESTRING = factory.createGeometryType(
        new TypeName(NAMESPACE,"linestring"), LineString.class, null, false, 
        false, Collections.EMPTY_SET, GEOMETRY, null
    );
    /** LINEARRING (extends GEOMETRY) binds to LinearRing.class */            
    public static final GeometryType LINEARRING = factory.createGeometryType(
        new TypeName(NAMESPACE,"linearring"), LinearRing.class, null, false, 
        false, Collections.EMPTY_SET, LINESTRING, null
    );
    /**  POLYGON (extends GEOMETRY) binds to Polygon.class */            
    public static final GeometryType POLYGON = factory.createGeometryType(
        new TypeName(NAMESPACE,"polygon"), Polygon.class, null, false, 
        false, Collections.EMPTY_SET, GEOMETRY, null
    );
    /**  MULTIGEOMETRY (extends GEOMETRY) binds to GeometryCollection.class */                
    public static final GeometryType MULTIGEOMETRY = factory.createGeometryType(
        new TypeName(NAMESPACE,"multigeometry"), GeometryCollection.class, null,
        false, false, Collections.EMPTY_SET, GEOMETRY, null
    );
    
    /**  MULTIPOINT (extends MULTIGEOMETRY) binds to MultiPoint.class */            
    public static final GeometryType MULTIPOINT = factory.createGeometryType(
        new TypeName(NAMESPACE,"multipoint"), MultiPoint.class, null, false, false, 
        Collections.EMPTY_SET, MULTIGEOMETRY, null
    );
    
    /**  MULTILINESTRING (extends MULTIGEOMETRY) binds to MultiLineString.class */            
    public static final GeometryType MULTILINESTRING = factory.createGeometryType(
        new TypeName(NAMESPACE,"multilinestring"), MultiLineString.class, null, 
        false, false, Collections.EMPTY_SET, MULTIGEOMETRY, null
    );
    
    /** MULTIPOLYGON (extends MULTIGEOMETRY) binds to MultiPolygon.class */            
    public static final GeometryType MULTIPOLYGON = factory.createGeometryType(
        new TypeName(NAMESPACE,"multipolygon"), MultiPolygon.class, null, false, 
        false, Collections.EMPTY_SET, MULTIGEOMETRY, null
    );
    
    public SimpleSchema() {
        super(NAMESPACE);
        
        put(INTEGER.getName(),INTEGER);
        put(DOUBLE.getName(),DOUBLE);
        put(LONG.getName(),LONG);
        put(FLOAT.getName(),FLOAT);
        put(SHORT.getName(),SHORT);
        put(BYTE.getName(),BYTE);
        put(NUMBER.getName(),NUMBER);
        put(STRING.getName(),STRING);
        put(BOOLEAN.getName(),BOOLEAN);
        put(DATE.getName(),DATE);
        put(DATETIME.getName(),DATETIME);
        
        put(GEOMETRY.getName(),GEOMETRY);
        put(POINT.getName(),POINT);
        put(LINESTRING.getName(),LINESTRING);
        put(LINEARRING.getName(),LINEARRING);
        put(POLYGON.getName(),POLYGON);
        put(MULTIGEOMETRY.getName(),MULTIGEOMETRY);
        put(MULTIGEOMETRY.getName(),MULTIGEOMETRY);
        put(MULTIPOINT.getName(),MULTIPOINT);
        put(MULTILINESTRING.getName(),MULTILINESTRING);
        put(MULTIPOLYGON.getName(),MULTIPOLYGON);
        
    }

}
