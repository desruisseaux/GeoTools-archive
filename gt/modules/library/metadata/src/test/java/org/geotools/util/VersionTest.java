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

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Tests the {@link Version} class, especially the {@code compareTo} method.
 *
 * @since 2.4
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class VersionTest extends TestCase {
    /**
     * Run the suit from the command line.
     */
    public static void main(final String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(VersionTest.class);
    }
    
    /**
     * Constructs a test case with the given name.
     */
    public VersionTest(final String name) {
        super(name);
    }

    /**
     * Tests a numeric-only version.
     */
    public void testNumeric() {
        final Version version = new Version("6.11.2");
        assertEquals("6.11.2",        version.toString());
        assertEquals(new Integer( 6), version.getMajor());
        assertEquals(new Integer(11), version.getMinor());
        assertEquals(new Integer( 2), version.getRevision());
        assertSame(version.getRevision(), version.getComponent(2));
        assertNull(version.getComponent(3));
 
        assertTrue(version.compareTo(new Version("6.11.2")) == 0);
        assertTrue(version.compareTo(new Version("6.8"   )) >  0);
        assertTrue(version.compareTo(new Version("6.12.0")) <  0);
        assertTrue(version.compareTo(new Version("6.11"  )) >  0);
    }

    /**
     * Tests a alpha-numeric version.
     */
    public void testAlphaNumeric() {
        final Version version = new Version("1.6.b2");
        assertEquals("1.6.b2",        version.toString());
        assertEquals(new Integer( 1), version.getMajor());
        assertEquals(new Integer( 6), version.getMinor());
        assertEquals("b2",            version.getRevision());
        assertSame(version.getRevision(), version.getComponent(2));
        assertNull(version.getComponent(3));
 
        assertTrue(version.compareTo(new Version("1.6.b2")) == 0);
        assertTrue(version.compareTo(new Version("1.6.b1"))  > 0);
        assertTrue(version.compareTo(new Version("1.07.b1")) < 0);
    }
}
