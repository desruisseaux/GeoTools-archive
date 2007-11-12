package org.geotools.util;

import org.geotools.util.ObjectCache;

import junit.framework.TestCase;

/**
 * Tests the WeakObjectCache with simple tests.
 * 
 * @author Cory Horner
 */
public class WeakObjectCacheTest extends TestCase {

    Integer  key1 = 1;
    Integer  key2 = 2;
    String value1 = new String("value 1");
    String value2 = new String("value 2");
    String value3 = new String("value 3");

    public void testSimple() {
        ObjectCache cache = new WeakObjectCache();
        assertNotNull(cache);

        assertEquals(null, cache.get(key1));

        cache.writeLock(key1);
        cache.put(key1, value1);
        cache.writeUnLock(key1);
        assertEquals(value1, cache.get(key1));

        assertEquals(null, cache.get(key2));
    }
    
    public void testConcurrent() throws InterruptedException {
        ObjectCache cache = new WeakObjectCache();
        
        //lock the cache as if we were writing
        cache.writeLock(key1);
        
        //create another thread which starts writing and blocks
        Runnable thread1 = new WriterThread(cache);
        Thread t1 = new Thread(thread1);
        t1.start();
        Thread.yield();

        //write
        cache.put(key1, value2);
        
        //check that the write thread was blocked
        Object[] values = ((WriterThread) thread1).getValue();
        assertEquals(null, values);
        assertEquals(value2, cache.peek(key1));

        //check that a separate write thread can get through
        cache.writeLock(key2);
        cache.put(key2, value3);
        cache.writeUnLock(key2);
        
        //unlock
        try {
            cache.writeUnLock(key1);
        } catch (Exception e) {
            fail("couldn't unlock");
        }

        //check that the write thread is unblocked
        t1.join();
        values = ((WriterThread) thread1).getValue();
        assertNotNull(values);
        assertEquals(value1, values[0]);

    }
    
    private class WriterThread implements Runnable {

        ObjectCache cache = null;
        Object[] values = null;
        
        public WriterThread(ObjectCache cache) {
            this.cache = cache;
        }
        
        public void run() {
            try {
                cache.writeLock(key1);
                cache.put(key1, value1);
                values = new Object[] {cache.get(key1)};
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    cache.writeUnLock(key1);
                } catch (Exception e) {
                    fail("couldn't unlock");
                }
            }
        }
        
        public Object[] getValue() {
            return values;
        }
    }

}
