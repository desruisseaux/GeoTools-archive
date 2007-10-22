/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, Geotools Project Managment Committee (PMC)
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
package org.geotools.coverage.grid;

// J2SE dependencies
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Logger;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// OpenGIS dependencies
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.resources.Utilities;


/**
 * Tests the {@link GridCoverage2D} implementation.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class GridCoverageTest extends TestCase {
    /**
     * Run the suite from the command line.
     */
    public static void main(final String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(GridCoverageTest.class);
    }
    
    /**
     * Constructs a test case with the given name.
     */
    public GridCoverageTest(final String name) {
        super(name);
    }

    /**
     * Tests the construction and access to a grid coverage.
     *
     * @throws IOException if an I/O operation was needed and failed.
     */
    public void testGridCoverage() throws IOException {
        final GridCoverage2D coverage = getRandomCoverage();
        assertNotNull(coverage);
        // Not much more test to do here, since most tests has been done
        // inside 'getRandomCoverage'. This method will be overridden by
        // 'InterpolatorTest', which will perform more tests.
        for (int i=GridCoverageExamples.getNumExamples(); --i>=0;) {
            assertNotNull(GridCoverageExamples.getExample(i));
        }
    }

    /**
     * Tests the serialization of a grid coverage.
     *
     * @throws IOException if an I/O operation was needed and failed.
     */
    public void testSerialization() throws IOException, ClassNotFoundException {
        final GridCoverage2D coverage = getRandomCoverage();
        assertNotNull(coverage);
        coverage.tileEncoding = null;
        /*
         * The previous line is not something that we should do.
         * But we want to test the default GridCoverage2D encoding.
         */
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        final ObjectOutputStream out = new ObjectOutputStream(buffer);
        try {
            out.writeObject(coverage.geophysics(false));
//          out.writeObject(coverage.geophysics(true ));
        } catch (IllegalArgumentException e) {
            /*
             * TODO: this exception occurs when ImageLayout contains a SampleModel or a ColorModel
             *       unknow to javax.media.jai.remote.SerializerFactory getState(...) method. This
             *       happen if an operation we applied on the coverage in some subclass (especially
             *       OperationsTest). Ignore the exception for now, but we need to revisit this
             *       issue later.
             */
            if (getClass().equals(GridCoverageTest.class)) {
                e.printStackTrace();
            }
            out.close();
            return;
        }
        out.close();
        /*
         * Deserialization requires J2SE 1.5 or above.
         */
        if (System.getProperty("java.version").compareTo("1.5") >= 0) {
            final ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
            GridCoverage2D read;
            read = (GridCoverage2D) in.readObject(); assertSame(read, read.geophysics(false));
//          read = (GridCoverage2D) in.readObject(); assertSame(read, read.geophysics(true ));
//          assertNotSame(read, read.geophysics(true));
            in.close();
        } else {
            Logger.getLogger("org.geotools.coverage.grid")
                  .fine("Deserialization test skipped for pre-1.5 Java version.");
        }
    }

    /**
     * Applies an operation on the specified coverage, if wanted. The
     * default implementation returns {@code coverage} with no change.
     */
    protected GridCoverage2D transform(final GridCoverage2D coverage) {
        return coverage;
    }

    /**
     * Returns a grid coverage filled with random values. The coordinate
     * reference system default to {@link DefaultGeographicCRS#WGS84}.
     *
     * @return A random coverage.
     */
    protected GridCoverage2D getRandomCoverage() {
        return getRandomCoverage(DefaultGeographicCRS.WGS84);
    }

    /**
     * Returns a grid coverage filled with random values.
     *
     * @param crs The coverage coordinate reference system.
     * @return A random coverage.
     */
    protected GridCoverage2D getRandomCoverage(final CoordinateReferenceSystem crs) {
        final GridCoverage2D original = GridCoverageExamples.getRandomCoverage(crs);
        final GridCoverage2D coverage = transform(original);
        /*
         * Grid coverage construction finished. Now test it. Some tests will not be applicable
         * if a subclass overridden the 'transform' method are returned a transformed coverage.
         * We detect this case when 'coverage != original'.
         */
        assertSame(coverage.getRenderedImage(), coverage.getRenderableImage(0,1).createDefaultRendering());
        if (!coverage.getCoordinateReferenceSystem().equals(crs)) {
            assertEquals("Resampler2D", Utilities.getShortClassName(coverage));
        }
        /*
         * Tests the creation of a "geophysics" view. This test make sure that the
         * 'geophysics' method do not creates more grid coverage than needed.
         */
        GridCoverage2D geophysics= coverage.geophysics(true);
        assertSame(coverage,       coverage.geophysics(false));
        assertSame(coverage,     geophysics.geophysics(false));
        assertSame(geophysics,   geophysics.geophysics(true ));
        assertFalse( coverage.equals(geophysics));
        assertFalse( coverage.getSampleDimension(0).getSampleToGeophysics().isIdentity());
        assertTrue(geophysics.getSampleDimension(0).getSampleToGeophysics().isIdentity());
        return coverage;
    }
}
