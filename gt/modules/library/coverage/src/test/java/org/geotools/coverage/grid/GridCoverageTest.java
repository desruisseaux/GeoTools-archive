/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, Geotools Project Managment Committee (PMC)
 *    (C) 2002, Institut de Recherche pour le D�veloppement
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

// J2SE dependencies and extensions
import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Random;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.units.SI;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// OpenGIS dependencies
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.geometry.Envelope;

// Geotools dependencies
import org.geotools.factory.Hints;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.coverage.Category;
import org.geotools.coverage.FactoryFinder;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.resources.Utilities;
import org.geotools.test.TestData;
import org.geotools.util.NumberRange;

import com.sun.media.jai.codecimpl.util.RasterFactory;


/**
 * Tests the {@link GridCoverage2D} implementation. This class can also be used
 * as a factory for sample {@link GridCoverage2D}, which may be used for tests
 * in other modules. The two following methods are for this purpose:
 *
 * <ul>
 *  <li>{@link #getNumExamples}</li>
 *  <li>{@link #getExample}</li>
 * </ul>
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @todo Refactor this class to some factory that do not extends {@link TestCase}.
 *       Try to make it available to other coverage modules if possible, together
 *       with the test data.
 */
public class GridCoverageTest extends TestCase {
    /**
     * Small value for comparaison of sample values. Since most grid coverage implementation in
     * Geotools 2 store geophysics values as <code>float</code> numbers, this <code>EPS</code>
     * value must be of the order of <code>float</code> relative precision, not <code>double</code>.
     */
    public static final double EPS = 1E-5;

