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

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * A HashMap implementation that uses soft references, 20 * leaving memory
 * 
 * @author Simone Giannecchini (thanks to Dr. Kabutz on whose article the code
 *         is based)
 * @since 2.3
 */
public class SoftHashMap extends AbstractMap {
	private static final int DEFAULT_SIZE = 100;

	/** The internal HashMap that will hold the SoftReference. */
	private final Map hash = new HashMap();

	/** The number of "hard" references to hold internally. */
	private final int hardReferencesMapSize;

	/** The FIFO list of hard references, order of last access. */
	private final LinkedList hardCache = new LinkedList();

	/** Reference queue for cleared SoftReference objects. */
	private final ReferenceQueue queue = new ReferenceQueue();

	public SoftHashMap() {
		this(DEFAULT_SIZE);
	}

	public SoftHashMap(int hardSize) {
		hardReferencesMapSize = hardSize;
	}

	public Object get(Object key) {
		Object result = null;
		// We get the SoftReference represented by that key
		SoftReference soft_ref = (SoftReference) hash.get(key);
		if (soft_ref != null) {
			// From the SoftReference we get the value, which can be
			// null if it was not in the map, or it was removed in
			// the processQueue() method defined below
			result = soft_ref.get();
			if (result == null) {
				// If the value has been garbage collected, remove the
				// entry from the HashMap.
				hash.remove(key);
			} else {
				// We now add this object to the beginning of the hard
				// reference queue. One reference can occur more than
				// once, because lookups of the FIFO queue are slow, so
				// we don't want to search through it each time to remove
				// duplicates.
				hardCache.addFirst(result);
				if (hardCache.size() > hardReferencesMapSize) {
					// Remove the last entry if list longer than hardReferencesMapSize
					hardCache.removeLast();
				}
			}
		}
		return result;
	}

	/**
	 * We define our own subclass of SoftReference which contains not only the
	 * value but also the key to make it easier to find the entry in the HashMap
	 * after it's been garbage collected.
	 */
	private static class SoftValue extends SoftReference {
		private final Object key; // always make data member final

		/**
		 * Did you know that an outer class can access private data members and
		 * methods of an inner class? I didn't know that! I thought it was only
		 * the inner class who could access the outer class's private
		 * information. An outer class can also access private members of an
		 * inner class inside its inner class.
		 */
		private SoftValue(Object k, Object key, ReferenceQueue q) {
			super(k, q);
			this.key = key;
		}
	}

	/**
	 * Here we go through the ReferenceQueue and remove garbage collected
	 * SoftValue objects from the HashMap by looking them up using the
	 * SoftValue.key data member.
	 */
	private void processQueue() {
		SoftValue sv;
		while ((sv = (SoftValue) queue.poll()) != null) {
			hash.remove(sv.key); // we can access private data!
		}
	}

	/**
	 * Here we put the key, value pair into the HashMap using a SoftValue
	 * object.
	 */
	public Object put(Object key, Object value) {
		processQueue(); // throw out garbage collected values first
		return hash.put(key, new SoftValue(value, key, queue));
	}

	public Object remove(Object key) {
		processQueue(); // throw out garbage collected values first
		return hash.remove(key);
	}

	public void clear() {
		hardCache.clear();
		processQueue(); // throw out garbage collected values
		hash.clear();
	}

	public int size() {
		processQueue(); // throw out garbage collected values first
		return hash.size();
	}

	public Set entrySet() {
		throw new UnsupportedOperationException();
	}
}
