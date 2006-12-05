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
package org.geotools.coverage;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;


/**
 * Performs all tests for the <code>org.geotools.coverage</code> packages and its dependencies.
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
        org.geotools.util.Logging.GEOTOOLS.forceMonolineConsoleOutput();
        TestRunner.run(suite());
    }

    /**
     * Returns all suites.
     */
    public static Test suite() {
        final TestSuite suite = new TestSuite("org.geotools.coverage");
//      suite.addTest(org.geotools.referencing.  AllTests             .suite());
        suite.addTest(org.geotools.coverage.     CategoryTest         .suite());
        suite.addTest(org.geotools.coverage.     CategoryListTest     .suite());
        suite.addTest(org.geotools.coverage.     SampleDimensionTest  .suite());
        suite.addTest(org.geotools.coverage.     ScaledColorSpaceTest .suite());
        suite.addTest(org.geotools.coverage.grid.GridGeometryTest     .suite());
        suite.addTest(org.geotools.coverage.grid.SampleTranscoderTest .suite());
        suite.addTest(org.geotools.coverage.grid.FloatRasterTest      .suite());
        suite.addTest(org.geotools.coverage.grid.GridCoverageTest     .suite());
        suite.addTest(org.geotools.coverage.grid.InterpolatorTest     .suite());
        suite.addTest(org.geotools.coverage.grid.CropTest             .suite());
        suite.addTest(org.geotools.coverage.grid.ScaleTest            .suite());
        suite.addTest(org.geotools.coverage.grid.ResampleTest         .suite());
        suite.addTest(org.geotools.coverage.grid.FilteredSubsampleTest.suite());
        suite.addTest(org.geotools.coverage.grid.SubsampleAverageTest .suite());
        suite.addTest(org.geotools.coverage.grid.OperationsTest       .suite());
        suite.addTest(org.geotools.coverage.io.MetadataBuilderTest    .suite());
        return suite;
    }
}
