/*
 * Created on Apr 23, 2004
 */
package org.geotools.data.arcgrid.test;

import java.io.InputStream;
import java.net.URL;
import java.util.Locale;

import org.geotools.data.arcgrid.ArcGridFormatFactory;
import org.geotools.data.arcgrid.ArcGridReader;
import org.geotools.data.coverage.grid.Format;
import org.geotools.data.coverage.grid.GridCoverageReader;
import org.opengis.parameter.GeneralOperationParameter;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;

/**
 * 
 * 
 * @author jeichar
 */
public class ArcGridReaderTest extends TestCaseSupport {

    GridCoverageReader reader;

    String TESTFILE = "ArcGrid.asc";

    String GZIP_TESTFILE = "spearfish_dem.asc.gz";
    
    Format format;

    URL url;

    /**
     * @param name
     */
    public ArcGridReaderTest(String name) {
        super(name);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        url = getTestResource(TESTFILE);
        format=(new ArcGridFormatFactory()).createFormat();
        reader = format.getReader(url);
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetSource() {
        assertEquals(url, reader.getSource());
    }

    /*
     * Class to test for GridCoverage read( Parameter[]) not gzipped and not
     * GRASS format
     */
    public void testReadStringParameterArray() throws Exception {
        assertNotNull(reader.read(null));
    }

    /*
     * Class to test for GridCoverage read( Parameter[]) gzipped but not GRASS
     * format
     */
    public void testGZIPReadStringParameterArray() throws Exception {
        reader=format.getReader(getTestResource(GZIP_TESTFILE));
        GeneralOperationParameter[] params = format.getReadParameters();
        GeneralParameterValue[] pvalues =new ParameterValue[2];
        ParameterValue value;
        for (int i = 0; i < params.length; i++) {
            if( params[i].getName(Locale.ENGLISH).equalsIgnoreCase("GRASS")){
                value=(ParameterValue)params[i].createValue();
                value.setValue(true);
                pvalues[0]=value;
            }
            if( params[i].getName(Locale.ENGLISH).equalsIgnoreCase("Compressed")){
                value=(ParameterValue)params[i].createValue();
                value.setValue(true);
                pvalues[1]=value;
            }
        }
    }

}