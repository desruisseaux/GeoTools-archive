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

import java.io.IOException;
import java.sql.Date;
import java.util.HashMap;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.geotools.data.DefaultQuery;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeBuilder;
import org.geotools.filter.ExpressionBuilder;
import org.geotools.filter.Filter;

import com.vividsolutions.jts.geom.LineString;


/**
 * DOCUMENT ME!
 *
 * @author Luca S. Percich, AMA-MI
 */
public class MIFDataStoreTest extends TestCase {
    private MIFDataStore ds;
    private String dataPath = MIFTestUtils.getDataPath();

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
     */
    public void testCreateSchema() {
        initDS(dataPath);

        try {
            FeatureTypeBuilder builder = FeatureTypeBuilder.newInstance("newschema");
            builder.addType(AttributeTypeFactory.newAttributeType("obj",
                    LineString.class, true));
            builder.addType(AttributeTypeFactory.newAttributeType("charfield",
                    String.class, false, 25, ""));
            builder.addType(AttributeTypeFactory.newAttributeType("intfield",
                    Integer.class, false, 0, new Integer(0)));

            builder.addType(AttributeTypeFactory.newAttributeType("datefield",
                    Date.class, true));
            builder.addType(AttributeTypeFactory.newAttributeType("doublefield",
                    Double.class, false, 0, new Double(0)));
            builder.addType(AttributeTypeFactory.newAttributeType("floatfield",
                    Float.class, false, 0, new Float(0)));
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
        initDS(dataPath);

        try {
            FeatureTypeBuilder builder = FeatureTypeBuilder.newInstance("newschema");
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
        initDS(dataPath);

        try {
            FeatureTypeBuilder builder = FeatureTypeBuilder.newInstance("newschema");
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
        initDS(dataPath + "grafo"); // .mif

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

            FeatureTypeBuilder builder = FeatureTypeBuilder.newInstance("grafo_new");
            
            for (int i = 0; i < ft.getAttributeCount(); i++) {
                builder.addType(ft.getAttributeType(i));
            }

            FeatureType newFT = builder.getFeatureType();
            ds.createSchema(newFT);

            Transaction transaction = new DefaultTransaction(); // Transaction.AUTO_COMMIT; 

            FeatureWriter fw = ds.getFeatureWriterAppend("grafo_new",
                    transaction);
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

            transaction.commit();

            fw.close();

            assertEquals(counter, 2);

            transaction = new DefaultTransaction(); // Transaction.AUTO_COMMIT;
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

            fw = ds.getFeatureWriterAppend("grafo_new", transaction);

            f = fw.next();
            f.setAttribute("ID", "99998");
            f.setAttribute("NOMECOMUNE", "foo");
            fw.write();

            f = fw.next();
            f.setAttribute("ID", "99999");
            f.setAttribute("NOMECOMUNE", "bar");
            fw.write();

            transaction.commit();

            fw.close();
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
