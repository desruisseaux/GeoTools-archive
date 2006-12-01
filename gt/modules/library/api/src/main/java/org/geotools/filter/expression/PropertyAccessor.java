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
package org.geotools.filter.expression;

import org.geotools.feature.IllegalAttributeException;


/**
 * Used to get and set object properties based on an xpath expression.
 *
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public interface PropertyAccessor {
    /**
     * Determines if the property accessor can handle the property denoted by <param>xpath</param>.
     * <p>
     * Can be used to perform checks against schema to ensure that the propery accessor
     * will actually work with the provided instance.
     * </p>
     *
     * @param object The target object.
     * @param xpath An xpath expression denoting a property of the target object.
     *
     * @return <code>true</code> if the property can be accessed, otherwise <code>false</code>
     */
    boolean canHandle(Object object, String xpath, Class target);

    /**
     * Accesses a property of <param>object</param> via xpath expression.
     * <p>
     * {@link #canHandle(Object, String)} should be called before calling this method to ensure
     * that the property can be safely accessed.
     * </p>
     * @param object The target object.
     * @param xpath An xpath expression denoting a property of the target object.
     * @param target Target context we intend to access (often null or Geometry.class)
     * @return The property, which might be <code>null</code>
     *
     */
    Object get(Object object, String xpath, Class target);

    /**
     * Sets a property of <param>object</param> via xpath expression.
     * <p>
     * {@link #canHandle(Object, String)} should be called before calling this method to ensure
     * that the property can be safely accessed.
     * </p>
     * @param object The target object.
     * @param xpath An xpath expression denoting a property of the target object.
     * @param value The new value to set
     * @param target The target context we intend to update (often null or Geometry.class)
     * @throws IllegalAttributeException If the value set is not legal for the target object.
     */
    void set(Object object, String xpath, Object value, Class target)
        throws IllegalAttributeException;
}
