/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2005, Geotools Project Management Committee (PMC)
 * (C) 2005, Institut de Recherche pour le Développement
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
import java.io.IOException;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import javax.media.jai.OperationNode;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestSuite;

// OpenGIS dependencies
import org.opengis.coverage.grid.GridCoverage;

// Geotools dependencies
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.processing.Operations;


/**
 * Tests JAI operation wrapped as {@link OperatorJAI}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class OperationsTest extends GridCoverageTest {
    /**
     * Sample image. This field is static in order to avoid reloading it for every test cases
     * in this class.
     */
    private static GridCoverage2D SST;

    /**
     * The grid coverage processor.
     */
    private Operations processor;

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
        return new TestSuite(OperationsTest.class);
    }

    /**
     * Constructs a test case with the given name.
     */
    public OperationsTest(final String name) {
        super(name);
    }

    /**
     * Fetch the processor before each test.
     */
    protected void setUp() throws IOException {
        processor = Operations.DEFAULT;
        if (SST == null) {
            SST = getExample(0);
        }
    }

    /**
     * Applies an operation on the specified coverage. All tests in the parent classes will
     * be executed with on this transformed coverage.
     *
     * @todo Applies some operation.
     */
    protected GridCoverage2D transform(final GridCoverage2D coverage) {
        return ((GridCoverage2D) processor.nodataFilter(coverage.geophysics(true))).geophysics(false);
    }

    /**
     * Show the specified coverage. This is used for debugging only.
     */
    private static void show(GridCoverage coverage) {
        if (coverage instanceof GridCoverage2D) {
            coverage = ((GridCoverage2D) coverage).geophysics(false);
        }
        final RenderedImage image = coverage.getRenderableImage(0,1).createDefaultRendering();
        try {
            Class.forName("org.geotools.gui.swing.OperationTreeBrowser")
                 .getMethod("show", new Class[]{RenderedImage.class})
                 .invoke(null, new Object[]{image});
        } catch (Exception e) {
            /*
             * The OperationTreeBrowser is not part of Geotools's core. It is optional and this
             * class should not fails if it is not presents. This is only a helper for debugging.
             */
        }
    }

    /**
     * Tests {@link Operations#subtract}.
     *
     * @todo Investigate why the color palette is lost.
     */
    public void testSubtract() {
        double[]      constants      = new double[] {18.75};
        GridCoverage  sourceCoverage = SST.geophysics(true);
        GridCoverage  targetCoverage = (GridCoverage) processor.subtract(sourceCoverage, constants);
        RenderedImage sourceImage    = sourceCoverage.getRenderableImage(0,1).createDefaultRendering();
        RenderedImage targetImage    = targetCoverage.getRenderableImage(0,1).createDefaultRendering();
        Raster        sourceRaster   = sourceImage.getData();
        Raster        targetRaster   = targetImage.getData();
        assertNotSame(sourceCoverage,                                targetCoverage);
        assertNotSame(sourceImage,                                   targetImage);
        assertNotSame(sourceRaster,                                  targetRaster);
        assertSame   (sourceCoverage.getCoordinateReferenceSystem(), targetCoverage.getCoordinateReferenceSystem());
        assertEquals (sourceCoverage.getEnvelope(),                  targetCoverage.getEnvelope());
        assertEquals (sourceCoverage.getGridGeometry(),              targetCoverage.getGridGeometry());
        assertEquals (sourceRaster  .getMinX(),                      targetRaster  .getMinX());
        assertEquals (sourceRaster  .getMinY(),                      targetRaster  .getMinY());
        assertEquals (sourceRaster  .getWidth(),                     targetRaster  .getWidth());
        assertEquals (sourceRaster  .getHeight(),                    targetRaster  .getHeight());
        assertEquals (0, sourceRaster.getMinX());
        assertEquals (0, sourceRaster.getMinY());
        assertEquals ("SubtractConst", ((OperationNode) targetImage).getOperationName());

        for (int y=sourceRaster.getHeight(); --y>=0;) {
            for (int x=sourceRaster.getWidth(); --x>=0;) {
                final float s = sourceRaster.getSampleFloat(x, y, 0);
                final float t = targetRaster.getSampleFloat(x, y, 0);
                if (Float.isNaN(s)) {
                    assertTrue(Float.isNaN(t));
                } else {
                    assertEquals(s - constants[0], t, 1E-3f);
                }
            }
        }
        if (false) {
            show(targetCoverage);
        }
    }

    /**
     * Tests {@link Operations#nodataFilter}.
     */
    public void testNodataFilter() {
        GridCoverage  sourceCoverage = SST.geophysics(true);
        GridCoverage  targetCoverage = processor.nodataFilter(sourceCoverage);
        RenderedImage sourceImage    = sourceCoverage.getRenderableImage(0,1).createDefaultRendering();
        RenderedImage targetImage    = targetCoverage.getRenderableImage(0,1).createDefaultRendering();
        Raster        sourceRaster   = sourceImage.getData();
        Raster        targetRaster   = targetImage.getData();
        assertNotSame(sourceCoverage,                                targetCoverage);
        assertNotSame(sourceImage,                                   targetImage);
        assertNotSame(sourceRaster,                                  targetRaster);
        assertSame   (sourceCoverage.getCoordinateReferenceSystem(), targetCoverage.getCoordinateReferenceSystem());
        assertEquals (sourceCoverage.getEnvelope(),                  targetCoverage.getEnvelope());
        assertEquals (sourceCoverage.getGridGeometry(),              targetCoverage.getGridGeometry());
        assertEquals (sourceRaster  .getMinX(),                      targetRaster  .getMinX());
        assertEquals (sourceRaster  .getMinY(),                      targetRaster  .getMinY());
        assertEquals (sourceRaster  .getWidth(),                     targetRaster  .getWidth());
        assertEquals (sourceRaster  .getHeight(),                    targetRaster  .getHeight());
        assertEquals (0, sourceRaster.getMinX());
        assertEquals (0, sourceRaster.getMinY());
        assertEquals ("org.geotools.NodataFilter", ((OperationNode) targetImage).getOperationName());

        for (int y=sourceRaster.getHeight(); --y>=0;) {
            for (int x=sourceRaster.getWidth(); --x>=0;) {
                final float s = sourceRaster.getSampleFloat(x, y, 0);
                final float t = targetRaster.getSampleFloat(x, y, 0);
                if (Float.isNaN(s)) {
                    if (!Float.isNaN(t)) {
                        // TODO: put some test here.
                    }
                } else {
                    assertEquals(s, t, 1E-5f);
                }
            }
        }
        if (false) {
            show(targetCoverage);
        }
    }

    /**
     * Tests {@link Operations#gradientMagnitude}.
     *
     * @todo Investigate why the geophysics view is much more visible than the non-geophysics one.
     */
    public void testGradientMagnitude() {
        GridCoverage  sourceCoverage = SST.geophysics(true);
        GridCoverage  targetCoverage = (GridCoverage) processor.gradientMagnitude(sourceCoverage);
        RenderedImage sourceImage    = sourceCoverage.getRenderableImage(0,1).createDefaultRendering();
        RenderedImage targetImage    = targetCoverage.getRenderableImage(0,1).createDefaultRendering();
        Raster        sourceRaster   = sourceImage.getData();
        Raster        targetRaster   = targetImage.getData();
        assertNotSame(sourceCoverage,                                targetCoverage);
        assertNotSame(sourceImage,                                   targetImage);
        assertNotSame(sourceRaster,                                  targetRaster);
        assertSame   (sourceCoverage.getCoordinateReferenceSystem(), targetCoverage.getCoordinateReferenceSystem());
        assertEquals (sourceCoverage.getEnvelope(),                  targetCoverage.getEnvelope());
        assertEquals (sourceCoverage.getGridGeometry(),              targetCoverage.getGridGeometry());
        assertEquals (sourceRaster  .getMinX(),                      targetRaster  .getMinX());
        assertEquals (sourceRaster  .getMinY(),                      targetRaster  .getMinY());
        assertEquals (sourceRaster  .getWidth(),                     targetRaster  .getWidth());
        assertEquals (sourceRaster  .getHeight(),                    targetRaster  .getHeight());
        assertEquals (0, sourceRaster.getMinX());
        assertEquals (0, sourceRaster.getMinY());
        assertEquals ("GradientMagnitude", ((OperationNode) targetImage).getOperationName());
        
        assertEquals(3.95f, targetRaster.getSampleFloat(304, 310, 0), 1E-2f);
        assertEquals(1.88f, targetRaster.getSampleFloat(262, 357, 0), 1E-2f);

        if (false) {
            show(targetCoverage);
        }
    }
}
