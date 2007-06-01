package org.geotools.data.jdbc;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.HashMap;

import org.geotools.feature.simple.SimpleTypeBuilder;
import org.opengis.feature.simple.SimpleTypeFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Type Builder with which one can specify sql types as contants from the
 *  {@link Types} class.
 *
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 */
public class JDBCTypeBuilder extends SimpleTypeBuilder {

    /**
     * Mappings from sql type, to java class
     */
    static HashMap MAPPINGS = new HashMap();

    static {
        MAPPINGS.put(new Integer(Types.VARCHAR), String.class);
        MAPPINGS.put(new Integer(Types.CHAR), String.class);
        MAPPINGS.put(new Integer(Types.LONGVARCHAR), String.class);

        MAPPINGS.put(new Integer(Types.BIT), Boolean.class);
        MAPPINGS.put(new Integer(Types.BOOLEAN), Boolean.class);

        MAPPINGS.put(new Integer(Types.TINYINT), Short.class);
        MAPPINGS.put(new Integer(Types.SMALLINT), Short.class);

        MAPPINGS.put(new Integer(Types.INTEGER), Integer.class);
        MAPPINGS.put(new Integer(Types.BIGINT), Long.class);

        MAPPINGS.put(new Integer(Types.REAL), Float.class);
        MAPPINGS.put(new Integer(Types.FLOAT), Double.class);
        MAPPINGS.put(new Integer(Types.DOUBLE), Double.class);

        MAPPINGS.put(new Integer(Types.DECIMAL), BigDecimal.class);
        MAPPINGS.put(new Integer(Types.NUMERIC), BigDecimal.class);

        MAPPINGS.put(new Integer(Types.DATE), Date.class);
        MAPPINGS.put(new Integer(Types.TIME), Time.class);
        MAPPINGS.put(new Integer(Types.TIMESTAMP), Timestamp.class);

        MAPPINGS.put(new Integer(Types.OTHER), Geometry.class);
    }

    /**
     * Mappings from java class to sql tpe
     */
    static HashMap RMAPPINGS = new HashMap();

    static {
        RMAPPINGS.put(String.class, new Integer(Types.VARCHAR));

        RMAPPINGS.put(Boolean.class, new Integer(Types.BOOLEAN));

        RMAPPINGS.put(Short.class, new Integer(Types.SMALLINT));

        RMAPPINGS.put(Integer.class, new Integer(Types.INTEGER));
        RMAPPINGS.put(Long.class, new Integer(Types.BIGINT));

        RMAPPINGS.put(Float.class, new Integer(Types.REAL));
        RMAPPINGS.put(Double.class, new Integer(Types.DOUBLE));

        RMAPPINGS.put(BigDecimal.class, new Integer(Types.NUMERIC));

        RMAPPINGS.put(Date.class, new Integer(Types.DATE));
        RMAPPINGS.put(Time.class, new Integer(Types.TIME));
        RMAPPINGS.put(Timestamp.class, new Integer(Types.TIMESTAMP));

        RMAPPINGS.put(Geometry.class, new Integer(Types.OTHER));
        RMAPPINGS.put(Point.class, new Integer(Types.OTHER));
        RMAPPINGS.put(LineString.class, new Integer(Types.OTHER));
        RMAPPINGS.put(Polygon.class, new Integer(Types.OTHER));
        
    }

    /**
     * {@inheritDoc}
     */
    public JDBCTypeBuilder(SimpleTypeFactory factory) {
        super(factory);
    }

    /**
     * Looks up the java class mapped to a particular sql type.
     * 
     * @throws IllegalArgumentException When there is no mapping for <tt>type</tt>
     */
    public Class mapping(int type) throws IllegalArgumentException {
    	Class mapping = (Class) MAPPINGS.get(new Integer(type));
    	if ( mapping == null ) {
    		throw new IllegalArgumentException( "No such mapping for type: " + type );
    	}
    	
    	return mapping;
    }

    /**
     * Looks up the sql type mapped to a particular java class.
     * 
     * @throws IllegalArgumentException When there is no mapping for <tt>clazz</tt>
     */
    public int mapping(Class clazz) throws IllegalArgumentException {
    	Integer mapping = (Integer) RMAPPINGS.get(clazz);
    	if ( mapping == null ) {
    		throw new IllegalArgumentException( "No such mapping for class: " + clazz.getName() );
    	}
        return mapping.intValue();
    }

    /**
     * Adds an attribute to the type being built from a name, java.sql.Types
     * constance pair.
     *
     * @param name The name of the attribute.
     * @param binding The type of the attribute, a constant in {@link java.sql.Types}.
     *
     * @return This builder.
     */
    public SimpleTypeBuilder attribute(String name, int binding) {
        //turn the constant into a class
        Class clazz = mapping(binding);

        if (clazz == null) {
            throw new IllegalArgumentException("No class mapping for: " + binding);
        }

        return attribute(name, clazz);
    }

}
