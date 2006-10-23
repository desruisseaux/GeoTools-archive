package org.geotools.feature.simple;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import org.geotools.feature.AttributeType;
import org.geotools.feature.GeometryAttributeType;
import org.geotools.feature.type.SchemaImpl;
import org.geotools.feature.type.TypeFactoryImpl;
import org.geotools.feature.type.TypeName;

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
 * Schema containing a simple set of types for import into
 * the SimpleFeatureBuilder.
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
	private static SimpleTypeFactoryImpl factory = new SimpleTypeFactoryImpl();
	
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
    /** DATE to Data.class */
    public static final AttributeType DATE = factory.createAttributeType(
        new TypeName(NAMESPACE,"date"), Date.class, false,
        false,Collections.EMPTY_SET, (AttributeType) null, null
    );
    /**
     * DATETIME to Calendar.class.
     * <p>
     * Data and a Time like a timestamp.
     */    
    public static final AttributeType DATETIME = factory.createAttributeType(
        new TypeName(NAMESPACE,"datetime"), Calendar.class, false,
        false,Collections.EMPTY_SET, (AttributeType) null, null
    );
    
    //
    // Geomtries
    //
    /** Geometry to Geometry.class */
    public static final GeometryAttributeType GEOMETRY = factory.createGeometryType(
        new TypeName(NAMESPACE,"geometry"), Geometry.class, null, false, false, 
        Collections.EMPTY_SET, (AttributeType) null, null
    );
    /** POINT (extends GEOMETRY) binds to Point.class */    
    public static final GeometryAttributeType POINT = factory.createGeometryType(
        new TypeName(NAMESPACE,"point"), Point.class, null, false, false, 
        Collections.EMPTY_SET, GEOMETRY, null
    );
    /** LINESTRING (extends GEOMETRY) binds to LineString.class */        
    public static final GeometryAttributeType LINESTRING = factory.createGeometryType(
        new TypeName(NAMESPACE,"linestring"), LineString.class, null, false, 
        false, Collections.EMPTY_SET, GEOMETRY, null
    );
    /** LINEARRING (extends GEOMETRY) binds to LinearRing.class */            
    public static final GeometryAttributeType LINEARRING = factory.createGeometryType(
        new TypeName(NAMESPACE,"linearring"), LinearRing.class, null, false, 
        false, Collections.EMPTY_SET, LINESTRING, null
    );
    /**  POLYGON (extends GEOMETRY) binds to Polygon.class */            
    public static final GeometryAttributeType POLYGON = factory.createGeometryType(
        new TypeName(NAMESPACE,"polygon"), Polygon.class, null, false, 
        false, Collections.EMPTY_SET, GEOMETRY, null
    );
    /**  MULTIGEOMETRY (extends GEOMETRY) binds to GeometryCollection.class */                
    public static final GeometryAttributeType MULTIGEOMETRY = factory.createGeometryType(
        new TypeName(NAMESPACE,"multigeometry"), GeometryCollection.class, null,
        false, false, Collections.EMPTY_SET, GEOMETRY, null
    );
    
    /**  MULTIPOINT (extends MULTIGEOMETRY) binds to MultiPoint.class */            
    public static final GeometryAttributeType MULTIPOINT = factory.createGeometryType(
        new TypeName(NAMESPACE,"multipoint"), MultiPoint.class, null, false, false, 
        Collections.EMPTY_SET, MULTIGEOMETRY, null
    );
    
    /**  MULTILINESTRING (extends MULTIGEOMETRY) binds to MultiLineString.class */            
    public static final GeometryAttributeType MULTILINESTRING = factory.createGeometryType(
        new TypeName(NAMESPACE,"multilinestring"), MultiLineString.class, null, 
        false, false, Collections.EMPTY_SET, MULTIGEOMETRY, null
    );
    
    /** MULTIPOLYGON (extends MULTIGEOMETRY) binds to MultiPolygon.class */            
    public static final GeometryAttributeType MULTIPOLYGON = factory.createGeometryType(
        new TypeName(NAMESPACE,"multipolygon"), MultiPolygon.class, null, false, 
        false, Collections.EMPTY_SET, MULTIGEOMETRY, null
    );
    
    public SimpleSchema() {
        super(NAMESPACE);
        
        put(new TypeName(NAMESPACE,INTEGER.getName()),INTEGER);
        put(new TypeName(NAMESPACE,DOUBLE.getName()),DOUBLE);
        put(new TypeName(NAMESPACE,LONG.getName()),LONG);
        put(new TypeName(NAMESPACE,FLOAT.getName()),FLOAT);
        put(new TypeName(NAMESPACE,SHORT.getName()),SHORT);
        put(new TypeName(NAMESPACE,BYTE.getName()),BYTE);
        put(new TypeName(NAMESPACE,NUMBER.getName()),NUMBER);
        put(new TypeName(NAMESPACE,STRING.getName()),STRING);
        put(new TypeName(NAMESPACE,BOOLEAN.getName()),BOOLEAN);
        put(new TypeName(NAMESPACE,DATE.getName()),DATE);
        put(new TypeName(NAMESPACE,DATETIME.getName()),DATETIME);
        
        put(new TypeName(NAMESPACE,GEOMETRY.getName()),GEOMETRY);
        put(new TypeName(NAMESPACE,POINT.getName()),POINT);
        put(new TypeName(NAMESPACE,LINESTRING.getName()),LINESTRING);
        put(new TypeName(NAMESPACE,LINEARRING.getName()),LINEARRING);
        put(new TypeName(NAMESPACE,POLYGON.getName()),POLYGON);
        put(new TypeName(NAMESPACE,MULTIGEOMETRY.getName()),MULTIGEOMETRY);
        put(new TypeName(NAMESPACE,MULTIGEOMETRY.getName()),MULTIGEOMETRY);
        put(new TypeName(NAMESPACE,MULTIPOINT.getName()),MULTIPOINT);
        put(new TypeName(NAMESPACE,MULTILINESTRING.getName()),MULTILINESTRING);
        put(new TypeName(NAMESPACE,MULTIPOLYGON.getName()),MULTIPOLYGON);
        
    }

}
