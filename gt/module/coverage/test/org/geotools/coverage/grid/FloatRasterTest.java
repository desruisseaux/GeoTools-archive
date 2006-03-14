/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2006, Geotools Project Managment Committee (PMC)
 * (C) 2006, Institut de Recherche pour le Développement
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
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.geotools.coverage.grid;

// J2SE and JAI dependencies
import java.awt.Color;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;
import javax.media.jai.RasterFactory;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// GeoAPI dependencies
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.spatialschema.geometry.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

// Geotools dependencies
import org.geotools.geometry.Envelope2D;
import org.geotools.coverage.FactoryFinder;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.referencing.crs.DefaultGeographicCRS;


/**
 * Tests the creation of a grid coverage using floating point value.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class FloatRasterTest extends TestCase {
    /**
     * Tells if the test should show the image in a windows. Set to {@code true} only if this
     * test is executed from the command line (instead than from Maven or Netbeans for example).
     */
    private static boolean display;

    /**
     * Run the suite from the command line.
     */
    public static void main(final String[] args) {
        display = true;
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(FloatRasterTest.class);
    }

    /**
     * Constructs a test case with the given name.
     */
    public FloatRasterTest(final String name) {
        super(name);
    }

    /**
     * Tests the creation of a floating point {@link WritableRaster}.
     */
    public void testRaster() {
        /*
         * Set the pixel values.  Because we use only one tile with one band, the code below
         * is pretty similar to the code we would have if we were just setting the values in
         * a matrix.
         */
        final int width  = 500;
        final int height = 500;
        WritableRaster raster = RasterFactory.createBandedRaster(DataBuffer.TYPE_FLOAT,
                                                                 width, height, 1, null);
        for (int y=0; y<height; y++) {
            for (int x=0; x<width; x++) {
                raster.setSample(x, y, 0, x+y);
            }
        }
        /*
         * Set some metadata (the CRS, the geographic envelope, etc.) and display the image.
         * The display may be slow, since the translation from floating-point values to some
         * color (or grayscale) is performed on the fly everytime the image is rendered.
         */
        CoordinateReferenceSystem crs = DefaultGeographicCRS.WGS84;
        Envelope envelope = new Envelope2D(crs, 0, 0, 30, 30);
        GridCoverageFactory factory = FactoryFinder.getGridCoverageFactory(null);
        GridCoverage gc = factory.create("My grayscale coverage", raster, envelope);
        if (display) ((GridCoverage2D) gc).show(); // Convenience method specific to Geotools.
        /*
         * The above example created a grayscale image. The example below creates a new grid
         * coverage for the same data, but using a specified color map. Note that the factory
         * used allows more details to be specified, for example units. Setting some of those
         * arguments to null (as in this example) lets GridCoverage computes automatically a
         * default value.
         */
        Color[] colors = new Color[] {Color.BLUE, Color.CYAN, Color.WHITE, Color.YELLOW, Color.RED};
        gc = factory.create("My colored coverage", raster, envelope,
                            null, null, null, new Color[][] {colors}, null);
        if (display) ((GridCoverage2D) gc).geophysics(false).show();
    }

    /**
     * Tests the creation of a floating point matrix.
     */
    public void testMatrix() {
        final int width  = 500;
        final int height = 500;
        final float[][] matrix = new float[height][width];
        for (int y=0; y<height; y++) {
            for (int x=0; x<width; x++) {
                matrix[y][x] = x+y;
            }
        }
        CoordinateReferenceSystem crs = DefaultGeographicCRS.WGS84;
        Envelope envelope = new Envelope2D(crs, 0, 0, 30, 30);
        GridCoverageFactory factory = FactoryFinder.getGridCoverageFactory(null);
        GridCoverage gc = factory.create("My grayscale matrix", matrix, envelope);
        if (display) ((GridCoverage2D) gc).show(); // Convenience method specific to Geotools.
    }
}
