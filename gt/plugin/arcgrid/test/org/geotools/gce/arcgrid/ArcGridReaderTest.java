/*
 * Created on Apr 23, 2004
 */
package org.geotools.gce.arcgrid;

import org.geotools.resources.TestData;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;

import java.net.URL;

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
        url = TestData.getResource(this, TESTFILE);
        format = (new ArcGridFormatFactory()).createFormat();
        reader = new ArcGridReader(url);
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown()
        throws Exception {
        super.tearDown();
    }

    public void testGetSource() {
        assertEquals(url, reader.getSource());
    }

    /*
     * Class to test for GridCoverage read( Parameter[]) not gzipped and not
     * GRASS format
     */
    public void testReadStringParameterArray()
        throws Exception {
        assertNotNull(reader.read(null));
    }

    /*
     * Class to test for GridCoverage read( Parameter[]) gzipped but not GRASS
     * format
     */
    public void testGZIPReadStringParameterArray()
        throws Exception {
        URL gzipUrl = TestData.getResource(this, GZIP_TESTFILE);

        reader = new ArcGridReader(gzipUrl);
    }

    public static final void main(String[] args)
        throws Exception {
        junit.textui.TestRunner.run(suite(ArcGridReaderTest.class));
    }
}
