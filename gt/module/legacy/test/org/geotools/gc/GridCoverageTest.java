/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
 * (C) 2002, Institut de Recherche pour le Développement
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
package org.geotools.gc;

// J2SE dependencies
import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Random;

import javax.imageio.ImageIO;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.geotools.cs.CoordinateSystem;
import org.geotools.cs.GeographicCoordinateSystem;
import org.geotools.cv.Category;
import org.geotools.cv.SampleDimension;
import org.geotools.pt.Envelope;
import org.geotools.resources.TestData;
import org.geotools.units.Unit;
import org.geotools.util.NumberRange;


/**
 * Test the {@link GridCoverage} implementation. This class can also be used
 * as a factory for sample {@link GridCoverage}, which may be used for tests
 * in other modules. The two following methods are for this purpose:
 *
 * <ul>
 *  <li>{@link #getNumExamples}</li>
 *  <li>{@link #getExample}</li>
 * </ul>
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @todo Move and rename this file as TestCoverageFactory.
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
    private static Random random = new Random();;

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
     * Apply an operation on the specified coverage, if wanted.
     * The default implementation returns <code>coverage</code>
     * with no change.
     */
    protected GridCoverage transform(final GridCoverage coverage) {
        return coverage;
    }

    /**
     * Returns a grid coverage filled with random values. The coordinate
     * system default to {@link GeographicCoordinateSystem#WGS84}.
     *
     * @return A random coverage.
     */
    protected GridCoverage getRandomCoverage() {
        return getRandomCoverage(GeographicCoordinateSystem.WGS84);
    }

    /**
     * Returns a grid coverage filled with random values.
     *
     * @param coordinateSystem The coverage coordinate system.
     * @return A random coverage.
     */
    protected GridCoverage getRandomCoverage(final CoordinateSystem coordinateSystem) {
        /*
         * Some constants used for the construction and test of the grid coverage.
         */
        final double      SCALE = 0.1; // Scale factor for pixel transcoding.
        final double     OFFSET = 5.0; // Offset factor for pixel transcoding.
        final double PIXEL_SIZE = .25; // Pixel size (in degrees). Used in transformations.
        final int   BEGIN_VALID = 3;   // The minimal valid index for quantative category.
        /*
         * Construct the grid coverage. We will assume that the grid coverage use
         * (longitude,latitude) coordinates, pixels of 0.25 degrees and a lower
         * left corner at 10°W 30°N.
         */
        final GridCoverage coverage;  // The grid coverage.
        final BufferedImage   image;  // The GridCoverage's data.
        final WritableRaster raster;  // The image's data as a raster.
        final Rectangle2D    bounds;  // The GridCoverage's envelope.
        final SampleDimension  band;  // The only image's band.

        band = new SampleDimension(new Category[] {
            new Category("No data",     null, 0),
            new Category("Land",        null, 1),
            new Category("Cloud",       null, 2),
            new Category("Temperature", null, BEGIN_VALID, 256, SCALE, OFFSET)
        }, Unit.get("°C"));
        image  = new BufferedImage(120, 80, BufferedImage.TYPE_BYTE_INDEXED);
        raster = image.getRaster();
        for (int i=raster.getWidth(); --i>=0;) {
            for (int j=raster.getHeight(); --j>=0;) {
                raster.setSample(i,j,0, random.nextInt(256));
            }
        }
        bounds = new Rectangle2D.Double(-10, 30, PIXEL_SIZE*image.getWidth(),
                                                 PIXEL_SIZE*image.getHeight());
        final Envelope envelope = new Envelope(coordinateSystem.getDimension());
        envelope.setRange(0, bounds.getMinX(), bounds.getMaxX());
        envelope.setRange(1, bounds.getMinY(), bounds.getMaxY());
        for (int i=coordinateSystem.getDimension(); --i>=2;) {
            envelope.setRange(i, 10*i, 10*i+5);
        }
        coverage = transform(new GridCoverage("Test", image, coordinateSystem,
                                              envelope, new SampleDimension[]{band},
                                              null, null));

        /* ----------------------------------------------------------------------------------------
         *
         * Grid coverage construction finished. Now, test it.
         */
        assertSame(image.getTile(0,0), coverage.getRenderedImage().getTile(0,0));

        // Test the creation of a "geophysics" view.
        GridCoverage geophysics= coverage.geophysics(true);
        assertSame(coverage,     coverage.geophysics(false));
        assertSame(coverage,   geophysics.geophysics(false));
        assertSame(geophysics, geophysics.geophysics(true ));
        assertTrue(!coverage.equals(geophysics));

        // Test sample dimensions.
        assertTrue( !coverage.getSampleDimensions()[0].getSampleToGeophysics().isIdentity());
        assertTrue(geophysics.getSampleDimensions()[0].getSampleToGeophysics().isIdentity());

        // Compare data.
        final int bandN = 0; // Band to test.
        double[] bufferCov = null;
        double[] bufferGeo = null;
        final double left  = bounds.getMinX() + (0.5*PIXEL_SIZE); // Includes translation to center
        final double upper = bounds.getMaxY() - (0.5*PIXEL_SIZE); // Includes translation to center
        final Point2D.Double point = new Point2D.Double(); // Will maps to pixel center.
        for (int j=raster.getHeight(); --j>=0;) {
            for (int i=raster.getWidth(); --i>=0;) {
                point.x = left  + PIXEL_SIZE*i;
                point.y = upper - PIXEL_SIZE*j;
                double r = raster.getSampleDouble(i,j,bandN);
                bufferCov =   coverage.evaluate(point, bufferCov);
                bufferGeo = geophysics.evaluate(point, bufferGeo);
                assertEquals(r, bufferCov[bandN], EPS);

                // Compare transcoded samples.
                if (r < BEGIN_VALID) {
                    assertTrue(Double.isNaN(bufferGeo[bandN]));
                } else {
                    assertEquals(OFFSET + SCALE*r, bufferGeo[bandN], EPS);
                }
            }
        }
        return coverage;
    }

    /**
     * Test the construction and access to a grid coverage.
     *
     * @throws IOException if an I/O operation was needed and failed.
     */
    public void testGridCoverage() throws IOException {
        final GridCoverage coverage = getRandomCoverage();
        assertNotNull(coverage);
        // Not much more test to do here, since most tests has been done
        // inside 'getRandomCoverage'.  This method will be overriden by
        // 'InterpolatorTest', which will perform more tests.
        for (int i=getNumExamples(); --i>=0;) {
            assertNotNull(getExample(i));
        }
    }

    /**
     * Test the serialization of a grid coverage.
     *
     * @throws IOException if an I/O operation was needed and failed.
     *
     * @todo: this test is not yet enabled.
     */
    private void serialization() throws IOException, ClassNotFoundException {
        final GridCoverage coverage = getRandomCoverage();
        assertNotNull(coverage);
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        final ObjectOutputStream out = new ObjectOutputStream(buffer);
        out.writeObject(coverage);
        out.close();

        final ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        final GridCoverage read = (GridCoverage) in.readObject();
        in.close();
    }

