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
package org.geotools.util.logging;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Tests the {@link Logging} class.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class LoggingTest extends TestCase {
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
        return new TestSuite(LoggingTest.class);
    }

    /**
     * Constructs a test case with the given name.
     */
    public LoggingTest(final String name) {
        super(name);
    }

    /**
     * Checks {@link Logging#GEOTOOLS}.
     */
    public void testGeotools() {
        assertEquals("",             Logging.ALL.name);
        assertEquals("org.geotools", Logging.GEOTOOLS.name);
        assertEquals(0,              Logging.GEOTOOLS.getChildren().length);
        Logging[] children =         Logging.ALL.getChildren();
        assertEquals(1,              children.length);
        assertEquals("org",          children[0].name);
        assertSame(children[0],      Logging.getLogging("org"));
        children =                   children[0].getChildren();
        assertEquals(1,              children.length);
        assertSame(Logging.GEOTOOLS, children[0]);
        assertSame(Logging.ALL,      Logging.getLogging(""));
        assertSame(Logging.GEOTOOLS, Logging.getLogging("org.geotools"));
    }
}
