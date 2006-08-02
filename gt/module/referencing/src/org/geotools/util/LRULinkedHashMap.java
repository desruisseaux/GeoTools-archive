package org.geotools.util;

import java.lang.ref.Reference;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This class implements a simple technique for LRU pooling of objects.
 * <p>
 * Note that this cache is based on hard references not on weak or soft references because I have
 * personally found that especially for server side applications such caches are almost useless.
 * 
 * @author Simone Giannecchini
 * @since 2.3
 */
public final class LRULinkedHashMap extends LinkedHashMap {

    /**
     * 
     */
    private static final long serialVersionUID = 2082871604196698140L;

    /** Default grow factor. */
    private static final float DEFAULT_GROWTH_FACTOR = .75F;

    /** Default initial capacity. */
    private static final int DEFAULT_INITIAL_CAPACITY = 100;

    /** Default maximum capacity. */
    private static final int DEFAULT_MAXIMUM_CAPACITY = 100;

    /** Maximum number of entries for this lru cache. */
    private final int maxEntries;

    /**
     * Constructor for <code>LRULinkedHashMap</code>.
     * 
     * @param initialCapacity
     * @param loadFactor
     */
    public LRULinkedHashMap( int initialCapacity, float loadFactor ) {
        super(initialCapacity, loadFactor);
        this.maxEntries = DEFAULT_MAXIMUM_CAPACITY;

    }

    /**
     * Constructor for <code>LRULinkedHashMap</code>.
     * 
     * @param initialCapacity
     */
    public LRULinkedHashMap( int initialCapacity ) {
        super(initialCapacity, DEFAULT_GROWTH_FACTOR);
        maxEntries = DEFAULT_MAXIMUM_CAPACITY;
    }

    /**
     * Default constructor for <code>LRULinkedHashMap</code>.
     */
    public LRULinkedHashMap() {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_GROWTH_FACTOR);
    }

    /**
     * Constructor for <code>LRULinkedHashMap</code>.
     * 
     * @param m
     */
    private LRULinkedHashMap( Map m ) {
        this(m, DEFAULT_MAXIMUM_CAPACITY);

    }

    /**
     * Constructor for <code>LRULinkedHashMap</code>.
     * 
     * @param m
     * @param maxEntries
     */
    private LRULinkedHashMap( Map m, final int maxEntries ) {
        super(m);
        this.maxEntries = maxEntries;

    }

    /**
     * Constructor for <code>LRULinkedHashMap</code>.
     * 
     * @param initialCapacity
     * @param loadFactor
     * @param accessOrder
     * @param maxEntries
     */
    public LRULinkedHashMap( int initialCapacity, float loadFactor, boolean accessOrder,
            final int maxEntries ) {
        super(initialCapacity, loadFactor, accessOrder);
        this.maxEntries = maxEntries;
    }

    /**
     * Constructor for <code>LRULinkedHashMap</code>.
     * 
     * @param initialCapacity
     * @param loadFactor
     * @param accessOrder
     */
    public LRULinkedHashMap( int initialCapacity, float loadFactor, boolean accessOrder ) {
        this(initialCapacity, loadFactor, accessOrder, DEFAULT_MAXIMUM_CAPACITY);

    }

    /**
     * Returns <tt>true</tt> if this map should remove its eldest entry. This method is invoked by
     * <tt>put</tt> and <tt>putAll</tt> after inserting a new entry into the map. It provides
     * the implementer with the opportunity to remove the eldest entry each time a new one is added.
     * 
     * @param eldest The least recently inserted entry in the map, or if this is an access-ordered
     *        map, the least recently accessed entry. This is the entry that will be removed it this
     *        method returns <tt>true</tt>.
     * @return true if the eldest entry should be removed from the map;
     *         <tt>false</t> if it should be retained.
     */
    protected boolean removeEldestEntry( Entry eldest ) {
        // /////////////////////////////////////////////////////////////////////
        //
        // Do I have to remove anything?
        //
        // If I still am below the desired threshold I just return false nad
        // that is it.
        //
        // /////////////////////////////////////////////////////////////////////
        return size() > maxEntries;

    }

}
