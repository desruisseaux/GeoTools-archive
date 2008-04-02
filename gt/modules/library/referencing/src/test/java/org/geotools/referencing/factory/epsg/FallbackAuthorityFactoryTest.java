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
package org.geotools.referencing.factory.epsg;

import java.net.URL;
import java.util.Iterator;
import java.util.Set;

import org.geotools.referencing.CRS;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.ProjectedCRS;

import org.junit.*;
import static org.junit.Assert.*;


/**
 * Checks the exception thrown by the fallback system do report actual errors when the code is
 * available but for some reason broken, and not "code not found" ones.
 *
 * @source $URL$
 * @version $Id$
 * @author Andrea Aime (TOPP)
 */
public final class FallbackAuthorityFactoryTest {
    /**
     * Set to {@code true} for printing debugging information.
     */
    private static final boolean VERBOSE = false;

    /**
     * The extra factory.
     */
    private FactoryEPSGExtra extra;

    /**
     * Adds the extra factory to the set of authority factories.
     */
    @Before
    public void setUp() {
        assertNull(extra);
        extra = new FactoryEPSGExtra();
        ReferencingFactoryFinder.addAuthorityFactory(extra);
        ReferencingFactoryFinder.scanForPlugins();
    }

    /**
     * Removes the extra factory from the set of authority factories.
     */
    @After
    public void tearDown() {
        assertNotNull(extra);
        ReferencingFactoryFinder.removeAuthorityFactory(extra);
        extra = null;
    }

    /**
     * Makes sure that the testing {@link FactoryEPSGExtra} has precedence over
     * {@link FactoryUsingWKT}.
     */
    @Test
    public void testFactoryOrdering() {
        Set factories =  ReferencingFactoryFinder.getCRSAuthorityFactories(null);
        boolean foundWkt = false;
        boolean foundExtra = false;
        for (Iterator it = factories.iterator(); it.hasNext();) {
            CRSAuthorityFactory factory = (CRSAuthorityFactory) it.next();
            Class type = factory.getClass();
            if (VERBOSE) {
                System.out.println(type);
            }
            if (type == FactoryEPSGExtra.class) {
                foundExtra = true;
            } else if (type == FactoryUsingWKT.class) {
                foundWkt = true;
                assertTrue("We should have encountered WKT factory after the extra one", foundExtra);
            }
        }
        assertTrue(foundWkt);
        assertTrue(foundExtra);
    }

    /**
     * Tests the {@code 42101} code. The purpose of this test is mostly
     * to make sure that {@link FactoryUsingWKT} is in the chain.
     */
    @Test
    public void test42101() throws FactoryException {
        assertTrue(CRS.decode("EPSG:42101") instanceof ProjectedCRS);
    }

    /**
     * Tests the {@code 00001} fake code.
     */
    @Test
    public void test00001() throws FactoryException {
        try {
            CRS.decode("EPSG:00001");
            fail("This code should not be there");
        } catch (NoSuchAuthorityCodeException e) {
            fail("The code 00001 is there, exception should report it's broken");
        } catch (FactoryException e) {
            // cool, that's what we expected
        }
    }

    /**
     * GEOT-1702, make sure looking up for an existing code does not result in a
     * {@link StackOverflowException}.
     */
    @Test
    public void testLookupSuccessfull() throws FactoryException {
        CoordinateReferenceSystem crs = CRS.decode("EPSG:42101");
        String code = CRS.lookupIdentifier(crs, true);
        assertEquals("EPSG:42101", code);
    }

    /**
     * GEOT-1702, make sure looking up for a non existing code does not result in a
     * {@link StackOverflowException}.
     */
    @Test
    public void testLookupFailing() throws FactoryException {
        String wkt = "PROJCS[\"Google Mercator\", \r\n" +
                "  GEOGCS[\"WGS 84\", \r\n" +
                "    DATUM[\"World Geodetic System 1984\", \r\n" +
                "      SPHEROID[\"WGS 84\", 6378137.0, 298.257223563, AUTHORITY[\"EPSG\",\"7030\"]], \r\n" +
                "      AUTHORITY[\"EPSG\",\"6326\"]], \r\n" +
                "    PRIMEM[\"Greenwich\", 0.0, AUTHORITY[\"EPSG\",\"8901\"]], \r\n" +
                "    UNIT[\"degree\", 0.017453292519943295], \r\n" +
                "    AXIS[\"Geodetic latitude\", NORTH], \r\n" +
                "    AXIS[\"Geodetic longitude\", EAST], \r\n" +
                "    AUTHORITY[\"EPSG\",\"4326\"]], \r\n" +
                "  PROJECTION[\"Mercator_1SP\"], \r\n" +
                "  PARAMETER[\"latitude_of_origin\", 0.0], \r\n" +
                "  PARAMETER[\"central_meridian\", 0.0], \r\n" +
                "  PARAMETER[\"scale_factor\", 1.0], \r\n" +
                "  PARAMETER[\"false_easting\", 0.0], \r\n" +
                "  PARAMETER[\"false_northing\", 0.0], \r\n" +
                "  UNIT[\"m\", 1.0], \r\n" +
                "  AXIS[\"Easting\", EAST], \r\n" +
                "  AXIS[\"Northing\", NORTH], \r\n" +
                "  AUTHORITY[\"EPSG\",\"900913\"]]";
        CoordinateReferenceSystem crs = CRS.parseWKT(wkt);
        assertNull(CRS.lookupIdentifier(crs, true));
    }

    /**
     * Extra class used to make sure we have {@link FactoryUsingWKT} among the fallbacks
     * (used to check the fallback mechanism).
     *
     * @author Andrea Aime (TOPP)
     */
    private static class FactoryEPSGExtra extends FactoryUsingWKT {
        /**
         * Creates a factory to be registered before {@link FactoryUsingWKT} in the fallback chain.
         */
        public FactoryEPSGExtra() {
            // make sure we are before FactoryUsingWKT in the fallback chain
            super(null, DEFAULT_PRIORITY + 5);
        }

        /**
         * Returns the URL to the test file that contains a broken CRS for code EPSG:1.
         */
        @Override
        protected URL getDefinitionsURL() {
            return FactoryUsingWKT.class.getResource("epsg2.properties");
        }
    }
}
