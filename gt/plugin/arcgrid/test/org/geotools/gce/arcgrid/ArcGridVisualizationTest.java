package org.geotools.gce.arcgrid;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageImpl;
import org.geotools.resources.TestData;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.parameter.ParameterValueGroup;

import javax.media.jai.PlanarImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import java.awt.image.BufferedImage;

import java.io.File;

import java.net.URL;

/**
 * <p>Title: TestArcGridClass</p>
 * <p>Description: Testing ArcGrid ascii grids related classes.</p>
 * <p>Copyright: Copyright (c) 2005 Simone Giannecchini</p>
 * <p>Company: </p>
 * @author <a href="mailto:simboss_ml@tiscali.it">Simone Giannecchini (simboss)</a>
 * @version 1.0
 */
public class ArcGridVisualizationTest extends TestCaseSupport {
    /** ArcGrid files (and associated parameters) to test*/
    final TestParams[] params = new TestParams[] {
            new TestParams("spearfish_dem.asc.gz", true, true),
            new TestParams("ArcGrid.asc", false, false),
            new TestParams("vandem.asc.gz", true, false)
        };

    /** Creates a new instance of ArcGridReadWriteTest */
    public ArcGridVisualizationTest(String name) {
        super(name);
    }

    public void testAll() throws Exception {
        StringBuffer errors = new StringBuffer();

        for (int i = 0; i < params.length; i++) {
            try {
                test(params[i]);
            }
            catch (Exception e) {
                e.printStackTrace();
                errors.append("\nFile " + params[i].fileName + " : "
                    + e.getMessage());
            }
        }

        if (errors.length() > 0) {
            fail(errors.toString());
        }
    }

    void test(TestParams testParam)
        throws Exception {
        //create a temporary output file
        //temporary file to use
        File tmpFile = null;

        if (testParam.compressed) {
            tmpFile = File.createTempFile("temp", ".gz");
        }
        else {
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
        GridCoverage2D gc = (GridCoverage2D) reader.read(null);

        ImageIcon icon = new ImageIcon(((PlanarImage) gc.getRenderedImage())
                .getAsBufferedImage());
        JLabel label = new JLabel(icon);
        JFrame frame = new JFrame();

        frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new JScrollPane(label));
        frame.pack();
        frame.show();
    }

    public static final void main(String[] args)
        throws Exception {
        junit.textui.TestRunner.run(suite(ArcGridReadWriteTest.class));
    }
}
