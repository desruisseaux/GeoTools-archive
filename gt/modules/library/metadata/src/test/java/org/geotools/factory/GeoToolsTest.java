/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2007, Geotools Project Managment Committee (PMC)
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
package org.geotools.factory;

import java.util.HashMap;
import java.util.Map;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Tests {@link GeoTools}.
 *
 * @since 2.4
 * @source $URL$
 * @version $Id$
 * @author Jody Garnett
 * @author Martin Desruisseaux
 */
public class GeoToolsTest extends TestCase {
    /**
     * Run the suite from the command line.
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(GeoToolsTest.class);
    }

    /**
     * Constructs a test case.
     */
    public GeoToolsTest(final String testName) {
        super(testName);
    }

    /**
     * Make sures that J2SE 1.4 assertions are enabled.
     */
    public void testAssertionEnabled() {
        assertTrue("Assertions not enabled.", GeoToolsTest.class.desiredAssertionStatus());
    }

    /**
     * Tests addition of custom hints.
     */
    public void testMyHints(){
        Hints hints = GeoTools.getDefaultHints();
        assertTrue(hints.isEmpty());
        assertNull(Hints.putSystemDefault(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.FALSE));
        try {
            hints = GeoTools.getDefaultHints();
            assertNotNull(hints);
            assertFalse(hints.isEmpty());
            assertEquals(1, hints.size());
            final Object value = hints.get(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER);
            assertTrue(value instanceof Boolean);
            assertFalse(((Boolean) value).booleanValue());
            /*
             * Tests the toString() method.
             */
            String text = hints.toString().trim();
            assertTrue(text.matches("Hints:\\s+FORCE_LONGITUDE_FIRST_AXIS_ORDER = false"));

            assertEquals(hints.put(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE), Boolean.FALSE);
            text = hints.toString().trim();
            assertTrue(text.matches("Hints:\\s+FORCE_LONGITUDE_FIRST_AXIS_ORDER = true"));

            assertEquals(hints.remove(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER), Boolean.TRUE);
            text = hints.toString().trim();
            assertTrue(text.matches("Hints:\\s+System defaults:\\s+FORCE_LONGITUDE_FIRST_AXIS_ORDER = false"));
        } finally {
            assertNotNull(Hints.removeSystemDefault(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER));
        }
        assertTrue(GeoTools.getDefaultHints().isEmpty());
    }

    /**
     * Tests the use of system properties.
     *
     * @todo Uncomment when we will be allowed to compile for J2SE 1.5.
     *       Call to {@link System#clearProperty} is mandatory for this test.
     */
//    public void testSystemHints() {
//        Hints hints = GeoTools.getDefaultHints();
//        assertNotNull(hints);
//        assertTrue(hints.isEmpty());
//        System.setProperty(GeoTools.FORCE_LONGITUDE_FIRST_AXIS_ORDER, "true");
//        assertTrue ("Property change should have been detected.", Hints.scanSystemProperties());
//        assertFalse("No more property should have been changed.", Hints.scanSystemProperties());
//        try {
//            hints = GeoTools.getDefaultHints();
//            assertNotNull(hints);
//            assertFalse(hints.isEmpty());
//            assertEquals(1, hints.size());
//            final Object value = hints.get(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER);
//            assertTrue(value instanceof Boolean);
//            assertTrue(((Boolean) value).booleanValue());
//        } finally {
//            System.clearProperty(GeoTools.FORCE_LONGITUDE_FIRST_AXIS_ORDER);
//            assertNotNull(Hints.removeSystemDefault(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER));
//        }
//        hints = GeoTools.getDefaultHints();
//        assertNotNull(hints);
//        assertTrue(hints.isEmpty());
//    }
}
