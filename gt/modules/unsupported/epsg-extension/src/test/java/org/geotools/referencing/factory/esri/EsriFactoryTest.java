/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.referencing.factory.esri;

// JSE dependencies
import java.util.Set;
import java.util.Iterator;
import java.io.PrintWriter;

// OpenGIS dependencies
import org.opengis.metadata.citation.Citation;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.CRSAuthorityFactory;

// Geotools dependencies
import org.geotools.referencing.CRS;
import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.NamedIdentifier;
import org.geotools.referencing.factory.esri.FactoryUsingWKT;
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.resources.Arguments;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Test ESRI CRS support. This class doesn't test the fallback in EPSG namespace.
 * 
 * @source $URL$
 * @version $Id$
 * @author Jody Garnett
 * @author Martin Desruisseaux
 */
public class EsriFactoryTest extends TestCase {
    /**
     * Set to non-null value when run from the command line.
     */
    private static PrintWriter out;

    /**
     * The factory to test.
     */
    private CRSAuthorityFactory factory;

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(EsriFactoryTest.class);
    }

    /**
     * Run the test from the command line.
     * Options: {@code -verbose}.
     *
     * @param args the command line arguments.
     */
    public static void main(final String[] args) {
        org.geotools.util.Logging.GEOTOOLS.forceMonolineConsoleOutput();
        final Arguments arguments = new Arguments(args);
        if (arguments.getFlag("-verbose")) {
            out = arguments.out;
        }
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Get the authority factory for ESRI.
     */
    protected void setUp() throws Exception {
        super.setUp();
        factory = FactoryFinder.getCRSAuthorityFactory("ESRI", null);
    }

    /**
     * Tests the authority code.
     */
    public void testAuthority() {
        Citation authority = factory.getAuthority();
        assertNotNull(authority);
        assertEquals("ESRI", authority.getTitle().toString());
        assertTrue(factory instanceof FactoryUsingWKT);
    }

    /**
     * Tests the vendor.
     */
    public void testVendor() {
        Citation vendor = factory.getVendor();        
        assertNotNull(vendor);
        assertEquals("Geotools", vendor.getTitle().toString());
    }

    /**
     * Tests the codes.
     */
    public void testCodes() throws FactoryException {
        final Set codes = factory.getAuthorityCodes(IdentifiedObject.class);
        assertEquals(784, codes.size());
        final Set subset = factory.getAuthorityCodes(CoordinateReferenceSystem.class);
        assertNotNull(codes);
        assertEquals(codes.size(), subset.size());
        assertTrue(codes.containsAll(subset));
        assertFalse(codes.contains("26910"));  // This is an EPSG code.
    }

    /**
     * Tests an EPSG code.
     */
    public void test26910() throws FactoryException {
        try {
            CoordinateReferenceSystem crs = factory.createCoordinateReferenceSystem("26910");
            fail();
        } catch (NoSuchAuthorityCodeException e) {
            // This is the expected exception.
        }
    }

    /**
     * Tests an EPSG code.
     */
    public void test4326() throws FactoryException {
        try {
            CoordinateReferenceSystem crs = factory.createCoordinateReferenceSystem("4326");
            fail();
        } catch (NoSuchAuthorityCodeException e) {
            // This is the expected exception.
        }
    }

    /**
     * Tests an EPSG code.
     */
    public void test4269() throws FactoryException {
        try {
            CoordinateReferenceSystem crs = factory.createCoordinateReferenceSystem("4269");
            fail();
        } catch (NoSuchAuthorityCodeException e) {
            // This is the expected exception.
        }
    }

    /**
     * Tests an extra code (neither EPSG or ESRI).
     */
    public void test42102() throws FactoryException {
        try {
            CoordinateReferenceSystem crs = factory.createCoordinateReferenceSystem("42102");
            fail();
        } catch (NoSuchAuthorityCodeException e) {
            // This is the expected exception.
        }
    }

    /**
     * Tests an ESRI code.
     */
    public void test30591() throws FactoryException {
        final CoordinateReferenceSystem crs = factory.createCoordinateReferenceSystem("30591");
        assertSame(crs, factory.createCoordinateReferenceSystem("ESRI:30591"));
        assertSame(crs, factory.createCoordinateReferenceSystem("esri:30591"));
        assertSame(crs, factory.createCoordinateReferenceSystem(" ESRI : 30591 "));
        assertSame(crs, factory.createObject("30591"));
        final Set identifiers = crs.getIdentifiers();
        assertNotNull(identifiers);
        assertFalse(identifiers.isEmpty());
        NamedIdentifier expected = new NamedIdentifier(Citations.ESRI, "30591");
        assertTrue(identifiers.toString(), identifiers.contains(expected));
    }

    /**
     * Count the number of CRS successfully created.
     */
    public void testSuccessRate() throws FactoryException {
        Set codes = factory.getAuthorityCodes(CoordinateReferenceSystem.class);
        int total = codes.size();
        int count = 0;
        for (final Iterator i=codes.iterator(); i.hasNext();) {
            final String code = (String) i.next();
            CoordinateReferenceSystem crs;
            try {
                crs = factory.createCoordinateReferenceSystem(code);
            } catch (FactoryException e) {
                if (out != null) {
                    out.println("WARNING (CRS: " + code + " ):" + e);
                }
                continue;
            }            
            assertNotNull(crs);
            assertSame(crs, factory.createObject(code));
            count++;
        }
        if (out != null) {
            out.println("success:" + count + "/" + total + " (" + 100f*count/total + "%)");
        }
    }
}
