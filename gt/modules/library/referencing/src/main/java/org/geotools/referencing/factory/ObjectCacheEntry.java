package org.geotools.referencing.factory;

import EDU.oswego.cs.dl.util.concurrent.ReadWriteLock;
import EDU.oswego.cs.dl.util.concurrent.ReentrantWriterPreferenceReadWriteLock;


/**
 * An entry in the ReferencingObjectCache.
 * 
 * To use as a reader:
 * <pre><code>
 * entry.get();
 * </code></pre>
 * 
 * To use as a writer:
 * <pre><code>
 * try {
 *    entry.writeLock();
 *    entry. set( value );
 * } finally {
 *    entry.writeUnLock();
 * }
 * </code></pre>
 * Tip: The use of try/finally is more than just a good idea - it is the law
 * 
 * @since 2.4
 * @author Cory Horner (Refractions Research)
 * @author Jody Garnett (Refractions Research)
 * @todo change from edu.oswego to java.concurrent
 */
public class ObjectCacheEntry {
    
    /**
     * Value of this cache entry; managed by <code>lock</code>
     */
    private volatile Object value;
    
    /**
     * This lock is used to manage value.
     */
    private volatile ReadWriteLock lock = new ReentrantWriterPreferenceReadWriteLock();

    public ObjectCacheEntry() {
        this.value = null;
    }
    
    public ObjectCacheEntry(Object value) {
        this.value = value;
    }

    /**
     * Used by a writer to test the contents of the entry.
     * <p>
     * This method is similar to {@link get} (except it will not DEADLOCK):<pre></code>
     * try {
     *    entry.writeLock();
     *    value = entry.test();
     * }
     * finally {
     *    entry.writeUnLock();
     * }
     * </code></pre>
     */
    public Object test() {
        try {
            lock.writeLock().acquire();
            return value;
        } catch (InterruptedException e) {
            return null;
        }
        finally {
            lock.writeLock().release();
        }
    }
    
    /**
     * Acquires a read lock, obtains the value, unlocks, and returns the value.
     * If another thread already has the read lock or the write lock, this
     * method will block.
     * <p>
     * COMMON DEADLOCK:<pre></code>
     * try {
     *    entry.writeLock();
     *    value = entry.get(); // DEADLOCK as we wait for the writeLock to be released
     * }
     * finally {
     *    entry.writeUnLock();
     * }
     * </code></pre>
     * 
     * @return cached value or null if empty
     */
    public Object get() {
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
     * is re-enterant).
     * 
     * @param value
     */
    public void set(Object value) {
        try {
           lock.writeLock().acquire();
           this.value = value;
        } catch (InterruptedException e) {
            
        }
        finally {
            lock.writeLock().release();
        }
    }
    
    /**
     * Acquires a write lock. This will block other readers and writers (on this
     * entry only), and other readers and writers will need to be cleared before
     * the write lock can be acquired.
     */
    public boolean writeLock() {
        try {
            lock.writeLock().acquire();
        } catch (InterruptedException e) {
            return false;
        }
        return true;
    }

    public void writeUnLock() {
        lock.writeLock().release();
    }

}