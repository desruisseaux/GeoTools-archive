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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Tests the {@link DisjointSet} class.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class DisjointSetTest extends TestCase {
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
        return new TestSuite(DisjointSetTest.class);
    }

    /**
     * Constructs a test case with the given name.
     */
    public DisjointSetTest(final String name) {
        super(name);
    }

     /**
      * Test the set.
      */
     public void testDisjointSet() {
        DisjointSet<String> t1 = new DisjointSet<String>(true);
        DisjointSet<String> t2 = new DisjointSet<String>(t1);
        DisjointSet<String> t3 = new DisjointSet<String>(t2);

        assertNotNull(t1.getTrash());
        assertSame(t1.getTrash(), t2.getTrash());
        assertSame(t2.getTrash(), t3.getTrash());

        assertTrue(t1.add("alpha"));
        assertTrue(t2.add("bêta"));
        assertTrue(t3.add("gamma"));
        assertTrue(t2.add("delta"));
        assertTrue(t1.add("epsilon"));
        assertTrue(t2.add("alpha"));
        assertTrue(t2.remove("bêta"));

        assertEquals(Collections.singleton("epsilon"), t1);
        assertEquals(new HashSet<String>(Arrays.asList(new String[] {"alpha","delta"})), t2);
        assertEquals(Collections.singleton("gamma"), t3);
        assertEquals(Collections.singleton("bêta"),  t1.getTrash());
    }
}
