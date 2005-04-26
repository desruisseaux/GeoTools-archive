/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package org.geotools.gce.arcgrid;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.resources.TestData;
import org.opengis.coverage.grid.GridCoverageReader;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import javax.media.jai.PlanarImage;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;


/**
 * <p>
 * Title: TestArcGridClass
 * </p>
 * 
 * <p>
 * Description: Testing ArcGrid ascii grids related classes.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2005 Simone Giannecchini
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 *
 * @author <a href="mailto:simboss_ml@tiscali.it">Simone Giannecchini
 *         (simboss)</a>
 * @version 1.0
 */
public class ArcGridVisualizationTest extends TestCaseSupport {
    /** ArcGrid files (and associated parameters) to test */
    final TestParams[] params = new TestParams[] {
            new TestParams("vandem.asc.gz", true, false),
            new TestParams("ArcGrid.asc", false, false),
            new TestParams("spearfish_dem.asc.gz", true, true)
        };

    /**
     * Creates a new instance of ArcGridReadWriteTest
     *
     * @param name 
     */
    public ArcGridVisualizationTest(String name) {
        super(name);
    }

    public void testAll() throws Exception {
        StringBuffer errors = new StringBuffer();

        for (int i = 0; i < params.length; i++) {
            try {
                test(params[i]);
            } catch (Exception e) {
                e.printStackTrace();
                errors.append("\nFile " + params[i].fileName + " : "
                    + e.getMessage());
            }
        }

        if (errors.length() > 0) {
            fail(errors.toString());
        }
    }

    void test(TestParams testParam) throws Exception {
        //create a temporary output file
        //temporary file to use
        File tmpFile = null;

        if (testParam.compressed) {
            tmpFile = File.createTempFile("temp", ".gz");
        } else {
            tmpFile = File.createTempFile("temp", ".asc");
        }

        tmpFile.deleteOnExit();

        //file to use
        URL file = TestData.getResource(this, testParam.fileName);

        /*Step 1 read it*/

        //read in the grid coverage
        GridCoverageReader reader = new ArcGridReader((TestData.getResource(
                    this, testParam.fileName)));

        //reading the coverage
        GridCoverage2D gc = ((GridCoverage2D) reader.read(null)).geophysics(false);

        //visualizing it
        BufferedImage bufferedImage = ((PlanarImage) gc.getRenderedImage())
            .getAsBufferedImage();
        ImageIcon icon = new ImageIcon(bufferedImage);
        JLabel label = new JLabel(icon);
        JFrame frame = new JFrame();
        frame.setTitle(testParam.fileName);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new JScrollPane(label));
        frame.pack();
        frame.show();
    }

    public static final void main(String[] args) throws Exception {
        junit.textui.TestRunner.run(suite(ArcGridVisualizationTest.class));
    }
}
