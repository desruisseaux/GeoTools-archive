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
public final class DefaultObjectCache implements ObjectCache {

    /**
     * A cheap cache map implementation (not scalable).
     * 
     * <Object, Entry>
     */
    private volatile Map cache;
    
    /**
     * Creates a new cache.
     */
    public DefaultObjectCache() {
        cache = Collections.synchronizedMap(new HashMap());
    }
    
    /**
     * Creates a new cache using the indicated initialSize.
     */
    public DefaultObjectCache(final int initialSize) {
        cache = Collections.synchronizedMap(new HashMap(initialSize));
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
     * Check if an entry exists in the cache.
     * 
     * @param key
     * @return boolean
     */
    public boolean containsKey(final Object key) {
        return cache.containsKey(key);
    }
    
    /**
     * Returns the object from the cache.
     * <p>
     * Please note that a read lock is maintained on the cache contents; you 
     * may be stuck waiting for a writer to produce the result over the
     * course of calling this method.
     * </p>
     * The contents (of course) may be null.
     * 
     * @param key
     *            The authority code.
     * 
     * @todo Consider logging a message here to the finer or finest level.
     */
    public Object get(final Object key) {
        return getEntry(key).get();
    }

    public Object test(final Object key) {
        if (!cache.containsKey(key)) {
            // no entry for this key - so no value
            return null;
        }
        ObjectCacheEntry entry = getEntry(key);
        try {
            entry.writeLock();
            return entry.get();
        } finally {
            entry.writeUnLock();
        }
    }

    public void writeLock(final Object key) {
        getEntry(key).writeLock();
    }

    public void writeUnLock(final Object key) {
        if (!cache.containsKey(key)) {
            throw new IllegalStateException("Cannot unlock prior to locking");
        }
        getEntry(key).writeUnLock();
    }

    /**
     * Stores a value
     */
    public void put(final Object key, final Object object) {
        getEntry(key).set(object);        
    }

    /**
     * Retrieve cache entry, will create one if needed.
     * 
     * @param key
     * @return ObjectCacheEntry
     */
    protected ObjectCacheEntry getEntry(Object key) {
        synchronized (cache) {
            if (!cache.containsKey(key)) {
                ObjectCacheEntry newEntry = new ObjectCacheEntry();
                cache.put(key, newEntry);
            }
            return (ObjectCacheEntry) cache.get(key);
        }
    }
}
