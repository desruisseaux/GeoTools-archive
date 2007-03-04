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
 * Tests the {@link DefaultCoordinateSystemAxis} class.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class DefaultCoordinateSystemAxisTest extends TestCase {
    /**
     * For floating point number comparaisons.
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
        return new TestSuite(DefaultCoordinateSystemAxisTest.class);
    }

    /**
     * Constructs a test case with the given name.
     */
    public DefaultCoordinateSystemAxisTest(final String name) {
        super(name);
    }

    /**
     * Makes sure that the compass directions in {@link AxisDirection} are okay.
     *
     * @todo Use "static import" when we will be allowed to compile for J2SE 1.5.
     */
    public void testCompass() {
        final AxisDirection[] compass = new AxisDirection[] {
            AxisDirection.NORTH,
            AxisDirection.NORTH_NORTH_EAST,
            AxisDirection.NORTH_EAST,
            AxisDirection.EAST_NORTH_EAST,
            AxisDirection.EAST,
            AxisDirection.EAST_SOUTH_EAST,
            AxisDirection.SOUTH_EAST,
            AxisDirection.SOUTH_SOUTH_EAST,
            AxisDirection.SOUTH,
            AxisDirection.SOUTH_SOUTH_WEST,
            AxisDirection.SOUTH_WEST,
            AxisDirection.WEST_SOUTH_WEST,
            AxisDirection.WEST,
            AxisDirection.WEST_NORTH_WEST,
            AxisDirection.NORTH_WEST,
            AxisDirection.NORTH_NORTH_WEST
        };
        assertEquals(compass.length, DefaultCoordinateSystemAxis.COMPASS_DIRECTION_COUNT);
        final int base = AxisDirection.NORTH.ordinal();
        final int h = compass.length / 2;
        for (int i=0; i<compass.length; i++) {
            final String index = "compass[" + i +']';
            final AxisDirection c = compass[i];
            double angle = i * (360.0/compass.length);
            if (angle > 180) {
                angle -= 360;
            }
            assertEquals(index, base + i, c.ordinal());
            assertEquals(index, base + i + (i<h ? h : -h), c.opposite().ordinal());
            assertEquals(index, 0, DefaultCoordinateSystemAxis.getAngle(c, c), EPS);
            assertEquals(index, 180, Math.abs(DefaultCoordinateSystemAxis.getAngle(c, c.opposite())), EPS);
            assertEquals(index, angle, DefaultCoordinateSystemAxis.getAngle(c, AxisDirection.NORTH), EPS);
        }
    }

    /**
     * Tests {@link DefaultCoordinateSystemAxis#getAngle}.
     */
    public void testAngle() {
        assertEquals( 90.0, DefaultCoordinateSystemAxis.getAngle(AxisDirection.WEST, AxisDirection.SOUTH), EPS);
        assertEquals(-90.0, DefaultCoordinateSystemAxis.getAngle(AxisDirection.SOUTH, AxisDirection.WEST), EPS);
        assertEquals( 45.0, DefaultCoordinateSystemAxis.getAngle(AxisDirection.SOUTH, AxisDirection.SOUTH_EAST), EPS);
        assertEquals(-22.5, DefaultCoordinateSystemAxis.getAngle(AxisDirection.NORTH_NORTH_WEST, AxisDirection.NORTH), EPS);
    }

    /**
     * Tests {@link DefaultCoordinateSystemAxis#getAngle} using textual directions.
     */
    public void testAngle2() {
        compareAngle( 90.0, "West", "South");
        compareAngle(-90.0, "South", "West");
        compareAngle( 45.0, "South", "South-East");
        compareAngle(-22.5, "North-North-West", "North");
        compareAngle(-22.5, "North_North_West", "North");
        compareAngle(-22.5, "North North West", "North");
        compareAngle( 90.0, "North along 90 deg East", "North along 0 deg");
        compareAngle( 90.0, "South along 180 deg", "South along 90 deg West");
    }

    /**
     * Compare the angle between the specified directions.
     */
    private static void compareAngle(final double expected, final String source, final String target) {
        final AxisDirection dir1 = DefaultCoordinateSystemAxis.getDirection(source);
        final AxisDirection dir2 = DefaultCoordinateSystemAxis.getDirection(target);
        assertNotNull(dir1);
        assertNotNull(dir2);
        assertEquals(expected, DefaultCoordinateSystemAxis.getAngle(dir1, dir2), EPS);
    }
}
