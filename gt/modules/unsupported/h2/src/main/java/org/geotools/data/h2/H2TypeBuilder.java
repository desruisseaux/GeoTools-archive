package org.geotools.data.h2;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.HashMap;

import org.geotools.feature.simple.SimpleTypeBuilder;
import org.opengis.feature.simple.SimpleTypeFactory;

/**
 * Type Builder with which one can specify types as contants of {@link Types}.
 * 
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 */
public class H2TypeBuilder extends SimpleTypeBuilder {

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

		MAPPINGS.put(new Integer(Types.DATE), java.sql.Date.class);
		MAPPINGS.put(new Integer(Types.TIME), java.sql.Time.class);
		MAPPINGS.put(new Integer(Types.TIMESTAMP),
				java.sql.Timestamp.class);
	}
	
	/**
	 * Mappings from java class to sql tpe
	 */
	static HashMap RMAPPINGS = new HashMap();
	static {
		RMAPPINGS.put(String.class,new Integer(Types.VARCHAR));

		RMAPPINGS.put(Boolean.class,new Integer(Types.BOOLEAN));

		RMAPPINGS.put(Short.class,new Integer(Types.SMALLINT));

		RMAPPINGS.put(Integer.class,new Integer(Types.INTEGER));
		RMAPPINGS.put(Long.class,new Integer(Types.BIGINT));

		RMAPPINGS.put(Float.class,new Integer(Types.REAL));
		RMAPPINGS.put(Double.class,new Integer(Types.DOUBLE));

		RMAPPINGS.put(BigDecimal.class,new Integer(Types.NUMERIC));

		RMAPPINGS.put(java.sql.Date.class,new Integer(Types.DATE));
		RMAPPINGS.put(java.sql.Time.class,new Integer(Types.TIME));
		RMAPPINGS.put(java.sql.Timestamp.class,new Integer(Types.TIMESTAMP));
	}
	
	/**
	 * static accessor for looking up the java class mapped to a particular sql type.
	 */
	static Class mapping( int type ) {
		return (Class) MAPPINGS.get( new Integer( type ) );
	}
	
	/**
	 * static accessor for looking up the sql type mapped to a particular java class. 
	 */
	static int mapping( Class clazz ) {
		return ((Integer) RMAPPINGS.get( clazz )).intValue();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public H2TypeBuilder(SimpleTypeFactory factory) {
		super(factory);
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
		Class clazz = mapping( binding );
		if ( clazz == null ) {
			throw new IllegalArgumentException( "No class mapping for: " + binding );
		}
		
		return attribute( name, clazz );
	}

}
