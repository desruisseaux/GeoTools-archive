/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, Geotools Project Managment Committee (PMC)
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
package org.geotools.resources;

import java.io.File;
import java.io.Serializable;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Tests the {@link Utilities} static methods.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class UtilitiesTest extends TestCase {
    /**
     * Run the test from the command line.
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(UtilitiesTest.class);
    }

    /**
     * Constructs a test case with the given name.
     */
    public UtilitiesTest(String name) {
        super(name);
    }

    /**
     * Tests {@link Utilities#equals}.
     */
    public void testEquals() {
        assertTrue (Utilities.equals(null, null));
        assertFalse(Utilities.equals(null, ""  ));
        assertFalse(Utilities.equals(""  , null));
        assertTrue (Utilities.equals(""  , ""  ));
        assertFalse(Utilities.equals(" " , ""  ));
    }

    /**
     * Tests {@link Utilities#spaces}.
     */
    public void testSpaces() {
        assertEquals("",         Utilities.spaces(0));
        assertEquals(" ",        Utilities.spaces(1));
        assertEquals("        ", Utilities.spaces(8));
    }
}
