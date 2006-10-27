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

// J2SE dependencies
import java.lang.ref.SoftReference;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.NoSuchElementException;
import java.util.ConcurrentModificationException;

// Geotools dependencies
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;


/**
 * A hash map implementation that uses {@linkplain SoftReference soft references}, leaving memory
 * when an entry is not used anymore and memory is low.
 * <p>
 * This map implementation actually maintains some of the first entries as hard references.
 * Only oldest entries are retained by soft references, in order to avoid too aggressive garbage
 * collection. The amount of entries to retain by hard reference is specified at {@linkplain
 * #SoftValueHashMap(int) construction time}.
 * <p>
 * This map is thread-safe. It accepts the null key, but doesn't accepts null values. Usage
 * of {@linkplain #values value}, {@linkplain #keySet key} or {@linkplain #entrySet entry}
 * collections are supported except for direct usage of their iterators, which may throw
 * {@link ConcurrentModificationException} randomly depending on the garbage collector activity.
 * 
 * @since 2.3
 * @source $URL$
 * @version $Id$
 * @author Simone Giannecchini
 * @author Martin Desruisseaux
 */
public class SoftValueHashMap/*<K,V>*/ extends AbstractMap/*<K,V>*/ {
    /**
     * The default value for {@link #hardReferencesCount}.
     */
    private static final int DEFAULT_HARD_REFERENCE_COUNT = 20;

    /**
     * The map of hard or soft references. Values are either direct reference to the objects,
     * or wrapped in a {@code Reference} object.
     */
    private final Map/*<K,Object>*/ hash = new HashMap/*<K,Object>*/();

    /**
     * The FIFO list of keys to hard references. Newest elements are first, and latest elements
     * are last. This list should never be longer than {@link #hardReferencesCount}.
     */
    private final LinkedList/*<K>*/ hardCache = new LinkedList/*<K>*/();

    /**
     * The number of hard references to hold internally.
     */
    private final int hardReferencesCount;

    /**
     * The entries to be returned by {@link #entrySet()}, or {@code null} if not yet created.
     */
    private transient Set entries;

    /**
     * Creates a map with the default hard references count.
     */
    public SoftValueHashMap() {
        hardReferencesCount = DEFAULT_HARD_REFERENCE_COUNT;
    }

    /**
     * Creates a map with the specified hard references count.
     */
    public SoftValueHashMap(final int hardReferencesCount) {
        this.hardReferencesCount = hardReferencesCount;
    }

    /**
     * Ensures that the specified value is non-null.
     */
    private static void ensureNotNull(final Object/*V*/ value) throws IllegalArgumentException {
        if (value == null) {
            throw new IllegalArgumentException(Errors.format(ErrorKeys.NULL_ARGUMENT_$1, "value"));
        }
    }

    /**
     * Performs a consistency check on this map. This method is used for tests and
     * assertions only.
     */
    final boolean isValid() {
        int count=0, size=0;
        synchronized (hash) {
            for (final Iterator it=hash.entrySet().iterator(); it.hasNext();) {
                final Map.Entry entry = (Map.Entry) it.next();
                if (entry.getValue() instanceof Reference) {
                    count++;
                } else {
                    assert hardCache.contains(entry.getKey());
                }
                size++;
            }
            assert size == hash.size();
            assert hardCache.size() == Math.min(size, hardReferencesCount);
        }
        return count == Math.max(size - hardReferencesCount, 0);
    }

    /**
     * Returns the number of entries in this map.
     */
    public int size() {
        synchronized (hash) {
            return hash.size();
        }
    }

    /**
     * Returns {@code true} if this map contains a mapping for the specified key.
     */
    public boolean containsKey(final Object key) {
        synchronized (hash) {
            return hash.containsKey(key);
        }
    }

    /**
     * Returns {@code true} if this map maps one or more keys to this value.
     */
    public boolean containsValue(final Object value) {
        ensureNotNull(value);
        synchronized (hash) {
            /*
             * We must rely on the super-class default implementation, not on HashMap
             * implementation, because some references are wrapped into SoftReferences.
             */
            return super.containsValue(value);
        }
    }

