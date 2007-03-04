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

// J2SE dependencies and extensions
import javax.units.SI;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// OpenGIS dependencies
import org.opengis.referencing.cs.AxisDirection;


/**
 * Tests the {@link CartesianCS} class.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class DefaultCartesianCSTest extends TestCase {
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
        return new TestSuite(DefaultCartesianCSTest.class);
    }

    /**
     * Constructs a test case with the given name.
     */
    public DefaultCartesianCSTest(final String name) {
        super(name);
    }

    /**
     * Tests the creation of a cartesian CS with legal and illegal axis.
     */
    public void testAxis() {
        DefaultCartesianCS cs;
        try {
            cs = new DefaultCartesianCS("Test",
                    DefaultCoordinateSystemAxis.LONGITUDE,
                    DefaultCoordinateSystemAxis.LATITUDE);
            fail("Angular units should not be accepted.");
        } catch (IllegalArgumentException e) {
            // Expected exception: illegal angular units.
        }

        // Legal CS (the most usuan one).
        cs = new DefaultCartesianCS("Test",
                DefaultCoordinateSystemAxis.EASTING,
                DefaultCoordinateSystemAxis.NORTHING);

        try {
            cs = new DefaultCartesianCS("Test",
                    DefaultCoordinateSystemAxis.SOUTHING,
                    DefaultCoordinateSystemAxis.NORTHING);
            fail("Colinear units should not be accepted.");
        } catch (IllegalArgumentException e) {
            // Expected exception: colinear axis.
        }

        // Legal CS rotated 45°
        cs = create(AxisDirection.NORTH_EAST, AxisDirection.SOUTH_EAST);

        try {
            cs = create(AxisDirection.NORTH_EAST, AxisDirection.EAST);
            fail("Non-perpendicular axis should not be accepted.");
        } catch (IllegalArgumentException e) {
            // Expected exception: non-perpendicular axis.
        }

        // Legal CS, but no perpendicularity check.
        cs = create(AxisDirection.NORTH_EAST, AxisDirection.UP);
    }

    /**
     * Creates a coordinate system with the specified axis directions.
     */
    private static DefaultCartesianCS create(final AxisDirection x, final AxisDirection y) {
        return new DefaultCartesianCS("Test",
                new DefaultCoordinateSystemAxis("x", x, SI.METER),
                new DefaultCoordinateSystemAxis("y", y, SI.METER));
    }
}
