/*
 * ArcGridReadWriteTest.java
 *
 * Created on September 2, 2004, 9:26 PM
 */

package org.geotools.data.arcgrid;

import java.io.File;
import java.awt.image.Raster;

import org.geotools.gc.GridCoverage;
import org.geotools.pt.Envelope;
import org.geotools.data.coverage.grid.Format;
import org.geotools.data.coverage.grid.GridCoverageReader;
import org.geotools.data.coverage.grid.GridCoverageWriter;
import org.geotools.resources.TestData;

import org.opengis.parameter.ParameterValueGroup;

/**
 * Test reading and writing arcgrid grid coverages.
 *
 * @author  rschulz
 */
public class ArcGridReadWriteTest extends TestCaseSupport {
    
    private Format f = null;
    
    /** ArcGrid files (and associated parameters) to test*/
    final TestParams params[] = new TestParams[] {
        new TestParams("spearfish_dem.asc.gz", true, true),
        new TestParams("ArcGrid.asc", false, false),
        new TestParams("vandem.asc.gz", true, false)
    };
    
    /** Creates a new instance of ArcGridReadWriteTest */
    public ArcGridReadWriteTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        f = new org.geotools.data.arcgrid.ArcGridFormat();
    }
    
    public void testAll() throws Exception {
         StringBuffer errors = new StringBuffer();
         for (int i=0; i < params.length; i++) {
             try {
                 test(params[i]);
             } catch (Exception e) {
                 e.printStackTrace();
                 errors.append("\nFile " + params[i].fileName + " : " + e.getMessage());
             }
         }
         if (errors.length() > 0) {
             fail(errors.toString());
         }
    }
    
    void test(TestParams testParam) throws Exception {
        //temporary file to use
        File tmpFile = TestData.temp(this,"temp.asc");
        
        //read in the grid coverage
        GridCoverageReader reader = f.getReader(TestData.getResource( this, testParam.fileName ));
        ParameterValueGroup paramDescriptor = f.getReadParameters();
//        params.getValue( "Compressed" ).setValue( testParam.compressed );
//        params.getValue( "GRASS" ).setValue( testParam.grass );
//        GridCoverage gc1 = reader.read( params );
//
//        //write grid coverage out to temp file
//        GridCoverageWriter writer = f.getWriter(tmpFile);
//        paramDescriptor = f.getWriteParameters();
//        params = (ParameterValueGroup) paramDescriptor.createValue();
//        params.getValue( "Compressed" ).setValue( testParam.compressed );
//        params.getValue( "GRASS" ).setValue( testParam.grass );
//        writer.write(gc1, params);
//        
//        //read the grid coverage back in from temp file
//        reader = f.getReader(tmpFile);
//        paramDescriptor = f.getReadParameters();
//        params = (ParameterValueGroup) paramDescriptor.createValue();
//        params.getValue( "Compressed" ).setValue( testParam.compressed );
//        params.getValue( "GRASS" ).setValue( testParam.grass );
//        GridCoverage gc2 = reader.read( params );
//        
//        //check that the original and temporary grid are the same
//        compare(gc1, gc2);
    }
    
    /** 
     * Compares 2 grid covareages, throws an exception if they are not the
     * same.
     */
    void compare(GridCoverage gc1, GridCoverage gc2) throws Exception {
        
        Envelope e1 = gc1.getEnvelope();
        Envelope e2 = gc2.getEnvelope();
        if (!e1.equals(e2)) {
            throw new Exception("GridCoverage Envelopes are not equal");
        }
        
        double[] values1 = null;
        double[] values2 = null;
        Raster r1 = gc1.getRenderedImage().getData();
        Raster r2 = gc2.getRenderedImage().getData();
        for(int i=r1.getMinX(); i < r1.getWidth(); i++) {
            for(int j=r1.getMinY(); j<r1.getHeight(); j++) {
                values1 = r1.getPixel(i,j, values1);
                values2 = r2.getPixel(i,j, values2); 
                for(int k=0; k<values1.length; k++) {
                    if(!(Double.isNaN(values1[k]) && Double.isNaN(values2[k])) &&
                       (values1[k] != values2[k])) {
                        throw new Exception("GridCoverage Values are not equal: " + values1[k] + ", " + values2[k]);
                    }
                }
            }   
        }
    }
    
  public static final void main(String[] args) throws Exception {
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
    
    TestParams (String fileName, boolean compressed, boolean grass) {
        this.fileName = fileName;
        this.compressed = compressed;
        this.grass = grass;
    }  
}
