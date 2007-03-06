/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2007, GeoTools Project Managment Committee (PMC)
 *    (C) 2007, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.referencing.cs;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// OpenGIS dependencies
import org.opengis.referencing.cs.AxisDirection;


/**
 * Tests the {@link DirectionAlongMeridian} class.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class DirectionAlongMeridianTest extends TestCase {
    /**
     * For floating point comparaisons.
     */
    private static final double EPS = 1E-10;

    /**
     * Run the suite from the command line.
     */
    public static void main(final String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(DirectionAlongMeridianTest.class);
    }

    /**
     * Constructs a test case with the given name.
     */
    public DirectionAlongMeridianTest(final String name) {
        super(name);
    }

    /**
     * Tests the {@link DirectionAlongMeridian#parse} method.
     */
    public void testParse() {
        DirectionAlongMeridian dir;
        String name;

        name = "South along 180 deg";
        dir  = DirectionAlongMeridian.parse(name);
        assertNotNull(dir);
        assertEquals(AxisDirection.SOUTH, dir.baseDirection);
        assertEquals(180, dir.meridian, 0);
        assertEquals(name, dir.toString());

        name = "South along 90 deg East";
        dir  = DirectionAlongMeridian.parse(name);
        assertNotNull(dir);
        assertEquals(AxisDirection.SOUTH, dir.baseDirection);
        assertEquals(90, dir.meridian, 0);
        assertEquals(name, dir.toString());

        name = "South along 90 deg West";
        dir  = DirectionAlongMeridian.parse(name);
        assertNotNull(dir);
        assertEquals(AxisDirection.SOUTH, dir.baseDirection);
        assertEquals(-90, dir.meridian, 0);
        assertEquals(name, dir.toString());

        name = "North along 45 deg East";
        dir  = DirectionAlongMeridian.parse(name);
        assertNotNull(dir);
        assertEquals(AxisDirection.NORTH, dir.baseDirection);
        assertEquals(45, dir.meridian, 0);
        assertEquals(name, dir.toString());
    }

    /**
     * Tests the ordering, which also involve a test of angle measurement.
     */
    public void testOrdering() {
        assertOrdered("North along  90 deg East",   "North along   0 deg");
        assertOrdered("North along  75 deg West",   "North along 165 deg West");
        assertOrdered("South along  90 deg West",   "South along   0 deg");
        assertOrdered("South along 180 deg",        "South along  90 deg West");
        assertOrdered("North along 130 deg West",   "North along 140 deg East");
    }

    /**
     * Tests if the following directions have an angle of 90° between each other.
     */
    private static void assertOrdered(final String dir1, final String dir2) {
        final DirectionAlongMeridian m1 = DirectionAlongMeridian.parse(dir1);
        final DirectionAlongMeridian m2 = DirectionAlongMeridian.parse(dir2);
        assertEquals(+90, m1.getAngle(m2), EPS);
        assertEquals(-90, m2.getAngle(m1), EPS);
        assertEquals( -1, m1.compareTo(m2));
        assertEquals( +1, m2.compareTo(m1));
        assertFalse (m1.equals(m2));
    }
}