    /**
     * Returns the value to which this map maps the specified key. Returns {@code null} if
     * the map contains no mapping for this key, or the value has been garbage collected.
     *
     * @param key key whose associated value is to be returned.
     * @return the value to which this map maps the specified key, or {@code null} if none.
     */
    public /*V*/ Object get(final Object key) {
        synchronized (hash) {
            Object value = hash.get(key);
            if (value instanceof Reference) {
                /*
                 * The value is a soft reference only if it was not used for a while and the map
                 * contains more than 'hardReferenceCount' entries. Otherwise, it is an ordinary
                 * reference and is returned directly. See the 'retainStrongly' method.
                 *
                 * If the value is a soft reference, get the referent and clear it immediately
                 * for avoiding the reference to be enqueded. We abandon the soft reference and
                 * reinject the referent as a strong reference in the hash map, since we try to
                 * keep the last entries by strong references.
                 */
                value = ((Reference) value).getAndClear();
                if (value != null) {
                    // Transforms the soft reference into a hard one.
                    hash.put(key, value);
                    retainStrongly(key);
                } else {
                    // The value has already been garbage collected.
                    hash.remove(key);
                }
            }
            return value;
        }
    }

    /**
     * Declares that the value for the specified key must be retained by hard reference.
     * If there is already {@link #hardReferencesCount} hard references, then this method
     * replaces the oldest hard reference by a soft one.
     */
    private void retainStrongly(final /*K*/ Object key) {
        assert Thread.holdsLock(hash);
        assert !hardCache.contains(key) : key;
        hardCache.addFirst(key);
        if (hardCache.size() > hardReferencesCount) {
            // Remove the last entry if list longer than hardReferencesCount
            final /*K*/ Object toRemove = hardCache.removeLast();
            final Object value = hash.get(toRemove);
            assert value!=null && !(value instanceof Reference) : toRemove;
            hash.put(toRemove, new Reference(hash, toRemove, value));
            assert hardCache.size() == hardReferencesCount;
        }
        assert isValid();
    }

    /**
     * Associates the specified value with the specified key in this map.
     *
     * @param key Key with which the specified value is to be associated.
     * @param value Value to be associated with the specified key. The value can't be null.
     * 
     * @return Previous value associated with specified key, or {@code null}
     *	       if there was no mapping for key.
     */
    public Object put(final Object key, final Object value) {
        ensureNotNull(value);
        synchronized (hash) {
            Object oldValue = hash.put(key, value);
            if (oldValue instanceof Reference) {
                oldValue = ((Reference) oldValue).getAndClear();
            } else if (oldValue != null) {
                /*
                 * The value was retained by hard reference, which implies that the key must be in
                 * the hard-cache list. Removes the key from the list, since we want to reinsert it
                 * at the begining of the list in order to mark the value as the most recently used.
                 * This method performs a linear search, which may be quite ineficient. But it still
                 * efficient enough if the key was recently used, in which case it appears near the
                 * begining of the list. We assume that this is a common case. We may revisit later
                 * if profiling show that this is a performance issue.
                 */
                if (!hardCache.remove(key)) {
                    throw new AssertionError(key);
                }
            }
            retainStrongly(key);
            return oldValue;
        }
    }

    /**
     * Copies all of the mappings from the specified map to this map.
     */
    public void putAll(Map/*<? extends K, ? extends V>*/ map) {
        synchronized (hash) {
            super.putAll(map);
        }
    }

    /**
     * Removes the mapping for this key from this map if present.
     *
     * @param  key Key whose mapping is to be removed from the map.
     * @return previous value associated with specified key, or {@code null}
     *	       if there was no entry for key.
     */
    public Object remove(final Object key) {
        synchronized (hash) {
            Object oldValue = hash.remove(key);
            if (oldValue instanceof Reference) {
                oldValue = ((Reference) oldValue).getAndClear();
            } else if (oldValue != null) {
                /*
                 * See the comment in the 'put' method.
                 */
                if (!hardCache.remove(key)) {
                    throw new AssertionError(key);
                }
            }
            return oldValue;
        }
    }

