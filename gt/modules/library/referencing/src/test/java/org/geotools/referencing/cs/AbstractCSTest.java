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
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.operation.Matrix;

// Geotools dependencies
import org.geotools.referencing.operation.matrix.GeneralMatrix;


/**
 * Tests the {@link AbstractCS} class.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class AbstractCSTest extends TestCase {
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
        return new TestSuite(AbstractCSTest.class);
    }

    /**
     * Constructs a test case with the given name.
     */
    public AbstractCSTest(final String name) {
        super(name);
    }

    /**
     * Tests the swapping of axis.
     */
    public void testAxisSwapping() {
        CoordinateSystem cs1, cs2;
        cs1 = new DefaultEllipsoidalCS("cs1",
                DefaultCoordinateSystemAxis.GEODETIC_LONGITUDE,
                DefaultCoordinateSystemAxis.GEODETIC_LATITUDE);
        cs2 = new DefaultEllipsoidalCS("cs2",
                DefaultCoordinateSystemAxis.GEODETIC_LATITUDE,
                DefaultCoordinateSystemAxis.GEODETIC_LONGITUDE);
        assertTrue(AbstractCS.swapAndScaleAxis(cs1, cs1).isIdentity());
        assertTrue(AbstractCS.swapAndScaleAxis(cs2, cs2).isIdentity());
        compareMatrix(cs1, cs2, new double[] {
            0, 1, 0,
            1, 0, 0,
            0, 0, 1
        });

        cs1 = new DefaultEllipsoidalCS("cs1",
                DefaultCoordinateSystemAxis.GEODETIC_LONGITUDE,
                DefaultCoordinateSystemAxis.GEODETIC_LATITUDE,
                DefaultCoordinateSystemAxis.ELLIPSOIDAL_HEIGHT);
        cs2 = new DefaultEllipsoidalCS("cs2",
                DefaultCoordinateSystemAxis.GEODETIC_LATITUDE,
                DefaultCoordinateSystemAxis.GEODETIC_LONGITUDE,
                DefaultCoordinateSystemAxis.ELLIPSOIDAL_HEIGHT);
        compareMatrix(cs1, cs2, new double[] {
            0, 1, 0, 0,
            1, 0, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1
        });

        cs1 = new DefaultCartesianCS("cs1",
                DefaultCoordinateSystemAxis.ELLIPSOIDAL_HEIGHT,
                DefaultCoordinateSystemAxis.EASTING,
                DefaultCoordinateSystemAxis.NORTHING);
        cs2 = new DefaultCartesianCS("cs2",
                DefaultCoordinateSystemAxis.SOUTHING,
                DefaultCoordinateSystemAxis.EASTING,
                DefaultCoordinateSystemAxis.ELLIPSOIDAL_HEIGHT);
        compareMatrix(cs1, cs2, new double[] {
            0, 0,-1, 0,
            0, 1, 0, 0,
            1, 0, 0, 0,
            0, 0, 0, 1
        });
    }

    /**
     * Compares the matrix computes by {@link AbstractCS#swapAndScaleAxis} with the specified one.
     */
    private static void compareMatrix(final CoordinateSystem cs1,
                                      final CoordinateSystem cs2,
                                      final double[] expected)
    {
        final Matrix matrix = AbstractCS.swapAndScaleAxis(cs1, cs2);
        final int numRow = matrix.getNumRow();
        final int numCol = matrix.getNumCol();
        assertEquals(expected.length, numRow*numCol);
        final Matrix em = new GeneralMatrix(numRow, numCol, expected);
        assertEquals(em, matrix);
    }
}
