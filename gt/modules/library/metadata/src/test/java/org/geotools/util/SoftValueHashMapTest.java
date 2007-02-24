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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.ConcurrentModificationException;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Tests {@link SoftValueHashMap}.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class SoftValueHashMapTest extends TestCase {
    /**
     * The size of the test sets to be created.
     */
    private static final int SAMPLE_SIZE = 200;

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
         return new TestSuite(SoftValueHashMapTest.class);
    }

    /**
     * Constructs a test case with the given name.
     */
    public SoftValueHashMapTest(final String name) {
        super(name);
    }

    /**
     * Tests the {@link SoftValueHashMap} using strong references. The tested
     * {@link SoftValueHashMap} should behave like a standard {@link Map} object.
     */
    public void testStrongReferences() {
        final Random random = new Random();
        for (int pass=0; pass<4; pass++) {
            final SoftValueHashMap softMap = new SoftValueHashMap();
            final HashMap        strongMap = new HashMap();
            for (int i=0; i<SAMPLE_SIZE; i++) {
                final Integer key   = new Integer(random.nextInt(SAMPLE_SIZE));
                final Integer value = new Integer(random.nextInt(SAMPLE_SIZE));
                assertEquals("containsKey:",   strongMap.containsKey(key),
                                                 softMap.containsKey(key));
                assertEquals("containsValue:", strongMap.containsValue(value),
                                                 softMap.containsValue(value));
                assertSame  ("get:",           strongMap.get(key),
                                                 softMap.get(key));
                if (random.nextBoolean()) {
                    // Test addition.
                    assertSame("put:", strongMap.put(key, value),
                                         softMap.put(key, value));
                } else {
                    // Test remove
                    assertSame("remove:", strongMap.remove(key),
                                            softMap.remove(key));
                }
                assertEquals("equals:", strongMap, softMap);
            }
        }
    }

    /**
     * Tests the {@link SoftValueHashMap} using soft references.
     * In this test, we have to keep in mind than some elements
     * in {@code softMap} may disaspear at any time.
     */
    public void testSoftReferences() throws InterruptedException {
        final Random random = new Random();
        final SoftValueHashMap softMap = new SoftValueHashMap();
        final HashMap strongMap = new HashMap();
        for (int pass=0; pass<2; pass++) {
            int count = 0;
            softMap.clear();
            strongMap.clear();
            for (int i=0; i<SAMPLE_SIZE; i++) {
                final Integer key   = new Integer(random.nextInt(SAMPLE_SIZE));
                final Integer value = new Integer(random.nextInt(SAMPLE_SIZE));
                if (random.nextBoolean()) {
                    /*
                     * Test addition.
                     */
                    final Object   softPrevious = softMap  .put(key, value);
                    final Object strongPrevious = strongMap.put(key, value);
                    if (softPrevious == null) {
                        // If the element was not in the SoftValueHashMap (i.e. if the garbage
                        // collector has cleared it), then it must not been in HashMap neither
                        // (otherwise GC should not have cleared it).
                        assertNull("put:", strongPrevious);
                        count++; // Count only the new values.
                    } else {
                        assertNotSame(value, softPrevious);
                    }
                    if (strongPrevious != null) {
                        // Note: If 'strongPrevious==null', 'softPrevious' may not
                        //       be null if GC has not collected its entry yet.
                        assertSame("put:", strongPrevious, softPrevious);
                    }
                } else {
                    /*
                     * Test remove
                     */
                    final Object   softPrevious = softMap.get(key);
                    final Object strongPrevious = strongMap.remove(key);
                    if (strongPrevious != null) {
                        assertSame("remove:", strongPrevious, softPrevious);
                    }
                }
                assertTrue("containsAll:", softMap.entrySet().containsAll(strongMap.entrySet()));
            }
            // Do our best to lets GC finish its work.
            for (int i=0; i<4; i++) {
                Thread.sleep(50);
                System.gc();
            }
            assertTrue(softMap.isValid());
            assertTrue("size:", softMap.size() <= count);
            /*
             * Make sure that all values are of the correct type. More specifically, we
             * want to make sure that we didn't forget to convert some Reference object.
             */
            for (final Iterator it=softMap.values().iterator(); it.hasNext();) {
                final Object value;
                try {
                    value = it.next();
                } catch (ConcurrentModificationException e) {
                    break;
                }
                assertTrue(value instanceof Integer);
                assertNotNull(value);
            }
        }
    }
}
