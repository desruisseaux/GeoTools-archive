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
package org.geotools.geometry;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.geotools.referencing.crs.DefaultGeographicCRS;


/**
 * Tests the {@link GeneralDirectPosition} and {@link DirectPosition2D} classes.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class DirectPositionTest extends TestCase {
    /**
     * Run the suite from the command line.
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(DirectPositionTest.class);
    }

    /**
     * Constructs a test case with the given name.
     */
    public DirectPositionTest(final String name) {
        super(name);
    }

    /**
     * Tests {@link GeneralDirectPosition#equals} method between different implementations. The
     * purpose of this test is also to run the assertion in the direct position implementations.
     */
    public void testEquals() {
        assertTrue(GeneralDirectPosition.class.desiredAssertionStatus());
        assertTrue(DirectPosition2D.class.desiredAssertionStatus());

        CoordinateReferenceSystem WGS84 = DefaultGeographicCRS.WGS84;
        DirectPosition p1 = new DirectPosition2D(WGS84, 48.543261561072285, -123.47009555832284);
        GeneralDirectPosition p2 = new GeneralDirectPosition(48.543261561072285, -123.47009555832284);
        assertFalse(p1.equals(p2));
        assertFalse(p2.equals(p1));

        p2.setCoordinateReferenceSystem(WGS84);
        assertTrue(p1.equals(p2));
        assertTrue(p2.equals(p1));
    }
}
