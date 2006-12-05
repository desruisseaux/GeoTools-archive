/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, Geotools Project Managment Committee (PMC)
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

// J2SE and JAI dependencies
import java.awt.image.RenderedImage;
import javax.media.jai.BorderExtender;
import javax.media.jai.BorderExtenderCopy;
import javax.media.jai.Interpolation;
import javax.media.jai.RenderedOp;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// OpenGIS dependencies
import org.opengis.parameter.ParameterValueGroup;
import org.geotools.coverage.processing.AbstractProcessor;

// Geotools dependencies
import org.geotools.coverage.processing.Operations;


/**
 * Tests the crop operation.
 *
 * @source $URL$
 * @version $Id$
 * @author Simone Giannecchini
 *
 * @since 2.3
 */
public class ScaleTest extends GridCoverageTest {
    /**
     * {@code true} if the result should be displayed in windows during test execution.
     * Default to {@code false}. This flag is set to {@code true} only if this test suite
     * is executed explicitly though the {@link #main} method.
     */
    private static boolean SHOW;

    /**
     * The source grid coverage.
     */
    private GridCoverage2D coverage;

    /**
     * Creates a test suite for the given name.
     */
    public ScaleTest(String name) {
        super(name);
    }

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(ScaleTest.class);
    }

    /**
     * Run the suit from the command line.
     */
    public static void main(final String[] args) {
        SHOW = true;
        org.geotools.util.Logging.GEOTOOLS.forceMonolineConsoleOutput(AbstractProcessor.OPERATION);
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Set up common objects used for all tests.
     */
    protected void setUp() throws Exception {
        super.setUp();
        coverage = getExample(0);
    }

    /**
     * Tests the "Scale" operation.
     */
    public void testScale() {
        final AbstractProcessor processor = AbstractProcessor.getInstance();
        GridCoverage2D source = coverage.geophysics(true);
        final ParameterValueGroup param = processor.getOperation("Scale").getParameters();
        param.parameter("Source").setValue(source);
        param.parameter("xScale").setValue(new Float(0.5));
        param.parameter("yScale").setValue(new Float(0.5));
        param.parameter("xTrans").setValue(new Float(0));
        param.parameter("yTrans").setValue(new Float(0));
        param.parameter("Interpolation").setValue(
                Interpolation.getInstance(Interpolation.INTERP_BILINEAR));
        param.parameter("BorderExtender").setValue(
                BorderExtenderCopy.createInstance(BorderExtender.BORDER_COPY));
        GridCoverage2D scaled = (GridCoverage2D) processor.doOperation(param);
        assertNotNull(scaled.getRenderedImage().getData());
        final RenderedImage image = scaled.getRenderedImage();
        scaled = scaled.geophysics(false);
        String operation = null;
        if (image instanceof RenderedOp) {
            operation = ((RenderedOp) image).getOperationName();
            AbstractProcessor.LOGGER.fine("Applied \"" + operation + "\" JAI operation.");
        }
        if (SHOW) {
            Viewer.show(coverage,coverage.getName().toString());
            Viewer.show(scaled,scaled.getName().toString());
        } else {
            // Force computation
            assertNotNull(coverage.getRenderedImage().getData());
            assertNotNull(scaled.getRenderedImage().getData());
        }

        final GridCoverage2D scaledGridCoverage = (GridCoverage2D) Operations.DEFAULT
                .scale(source, 10, 10, 0.0, 0.0, 
                        Interpolation.getInstance(Interpolation.INTERP_BILINEAR),
                        BorderExtender.createInstance(BorderExtender.BORDER_COPY));
        if (SHOW) {
            Viewer.show(scaledGridCoverage,scaledGridCoverage.getName().toString());
        } else {
            // Force computation
            assertNotNull(scaledGridCoverage.getRenderedImage().getData());
        }
    }
}
