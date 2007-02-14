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
import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.factory.AbstractAuthorityFactory;


/**
 * Tests {@link WebCRSFactory}.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class CRSTest extends TestCase {
    /**
     * The factory to test.
     */
    private CRSAuthorityFactory factory;

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
     * Tests the registration in {@link FactoryFinder}.
     */
    public void testFactoryFinder() {
        final Collection authorities = FactoryFinder.getAuthorityNames();
        assertTrue(authorities.contains("CRS"));
        factory = FactoryFinder.getCRSAuthorityFactory("CRS", null);
        assertTrue(factory instanceof WebCRSFactory);
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
     * Tests the {@link AbstractAuthorityFactory#find} method.
     */
    public void testFind() throws FactoryException {
        final AbstractAuthorityFactory factory = (AbstractAuthorityFactory) this.factory;
        GeographicCRS crs = factory.createGeographicCRS("CRS:84");
        assertSame   (crs, factory.find(crs, false));
        assertSame   (crs, factory.find(crs, true ));
        assertNotSame(crs, DefaultGeographicCRS.WGS84);
        assertSame   (crs, factory.find(DefaultGeographicCRS.WGS84, true ));
        assertNull   (     factory.find(DefaultGeographicCRS.WGS84, false));

        String wkt = "GEOGCS[\"WGS 84\",\n" +
                     "  DATUM[\"WGS84\",\n" +
                     "    SPHEROID[\"WGS 84\", 6378137.0, 298.257223563]],\n" +
                     "  PRIMEM[\"Greenwich\", 0.0],\n" +
                     "  UNIT[\"degree\", 0.017453292519943295]]";
        CoordinateReferenceSystem search = CRS.parseWKT(wkt);
        assertFalse(crs.equals(search));
        assertNull (factory.find(search, false));
        assertSame (crs, factory.find(search, true));
    }
}
