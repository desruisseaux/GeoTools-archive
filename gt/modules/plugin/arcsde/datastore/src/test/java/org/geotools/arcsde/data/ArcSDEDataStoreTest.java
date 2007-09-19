/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, Geotools Project Managment Committee (PMC)
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
package org.geotools.arcsde.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.geotools.arcsde.ArcSDEDataStoreFactory;
import org.geotools.arcsde.pool.ArcSDEConnectionPool;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureType;
import org.geotools.feature.GeometryAttributeType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.filter.FilterFilter;
import org.geotools.gml.GMLFilterDocument;
import org.geotools.gml.GMLFilterGeometry;
import org.opengis.filter.And;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.Id;
import org.opengis.filter.identity.FeatureId;
import org.opengis.filter.spatial.BBOX;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.xml.sax.helpers.ParserAdapter;

import com.esri.sde.sdk.pe.PeFactory;
import com.esri.sde.sdk.pe.PeProjectedCS;
import com.esri.sde.sdk.pe.PeProjectionException;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;


/**
 * ArcSDEDAtaStore test cases
 *
 * @author Gabriel Roldan, Axios Engineering
 * @source $URL$
 * @version $Id$
 */
public class ArcSDEDataStoreTest extends TestCase {
    /** package logger */
    private static Logger LOGGER = Logger.getLogger(ArcSDEDataStoreTest.class.getPackage()
                                                                             .getName());

    /** DOCUMENT ME! */
    private static TestData testData;

    /** an ArcSDEDataStore created on setUp() to run tests against */
    private DataStore store;

