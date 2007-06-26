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
package org.geotools.referencing.factory;

import java.lang.ref.Reference;


/**
 * A cache for referencing objects.
 *
 * @since 2.4
 * @version $Id$
 * @source $URL$
 *
 * @todo Consider renaming as {@code ObjectCache} or {@code Cache} and move to
 *       the {@code org.geotools.util} package.
 */
public interface ReferencingObjectCache {

    /**
     * Removes all entries from this cache.
     */
    void clear();

    /**
     * Returns an object from the pool for the specified code. If the object was retained as a
     * {@linkplain Reference weak reference}, the {@link Reference#get referent} is returned.
     *
     * @param key The authority code.
     */
    Object get(Object key);

    /**
     * Put an element in the pool. This method is invoked everytime a {@code createFoo(...)}
     * method is invoked, even if an object was already in the pool for the given code, for
     * the following reasons: 1) Replaces weak reference by strong reference (if applicable)
     * and 2) Alters the linked hash set order, so that this object is declared as the last
     * one used.
     *
     * @param key the authority code.
     * @param object The referencing object to add in the pool.
     */
    void put(Object key, Object object);
    
    void writeLock(Object key);
    
    void writeUnLock(Object key);
    
    /**
     * Non-blocking indicator if an entry exists in the cache.
     */
    public boolean containsKey(final Object key);

}
