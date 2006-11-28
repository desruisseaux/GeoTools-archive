package org.geotools.xml;

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
			
			if ( Date.class.isAssignableFrom( target ) ) {
				return DatatypeConverter.parseDate( value ).getTime();
			}
			
			if ( Calendar.class.isAssignableFrom( target ) ) {
				return DatatypeConverter.parseDateTime( value );
			}
			
			return null;
		}
		
	}

}