    /** a filter factory for testing */
    FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);

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
     * Builds a test suite for all this class' tests with per suite
     * initialization directed to {@link #oneTimeSetUp()} and per suite clean up
     * directed to {@link #oneTimeTearDown()}
     * 
     * @return
     */
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(ArcSDEDataStoreTest.class);

        TestSetup wrapper = new TestSetup(suite) {
            protected void setUp() throws IOException {
                oneTimeSetUp();
            }

            protected void tearDown() {
                oneTimeTearDown();
            }
        };
        return wrapper;
    }

    private static void oneTimeSetUp() throws IOException {
        testData = new TestData();
        testData.setUp();
        if (ArcSDEDataStoreFactory.JSDE_CLIENT_VERSION == ArcSDEDataStoreFactory.JSDE_VERSION_DUMMY)
            throw new RuntimeException("Don't run the test-suite with the dummy jar.  Make sure the real ArcSDE jars are on your classpath.");
    }

    private static void oneTimeTearDown() {
        boolean cleanTestTable = false;
        boolean cleanPool = true;
        testData.tearDown(cleanTestTable, cleanPool);
    }

    /**
     * loads {@code testData/testparams.properties} into a Properties object, wich is
     * used to obtain test tables names and is used as parameter to find the DataStore
     *
     * @throws Exception DOCUMENT ME!
     */
    protected void setUp() throws Exception {
        super.setUp();
        //facilitates running a single test at a time (eclipse lets you do this and it's very useful)
        if (testData == null) {
            oneTimeSetUp();
        }
        this.store = testData.getDataStore();
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        this.store = null;
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public void testFinder() throws IOException {
        DataStore sdeDs = null;

        DataStoreFinder.scanForPlugins();
        sdeDs = DataStoreFinder.getDataStore(testData.getConProps());
        assertNotNull(sdeDs);
        String failMsg = sdeDs + " is not an ArcSDEDataStore";
        assertTrue(failMsg, (sdeDs instanceof ArcSDEDataStore));
        LOGGER.fine("testFinder OK :" + sdeDs.getClass().getName());
    }
    
    /**
     * This test is currently broken.  It's a placeholder for some logic
     * that sfarber wrote which tries to guess the SRS of a featureclass, based on connecting
     * to it via an SeLayer.
     * 
     * @throws Throwable
     */
    public void _testAutoFillSRS() throws Throwable {
        
        ArcSDEDataStore ds = testData.getDataStore();
        CoordinateReferenceSystem sdeCRS = ds.getSchema("GISDATA.TOWNS_POLY").getDefaultGeometry().getCoordinateSystem();
        
        LOGGER.info(sdeCRS.toWKT().replaceAll(" ","").replaceAll("\n", "").replaceAll("\"", "\\\""));
        
        //CoordinateReferenceSystem epsgCRS = CRS.decode("EPSG:26986");
        
        //LOGGER.info("are these two CRS's equal? " + CRS.equalsIgnoreMetadata(sdeCRS, epsgCRS));
        
        
        
        if (1==1) return;
        
        int epsgCode = -1;
        int[] projcs = PeFactory.projcsCodelist();
        LOGGER.info(projcs.length + " projections available.");
        for (int i = 0; i < projcs.length; i++) {
            try {
                PeProjectedCS candidate = PeFactory.projcs(projcs[i]);
                //in ArcSDE 9.2, if the PeFactory doesn't support a projection it claimed
                //to support, it returns 'null'.  So check for it.
                if (candidate != null && candidate.getName().indexOf("Massachusetts") != -1) {
                    //LOGGER.info("\n\n" + projcs[i] + " has name " + candidate.getName() + "\ntried to match " + wktName + "\n\n");
                    epsgCode = projcs[i];
                } else if (candidate == null) { 
                    //LOGGER.info(projcs[i] + " was null");
                } else if (candidate != null) {
                    //LOGGER.info(projcs[i] + " wasn't null");
                }
            } catch (PeProjectionException pe) {
                // Strangely SDE includes codes in the projcsCodeList() that
                // it doesn't actually support.
                // Catch the exception and skip them here.
            }
        }
        
        
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public void _testStress() throws Exception {
        try {
            ArcSDEDataStore ds = testData.getDataStore();
            
            ArcSDEConnectionPool pool = ds.getConnectionPool();
            final int initialAvailableCount = pool.getAvailableCount();
            final int initialPoolSize = pool.getPoolSize();
            
            String typeName = testData.getPoint_table();

            FeatureSource source = ds.getFeatureSource(typeName);
            
            assertEquals(initialAvailableCount, pool.getAvailableCount());
            assertEquals(initialPoolSize, pool.getPoolSize());

            FeatureType schema = source.getSchema();

            assertEquals("After getSchema()", initialAvailableCount, pool.getAvailableCount());
            assertEquals("After getSchema()", initialPoolSize, pool.getPoolSize());
            
            final Envelope layerBounds = source.getBounds();
            
            assertEquals("After getBounds()", initialAvailableCount, pool.getAvailableCount());
            assertEquals("After getBounds()", initialPoolSize, pool.getPoolSize());

            source.getCount(Query.ALL);

            assertEquals("After size()", initialAvailableCount, pool.getAvailableCount());
            assertEquals("After size()", initialPoolSize, pool.getPoolSize());
            
            
            BBOX bbox = ff.bbox(schema.getDefaultGeometry().getLocalName(),
                    layerBounds.getMinX() + 10,
                    layerBounds.getMinY() + 10,
                    layerBounds.getMaxX() - 10,
                    layerBounds.getMaxY() - 10,
                    schema.getDefaultGeometry().getCoordinateSystem().getName().getCode());
            
            for(int i = 0; i < 20; i++){
            	LOGGER.fine("Running iteration #" + i);
            	
                FeatureCollection res = source.getFeatures(bbox);
            	FeatureIterator reader = res.features();

            	assertNotNull(reader.next());

            	assertTrue(0 < res.size());
            	assertNotNull(res.getBounds());
            	
            	assertNotNull(reader.next());
            	
            	assertTrue(0 < res.size());
            	assertNotNull(res.getBounds());

            	assertNotNull(reader.next());
            	
            	reader.close();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
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

        if (LOGGER.isLoggable(Level.FINE)) {
            for (int i = 0; i < featureTypes.length; i++)
                System.out.println(featureTypes[i]);
        }
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
        LOGGER.fine("testGetSchema OK: " + schema);
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

        LOGGER.fine("testGetFeatureReader: traversed " + scounts
            + " features simultaneously from " + NUM_READERS
            + " different FeatureReaders in " + t + "ms");
    }

    /**
     * Checks that a query returns only the specified attributes.
     *
     * @throws IOException
     * @throws IllegalAttributeException
     */
    public void testRestrictsAttributes()
        throws IOException, IllegalAttributeException {
        final String typeName = testData.getPoint_table();
        final DataStore ds = testData.getDataStore();
        final FeatureType schema = ds.getSchema(typeName);
        final int queriedAttributeCount = schema.getAttributeCount() - 3;
        final String[] queryAtts = new String[queriedAttributeCount];

        for (int i = 0; i < queryAtts.length; i++) {
            queryAtts[i] = schema.getAttributeType(i).getLocalName();
        }

        //build the query asking for a subset of attributes
        final Query query = new DefaultQuery(typeName, Filter.INCLUDE, queryAtts);

        FeatureReader reader = null;
        FeatureType resultSchema;
        try {
            reader = ds.getFeatureReader(query, Transaction.AUTO_COMMIT);
            resultSchema = reader.getFeatureType();
        } finally {
            reader.close();
        }
        // it's conceivable that we didn't add the FID attribute, so be a little lenient.
        // Either the result is exactly equal, or one greater
        assertTrue(queriedAttributeCount == resultSchema.getAttributeCount() || queriedAttributeCount == resultSchema.getAttributeCount() - 1);
        //assertEquals(queriedAttributeCount, resultSchema.getAttributeCount());

        for (int i = 0; i < queriedAttributeCount; i++) {
            assertEquals(queryAtts[i],
                resultSchema.getAttributeType(i).getLocalName());
        }
    }

    /**
     * Checks that arcsde datastore returns featuretypes whose attributes are
     * exactly in the requested order.
     *
     * @throws IOException DOCUMENT ME!
     * @throws IllegalAttributeException DOCUMENT ME!
     */
    public void testRespectsAttributeOrder()
        throws IOException, IllegalAttributeException {
        final String typeName = testData.getPoint_table();
        final DataStore ds = testData.getDataStore();
        final FeatureType schema = ds.getSchema(typeName);
        final int queriedAttributeCount = schema.getAttributeCount();
        final String[] queryAtts = new String[queriedAttributeCount];

        //build the attnames in inverse order
        for (int i = queryAtts.length, j = 0; i > 0; j++) {
            --i;
            queryAtts[j] = schema.getAttributeType(i).getLocalName();
        }

        //build the query asking for a subset of attributes
        final Query query = new DefaultQuery(typeName, Filter.INCLUDE, queryAtts);

        FeatureReader reader;
        reader = ds.getFeatureReader(query, Transaction.AUTO_COMMIT);
        try {

            FeatureType resultSchema = reader.getFeatureType();
            assertEquals(queriedAttributeCount, resultSchema.getAttributeCount());

            for (int i = 0; i < queriedAttributeCount; i++) {
                assertEquals(queryAtts[i], resultSchema.getAttributeType(i).getLocalName());
            }
        } finally {
            reader.close();
        }
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
        Query q = new DefaultQuery(typeName, Filter.INCLUDE);
        FeatureReader reader = store.getFeatureReader(q, Transaction.AUTO_COMMIT);
        FeatureType retType = reader.getFeatureType();
        assertNotNull(retType.getDefaultGeometry());
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
     * A mixed query does not fails when size() is performed
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
        LOGGER.fine("Geometry filter: " + bboxFilter);
        LOGGER.fine("SQL filter: " + sqlFilter);

        And mixedFilter = ff.and( sqlFilter, bboxFilter );
        
        LOGGER.fine("Mixed filter: " + mixedFilter);

        //verify both filter constraints are met
        testFilter(mixedFilter, fs, EXPECTED_RESULT_COUNT);

        final int LOOP_COUNT = 6;

        for (int i = 0; i < LOOP_COUNT; i++) {
            LOGGER.info("Running #" + i + " iteration for mixed query test");

            // check that getBounds and size do function
            FeatureIterator reader = null;
            FeatureCollection results = fs.getFeatures(mixedFilter);
            Envelope bounds = results.getBounds();
            assertNotNull(bounds);
            LOGGER.fine("results bounds: " + bounds);

            reader = results.features();
            try {
                /*
                 * verify that when features are already being fetched,
                 * getBounds and size still work
                 */
                reader.next();
                bounds = results.getBounds();
                assertNotNull(bounds);
                LOGGER.fine("results bounds when reading: " + bounds);

                int count = results.size();
                assertEquals(EXPECTED_RESULT_COUNT, count);
                LOGGER.fine("wooohoooo...");

            } finally {
                reader.close();
            }
        }
    }

    /**
     * to expose GEOT-408, tests that queries in which only non spatial
     * attributes are requested does not fails due to the datastore trying to
     * parse the geometry attribute.
     * 
     * @throws Exception
     *             DOCUMENT ME!
     */
    public void testAttributeOnlyQuery() throws Exception {
        DataStore ds = testData.getDataStore();
        FeatureSource fSource = ds.getFeatureSource(testData.getLine_table());
        FeatureType type = fSource.getSchema();
        DefaultQuery attOnlyQuery = new DefaultQuery(type.getTypeName());
        List propNames = new ArrayList(type.getAttributeCount() - 1);

        for (int i = 0; i < type.getAttributeCount(); i++) {
            if (type.getAttributeType(i) instanceof GeometryAttributeType) {
                continue;
            }

            propNames.add(type.getAttributeType(i).getLocalName());
        }

        attOnlyQuery.setPropertyNames(propNames);

        FeatureCollection results = fSource.getFeatures(attOnlyQuery);
        FeatureType resultSchema = results.getSchema();
        assertEquals(propNames.size(), resultSchema.getAttributeCount());

        for (int i = 0; i < propNames.size(); i++) {
            assertEquals(propNames.get(i),
                resultSchema.getAttributeType(i).getLocalName());
        }

        //the problem described in GEOT-408 arises in attribute reader, so
        //we must to try fetching features
        FeatureIterator iterator = results.features();
        Feature feature = iterator.next();
        iterator.close();
        assertNotNull(feature);

        //the id must be grabed correctly.
        //this exercises the fact that although the geometry is not included
        //in the request, it must be fecthed anyway to obtain the SeShape.getFeatureId()
        //getID() should throw an exception if the feature is was not grabed (see
        // ArcSDEAttributeReader.readFID().
        String id = feature.getID();
        assertNotNull(id);
        assertFalse(id.endsWith(".-1"));
        assertFalse(id.endsWith(".0"));
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
            fids.add(ff.featureId(reader.next().getID()));

            //skip one
            if (reader.hasNext()) {
                reader.next();
            }
        }

        reader.close();
        
        Id filter = ff.id(new HashSet(fids));

        FeatureSource source = ds.getFeatureSource(typeName);
        Query query = new DefaultQuery(typeName, filter);
        FeatureCollection results = source.getFeatures(query);

        assertEquals(fids.size(), results.size());
        FeatureIterator iterator = results.features();

        while (iterator.hasNext()) {
            String fid = iterator.next().getID();
            assertTrue("a fid not included in query was returned: " + fid,
                fids.contains(ff.featureId(fid)));
        }
        results.close( iterator );
    }
    
    public void testMoreThan1000FidFilters() throws Exception {
        final DataStore ds = testData.getDataStore();
        final String typeName = testData.getPoint_table();

        //grab some fids
        FeatureReader reader = ds.getFeatureReader(new DefaultQuery(typeName),
                Transaction.AUTO_COMMIT);
        List fids = new ArrayList();

        if (reader.hasNext()) {
            fids.add(ff.featureId(reader.next().getID()));
        }

        reader.close();
        
        String idTemplate = ((FeatureId)fids.get(0)).getID();
        idTemplate = idTemplate.substring(0, idTemplate.length() - 1);
        
        for (int x = 100; x < 2000; x++) {
            fids.add(ff.featureId(idTemplate + x));
        }
        
        Id filter = ff.id(new HashSet(fids));

        FeatureSource source = ds.getFeatureSource(typeName);
        Query query = new DefaultQuery(typeName, filter);
        FeatureCollection results = source.getFeatures(query);

        assertEquals(1, results.size());
        FeatureIterator iterator = results.features();

        while (iterator.hasNext()) {
            String fid = iterator.next().getID();
            assertTrue("a fid not included in query was returned: " + fid,
                fids.contains(ff.featureId(fid)));
        }
        results.close( iterator );
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
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testBBoxFilterPoints() throws Exception {
        //String uri = getFilterUri("filters.bbox.points.filter");
        //int expected = getExpectedCount("filters.bbox.points.expectedCount");
        int expected = 6;
        testBBox(testData.getPoint_table(), expected);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testBBoxFilterLines() throws Exception {
        //String uri = getFilterUri("filters.bbox.lines.filter");
        //int expected = getExpectedCount("filters.bbox.lines.expectedCount");
        int expected = 22;
        testBBox(testData.getLine_table(), expected);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testBBoxFilterPolygons() throws Exception {
        //String uri = getFilterUri("filters.bbox.polygons.filter");
        //int expected = getExpectedCount("filters.bbox.polygons.expectedCount");
        int expected = 8;
        testBBox(testData.getPolygon_table(), expected);
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

        FeatureCollection results = fsource.getFeatures();
        int count = results.size();
        assertTrue("size returns " + count, count > 0);
        LOGGER.fine("feature count: " + count);

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

        FeatureIterator reader = results.features();
        assertTrue(reader.hasNext());

        try {
            assertNotNull(reader.next());
        } catch (NoSuchElementException ex) {
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
    private String getFilterUri(String filterKey) throws IOException {
        String filterFileName = testData.getConProps().getProperty(filterKey);

        if (filterFileName == null) {
            fail(filterKey
                + " param not found in tests configurarion properties file");
        }

        String uri = org.geotools.test.TestData.url(null, filterFileName).toString();

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
            fail(key
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
     *
     * @throws IOException DOCUMENT ME!
     */
    private void testFilter(Filter filter, FeatureSource fsource, int expected)
        throws IOException {
        FeatureCollection fc = fsource.getFeatures(filter);
        int fCount = fc.size();
        LOGGER.info("collection size: "
            + fCount);

        FeatureIterator fi = fc.features();
        int numFeat = 0;
        while (fi.hasNext()) {
            fi.next();
            numFeat++;
        }
        

        String failMsg = "Fully fetched features size and estimated num features count does not match";
        assertEquals(failMsg, fCount, numFeat);
        fc.close(fi);
    }

    /**
     * DOCUMENT ME!
     *
     * @param table DOCUMENT ME!
     * @param expected DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    private void testBBox(String table, int expected) throws Exception {
        FeatureSource fs = store.getFeatureSource(table);
        Filter bboxFilter = getBBoxfilter(fs);
        testFilter(bboxFilter, fs, expected);
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
        FeatureType schema = fs.getSchema();
        BBOX bbe = ff.bbox(schema.getDefaultGeometry().getLocalName(),
                -60, -55, -40, -20,
                schema.getDefaultGeometry().getCoordinateSystem().getName().getCode());
        return bbe;
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
                LOGGER.fine("testTypeExists OK: " + table);

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
        LOGGER.fine("getting all features from " + table);

        FeatureSource source = store.getFeatureSource(table);
        int expectedCount = getExpectedCount("getfeatures." + wich
                + ".expectedCount");
        int fCount = source.getCount(Query.ALL);
        String failMsg = "Expected and returned result count does not match";
        assertEquals(failMsg, expectedCount, fCount);

        FeatureCollection fresults = source.getFeatures();
        FeatureCollection features = fresults;
        failMsg = "FeatureResults.size and .collection().size thoes not match";
        assertEquals(failMsg, fCount, features.size());
        LOGGER.fine("fetched " + fCount + " features for " + wich
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
        LOGGER.finer("just made parser, " + uri);
        p.parse(uri);
        LOGGER.finest("just parsed: " + uri);

        Filter filter = filterHandler.getFilter();

        return filter;
    }
}
