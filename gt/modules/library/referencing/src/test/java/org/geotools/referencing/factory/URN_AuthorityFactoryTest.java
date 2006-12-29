/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2006, Geomatys
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
package org.geotools.referencing.factory;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// OpenGIS dependencies
import org.opengis.referencing.AuthorityFactory;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchIdentifierException;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.crs.CRSAuthorityFactory;

// Geotools dependencies
import org.geotools.referencing.CRS;
import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;


/**
 * Tests the {@link URN_AuthorityFactory} class backed by WMS or AUTO factories.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class URN_AuthorityFactoryTest extends TestCase {
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
        return new TestSuite(URN_AuthorityFactoryTest.class);
    }

    /**
     * Creates a suite of the given name.
     */
    public URN_AuthorityFactoryTest(final String name) {
        super(name);
    }

    /**
     * Make sure that a singleton instance is registered.
     */
    public void testRegistration() {
        String authority = "URN:OGC:DEF";
        final AuthorityFactory factory = FactoryFinder.getCRSAuthorityFactory(authority, null);
        assertSame(factory, FactoryFinder.getCRSAuthorityFactory  (authority, null));
        assertSame(factory, FactoryFinder.getCSAuthorityFactory   (authority, null));
        assertSame(factory, FactoryFinder.getDatumAuthorityFactory(authority, null));
        /*
         * Tests the X-OGC namespace, which should be synonymous.
         */
        authority = "URN:X-OGC:DEF";
        assertSame(factory, FactoryFinder.getCRSAuthorityFactory  (authority, null));
        assertSame(factory, FactoryFinder.getCSAuthorityFactory   (authority, null));
        assertSame(factory, FactoryFinder.getDatumAuthorityFactory(authority, null));
    }

    /**
     * Tests the CRS factory.
     */
    public void testCRS() throws FactoryException {
        CRSAuthorityFactory factory = FactoryFinder.getCRSAuthorityFactory("URN:OGC:DEF", null);
        GeographicCRS crs;
        try {
            crs = factory.createGeographicCRS("CRS:84");
            fail();
        } catch (NoSuchIdentifierException exception) {
            // This is the expected exception.
            assertEquals("CRS:84", exception.getIdentifierCode());
        }
        crs =           factory.createGeographicCRS("urn:ogc:def:crs:CRS:WMS1.3:84");
        assertSame(crs, factory.createGeographicCRS("urn:ogc:def:crs:CRS:1.3:84"));
        assertSame(crs, factory.createGeographicCRS("URN:OGC:DEF:CRS:CRS:1.3:84"));
        assertSame(crs, factory.createGeographicCRS("URN:OGC:DEF:CRS:CRS:84"));
        assertSame(crs, factory.createGeographicCRS("urn:x-ogc:def:crs:CRS:1.3:84"));
        assertSame(crs, CRS.decode("urn:ogc:def:crs:CRS:1.3:84"));
        assertSame(crs, CRS.decode("CRS:84"));
        assertNotSame(crs, DefaultGeographicCRS.WGS84);
        assertFalse(DefaultGeographicCRS.WGS84.equals(crs));
        assertTrue(CRS.equalsIgnoreMetadata(DefaultGeographicCRS.WGS84, crs));

        // Test CRS:83
        crs = factory.createGeographicCRS("urn:ogc:def:crs:CRS:1.3:83");
        assertSame(crs, CRS.decode("CRS:83"));
        assertFalse(CRS.equalsIgnoreMetadata(DefaultGeographicCRS.WGS84, crs));
    }
}
