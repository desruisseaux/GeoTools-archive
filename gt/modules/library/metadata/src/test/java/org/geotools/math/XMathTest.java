/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, Geotools Project Managment Committee (PMC)
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
package org.geotools.math;

import java.util.Arrays;
import org.geotools.resources.XArray;
import static org.geotools.math.XMath.*;

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
public final class XMathTest extends TestCase {
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
            assertEquals(Double.parseDouble("1E"+i), pow10(i), 0);
        }
    }

    /**
     * Tests the {@link XMath#countFractionDigits} method.
     */
    public void testCountDecimalFractionDigits() {
        assertEquals(0,   countDecimalFractionDigits(-65.0));
        assertEquals(1,   countDecimalFractionDigits(-65.5));
        assertEquals(6,   countDecimalFractionDigits( 65.123456));
        assertEquals(0,   countDecimalFractionDigits(1.23456E+200));
        assertEquals(1,   countDecimalFractionDigits(5E-1));
        assertEquals(5,   countDecimalFractionDigits(5E-5));
        assertEquals(10,  countDecimalFractionDigits(5E-10));
        assertEquals(200, countDecimalFractionDigits(5E-200));
        assertEquals(203, countDecimalFractionDigits(5.125E-200));
    }

    /**
     * Tests the {@link XMath#fixRoundingError} method.
     */
    public void testTrimDecimalFractionDigits() {
        assertEquals(-61.5,              trimDecimalFractionDigits(-61.50000000000001, 4, 12), 0);
        assertEquals(-61.5,              trimDecimalFractionDigits(-61.50000000000001, 4, 13), 0);
        assertEquals(-61.50000000000001, trimDecimalFractionDigits(-61.50000000000001, 4, 14), 0);
        assertEquals(-61.50000010000001, trimDecimalFractionDigits(-61.50000010000001, 4, 10), 0);
        assertEquals(-61.5,              trimDecimalFractionDigits(-61.50000000000000, 4, 15), 0);
    }

    /**
     * Tests the {@link XMath#primeNumber} method.
     */
    public void testPrimeNumber() {
        final int[] primes = {
            2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53,
            59, 61, 67, 71, 73, 79, 83, 89, 97, 101, 103, 107, 109, 113
        };
        for (int i=0; i<primes.length; i++) {
            assertEquals(primes[i], primeNumber(i));
        }
    }

    /**
     * Tests the {@link XMath#divisors} method.
     */
    public void testDivisors() {
        for (int i=0; i<10000; i++) {
            final int[] divisors = divisors(i);
            assertTrue(XArray.isStrictlySorted(divisors));
            for (int j=0; j<divisors.length; j++) {
                assertEquals(0, i % divisors[j]);
            }
            if (i == 0){
                assertEquals(0, divisors.length);
            } else {
                assertEquals(1, divisors[0]);
                assertEquals(i, divisors[divisors.length - 1]);
            }
        }
        assertTrue(Arrays.equals(divisors(2000), new int[] {
            1, 2, 4, 5, 8, 10, 16, 20, 25, 40, 50, 80, 100, 125, 200, 250, 400, 500, 1000, 2000
        }));
        assertTrue(Arrays.equals(divisors(4331), new int[] {
            1, 61, 71, 4331
        }));
        assertTrue(Arrays.equals(divisors(7800), new int[] {
            1, 2, 3, 4, 5, 6, 8, 10, 12, 13, 15, 20, 24, 25, 26, 30, 39, 40, 50, 52, 60, 65, 75,
            78, 100, 104, 120, 130, 150, 156, 195, 200, 260, 300, 312, 325, 390, 520, 600, 650,
            780, 975, 1300, 1560, 1950, 2600, 3900, 7800
        }));
    }
}
