/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, Geotools Project Managment Committee (PMC)
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
package org.geotools.referencing.factory.wms;

// J2SE dependencies
import java.util.Collection;
import java.util.logging.Level;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// OpenGIS dependencies
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

// Geotools dependencies
import org.geotools.referencing.CRS;
import org.geotools.resources.Arguments;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.factory.IdentifiedObjectFinder;
import org.geotools.referencing.factory.AbstractAuthorityFactory;
import org.geotools.referencing.factory.BufferedAuthorityFactory;


/**
 * Tests {@link WebCRSFactory}.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class CRSTest extends TestCase {
    /**
     * The factory to test.
     */
    private AbstractAuthorityFactory factory;

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
        return new TestSuite(CRSTest.class);
    }

    /**
     * Creates a suite of the given name.
     */
    public CRSTest(final String name) {
        super(name);
    }

    /**
     * Initializes the factory to test.
     */
    protected void setUp() throws Exception {
        super.setUp();
        factory = new WebCRSFactory();
    }

    /**
     * Tests the registration in {@link ReferencingFactoryFinder}.
     */
    public void testFactoryFinder() {
        final Collection authorities = ReferencingFactoryFinder.getAuthorityNames();
        assertTrue(authorities.contains("CRS"));
        CRSAuthorityFactory found = ReferencingFactoryFinder.getCRSAuthorityFactory("CRS", null);
        assertTrue(found instanceof WebCRSFactory);
    }

    /**
     * Checks the authority names.
     */
    public void testAuthority() {
        final Collection identifiers = factory.getAuthority().getIdentifiers();
        assertTrue (identifiers.contains("CRS"));
        assertFalse(identifiers.contains("EPSG"));
        assertFalse(identifiers.contains("AUTO"));
        assertFalse(identifiers.contains("AUTO2"));
    }

    /**
     * Tests the CRS:84 code.
     */
    public void testCRS84() throws FactoryException {
        GeographicCRS crs = factory.createGeographicCRS("CRS:84");
        assertSame(crs, factory.createGeographicCRS("84"));
        assertSame(crs, factory.createGeographicCRS("CRS84"));
        assertSame(crs, factory.createGeographicCRS("CRS:CRS84"));
        assertSame(crs, factory.createGeographicCRS("crs : crs84"));
        assertNotSame(crs, factory.createGeographicCRS("CRS:83"));
        assertFalse(DefaultGeographicCRS.WGS84.equals(crs));
        assertTrue(CRS.equalsIgnoreMetadata(DefaultGeographicCRS.WGS84, crs));
    }

    /**
     * Tests the CRS:83 code.
     */
    public void testCRS83() throws FactoryException {
        GeographicCRS crs = factory.createGeographicCRS("CRS:83");
        assertSame(crs, factory.createGeographicCRS("83"));
        assertSame(crs, factory.createGeographicCRS("CRS83"));
        assertSame(crs, factory.createGeographicCRS("CRS:CRS83"));
        assertNotSame(crs, factory.createGeographicCRS("CRS:84"));
        assertFalse(CRS.equalsIgnoreMetadata(DefaultGeographicCRS.WGS84, crs));
    }

    /**
     * Tests the {@link IdentifiedObjectFinder#find} method.
     */
    public void testFind() throws FactoryException {
        final GeographicCRS CRS84 = factory.createGeographicCRS("CRS:84");
        final IdentifiedObjectFinder finder = factory.getIdentifiedObjectFinder(CoordinateReferenceSystem.class);
        assertTrue("Newly created finder should default to full scan.", finder.isFullScanAllowed());

        finder.setFullScanAllowed(false);
        assertSame("Should find without the need for scan, since we can use the CRS:84 identifier.",
                   CRS84, finder.find(CRS84));

        finder.setFullScanAllowed(true);
        assertSame("Allowing scanning should not make any difference for this CRS84 instance.",
                   CRS84, finder.find(CRS84));

        assertNotSame("Required condition for next test.", CRS84, DefaultGeographicCRS.WGS84);
        assertFalse  ("Required condition for next test.", CRS84.equals(DefaultGeographicCRS.WGS84));
        assertTrue   ("Required condition for next test.", CRS.equalsIgnoreMetadata(CRS84, DefaultGeographicCRS.WGS84));

        finder.setFullScanAllowed(false);
        assertNull("Should not find WGS84 without a full scan, since it doesn't contains the CRS:84 identifier.",
                   finder.find(DefaultGeographicCRS.WGS84));

        finder.setFullScanAllowed(true);
        assertSame("A full scan should allow us to find WGS84, since it is equals ignoring metadata to CRS:84.",
                   CRS84, finder.find(DefaultGeographicCRS.WGS84));

        finder.setFullScanAllowed(false);
        assertNull("The scan result should not be cached.",
                   finder.find(DefaultGeographicCRS.WGS84));

        // --------------------------------------------------
        // Same test than above, using a CRS created from WKT
        // --------------------------------------------------

        String wkt = "GEOGCS[\"WGS 84\",\n" +
                     "  DATUM[\"WGS84\",\n" +
                     "    SPHEROID[\"WGS 84\", 6378137.0, 298.257223563]],\n" +
                     "  PRIMEM[\"Greenwich\", 0.0],\n" +
                     "  UNIT[\"degree\", 0.017453292519943295]]";
        CoordinateReferenceSystem search = CRS.parseWKT(wkt);
        assertFalse("Required condition for next test.", CRS84.equals(search));
        assertTrue ("Required condition for next test.", CRS.equalsIgnoreMetadata(CRS84, search));

        finder.setFullScanAllowed(false);
        assertNull("Should not find WGS84 without a full scan, since it doesn't contains the CRS:84 identifier.",
                   finder.find(search));

        finder.setFullScanAllowed(true);
        assertSame("A full scan should allow us to find WGS84, since it is equals ignoring metadata to CRS:84.",
                   CRS84, finder.find(search));

        assertEquals("CRS:84", finder.findIdentifier(search));
    }

    /**
     * Tests the {@link IdentifiedObjectFinder#find} method through a.
     */
    public void testBufferedFind() throws FactoryException {
        final AbstractAuthorityFactory factory = new Buffered(this.factory);
        final GeographicCRS CRS84 = factory.createGeographicCRS("CRS:84");
        final IdentifiedObjectFinder finder = factory.getIdentifiedObjectFinder(CoordinateReferenceSystem.class);

        finder.setFullScanAllowed(false);
        assertSame("Should find without the need for scan, since we can use the CRS:84 identifier.",
                   CRS84, finder.find(CRS84));

        finder.setFullScanAllowed(false);
        assertNull("Should not find WGS84 without a full scan, since it doesn't contains the CRS:84 identifier.",
                   finder.find(DefaultGeographicCRS.WGS84));

        finder.setFullScanAllowed(true);
        assertSame("A full scan should allow us to find WGS84, since it is equals ignoring metadata to CRS:84.",
                   CRS84, finder.find(DefaultGeographicCRS.WGS84));

        finder.setFullScanAllowed(false);
        assertSame("At the contrary of testFind(), the scan result should be cached.",
                   CRS84, finder.find(DefaultGeographicCRS.WGS84));

        assertEquals("CRS:84", finder.findIdentifier(DefaultGeographicCRS.WGS84));
    }

    /**
     * For {@link #testBufferedFind}.
     */
    private static final class Buffered extends BufferedAuthorityFactory implements CRSAuthorityFactory {
        Buffered(final AbstractAuthorityFactory factory) {
            super(factory);
        }
    }
}
