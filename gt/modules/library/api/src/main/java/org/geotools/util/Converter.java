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


/**
 * Converts values of one type into another.
 *
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public interface Converter {
    /**
     * Determines if this converter can convert instances of one type into another.
     *
     * @param source The type being converted from.
     * @param terget The type being converted to.
     *
     * @return <code>true</code> if the conversion can take place with the givem arguments,
     * otherwise <code>false</code>.
     */
    boolean canConvert(Class source, Class target);

    /**
     * Converts an object to an object of another type.
     * <p>
     * This method should not be called unless <code>canConvert( object.getClass(), target )<code>
     * returns <code>true</code>.
     * </p>
     *
     * @param source The original object, never <code>null</code>
     * @param target The type of the converted object.
     *
     * @return An instance of target, or <code>null</code> if the conversion could not take place.
     */
    Object convert(Object source, Class target) throws Exception;
}
