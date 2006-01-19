/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Managment Committee (PMC)
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

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Tests the {@link XMath} static methods.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class XMathTest extends TestCase {
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
        return new TestSuite(XMathTest.class);
    }

    /**
     * Constructs a test case with the given name.
     */
    public XMathTest(String name) {
        super(name);
    }

    /**
     * Tests the {@link XMath#pow10} method.
     */
    public void testPow10() {
        for (int i=-304; i<=304; i++) {
            assertEquals(Double.parseDouble("1E"+i), XMath.pow10(i), 0);
        }
    }

    /**
     * Tests the {@link XMath#countFractionDigits} method.
     */
    public void testCountFractionDigits() {
        assertEquals(0,   XMath.countFractionDigits(-65.0));
        assertEquals(1,   XMath.countFractionDigits(-65.5));
        assertEquals(6,   XMath.countFractionDigits( 65.123456));
        assertEquals(0,   XMath.countFractionDigits(1.23456E+200));
        assertEquals(1,   XMath.countFractionDigits(5E-1));
        assertEquals(5,   XMath.countFractionDigits(5E-5));
        assertEquals(10,  XMath.countFractionDigits(5E-10));
        assertEquals(200, XMath.countFractionDigits(5E-200));
        assertEquals(203, XMath.countFractionDigits(5.125E-200));
    }

    /**
     * Tests the {@link XMath#fixRoundingError} method.
     */
    public void testFixRoundingError() {
        assertEquals(-61.5,              XMath.fixRoundingError(-61.50000000000001, 12), 0);
        assertEquals(-61.5,              XMath.fixRoundingError(-61.50000000000001, 13), 0);
        assertEquals(-61.50000000000001, XMath.fixRoundingError(-61.50000000000001, 14), 0);
        assertEquals(-61.50000010000001, XMath.fixRoundingError(-61.50000010000001, 10), 0);
        assertEquals(-61.5,              XMath.fixRoundingError(-61.50000000000000, 15), 0);
    }
}
