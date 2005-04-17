/*
 * ArcGridReadWriteTest.java
 *
 * Created on September 2, 2004, 9:26 PM
 */
package org.geotools.gce.arcgrid;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.resources.TestData;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.parameter.ParameterValueGroup;


import java.awt.image.Raster;

import java.io.File;

import java.net.URL;

/**
 * Test reading and writing arcgrid grid coverages.
 *
 * @author  rschulz
 */
public class ArcGridReadWriteTest extends TestCaseSupport {
    private Format f = null;

    /** ArcGrid files (and associated parameters) to test*/
    final TestParams[] params = new TestParams[] {
            new TestParams("spearfish_dem.asc.gz", true, true),
            new TestParams("ArcGrid.asc", false, false),
            new TestParams("vandem.asc.gz", true, false)
        };

    /** Creates a new instance of ArcGridReadWriteTest */
    public ArcGridReadWriteTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        f = new org.geotools.gce.arcgrid.ArcGridFormat();
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

        //arcgridformat
        Format f = new ArcGridFormat();

        //setting general format parameteres to be used later on
        ParameterValueGroup params = f.getReadParameters();

        params.parameter("Compressed").setValue(testParam.compressed);
        params.parameter("GRASS").setValue(testParam.grass);
        params = f.getWriteParameters();
        params.parameter("Compressed").setValue(testParam.compressed);
        params.parameter("GRASS").setValue(testParam.grass);

        /*Step 1 read it*/

        //read in the grid coverage
        GridCoverageReader reader = new ArcGridReader((TestData.getResource(
                    this, testParam.fileName)));

        params = reader.getFormat().getReadParameters();

        //setting params
        params.parameter("Compressed").setValue(f.getReadParameters()
                                                 .parameter("Compressed")
                                                 .booleanValue());
        params.parameter("GRASS").setValue(f.getReadParameters()
                                            .parameter("GRASS").booleanValue());

        //reading the coverage
        GridCoverage gc1 = reader.read(null); //

        //            (GeneralParameterValue[]) params.values().toArray(new GeneralParameterValue[params.values().size()])
        //            );

        /*step 2 write it*/

        //write grid coverage out to temp file
        GridCoverageWriter writer = new ArcGridWriter(tmpFile);

        //setting write parameters
        params = writer.getFormat().getWriteParameters();
        params.parameter("Compressed").setValue(f.getWriteParameters()
                                                 .parameter("Compressed")
                                                 .booleanValue());
        params.parameter("GRASS").setValue(f.getWriteParameters()
                                            .parameter("GRASS").booleanValue());
        writer.write(gc1, null); //

        //                   (GeneralParameterValue[]) params.values().toArray(new GeneralParameterValue[
        //        params.values().size()]));
        /*step 3 read it again and compared them*/
        //read the grid coverage back in from temp file
        reader = new ArcGridReader(tmpFile);

        //setting params
        params = reader.getFormat().getReadParameters();
        params.parameter("Compressed").setValue(f.getReadParameters()
                                                 .parameter("Compressed")
                                                 .booleanValue());
        params.parameter("GRASS").setValue(f.getReadParameters()
                                            .parameter("GRASS").booleanValue());

        //read it
        GridCoverage gc2 = reader.read(null); // (GeneralParameterValue[]) params.values().

        //                                     toArray(new GeneralParameterValue[params.
        //                                            values().size()]));
        //check that the original and temporary grid are the same
        compare(gc1, gc2);
    }

    /**
     * Compares 2 grid covareages, throws an exception if they are not the
     * same.
     */
    void compare(GridCoverage gc1, GridCoverage gc2)
        throws Exception {
        GeneralEnvelope e1 = (GeneralEnvelope) gc1.getEnvelope();
        GeneralEnvelope e2 = (GeneralEnvelope) gc2.getEnvelope();

        if ((e1.getLowerCorner().getOrdinate(0) != e1.getLowerCorner()
                                                     .getOrdinate(0))
            || (e1.getLowerCorner().getOrdinate(1) != e1.getLowerCorner()
                                                        .getOrdinate(1))
            || (e1.getUpperCorner().getOrdinate(0) != e1.getUpperCorner()
                                                        .getOrdinate(0))
            || (e1.getUpperCorner().getOrdinate(1) != e1.getUpperCorner()
                                                        .getOrdinate(1))) {
            throw new Exception("GridCoverage Envelopes are not equal"
                + e1.toString() + e2.toString());
        }

        if (e1.getCoordinateReferenceSystem().toWKT().compareToIgnoreCase(e2.getCoordinateReferenceSystem()
                                                                            .toWKT()) != 0) {
            throw new Exception("GridCoverage Envelopes are not equal"
                + e1.getCoordinateReferenceSystem().toWKT()
                + e2.getCoordinateReferenceSystem().toWKT());
        }

        double[] values1 = null;
        double[] values2 = null;
        Raster r1 = ((GridCoverage2D) gc1).getRenderedImage().getData();
        Raster r2 = ((GridCoverage2D) gc2).getRenderedImage().getData();

        for (int i = r1.getMinX(); i < r1.getWidth(); i++) {
            for (int j = r1.getMinY(); j < r1.getHeight(); j++) {
                values1 = r1.getPixel(i, j, values1);
                values2 = r2.getPixel(i, j, values2);

                for (int k = 0; k < values1.length; k++) {
                    if (!(Double.isNaN(values1[k]) && Double.isNaN(values2[k]))
                        && (values1[k] != values2[k])) {
                        throw new Exception(
                            "GridCoverage Values are not equal: " + values1[k]
                            + ", " + values2[k]);
                    }
                }
            }
        }
    }

    public static final void main(String[] args)
        throws Exception {
        junit.textui.TestRunner.run(suite(ArcGridReadWriteTest.class));
    }
}


/**
 * Simple class to hold parameters for tests.
 */
class TestParams {
    public String fileName;
    public boolean compressed;
    public boolean grass;

    TestParams(String fileName, boolean compressed, boolean grass) {
        this.fileName = fileName;
        this.compressed = compressed;
        this.grass = grass;
    }
}
