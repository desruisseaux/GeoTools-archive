/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2006, Geotools Project Managment Committee (PMC)
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
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.geotools.factory;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Tests {@link AbstractFactory}.
 *
 * @since 2.3
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class AbstractFactoryTest extends TestCase {
    /**
     * Run the suite from the command line.
     */
    public static void main(String[] args) {
        org.geotools.util.MonolineFormatter.initGeotools();
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(AbstractFactoryTest.class);
    }

    /**
     * Constructs a test case.
     */
    public AbstractFactoryTest(final String testName) {
        super(testName);
    }

    /**
     * Tests {@link AbstractFactory#equals}.
     */
    public void testEquals() {
        final AbstractFactory f1 = new AbstractFactory();
        final AbstractFactory f2 = new AbstractFactory();
        final AbstractFactory f3 = new AbstractFactory();
        f1.hints.put("Key 1",       "Value 1");
        f2.hints.put("Key 2",       "Value 2");
        f3.hints.put("Key 3 reference f1", f1);
        f2.hints.put("Key 2 reference f3", f3);
        f1.hints.put("Key 1 reference f2", f2);

        assertFalse(f1.toString().length() == 0);

        assertEquals(f1, f1);
        assertEquals(f2, f2);
        assertEquals(f3, f3);
        assertFalse (f1.equals(f2));  // Different number of hints.
        assertFalse (f1.equals(f3));  // Same number of hints, differerent key.
        assertFalse (f2.equals(f3));  // Different number of hints.

        // Tests recursivity on a f2 --> f3 --> f1 --> f2 dependency graph.
        final AbstractFactory f1b = new AbstractFactory();
        final AbstractFactory f2b = new AbstractFactory();
        final AbstractFactory f3b = new AbstractFactory();
        f1b.hints.put("Key 1",        "Value 1");
        f2b.hints.put("Key 2",        "Value 2");
        f3b.hints.put("Key 3 reference f1", f1b);
        f2b.hints.put("Key 2 reference f3", f3b);
        f1b.hints.put("Key 1 reference f2", f2b);
        assertEquals(f2, f2b);

        f1b.hints.put("Key 1", "Different value");
        assertFalse(f2.equals(f2b));
    }
}
