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

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.FactoryCreator;
import org.geotools.factory.FactoryRegistry;
import org.geotools.factory.GeoTools;
import org.geotools.factory.Hints;
import org.geotools.resources.LazySet;

/**
 * Convenience class for converting an object from one type to an object of another.
 *
 * @author Justin Deoliveira, The Open Planning Project
 * @since 2.4
 */
public final class Converters {

	/**
	 * Cached list of converter factories
	 */
	static Collection factories;

    /**
     * The service registry for this manager.
     * Will be initialized only when first needed.
     */
    private static FactoryRegistry registry;

    /**
     * Returns the service registry. The registry will be created the first
     * time this method is invoked.
     */
    private static FactoryRegistry getServiceRegistry() {
        assert Thread.holdsLock(Converters.class);
        if (registry == null) {
            registry = new FactoryCreator(Arrays.asList(new Class<?>[] {
            		ConverterFactory.class,}));
        }
        return registry;
    }

    private static Hints addDefaultHints(final Hints hints) {
        final Hints completed = GeoTools.getDefaultHints();
        if (hints != null) {
            completed.add(hints);
        }
        return completed;
    }


    /**
     * Returns a set of all available implementations for the {@link ConverterFactory} interface.
     *
     * @param  hints An optional map of hints, or {@code null} if none.
     * @return Set of available ConverterFactory implementations.
     */
    public static synchronized Set getConverterFactories(Hints hints) {
        hints = addDefaultHints(hints);
        return new LazySet(getServiceRegistry().getServiceProviders(
                ConverterFactory.class, null, hints));
    }

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
		//cant convert null
                if ( source == null )
			return null;

                //handle case of source being a direct instance of target
                // up front
                if ( source.getClass().equals( target ) ) {
                    return source;
                }


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
	 * @return A collection of converter factories.
	 * @since 2.4
	 */
	static Collection factories() {
	    if(factories == null)
		factories = getConverterFactories(GeoTools.getDefaultHints());
	    return factories;
	}
}
