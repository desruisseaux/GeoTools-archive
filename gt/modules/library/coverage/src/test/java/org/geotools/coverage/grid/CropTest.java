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
import java.awt.geom.AffineTransform;
import java.awt.image.RenderedImage;
import java.util.logging.Logger;
import javax.media.jai.RenderedOp;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// OpenGIS dependencies
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

// Geotools dependencies
import org.geotools.coverage.processing.AbstractProcessor;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.operation.transform.ProjectiveTransform;
import org.geotools.referencing.CRS;


/**
 * Tests the crop operation.
 *
 * @source $URL$
 * @version $Id$
 * @author Simone Giannecchini
 *
 * @since 2.3
 */
public class CropTest extends GridCoverageTest {
    /**
     * The logger for trace informations in this test suite.
     */
    private final static Logger LOGGER = Logger.getLogger("org.geotools.coverage.grid");

    /**
     * {@code true} if the result should be displayed in windows during test execution.
     * Default to {@code false}. This flag is set to {@code true} only if this test suite
     * is executed explicitly though the {@link #main} method.
     */
    private static boolean SHOW;

    /**
     * The grid coverage to test.
     */
    private GridCoverage2D coverage;

    /**
     * Creates a test suite for the given name.
     */
    public CropTest(String name) {
        super(name);
    }

    /**
     * Set up common objects used for all tests.
     */
    protected void setUp() throws Exception {
        super.setUp();
        coverage = getExample(0);
    }

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(CropTest.class);
    }

    /**
     * Run the tests from the command line.
     */
    public static void main(String[] args) {
        SHOW = true;
        org.geotools.util.Logging.GEOTOOLS.forceMonolineConsoleOutput(AbstractProcessor.OPERATION);
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Tests the "Crop" operation.
     */
    public void testCrop() throws InvalidGridGeometryException, TransformException {
        final AbstractProcessor processor = AbstractProcessor.getInstance();
        GridCoverage2D source = coverage.geophysics(true);
        final ParameterValueGroup param = processor.getOperation("CoverageCrop").getParameters();
        param.parameter("Source").setValue(source);
        param.parameter("Envelope").setValue(source.getEnvelope());

        GridCoverage2D cropped = (GridCoverage2D) processor.doOperation(param);
        assertNotNull(cropped.getRenderedImage().getData());
        final RenderedImage image = cropped.getRenderedImage();
        cropped = cropped.geophysics(false);
        String operation = null;
        if (image instanceof RenderedOp) {
            operation = ((RenderedOp) image).getOperationName();
            AbstractProcessor.LOGGER.fine("Applied \"" + operation + "\" JAI operation.");
        }
        if (SHOW) {
            Viewer.show(coverage, coverage.getName().toString());
            Viewer.show(cropped, cropped.getName().toString());
        } else {
            // Force computation
            assertNotNull(coverage.getRenderedImage().getData());
        }

        final AffineTransform gridToWorld = new AffineTransform(
                (AffineTransform) coverage.gridGeometry.getGridToCRS());
        gridToWorld.translate(-0.5, -0.5);
        final MathTransform worldToGrid = ProjectiveTransform.create(gridToWorld).inverse();
        final GeneralEnvelope sourceRange = CRS.transform(worldToGrid, source.getEnvelope2D());
        LOGGER.fine("Source envelope: " + source.getEnvelope2D());

        final GridGeometry2D gridG = new GridGeometry2D(new GeneralGridRange(sourceRange),
                coverage.gridGeometry.getGridToCRS(), source.getCoordinateReferenceSystem2D());
        LOGGER.fine("Target grid range: " + gridG.getGridRange());
        LOGGER.fine("Target grid geometry: " + gridG.getEnvelope2D());
    }
}
