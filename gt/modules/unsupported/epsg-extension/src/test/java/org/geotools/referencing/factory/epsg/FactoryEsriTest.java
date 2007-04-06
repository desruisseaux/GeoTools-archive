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
package org.geotools.referencing.factory.epsg;

// JSE dependencies
import java.util.Set;
import java.io.PrintWriter;
import java.io.StringWriter;

// OpenGIS dependencies
import org.opengis.metadata.citation.Citation;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.CRSAuthorityFactory;

// Geotools dependencies
import org.geotools.referencing.ReferencingFactoryFinder;
import org.geotools.referencing.NamedIdentifier;
import org.geotools.metadata.iso.citation.Citations;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Tests ESRI CRS support.
 * 
 * @source $URL$
 * @version $Id$
 * @author Jody Garnett
 * @author Martin Desruisseaux
 */
public class FactoryEsriTest extends TestCase {
    /**
     * The factory to test.
     */
    private FactoryESRI factory;

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(FactoryEsriTest.class);
    }

    /**
     * Run the test from the command line.
     * Options: {@code -verbose}.
     *
     * @param args the command line arguments.
     */
    public static void main(final String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Creates a test case with the specified name.
     */
    public FactoryEsriTest(final String name) {
        super(name);
    }

    /**
     * Get the authority factory for ESRI.
     */
    protected void setUp() throws Exception {
        super.setUp();
        factory = (FactoryESRI) ReferencingFactoryFinder.getCRSAuthorityFactory("ESRI", null);
    }

    /**
     * Tests the authority code.
     */
    public void testAuthority() {
        Citation authority = factory.getAuthority();
        assertNotNull(authority);
        assertEquals("ESRI", authority.getTitle().toString());
        assertTrue(factory instanceof FactoryESRI);
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
        final Set codes  = factory.getAuthorityCodes(IdentifiedObject.class);
        final Set subset = factory.getAuthorityCodes(CoordinateReferenceSystem.class);
        assertNotNull(codes);
        assertEquals(codes.size(), subset.size());
        assertTrue(codes.containsAll(subset));
        assertFalse(codes.contains("26910"));  // This is an EPSG code.
        // The following number may be adjusted if esri.properties is updated.
        assertEquals(779, codes.size());
    }

    /**
     * Checks for duplication with EPSG-HSQL.
     */
    public void testDuplication() throws FactoryException {
        final StringWriter buffer = new StringWriter();
        final PrintWriter  writer = new PrintWriter(buffer);
        final Set duplicated = factory.reportDuplicatedCodes(writer);
        assertTrue(buffer.toString(), duplicated.isEmpty());
    }

    /**
     * Checks for CRS instantiations.
     */
    public void testInstantiation() throws FactoryException {
        final StringWriter buffer = new StringWriter();
        final PrintWriter  writer = new PrintWriter(buffer);
        final Set duplicated = factory.reportInstantiationFailures(writer);
        // The following number may be adjusted if esri.properties is updated.
        assertTrue(buffer.toString(), duplicated.size() <= 87);
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
        assertSame(crs, factory.createCoordinateReferenceSystem("EPSG:30591"));
        assertSame(crs, factory.createObject("30591"));
        final Set identifiers = crs.getIdentifiers();
        assertNotNull(identifiers);
        assertFalse(identifiers.isEmpty());

        String asString = identifiers.toString();
        assertTrue(asString, identifiers.contains(new NamedIdentifier(Citations.ESRI, "30591")));
        assertTrue(asString, identifiers.contains(new NamedIdentifier(Citations.EPSG, "30591")));
    }
}
