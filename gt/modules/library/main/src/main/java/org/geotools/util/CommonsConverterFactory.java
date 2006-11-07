package org.geotools.util;

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
	class CommonsConverterWrapper implements Converter {

		org.apache.commons.beanutils.Converter delegate;
		
		public CommonsConverterWrapper( org.apache.commons.beanutils.Converter delegate ) {
			this.delegate = delegate;
		}
		
		public boolean canConvert(Class source, Class target) {
			return true;
		}

		public Object convert(Object source, Class target) throws Exception {
			return delegate.convert( target, source );
		}
		
	}
}
