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
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureWriter;
import org.geotools.data.mif.MIFFile;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;


/**
 * DOCUMENT ME!
 *
 * @author Luca S. Percich, AMA-MI
 */
public class MIFFileTest extends TestCase {
    private static Logger LOGGER = Logger.getLogger(
            "org.geotools.data.mif.MIFFileTest");
    private String dataPath = MIFTestUtils.getDataPath();
    private MIFFile mif = null;

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Tests for schema and feature reading from an existing MIF file
     */
    public void testMIFFileOpen() {
        try {
            mif = new MIFFile(MIFTestUtils.getDataPath() + "mixed.mif",
                    MIFTestUtils.getParams("mif", ""));
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
        try {
            // Input file
            MIFFile in = new MIFFile(dataPath + "grafo.mif", null);
            FeatureType ft = in.getSchema();

            int maxAttr = ft.getAttributeCount() - 1;

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
            MIFFile out = new MIFFile(dataPath + "grafo_out.mif", ft, params);

            FeatureReader inFR = in.getFeatureReader();
            FeatureWriter outFW = out.getFeatureWriter();

            Feature inF;
            Feature outF;
            int counter = 0;

            while (inFR.hasNext()) {
                inF = inFR.next();
                outF = outFW.next();

                for (int i = 0; i < outF.getNumberOfAttributes(); i++)
                    outF.setAttribute(i, inF.getAttribute(i));

                outFW.write();
                counter++;
            }

            inFR.close();
            outFW.close();

            inFR = in.getFeatureReader();

            FeatureReader outFR = out.getFeatureReader();

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
            fail(e.getMessage());
        }
    }

    /**
     * Test writing / appending
     */
    public void testFeatureWriter() {
        try {
            MIFTestUtils.copyMif("mixed", "mixed_wri");

            MIFFile in = new MIFFile(dataPath + "mixed_wri.mif",
                    MIFTestUtils.getParams("", ""));
            FeatureWriter fw = in.getFeatureWriter();

            Feature f;
            int counter = 0;

            while (fw.hasNext()) {
                f = fw.next();
                ++counter;

                if (counter == 5) {
                    fw.remove(); // removes last 2 features
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

            assertEquals(9, counter);

            // Now checks results
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
}
