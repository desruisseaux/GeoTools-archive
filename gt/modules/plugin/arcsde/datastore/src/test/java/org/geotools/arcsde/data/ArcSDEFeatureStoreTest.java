package org.geotools.arcsde.data;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
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
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.filter.text.cql2.CQL;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.identity.FeatureId;

import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeQuery;
import com.esri.sde.sdk.client.SeRow;
import com.esri.sde.sdk.client.SeSqlConstruct;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Unit tests for transaction support
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/plugin/arcsde/datastore/src/test/java/org/geotools/arcsde/data/ArcSDEFeatureStoreTest.java $
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
            FeatureReader reader = ds.getFeatureReader(query, Transaction.AUTO_COMMIT);
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
            FeatureWriter writer = ds
                    .getFeatureWriter(typeName, fidFilter, Transaction.AUTO_COMMIT);

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
            FeatureReader reader = ds.getFeatureReader(query, Transaction.AUTO_COMMIT);
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
        FeatureWriter writer = ds.getFeatureWriter(typeName, or, Transaction.AUTO_COMMIT);

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
        FeatureReader read = ds.getFeatureReader(new DefaultQuery(typeName, or),
                Transaction.AUTO_COMMIT);
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

        testInsertAutoCommit(Geometry.class);
        testInsertAutoCommit(Point.class);
        testInsertAutoCommit(MultiPoint.class);
        testInsertAutoCommit(LineString.class);
        testInsertAutoCommit(MultiLineString.class);
        testInsertAutoCommit(Polygon.class);
        testInsertAutoCommit(MultiPolygon.class);
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
        final FeatureCollection testFeatures = testData.createTestFeatures(LineString.class,
                featureCount);

        final DataStore ds = testData.getDataStore();

        final SimpleFeatureType ftype = testFeatures.getSchema();
        final FeatureIterator iterator = testFeatures.features();

        final Transaction transaction = new DefaultTransaction();
        final FeatureWriter writer = ds.getFeatureWriter(typeName, transaction);

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

        FeatureReader reader = ds.getFeatureReader(new DefaultQuery(typeName),
                Transaction.AUTO_COMMIT);
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
    public void testUpdateAutoCommit() throws Exception {
        testData.insertTestData();

        final String typeName = testData.getTemp_table();
        final DataStore ds = testData.getDataStore();
        final Filter filter = CQL.toFilter("INT32_COL = 3");

        FeatureWriter writer = ds.getFeatureWriter(typeName, filter, Transaction.AUTO_COMMIT);

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
        FeatureReader reader = ds.getFeatureReader(query, Transaction.AUTO_COMMIT);
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
        final int insertCount = 5;
        final FeatureCollection testFeatures = testData.createTestFeatures(geometryClass,
                insertCount);

        final DataStore ds = testData.getDataStore();
        final FeatureSource fsource = ds.getFeatureSource(typeName);

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

        FeatureWriter writer = ds.getFeatureWriterAppend(typeName, Transaction.AUTO_COMMIT);

        SimpleFeature source;
        SimpleFeature dest;

        try {
            for (FeatureIterator fi = testFeatures.features(); fi.hasNext();) {
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

        try {
            FeatureWriter writer = ds.getFeatureWriter(typeName, Transaction.AUTO_COMMIT);
            SimpleFeature f = writer.next();
            f.setAttribute("INT32_COL", Integer.valueOf(1000));

            writer.write();
            writer.close();
            LOGGER.info("Wrote null-geom feature to sde");

            FeatureReader r = ds.getFeatureReader(new DefaultQuery(typeName, Filter.INCLUDE),
                    Transaction.AUTO_COMMIT);
            assertTrue(r.hasNext());
            f = r.next();
            LOGGER.info("recovered geometry " + f.getDefaultGeometry()
                    + " from single inserted feature.");
            assertNull(f.getDefaultGeometry());
            // save the ID to update the feature later
            String newId = f.getID();
            assertFalse(r.hasNext());
            r.close();
            LOGGER.info("Confirmed exactly one feature in new sde layer");

            FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);
            HashSet ids = new HashSet();
            ids.add(ff.featureId(newId));
            Filter idFilter = ff.id(ids);

            writer = ds.getFeatureWriter(typeName, idFilter, Transaction.AUTO_COMMIT);

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
            writer.close();

            LOGGER.info("Null-geom feature updated with a sample geometry.");

            DefaultQuery query = new DefaultQuery(testData.getTemp_table(), idFilter);
            r = ds.getFeatureReader(query, Transaction.AUTO_COMMIT);
            assertTrue(r.hasNext());
            f = r.next();
            MultiLineString recoveredMLS = (MultiLineString) f.getDefaultGeometry();
            assertTrue(!recoveredMLS.isEmpty());
            // I tried to compare the recovered MLS to the
            // sampleMultiLineString, but they're
            // slightly different. SDE does some rounding, and winds up giving
            // me 0.0000002 for zero,
            // and 11.9992 for 12. Meh.
            r.close();

        } catch (Exception e) {
            throw new RuntimeException(e);
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

        String typeName = testData.getTemp_table();

        DataStore ds = testData.getDataStore();
        FeatureSource fsource = ds.getFeatureSource(typeName);

        final int initialCount = fsource.getCount(Query.ALL);
        final int writeCount = initialCount + 2;
        FeatureCollection features = testData.createTestFeatures(LineString.class, writeCount);

        // incremented on each feature added event to
        // ensure events are being raised as expected
        // (the count is wraped inside an array to be able of declaring
        // the variable as final and accessing it from inside the anonymous
        // inner class)
        // final int[] featureAddedEventCount = { 0 };

        Transaction transaction = new DefaultTransaction();
        FeatureWriter writer = ds.getFeatureWriter(typeName, Filter.INCLUDE, transaction);

        SimpleFeature source;
        SimpleFeature dest;

        int count = 0;
        for (FeatureIterator fi = features.features(); fi.hasNext(); count++) {
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
        writer.close();

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

        String typeName = testData.getTemp_table();
        FeatureCollection features = testData.createTestFeatures(LineString.class, 2);

        DataStore ds = testData.getDataStore();
        FeatureSource fsource = ds.getFeatureSource(typeName);

        final int initialCount = fsource.getCount(Query.ALL);

        FeatureWriter writer = ds.getFeatureWriterAppend(typeName, Transaction.AUTO_COMMIT);

        SimpleFeature source;
        SimpleFeature dest;

        for (FeatureIterator fi = features.features(); fi.hasNext();) {
            assertFalse(writer.hasNext());
            source = fi.next();
            dest = writer.next();
            dest.setAttributes(source.getAttributes());
            writer.write();
        }

        writer.close();

        // were the features really inserted?
        int fcount = fsource.getCount(Query.ALL);
        assertEquals(features.size() + initialCount, fcount);
    }

    /**
     * Ensure modified features for a given FeatureStore are returned by
     * subsequent queries even if the transaction has not beeb commited.
     * 
     * @throws Exception
     */
    public void testTransactionStateDiff() throws Exception {
        testData.insertTestData();

        final DataStore ds = testData.getDataStore();
        final String typeName = testData.getTemp_table();
        final FeatureStore transFs = (FeatureStore) ds.getFeatureSource(typeName);
        final SimpleFeatureType schema = transFs.getSchema();

        Transaction transaction = new DefaultTransaction("test_handle");
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
        Filter filterNewFeature = CQL.toFilter("INT32_COL = 1000");
        FeatureCollection features = transFs.getFeatures(filterNewFeature);
        int size = features.size();
        assertEquals(1, size);

        // ok transaction respected, assert the feature does not exist outside
        // it
        DefaultQuery query = new DefaultQuery(typeName, filterNewFeature);
        FeatureReader reader = ds.getFeatureReader(query, Transaction.AUTO_COMMIT);
        assertFalse(reader.hasNext());
        reader.close();

        // ok, but what if we ask for a feature reader with the same transaction
        reader = ds.getFeatureReader(query, transaction);
        assertTrue(reader.hasNext());
        reader.next();
        assertFalse(reader.hasNext());
        reader.close();

        // now commit, and Transaction.AUTO_COMMIT should carry it over
        try {
            transaction.commit();
        } catch (IOException e) {
            transaction.rollback();
        } finally {
            transaction.close();
        }

        reader = ds.getFeatureReader(query, Transaction.AUTO_COMMIT);
        assertTrue(reader.hasNext());
        reader.close();

    }

    public void testSetFeaturesAutoCommit() throws Exception {
        testData.insertTestData();
        final FeatureCollection featuresToSet = testData.createTestFeatures(Point.class, 5);
        final DataStore ds = testData.getDataStore();
        final String typeName = testData.getTemp_table();

        final FeatureStore store = (FeatureStore) ds.getFeatureSource(typeName);

        final int initialCount = store.getCount(Query.ALL);
        assertTrue(initialCount > 0);
        assertTrue(initialCount != 5);

        store.setFeatures(DataUtilities.reader(featuresToSet));

        final int newCount = store.getCount(Query.ALL);
        assertEquals(5, newCount);
    }

    public void testSetFeaturesTransaction() throws Exception {
        testData.insertTestData();
        final FeatureCollection featuresToSet = testData.createTestFeatures(Point.class, 5);
        final DataStore ds = testData.getDataStore();
        final String typeName = testData.getTemp_table();

        final Transaction transaction = new DefaultTransaction("testSetFeaturesTransaction handle");
        final FeatureStore store = (FeatureStore) ds.getFeatureSource(typeName);
        store.setTransaction(transaction);

        final int initialCount = store.getCount(Query.ALL);
        assertTrue(initialCount > 0);
        assertTrue(initialCount != 5);

        try {
            store.setFeatures(DataUtilities.reader(featuresToSet));
            final int countInsideTransaction = store.getCount(Query.ALL);
            assertEquals(5, countInsideTransaction);
            
            final FeatureSource sourceNoTransaction = ds.getFeatureSource(typeName);
            int countNoTransaction = sourceNoTransaction.getCount(Query.ALL);
            assertEquals(initialCount, countNoTransaction);
            
            //now commit
            transaction.commit();
            countNoTransaction = sourceNoTransaction.getCount(Query.ALL);
            assertEquals(5, countNoTransaction);
        }catch(Exception e){
            transaction.rollback();
            throw e;
        }catch(AssertionFailedError e){
            transaction.rollback();
            throw e;
        } finally {
            transaction.close();
        }
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
