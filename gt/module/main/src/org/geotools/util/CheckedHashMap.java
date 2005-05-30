/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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

// J2SE dependencies
import java.util.LinkedHashMap;

import org.geotools.resources.Utilities;
import org.geotools.resources.cts.ResourceKeys;
import org.geotools.resources.cts.Resources;
import org.opengis.util.Cloneable;


/**
 * Acts as a typed {@link java.util.Map} while we wait for Java 5.0.
 * 
 * @version $Id$
 * @author Jody Garnett (Refractions Research)
 * @author Martin Desruisseaux
 *
 * @since 2.1
 *
 * @todo Provides synchronization facility on arbitrary lock, for use with the metadata package.
 *       The lock would be the metadata that owns this collection. Be carefull to update the lock
 *       after a clone (this work my be done in {@code MetadataEntity.unmodifiable(Object)}).
 */
public class CheckedHashMap extends LinkedHashMap implements Cloneable {
    /**
     * Serial version UID for compatibility with different versions.
     */
    private static final long serialVersionUID = -7777695267921872849L;

    /**
     * The class type for keys.
     */
    private final Class keyType;

    /**
     * The class type for values.
     */
    private final Class valueType;

    /**
     * Constructs a map of the specified type.
     *
     * @param keyType   The key type (should not be null).
     * @param valueType The value type (should not be null).
     */
    public CheckedHashMap(final Class keyType, final Class valueType) {
        this.keyType   = keyType;
        this.valueType = valueType;
        ensureNonNull(  keyType,   "keyType");
        ensureNonNull(valueType, "valueType");
    }

    /**
     * Ensure that the given argument is non-null.
     */
    private static void ensureNonNull(final Class type, final String name) {
        if (type == null) {
            throw new NullPointerException(
                      Resources.format(ResourceKeys.ERROR_NULL_ARGUMENT_$1, name));
        }
    }

    /**
     * Checks the type of the specified object. The default implementation ensure
     * that the object is assignable to the type specified at construction time.
     *
     * @param  element the object to check, or {@code null}.
     * @throws IllegalArgumentException if the specified element is not of the expected type.
     */
    private static void ensureValidType(final Object element, final Class type)
            throws IllegalArgumentException
    {
        if (element!=null && !type.isInstance(element)) {
            throw new IllegalArgumentException(Resources.format(ResourceKeys.ERROR_ILLEGAL_CLASS_$2,
                      Utilities.getShortClassName(element), Utilities.getShortName(type)));
        }
    }

    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for this key, the old
     * value is replaced.
     *
     * @param key key with which the specified value is to be associated.
     * @param value value to be associated with the specified key.
     * @return previous value associated with specified key, or {@code null}.
     */
    public Object put(final Object key, final Object value) {
        ensureValidType(key,     keyType);
        ensureValidType(value, valueType);
        return super.put(key, value);
    }
}
