package org.geotools.util;

import junit.framework.TestCase;

/**
 * Simple deadlock tests for ObjectCacheEntry.
 * 
 * @author Cory Horner (Refractions Research)
 */
public class ObjectCacheEntryTest extends TestCase {

    DefaultObjectCache.ObjectCacheEntry entry;
    
    private class EntryReaderThread implements Runnable {

        Object[] values = null;
        
        public EntryReaderThread() {
        }
        
        public void run() {
            try {
                values = new Object[] {entry.getValue()};
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        public Object[] getValue() {
            return values;
        }
    }
    
    private class EntryWriterThread implements Runnable {

        Object[] values = null;
        
        public EntryWriterThread() {
        }
        
        public void run() {
            try {
                entry.writeLock();
                entry.setValue(1);
                entry.writeUnLock();
                values = new Object[] {entry.getValue()};
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        public Object[] getValue() {
            return values;
        }
    }

    public void testWriteReadDeadlock() throws InterruptedException {
        //lock the entry as if we were writing
        entry = new DefaultObjectCache.ObjectCacheEntry();
        entry.writeLock();
        
        //create another thread which starts reading
        Runnable thread1 = new EntryReaderThread();
        Thread t1 = new Thread(thread1);
        t1.start();
        Thread.yield();

        //write
        entry.setValue(1);
        
        //check that the read thread was blocked
        Object[] values = ((EntryReaderThread) thread1).getValue();
        assertEquals(null, values);
        
        //unlock
        entry.writeUnLock();

        //check that the read thread is unblocked
        t1.join();
        values = ((EntryReaderThread) thread1).getValue();
        assertNotNull(values);
        assertEquals(1, values[0]);
    }

    public void testWriteWriteDeadlock() throws InterruptedException {
        //lock the entry as if we were writing
        entry = new DefaultObjectCache.ObjectCacheEntry();
        entry.writeLock();

        //write the value 2
        entry.setValue(2);
        
        //create another thread which starts writing
        Runnable thread1 = new EntryWriterThread();
        Thread t1 = new Thread(thread1);
        t1.start();
        Thread.yield();

        //check that the write thread was blocked
        Object[] values = ((EntryWriterThread) thread1).getValue();
        assertNull(values);
        assertEquals(2, entry.getValue());
        
        //unlock
        entry.writeUnLock();

        //check that the write thread is unblocked
        t1.join();
        values = ((EntryWriterThread) thread1).getValue();
        assertNotNull(values);
        assertEquals(1, values[0]);
    }
}
