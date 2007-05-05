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

// OpenGIS dependencies
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

// Geotools dependencies
import org.geotools.referencing.CRS;
import org.geotools.referencing.NamedIdentifier;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.metadata.iso.citation.Citations;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Tests {@link FactoryUsingWKT} as a fallback after {@link DefaultFactory}.
 * This method performs the tests through the {@link CRS#decode} method.
 * 
 * @source $URL$
 * @version $Id$
 * @author Jody Garnett
 * @author Martin Desruisseaux
 */
public class EpsgFallbackTest extends TestCase {
    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(EpsgFallbackTest.class);
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
    public EpsgFallbackTest(final String name) {
        super(name);
    }

    /**
     * A random CRS for fun.
     */
    public void test26910() throws FactoryException {
        CoordinateReferenceSystem crs = CRS.decode("EPSG:26910");
        assertNotNull(crs);
    }

    /**
     * UDIG requires this to work.
     */
    public void test4326() throws FactoryException {
        CoordinateReferenceSystem crs = CRS.decode("EPSG:4326");
        assertNotNull(crs);
        assertTrue(crs instanceof GeographicCRS);
    }

    /**
     * UDIG requires this to work.
     */
    public void test4269() throws FactoryException {
        CoordinateReferenceSystem crs = CRS.decode("EPSG:4269");
        assertNotNull(crs);
    }

    /**
     * UDIG requires this to work.
     */
    public void test42102() throws FactoryException {
        CoordinateReferenceSystem crs = CRS.decode("EPSG:42102");
        assertNotNull(crs);
        assertNotNull(crs.getIdentifiers());
        assertTrue(!crs.getIdentifiers().isEmpty());
        NamedIdentifier expected = new NamedIdentifier(Citations.EPSG, "42102");
        assertTrue( crs.getIdentifiers() .contains(expected));
    }

    /**
     * A random CRS for fun.
     */
    public void test26910Lower() throws FactoryException {
        CoordinateReferenceSystem crs = CRS.decode("epsg:26910");
        assertNotNull(crs);
    }

    /**
     * A random CRS for fun.
     */
    public void test26986Lower() throws FactoryException {
        CoordinateReferenceSystem crs = CRS.decode("epsg:26986");
        assertNotNull(crs);
    }

    /**
     * WFS requires this to work.
     */
    public void test4326Lower() throws FactoryException {
        CoordinateReferenceSystem crs = CRS.decode("epsg:4326");
        assertNotNull(crs);
    }

    /**
     * WFS requires this to work.
     */
    public void test26742Lower() throws FactoryException {
        CoordinateReferenceSystem crs = CRS.decode("epsg:26742");
        assertNotNull(crs);
    }

    /**
     * WFS requires this to work.
     */
    public void test4269Lower() throws FactoryException {
        CoordinateReferenceSystem crs = CRS.decode("epsg:4269");
        assertNotNull(crs);
    }

    /**
     * WFS requires this to work.
     */
    public void test42304Lower() throws FactoryException {
        CoordinateReferenceSystem crs = CRS.decode("epsg:42304");
        assertNotNull(crs);
    }

    /**
     * WFS requires this to work.
     */
    public void test42102Lower() throws FactoryException {
        CoordinateReferenceSystem crs = CRS.decode("epsg:42102");
        assertNotNull(crs);
        assertNotNull(crs.getIdentifiers());
        assertTrue(!crs.getIdentifiers().isEmpty());
        NamedIdentifier expected = new NamedIdentifier(Citations.EPSG, "42102");
        assertTrue(crs.getIdentifiers() .contains(expected));
    }
}
