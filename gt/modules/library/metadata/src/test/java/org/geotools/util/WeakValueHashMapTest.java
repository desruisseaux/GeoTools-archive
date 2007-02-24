/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, Geotools Project Managment Committee (PMC)
 *    (C) 2001, Institut de Recherche pour le Développement
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Test the {@link WeakHashSet}. A standard {@link HashSet} object
 * is used for comparaison purpose.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class WeakValueHashMapTest extends TestCase {
    /**
     * The size of the test sets to be created.
     */
    private static final int SAMPLE_SIZE = 500;

    /**
     * Run the suit from the command line.
     */
    public static void main(final String[] args) {
        org.geotools.util.Logging.GEOTOOLS.forceMonolineConsoleOutput();
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Returns the test suite.
     */
    public static Test suite() {
         return new TestSuite(WeakValueHashMapTest.class);
    }

    /**
     * Constructs a test case with the given name.
     */
    public WeakValueHashMapTest(final String name) {
        super(name);
    }

    /**
     * Tests the {@link WeakValueHashMap} using strong references.
     * The tested {@link WeakValueHashMap} should behave like a
     * standard {@link Map} object.
     */
    public void testStrongReferences() {
        final Random random = new Random();
        for (int pass=0; pass<4; pass++) {
            final WeakValueHashMap weakMap = new WeakValueHashMap();
            final HashMap        strongMap = new HashMap();
            for (int i=0; i<SAMPLE_SIZE; i++) {
                final Integer key   = new Integer(random.nextInt(SAMPLE_SIZE));
                final Integer value = new Integer(random.nextInt(SAMPLE_SIZE));
                assertEquals("containsKey:",   strongMap.containsKey(key),
                                                 weakMap.containsKey(key));
                if (false) {
                    // Can't test this one, since 'WeakValueHashMap.entrySet()' is not implemented.
                    assertEquals("containsValue:", strongMap.containsValue(value),
                                                     weakMap.containsValue(value));
                }
                assertSame("get:", strongMap.get(key),
                                     weakMap.get(key));
                if (random.nextBoolean()) {
                    // Test addition.
                    assertSame("put:", strongMap.put(key, value),
                                         weakMap.put(key, value));
                } else {
                    // Test remove
                    assertSame("remove:", strongMap.remove(key),
                                            weakMap.remove(key));
                }
                assertEquals("equals:", strongMap, weakMap);
            }
        }
    }

    /**
     * Tests the {@link WeakValueHashMap} using weak references.
     * In this test, we have to keep in mind than some elements
     * in {@code weakMap} may disaspear at any time.
     */
    public void testWeakReferences() throws InterruptedException {
        final Random random = new Random();
        for (int pass=0; pass<2; pass++) {
            final WeakValueHashMap weakMap = new WeakValueHashMap();
            final HashMap        strongMap = new HashMap();
            for (int i=0; i<SAMPLE_SIZE; i++) {
                final Integer key   = new Integer(random.nextInt(SAMPLE_SIZE));
                final Integer value = new Integer(random.nextInt(SAMPLE_SIZE));
                if (random.nextBoolean()) {
                    /*
                     * Test addition.
                     */
                    final Object   weakPrevious = weakMap  .put(key, value);
                    final Object strongPrevious = strongMap.put(key, value);
                    if (weakPrevious == null) {
                        // If the element was not in the WeakValueHashMap (i.e. if the garbage
                        // collector has cleared it), then it must not been in HashMap neither
                        // (otherwise GC should not have cleared it).
                        assertNull("put:", strongPrevious);
                    } else {
                        assertNotSame(value, weakPrevious);
                    }
                    if (strongPrevious != null) {
                        // Note: If 'strongPrevious==null', 'weakPrevious' may not
                        //       be null if GC has not collected its entry yet.
                        assertSame("put:", strongPrevious, weakPrevious);
                    }
                } else {
                    /*
                     * Test remove
                     */
                    final Object   weakPrevious = weakMap.get(key);
                    final Object strongPrevious = strongMap.remove(key);
                    if (strongPrevious != null) {
                        assertSame("remove:", strongPrevious, weakPrevious);
                    }
                }
                if (false) {
                    // Can't test this one, since 'WeakValueHashMap.entrySet()' is not implemented.
                    assertTrue("containsAll:", weakMap.entrySet().containsAll(strongMap.entrySet()));
                }
            }
            // Do our best to lets GC finish its work.
            for (int i=0; i<4; i++) {
                Thread.sleep(50);
                System.gc();
            }
            assertTrue("equals:", strongMap.equals(weakMap));
        }
    }
}
