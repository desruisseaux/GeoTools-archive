package org.geotools.xml;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import javax.xml.bind.DatatypeConverter;

import org.geotools.factory.Hints;
import org.geotools.util.Converter;
import org.geotools.util.ConverterFactory;

import com.sun.xml.bind.DatatypeConverterImpl;

/**
 * A ConverterFactory which can convert strings using {@link javax.xml.datatype.DatatypeFactory}.
 * <p>
 * Supported converstions:
 * <ul>
 * 	<li>String to {@link java.util.Date}
 * 	<li>String to {@link java.util.Calendar}
 * </ul>
 * </p>
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class XmlConverterFactory implements ConverterFactory {

	static {
		DatatypeConverter.setDatatypeConverter(DatatypeConverterImpl.theInstance);
	}
	
	public Converter createConverter(Class source, Class target, Hints hints) {
		if ( String.class.equals( source ) ) {
			return new XmlConverter();
		}
		
		return null;
	}
	
	static class XmlConverter implements Converter {

		public Object convert(Object source, Class target) throws Exception {
			String value = (String)source;
			
			Calendar date;
			//try parsing as dateTime
			try {
				date = DatatypeConverter.parseDateTime( value );	
			}
			catch( Exception e ) {
				//try as just date
				date = DatatypeConverter.parseDate( value );
			}
			
			
			if ( Calendar.class.equals( target ) ) {
				return date;
			}
			
			if ( Date.class.isAssignableFrom( target ) ) {
				Date time = date.getTime();
				
				//check for subclasses
				if ( java.sql.Date.class.equals( target ) )  {
					return new java.sql.Date( time.getTime() );
				}
				if ( Time.class.equals( target ) ) {
					return new Time( time.getTime() );
				}
				if ( Timestamp.class.equals( target) ) {
					return new Timestamp( time.getTime() );
				}
				
				return time;
			}
		
			return null;
		}
		
	}

}
