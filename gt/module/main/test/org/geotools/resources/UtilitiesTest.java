/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2005, Geotools Project Managment Committee (PMC)
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
package org.geotools.resources;

// J2SE dependencies
import java.io.File;
import java.io.Serializable;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Tests the {@link Utilities} static methods.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class UtilitiesTest extends TestCase {
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

    /**
     * Tests {@link Utilities#sameInterfaces}.
     */
    public void testSameInterfaces() {
        assertTrue (Utilities.sameInterfaces(StringBuffer.class, String.class, CharSequence.class));
        assertTrue (Utilities.sameInterfaces(StringBuffer.class, String.class, Serializable.class));
        assertFalse(Utilities.sameInterfaces(        File.class, String.class, CharSequence.class));
        assertTrue (Utilities.sameInterfaces(        File.class, String.class, Serializable.class));
    }
}
