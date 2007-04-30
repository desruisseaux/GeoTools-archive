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

// Java dependencies
import java.util.Set;
import java.util.Collection;
import java.io.PrintWriter;
import java.io.StringWriter;

// OpenGIS dependencies
import org.opengis.metadata.citation.Citation;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

// Geotools dependencies
import org.geotools.factory.Hints;
import org.geotools.referencing.CRS;
import org.geotools.referencing.NamedIdentifier;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.geotools.referencing.factory.OrderedAxisAuthorityFactory;
import org.geotools.metadata.iso.citation.Citations;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Tests {@link FactoryExtension}.
 * 
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Jody Garnett
 */
public class FactoryExtensionTest extends TestCase {
    /**
     * The factory to test.
     */
    private FactoryExtension factory;

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(FactoryExtensionTest.class);
    }

    /**
     * Run the test from the command line.
     * Options: {@code -verbose}.
     *
     * @param args the command line arguments.
     */
    public static void main(final String[] args) {
        org.geotools.util.Logging.GEOTOOLS.forceMonolineConsoleOutput();
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Creates a test case with the specified name.
     */
    public FactoryExtensionTest(final String name) {
        super(name);
    }

    /**
     * Gets the authority factory for ESRI.
     */
    protected void setUp() throws Exception {
        super.setUp();
        factory = (FactoryExtension) ReferencingFactoryFinder.getCRSAuthorityFactory("EPSG",
                new Hints(Hints.CRS_AUTHORITY_FACTORY, FactoryExtension.class));
    }

    /**
     * Tests the authority code.
     */
    public void testAuthority(){
        final Citation authority = factory.getAuthority();
        assertNotNull(authority);
        assertEquals("European Petroleum Survey Group", authority.getTitle().toString());
        assertTrue (authority.getIdentifiers().contains("EPSG"));
        assertFalse(authority.getIdentifiers().contains("ESRI"));
        assertTrue(factory instanceof FactoryExtension);
    }

    /**
     * Tests the vendor.
     */
    public void testVendor(){
        final Citation vendor = factory.getVendor();        
        assertNotNull(vendor);
        assertEquals("Geotools", vendor.getTitle().toString());
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
        assertTrue(buffer.toString(), duplicated.isEmpty());
    }

    /**
     * Tests the {@code 41001} code.
     */
    public void test41001() throws FactoryException {
        CoordinateReferenceSystem actual, expected;
        expected = factory.createCoordinateReferenceSystem("41001");
        actual   = CRS.decode("EPSG:41001");
        assertSame(expected, actual);
        assertTrue(actual instanceof ProjectedCRS);
        Collection ids = actual.getIdentifiers();
        assertTrue (ids.contains(new NamedIdentifier(Citations.EPSG, "41001")));
        assertFalse(ids.contains(new NamedIdentifier(Citations.ESRI, "41001")));
    }

    /**
     * UDIG requires this to work.
     */
    public void test42102() throws FactoryException {
        final Hints hints = new Hints(Hints.CRS_AUTHORITY_FACTORY, FactoryExtension.class);
        final CRSAuthorityFactory factory = new OrderedAxisAuthorityFactory("EPSG", hints, null);
        final CoordinateReferenceSystem crs = factory.createCoordinateReferenceSystem("EPSG:42102");
        assertNotNull(crs);
        assertNotNull(crs.getIdentifiers());
        assertFalse(crs.getIdentifiers().isEmpty());
        NamedIdentifier expected = new NamedIdentifier(Citations.EPSG, "42102");
        assertTrue(crs.getIdentifiers().contains(expected));
    }

    /**
     * WFS requires this to work.
     */
    public void test42102Lower() throws FactoryException {
        CoordinateReferenceSystem crs = CRS.decode("epsg:42102");
        assertNotNull(crs);
        assertNotNull(crs.getIdentifiers());
        assertFalse(crs.getIdentifiers().isEmpty());
        NamedIdentifier expected = new NamedIdentifier(Citations.EPSG, "42102");
        assertTrue(crs.getIdentifiers().contains(expected));
    }

    /**
     * WFS requires this to work.
     */
    public void test42304Lower() throws FactoryException {
        CoordinateReferenceSystem crs = CRS.decode("epsg:42304");
        assertNotNull(crs);
    }
}
