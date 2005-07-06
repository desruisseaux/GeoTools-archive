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
import org.geotools.coverage.processing.AbstractProcessor;


/**
 * Tests JAI operation wrapped as {@link OperatorJAI}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class OperationJAI_Test extends GridCoverageTest {
    /**
     * The grid coverage processor.
     */
    private AbstractProcessor processor;

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
        return new TestSuite(OperationJAI_Test.class);
    }

    /**
     * Constructs a test case with the given name.
     */
    public OperationJAI_Test(final String name) {
        super(name);
    }

    /**
     * Fetch the processor before each test.
     */
    protected void setUp() {
        processor = AbstractProcessor.getInstance();
    }

    /**
     * Applies an operation on the specified coverage. All tests in the parent classes will
     * be executed with on this transformed coverage.
     *
     * @todo Applies some operation.
     */
    protected GridCoverage2D transform(final GridCoverage2D coverage) {
        return coverage;
    }

    /**
     * Tests operations requerying only one image.
     */
    public void test1Param() {
        if (true) return; // TODO
        String        operationName  = "SubtractConst";
        GridCoverage  sourceCoverage = getRandomCoverage().geophysics(true);
        GridCoverage  targetCoverage = null; // TODO processor.doOperation(operationName, sourceCoverage);
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
        assertEquals (operationName, ((OperationNode) targetImage).getOperationName());
        for (int y=sourceRaster.getHeight(); --y>=0;) {
            for (int x=sourceRaster.getWidth(); --x>=0;) {
                final double s = sourceRaster.getSample(x, y, 0);
                final double t = targetRaster.getSample(x, y, 0);
                System.out.println(s + "\t" + t);
                assertEquals(Math.abs(s), t, 1E-7);
            }
        }
    }
}
