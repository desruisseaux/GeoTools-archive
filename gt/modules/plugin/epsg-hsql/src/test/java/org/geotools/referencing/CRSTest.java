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

// J2SE dependencies
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// OpenGIS dependencies
import org.opengis.metadata.citation.Citation;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

// Geotools dependencies
import org.geotools.resources.Arguments;
import org.geotools.referencing.factory.OrderedAxisAuthorityFactory;


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
     * {@code true} for tracing operations on the standard output.
     */
    private static boolean verbose;

    /**
     * Run the suite from the command line.
     */
    public static void main(final String[] args) {
        final Arguments arguments = new Arguments(args);
        verbose = arguments.getFlag("-verbose");
        arguments.getRemainingArguments(0);
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

    // -------------------------------------------------------------------------
    // The following tests are copied from the legacy plugin/epsg-wkt test suite
    // -------------------------------------------------------------------------

    /**
     * Makes sure that the authority factory has the proper name.
     */
    public void testAuthority() {
        CRSAuthorityFactory factory;
        Citation authority;

        // Tests the official factory.
        factory   = ReferencingFactoryFinder.getCRSAuthorityFactory("EPSG", null);
        authority = factory.getAuthority();
        assertNotNull(authority);
        assertEquals("European Petroleum Survey Group", authority.getTitle().toString(Locale.US));
        assertTrue(authority.getIdentifiers().contains("EPSG"));

        // Tests the modified factory.
        factory   = new OrderedAxisAuthorityFactory("EPSG", null, null);
        authority = factory.getAuthority();
        assertNotNull(authority);
        assertTrue(authority.getIdentifiers().contains("EPSG"));
    }

    /**
     * Tests the vendor name.
     */
    public void testVendor() {
        CRSAuthorityFactory factory;
        Citation vendor;

        factory = new OrderedAxisAuthorityFactory("EPSG", null, null);
        vendor  = factory.getVendor();
        assertNotNull(vendor);
        assertEquals("Geotools", vendor.getTitle().toString(Locale.US));
        assertFalse(vendor.getIdentifiers().contains("EPSG"));
    }

    /**
     * Tests the amount of codes available.
     */
    public void testCodes() throws FactoryException {
        final CRSAuthorityFactory factory = new OrderedAxisAuthorityFactory("EPSG", null, null);
        final Set codes = factory.getAuthorityCodes( CoordinateReferenceSystem.class );
        assertNotNull(codes);
        assertTrue(codes.size() >= 3000);
    }

    /**
     * A random CRS for fun.
     */
    public void test26910() throws FactoryException {
        final CRSAuthorityFactory factory = new OrderedAxisAuthorityFactory("EPSG", null, null);
        final CoordinateReferenceSystem crs = factory.createCoordinateReferenceSystem("EPSG:26910");
        assertNotNull(crs);
        assertSame(crs, factory.createObject("EPSG:26910"));
    }

    /**
     * UDIG requires this to work.
     */
    public void test4326() throws FactoryException {
        final CRSAuthorityFactory factory = new OrderedAxisAuthorityFactory("EPSG", null, null);
        final CoordinateReferenceSystem crs = factory.createCoordinateReferenceSystem("EPSG:4326");
        assertNotNull(crs);
        assertSame(crs, factory.createObject("EPSG:4326"));
    }

    /**
     * UDIG requires this to work.
     */
    public void test4269() throws FactoryException {
        final CRSAuthorityFactory factory = new OrderedAxisAuthorityFactory("EPSG", null, null);
        final CoordinateReferenceSystem crs = factory.createCoordinateReferenceSystem("EPSG:4269");
        assertNotNull(crs);
        assertSame(crs, factory.createObject("EPSG:4269"));
    }

    /**
     * A random CRS for fun.
     */
    public void test26910Lower() throws FactoryException {
        CoordinateReferenceSystem crs = CRS.decode("epsg:26910");
        assertNotNull(crs);                
    }

    /**
     * A random CRS for fun.
     */
    public void test26986Lower() throws FactoryException {
        CoordinateReferenceSystem crs = CRS.decode("epsg:26986");
        assertNotNull(crs);                
    }

    /**
     * WFS requires this to work.
     */
    public void test4326Lower() throws FactoryException {
        CoordinateReferenceSystem crs = CRS.decode("epsg:4326");
        assertNotNull(crs);
    }

    /**
     * WFS requires this to work.
     */
    public void test26742Lower() throws FactoryException {
        CoordinateReferenceSystem crs = CRS.decode("epsg:26742");
        assertNotNull(crs);
    }

    /**
     * WFS requires this to work.
     */
    public void test4269Lower() throws FactoryException {
        CoordinateReferenceSystem crs = CRS.decode("epsg:4269");
        assertNotNull(crs);
    }

    /**
     * Tests the number of CRS that can be created. This test will be executed only if this test
     * suite is run with the {@code -verbose} option provided on the command line.
     */
    public void testSuccess() throws FactoryException {
        if (!verbose) {
            return;
        }
        final CRSAuthorityFactory factory = new OrderedAxisAuthorityFactory("EPSG", null, null);
        Set codes = factory.getAuthorityCodes(CoordinateReferenceSystem.class);
        int total = codes.size();
        int count = 0;
        for (Iterator i=codes.iterator(); i.hasNext();) {
            CoordinateReferenceSystem crs;
            String code = (String) i.next();
            try {
                crs = factory.createCoordinateReferenceSystem(code);
                assertNotNull(crs);
                count++;
            } catch (FactoryException e) {
                System.err.println("WARNING (CRS: "+code+" ):" + e.getMessage());
            }            
        }
        System.out.println("Success: " + count + "/" + total + " (" + (count*100)/total + "%)");
    }
}