    /**
     * Random number generator for this test.
     */
    private static Random random = new Random(684673898634768L);

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
        for (int i=getNumExamples(); --i>=0;) {
            assertNotNull(getExample(i));
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
     * Applies an operation on the specified coverage, if wanted.
     * The default implementation returns <code>coverage</code>
     * with no change.
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
     * @param coordinateSystem The coverage coordinate reference system.
     * @return A random coverage.
     */
    protected GridCoverage2D getRandomCoverage(final CoordinateReferenceSystem crs) {
        /*
         * Some constants used for the construction and tests of the grid coverage.
         */
        final double      SCALE = 0.1; // Scale factor for pixel transcoding.
        final double     OFFSET = 5.0; // Offset factor for pixel transcoding.
        final double PIXEL_SIZE = .25; // Pixel size (in degrees). Used in transformations.
        final int   BEGIN_VALID = 3;   // The minimal valid index for quantative category.
        /*
         * Constructs the grid coverage. We will assume that the grid coverage use
         * (longitude,latitude) coordinates, pixels of 0.25 degrees and a lower
         * left corner at 10�W 30�N.
         */
        final GridCoverage2D  coverage;  // The final grid coverage.
        final BufferedImage      image;  // The GridCoverage's data.
        final WritableRaster    raster;  // The image's data as a raster.
        final Rectangle2D       bounds;  // The GridCoverage's envelope.
        final GridSampleDimension band;  // The only image's band.
        band = new GridSampleDimension("Temperature", new Category[] {
            new Category("No data",     null, 0),
            new Category("Land",        null, 1),
            new Category("Cloud",       null, 2),
            new Category("Temperature", null, BEGIN_VALID, 256, SCALE, OFFSET)
        }, SI.CELSIUS);
        image  = new BufferedImage(120, 80, BufferedImage.TYPE_BYTE_INDEXED);
        raster = image.getRaster();
        for (int i=raster.getWidth(); --i>=0;) {
            for (int j=raster.getHeight(); --j>=0;) {
                raster.setSample(i,j,0, random.nextInt(256));
            }
        }
        bounds = new Rectangle2D.Double(-10, 30, PIXEL_SIZE*image.getWidth(),
                                                 PIXEL_SIZE*image.getHeight());
        final GeneralEnvelope envelope = new GeneralEnvelope(crs.getCoordinateSystem().getDimension());
        envelope.setRange(0, bounds.getMinX(), bounds.getMaxX());
        envelope.setRange(1, bounds.getMinY(), bounds.getMaxY());
        for (int i=envelope.getDimension(); --i>=2;) {
            envelope.setRange(i, 10*i, 10*i+5);
        }
        envelope.setCoordinateReferenceSystem(crs);
        final Hints                 hints = new Hints(Hints.TILE_ENCODING, "raw");
        final GridCoverageFactory factory = FactoryFinder.getGridCoverageFactory(hints);
        final GridCoverage2D     original = (GridCoverage2D)factory.create("Test", image, envelope,
                                            new GridSampleDimension[]{band}, null, null);
        coverage = transform(original);
        assertEquals("raw", original.tileEncoding);
        /*
         * Grid coverage construction finished. Now test it. Some tests will not be applicable
         * if a subclass overridden the 'transform' method are returned a transformed coverage.
         * We detect this case when 'coverage != original'.
         */
        assertSame(coverage.getRenderedImage(), coverage.getRenderableImage(0,1).createDefaultRendering());
        if (coverage == original) {
            assertSame(image.getTile(0,0), coverage.getRenderedImage().getTile(0,0));
        }
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
        assertTrue( !coverage.equals(geophysics));
        assertTrue( !coverage.getSampleDimension(0).getSampleToGeophysics().isIdentity());
        assertTrue(geophysics.getSampleDimension(0).getSampleToGeophysics().isIdentity());
        /*
         * Compares data. This test can be applied only if no operation were applied
         * on the coverage by some subclass (otherwise we can't check the value since
         * we don't know what the subclass operation did).
         */
        if (coverage == original) {
            final int bandN = 0; // Band to test.
            double[] bufferCov = null;
            double[] bufferGeo = null;
            final double left  = bounds.getMinX() + (0.5*PIXEL_SIZE); // Includes translation to center
            final double upper = bounds.getMaxY() - (0.5*PIXEL_SIZE); // Includes translation to center
            final Point2D.Double point = new Point2D.Double();        // Will maps to pixel center.
            for (int j=raster.getHeight(); --j>=0;) {
                for (int i=raster.getWidth(); --i>=0;) {
                    point.x = left  + PIXEL_SIZE*i;
                    point.y = upper - PIXEL_SIZE*j;
                    double r = raster.getSampleDouble(i,j,bandN);
                    bufferCov =   coverage.evaluate(point, bufferCov);
                    bufferGeo = geophysics.evaluate(point, bufferGeo);
                    assertEquals(r, bufferCov[bandN], EPS);

                    // Compares transcoded samples.
                    if (r < BEGIN_VALID) {
                        assertTrue(Double.isNaN(bufferGeo[bandN]));
                    } else {
                        assertEquals(OFFSET + SCALE*r, bufferGeo[bandN], EPS);
                    }
                }
            }
        }
        return coverage;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    ////////                                                                           ////////
    ////////    FACTORY METHODS FOR SAMPLE GridCoverage                                ////////
    ////////    Those methods do not use any of the above methods in this class.       ////////
    ////////    Factory methods are static and used by some tests in other modules.    ////////
    ////////                                                                           ////////
    ///////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Returns the number of available image which may be used as example.
     */
    public static int getNumExamples() {
        return 1; // TODO: set to '5' if we commit the 'CHL01195.png' image (160 ko).
    }

    /**
     * Returns a {@link GridCoverage} which may be used as a "real world" example.
     *
     * @param  number The example number. Numbers are numeroted from
     *               0 to {@link #getNumExamples()} exclusive.
     * @return The "real world" grid coverage.
     * @throws IOException if an I/O operation was needed and failed.
     */
    public static GridCoverage2D getExample( final int number) throws IOException {
        final GridCoverageFactory factory = FactoryFinder.getGridCoverageFactory(null);
        final String                   path;
        final Category[]         categories;
        final CoordinateReferenceSystem crs;
        final Rectangle2D            bounds;
        final GridSampleDimension[]   bands;
        switch (number) {
            default: {
                throw new IllegalArgumentException(String.valueOf(number));
            }
            case 0: {
                //unit = "�C";
                path = "QL95209.png";
                crs  = DefaultGeographicCRS.WGS84;
                categories = new Category[] {
                    new Category("Coast line", Color.decode("#000000"), new NumberRange(  0,   0)),
                    new Category("Cloud",      Color.decode("#C3C3C3"), new NumberRange(  1,   9)),
                    new Category("Unused",     Color.decode("#822382"), new NumberRange( 10,  29)),
                    new Category("Sea Surface Temperature", null,       new NumberRange( 30, 219), 0.1, 10.0),
                    new Category("Unused",     Color.decode("#A0505C"), new NumberRange(220, 239)),
                    new Category("Land",       Color.decode("#D2C8A0"), new NumberRange(240, 254)),
                    new Category("No data",    Color.decode("#FFFFFF"), new NumberRange(255, 255)),
                };
                // 41�S - 5�N ; 35�E - 80�E  (450 x 460 pixels)
                bounds = new Rectangle2D.Double(35, -41, 45, 46);
                bands = new GridSampleDimension[] {
                        new GridSampleDimension("Measure", categories, null)
                    };
                break;
            }
            case 1: {
                //unit = "mg/m�";
                path = "CHL01195.png";
                crs  = DefaultGeographicCRS.WGS84;
                categories = new Category[] {
                    new Category("Land",       Color.decode("#000000"), new NumberRange(255, 255)),
                    new Category("No data",    Color.decode("#FFFFFF"), new NumberRange(  0,   0)),
                    new Category("Log chl-a",  null,                    new NumberRange(  1, 254), 0.015, -1.985)
                };
                // 34�N - 45�N ; 07�W - 12�E  (1200 x 700 pixels)
                bounds = new Rectangle2D.Double(-7, 34, 19, 11);
                bands = new GridSampleDimension[] {
                        new GridSampleDimension("Measure", categories, null).geophysics(false)
                    };
                
                break;
            }
            case 2: {
            	////
            	//
            	// 	WORLD DEM
            	//
            	////
            	path   = "world_dem.gif";
                bounds = new Rectangle2D.Double(-180,-90, 360, 180);
                crs    = DefaultGeographicCRS.WGS84;
                bands=null;
                break;
            }
            case 3:{
            	////
            	//
            	// 	WORLD BATHY
            	//
            	////
            	path   = "BATHY.gif";
                bounds = new Rectangle2D.Double(-180,-90, 360, 180);
                crs    = DefaultGeographicCRS.WGS84;
                bands  = null;
                break;
            }
            case 4: {
            	////
            	//
            	// 	A float coverage
            	//
            	////
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
                Color[] colors = new Color[] {Color.BLUE, Color.CYAN, Color.WHITE, Color.YELLOW, Color.RED};
                return factory.create("My colored coverage", raster,
                        new Envelope2D(DefaultGeographicCRS.WGS84, 35, -41, 35+45, -41+46),
                                    null, null, null, new Color[][] {colors}, null);
                
            }
        }
        final GeneralEnvelope envelope = new GeneralEnvelope(bounds);
        final RenderedImage      image = ImageIO.read(TestData.getResource(GridCoverageTest.class, path));
        final String          filename = new File(path).getName();
        envelope.setCoordinateReferenceSystem(crs);
        return (GridCoverage2D) factory.create(filename, image, envelope, bands, null, null);
    }
}
