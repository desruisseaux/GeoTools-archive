/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2007, GeoTools Project Managment Committee (PMC)
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
package org.geotools.referencing;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// OpenGIS dependencies
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.cs.CoordinateSystemAxis;

// Geotools dependencies
import org.geotools.factory.GeoTools;


/**
 * Tests if the CRS utility class is functioning correctly when using HSQL datastore.
 * 
 * @source $URL$
 * @version $Id$
 * @author Jody Garnett
 * @author Martin Desruisseaux
 */
public class CRSTest extends TestCase {
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
        return new TestSuite(CRSTest.class);
    }

    /**
     * Constructs a test case with the given name.
     */
    public CRSTest(final String name) {
        super(name);
    }

    /**
     * Tests the (latitude, longitude) axis order for EPSG:4326.
     */
    public void testCorrectAxisOrder() throws NoSuchAuthorityCodeException, FactoryException {
        final CoordinateReferenceSystem crs = CRS.decode("EPSG:4326");
        final CoordinateSystem cs = crs.getCoordinateSystem();
        assertEquals(2, cs.getDimension());

        CoordinateSystemAxis axis0 = cs.getAxis(0);
        assertEquals("Lat", axis0.getAbbreviation());

        CoordinateSystemAxis axis1 = cs.getAxis(1);
        assertEquals("Long", axis1.getAbbreviation());
    }

    /**
     * Tests again EPSG:4326, but forced to (longitude, latitude) axis order.
     *
     * @todo Uncomment when we will be allowed to compile for J2SE 1.5.
     *       Call to {@link System#clearProperty} is mandatory for this test.
     */
//    public void testSystemPropertyToForceXY() throws NoSuchAuthorityCodeException, FactoryException {
//        assertNull(System.getProperty(GeoTools.FORCE_LONGITUDE_FIRST_AXIS_ORDER));
//        System.setProperty(GeoTools.FORCE_LONGITUDE_FIRST_AXIS_ORDER, "true");
//        try {
//            CoordinateReferenceSystem crs = CRS.decode("EPSG:4326");
//            final CoordinateSystem cs = crs.getCoordinateSystem();
//            assertEquals(2, cs.getDimension());
//
//            CoordinateSystemAxis axis0  = cs.getAxis(0);
//            assertEquals("forceXY did not work", "Long", axis0.getAbbreviation());
//
//            CoordinateSystemAxis axis1  = cs.getAxis(1);
//            assertEquals("forceXY did not work", "Lat", axis1.getAbbreviation());
//        } finally {
//            System.clearProperty(GeoTools.FORCE_LONGITUDE_FIRST_AXIS_ORDER);
//        }
//    }
}
