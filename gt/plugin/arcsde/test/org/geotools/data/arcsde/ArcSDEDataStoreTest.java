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
package org.geotools.data.arcsde;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import junit.framework.TestCase;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureResults;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.filter.AbstractFilter;
import org.geotools.filter.BBoxExpression;
import org.geotools.filter.Expression;
import org.geotools.filter.FidFilter;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFilter;
import org.geotools.filter.LogicFilter;
import org.geotools.gml.GMLFilterDocument;
import org.geotools.gml.GMLFilterGeometry;
import org.xml.sax.helpers.ParserAdapter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


/**
 * ArcSDEDAtaStore test cases
 *
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id: ArcSDEDataStoreTest.java,v 1.1 2004/03/11 00:36:41 groldan Exp $
 */
public class ArcSDEDataStoreTest extends TestCase {
    /** package logger */
    private static Logger LOGGER = Logger.getLogger(ArcSDEDataStoreTest.class.getPackage()
                                                                             .getName());

    /** DOCUMENT ME! */
    private TestData testData;

    /** an ArcSDEDataStore created on setUp() to run tests against */
    private DataStore store;

    /** a filter factory for testing */
    FilterFactory ff = FilterFactory.createFilterFactory();

    /**
     * Creates a new ArcSDEDataStoreTest object.
     */
    public ArcSDEDataStoreTest() {
        this("ArcSDE DataStore unit tests");
    }

    /**
     * Creates a new ArcSDEDataStoreTest object.
     *
     * @param name a name for the junit test
     */
    public ArcSDEDataStoreTest(String name) {
        super(name);
    }

