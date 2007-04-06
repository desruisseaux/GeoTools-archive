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
import java.io.File;
import java.util.Collection;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// OpenGIS dependencies
import org.opengis.metadata.citation.Citation;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.ProjectedCRS;

// Geotools dependencies
import org.geotools.factory.Hints;
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.referencing.CRS;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.geotools.referencing.NamedIdentifier;


/**
 * Tests {@link FactoryUsingWKT}.
 * 
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Jody Garnett
 */
public class FactoryUsingWktTest extends TestCase {
    /**
     * The factory to test.
     */
    private FactoryUsingWKT factory;

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(FactoryUsingWktTest.class);
    }

    /**
     * Run the test from the command line.
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
    public FactoryUsingWktTest(final String name) {
        super(name);
    }

    /**
     * Gets the authority factory for ESRI.
     */
    protected void setUp() throws Exception {
        super.setUp();
        factory = (FactoryUsingWKT) ReferencingFactoryFinder.getCRSAuthorityFactory("EPSG",
                new Hints(Hints.CRS_AUTHORITY_FACTORY, FactoryUsingWKT.class));
    }

    public void testCrsAuthorityExtraDirectoryHint() throws Exception {
        Hints hints = new Hints(Hints.CRS_AUTHORITY_FACTORY, FactoryUsingWKT.class);
        try {
           hints.put( Hints.CRS_AUTHORITY_EXTRA_DIRECTORY, "invalid" );
           fail("Should of been tossed out as an invalid hint");
        }
        catch( IllegalArgumentException expected){            
        }
        
        String directory = new File(".").getAbsolutePath();
        hints = new Hints(Hints.CRS_AUTHORITY_FACTORY, FactoryUsingWKT.class);
        hints.put( Hints.CRS_AUTHORITY_EXTRA_DIRECTORY, directory );
        
        System.setProperty( "org.geotools.referencing.crs-directory", directory );        
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
        assertTrue(factory instanceof FactoryUsingWKT);
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
     * Tests the {@code 18001} code.
     */
    public void test42101() throws FactoryException {
        CoordinateReferenceSystem actual, expected;
        expected = factory.createCoordinateReferenceSystem("42101");
        actual   = CRS.decode("EPSG:42101");
        assertSame(expected, actual);
        assertTrue(actual instanceof ProjectedCRS);
        Collection ids = actual.getIdentifiers();
        assertTrue (ids.contains(new NamedIdentifier(Citations.EPSG, "42101")));
        assertFalse(ids.contains(new NamedIdentifier(Citations.ESRI, "42101")));
    }    
}
