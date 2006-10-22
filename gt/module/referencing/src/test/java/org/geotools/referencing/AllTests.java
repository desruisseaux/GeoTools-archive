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
package org.geotools.referencing;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;


/**
 * Performs all tests for the <code>org.geotools.referencing</code> packages. This
 * also includes tests in some dependencies, like <code>org.geotools.measure</code>
 * and <code>org.geotools.parameter</code> packages.
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
        suite.addTest(org.geotools.math                            .AllTests                 .suite());
        suite.addTest(org.geotools.measure                         .AllTests                 .suite());
        suite.addTest(org.geotools.parameter                       .AllTests                 .suite());
        suite.addTest(org.geotools.factory                         .FactoryRegistryTest      .suite());
        suite.addTest(org.geotools.referencing                     .BasicTest                .suite());
        suite.addTest(org.geotools.referencing                     .CreationTest             .suite());
        suite.addTest(org.geotools.referencing                     .WKTParserTest            .suite());
        suite.addTest(org.geotools.referencing                     .GeodeticCalculatorTest   .suite());
        suite.addTest(org.geotools.referencing.operation           .BasicTest                .suite());
        suite.addTest(org.geotools.referencing.operation           .Transform3DTest          .suite());
        suite.addTest(org.geotools.referencing.operation           .TransformationTest       .suite());
        suite.addTest(org.geotools.referencing.operation.transform .MathTransformTest        .suite());
        suite.addTest(org.geotools.referencing.operation.transform .GeocentricTransformTest  .suite());
        suite.addTest(org.geotools.referencing.operation.transform .GeocentricTranslationTest.suite());
        suite.addTest(org.geotools.referencing.operation.transform .PassthroughTransformTest .suite());
        suite.addTest(org.geotools.referencing.operation.transform .LocalizationGridTest     .suite());
        suite.addTest(org.geotools.referencing.operation.transform .WarpTransformTest        .suite());
        suite.addTest(org.geotools.referencing.operation.projection.MathTransformTest        .suite());
        suite.addTest(org.geotools.referencing.operation.projection.NewZealandMapGridTest    .suite());
        suite.addTest(org.geotools.referencing                     .ScriptTest               .suite());
        suite.addTest(org.geotools.referencing                     .CrsTest                  .suite());
        suite.addTest(org.geotools.referencing.factory.wms         .AUTOTest                 .suite());
        suite.addTest(org.geotools.referencing.factory.wms         .CRSTest                  .suite());
        /*
         * If the EPSG authority factory on HSQL is available in the class path, add its tests.
         * It is never the case when the referencing module is build by Maven  (the EPSG tests
         * will be run at plugin/epsg-hsql building time instead). But it is sometime the case
         * when the tests are run from the command line.
         */
        try {
            final Class c = Class.forName("org.geotools.referencing.factory.epsg.AllTests");
            suite.addTest((Test) c.getMethod("suite", (Class[]) null).invoke(null, (Object[])null));
        } catch (Exception ignore) {
            /*
             * EPSG tests not found on the class path (which may be normal), or method invocation
             * failed (which should not happen). Ignore, because the addition of those tests here
             * was just a convenience. All tests will be run at Maven build anyway.
             */
        }
        /*
         * The following must be tested after the EPSG factory tests, because it may involves more
         * interactions with the EPSG factory.  No work on the database-backed EPSG factory should
         * be performed before the tests from the EPSG module (the 'try {...} catch' block above),
         * otherwise the org.geotools.referencing.factory.epsg.DefaultDataSourceTest.testTimeout()
         * test may fails.
         */
        suite.addTest(org.geotools.referencing.factory.AllAuthoritiesFactoryTest.suite());
        return suite;
    }
}