    /**
     * loads /testData/testparams.properties into a Properties object, wich is
     * used to obtain test tables names and is used as parameter to find the
     * DataStore
     *
     * @throws Exception DOCUMENT ME!
     */
    protected void setUp() throws Exception {
        super.setUp();
        this.testData = new TestData();
        this.store = testData.getDataStore();
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    protected void tearDown() throws Exception {
        testData = null;
        super.tearDown();
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public void testFinder() throws IOException {
        DataStore sdeDs = null;

        sdeDs = DataStoreFinder.getDataStore(testData.getConProps());

        String failMsg = sdeDs + " is not an ArcSDEDataStore";
        assertTrue(failMsg, (sdeDs instanceof ArcSDEDataStore));
        LOGGER.info("testFinder OK :" + sdeDs.getClass().getName());
    }

    /**
     * tests that a connection to a live ArcSDE database can be established
     * with the parameters defined int testparams.properties, and a
     * ArcSDEConnectionPool can be properly setted up
     *
     * @throws IOException DOCUMENT ME!
     */
    public void testConnect() throws IOException {
        LOGGER.info("testing connection to the sde database");

        ConnectionPoolFactory pf = ConnectionPoolFactory.getInstance();
        ConnectionConfig congfig = null;

        congfig = new ConnectionConfig(testData.getConProps());

        try {
            ArcSDEConnectionPool pool = pf.createPool(congfig);
            LOGGER.info("connection succeed " + pool.getPoolSize()
                + " connections ready");
        } catch (DataSourceException ex) {
            throw ex;
        } finally {
            pf.clear(); //close and remove all pools
        }
    }

    /**
     * test that a ArcSDEDataStore that connects to de configured test database
     * contains the tables defined by the parameters "point_table",
     * "line_table" and "polygon_table", wether ot not they're defined as
     * single table names or as full qualified sde table names (i.e.
     * SDE.SDE.TEST_POINT)
     *
     * @throws IOException
     */
    public void testGetTypeNames() throws IOException {
        String[] featureTypes = store.getTypeNames();
        assertNotNull(featureTypes);

        for (int i = 0; i < featureTypes.length; i++)
            System.out.println(featureTypes[i]);

        testTypeExists(featureTypes, testData.getPoint_table());
        testTypeExists(featureTypes, testData.getLine_table());
        testTypeExists(featureTypes, testData.getPolygon_table());
    }

    /**
     * tests that the schema for the defined tests tables are returned.
     *
     * @throws IOException DOCUMENT ME!
     */
    public void testGetSchema() throws IOException {
        FeatureType schema;

        schema = store.getSchema(testData.getPoint_table());
        assertNotNull(schema);
        assertTrue(schema.getAttributeCount() > 0);
        schema = store.getSchema(testData.getLine_table());
        assertNotNull(schema);
        assertTrue(schema.getAttributeCount() > 0);
        schema = store.getSchema(testData.getPolygon_table());
        assertNotNull(schema);
        assertTrue(schema.getAttributeCount() > 0);
        LOGGER.info("testGetSchema OK: " + schema);
    }

    /**
     * This method tests the feature reader by opening various simultaneous
     * FeatureReaders using the 3 test tables.
     * 
     * <p>
     * I found experimentally that until 24 simultaneous streams can be opened
     * by a single connection. Each featurereader has an ArcSDE stream opened
     * until its <code>close()</code> method is called or hasNext() returns
     * flase, wich automatically closes the stream. If more than 24
     * simultaneous streams are tryied to be opened upon a single
     * SeConnection, an exception is thrown by de Java ArcSDE API saying that
     * a "NETWORK I/O OPERATION FAILED"
     * </p>
     *
     * @throws IOException DOCUMENT ME!
     * @throws IllegalAttributeException DOCUMENT ME!
     */
    public void testGetFeatureReader()
        throws IOException, IllegalAttributeException {
        final int NUM_READERS = Integer.parseInt(testData.getConProps()
                                                         .getProperty("pool.maxConnections"));
        String[] typeNames = {
                testData.getPoint_table(), testData.getLine_table(),
                testData.getPolygon_table()
            };
        FeatureReader[] readers = new FeatureReader[NUM_READERS];
        int[] counts = new int[NUM_READERS];

        for (int i = 0; i < NUM_READERS;) {
            for (int j = 0; (j < typeNames.length) && (i < NUM_READERS);
                    j++, i++) {
                readers[i] = getReader(typeNames[j]);
            }
        }

        Feature f;
        Geometry geom;
        int count1 = 0;
        int count2 = 0;
        int count3 = 0;
        long t = System.currentTimeMillis();
        boolean hasNext = false;

        while (true) {
            for (int i = 0; i < NUM_READERS; i++) {
                if (readers[i].hasNext()) {
                    hasNext = true;

                    break;
                }

                hasNext = false;
            }

            if (!hasNext) {
                for (int i = 0; i < NUM_READERS; i++)
                    readers[i].close();

                break;
            }

            for (int i = 0; i < NUM_READERS; i++) {
                if (testNext(readers[i])) {
                    ++counts[i];
                }
            }
        }

        t = System.currentTimeMillis() - t;

        String scounts = "";

        for (int i = 0; i < NUM_READERS; i++)
            scounts += (counts[i] + ", ");

        LOGGER.info("testGetFeatureReader: traversed " + scounts
            + " features simultaneously from " + NUM_READERS
            + " different FeatureReaders in " + t + "ms");
    }

    /**
     * DOCUMENT ME!
     *
     * @param r DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws IllegalAttributeException DOCUMENT ME!
     */
    private boolean testNext(FeatureReader r)
        throws IOException, IllegalAttributeException {
        if (r.hasNext()) {
            Feature f = r.next();
            assertNotNull(f);
            assertNotNull(f.getFeatureType());
            assertNotNull(f.getBounds());

            Geometry geom = f.getDefaultGeometry();
            assertNotNull(geom);

            return true;
        }

        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @param typeName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    private FeatureReader getReader(String typeName) throws IOException {
        Query q = new DefaultQuery(typeName, Filter.NONE);
        FeatureReader reader = store.getFeatureReader(q, Transaction.AUTO_COMMIT);
        FeatureType retType = reader.getFeatureType();
        assertNotNull(retType.getDefaultGeometry());
        assertTrue(retType.hasAttributeType("SHAPE"));
        assertTrue(reader.hasNext());

        return reader;
    }

    /**
     * tests the datastore behavior when fetching data based on mixed queries.
     * 
     * <p>
     * "Mixed queries" refers to mixing alphanumeric and geometry based
     * filters, since that is the natural separation of things in the Esri
     * Java API for ArcSDE. This is necessary since mixed queries sometimes
     * are problematic. So this test ensures that:
     * 
     * <ul>
     * <li>
     * A mixed query respects all filters
     * </li>
     * <li>
     * A mixed query does not fails when getBounds() is performed
     * </li>
     * <li>
     * A mixed query does not fails when getCount() is performed
     * </li>
     * </ul>
     * </p>
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testMixedQueries() throws Exception {
        final int EXPECTED_RESULT_COUNT = 3;
        FeatureSource fs = store.getFeatureSource(testData.getPolygon_table());
        Filter bboxFilter = getBBoxfilter(fs);
        String sqlFilterUri = getFilterUri("filters.sql.polygons.filter");
        Filter sqlFilter = parseDocument(sqlFilterUri);
        LOGGER.info("Geometry filter: " + bboxFilter);
        LOGGER.info("SQL filter: " + sqlFilter);

        FilterFactory ff = FilterFactory.createFilterFactory();
        LogicFilter mixedFilter = ff.createLogicFilter(sqlFilter,
                AbstractFilter.LOGIC_AND);
        mixedFilter.addFilter(bboxFilter);
        LOGGER.info("Mixed filter: " + mixedFilter);

        //verify both filter constraints are met
        testFilter(mixedFilter, fs, EXPECTED_RESULT_COUNT);

        final int LOOP_COUNT = 6;

        for (int i = 0; i < LOOP_COUNT; i++) {
            LOGGER.info("Running #" + i + " iteration for mixed query test");

            //check that getBounds and getCount do function
            try {
                FeatureResults results = fs.getFeatures(mixedFilter);
                Envelope bounds = results.getBounds();
                assertNotNull(bounds);
                LOGGER.info("results bounds: " + bounds);

                FeatureReader reader = results.reader();

                /*verify that then features are already being fetched, getBounds and
                 * getCount still work
                 */
                reader.next();
                bounds = results.getBounds();
                assertNotNull(bounds);
                LOGGER.info("results bounds when reading: " + bounds);

                int count = results.getCount();
                assertEquals(EXPECTED_RESULT_COUNT, count);
                LOGGER.info("wooohoooo...");
                reader.close();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "At iteration " + i, e);
                throw e;
            }
        }
    }

    /**
     * to expose GEOT-408, tests that queries in which only non spatial
     * attributes are requested does not fails due to the datastore trying to
     * parse the geometry attribute.
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testAttributeOnlyQuery() throws Exception {
        DataStore ds = testData.getDataStore();
        FeatureSource fSource = ds.getFeatureSource(testData.getLine_table());
        FeatureType type = fSource.getSchema();
        DefaultQuery attOnlyQuery = new DefaultQuery(type.getTypeName());
        List propNames = new ArrayList(type.getAttributeCount() - 1);

        for (int i = 0; i < type.getAttributeCount(); i++) {
            if (type.getAttributeType(i).isGeometry()) {
                continue;
            }

            propNames.add(type.getAttributeType(i).getName());
        }

        attOnlyQuery.setPropertyNames(propNames);

        FeatureResults results = fSource.getFeatures(attOnlyQuery);
        FeatureType resultSchema = results.getSchema();
        assertEquals(propNames.size(), resultSchema.getAttributeCount());

        for (int i = 0; i < propNames.size(); i++) {
            assertEquals(propNames.get(i),
                resultSchema.getAttributeType(i).getName());
        }

        //the problem described in GEOT-408 arises in attribute reader, so
        //we must to try fetching features
        results.reader().next();
    }

    /**
     * Test that FID filters are correctly handled
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testFidFilters() throws Exception {
        final DataStore ds = testData.getDataStore();
        final String typeName = testData.getPoint_table();

        //grab some fids
        FeatureReader reader = ds.getFeatureReader(new DefaultQuery(typeName),
                Transaction.AUTO_COMMIT);
        List fids = new ArrayList();

        while (reader.hasNext()) {
            fids.add(reader.next().getID());

            //skip one
            if (reader.hasNext()) {
                reader.next();
            }
        }

        reader.close();

        FidFilter filter = FilterFactory.createFilterFactory().createFidFilter();
        filter.addAllFids(fids);

        FeatureSource source = ds.getFeatureSource(typeName);
        Query query = new DefaultQuery(typeName, filter);
        FeatureResults results = source.getFeatures(query);

        assertEquals(fids.size(), results.getCount());
        reader = results.reader();

        while (reader.hasNext()) {
            String fid = reader.next().getID();
            assertTrue("a fid not included in query was returned: " + fid,
                fids.contains(fid));
        }
    }

    /**
     * Test that all supported geometry filters are working properly
     *
     * @throws UnsupportedOperationException DOCUMENT ME!
     */
    public void testGeometryFilters() {
        throw new UnsupportedOperationException("Don't forget to implement");
    }

    /**
     * test that getFeatureSource over the point_table table works
     *
     * @throws IOException DOCUMENT ME!
     */
    public void testGetFeatureSourcePoint() throws IOException {
        testGetFeatureSource(store.getFeatureSource(testData.getPoint_table()));
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public void testGetFeatureSourceLine() throws IOException {
        testGetFeatureSource(store.getFeatureSource(testData.getLine_table()));
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public void testGetFeatureSourcePoly() throws IOException {
        testGetFeatureSource(store.getFeatureSource(testData.getPolygon_table()));
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public void testGetFeaturesPoint() throws IOException {
        testGetFeatures("points", testData.getPoint_table());
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public void testGetFeaturesLine() throws IOException {
        testGetFeatures("lines", testData.getLine_table());
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public void testGetFeaturesPolygon() throws IOException {
        testGetFeatures("polygons", testData.getPolygon_table());
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testSQLFilterPoints() throws Exception {
        String uri = getFilterUri("filters.sql.points.filter");
        int expected = getExpectedCount("filters.sql.points.expectedCount");
        testFilter(uri, testData.getPoint_table(), expected);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testSQLFilterLines() throws Exception {
        String uri = getFilterUri("filters.sql.lines.filter");
        int expected = getExpectedCount("filters.sql.lines.expectedCount");
        testFilter(uri, testData.getLine_table(), expected);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testSQLFilterPolygons() throws Exception {
        String uri = getFilterUri("filters.sql.polygons.filter");
        int expected = getExpectedCount("filters.sql.polygons.expectedCount");
        testFilter(uri, testData.getPolygon_table(), expected);
    }

    /**
     * DOCUMENT ME!
     */
    public void testBBoxFilterPoints() {
        //String uri = getFilterUri("filters.bbox.points.filter");
        //int expected = getExpectedCount("filters.bbox.points.expectedCount");
        int expected = 6;
        testBBox(testData.getPoint_table(), expected);
    }

    /**
     * DOCUMENT ME!
     */
    public void testBBoxFilterLines() {
        //String uri = getFilterUri("filters.bbox.lines.filter");
        //int expected = getExpectedCount("filters.bbox.lines.expectedCount");
        int expected = 22;
        testBBox(testData.getLine_table(), expected);
    }

    /**
     * DOCUMENT ME!
     */
    public void testBBoxFilterPolygons() {
        //String uri = getFilterUri("filters.bbox.polygons.filter");
        //int expected = getExpectedCount("filters.bbox.polygons.expectedCount");
        int expected = 8;
        testBBox(testData.getPolygon_table(), expected);
    }

    /**
     * DOCUMENT ME!
     */
    public void testGeometryIntersectsFilters() {
    }

    /////////////////// HELPER FUNCTIONS ////////////////////////

    /**
     * for a given FeatureSource, makes the following assertions:
     * 
     * <ul>
     * <li>
     * it's not null
     * </li>
     * <li>
     * .getDataStore() != null
     * </li>
     * <li>
     * .getDataStore() == the datastore obtained in setUp()
     * </li>
     * <li>
     * .getSchema() != null
     * </li>
     * <li>
     * .getBounds() != null
     * </li>
     * <li>
     * .getBounds().isNull() == false
     * </li>
     * <li>
     * .getFeatures().getCounr() > 0
     * </li>
     * <li>
     * .getFeatures().reader().hasNex() == true
     * </li>
     * <li>
     * .getFeatures().reader().next() != null
     * </li>
     * </ul>
     * 
     *
     * @param fsource DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    private void testGetFeatureSource(FeatureSource fsource)
        throws IOException {
        assertNotNull(fsource);
        assertNotNull(fsource.getDataStore());
        assertEquals(fsource.getDataStore(), store);
        assertNotNull(fsource.getSchema());

        FeatureResults results = fsource.getFeatures();
        int count = results.getCount();
        assertTrue(count > 0);
        LOGGER.info("feature count: " + count);

        Envelope env1;
        Envelope env2;
        env1 = fsource.getBounds();
        assertNotNull(env1);
        assertFalse(env1.isNull());
        env2 = fsource.getBounds(Query.ALL);
        assertNotNull(env2);
        assertFalse(env2.isNull());
        env1 = results.getBounds();
        assertNotNull(env1);
        assertFalse(env1.isNull());

        FeatureReader reader = results.reader();
        assertTrue(reader.hasNext());

        try {
            assertNotNull(reader.next());
        } catch (NoSuchElementException ex) {
            ex.printStackTrace();
            fail(ex.getMessage());
        } catch (IllegalAttributeException ex) {
            ex.printStackTrace();
            fail(ex.getMessage());
        }

        reader.close();
    }

    /**
     * DOCUMENT ME!
     *
     * @param filterKey DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private String getFilterUri(String filterKey) {
        String filterFileName = testData.getConProps().getProperty(filterKey);

        if (filterFileName == null) {
            super.fail(filterKey
                + " param not found in tests configurarion properties file");
        }

        String uri = testData.getDataFolder() + filterFileName;

        return uri;
    }

    /**
     * DOCUMENT ME!
     *
     * @param key DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private int getExpectedCount(String key) {
        try {
            return Integer.parseInt(testData.getConProps().getProperty(key));
        } catch (NumberFormatException ex) {
            super.fail(key
                + " parameter not found or not an integer in testParams.properties");
        }

        return -1;
    }

    /**
     * DOCUMENT ME!
     *
     * @param filterUri DOCUMENT ME!
     * @param table DOCUMENT ME!
     * @param expected DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    private void testFilter(String filterUri, String table, int expected)
        throws Exception {
        Filter filter = parseDocument(filterUri);
        FeatureSource fsource = store.getFeatureSource(table);
        testFilter(filter, fsource, expected);
    }

    /**
     * DOCUMENT ME!
     *
     * @param filter DOCUMENT ME!
     * @param fsource DOCUMENT ME!
     * @param expected DOCUMENT ME!
     */
    private void testFilter(Filter filter, FeatureSource fsource, int expected) {
        try {
            FeatureResults results = fsource.getFeatures(filter);
            FeatureCollection fc = results.collection();
            int resCount = results.getCount();
            int fCount = fc.size();
            LOGGER.info("results count: " + resCount + " collection size: "
                + fCount);

            Feature f = fc.features().next();
            LOGGER.info("first feature is: " + f.getID());

            String failMsg = "Expected and returned result count does not match";
            assertEquals(failMsg, expected, fCount);
            assertEquals(failMsg, expected, resCount);
        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param table DOCUMENT ME!
     * @param expected DOCUMENT ME!
     */
    private void testBBox(String table, int expected) {
        try {
            FeatureSource fs = store.getFeatureSource(table);
            Filter bboxFilter = getBBoxfilter(fs);
            testFilter(bboxFilter, fs, expected);
        } catch (Exception ex) {
            ex.printStackTrace();
            super.fail(ex.getMessage());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param fs DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    private Filter getBBoxfilter(FeatureSource fs) throws Exception {
        Envelope env = new Envelope(-60, -40, -55, -20);
        BBoxExpression bbe = ff.createBBoxExpression(env);
        org.geotools.filter.GeometryFilter gf = ff.createGeometryFilter(AbstractFilter.GEOMETRY_BBOX);
        FeatureType schema = fs.getSchema();
        Expression attExp = ff.createAttributeExpression(schema,
                schema.getDefaultGeometry().getName());
        gf.addLeftGeometry(attExp);
        gf.addRightGeometry(bbe);

        return gf;
    }

    /**
     * checks for the existence of <code>table</code> in
     * <code>featureTypes</code>. <code>table</code> must be a full qualified
     * sde feature type name. (i.e "TEST_POINT" == "SDE.SDE.TEST_POINT")
     *
     * @param featureTypes DOCUMENT ME!
     * @param table DOCUMENT ME!
     */
    private void testTypeExists(String[] featureTypes, String table) {
        for (int i = 0; i < featureTypes.length; i++) {
            if (featureTypes[i].equalsIgnoreCase(table.toUpperCase())) {
                LOGGER.info("testTypeExists OK: " + table);

                return;
            }
        }

        fail("table " + table + " not found in getFeatureTypes results");
    }

    /**
     * DOCUMENT ME!
     *
     * @param wich DOCUMENT ME!
     * @param table DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    private void testGetFeatures(String wich, String table)
        throws IOException {
        LOGGER.info("getting all features from " + table);

        FeatureSource source = store.getFeatureSource(table);
        int expectedCount = getExpectedCount("getfeatures." + wich
                + ".expectedCount");
        int fCount = source.getCount(Query.ALL);
        String failMsg = "Expected and returned result count does not match";
        assertEquals(failMsg, expectedCount, fCount);

        FeatureResults fresults = source.getFeatures();
        FeatureCollection features = fresults.collection();
        failMsg = "FeatureResults.getCount and .collection().size thoes not match";
        assertEquals(failMsg, fCount, features.size());
        LOGGER.info("fetched " + fCount + " features for " + wich
            + " layer, OK");
    }

    /**
     * stolen from filter module tests
     *
     * @param uri DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    private Filter parseDocument(String uri) throws Exception {
        LOGGER.finest("about to create parser");

        // chains all the appropriate filters together (in correct order)
        //  and initiates parsing
        TestFilterHandler filterHandler = new TestFilterHandler();
        FilterFilter filterFilter = new FilterFilter(filterHandler, null);
        GMLFilterGeometry geometryFilter = new GMLFilterGeometry(filterFilter);
        GMLFilterDocument documentFilter = new GMLFilterDocument(geometryFilter);
        SAXParserFactory fac = SAXParserFactory.newInstance();
        SAXParser parser = fac.newSAXParser();
        ParserAdapter p = new ParserAdapter(parser.getParser());
        p.setContentHandler(documentFilter);
        LOGGER.fine("just made parser, " + uri);
        p.parse(uri);
        LOGGER.finest("just parsed: " + uri);

        Filter filter = filterHandler.getFilter();

        return filter;
    }
}
