/*
 * Created on Apr 23, 2004
 */
package org.geotools.data.arcgrid.test;

import java.net.URL;

import org.geotools.data.arcgrid.ArcGridFormatFactory;
import org.geotools.data.coverage.grid.Format;
import org.geotools.data.coverage.grid.GridCoverageReader;
import org.geotools.resources.TestData;
import org.opengis.parameter.OperationParameter;
import org.opengis.parameter.OperationParameterGroup;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;

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
        url = TestData.getResource( this, TESTFILE );        
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
        URL gzipUrl = TestData.getResource( this, GZIP_TESTFILE );
        reader=format.getReader( gzipUrl );
        OperationParameterGroup params = format.getReadParameters();
        ParameterValueGroup values = (ParameterValueGroup) params.createValue();
        
        OperationParameter grassInfo = params.getParameter( "GRASS" );
        ParameterValue grass = values.getValue( "GRASS" );
        grass.setValue( true );
        
        
        OperationParameter compressInfo = params.getParameter( "Compressed" );
        ParameterValue compress = values.getValue( "Compressed" );
        compress.setValue( true );
        
        params.createValue();
    }

}