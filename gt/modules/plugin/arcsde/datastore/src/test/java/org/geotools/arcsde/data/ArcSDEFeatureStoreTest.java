/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2008, Geotools Project Managment Committee (PMC)
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
 */
package org.geotools.arcsde.data;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import junit.extensions.TestSetup;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.geotools.arcsde.ArcSDEDataStoreFactory;
import org.geotools.arcsde.pool.ArcSDEConnectionPool;
import org.geotools.arcsde.pool.ArcSDEPooledConnection;
import org.geotools.data.DataStore;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultQuery;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.filter.text.cql2.CQL;
import org.opengis.feature.Attribute;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.Id;
import org.opengis.filter.identity.FeatureId;

import com.esri.sde.sdk.client.SeConnection;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeQuery;
import com.esri.sde.sdk.client.SeRegistration;
import com.esri.sde.sdk.client.SeRow;
import com.esri.sde.sdk.client.SeSqlConstruct;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTReader;

/**
 * Unit tests for transaction support
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/plugin/arcsde/da/src/test/java/org/geotools/arcsde/data/ArcSDEFeatureStoreTest.java $
 * @version $Id$
 */
public class ArcSDEFeatureStoreTest extends TestCase {
    /** package logger */
    private static Logger LOGGER = org.geotools.util.logging.Logging
            .getLogger(ArcSDEFeatureStoreTest.class.getPackage().getName());

    /** DOCUMENT ME! */
    private static TestData testData;

    /**
     * Builds a test suite for all this class' tests with per suite
     * initialization directed to {@link #oneTimeSetUp()} and per suite clean up
     * directed to {@link #oneTimeTearDown()}
     * 
     * @return
     */
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(ArcSDEFeatureStoreTest.class);

