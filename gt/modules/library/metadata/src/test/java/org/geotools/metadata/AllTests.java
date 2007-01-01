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
package org.geotools.metadata;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;


/**
 * Performs all tests for the <code>org.geotools.metadata</code> package and dependencies.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class AllTests {
    /**
     * No need to construct this class.
     */
    private AllTests() {
    }

    /**
     * Run the suite from the command line.
     */
    public static void main(final String[] args) {
        org.geotools.util.Logging.GEOTOOLS.forceMonolineConsoleOutput();
        TestRunner.run(suite());
    }

    /**
     * Returns all suites.
     */
    public static Test suite() {
        final TestSuite suite = new TestSuite("org.geotools.metadata");
        suite.addTest(org.geotools.resources.XMathTest              .suite());
        suite.addTest(org.geotools.resources.UtilitiesTest          .suite());
        suite.addTest(org.geotools.resources.ResourceBundleTest     .suite());
        suite.addTest(org.geotools.util     .MonolineFormatterTest  .suite());
        suite.addTest(org.geotools.util     .InternationalStringTest.suite());
        suite.addTest(org.geotools.util     .VersionTest            .suite());
        suite.addTest(org.geotools.util     .RangeSetTest           .suite());
        suite.addTest(org.geotools.util     .DisjointSetTest        .suite());
        suite.addTest(org.geotools.util     .KeySortedListTest      .suite());
        suite.addTest(org.geotools.util     .WeakHashSetTest        .suite());
        suite.addTest(org.geotools.util     .WeakValueHashMapTest   .suite());
        suite.addTest(org.geotools.util     .SoftValueHashMapTest   .suite());
        suite.addTest(org.geotools.math     .GeometryTest           .suite());
        suite.addTest(org.geotools.factory  .AbstractFactoryTest    .suite());
        suite.addTest(org.geotools.factory  .FactoryRegistryTest    .suite());
        return suite;
    }
}
