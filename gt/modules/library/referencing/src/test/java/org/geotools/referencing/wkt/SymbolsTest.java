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
package org.geotools.referencing.wkt;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Tests the {@link Symbols} implementation.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class SymbolsTest extends TestCase {
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
        return new TestSuite(SymbolsTest.class);
    }
    
    /**
     * Constructs a test case with the given name.
     */
    public SymbolsTest(final String name) {
        super(name);
    }

    /**
     * Tests the {@link Symbols#containsAxis} method.
     */
    public void testContainsAxis() {
        final Symbols s = Symbols.DEFAULT;
        assertTrue("AXIS at the begining of a line.",
                s.containsAxis("AXIS[\"Long\", EAST]"));
        assertTrue("AXIS embeded in GEOGCS.",
                s.containsAxis("GEOGCS[\"WGS84\", AXIS[\"Long\", EAST]]"));
        assertTrue("AXIS followed by spaces and different opening brace.",
                s.containsAxis("GEOGCS[\"WGS84\", AXIS (\"Long\", EAST)]"));
        assertTrue("AXIS in mixed cases.",
                s.containsAxis("GEOGCS[\"WGS84\", aXis[\"Long\", EAST]]"));
        assertFalse("AXIS in quoted text.",
                s.containsAxis("GEOGCS[\"AXIS\"]"));
        assertFalse("AXIS without opening bracket.",
                s.containsAxis("GEOGCS[\"WGS84\", AXIS]"));
        assertFalse("No AXIS.",
                s.containsAxis("GEOGCS[\"WGS84\"]"));
    }
}
