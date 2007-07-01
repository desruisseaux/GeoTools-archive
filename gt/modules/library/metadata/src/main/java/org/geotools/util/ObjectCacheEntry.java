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

import EDU.oswego.cs.dl.util.concurrent.ReadWriteLock;
import EDU.oswego.cs.dl.util.concurrent.ReentrantWriterPreferenceReadWriteLock;


/**
 * An entry in the {@link DefaultObjectCache}.
 * 
 * To use as a reader:
 * <blockquote><pre>
 * entry.get();
 * </pre></blockquote>
 * 
 * To use as a writer:
 * <blockquote><pre>
 * try {
 *    entry.writeLock();
 *    entry.set(value);
 * } finally {
 *    entry.writeUnLock();
 * }
 * </pre></blockquote>
 * Tip: The use of try/finally is more than just a good idea - it is the law.
 * 
 * @since 2.4
 * @author Cory Horner (Refractions Research)
 * @author Jody Garnett (Refractions Research)
 *
 * @todo change from edu.oswego to java.concurrent
 */
final class ObjectCacheEntry {
    /**
     * Value of this cache entry, managed by the {@linkplain #lock}.
     *
     * @todo According {@link java.util.concurrent.locks.ReentrantReadWriteLock} documentation,
     *       we don't need to declare this field as volatile. Revisit when we will be allowed to
     *       compile for J2SE 1.5.
     */
    private volatile Object value;

    /**
     * The lock used to manage the {@linkplain #value}.
     */
    private final ReadWriteLock lock = new ReentrantWriterPreferenceReadWriteLock();

    /**
     * Creates an entry with no initial value.
     */
    public ObjectCacheEntry() {
    }

    /**
     * Creates an entry with the specified initial value.
     */
    public ObjectCacheEntry(final Object value) {
        this.value = value;
    }

    /**
     * Acquires a write lock, obtains the value, unlocks, and returns the value.
     * If another thread already has the read or write lock, this method will block.
     * 
     * <blockquote><pre>
     * try {
     *    entry.writeLock();
     *    value = entry.peek();
     * }
     * finally {
     *    entry.writeUnLock();
     * }
     * </pre></blockquote>
     */
    public Object peek() {
        try {
            lock.writeLock().acquire();
            return value;
        } catch (InterruptedException e) {
            return null;
        } finally {
            lock.writeLock().release();
        }
    }

    /**
     * Acquires a read lock, obtains the value, unlocks, and returns the value.
     * If another thread already has the write lock, this method will block.
     * 
     * @return cached value or null if empty
     */
    public Object getValue() {
        try {
            lock.readLock().acquire();
            return value;
        } catch (InterruptedException e) {
            //TODO: add logger, or is this too performance constrained?
            return null;
        } finally {
            lock.readLock().release();
        }
    }

    /**
     * Stores the value in the entry, using the write lock. 
     * It is common to use this method while already holding the writeLock (since writeLock
     * is re-entrant).
     * 
     * @param value
     */
    public void setValue(Object value) {
        try {
           lock.writeLock().acquire();
           this.value = value;
        } catch (InterruptedException e) {

        } finally {
            lock.writeLock().release();
        }
    }

    /**
     * Acquires a write lock. This will block other readers and writers (on this
     * entry only), and other readers and writers will need to be cleared before
     * the write lock can be acquired, unless it is the same thread attempting
     * to read or write.
     */
    public boolean writeLock() {
        try {
            lock.writeLock().acquire();
        } catch (InterruptedException e) {
            return false;
        }
        return true;
    }

    /**
     * Releases a write lock.  
     */
    public void writeUnLock() {
        lock.writeLock().release();
    }
}
