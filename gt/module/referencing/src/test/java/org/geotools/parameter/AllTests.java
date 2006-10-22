/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, Geotools Project Managment Committee (PMC)
 *    (C) 2002, Institut de Recherche pour le Développement
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
package org.geotools.parameter;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;


/**
 * Performs all tests for the <code>org.geotools.parameter</code> packages.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class AllTests extends TestCase {
    /** No need to construct this class. */
    private AllTests() {
    }

    /**
     * Run the suite from the command line.
     */
    public static void main(final String[] args) {
        org.geotools.util.MonolineFormatter.initGeotools();
        TestRunner.run(suite());
    }

    /**
     * Returns all suites.
     */
    public static Test suite() {
        final TestSuite suite = new TestSuite("org.geotools.referencing");
        suite.addTest(ParametersTest       .suite());
        suite.addTest(ImagingParametersTest.suite());
        return suite;
    }
}
