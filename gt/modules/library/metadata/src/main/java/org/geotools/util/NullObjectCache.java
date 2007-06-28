/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2007, GeoTools Project Managment Committee (PMC)
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
 * Null implementation for the ReferencingObjectCache. Used for cases where
 * caching is *not* desired.
 * 
 * @since 2.4
 * @version $Id$
 * @source $URL$
 * @author Cory Horner (Refractions Research)
 */
public final class NullObjectCache implements ObjectCache {
    /**
     * Do nothing since this map is already empty.
     */
    public void clear() {
    }

    /**
     * Returns {@code null} since this map is empty.
     */
    public Object get(Object key) {
        return null;
    }

    /**
     * Do nothing since this map does not cache anything.
     */
    public void put(Object key, Object object) {
    }

    /**
     * There is no cache, therefore a cache miss is a safe assumption.
     */
    public boolean containsKey(Object key) {
        return false;
    }

    /**
     * Do nothing since there is no write lock.
     */
    public void writeLock(Object key) {
    }

    /**
     * Do nothing since there is no write lock.
     */
    public void writeUnLock(Object key) {
    }

    public Object peek( Object key ) {
        return null;
    }
}
