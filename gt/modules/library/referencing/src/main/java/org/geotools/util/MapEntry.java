/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, Geotools Project Managment Committee (PMC)
 *    (C) 2004, Institut de Recherche pour le Développement
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.util;

// J2SE dependencies
import java.io.Serializable;
import java.util.Map;

// Geotools dependencies
import org.geotools.resources.Utilities;


/**
 * A default implementation of {@link java.util.Map.Entry} which map an arbitrary
 * key-value pairs. This entry is immutable by default.
 *
 * @since 2.1
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @todo This class will be removed when we will be allowed to compile for JSE 1.6, since a
 *       default map entry implementation is provided there.
 */
public class MapEntry implements Map.Entry, Serializable {
    /**
     * The key.
     */
    private final Object key;

    /**
     * The value.
     */
    private final Object value;

    /**
     * Creates a new map entry with the specified key-value pair.
     */
    public MapEntry(final Object key, final Object value) {
        this.key   = key;
        this.value = value;
    }

    /**
     * Returns the key corresponding to this entry.
     */
    public Object getKey() {
        return key;
    }

    /**
     * Returns the value corresponding to this entry.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Replaces the value corresponding to this entry with the specified
     * value (optional operation). The default implementation throws an
     * {@link UnsupportedOperationException}.
     */
    public Object setValue(final Object value) {
        throw new UnsupportedOperationException();
    }

    /**
     * Compares the specified object with this entry for equality.
     */
    public boolean equals(final Object object) {
        if (object instanceof Map.Entry) {
            final Map.Entry that = (Map.Entry) object;
            return Utilities.equals(this.getKey(),   that.getKey()) &&
                   Utilities.equals(this.getValue(), that.getValue());
        }
        return false;
    }

    /**
     * Returns the hash code value for this map entry
     */
    public int hashCode() {
        int code = 0;
        if (key   != null) code  =   key.hashCode();
        if (value != null) code ^= value.hashCode();
        return code;
    }

    /**
     * Returns a string representation of this entry.
     */
    public String toString() {
        return Utilities.getShortClassName(this) + "[key=" + key + ", value=" + value + ']';
    }
}