    /**
     * Removes all mappings from this map.
     */
    public void clear() {
        synchronized (hash) {
            for (final Iterator it=hash.values().iterator(); it.hasNext();) {
                final Object value = it.next();
                if (value instanceof Reference) {
                    ((Reference) value).getAndClear();
                }
            }
            hash.clear();
            hardCache.clear();
        }
    }

    /**
     * Returns a set view of the mappings contained in this map.
     */
    public Set entrySet() {
        synchronized (hash) {
            if (entries == null) {
                entries = new Entries();
            }
            return entries;
        }
    }

    /**
     * Compares the specified object with this map for equality.
     */
    public boolean equals(final Object object) {
        synchronized (hash) {
            return super.equals(object);
        }
    }

    /**
     * Returns the hash code value for this map.
     */
    public int hashCode() {
        synchronized (hash) {
            return super.hashCode();
        }
    }

    /**
     * Returns a string representation of this map.
     */
    public String toString() {
        synchronized (hash) {
            return super.toString();
        }
    }

    /**
     * Implementation of the entries set to be returned by {@link #entrySet()}.
     */
    private final class Entries extends AbstractSet {
        /**
         * Returns an iterator over the elements contained in this collection.
         */
        public Iterator iterator() {
            synchronized (hash) {
                return new Iter(hash);
            }
        }

        /**
         * Returns the number of elements in this collection.
         */
        public int size() {
            return SoftValueHashMap.this.size();
        }

        /**
         * Returns {@code true} if this collection contains the specified element.
         */
        public boolean contains(final Object entry) {
            synchronized (hash) {
                return super.contains(entry);
            }
        }

        /**
         * Returns an array containing all of the elements in this collection.
         */
        public Object[] toArray() {
            synchronized (hash) {
                return super.toArray();
            }
        }

        /**
         * Returns an array containing all of the elements in this collection.
         */
        public /*<T> T[]*/ Object[] toArray(final /*T[]*/ Object[] array) {
            synchronized (hash) {
                return super.toArray(array);
            }
        }

        /**
         * Removes a single instance of the specified element from this collection,
         * if it is present.
         */
        public boolean remove(final Object entry) {
            synchronized (hash) {
                return super.remove(entry);
            }
        }

        /**
         * Returns {@code true} if this collection contains all of the elements
         * in the specified collection.
         */
        public boolean containsAll(final Collection/*<?>*/ collection) {
            synchronized (hash) {
                return super.containsAll(collection);
            }
        }

        /**
         * Adds all of the elements in the specified collection to this collection.
         */
        public boolean addAll(final Collection/*<? extends E>*/ collection) {
            synchronized (hash) {
                return super.addAll(collection);
            }
        }

        /**
         * Removes from this collection all of its elements that are contained in
         * the specified collection.
         */
        public boolean removeAll(final Collection/*<?>*/ collection) {
            synchronized (hash) {
                return super.removeAll(collection);
            }
        }

        /**
         * Retains only the elements in this collection that are contained in the
         * specified collection.
         */
        public boolean retainAll(final Collection/*<?>*/ collection) {
            synchronized (hash) {
                return super.retainAll(collection);
            }
        }

        /**
         * Removes all of the elements from this collection.
         */
        public void clear() {
            SoftValueHashMap.this.clear();
        }

        /**
         * Returns a string representation of this collection.
         */
        public String toString() {
            synchronized (hash) {
                return super.toString();
            }
        }
    }

    /**
     * The iterator to be returned by {@link Entries}.
     */
    private static final class Iter implements Iterator {
        /**
         * A copy of the {@link SoftValueHashMap#hash} field.
         */
        private final Map hash;

