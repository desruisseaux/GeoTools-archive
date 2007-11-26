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
import java.net.URL;
import java.util.Iterator;
import java.util.Set;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// OpenGIS dependencies
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.ProjectedCRS;

// Geotools dependencies
import org.geotools.referencing.CRS;
import org.geotools.referencing.ReferencingFactoryFinder;


/**
 * Checks the exception thrown by the fallback system do report actual errors when the code is
 * available but for some reason broken, and not "code not found" ones.
 *
 * @source $URL$
 * @version $Id$
 * @author Andrea Aime (TOPP)
 */
public class FallbackAuthorityFactoryTest extends TestCase {
    /**
     * Set to {@code true} for printing debugging information.
     */
    private static final boolean VERBOSE = false;

    /**
     * The extra factory.
     */
    private FactoryEPSGExtra extra;

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(FallbackAuthorityFactoryTest.class);
    }

    /**
     * Run the test from the command line.
     *
     * @param args the command line arguments.
     */
    public static void main(final String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Creates a test case with the specified name.
     */
    public FallbackAuthorityFactoryTest(final String name) {
        super(name);
    }

    /**
     * Adds the extra factory to the set of authority factories.
     */
    @Override
    public void setUp() {
        assertNull(extra);
        extra = new FactoryEPSGExtra();
        ReferencingFactoryFinder.addAuthorityFactory(extra);
        ReferencingFactoryFinder.scanForPlugins();
    }

    /**
     * Removes the extra factory from the set of authority factories.
     */
    @Override
    public void tearDown() {
        if (org.geotools.test.TestData.isBaseJavaPlatform()) {
            // Disabled in J2SE 1.4 build because of a bug.
            // TODO: Remove when we will be target J2SE 1.4 or 1.5.
            return;
        }
        assertNotNull(extra);
        ReferencingFactoryFinder.removeAuthorityFactory(extra);
        extra = null;
    }

    /**
     * Makes sure that the testing {@link FactoryEPSGExtra} has precedence over
     * {@link FactoryUsingWKT}.
     */
    public void testFactoryOrdering() {
        if (org.geotools.test.TestData.isBaseJavaPlatform()) {
            // Disabled in J2SE 1.4 build because of a bug.
            // TODO: Remove when we will be target J2SE 1.4 or 1.5.
            return;
        }
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
    public void test42101() throws FactoryException {
        assertTrue(CRS.decode("EPSG:42101") instanceof ProjectedCRS);
    }

    /**
     * Tests the {@code 00001} fake code.
     */
    public void test00001() throws FactoryException {
        if (org.geotools.test.TestData.isBaseJavaPlatform()) {
            // Disabled in J2SE 1.4 build because of a bug.
            // TODO: Remove when we will be target J2SE 1.4 or 1.5.
            return;
        }
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
