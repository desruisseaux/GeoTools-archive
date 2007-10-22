package org.geotools.util;

import org.geotools.util.DefaultObjectCache;
import org.geotools.util.ObjectCache;

import junit.framework.TestCase;

/**
 * Tests the DefaultObjectCache with simple tests.
 * 
 * @author Cory Horner
 */
public class DefaultObjectCacheTest extends TestCase {

    Integer key1 = new Integer(1);
    Integer key2 = new Integer(2);
    String value1 = new String("value 1");
    String value2 = new String("value 2");

    public void testSimple() {
        ObjectCache cache = new DefaultObjectCache();
        assertNotNull(cache);

        assertEquals(null, cache.get(key1));

        cache.writeLock(key1);
        cache.put(key1, value1);
        cache.writeUnLock(key1);
        assertEquals(value1, cache.get(key1));

        assertEquals(null, cache.get(key2));
    }
}
