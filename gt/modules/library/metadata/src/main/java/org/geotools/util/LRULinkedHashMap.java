/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, Geotools Project Managment Committee (PMC)
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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * A {@link Map} with a fixed maximum size which removes the <cite>least recently used</cite> (LRU)
 * entry if an entry is added when full. This class implements a simple technique for LRU pooling
 * of objects.
 *
 * @source $URL$
 * @version $Id$
 * @author Simone Giannecchini
 * @since 2.3
 */
public final class LRULinkedHashMap<K,V> extends LinkedHashMap<K,V> {
    /**
     * Serial number for cross-version compatibility.
     */
    private static final long serialVersionUID = 2082871604196698140L;

    /**
     * Default maximum capacity.
     */
    private static final int DEFAULT_MAXIMUM_CAPACITY = 100;

    /**
     * Maximum number of entries for this LRU cache.
     */
    private final int maxEntries;

    /**
     * Constructs a {@code LRULinkedHashMap} with default initial capacity, maximum capacity
     * and load factor.
     */
    public LRULinkedHashMap() {
        super();
        maxEntries = DEFAULT_MAXIMUM_CAPACITY;
    }

    /**
     * Constructs a {@code LRULinkedHashMap} with default maximum capacity and load factor.
     *
     * @param initialCapacity The initial capacity.
     */
    public LRULinkedHashMap(final int initialCapacity) {
        super(initialCapacity);
        maxEntries = DEFAULT_MAXIMUM_CAPACITY;
    }

    /**
     * Constructs a {@code LRULinkedHashMap} with default maximum capacity.
     *
     * @param initialCapacity The initial capacity.
     * @param loadFactor      The load factor.
     */
    public LRULinkedHashMap(final int initialCapacity, final float loadFactor) {
        super(initialCapacity, loadFactor);
        maxEntries = DEFAULT_MAXIMUM_CAPACITY;
    }

    /**
     * Constructs a {@code LRULinkedHashMap} with default maximum capacity.
     *
     * @param initialCapacity The initial capacity.
     * @param loadFactor      The load factor.
     * @param accessOrder     The ordering mode: {@code true} for access-order,
     *                        {@code false} for insertion-order.
     */
    public LRULinkedHashMap(final int initialCapacity, final float loadFactor,
                            final boolean accessOrder)
    {
        super(initialCapacity, loadFactor, accessOrder);
        maxEntries = DEFAULT_MAXIMUM_CAPACITY;
    }

    /**
     * Constructs a {@code LRULinkedHashMap} with the specified maximum capacity.
     *
     * @param initialCapacity The initial capacity.
     * @param loadFactor      The load factor.
     * @param accessOrder     The ordering mode: {@code true} for access-order,
     *                        {@code false} for insertion-order.
     * @param maxEntries      Maximum number of entries for this LRU cache.
     */
    public LRULinkedHashMap(final int initialCapacity, final float loadFactor,
                            final boolean accessOrder, final int maxEntries)
    {
        super(initialCapacity, loadFactor, accessOrder);
        this.maxEntries = maxEntries;
    }

    /**
     * Constructs a {@code LRULinkedHashMap} with all entries from the specified map.
     *
     * @param m the map whose mappings are to be placed in this map.
     */
    private LRULinkedHashMap(final Map<K,V> map) {
        super(map);
        maxEntries = DEFAULT_MAXIMUM_CAPACITY;
        removeExtraEntries();
    }

    /**
     * Constructs a {@code LRULinkedHashMap} with all entries from the specified map
     * and maximum number of entries.
     *
     * @param m the map whose mappings are to be placed in this map.
     * @param maxEntries      Maximum number of entries for this LRU cache.
     */
    private LRULinkedHashMap(final Map<K,V> map, final int maxEntries) {
        super(map);
        this.maxEntries = maxEntries;
        removeExtraEntries();
    }

    /**
     * If there is more entries than the maximum amount, remove extra entries.
     * <p>
     * <b>Note:</b> Invoking {@code removeExtraEntries()} after adding all entries in the
     * {@code LRULinkedHashMap(Map)} constructor is less efficient than just iterating over
     * the {@code maxEntries} first entries at construction time, but super-class constructor
     * is more efficient for maps with less than {@code maxEntries}. We assume that this is the
     * most typical case. In addition, this method would be needed anyway if we add a
     * {@code setMaximumEntries(int)} method in some future Geotools version.
     */
    private void removeExtraEntries() {
        if (size() > maxEntries) {
            final Iterator<Map.Entry<K,V>> it = entrySet().iterator();
            for (int c=0; c<maxEntries; c++) {
                it.next();
            }
            while (it.hasNext()) {
                it.remove();
            }
        }
    }

    /**
     * Returns {@code true} if this map should remove its eldest entry. The default implementation
     * returns {@code true} if the {@linkplain #size number of entries} in this map has reached the
     * maximum number of entries specified at construction time.
     *
     * @param eldest The least recently inserted entry in the map, or if this is an access-ordered
     *        map, the least recently accessed entry. This is the entry that will be removed it this
     *        method returns {@code true}.
     * @return {@code true} if the eldest entry should be removed from the map;
     *         {@code false} if it should be retained.
     */
    @Override
    protected boolean removeEldestEntry(final Map.Entry<K,V> eldest) {
        /*
         * Do we have to remove anything?
         * If we still below the desired threshold, just return false.
         */
        return size() > maxEntries;
    }
}
