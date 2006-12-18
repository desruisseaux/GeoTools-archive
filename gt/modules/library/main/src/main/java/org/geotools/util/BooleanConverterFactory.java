package org.geotools.util;

import org.geotools.factory.Hints;

public class BooleanConverterFactory implements ConverterFactory {

	public Converter createConverter(Class source, Class target, Hints hints) {
		if ( target.equals( Boolean.class ) ) {
			
			//string to boolean
			if ( source.equals( String.class ) ) {
				return new Converter() {

					public Object convert(Object source, Class target) throws Exception {
						if ( "true".equals( source ) || "1".equals( source ) ) {
							return Boolean.TRUE;
						}
						if ( "false".equals( source ) || "0".equals( source ) ) {
							return Boolean.FALSE;
						}
						
						return null;
					}
					
				};
			}
			
			//integer to boolean
			if ( source.equals( Integer.class ) ) {
				return new Converter() {

					public Object convert(Object source, Class target) throws Exception {
						if ( new Integer( 1 ).equals( source ) ) {
							return Boolean.TRUE;
						}
						if ( new Integer( 0 ).equals( source ) ) {
							return Boolean.FALSE;
						}
						
						return null;
					}
					
				};
			}
			
		}
		
		return null;
	}

}
