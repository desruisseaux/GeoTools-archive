package org.geotools.referencing.factory;

import EDU.oswego.cs.dl.util.concurrent.ReadWriteLock;
import EDU.oswego.cs.dl.util.concurrent.ReentrantWriterPreferenceReadWriteLock;


/**
 * An entry in the ReferencingObjectCache.
 * 
 * To use as a reader:
 * 
 * <pre><code>
 * entry.get();
 * </code></pre>
 * 
 * To use as a writer:
 * 
 * <pre><code>
 * entry.lock();
 * //determine value
 * entry.set(newValue);
 * </code></pre>
 * 
 * @since 2.4
 * @author Cory Horner (Refractions Research)
 * @author Jody Garnett (Refractions Research)
 * @todo change from edu.oswego to java.concurrent
 */
public class ObjectCacheEntry {
    
    private volatile Object value;
    private volatile ReadWriteLock lock = new ReentrantWriterPreferenceReadWriteLock();

    public ObjectCacheEntry() {
        this.value = null;
    }
    
    public ObjectCacheEntry(Object value) {
        this.value = value;
    }

    /**
     * Non-blocking determination if a value exists in this entry.
     */
    public boolean containsValue() {
        return (value == null) ? false : true;
    }
    
    /**
     * Acquires a read lock, obtains the value, unlocks, and returns the value.
     * If another thread already has the read lock or the write lock, this
     * method will block.
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
     * Stores the value in the entry.  This method expects writeLock to be called before it.
     * 
     * @param value
     */
    public void set(Object value) {
        this.value = value;
        lock.writeLock().release();
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