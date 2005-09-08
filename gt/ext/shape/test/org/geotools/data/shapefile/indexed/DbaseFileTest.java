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
 * ShapefileTest.java
 * JUnit based test
 *
 * Created on 12 February 2002, 21:27
 */
package org.geotools.data.shapefile.indexed;

import org.geotools.data.shapefile.dbf.DbaseFileHeader;
import org.geotools.data.shapefile.dbf.DbaseFileWriter;
import org.geotools.data.shapefile.dbf.IndexedDbaseFileReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;


/**
 * DOCUMENT ME!
 *
 * @author Ian Schneider
 * @author James Macgill
 */
public class DbaseFileTest extends TestCaseSupport {
    static final String TEST_FILE = "statepop.dbf";
    private IndexedDbaseFileReader dbf = null;

    public DbaseFileTest(java.lang.String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite(DbaseFileTest.class));
    }

    protected void setUp() throws Exception {
        dbf = new IndexedDbaseFileReader(getTestResourceChannel(TEST_FILE));
    }

    public void testNumberofColsLoaded() {
        assertEquals("Number of attributes found incorect", 252,
            dbf.getHeader().getNumFields());
    }

    public void testNumberofRowsLoaded() {
        assertEquals("Number of rows", 49, dbf.getHeader().getNumRecords());
    }

    public void testDataLoaded() throws Exception {
        Object[] attrs = new Object[dbf.getHeader().getNumFields()];
        dbf.readEntry(attrs);
        assertEquals("Value of Column 0 is wrong", attrs[0],
            new String("Illinois"));
        assertEquals("Value of Column 4 is wrong",
            ((Double) attrs[4]).doubleValue(), 143986.61, 0.001);
    }

    public void testRowVsEntry() throws Exception {
        Object[] attrs = new Object[dbf.getHeader().getNumFields()];
        IndexedDbaseFileReader dbf2 = new IndexedDbaseFileReader(getTestResourceChannel(
                    TEST_FILE));

        while (dbf.hasNext()) {
            dbf.readEntry(attrs);

            IndexedDbaseFileReader.Row r = dbf2.readRow();

            for (int i = 0, ii = attrs.length; i < ii; i++) {
                assertNotNull(attrs[i]);
                assertNotNull(r.read(i));
                assertEquals(attrs[i], r.read(i));
            }
        }
    }

    public void testHeader() throws Exception {
        DbaseFileHeader header = new DbaseFileHeader();
        header.addColumn("emptyString", 'C', 20, 0);
        header.addColumn("emptyInt", 'N', 20, 0);
        header.addColumn("emptyDouble", 'N', 20, 5);
        header.addColumn("emptyFloat", 'F', 20, 5);
        header.addColumn("emptyLogical", 'L', 1, 0);
        header.addColumn("emptyDate", 'D', 20, 0);

        int length = header.getRecordLength();
        header.removeColumn("emptyDate");
        assertTrue(length != header.getRecordLength());
        header.addColumn("emptyDate", 'D', 20, 0);
        assertTrue(length == header.getRecordLength());
        header.removeColumn("billy");
        assertTrue(length == header.getRecordLength());
    }

    public void testEmptyFields() throws Exception {
        DbaseFileHeader header = new DbaseFileHeader();
        header.addColumn("emptyString", 'C', 20, 0);
        header.addColumn("emptyInt", 'N', 20, 0);
        header.addColumn("emptyDouble", 'N', 20, 5);
        header.addColumn("emptyFloat", 'F', 20, 5);
        header.addColumn("emptyLogical", 'L', 1, 0);
        header.addColumn("emptyDate", 'D', 20, 0);
        header.setNumRecords(20);

        File f = new File(System.getProperty("java.io.tmpdir"), "scratchDBF.dbf");
        FileOutputStream fout = new FileOutputStream(f);
        DbaseFileWriter dbf = new DbaseFileWriter(header, fout.getChannel());

        for (int i = 0; i < header.getNumRecords(); i++) {
            dbf.write(new Object[6]);
        }

        dbf.close();

        FileInputStream in = new FileInputStream(f);
        IndexedDbaseFileReader r = new IndexedDbaseFileReader(in.getChannel());
        int cnt = 0;

        while (r.hasNext()) {
            cnt++;

            Object[] o = r.readEntry();
            assertTrue(o.length == r.getHeader().getNumFields());
        }

        assertEquals("Bad number of records", cnt, 20);
        f.delete();
    }
}
