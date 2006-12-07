package org.geotools.util;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.beanutils.ConvertUtils;
import org.geotools.factory.Hints;

/**
 * ConverterFactory based on the apache commons {@link org.apache.commons.beanutils.Converter}
 * interface.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class CommonsConverterFactory implements ConverterFactory {
	
	//some additional converters 
	static org.apache.commons.beanutils.Converter uri = new org.apache.commons.beanutils.Converter() {
		public Object convert( Class target, Object value ) {
			String string = (String) value;
			try {
				return new URI( string );
			} 
			catch (URISyntaxException e) { } 
		
			return null;
		}
	};
	static {
		ConvertUtils.register( uri, URI.class );
	}
	
	
	/**
	 * Delegates to {@link ConvertUtils#lookup(java.lang.Class)} to create a 
	 * converter instance.
	 * 
	 * @see ConverterFactory#createConverter(Class, Class, Hints). 
	 */
	public Converter createConverter(Class source, Class target, Hints hints) {
		//only do strings
		if ( source.equals( String.class ) ) {
			org.apache.commons.beanutils.Converter converter = ConvertUtils.lookup( target );
			if ( converter != null ) {
				return new CommonsConverterWrapper( converter );
			}
		}
		
		return null;
	}

	/**
	 * Decorates a beanutils converter in a geotools converter.
	 * 
	 * @author Justin Deoliveira, The Open Planning Project
	 *
	 */
	static class CommonsConverterWrapper implements Converter {

		org.apache.commons.beanutils.Converter delegate;
		
		public CommonsConverterWrapper( org.apache.commons.beanutils.Converter delegate ) {
			this.delegate = delegate;
		}
		
		public Object convert(Object source, Class target) throws Exception {
			return delegate.convert( target, source );
		}
		
	}
	
	
}
