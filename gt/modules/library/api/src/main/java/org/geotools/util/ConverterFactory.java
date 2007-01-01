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

import org.geotools.factory.Hints;


/**
 * Factory used to create instances of {@link Converter}.
 *
 * @author Justin Deoliveira, The Open Planning Project
 * 
 * @since 2.4
 *
 */
public interface ConverterFactory {
    /**
     * Creates a {@link Converter} instance for converting one type
     * of object to another.
     *
     * @param source The type to convert from.
     * @param target The type to convert to.
     * @param hints Hints used to be used while creating a converter.
     *
     * @return The converter, or <code>null</code> if one could not be found.
     */
    Converter createConverter(Class source, Class target, Hints hints);
}