        /**
         * The iterator over the {@link #hash} entries.
         */
        private final Iterator iterator;

        /**
         * The next entry to be returned by the {@link #next} method, or {@code null}
         * if not yet computed of if the iteration is finished.
         */
        private transient Map.Entry entry;

        /**
         * Creates an iterator for the specified {@link SoftValueHashMap#hash} field.
         */
        Iter(final Map hash) {
            this.hash = hash;
            this.iterator = hash.entrySet().iterator();
        }

        /**
         * Set {@link #entry} to the next entry to iterate. Returns {@code true} if
         * an entry has been found, or {@code false} if the iteration is finished.
         */
        private boolean findNext() {
            assert Thread.holdsLock(hash);
            while (iterator.hasNext()) {
                final Map.Entry candidate = (Map.Entry) iterator.next();
                Object value = candidate.getValue();
                if (value instanceof Reference) {
                    value = ((Reference) value).get();
                    entry = new MapEntry(candidate.getKey(), value);
                    return true;
                }
                if (value != null) {
                    entry = candidate;
                    return true;
                }
            }
            return false;
        }

        /**
         * Returns {@code true} if this iterator can return more value.
         */
        public boolean hasNext() {
            synchronized (hash) {
                return entry!=null || findNext();
            }
        }

        /**
         * Returns the next value. If some value were garbage collected after the
         * iterator was created, they will not be returned. Note however that a
         * {@link ConcurrentModificationException} may be throw if the iteration
         * is not synchronized on {@link #hash}.
         */
        public Object/*Map.Entry*/ next() {
            synchronized (hash) {
                if (entry==null && !findNext()) {
                    throw new NoSuchElementException();
                }
                final Map.Entry next = entry;
                entry = null; // Flag that a new entry will need to be lazily fetched.
                return next;
            }
        }

        /**
         * Removes the last entry.
         */
        public void remove() {
            synchronized (hash) {
                iterator.remove();
            }
        }
    }

    /**
     * A soft reference to a map entry. Soft references are created only when the map contains
     * more than {@link #hardReferencesCount}, in order to avoid to put more pressure on the
     * garbage collector.
     */
    private static final class Reference/*<K,V>*/ extends SoftReference/*<V>*/ {
        /**
         * A reference to the {@link SoftValueHashMap#hash} entries. We keep this reference instead
         * than a reference to {@link SoftValueHashMap} itself in order to avoid indirect retention
         * of {@link SoftValueHashMap#hardCache}, which is not needed for this reference.
         */
        private final Map/*<K,V>*/ hash;

        /**
         * The key for the entry to be removed when the soft reference is cleared.
         */
        private final /*K*/ Object key;

        /**
         * Creates a soft reference for the specified key-value pair.
         */
        Reference(final Map/*<K,V>*/ hash, final /*K*/ Object key, final /*V*/ Object value) {
            super(value, WeakCollectionCleaner.DEFAULT.referenceQueue);
            this.hash = hash;
            this.key  = key;
        }

        /**
         * Gets and clear this reference object. This method performs no additional operation.
         * More specifically:
         * <ul>
         *   <li>It does not enqueue the reference.</li>
         *   <li>It does not remove the reference from the hash map.</li>
         * </ul>
         * This is because this method is invoked when the entry should have already be removed,
         * or is about to be removed.</li>
         */
        final Object getAndClear() {
            assert Thread.holdsLock(hash);
            final Object value = get();
            super.clear();
            return value;
        }

        /**
         * Removes the entries from the backing hash map. This method need to
         * override the {@link SoftReference#clear} method because it is invoked
         * by {@link WeakCollectionCleaner}.
         */
        public void clear() {
            super.clear();
            synchronized (hash) {
                final Object old = hash.remove(key);
                /*
                 * If the entry was used for an other value, then put back the old value. This
                 * case may occurs if a new value was set in the hash map before the old value
                 * was garbage collected.
                 */
                if (old != this && old != null) {
                    hash.put(key, old);
                }
            }
        }
    }
}
