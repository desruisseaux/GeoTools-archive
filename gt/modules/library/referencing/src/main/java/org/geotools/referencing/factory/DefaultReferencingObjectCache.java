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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * Caching implementation for ReferencingObjectCache. This instance is used when
 * actual caching is desired.
 * 
 * @since 2.4
 * @version $Id$
 * @source $URL$
 * @author Cory Horner (Refractions Research)
 */
final class DefaultReferencingObjectCache implements ReferencingObjectCache {

    /**
     * A cheap cache map implementation (not scalable).
     * 
     * <Object, Entry>
     */
    private volatile Map cache = Collections.synchronizedMap(new HashMap());
    
    /**
     * Creates a new cache.
     */
    public DefaultReferencingObjectCache() {
    }
    
    /**
     * Creates a new cache which will hold the specified amount of object by strong references.
     * Any additional object will be help by weak references.
     */
    public DefaultReferencingObjectCache(final int maxStrongReferences) {
    }

    /**
     * Removes all entries from this map.
     */
    public synchronized void clear() {
        if (cache != null) {
            cache.clear();
        }
    }

    /**
     * Non-blocking indicator if an entry exists in the cache.
     * 
     * @param key
     * @return boolean
     */
    public boolean containsKey(final Object key) {
        if (!cache.containsKey(key)) {
            return false;
        }
        ObjectCacheEntry entry = (ObjectCacheEntry) cache.get(key);
        return entry.containsValue();
    }
    
    /**
     * Returns the object from the cache. The contents may be null.
     * 
     * @param key
     *            The authority code.
     * 
     * @todo Consider logging a message here to the finer or finest level.
     */
    public Object get(final Object key) {
        checkCache(key);
        ObjectCacheEntry entry = (ObjectCacheEntry) cache.get(key);
        return entry.get();
    }

    public void writeLock(final Object key) {
        checkCache(key);
        ObjectCacheEntry entry = (ObjectCacheEntry) cache.get(key);
        entry.writeLock();
    }

    public void writeUnLock(final Object key) {
        checkCache(key);
        ObjectCacheEntry entry = (ObjectCacheEntry) cache.get(key);
        entry.writeUnLock();
    }

    /**
     * Puts an element into the cache.
     *
     * @param key the authority code.
     * @param object The referencing object to add in the pool.
     */
    public void put(final Object key, final Object object) {
        checkCache(key);
        ObjectCacheEntry entry = (ObjectCacheEntry) cache.get(key);
        entry.set(object);
    }

    /**
     * Checks the map for a missing entry. If one does not exist, a new entry is
     * created.
     * 
     * @param key
     *            referencing object identifier
     */
    private void checkCache(Object key) {
        synchronized (cache) {
            if (!cache.containsKey(key)) {
                ObjectCacheEntry newEntry = new ObjectCacheEntry();
                cache.put(key, newEntry);
            }
        }
    }
}
