package org.geotools.util;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.geotools.factory.Hints;

/**
 * ConverterFactory which converts between the "standard" numeric types.
 * <p>
 * 	Supported types:
 * <ul>
 * <li>{@link java.lang.Long}
 * <li>{@link java.lang.Integer}
 * <li>{@link java.lang.Short}
 * <li>{@link java.lang.Byte}
 * <li>{@link java.lang.BigInteger}
 * <li>{@link java.lang.Double}
 * <li>{@link java.lang.Float}
 * <li>{@link java.lang.BigDecimal}
 * </ul>
 * </p>
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class NumericConverterFactory implements ConverterFactory {

	public Converter createConverter(Class source, Class target, Hints hints) {
	
		//check if source is a number
		if ( !( Number.class.isAssignableFrom( source ) ) ) 
			return null;
		
		//check if target is one of supported
		if ( 
			Long.class.equals( target ) || 
			Integer.class.equals( target ) || 
			Short.class.equals( target ) || 
			Byte.class.equals( target ) || 
			BigInteger.class.equals( target ) || 
			BigDecimal.class.equals( target ) || 
			Double.class.equals( target ) || 
			Float.class.equals( target )
		) {
			return new NumericConverter();
		}
		
		return null;
	}
	
	class NumericConverter implements Converter {

		public Object convert(Object source, Class target) throws Exception {
			Number s = (Number) source;
			
			//integral
			if ( Long.class.equals( target ) ) {
				return new Long( s.longValue() );
			}
			if ( Integer.class.equals( target ) ) {
				return new Integer( s.intValue() );
			}
			if ( Short.class.equals( target ) ) {
				return new Short( s.shortValue() );
			}
			if ( Byte.class.equals( target ) ) {
				return new Byte( s.byteValue() );
			}
			if ( BigInteger.class.equals( target ) ) {
				return BigInteger.valueOf( s.longValue() );
			}
		
			//floating point
			if ( Double.class.equals( target ) ) {
				return new Double( s.doubleValue() );
			}
			if ( Float.class.equals( target ) ) {
				return new Float( s.floatValue() );
			}
			if ( BigDecimal.class.equals( target ) ) {
				return new BigDecimal( s.doubleValue() );
			}
			
			return null;
		}
	}

}