//    public static void main(String[] args) throws Exception {
//        new GridCoverageTest(null).serialization();
//    }


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
        return 1; // TODO: set to '2' if we commit the 'CHL01195.png' image (160 ko).
    }

    /**
     * Returns a {@link GridCoverage} which may be used as a "real world" example.
     *
     * @param  number The example number. Numbers are numeroted from
     *               0 to {@link #getNumExamples()} exclusive.
     * @return The "real world" grid coverage.
     * @throws IOException if an I/O operation was needed and failed.
     */
    public static GridCoverage getExample( final int number) throws IOException {
        final String           path;
        final String           unit;
        final Category[] categories;
        final CoordinateSystem   cs;
        final Rectangle2D    bounds;


        switch (number) {
            default: {
                throw new IllegalArgumentException(String.valueOf(number));
            }
            case 0: {
                //unit = "°C";
                path = "QL95209.png";
                cs   = GeographicCoordinateSystem.WGS84;
                categories = new Category[] {
                    new Category("Coast line", Color.decode("#000000"), new NumberRange(  0,   0)),
                    new Category("Cloud",      Color.decode("#C3C3C3"), new NumberRange(  1,   9)),
                    new Category("Unused",     Color.decode("#822382"), new NumberRange( 10,  29)),
                    new Category("Sea Surface Temperature", null,       new NumberRange( 30, 219), 0.1, 10.0),
                    new Category("Unused",     Color.decode("#A0505C"), new NumberRange(220, 239)),
                    new Category("Land",       Color.decode("#D2C8A0"), new NumberRange(240, 254)),
                    new Category("No data",    Color.decode("#FFFFFF"), new NumberRange(255, 255)),
                };
                // 41°S - 5°N ; 35°E - 80°E  (450 x 460 pixels)
                bounds = new Rectangle2D.Double(35, -41, 45, 46);
                break;
            }
            case 1: {
                //unit = "mg/m³";
                path = "CHL01195.png";
                cs   = GeographicCoordinateSystem.WGS84;
                categories = new Category[] {
                    new Category("Land",       Color.decode("#000000"), new NumberRange(255, 255)),
                    new Category("No data",    Color.decode("#FFFFFF"), new NumberRange(  0,   0)),
                    new Category("Log chl-a",  null,                    new NumberRange(  1, 254), 0.015, -1.985)
                };
                // 34°N - 45°N ; 07°W - 12°E  (1200 x 700 pixels)
                bounds = new Rectangle2D.Double(-7, 34, 19, 11);
                break;
            }
        }

        final SampleDimension[] bands = new SampleDimension[] {
            new SampleDimension(categories, null)
        };
        final Envelope   envelope = new Envelope(bounds);
        final RenderedImage image = ImageIO.read(TestData.getResource(GridCoverageTest.class, path));
        final String     filename = new File(path).getName();
        return new GridCoverage(filename, image, cs, envelope, bands, null, null);
    }
}
