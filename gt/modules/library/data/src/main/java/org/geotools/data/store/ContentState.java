/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.data.store;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.geotools.data.FeatureListener;


/**
 * State for a content entry.
 * <p>
 * This class maintains a cache of certain aspects of a feature type which are
 * subject to the state of a dataset modified in a transaction. Examples of 
 * content to cache:
 * <ul>
 * <li>key: instance of FeatureTypeFactory; value: instance of FeatureType 
 * <li>key: "bounds"; value: Envelope of dataset
 * <li>key: "count"; value: Number of features in dataset
 * </ul>
 * </p>
 * 
 * @author Jody Garnett, Refractions Research Inc.
 * @author Justin Deoliveira, The Open Planning Project
 */
public final class ContentState {
    /**
     * cache / state
     */
    Map cache = new HashMap();

    /**
     * observers
     */
    List listeners = new ArrayList(2);

    /**
     * entry maintaining the state
     */
    ContentEntry entry;

    /**
     * Creates a new state.
     *
     * @param entry The entry for the state.
     */
    public ContentState(ContentEntry entry) {
        this.entry = entry;
    }

    /**
     * Adds a new item to the cache.
     *
     * @param key The key for the item.
     * @param value The item.
     */
    public void put(Object key, Object value) {
        cache.put(key, value);
    }

    /**
     * Retreives an item from the cache.
     *
     * @param key The key for the item.
     *
     * @return The item, of <code>null</code> if no such item exists for key.
     */
    public Object get(Object key) {
        return cache.get(key);
    }

    /**
     * Flushes the cache.
     */
    final public void flush() {
        cache.clear();
    }

    /**
     * Access to content entry (containing content definition )
     */
    public ContentEntry getEntry() {
        return entry;
    }

    /**
     * Cleans up the state object by clearing cache and listeners.
     */
    public void close() {
        cache.clear();
        cache = null;
        listeners.clear();
        listeners = null;
    }

    /**
     * Adds a listener for collection events.
     *
     * @param listener The listener to add
     */
    public void addListener(FeatureListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a listener for collection events.
     *
     * @param listener The listener to remove
     */
    public void removeListener(FeatureListener listener) {
        listeners.remove(listener);
    }

    /**
     * Clones the state copying the cache.
     * <p>
     * Listeners are not copied.
     * </p>
     */
    protected Object clone() throws CloneNotSupportedException {
        ContentState clone = new ContentState(entry);

        clone.cache = new HashMap(cache);
        clone.listeners = new ArrayList(2);

        return clone;
    }

    /**
     * Copies the state.
     *
     * @return A copy of the state.
     */
    public ContentState copy() {
        try {
            return (ContentState) clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("ContentState always can be copied");
        }
    }
}
