package org.geotools.util;

import java.util.Calendar;
import java.util.Date;

import org.geotools.factory.Hints;

/**
 * Converter factory which created converting between the various temporal types.
 * <p>
 * Supported converstions:
 * <ul>
 * 	<li>{@link java.util.Date} to {@link Calendar}
 *  <li>{@link java.util.Calendar} to {@link java.util.Date}
 * 	
 * </ul>
 * </p>
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class TemporalConverterFactory implements ConverterFactory {

	public Converter createConverter(Class source, Class target, Hints hints) {
		if ( Date.class.isAssignableFrom( source ) ) {
			if ( Calendar.class.isAssignableFrom( target ) ) {
				return new Converter() {
					public Object convert(Object source, Class target) throws Exception {
						Calendar calendar = Calendar.getInstance();
						calendar.setTime( (Date) source );
						
						return calendar;
					}
				};
			}
		}
		
		if ( Calendar.class.isAssignableFrom( source ) ) {
			if ( Date.class.isAssignableFrom( target ) ) {
				return new Converter() {
					public Object convert(Object source, Class target) throws Exception {
						Calendar calendar = (Calendar) source;
						return calendar.getTime();
					}
				};
			}
		}
		
		return null;
	}

}
