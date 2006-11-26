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
package org.geotools.coverage;

// J2SE dependencies
import java.awt.image.BufferedImage;
import java.util.Random;

// JAI dependencies
import javax.media.jai.JAI;
import javax.media.jai.OperationRegistry;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.ParameterListDescriptor;
import javax.media.jai.RenderedOp;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Test the {@link GridSampleDimension} implementation. Since {@code GridSampleDimension}
 * rely on {@link CategoryList} for many of its work, many {@code GridSampleDimension}
 * tests are actually {@code CategoryList} tests.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class SampleDimensionTest extends TestCase {
    /**
     * The categories making the sample dimension to test.
     */
    private static final String[] CATEGORIES = {
        "No data",
        "Clouds",
        "Lands"
    };

    /**
     * The "no data" values making the sample dimension to test.
     * There is one for each category in {@link #CATEGORIES}.
     */
    private static final int[] NO_DATA = {0, 1, 255};

    /**
     * The minimal value for the geophysics category, inclusive.
     */
    private static final int minimum = 10;

    /**
     * The maximal value for the geophysics category, exclusive.
     */
    private static final int maximum = 200;

    /**
     * The scale factor for the sample dimension to test.
     */
    private static final double scale  = 0.1;

    /**
     * The offset value for the sample dimension to test.
     */
    private static final double offset = 5.0;

    /**
     * Small number for comparaison.
     */
    private static final double EPS = 1E-7;

    /**
     * The sample dimension to test.
     */
    private GridSampleDimension test;

    /**
     * Random number generator for this test.
     */
    private static final Random random = new Random(5134392769888592001L);

    /**
     * Run the suit from the command line.
     */
    public static void main(final String[] args) {
        org.geotools.util.Logging.GEOTOOLS.forceMonolineConsoleOutput();
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(SampleDimensionTest.class);
    }
    
    /**
     * Constructs a test case with the given name.
     */
    public SampleDimensionTest(final String name) {
        super(name);
    }

    /**
     * Set up common objects used for all tests.
     */
    protected void setUp() throws Exception {
        super.setUp();

        assertEquals("setUp", CATEGORIES.length, NO_DATA.length);
        final Category[] categories = new Category[CATEGORIES.length+1];
        for (int i=0; i<CATEGORIES.length; i++) {
            categories[i] = new Category(CATEGORIES[i], null, NO_DATA[i]);
        }
        categories[CATEGORIES.length] = new Category("SST", null, minimum, maximum, scale, offset);
        test = new GridSampleDimension("A",categories, null);
    }

    /**
     * Test the consistency of the sample dimension.
     */
    public void testSampleDimension() {
        final double[] nodataValues = test.getNoDataValues();
        assertEquals("nodataValues.length", CATEGORIES.length, nodataValues.length);
        for (int i=0; i<CATEGORIES.length; i++) {
            assertEquals("nodataValues["+i+']', NO_DATA[i], nodataValues[i], 0);
        }
        assertTrue  ("identity", !test.getSampleToGeophysics().isIdentity());
        assertEquals("scale",     scale,  test.getScale(),        0);
        assertEquals("offset",    offset, test.getOffset(),       0);
        assertEquals("minimum",   0,      test.getMinimumValue(), 0);
        assertEquals("maximum",   255,    test.getMaximumValue(), 0);

        final GridSampleDimension invt = test.geophysics(true);
        assertTrue(test != invt);
        assertTrue  ("identity", invt.getSampleToGeophysics().isIdentity());
        assertEquals("scale",    1,                       invt.getScale(),        0);
        assertEquals("offset",   0,                       invt.getOffset(),       0);
        assertEquals("minimum",  minimum   *scale+offset, invt.getMinimumValue(), 0);
        assertEquals("maximum", (maximum-1)*scale+offset, invt.getMaximumValue(), EPS);
    }

    /**
     * Test the creation of an {@link SampleTranscoder} using the image operation registry.
     * This allow to apply the operation in the same way than other JAI operations, without
     * any need for a direct access to package-private method.
     */
    public void testSampleTranscoderCreation() {
        final OperationRegistry registry = JAI.getDefaultInstance().getOperationRegistry();
        assertNotNull(registry.getDescriptor("rendered", SampleTranscoder.OPERATION_NAME));

        final BufferedImage       dummy = new BufferedImage(10, 10, BufferedImage.TYPE_BYTE_GRAY);
        final ParameterBlockJAI   param = new ParameterBlockJAI(SampleTranscoder.OPERATION_NAME);
        final ParameterListDescriptor d = param.getParameterListDescriptor();
        assertTrue(d.getParamClasses()[0].equals(GridSampleDimension[].class));

        try {
            JAI.create(SampleTranscoder.OPERATION_NAME, param);
            fail();
        } catch (IllegalArgumentException expected) {
            // This is the expected exception: source required.
        }

        param.addSource(dummy);
        try {
            JAI.create(SampleTranscoder.OPERATION_NAME, param);
            fail();
        } catch (IllegalArgumentException expected) {
            // This is the expected exception: parameter required.
        }

        param.setParameter("sampleDimensions", new GridSampleDimension[] {test});
        final RenderedOp op = JAI.create(SampleTranscoder.OPERATION_NAME, param);
        assertTrue(op.getRendering() instanceof SampleTranscoder);
    }

    /**
     * Test the {@link GridSampleDimension#rescale} method.
     */
    public void testRescale() {
        GridSampleDimension scaled;
        scaled = test.geophysics(false).rescale(2, -6);
        assertEquals("Incorrect scale",     2, scaled.getScale(),        EPS);
        assertEquals("Incorrect offset",   -6, scaled.getOffset(),       EPS);
        assertEquals("Incorrect minimum",   0, scaled.getMinimumValue(), EPS);
        assertEquals("Incorrect maximum", 255, scaled.getMaximumValue(), EPS);

        scaled = test.geophysics(true).rescale(2, -6).geophysics(false);
        assertEquals("Incorrect scale",   scale*2,         scaled.getScale(),        EPS);
        assertEquals("Incorrect offset",  offset*2-6,      scaled.getOffset(),       EPS);
        assertEquals("Incorrect minimum",   0,             scaled.getMinimumValue(), EPS);
        assertEquals("Incorrect maximum", 255,             scaled.getMaximumValue(), EPS);
    }
}
