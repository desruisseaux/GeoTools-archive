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
/*
 * Created on Apr 23, 2004
 */
package org.geotools.gce.arcgrid;

import org.geotools.resources.TestData;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverageReader;
import java.net.URL;


/**
 * DOCUMENT ME!
 *
 * @author jeichar
 * @source $URL$
 */
public class ArcGridReaderTest extends TestCaseSupport {
    GridCoverageReader reader;
    String TESTFILE = "ArcGrid.asc";
    String GZIP_TESTFILE = "spearfish_dem.asc.gz";
    Format format;
    URL url;

    /**
     * DOCUMENT ME!
     *
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
        URL gzipUrl = TestData.getResource(this, GZIP_TESTFILE);

        reader = new ArcGridReader(gzipUrl);
    }

    public static final void main(String[] args) throws Exception {
        junit.textui.TestRunner.run(suite(ArcGridReaderTest.class));
    }
}
