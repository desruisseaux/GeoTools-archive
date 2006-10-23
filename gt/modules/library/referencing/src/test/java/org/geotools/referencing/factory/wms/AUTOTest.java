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
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengis.referencing.crs.CRSAuthorityFactory;

// Geotools dependencies
import org.geotools.resources.Arguments;
import org.geotools.util.MonolineFormatter;
import org.geotools.referencing.FactoryFinder;


/**
 * Tests {@link AutoCRSFactory}.
 *
 * @source $URL$
 * @version $Id$
 * @author Jody Garnett
 * @author Martin Desruisseaux
 */
public class AUTOTest extends TestCase {
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
        MonolineFormatter.initGeotools(log ? Level.CONFIG : null);
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(AUTOTest.class);
    }

    /**
     * Creates a suite of the given name.
     */
    public AUTOTest(final String name) {
        super(name);
    }

    /**
     * Initializes the factory to test.
     */
    protected void setUp() throws Exception {
        super.setUp();
        factory = new AutoCRSFactory();
    }

    /**
     * Tests the registration in {@link FactoryFinder}.
     */
    public void testFactoryFinder() {
        final Collection authorities = FactoryFinder.getAuthorityNames();
        assertTrue(authorities.contains("AUTO"));
        assertTrue(authorities.contains("AUTO2"));
        factory = FactoryFinder.getCRSAuthorityFactory("AUTO", null);
        assertTrue(factory instanceof AutoCRSFactory);
        assertSame(factory, FactoryFinder.getCRSAuthorityFactory("AUTO2", null));
    }

    /**
     * Checks the authority names.
     */
    public void testAuthority() {
        final Collection identifiers = factory.getAuthority().getIdentifiers();
        assertTrue (identifiers.contains("AUTO"));
        assertTrue (identifiers.contains("AUTO2"));
        assertFalse(identifiers.contains("EPSG"));
        assertFalse(identifiers.contains("CRS"));
    }

    /**
     * UDIG requires this to work.
     */
    public void test42001() throws FactoryException {
        final ProjectedCRS utm = factory.createProjectedCRS("AUTO:42001,0.0,0.0");
        assertNotNull("auto-utm", utm);
        assertSame   (utm, factory.createObject("AUTO :42001, 0,0"));
        assertSame   (utm, factory.createObject("AUTO2:42001, 0,0"));
        assertSame   (utm, factory.createObject(      "42001, 0,0"));
        assertNotSame(utm, factory.createObject("AUTO :42001,30,0"));
        assertEquals ("Transverse_Mercator", utm.getConversionFromBase().getMethod().getName().getCode());
    }
}
