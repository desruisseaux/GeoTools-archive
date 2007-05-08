/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, Geotools Project Managment Committee (PMC)
 *    (C) 2005, Institut de Recherche pour le Développement
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
package org.geotools.referencing.factory;

// J2SE dependencies
import java.util.Collection;
import java.util.logging.Level;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// OpenGIS dependencies
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

// Geotools dependencies
import org.geotools.referencing.CRS;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.resources.Arguments;


/**
 * Tests the {@link AllAuthoritiesFactory} implementation.
 *
 * @author Martin Desruisseaux
 * @source $URL$
 * @version $Id$
 */
public final class AllAuthoritiesFactoryTest extends TestCase {
    /**
     * Run the suite from the command line.
     */
    public static void main(final String[] args) {
        final Arguments arguments = new Arguments(args);
        final boolean log = arguments.getFlag("-log");
        arguments.getRemainingArguments(0);
        org.geotools.util.Logging.GEOTOOLS.forceMonolineConsoleOutput(log ? Level.CONFIG : null);
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(AllAuthoritiesFactoryTest.class);
    }

    /**
     * Creates a suite of the given name.
     */
    public AllAuthoritiesFactoryTest(final String name) {
        super(name);
    }

    /**
     * Tests the {@link AllAuthoritiesFactory#getAuthorityCodes} method.
     */
    public void testAuthorityCodes() throws FactoryException {
        final CRSAuthorityFactory all  = AllAuthoritiesFactory.DEFAULT;
        final Collection codes = all.getAuthorityCodes(CoordinateReferenceSystem.class);
        assertFalse(codes.isEmpty());
        assertTrue(codes.contains("CRS:84"));
// "AUTO" fails on Java 1.4, and "AUTO2" fails on Java 5. Needs to investigate why.
//      assertTrue(codes.contains("AUTO:42001"));
    }

    /**
     * Tests the {@link AllAuthoritiesFactory#createCoordinateReferenceSystem} method.
     */
    public void testCreateCRS() throws FactoryException {
        final CRSAuthorityFactory auto = ReferencingFactoryFinder.getCRSAuthorityFactory("AUTO", null);
        final CRSAuthorityFactory crs  = ReferencingFactoryFinder.getCRSAuthorityFactory("CRS",  null);
        final CRSAuthorityFactory all  = AllAuthoritiesFactory.DEFAULT;
        CoordinateReferenceSystem actual, expected;

        actual   = all.createCoordinateReferenceSystem("CRS:84");
        expected = crs.createCoordinateReferenceSystem(    "84");
        assertSame(expected, actual);
        assertSame(expected, all.createObject("CRS:84"));

        actual   = all .createCoordinateReferenceSystem("AUTO:42001,0,0");
        expected = auto.createCoordinateReferenceSystem(     "42001,0,0");
        assertSame(expected, actual);
        assertSame(expected, all.createObject("AUTO:42001,0,0"));

        actual   = all.createCoordinateReferenceSystem("CRS:27");
        expected = crs.createCoordinateReferenceSystem(    "27");
        assertSame(expected, actual);
        assertSame(expected, all.createObject("CRS:27"));

        try {
            all.createCoordinateReferenceSystem("84");
            fail("Should not work without authority.");
        } catch (NoSuchAuthorityCodeException exception) {
            // This is the expected exception.
            assertEquals("84", exception.getAuthorityCode());
        }

        try {
            all.createCoordinateReferenceSystem("FOO:84");
            fail("Should not work with unknown authority.");
        } catch (NoSuchAuthorityCodeException exception) {
            // This is the expected exception.
            assertEquals("FOO", exception.getAuthority());
        }
    }

    /**
     * Tests the {@code "http://www.opengis.net/gml/srs/"} name space. This requires special
     * processing by {@link AllAuthoritiesFactory}, since the separator character is not the
     * usual {@code ':'}.
     */
    public void testHttp() throws FactoryException {
        final CRSAuthorityFactory crs = ReferencingFactoryFinder.getCRSAuthorityFactory("CRS", null);
        final CRSAuthorityFactory all = AllAuthoritiesFactory.DEFAULT;
        CoordinateReferenceSystem actual, expected;

        actual   = all.createCoordinateReferenceSystem("http://www.opengis.net/gml/srs/CRS#84");
        expected = crs.createCoordinateReferenceSystem("84");
        assertSame(expected, actual);

        actual   = all.createCoordinateReferenceSystem("HTTP://WWW.OPENGIS.NET/GML/SRS/crs#84");
        assertSame(expected, actual);

        actual   = all.createCoordinateReferenceSystem("http://www.opengis.net/gml/srs/CRS.xml#84");
        assertSame(expected, actual);

        try {
            all.createCoordinateReferenceSystem("http://www.dummy.net/gml/srs/CRS#84");
            fail("Expected a NoSuchAuthorityCodeException");
        } catch (NoSuchAuthorityCodeException e) {
            assertEquals("http://www.dummy.net", e.getAuthority());
        }

        try {
            all.createCoordinateReferenceSystem("http://www.opengis.net/gml/dummy/CRS#84");
            fail("Expected a NoSuchAuthorityCodeException");
        } catch (NoSuchAuthorityCodeException e) {
            assertEquals("http://www.opengis.net/gml/srs/", e.getAuthority());
        }
    }

    /**
     * Tests the {@link IdentifiedObjectFinder#find} method.
     */
    public void testFind() throws FactoryException {
        final AbstractAuthorityFactory all = AllAuthoritiesFactory.DEFAULT;
        final IdentifiedObjectFinder finder = all.getIdentifiedObjectFinder(CoordinateReferenceSystem.class);
        finder.setFullScanAllowed(false);
        assertNull("Should not find the CRS without a scan.", finder.find(DefaultGeographicCRS.WGS84));

        finder.setFullScanAllowed(true);
        final IdentifiedObject find = finder.find(DefaultGeographicCRS.WGS84);
        assertNotNull("With scan allowed, should find the CRS.", find);
        assertTrue(CRS.equalsIgnoreMetadata(find, DefaultGeographicCRS.WGS84));
        assertSame(all.createCoordinateReferenceSystem("CRS:84"), find);
        assertEquals("CRS:84", finder.findIdentifier(DefaultGeographicCRS.WGS84));
    }
}
