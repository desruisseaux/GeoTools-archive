/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2008, Geotools Project Managment Committee (PMC)
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

import java.awt.image.WritableRaster;
import java.util.Random;
import javax.units.SI;
import junit.framework.TestCase;


/**
 * Tests the {@link ViewsManager} class.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class ViewsManagerTest extends TestCase {
    /**
     * The random number generator to use in this test suite.
     */
    private static final Random random = new Random(7667138224618831007L);

    /**
     * Tests "Piecewise" operation using a simple transform.
     */
    public void testPiecewise() {
        // Initialize...
        final GridCoverageBuilder builder = new GridCoverageBuilder();
        final GridCoverageBuilder.Variable variable = builder.newVariable("Elevation", SI.METER);
        variable.addNodataValue("No data", 0);
        builder.setSampleRange(0, 40000);
        builder.setImageSize(360, 180);
        builder.setBufferedImage(random);
        final WritableRaster raster = builder.getBufferedImage().getRaster();
        raster.setSample(0,0,0,0); // For testing NaN value.
        raster.setSample(1,2,0,0);

        // Sanity check...
        assertEquals(360, raster.getWidth());
        assertEquals(180, raster.getHeight());

        // Tests...
        GridCoverage2D packed = builder.getGridCoverage2D();
        GridCoverage2D geophysics = packed.view(ViewType.GEOPHYSICS);
//        show(geophysics);
        // TODO: complete the tests...
    }

    /**
     * Tests "Piecewise" operation using setting found in IFREMER's Coriolis data.
     */
    public void testCoriolis() {
        final double scale  = 0.001;
        final double offset = 20.0;

        // Initialize...
        final GridCoverageBuilder builder = new GridCoverageBuilder();
        final GridCoverageBuilder.Variable variable = builder.newVariable("Temperature", SI.CELSIUS);
        variable.addNodataValue("No data", 32767);
        builder.setSampleRange(-20000, 23000);
        builder.setImageSize(360, 180);
        builder.setBufferedImage(random);
        final WritableRaster raster = builder.getBufferedImage().getRaster();
        raster.setSample(0,0,0,32767); // For testing NaN value.
        raster.setSample(1,2,0,32767);

        // Sanity check...
        assertEquals(360, raster.getWidth());
        assertEquals(180, raster.getHeight());

        // Tests without "sample to geophysics" transform...
        GridCoverage2D packed = builder.getGridCoverage2D();
        GridCoverage2D geophysics = packed.view(ViewType.GEOPHYSICS);

        variable.setLinearTransform(scale, offset);
        packed = builder.getGridCoverage2D();
    }

    /**
     * @todo Move this method in some subclass. Instead of sleeping, go back to work
     *       when the frame is closed.
     */
    private static void show(final GridCoverage2D coverage) {
//        org.geotools.gui.swing.image.OperationTreeBrowser.show(coverage.getRenderedImage());
        coverage.show();
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            // Go back to worj...
        }
    }
}