        TestSetup wrapper = new TestSetup(suite) {
            protected void setUp() throws Exception {
                oneTimeSetUp();
            }

            protected void tearDown() {
                oneTimeTearDown();
            }
        };
        return wrapper;
    }

    private static void oneTimeSetUp() throws Exception {
        testData = new TestData();
        testData.setUp();
        if (ArcSDEDataStoreFactory.getSdeClientVersion() == ArcSDEDataStoreFactory.JSDE_VERSION_DUMMY) {
            throw new RuntimeException("Don't run the test-suite with the dummy jar.  "
                    + "Make sure the real ArcSDE jars are on your classpath.");
        }
        // do not insert test data, will do it at each test case
        final boolean insertTestData = false;
        testData.createTempTable(insertTestData);
    }

    private static void oneTimeTearDown() {
        boolean cleanTestTable = false;
        boolean cleanPool = true;
        testData.tearDown(cleanTestTable, cleanPool);
    }

    /**
     * loads {@code test-data/testparams.properties} into a Properties object,
     * wich is used to obtain test tables names and is used as parameter to find
     * the DataStore
     * 
     * @throws Exception
     *             DOCUMENT ME!
     */
    protected void setUp() throws Exception {
        super.setUp();
        // facilitates running a single test at a time (eclipse lets you do this
        // and it's very useful)
        if (testData == null) {
            oneTimeSetUp();
        }
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testDeleteByFIDAutoCommit() throws Exception {
        testData.insertTestData();

        final DataStore ds = testData.getDataStore();
        final String typeName = testData.getTemp_table();

        final String fid;
        final Filter fidFilter;
        {
            // get a fid
            DefaultQuery query = new DefaultQuery(typeName);
            FeatureReader<SimpleFeatureType, SimpleFeature> reader = ds.getFeatureReader(query,
                    Transaction.AUTO_COMMIT);
            try {
                fid = reader.next().getID();
            } finally {
                reader.close();
            }

            final FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);
            Set<FeatureId> ids = new HashSet<FeatureId>();
            ids.add(ff.featureId(fid));
            fidFilter = ff.id(ids);
        }

        {
            FeatureWriter<SimpleFeatureType, SimpleFeature> writer = ds.getFeatureWriter(typeName,
                    fidFilter, Transaction.AUTO_COMMIT);

            try {
                assertTrue(writer.hasNext());
                SimpleFeature feature = writer.next();
                assertEquals(fid, feature.getID());
                writer.remove();
                assertFalse(writer.hasNext());
            } finally {
                writer.close();
            }
        }

        ArcSDEConnectionPool connectionPool = testData.getConnectionPool();
        ArcSDEPooledConnection connection = connectionPool.getConnection();
        SeQuery seQuery;
        try {
            int objectId = (int) ArcSDEAdapter.getNumericFid(fid);
            String whereClause = "ROW_ID=" + objectId;
            seQuery = new SeQuery(connection, new String[] { "ROW_ID", "INT32_COL", "STRING_COL" },
                    new SeSqlConstruct(typeName, whereClause));
            seQuery.prepareQuery();
            seQuery.execute();
        } finally {
            connection.close();
        }
        SeRow row = seQuery.fetch();
        assertNull(row);

        // was it really removed?
        {
            DefaultQuery query = new DefaultQuery(typeName, fidFilter);
            FeatureReader<SimpleFeatureType, SimpleFeature> reader = ds.getFeatureReader(query,
                    Transaction.AUTO_COMMIT);
            try {
                assertFalse(reader.hasNext());
            } finally {
                reader.close();
            }
        }
    }

    /**
     * Tests that all the features that match a filter based on attribute only
     * filters (aka non spatial filters), are deleted correctly. This test
     * assumes that there are no duplicate values in the test data.
     * 
     * @throws Exception
     */
    public void testDeleteByAttOnlyFilter() throws Exception {
        testData.insertTestData();

        final DataStore ds = testData.getDataStore();
        final String typeName = testData.getTemp_table();

        // get 2 features and build an OR'ed PropertyIsEqualTo filter
        Filter or = CQL.toFilter("INT32_COL = 1 OR INT32_COL = 2");
        FeatureWriter<SimpleFeatureType, SimpleFeature> writer = ds.getFeatureWriter(typeName, or,
                Transaction.AUTO_COMMIT);

        try {
            assertTrue(writer.hasNext());

            SimpleFeature feature = writer.next();
            assertEquals(Integer.valueOf(1), feature.getAttribute("INT32_COL"));
            writer.remove();

            feature = writer.next();
            assertEquals(Integer.valueOf(2), feature.getAttribute("INT32_COL"));
            writer.remove();

            assertFalse(writer.hasNext());
        } finally {
            writer.close();
        }

        // was it really removed?
        FeatureReader<SimpleFeatureType, SimpleFeature> read = ds.getFeatureReader(
                new DefaultQuery(typeName, or), Transaction.AUTO_COMMIT);
        try {
            assertFalse(read.hasNext());
        } finally {
            read.close();
        }
    }

    /**
     * Tests the creation of new feature types, with CRS and all.
     * 
     * <p>
     * This test also ensures that the arcsde datastore is able of creating
     * schemas where the geometry attribute is not the last one. This is
     * important since to do so, the ArcSDE datastore must break the usual way
     * of creating schemas with the ArcSDE Java API, in which one first creates
     * the (non spatially enabled) "table" with all the non spatial attributes
     * and finally creates the "layer", adding the spatial attribute to the
     * previously created table. So, this test ensures the datastore correctly
     * works arround this limitation.
     * </p>
     * 
     * @throws IOException
     *             DOCUMENT ME!
     * @throws SchemaException
     *             DOCUMENT ME!
     * @throws SeException
     */
    public void _testCreateSchema() throws IOException, SchemaException, SeException {
        final String typeName;
        {
            ArcSDEConnectionPool connectionPool = testData.getConnectionPool();
            ArcSDEPooledConnection connection = connectionPool.getConnection();
            final String user;
            user = connection.getUser();
            connection.close();
            typeName = user + ".GT_TEST_CREATE";
        }

        SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();
        b.setName(typeName);

        b.add("FST_COL", String.class);
        b.add("SECOND_COL", String.class);
        b.add("GEOM", Point.class);
        b.add("FOURTH_COL", Integer.class);

        final SimpleFeatureType type = b.buildFeatureType();

        DataStore ds = testData.getDataStore();
        testData.deleteTable(typeName);

        Map hints = new HashMap();
        hints.put("configuration.keyword", testData.getConfigKeyword());
        ((ArcSDEDataStore) ds).createSchema(type, hints);

        testData.deleteTable(typeName);
    }

    public void testInsertAutoCommit() throws Exception {
        // the table populated here is test friendly since it can hold
        // any kind of geometries.
        testData.truncateTempTable();

        // there are some commented out just because the server I'm hitting
        // is slow, not because they don't work. Feel free to uncomment.
        // testInsertAutoCommit(Geometry.class);
        testInsertAutoCommit(Point.class);
        // testInsertAutoCommit(MultiPoint.class);
        testInsertAutoCommit(LineString.class);
        // testInsertAutoCommit(MultiLineString.class);
        testInsertAutoCommit(Polygon.class);
        // testInsertAutoCommit(MultiPolygon.class);
    }

    /**
     * Add features to a FeatureWriter with a {@link Transaction} and ensure if
     * the transaction was not committed, a request gets no features, and when
     * the transaction is committed the query returns it.
     * 
     * @throws Exception
     */
    public void testInsertTransaction() throws Exception {
        // start with an empty table
        testData.truncateTempTable();
        final String typeName = testData.getTemp_table();
        final int featureCount = 2;
        final FeatureCollection<SimpleFeatureType, SimpleFeature> testFeatures = testData
                .createTestFeatures(LineString.class, featureCount);

        final DataStore ds = testData.getDataStore();

        final SimpleFeatureType ftype = testFeatures.getSchema();
        final FeatureIterator<SimpleFeature> iterator = testFeatures.features();

        final Transaction transaction = new DefaultTransaction();
        final FeatureWriter<SimpleFeatureType, SimpleFeature> writer = ds.getFeatureWriter(
                typeName, transaction);

        try {
            while (iterator.hasNext()) {
                SimpleFeature addFeature = iterator.next();
                SimpleFeature newFeature = writer.next();
                for (int i = 0; i < ftype.getAttributeCount(); i++) {
                    String localName = ftype.getAttribute(i).getLocalName();
                    newFeature.setAttribute(localName, addFeature.getAttribute(localName));
                }
                writer.write();
            }
        } finally {
            writer.close();
        }

        FeatureReader<SimpleFeatureType, SimpleFeature> reader = ds.getFeatureReader(
                new DefaultQuery(typeName), Transaction.AUTO_COMMIT);
        try {
            assertFalse("Features added, transaction not commited", reader.hasNext());
        } finally {
            reader.close();
        }

        try {
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            throw e;
        } finally {
            transaction.close();
        }

        try {
            reader = ds.getFeatureReader(new DefaultQuery(typeName), Transaction.AUTO_COMMIT);
            for (int i = 0; i < featureCount; i++) {
                assertTrue(reader.hasNext());
                reader.next();
            }
            assertFalse(reader.hasNext());
        } finally {
            reader.close();
        }
    }

    @SuppressWarnings("unchecked")
    public void testInsertTransactionAndQueryByFid() throws Exception {
        // start with an empty table
        final String typeName = testData.getTemp_table();
        final int featureCount = 2;
        final FeatureCollection<SimpleFeatureType, SimpleFeature> testFeatures = testData
                .createTestFeatures(LineString.class, featureCount);

        final DataStore ds = testData.getDataStore();
        final FeatureStore<SimpleFeatureType, SimpleFeature> fStore;
        fStore = (FeatureStore<SimpleFeatureType, SimpleFeature>) ds.getFeatureSource(typeName);
        final Transaction transaction = new DefaultTransaction("testInsertTransactionAndQueryByFid");
        fStore.setTransaction(transaction);
        try {
            final Set<String> addedFids = fStore.addFeatures(testFeatures);
            final FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);

            final Set<FeatureId> fids = new HashSet<FeatureId>();
            for (String fid : addedFids) {
                fids.add(ff.featureId(fid));
            }
            final Id newFidsFilter = ff.id(fids);

            FeatureCollection<SimpleFeatureType, SimpleFeature> features = fStore
                    .getFeatures(newFidsFilter);
            assertEquals(2, features.size());
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            throw e;
        } finally {
            transaction.close();
        }
    }

    public void testUpdateAutoCommit() throws Exception {
        testData.insertTestData();

        final String typeName = testData.getTemp_table();
        final DataStore ds = testData.getDataStore();
        final Filter filter = CQL.toFilter("INT32_COL = 3");

        FeatureWriter<SimpleFeatureType, SimpleFeature> writer = ds.getFeatureWriter(typeName,
                filter, Transaction.AUTO_COMMIT);

        try {
            assertTrue(writer.hasNext());
            SimpleFeature feature = writer.next();
            feature.setAttribute("INT32_COL", Integer.valueOf(-1000));
            writer.write();
            assertFalse(writer.hasNext());
        } finally {
            writer.close();
        }

        DefaultQuery query = new DefaultQuery(typeName, filter);
        FeatureReader<SimpleFeatureType, SimpleFeature> reader = ds.getFeatureReader(query,
                Transaction.AUTO_COMMIT);
        try {
            assertFalse(reader.hasNext());
        } finally {
            reader.close();
        }

        query = new DefaultQuery(typeName, CQL.toFilter("INT32_COL = -1000"));
        reader = ds.getFeatureReader(query, Transaction.AUTO_COMMIT);
        try {
            assertTrue(reader.hasNext());
            reader.next();
            assertFalse(reader.hasNext());
        } finally {
            reader.close();
        }
    }

    public void testUpdateTransaction() throws Exception {
        testData.insertTestData();

        final String typeName = testData.getTemp_table();
        final DataStore ds = testData.getDataStore();
        final Filter oldValueFilter = CQL.toFilter("INT32_COL = 3");
        final Query oldValueQuery = new DefaultQuery(typeName, oldValueFilter);
        final Filter newValueFilter = CQL.toFilter("INT32_COL = -1000");
        final Query newValueQuery = new DefaultQuery(typeName, newValueFilter);

        final Transaction transaction = new DefaultTransaction("testUpdateTransaction");
        FeatureWriter<SimpleFeatureType, SimpleFeature> writer = ds.getFeatureWriter(typeName,
                oldValueFilter, transaction);

        try {
            assertTrue(writer.hasNext());
            SimpleFeature feature = writer.next();
            feature.setAttribute("INT32_COL", Integer.valueOf(-1000));
            writer.write();
            assertFalse(writer.hasNext());
        } finally {
            writer.close();
        }

        FeatureReader<SimpleFeatureType, SimpleFeature> reader = ds.getFeatureReader(oldValueQuery,
                Transaction.AUTO_COMMIT);
        try {
            assertTrue(reader.hasNext());
        } finally {
            reader.close();
        }

        reader = ds.getFeatureReader(newValueQuery, Transaction.AUTO_COMMIT);
        try {
            assertFalse(reader.hasNext());
        } finally {
            reader.close();
        }

        reader = ds.getFeatureReader(oldValueQuery, transaction);
        try {
            assertFalse(reader.hasNext());
        } finally {
            reader.close();
        }

        reader = ds.getFeatureReader(newValueQuery, transaction);
        try {
            assertTrue(reader.hasNext());
        } finally {
            reader.close();
        }

        try {
            transaction.commit();
        } catch (IOException e) {
            transaction.rollback();
            throw e;
        } finally {
            transaction.close();
        }

        reader = ds.getFeatureReader(newValueQuery, Transaction.AUTO_COMMIT);
        try {
            assertTrue(reader.hasNext());
        } finally {
            reader.close();
        }
    }

    public void testModifyFeaturesTransaction() throws Exception {
        testData.insertTestData();

        final String typeName = testData.getTemp_table();
        final DataStore ds = testData.getDataStore();
        final FeatureStore<SimpleFeatureType, SimpleFeature> store;
        store = (FeatureStore<SimpleFeatureType, SimpleFeature>) ds.getFeatureSource(typeName);
        final SimpleFeatureType schema = store.getSchema();
        final Filter oldValueFilter = CQL.toFilter("INT32_COL = 3");
        final Filter newValueFilter = CQL.toFilter("INT32_COL = -1000");

        FeatureCollection<SimpleFeatureType, SimpleFeature> features = store
                .getFeatures(oldValueFilter);
        final int initialSize = features.size();
        assertEquals(1, initialSize);// just to not go forward with bad data
        final SimpleFeature originalFeature;
        FeatureIterator<SimpleFeature> iterator = features.features();
        try {
            originalFeature = iterator.next();
        } finally {
            iterator.close();
        }

        {
            final Transaction transaction = new DefaultTransaction("testModifyFeaturesTransaction");
            store.setTransaction(transaction);

            try {
                final AttributeDescriptor propDescriptor = schema.getAttribute("INT32_COL");
                store.modifyFeatures(propDescriptor, Integer.valueOf(-1000), oldValueFilter);
                transaction.commit();
            } catch (Exception e) {
                transaction.rollback();
            } finally {
                transaction.close();
            }
        }
        store.setTransaction(Transaction.AUTO_COMMIT);

        final Query oldValueQuery = new DefaultQuery(typeName, oldValueFilter);
        final Query newValueQuery = new DefaultQuery(typeName, newValueFilter);

        assertEquals(0, store.getCount(oldValueQuery));
        assertEquals(1, store.getCount(newValueQuery));

        final FeatureIterator<SimpleFeature> newFeatures = store.getFeatures(newValueQuery)
                .features();
        final SimpleFeature modifiedFeature;
        try {
            modifiedFeature = newFeatures.next();
        } finally {
            newFeatures.close();
        }

        // verify the non modified properties stay the same
        final List<Attribute> originalValues = (List<Attribute>) originalFeature.getValue();
        final List<Attribute> actualValues = (List<Attribute>) modifiedFeature.getValue();
        for (int i = 0; i < originalValues.size(); i++) {
            Attribute originalAtt = originalValues.get(i);
            Attribute actualAtt = actualValues.get(i);
            Name name = originalAtt.getName();
            // bah, date equals does not work, I don't care for this test
            if (!"INT32_COL".equals(name.getLocalPart()) && !"DATE_COL".equals(name.getLocalPart())) {
                assertEquals(name + " does not match", originalAtt, actualAtt);
            }
        }
    }

    public void testUpdateAdjacentPolygonsTransaction() throws Exception {
        final WKTReader reader = new WKTReader();
        final MultiPolygon p1 = (MultiPolygon) reader
                .read("MULTIPOLYGON(((-10 -10, -10 10, 0 10, 0 -10, -10 -10)))");
        final MultiPolygon p2 = (MultiPolygon) reader
                .read("MULTIPOLYGON(((0 -10, 0 10, 10 10, 10 -10, 0 -10)))");

        final MultiPolygon modif1 = (MultiPolygon) reader
                .read("MULTIPOLYGON (((-10 -10, -10 10, 5 10, -5 -10, -10 -10)))");
        final MultiPolygon modif2 = (MultiPolygon) reader
                .read("MULTIPOLYGON (((-5 -10, 5 10, 10 10, 10 -10, -5 -10)))");

        // final MultiPolygon p1 = (MultiPolygon) reader
        // .read("MULTIPOLYGON (((320000 545000, 320000 545100, 320100 545100,
        // 320100 545000, 320000 545000)))");
        // final MultiPolygon p2 = (MultiPolygon) reader
        // .read("MULTIPOLYGON (((320100 545000, 320100 545100, 320200 545100,
        // 320200 545000, 320100 545000)))");
        //
        // final MultiPolygon modif1 = (MultiPolygon) reader
        // .read("MULTIPOLYGON (((320000 545000, 320000 545100, 320150 545100,
        // 320050 545000, 320000 545000)))");
        // final MultiPolygon modif2 = (MultiPolygon) reader
        // .read("MULTIPOLYGON (((320050 545000, 320150 545100, 320200 545100,
        // 320200 545000, 320050 545000)))");

        final String typeName = testData.getTemp_table(); // "SDE.CJ_TST_1";
        final ArcSDEDataStore dataStore = testData.getDataStore();
        // String[] typeNames = dataStore.getTypeNames();
        // System.err.println(typeNames);
        final FeatureStore<SimpleFeatureType, SimpleFeature> store;
        store = (FeatureStore<SimpleFeatureType, SimpleFeature>) dataStore
                .getFeatureSource(typeName);
        final SimpleFeatureType schema = store.getSchema();
        GeometryDescriptor defaultGeometry = schema.getDefaultGeometry();
        String fid1;
        String fid2;
        // insert polygons p1, p2 and grab the fids for later retrieval
        {
            FeatureWriter<SimpleFeatureType, SimpleFeature> writer = dataStore
                    .getFeatureWriterAppend(typeName, Transaction.AUTO_COMMIT);
            SimpleFeature feature;
            try {
                feature = writer.next();
                feature.setAttribute(defaultGeometry.getName(), p1);
                writer.write();
                fid1 = feature.getID();

                feature = writer.next();
                feature.setAttribute(defaultGeometry.getName(), p2);
                writer.write();
                fid2 = feature.getID();
            } finally {
                writer.close();
            }
        }

        final Transaction transaction = new DefaultTransaction("testUpdateAdjacentPolygons");
        store.setTransaction(transaction);
        final FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);
        Filter fid1Filter = ff.id(Collections.singleton(ff.featureId(fid1)));
        Filter fid2Filter = ff.id(Collections.singleton(ff.featureId(fid2)));
        try {
            store.modifyFeatures(defaultGeometry, modif2, fid2Filter);
            store.modifyFeatures(defaultGeometry, modif1, fid1Filter);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            throw e;
        } finally {
            transaction.close();
        }
        store.setTransaction(Transaction.AUTO_COMMIT);

        try {
            FeatureCollection<SimpleFeatureType, SimpleFeature> features;
            FeatureIterator<SimpleFeature> iterator;

            features = store.getFeatures(fid1Filter);
            iterator = features.features();
            final SimpleFeature feature1 = iterator.next();
            iterator.close();

            features = store.getFeatures(fid2Filter);
            iterator = features.features();
            final SimpleFeature feature2 = iterator.next();
            iterator.close();

            MultiPolygon actual1 = (MultiPolygon) feature1.getAttribute(defaultGeometry
                    .getLocalName());
            MultiPolygon actual2 = (MultiPolygon) feature2.getAttribute(defaultGeometry
                    .getLocalName());
            System.out.println(actual1);
            System.out.println(actual2);
            assertTrue(modif1.equals(actual1));
            assertTrue(modif2.equals(actual2));
        } finally {
            try {
                store.removeFeatures(fid1Filter);
                store.removeFeatures(fid2Filter);
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }
    }

    /**
     * Tests the writing of features with autocommit transaction.
     * 
     * @param geometryClass
     *            DOCUMENT ME!
     * 
     * @throws Exception
     *             DOCUMENT ME!
     * @throws IllegalArgumentException
     *             DOCUMENT ME!
     */
    private void testInsertAutoCommit(Class<? extends Geometry> geometryClass) throws Exception {
        final String typeName = testData.getTemp_table();
        final int insertCount = 2;
        final FeatureCollection<SimpleFeatureType, SimpleFeature> testFeatures = testData
                .createTestFeatures(geometryClass, insertCount);

        final DataStore ds = testData.getDataStore();
        final FeatureSource<SimpleFeatureType, SimpleFeature> fsource = ds
                .getFeatureSource(typeName);

        // incremented on each feature added event to
        // ensure events are being raised as expected
        // (the count is wraped inside an array to be able of declaring
        // the variable as final and accessing it from inside the anonymous
        // inner class)
        /*
         * final int[] featureAddedEventCount = { 0 };
         * 
         * fsource.addFeatureListener(new FeatureListener() { public void
         * changed(FeatureEvent evt) { if (evt.getEventType() !=
         * FeatureEvent.FEATURES_ADDED) { throw new IllegalArgumentException(
         * "Expected FEATURES_ADDED event, got " + evt.getEventType()); }
         * 
         * ++featureAddedEventCount[0]; } });
         */

        final int initialCount = fsource.getCount(Query.ALL);

        FeatureWriter<SimpleFeatureType, SimpleFeature> writer = ds.getFeatureWriterAppend(
                typeName, Transaction.AUTO_COMMIT);

        SimpleFeature source;
        SimpleFeature dest;

        try {
            for (FeatureIterator<SimpleFeature> fi = testFeatures.features(); fi.hasNext();) {
                source = fi.next();
                dest = writer.next();
                dest.setAttributes(source.getAttributes());
                writer.write();
            }
        } finally {
            writer.close();
        }

        // was the features really inserted?
        int fcount = fsource.getCount(Query.ALL);
        assertEquals(testFeatures.size() + initialCount, fcount);

        /*
         * String msg = "a FEATURES_ADDED event should have been called " +
         * features.size() + " times"; assertEquals(msg, features.size(),
         * featureAddedEventCount[0]);
         */
    }

    public void testCreateNillableShapeSchema() throws IOException, SchemaException, SeException {
        SimpleFeatureType type;
        final String typeName = "GT_TEST_CREATE";

        SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();
        b.setName(typeName);

        b.add("OBJECTID", Integer.class);
        b.add("SHAPE", MultiLineString.class);

        type = b.buildFeatureType();

        ArcSDEDataStore ds = testData.getDataStore();

        testData.deleteTable(typeName);
        Map hints = new HashMap();
        hints.put("configuration.keyword", testData.getConfigKeyword());
        ds.createSchema(type, hints);
        testData.deleteTable(typeName);
    }

    public void testWriteAndUpdateNullShapes() throws Exception {
        final String typeName = testData.getTemp_table();
        testData.truncateTempTable();

        DataStore ds = testData.getDataStore();

        SimpleFeature feature;
        FeatureWriter<SimpleFeatureType, SimpleFeature> writer;
        writer = ds.getFeatureWriter(typeName, Transaction.AUTO_COMMIT);
        try {
            feature = writer.next();
            feature.setAttribute("INT32_COL", Integer.valueOf(1000));

            writer.write();
        } finally {
            writer.close();
        }
        LOGGER.info("Wrote null-geom feature to sde");

        FeatureReader<SimpleFeatureType, SimpleFeature> reader;
        reader = ds.getFeatureReader(new DefaultQuery(typeName, Filter.INCLUDE),
                Transaction.AUTO_COMMIT);

        // save the ID to update the feature later
        String newId;
        try {
            assertTrue(reader.hasNext());
            feature = reader.next();
            LOGGER.info("recovered geometry " + feature.getDefaultGeometry()
                    + " from single inserted feature.");
            assertNull(feature.getDefaultGeometry());
            newId = feature.getID();
            assertFalse(reader.hasNext());
        } finally {
            reader.close();
        }
        LOGGER.info("Confirmed exactly one feature in new sde layer");

        FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);
        HashSet<FeatureId> ids = new HashSet<FeatureId>();
        ids.add(ff.featureId(newId));
        Filter idFilter = ff.id(ids);

        writer = ds.getFeatureWriter(typeName, idFilter, Transaction.AUTO_COMMIT);

        try {
            assertTrue(writer.hasNext());

            LOGGER.info("Confirmed feature is fetchable via it's api-determined FID");

            GeometryFactory gf = new GeometryFactory();
            int index = 10;
            Coordinate[] coords1 = { new Coordinate(0, 0), new Coordinate(++index, ++index) };
            Coordinate[] coords2 = { new Coordinate(0, index), new Coordinate(index, 0) };
            LineString[] lines = { gf.createLineString(coords1), gf.createLineString(coords2) };
            MultiLineString sampleMultiLine = gf.createMultiLineString(lines);

            SimpleFeature toBeUpdated = writer.next();
            toBeUpdated.setAttribute("SHAPE", sampleMultiLine);
            writer.write();
        } finally {
            writer.close();
        }
        LOGGER.info("Null-geom feature updated with a sample geometry.");

        DefaultQuery query = new DefaultQuery(testData.getTemp_table(), idFilter);
        reader = ds.getFeatureReader(query, Transaction.AUTO_COMMIT);
        try {
            assertTrue(reader.hasNext());
            feature = reader.next();
            MultiLineString recoveredMLS = (MultiLineString) feature.getDefaultGeometry();
            assertTrue(!recoveredMLS.isEmpty());
            // I tried to compare the recovered MLS to the
            // sampleMultiLineString, but they're
            // slightly different. SDE does some rounding, and winds up giving
            // me 0.0000002 for zero,
            // and 11.9992 for 12. Meh.
        } finally {
            reader.close();
        }
    }

    /**
     * Tests the writing of features with real transactions
     * 
     * @throws UnsupportedOperationException
     *             DOCUMENT ME!
     */
    public void testFeatureWriterTransaction() throws Exception {
        // the table populated here is test friendly since it can hold
        // any kind of geometries.
        testData.insertTestData();

        final String typeName = testData.getTemp_table();

        final DataStore ds = testData.getDataStore();
        final FeatureSource<SimpleFeatureType, SimpleFeature> fsource = ds
                .getFeatureSource(typeName);

        final int initialCount = fsource.getCount(Query.ALL);
        final int writeCount = initialCount + 2;
        final FeatureCollection<SimpleFeatureType, SimpleFeature> testFeatures = testData
                .createTestFeatures(LineString.class, writeCount);

        // incremented on each feature added event to
        // ensure events are being raised as expected
        // (the count is wraped inside an array to be able of declaring
        // the variable as final and accessing it from inside the anonymous
        // inner class)
        // final int[] featureAddedEventCount = { 0 };

        final Transaction transaction = new DefaultTransaction();
        final FeatureWriter<SimpleFeatureType, SimpleFeature> writer = ds.getFeatureWriter(
                typeName, Filter.INCLUDE, transaction);

        SimpleFeature source;
        SimpleFeature dest;

        int count = 0;
        try {
            for (FeatureIterator<SimpleFeature> fi = testFeatures.features(); fi.hasNext(); count++) {
                if (count < initialCount) {
                    assertTrue("at index " + count, writer.hasNext());
                } else {
                    assertFalse("at index " + count, writer.hasNext());
                }

                source = fi.next();
                dest = writer.next();
                dest.setAttributes(source.getAttributes());
                writer.write();
            }
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            throw e;
        } finally {
            writer.close();
            transaction.close();
        }

        // was the features really inserted?
        int fcount = fsource.getCount(Query.ALL);
        assertEquals(writeCount, fcount);

        /*
         * String msg = "a FEATURES_ADDED event should have been called " +
         * features.size() + " times"; assertEquals(msg, features.size(),
         * featureAddedEventCount[0]);
         */
    }

    /**
     * DOCUMENT ME!
     * 
     * @throws UnsupportedOperationException
     *             DOCUMENT ME!
     */
    public void testFeatureWriterAppend() throws Exception {
        // the table populated here is test friendly since it can hold
        // any kind of geometries.
        testData.insertTestData();

        final String typeName = testData.getTemp_table();
        final FeatureCollection<SimpleFeatureType, SimpleFeature> testFeatures = testData
                .createTestFeatures(LineString.class, 2);

        final DataStore ds = testData.getDataStore();
        final FeatureSource<SimpleFeatureType, SimpleFeature> fsource = ds
                .getFeatureSource(typeName);
        final int initialCount = fsource.getCount(Query.ALL);

        final FeatureWriter<SimpleFeatureType, SimpleFeature> writer = ds.getFeatureWriterAppend(
                typeName, Transaction.AUTO_COMMIT);

        SimpleFeature source;
        SimpleFeature dest;

        for (FeatureIterator<SimpleFeature> fi = testFeatures.features(); fi.hasNext();) {
            assertFalse(writer.hasNext());
            source = fi.next();
            dest = writer.next();
            dest.setAttributes(source.getAttributes());
            writer.write();
        }

        writer.close();

        // were the features really inserted?
        int fcount = fsource.getCount(Query.ALL);
        assertEquals(testFeatures.size() + initialCount, fcount);
    }

    /**
     * Ensure modified features for a given FeatureStore are returned by
     * subsequent queries even if the transaction has not been committed.
     * 
     * @throws Exception
     */
    public void testTransactionStateDiff() throws Exception {
        testData.insertTestData();

        final DataStore ds = testData.getDataStore();
        final String typeName = testData.getTemp_table();
        final FeatureStore<SimpleFeatureType, SimpleFeature> transFs = (FeatureStore<SimpleFeatureType, SimpleFeature>) ds
                .getFeatureSource(typeName);
        final SimpleFeatureType schema = transFs.getSchema();

        // once the transaction is set to the FeatureStore, it lasts until
        // another transaction
        // is set. Calling transaction.close() closes Transaction.State
        // held on it, allowing State objects to release resources. After
        // close() the transaction
        // is no longer valid.
        final Transaction transaction = new DefaultTransaction("test_handle");
        transFs.setTransaction(transaction);

        // create a feature to add
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(schema);
        builder.set("INT32_COL", Integer.valueOf(1000));
        builder.set("STRING_COL", "inside transaction");
        SimpleFeature feature = builder.buildFeature(null);

        // add the feature
        transFs.addFeatures(DataUtilities.collection(feature));

        // now confirm for that transaction the feature is fetched, and outside
        // it it's not.
        final Filter filterNewFeature = CQL.toFilter("INT32_COL = 1000");
        final DefaultQuery newFeatureQuery = new DefaultQuery(typeName, filterNewFeature);

        FeatureCollection<SimpleFeatureType, SimpleFeature> features = transFs
                .getFeatures(filterNewFeature);
        int size = features.size();
        assertEquals(1, size);

        // ok transaction respected, assert the feature does not exist outside
        // it
        {
            FeatureReader<SimpleFeatureType, SimpleFeature> autoCommitReader = ds.getFeatureReader(
                    newFeatureQuery, Transaction.AUTO_COMMIT);
            try {
                assertFalse(autoCommitReader.hasNext());
            } finally {
                autoCommitReader.close();
            }
        }

        // ok, but what if we ask for a feature reader with the same transaction
        {
            FeatureReader<SimpleFeatureType, SimpleFeature> transactionReader = ds
                    .getFeatureReader(newFeatureQuery, transaction);
            try {
                assertTrue(transactionReader.hasNext());
                transactionReader.next();
                assertFalse(transactionReader.hasNext());
            } finally {
                transactionReader.close();
            }
        }

        // now commit, and Transaction.AUTO_COMMIT should carry it over
        // do not close the transaction, we'll keep using it
        try {
            transaction.commit();
        } catch (IOException e) {
            transaction.rollback();
            throw e;
        }

        {
            FeatureReader<SimpleFeatureType, SimpleFeature> autoCommitReader;
            autoCommitReader = ds.getFeatureReader(newFeatureQuery, Transaction.AUTO_COMMIT);
            try {
                assertTrue(autoCommitReader.hasNext());
            } finally {
                autoCommitReader.close();
            }
        }

        // now keep using the transaction, it should still work
        transFs.removeFeatures(filterNewFeature);

        // no features removed yet outside the transaction
        {
            FeatureReader<SimpleFeatureType, SimpleFeature> autoCommitReader;
            autoCommitReader = ds.getFeatureReader(newFeatureQuery, Transaction.AUTO_COMMIT);
            try {
                assertTrue(autoCommitReader.hasNext());
            } finally {
                autoCommitReader.close();
            }
        }

        // but yes inside it
        {
            FeatureReader<SimpleFeatureType, SimpleFeature> transactionReader;
            transactionReader = ds.getFeatureReader(newFeatureQuery, transaction);
            try {
                assertFalse(transactionReader.hasNext());
            } finally {
                transactionReader.close();
            }
        }

        {
            FeatureReader<SimpleFeatureType, SimpleFeature> autoCommitReader;
            try {
                transaction.commit();
                autoCommitReader = ds.getFeatureReader(newFeatureQuery, Transaction.AUTO_COMMIT);
                assertFalse(autoCommitReader.hasNext());
            } finally {
                transaction.close();
            }
        }

    }

    public void testSetFeaturesAutoCommit() throws Exception {
        testData.insertTestData();
        final FeatureCollection<SimpleFeatureType, SimpleFeature> featuresToSet = testData
                .createTestFeatures(Point.class, 5);
        final DataStore ds = testData.getDataStore();
        final String typeName = testData.getTemp_table();

        final FeatureStore<SimpleFeatureType, SimpleFeature> store = (FeatureStore<SimpleFeatureType, SimpleFeature>) ds
                .getFeatureSource(typeName);

        final int initialCount = store.getCount(Query.ALL);
        assertTrue(initialCount > 0);
        assertTrue(initialCount != 5);

        store.setFeatures(DataUtilities.reader(featuresToSet));

        final int newCount = store.getCount(Query.ALL);
        assertEquals(5, newCount);
    }

    public void testSetFeaturesTransaction() throws Exception {
        testData.insertTestData();
        final FeatureCollection<SimpleFeatureType, SimpleFeature> featuresToSet = testData
                .createTestFeatures(Point.class, 5);
        final DataStore ds = testData.getDataStore();
        final String typeName = testData.getTemp_table();

        final Transaction transaction = new DefaultTransaction("testSetFeaturesTransaction handle");
        final FeatureStore<SimpleFeatureType, SimpleFeature> store = (FeatureStore<SimpleFeatureType, SimpleFeature>) ds
                .getFeatureSource(typeName);
        store.setTransaction(transaction);

        final int initialCount = store.getCount(Query.ALL);
        assertTrue(initialCount > 0);
        assertTrue(initialCount != 5);

        try {
            store.setFeatures(DataUtilities.reader(featuresToSet));
            final int countInsideTransaction = store.getCount(Query.ALL);
            assertEquals(5, countInsideTransaction);

            final FeatureSource<SimpleFeatureType, SimpleFeature> sourceNoTransaction = ds
                    .getFeatureSource(typeName);
            int countNoTransaction = sourceNoTransaction.getCount(Query.ALL);
            assertEquals(initialCount, countNoTransaction);

            // now commit
            transaction.commit();
            countNoTransaction = sourceNoTransaction.getCount(Query.ALL);
            assertEquals(5, countNoTransaction);
        } catch (Exception e) {
            transaction.rollback();
            throw e;
        } catch (AssertionFailedError e) {
            transaction.rollback();
            throw e;
        } finally {
            transaction.close();
        }
    }

    /**
     * Simultate an application where one thread works over a transaction adding
     * features while another thread accesses the same FeatureStore with a
     * query. Archetypical use case being a udig addFeatures command sends calls
     * addFeatures and the rendering thread does getFeatures.
     */
    public void testTransactionMultithreadAccess() throws Exception {
        testData.insertTestData();
        // start with an empty table
        final String typeName = testData.getTemp_table();
        final int featureCount = 2;
        final FeatureCollection<SimpleFeatureType, SimpleFeature> testFeatures = testData
                .createTestFeatures(LineString.class, featureCount);

        final DataStore ds = testData.getDataStore();
        final FeatureStore<SimpleFeatureType, SimpleFeature> fStore = (FeatureStore<SimpleFeatureType, SimpleFeature>) ds
                .getFeatureSource(typeName);
        final Transaction transaction = new DefaultTransaction("testTransactionMultithreadAccess");
        fStore.setTransaction(transaction);

        final boolean[] done = { false, false };
        final Exception[] errors = new Exception[2];

        Runnable worker1 = new Runnable() {
            public void run() {
                try {
                    System.err.println("adding..");
                    Set<String> addedFids = fStore.addFeatures(testFeatures);
                    System.err.println("got " + addedFids);
                    final FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);

                    final Set<FeatureId> fids = new HashSet<FeatureId>();
                    for (String fid : addedFids) {
                        fids.add(ff.featureId(fid));
                    }
                    final Id newFidsFilter = ff.id(fids);

                    System.err.println("querying..");
                    FeatureCollection<SimpleFeatureType, SimpleFeature> features = fStore
                            .getFeatures(newFidsFilter);
                    System.err.println("querying returned...");

                    int size = features.size();
                    System.err.println("Collection Size: " + size);
                    assertEquals(2, size);

                    System.err.println("commiting...");
                    transaction.commit();
                    System.err.println("commited.");

                    size = fStore.getCount(new DefaultQuery(typeName, newFidsFilter));
                    System.err.println("Size: " + size);
                    assertEquals(2, size);
                } catch (Exception e) {
                    errors[0] = e;
                    try {
                        System.err.println("rolling back!.");
                        transaction.rollback();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                } finally {
                    done[0] = true;
                    try {
                        System.err.println("closing transaction.");
                        transaction.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        Runnable worker2 = new Runnable() {
            public void run() {
                try {
                    System.err.println("worker2 calling getFeartures()");
                    FeatureCollection<SimpleFeatureType, SimpleFeature> collection = fStore
                            .getFeatures();
                    System.err.println("worker2 opening iterator...");
                    FeatureIterator<SimpleFeature> features = collection.features();
                    try {
                        System.err.println("worker2 iterating...");
                        while (features.hasNext()) {
                            SimpleFeature next = features.next();
                            System.out.println("**Got feature " + next.getID());
                        }
                        System.err.println("worker2 closing FeatureCollection");
                    } finally {
                        features.close();
                    }
                    System.err.println("worker2 done.");
                } catch (Exception e) {
                    errors[1] = e;
                } finally {
                    done[1] = true;
                }
            }
        };

        Thread thread1 = new Thread(worker1, "worker1");
        Thread thread2 = new Thread(worker2, "worker2");
        thread1.start();
        thread2.start();
        while (!(done[0] && done[1])) {
            Thread.sleep(100);
        }
        Exception worker1Error = errors[0];
        Exception worker2Error = errors[1];
        if (worker1Error != null || worker2Error != null) {
            String errMessg = "worker1: "
                    + (worker1Error == null ? "ok." : worker1Error.getMessage());
            errMessg += " -- worker2: "
                    + (worker2Error == null ? "ok." : worker2Error.getMessage());

            if (worker1Error != null) {
                worker1Error.printStackTrace();
            }
            if (worker2Error != null) {
                worker2Error.printStackTrace();
            }
            fail(errMessg);
        }
    }

    // this is a test over a legacy table, it doesn't work as the tabe is
    // versioned,
    // so I'm just commenting it out
    public void _testSdeEditTableAutoCommit() throws Exception {
        final ArcSDEDataStore dataStore = testData.getDataStore();

        String[] typeNames = dataStore.getTypeNames();
        SeConnection conn = dataStore.getConnectionPool().getConnection();
        try {
            for (String tname : typeNames) {
                final SeRegistration reg = new SeRegistration(conn, tname);
                final boolean multiVersion = reg.isMultiVersion();
                System.out.println(tname + " is versioned: " + multiVersion);
                if (multiVersion) {
                    //@todo: handle versioned sde table
                }
            }
        } finally {
            conn.close();
        }

        final FeatureStore<SimpleFeatureType, SimpleFeature> store = (FeatureStore<SimpleFeatureType, SimpleFeature>) dataStore
                .getFeatureSource("SDE.EDIT");
        final SimpleFeatureType schema = store.getSchema();

        SimpleFeature feature = SimpleFeatureBuilder.build(schema, (Object[]) null, (String) null);
        String wellKnownText = "MULTIPOLYGON (((366895.32237292314 611939.927599425, 387372.27837891673 610847.8321985407, 373713.3856197129 585537.3264777199, 366895.32237292314 611939.927599425)))";
        Geometry polygon = new WKTReader().read(wellKnownText);
        feature.setAttribute("SHAPE", polygon);
        feature.setAttribute("ID_BLOC", "_test_");
        FeatureCollection<SimpleFeatureType, SimpleFeature> collection = DataUtilities
                .collection(feature);

        store.addFeatures(collection);

    }

    /**
     * DOCUMENT ME!
     * 
     * @param args
     *            DOCUMENT ME!
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(ArcSDEFeatureStoreTest.class);
    }
}
