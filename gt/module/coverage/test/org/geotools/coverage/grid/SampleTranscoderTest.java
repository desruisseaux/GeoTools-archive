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
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.RenderedImage;
import java.util.Random;

// JAI dependencies
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedImageAdapter;
import javax.media.jai.RenderedOp;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// OpenGIS dependencies
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

// Geotools dependencies
import org.geotools.coverage.Category;
import org.geotools.coverage.CategoryListTest;
import org.geotools.coverage.FactoryFinder;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.operation.transform.IdentityTransform;


/**
 * Test the {@link SampleTranscoder} implementation. Image adapter depends
 * heavily on {@link CategoryList}, so this one should be tested first.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class SampleTranscoderTest extends TestCase {
    /**
     * Small value for comparaisons. Remind: transformed values are stored in a new image
     * using the 'float' data type. So we can't expected as much precision than with a
     * 'double' data type.
     */
    private static final double EPS = 1E-5;

    /**
     * Random number generator for this test.
     */
    private static final Random random = new Random(6215962897884256696L);

    /**
     * A sample dimension for a band.
     */
    private GridSampleDimension band1;

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
        return new TestSuite(SampleTranscoderTest.class);
    }

    /**
     * Constructs a test case with the given name.
     */
    public SampleTranscoderTest(final String name) {
        super(name);
    }

    /**
     * Set up common objects used for all tests.
     */
    protected void setUp() throws Exception {
        super.setUp();
        band1 = new GridSampleDimension("a",new Category[] {
            new Category("No data",     null, 0),
            new Category("Land",        null, 1),
            new Category("Clouds",      null, 2),
            new Category("Temperature", null, 3, 100, 0.1, 5),
            new Category("Foo",         null, 100, 160, -1, 3),
            new Category("Tarzan",      null, 160)
        }, null);
    }

    /**
     * Tests the transformation using a random raster with only one band.
     */
    public void testOneBand() throws TransformException {
        assertTrue(testOneBand(1,  0) instanceof RenderedImageAdapter);
        assertTrue(testOneBand(.8, 2) instanceof RenderedOp);
        assertTrue(testOneBand(band1) instanceof RenderedOp);
    }

    /**
     * Tests the transformation using a random raster with only one band.
     * A sample dimension with only one category will be used.
     *
     * @param  scale The scale factor.
     * @param  offset The offset value.
     * @return The transformed image.
     */
    private RenderedImage testOneBand(final double scale, final double offset) throws TransformException {
        final Category category = new Category("Values", null, 0, 256, scale, offset);
        return testOneBand(new GridSampleDimension("a",new Category[] {category}, null));
    }

    /**
     * Tests the transformation using a random raster with only one band.
     *
     * @param  band The sample dimension for the only band.
     * @return The transformed image.
     */
    private RenderedImage testOneBand(final GridSampleDimension band) throws TransformException {
        final int SIZE = 64;
        /*
         * Constructs a 64x64 image with random values.
         * Samples values are integer in the range 0..160 inclusive.
         */
        final BufferedImage  source = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_BYTE_INDEXED);
        final DataBufferByte buffer = (DataBufferByte) source.getRaster().getDataBuffer();
        final byte[] array = buffer.getData(0);
        for (int i=0; i<array.length; i++) {
            array[i] = (byte) random.nextInt(161);
        }
        final MathTransform identity = IdentityTransform.create(2);
        final GridCoverageFactory factory = FactoryFinder.getGridCoverageFactory(null);
        GridCoverage2D coverage;
        coverage = (GridCoverage2D) factory.create("Test", source, DefaultGeographicCRS.WGS84,
                                        identity, new GridSampleDimension[]{band}, null, null);
        /*
         * Apply the operation. The SampleTranscoder class is suppose to transform our
         * integers into real-world values. Check if the result use floating-points.
         */
        final RenderedImage target = coverage.geophysics(true).getRenderedImage();
        assertSame(target, PlanarImage.wrapRenderedImage(target));
        assertEquals(DataBuffer.TYPE_BYTE, source.getSampleModel().getDataType());
        if (coverage.getRenderedImage() != target) {
            assertEquals(DataBuffer.TYPE_FLOAT, target.getSampleModel().getDataType());
        }
        /*
         * Now, gets the data as an array and compare it with the expected values.
         */
        double[] sourceData = source.getData().getSamples(0, 0, SIZE, SIZE, 0, (double[])null);
        double[] targetData = target.getData().getSamples(0, 0, SIZE, SIZE, 0, (double[])null);
        band.getSampleToGeophysics().transform(sourceData, 0, sourceData, 0, sourceData.length);
        CategoryListTest.compare(sourceData, targetData, EPS);
        /*
         * Construct a new image with the resulting data, and apply an inverse transformation.
         * Compare the resulting values with the original data.
         */
        RenderedImage back = PlanarImage.wrapRenderedImage(target).getAsBufferedImage();
        coverage = (GridCoverage2D) factory.create("Test", back, DefaultGeographicCRS.WGS84,
                    identity, new GridSampleDimension[]{band.geophysics(true)}, null, null);

        back = coverage.geophysics(false).getRenderedImage();
        assertEquals(DataBuffer.TYPE_BYTE, back.getSampleModel().getDataType());
        sourceData = source.getData().getSamples(0, 0, SIZE, SIZE, 0, (double[])null);
        targetData =   back.getData().getSamples(0, 0, SIZE, SIZE, 0, (double[])null);
        CategoryListTest.compare(sourceData, targetData, 1+EPS);
        /*
         * Returns the "geophysics view" of the image.
         */
        return target;
    }
}
