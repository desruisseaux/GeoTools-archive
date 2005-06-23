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
package org.geotools.data.mif;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureWriter;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import java.util.HashMap;


/**
 * DOCUMENT ME!
 *
 * @author Luca S. Percich, AMA-MI
 */
public class MIFFileTest extends TestCase {
    private MIFFile mif = null;

    /**
     * DOCUMENT ME!
     *
     * @param args DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public static void main(java.lang.String[] args) throws Exception {
        junit.textui.TestRunner.run(new TestSuite(MIFFileTest.class));
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        MIFTestUtils.cleanFiles();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        MIFTestUtils.cleanFiles();
        super.tearDown();
    }

    /**
     * Tests for schema and feature reading from an existing MIF file
     */
    public void testMIFFileOpen() {
        try {
            mif = new MIFFile(MIFTestUtils.fileName("mixed"), // .mif
                    MIFTestUtils.getParams("mif", "", null));
            assertEquals("450",
                mif.getHeaderClause(MIFDataStore.HCLAUSE_VERSION));

            FeatureType schema = mif.getSchema();
            assertNotNull(schema);
            assertEquals(11, schema.getAttributeCount());
            assertEquals("DESCRIPTION", schema.getAttributeType(1).getName());
            assertEquals(Double.class,
                schema.getAttributeType("LENGTH").getType());

            FeatureReader fr = mif.getFeatureReader();
            int tot = 0;

            while (fr.hasNext()) {
                Feature f = fr.next();

                if (++tot == 4) {
                    assertEquals("POLYGON", (String) f.getAttribute("GEOMTYPE"));
                }
            }

            fr.close();

            assertEquals(tot, 9);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    /*
     * Test a MIF file copy using input FeatureReader, createSchema and output FeatureWriter
     */
    public void testFileCopy() {
        MIFFile in = null;
        MIFFile out = null;
        FeatureReader inFR = null;
        FeatureReader outFR = null;
        FeatureWriter outFW = null;
        int maxAttr = 0;

        try {
            // Input file
            in = new MIFFile(MIFTestUtils.fileName("grafo"), null); // .mif

            FeatureType ft = in.getSchema();

            maxAttr = ft.getAttributeCount() - 1;

            // Params for output file
            HashMap params = new HashMap();

            // params.put(MIFFile.HCLAUSE_TRANSFORM, "100,100,0,0");
            params.put(MIFDataStore.HCLAUSE_UNIQUE,
                in.getHeaderClause(MIFDataStore.HCLAUSE_UNIQUE));
            params.put(MIFDataStore.HCLAUSE_INDEX,
                in.getHeaderClause(MIFDataStore.HCLAUSE_INDEX));
            params.put(MIFDataStore.HCLAUSE_VERSION,
                in.getHeaderClause(MIFDataStore.HCLAUSE_VERSION));
            params.put(MIFDataStore.HCLAUSE_COORDSYS,
                in.getHeaderClause(MIFDataStore.HCLAUSE_COORDSYS));

            // params.put(MIFDataStore.HCLAUSE_DELIMITER, in.getHeaderClause(MIFDataStore.HCLAUSE_DELIMITER));
            params.put(MIFDataStore.HCLAUSE_DELIMITER, ",");

            // Output file
            out = new MIFFile(MIFTestUtils.fileName("grafo_out"), ft, params); // .mif
        } catch (Exception e) {
            fail("Can't create grafo_out: " + e.getMessage());
        }

        try {
            inFR = in.getFeatureReader();
            outFW = out.getFeatureWriter();

            Feature inF;
            Feature outF;
            int counter = 0;

            while (inFR.hasNext()) {
                inF = inFR.next();
                outF = outFW.next();

                for (int i = 0; i < outF.getNumberOfAttributes(); i++) {
                    outF.setAttribute(i, inF.getAttribute(i));
                }

                outFW.write();
                counter++;
            }

            inFR.close();
            outFW.close();
        } catch (Exception e) {
            fail("Can't copy features: " + e.getMessage());
        }

        try {
            inFR = in.getFeatureReader();

            outFR = out.getFeatureReader();

            int n = 0;

            while (inFR.hasNext()) {
                Feature fin = inFR.next();
                Feature fout = outFR.next();

                // Cycling attribute sampling
                assertEquals(fin.getAttribute(n).toString(),
                    fout.getAttribute(n).toString());

                if (++n > maxAttr) {
                    n = 0;
                }
            }

            inFR.close();
            outFR.close();
        } catch (Exception e) {
            fail("Can't compare features: " + e.getMessage());
        }
    }

    /**
     * Test writing / appending
     */
    public void testFeatureWriter() {
        try {
            MIFTestUtils.copyMif("mixed", "mixed_wri");

            MIFFile in = new MIFFile(MIFTestUtils.fileName("mixed_wri"), // .mif
                    MIFTestUtils.getParams("", "", null));
            FeatureWriter fw = in.getFeatureWriter();

            Feature f;
            int counter = 0;

            while (fw.hasNext()) {
                f = fw.next();
                ++counter;

                if (counter == 5) {
                    fw.remove(); // removes multilinestring line
                } else if (counter == 7) {
                    f.setAttribute("DESCRIPTION", "fubar");
                    fw.write();
                } else {
                    f.setAttribute("DESCRIPTION", "foo"); // shouldn't affect data because I dont call write()
                }
            }

            // Appends a line
            Feature newf = fw.next();
            newf.setAttribute("DESCRIPTION", "newline");
            fw.write();

            fw.close();

            // Reopens a writer to modify feature # 3
            fw = in.getFeatureWriter();
            f = fw.next();
            f = fw.next();
            f = fw.next();
            f.setAttribute("NUM_OF_SEGMENTS", new Integer(179));
            fw.write();
            fw.close(); // should rewrite all other features

            FeatureReader fr = in.getFeatureReader();
            counter = 0;

            while (fr.hasNext()) {
                f = fr.next();
                ++counter;

                String descr = (String) f.getAttribute("DESCRIPTION");
                assertEquals(false, descr.equals("foo"));

                if (counter == 3) {
                    assertEquals(179,
                        ((Integer) f.getAttribute("NUM_OF_SEGMENTS")).intValue());
                } else if (counter == 5) {
                    assertEquals("Single polygon with 2 holes", descr);
                } else if (counter == 6) {
                    assertEquals("fubar", descr);
                } else if (counter == 9) {
                    assertEquals("newline", descr);
                }
            }

            fr.close();
            assertEquals(9, counter);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}
