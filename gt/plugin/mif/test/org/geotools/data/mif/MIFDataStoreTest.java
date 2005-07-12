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

import com.vividsolutions.jts.geom.LineString;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.geotools.data.DefaultQuery;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeBuilder;
import org.geotools.filter.Filter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Date;
import java.util.HashMap;


/**
 * DOCUMENT ME!
 *
 * @author Luca S. Percich, AMA-MI
 */
public class MIFDataStoreTest extends TestCase {
    private MIFDataStore ds;

    /**
     * DOCUMENT ME!
     *
     * @param args DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public static void main(java.lang.String[] args) throws Exception {
        junit.textui.TestRunner.run(new TestSuite(MIFDataStoreTest.class));
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
     * Utility method for instantiating a MIFDataStore
     *
     * @param initPath DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected boolean initDS(String initPath) {
        try {
            initPath = MIFTestUtils.fileName(initPath);

            HashMap params = MIFTestUtils.getParams("mif", initPath, null);
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
        initDS("");

        try {
            assertNotNull(ds.getSchema("grafo"));
            assertNotNull(ds.getSchema("nodi"));
            assertNotNull(ds.getSchema("mixed"));
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    /**
     */
    public void testCreateSchema() {
        initDS("");

        try {
            FeatureTypeBuilder builder = FeatureTypeBuilder.newInstance(
                    "newschema");
            builder.addType(AttributeTypeFactory.newAttributeType("obj",
                    LineString.class, true));
            builder.addType(AttributeTypeFactory.newAttributeType("charfield",
                    String.class, false, 25, ""));
            builder.addType(AttributeTypeFactory.newAttributeType("intfield",
                    Integer.class, false, 0, new Integer(0)));

            builder.addType(AttributeTypeFactory.newAttributeType("datefield",
                    Date.class, true));
            builder.addType(AttributeTypeFactory.newAttributeType(
                    "doublefield", Double.class, false, 0, new Double(0)));
            builder.addType(AttributeTypeFactory.newAttributeType(
                    "floatfield", Float.class, false, 0, new Float(0)));
            builder.addType(AttributeTypeFactory.newAttributeType("boolfield",
                    Boolean.class, false, 0, new Boolean(false)));

            FeatureType newFT = builder.getFeatureType();

            ds.createSchema(newFT);

            FeatureType builtFT = ds.getSchema("newschema");

            assertEquals(builtFT, newFT);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    /**
     */
    public void testCreateSchemaBadGeometry() {
        initDS("");

        try {
            FeatureTypeBuilder builder = FeatureTypeBuilder.newInstance(
                    "newschema");
            builder.addType(AttributeTypeFactory.newAttributeType("charfield",
                    String.class, false, 25, ""));
            builder.addType(AttributeTypeFactory.newAttributeType("intfield",
                    Integer.class, false, 0, new Integer(0)));
            builder.addType(AttributeTypeFactory.newAttributeType("obj",
                    LineString.class, true));

            FeatureType newFT = builder.getFeatureType();

            ds.createSchema(newFT);
            fail("SchemaException expected"); // Geometry must be the first field
        } catch (Exception e) {
        }
    }

    /**
     */
    public void testCreateSchemaTwoGeometry() {
        initDS("");

        try {
            FeatureTypeBuilder builder = FeatureTypeBuilder.newInstance(
                    "newschema");
            builder.addType(AttributeTypeFactory.newAttributeType("obj",
                    LineString.class, true));
            builder.addType(AttributeTypeFactory.newAttributeType("charfield",
                    String.class, false, 25, ""));
            builder.addType(AttributeTypeFactory.newAttributeType("obj2",
                    LineString.class, true));
            builder.addType(AttributeTypeFactory.newAttributeType("intfield",
                    Integer.class, false, 0, new Integer(0)));

            FeatureType newFT = builder.getFeatureType();

            ds.createSchema(newFT);
            fail("SchemaException expected"); // Only one geometry
        } catch (Exception e) {
        }
    }

    /**
     */
    public void testFeatureReaderFilter() {
        initDS("grafo"); // .mif

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
        initDS("");

        String outmif = "grafo_new";

        try {
            FeatureType newFT = MIFTestUtils.duplicateSchema(ds.getSchema(
                        "grafo"), outmif);
            ds.createSchema(newFT);

            int maxAttr = newFT.getAttributeCount() - 1;

            FeatureWriter fw = ds.getFeatureWriterAppend(outmif,
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

            fr.close();
            fw.close();

            assertEquals(counter, 2);

            fw = ds.getFeatureWriter(outmif,
                    MIFTestUtils.parseFilter("ID == 71045"),
                    Transaction.AUTO_COMMIT);

            assertEquals(true, fw.hasNext());
            f = fw.next();
            fw.remove();

            fw.close();

            fw = ds.getFeatureWriterAppend(outmif, Transaction.AUTO_COMMIT);

            f = fw.next();
            f.setAttribute("ID", "99998");
            f.setAttribute("NOMECOMUNE", "foobar");
            fw.write();

            fw.close();

            fr = getFeatureReader(outmif);

            counter = 0;

            while (fr.hasNext()) {
                f = fr.next();
                counter++;
            }

            fr.close();
            assertEquals(counter, 2);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    /**
     * Tests createSchema & FeatureWriter
     */
    public void testFeatureWriterAppendTransaction() {
        try {
            String outmif = "grafo_append";
            MIFTestUtils.copyMif("grafo", outmif);
            initDS(outmif);

            Feature f;
            Transaction transaction = new DefaultTransaction("mif");

            try {
                FeatureWriter fw = ds.getFeatureWriterAppend(outmif, transaction);

                f = fw.next();
                f = fw.next();
                f.setAttribute("ID", "80001");
                f.setAttribute("NOMECOMUNE", "foo");
                fw.write();

                f = fw.next();
                f.setAttribute("ID", "80002");
                f.setAttribute("NOMECOMUNE", "bar");
                fw.write();

                fw.close();

                transaction.commit();
            } catch (Exception e) {
                transaction.rollback();
                fail(e.getMessage());
            } finally {
                transaction.close();
            }

            FeatureReader fr = getFeatureReader(outmif,
                    "ID > 80000 && ID <80003");

            int counter = 0;

            while (fr.hasNext()) {
                f = fr.next();
                counter++;
            }

            fr.close();

            assertEquals(counter, 2);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void testFeatureSource() {
        String outmif = "mixed_fs";

        try {
            MIFTestUtils.copyMif("mixed", outmif);
        } catch (FileNotFoundException e) {
            fail(e.getMessage());
        }

        initDS(outmif);

        FeatureSource fs = null;
        FeatureType featureType = null;

        try {
            featureType = ds.getSchema(outmif);
            assertNotNull("Cannot get FeatureType", featureType);
        } catch (Exception e) {
            fail("Cannot get FeatureType: " + e.getMessage());
        }

        try {
            fs = ds.getFeatureSource(outmif);
            assertNotNull("Cannot get FeatureSource.", fs);
        } catch (IOException e) {
            fail("Cannot get FeatureSource: " + e.getMessage());
        }

        try {
            ((FeatureStore) fs).modifyFeatures(featureType.getAttributeType(
                    "DESCRIPTION"), "FOO", Filter.NONE);
        } catch (Exception e) {
            fail("Cannot update Features: " + e.getMessage());
        }

        try {
            ((FeatureStore) fs).removeFeatures(MIFTestUtils.parseFilter(
                    "GEOMTYPE != 'NULL'"));
        } catch (IOException e) {
            fail("Cannot delete Features: " + e.getMessage());
        }

        try {
            FeatureReader fr = getFeatureReader(outmif);

            assertEquals(true, fr.hasNext());

            Feature f = fr.next();
            assertEquals("FOO", f.getAttribute("DESCRIPTION"));
            assertEquals(false, fr.hasNext());

            fr.close();
        } catch (Exception e) {
            fail("Cannot check feature: " + e.getMessage());
        }
    }

    /**
     * Test that feature get the correct SRID
     */
    public void testSRID() {
        initDS("");

        FeatureReader fr;

        try {
            fr = getFeatureReader("grafo");

            Feature f = fr.next();
            assertEquals(f.getDefaultGeometry().getFactory().getSRID(),
                MIFTestUtils.SRID);

            fr.close();
        } catch (Exception e) {
            fail("Cannot check SRID: " + e.getMessage());
        }
    }

    /**
     * Obtain a feature reader for the given featureType / filter
     *
     * @param featureTypeName
     * @param filter
     *
     * @return
     *
     * @throws Exception
     */
    protected FeatureReader getFeatureReader(String featureTypeName,
        String filter) throws Exception {
        DefaultQuery q = new DefaultQuery(featureTypeName,
                MIFTestUtils.parseFilter(filter));

        return ds.getFeatureReader(q, Transaction.AUTO_COMMIT);
    }

    /**
     * Obtain a feature reader for all the features of the given featureType
     *
     * @param featureTypeName
     *
     * @return
     *
     * @throws Exception
     */
    protected FeatureReader getFeatureReader(String featureTypeName)
        throws Exception {
        return getFeatureReader(featureTypeName, "1=1");
    }
}
