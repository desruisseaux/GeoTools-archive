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

import java.util.Collection;
import java.util.logging.Level;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.opengis.metadata.citation.Citation;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengis.referencing.crs.CRSAuthorityFactory;

import org.geotools.resources.Arguments;
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.referencing.ReferencingFactoryFinder;


/**
 * Tests {@link AutoCRSFactory}.
 *
 * @source $URL$
 * @version $Id$
 * @author Jody Garnett
 * @author Martin Desruisseaux
 */
public final class AUTOTest extends TestCase {
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
        org.geotools.util.logging.Logging.GEOTOOLS.forceMonolineConsoleOutput(log ? Level.CONFIG : null);
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
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        factory = new AutoCRSFactory();
    }

    /**
     * Tests the registration in {@link ReferencingFactoryFinder}.
     */
    public void testFactoryFinder() {
        final Collection<String> authorities = ReferencingFactoryFinder.getAuthorityNames();
        assertTrue(authorities.contains("AUTO"));
        assertTrue(authorities.contains("AUTO2"));
        factory = ReferencingFactoryFinder.getCRSAuthorityFactory("AUTO", null);
        assertTrue(factory instanceof AutoCRSFactory);
        assertSame(factory, ReferencingFactoryFinder.getCRSAuthorityFactory("AUTO2", null));
    }

    /**
     * Checks the authority names.
     */
    public void testAuthority() {
        final Citation authority = factory.getAuthority();
        assertTrue (Citations.identifierMatches(authority, "AUTO"));
        assertTrue (Citations.identifierMatches(authority, "AUTO2"));
        assertFalse(Citations.identifierMatches(authority, "EPSG"));
        assertFalse(Citations.identifierMatches(authority, "CRS"));
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
