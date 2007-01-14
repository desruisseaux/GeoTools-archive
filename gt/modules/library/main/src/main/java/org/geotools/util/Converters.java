/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.geotools.factory.FactoryRegistry;
import org.geotools.factory.Hints;

/**
 * Convenience class for converting an object from one type to an object of another.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 * @since 2.4
 */
public class Converters {

	/**
	 * Cached list of converter factories
	 */
	static List factories;
	
	/**
	 * Convenience for {@link #convert(Object, Class, Hints)}
	 * 
	 * @since 2.4
	 */
	public static Object convert( Object source, Class target ) {
		return convert( source, target, null );
	}
	
	/**
	 * Converts an object of a particular type into an object of a differnt type.
	 * <p>
	 * This method uses the {@link ConverterFactory} extension point to find a converter capable
	 * of performing the conversion. The first converter found is the one used. Using this class 
	 * there is no way to guarantee which converter will be used.
	 * </p>
	 * @param source The object to convert.
	 * @param target The type of the converted value.
	 * @param hints Any hints for the converter factory.
	 * 
	 * @return The converted value as an instnace of target, or <code>null</code> if a converter 
	 * could not be found.
	 * 
	 * @since 2.4
	 */
	public static Object convert( Object source, Class target, Hints hints ) {
		if ( source == null ) 
			return null;
		
		for ( Iterator i = factories().iterator(); i.hasNext(); ) {
			ConverterFactory factory = (ConverterFactory) i.next();
			Converter converter = factory.createConverter( source.getClass(), target, hints );
			if ( converter != null ) {
				try {
					Object converted = converter.convert( source, target );
					if ( converted != null ) {
						return converted;
					}
				} 
				catch (Exception e) {
					//TODO: perhaps log this
				}
			}
		}
		
		//a couple of final tries
		if ( String.class.equals( target ) ) {
			return source.toString();
		}
		return null;
	}
	
	/**
	 * Processed the {@link ConverterFactory} extension point.
	 * 
	 * @return A list of converter factories.
	 * @since 2.4 
	 */
	static List factories() {
		if ( factories == null ) {
			synchronized ( Converters.class ) {
				if ( factories == null ) {
					Iterator i = FactoryRegistry.lookupProviders( ConverterFactory.class );
					factories = new ArrayList();
					while( i.hasNext() ) factories.add( i.next() );
				}
			}
			
		}
		
		return factories;
	}
}
