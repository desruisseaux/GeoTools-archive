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

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.WKTReader;
import junit.framework.TestCase;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.feature.AttributeType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.filter.ExpressionBuilder;
import org.geotools.filter.Filter;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;


/**
 * DOCUMENT ME!
 *
 * @author Luca S. Percich, AMA-MI
 */
public class MIFDataStoreTest extends TestCase {
    private static Logger LOGGER = Logger.getLogger(
            "org.geotools.data.mif.MIFDataStoreTest");
    private MIFDataStore ds;
    private String dataPath = MIFTestUtils.getDataPath();
    private WKTReader reader = new WKTReader(new GeometryFactory());
    private String geomName = "the_geom";

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
     * Utility method for instantiating a MIFDataStore via MIFDataStoreFactory
     *
     * @param initPath DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected boolean initDS(String initPath) {
        try {
            HashMap params = MIFTestUtils.getParams("mif", initPath);
            ds = new MIFDataStore(initPath, params);
            assertNotNull(ds);

            return true;
        } catch (Exception e) {
            fail(e.getMessage());

            return false;
        }
    }

    /**
     * See if all the MIF in data dir are recognized
     */
    public void testOpenDir() {
        initDS(dataPath);

        try {
            assertNotNull(ds.getSchema("grafo"));
            assertNotNull(ds.getSchema("nodi"));
            assertNotNull(ds.getSchema("mixed"));
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void testFeatureReaderFilter() {
        initDS(dataPath + "grafo.mif");

        try {
            FeatureReader fr = getFeatureReader("grafo", "ID = 33755");
            Feature arc = null;
            Integer id = new Integer(0);

            if (fr.hasNext()) {
                arc = fr.next();
                id = (Integer) arc.getAttribute("ID");
            }

            assertNotNull(arc);
            assertEquals(id.intValue(), 33755);
            fr.close();
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    /**
     * Tests createSchema & FeatureWriter
     */
    public void testFeatureWriter() {
        initDS(dataPath);

        try {
            FeatureType ft = ds.getSchema("grafo");
            int maxAttr = ft.getAttributeCount() - 1;

            AttributeType[] ats = new AttributeType[ft.getAttributeCount()];

            for (int i = 0; i < ats.length; i++) {
                ats[i] = ft.getAttributeType(i);
            }

            FeatureType newFT = FeatureTypeFactory.newFeatureType(ats,
                    "grafo_new");
            ds.createSchema(newFT);

            FeatureWriter fw = ds.getFeatureWriterAppend("grafo_new",
                    Transaction.AUTO_COMMIT);
            Feature f;
            FeatureReader fr = getFeatureReader("grafo",
                    "ID == 73690 || ID == 71045");

            int counter = 0;

            while (fr.hasNext()) {
                ++counter;

                Feature fin = fr.next();
                f = fw.next();

                for (int i = 0; i <= maxAttr; i++) {
                    f.setAttribute(i, fin.getAttribute(i));
                }

                fw.write();
            }

            fw.close();

            assertEquals(counter, 2);

            fw = ds.getFeatureWriter("grafo_new",
                    (Filter) ExpressionBuilder.parse("ID == 71045"),
                    Transaction.AUTO_COMMIT);

            assertEquals(true, fw.hasNext());
            f = fw.next();
            assertEquals("Via NOVARA", f.getAttribute("NOMECOMUNE"));
            fw.remove();

            fw.close();

            fr = getFeatureReader("grafo_new");

            assertEquals(true, fr.hasNext());
            f = fr.next();
            assertEquals("F3", f.getAttribute("CLASSE"));
            assertEquals(73690, ((Integer) f.getAttribute("ID")).intValue());
            fr.close();
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    /**
     * Obtain a feature reader for the given featureType / filter
     *
     * @param featureTypeName
     * @param filter
     *
     * @return
     */
    protected FeatureReader getFeatureReader(String featureTypeName,
        String filter) {
        try {
            DefaultQuery q = new DefaultQuery(featureTypeName,
                    (Filter) ExpressionBuilder.parse(filter));

            return ds.getFeatureReader(q, Transaction.AUTO_COMMIT);
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }

    /**
     * Obtain a feature reader for all the features of the given featureType
     *
     * @param featureTypeName
     *
     * @return
     */
    protected FeatureReader getFeatureReader(String featureTypeName) {
        return getFeatureReader(featureTypeName, "1=1");
    }
}
