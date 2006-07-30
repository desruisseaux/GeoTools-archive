package org.geotools.util;

import java.lang.ref.Reference;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This class implements a simple technique for LRU pooling of objects. Note
 * that this cache is based on hard references not on weak or soft references.
 * 
 * @author Simone Giannecchini
 * @since 2.3
 * 
 */
public final class LRULinkedHashMap extends LinkedHashMap {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2082871604196698140L;

	private static final float DEFAULT_GROWTH_FACTOR = .75F;

	private static final int DEFAULT_INITIAL_CAPACITY = 100;

	private static final int DEFAULT_MAXIMUM_CAPACITY = 100;

	private int maxEntries;

	public LRULinkedHashMap(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);

	}

	public LRULinkedHashMap(int initialCapacity) {
		super(initialCapacity, DEFAULT_GROWTH_FACTOR);
		maxEntries = DEFAULT_MAXIMUM_CAPACITY;
	}

	public LRULinkedHashMap() {
		this(DEFAULT_INITIAL_CAPACITY, DEFAULT_GROWTH_FACTOR);
	}

	public LRULinkedHashMap(Map m) {
		this(m, DEFAULT_MAXIMUM_CAPACITY);

	}

	public LRULinkedHashMap(Map m, final int maxEntries) {
		super(m);
		this.maxEntries = maxEntries;

	}

	public LRULinkedHashMap(int initialCapacity, float loadFactor,
			boolean accessOrder, final int maxEntries) {
		super(initialCapacity, loadFactor, accessOrder);
		this.maxEntries = maxEntries;
	}

	public LRULinkedHashMap(int initialCapacity, float loadFactor,
			boolean accessOrder) {
		this(initialCapacity, loadFactor, accessOrder, DEFAULT_MAXIMUM_CAPACITY);

	}

	/**
	 * Returns <tt>true</tt> if this map should remove its eldest entry. This
	 * method is invoked by <tt>put</tt> and <tt>putAll</tt> after inserting
	 * a new entry into the map. It provides the implementer with the
	 * opportunity to remove the eldest entry each time a new one is added. This
	 * is useful if the map represents a cache: it allows the map to reduce
	 * memory consumption by deleting stale entries. <p/> Will return true if:
	 * <ol>
	 * <li> the element has expired
	 * <li> the cache size is greater than the in-memory actual. In this case we
	 * spool to disk before returning.
	 * </ol>
	 * 
	 * @param eldest
	 *            The least recently inserted entry in the map, or if this is an
	 *            access-ordered map, the least recently accessed entry. This is
	 *            the entry that will be removed it this method returns
	 *            <tt>true</tt>.
	 * @return true if the eldest entry should be removed from the map;
	 *         <tt>false</t> if it should be retained.
	 */
	protected boolean removeEldestEntry(Entry eldest) {
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

	protected void finalize() throws Throwable {
		dispose();
		super.finalize();
	}

	public void dispose() {

	}

}
