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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.geotools.arcsde.ArcSDEDataStoreFactory;
import org.geotools.arcsde.pool.ISession;
import org.geotools.arcsde.pool.SessionPool;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.Transaction;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.FilterFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.pe.PeFactory;
import com.esri.sde.sdk.pe.PeProjectedCS;
import com.esri.sde.sdk.pe.PeProjectionException;
import com.vividsolutions.jts.geom.Point;

/**
 * ArcSDEDAtaStore test cases
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/plugin/arcsde/datastore/src/test/java/org/geotools/arcsde/data/ArcSDEDataStoreTest.java $
 * @version $Id$
 */
public class ArcSDEDataStoreTest extends TestCase {
    /** package logger */
    private static Logger LOGGER = org.geotools.util.logging.Logging
            .getLogger(ArcSDEDataStoreTest.class.getPackage().getName());

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
     * Builds a test suite for all this class' tests with per suite initialization directed to
     * {@link #oneTimeSetUp()} and per suite clean up directed to {@link #oneTimeTearDown()}
     * 
     * @return
     */
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(ArcSDEDataStoreTest.class);

        TestSetup wrapper = new TestSetup(suite) {
            @Override
            protected void setUp() throws Exception {
                oneTimeSetUp();
            }

            @Override
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
        final boolean insertTestData = true;
        testData.createTempTable(insertTestData);
    }

    private static void oneTimeTearDown() {
        boolean cleanTestTable = false;
        boolean cleanPool = true;
        testData.tearDown(cleanTestTable, cleanPool);
    }

    /**
     * loads {@code testData/testparams.properties} into a Properties object, wich is used to obtain
     * test tables names and is used as parameter to find the DataStore
     * 
     * @throws Exception DOCUMENT ME!
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // facilitates running a single test at a time (eclipse lets you do this
        // and it's very useful)
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
    @Override
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
     * This test is currently broken. It's a placeholder for some logic that sfarber wrote which
     * tries to guess the SRS of a featureclass, based on connecting to it via an SeLayer.
     * 
     * @throws Throwable
     */
    public void _testAutoFillSRS() throws Throwable {

        ArcSDEDataStore ds = testData.getDataStore();
        CoordinateReferenceSystem sdeCRS = ds.getSchema("GISDATA.TOWNS_POLY").getDefaultGeometry()
                .getCRS();

        LOGGER.info(sdeCRS.toWKT().replaceAll(" ", "").replaceAll("\n", "")
                .replaceAll("\"", "\\\""));

        // CoordinateReferenceSystem epsgCRS = CRS.decode("EPSG:26986");

        // LOGGER.info("are these two CRS's equal? " +
        // CRS.equalsIgnoreMetadata(sdeCRS, epsgCRS));

        if (1 == 1)
            return;

        int epsgCode = -1;
        int[] projcs = PeFactory.projcsCodelist();
        LOGGER.info(projcs.length + " projections available.");
        for (int i = 0; i < projcs.length; i++) {
            try {
                PeProjectedCS candidate = PeFactory.projcs(projcs[i]);
                // in ArcSDE 9.2, if the PeFactory doesn't support a projection
                // it claimed
                // to support, it returns 'null'. So check for it.
                if (candidate != null && candidate.getName().indexOf("Massachusetts") != -1) {
                    // LOGGER.info("\n\n" + projcs[i] + " has name " +
                    // candidate.getName() + "\ntried to match " + wktName +
                    // "\n\n");
                    epsgCode = projcs[i];
                } else if (candidate == null) {
                    // LOGGER.info(projcs[i] + " was null");
                } else if (candidate != null) {
                    // LOGGER.info(projcs[i] + " wasn't null");
                }
            } catch (PeProjectionException pe) {
                // Strangely SDE includes codes in the projcsCodeList() that
                // it doesn't actually support.
                // Catch the exception and skip them here.
            }
        }

    }

    public void testDispose() throws IOException{
        store.dispose();
        try{
            ((ArcSDEDataStore)store).getSession(Transaction.AUTO_COMMIT);
            fail("Expected IllegalStateException when the datastore has been disposed");
        }catch(IllegalStateException e){
            assertTrue(true);
        }finally{
            //dispose test data so next test does not fail due to pool being closed
            testData.tearDown(false, true);
            testData = null;
        }
    }

    /**
     * test that a ArcSDEDataStore that connects to de configured test database contains the tables
     * defined by the parameters "point_table", "line_table" and "polygon_table", wether ot not
     * they're defined as single table names or as full qualified sde table names (i.e.
     * SDE.SDE.TEST_POINT)
     * 
     * @throws IOException
     * @throws SeException
     */
    public void testGetTypeNames() throws IOException, SeException {
        String[] featureTypes = store.getTypeNames();
        assertNotNull(featureTypes);

        if (LOGGER.isLoggable(Level.FINE)) {
            for (int i = 0; i < featureTypes.length; i++)
                System.out.println(featureTypes[i]);
        }
        testTypeExists(featureTypes, testData.getTempTableName());
    }

    /**
     * tests that the schema for the defined tests tables are returned.
     * 
     * @throws IOException DOCUMENT ME!
     * @throws SeException
     */
    public void testGetSchema() throws IOException, SeException {
        SimpleFeatureType schema;

        schema = store.getSchema(testData.getTempTableName());
        assertNotNull(schema);
        // ROW_ID is not included in TEST_TABLE_COLS
        assertEquals(TestData.TEST_TABLE_COLS.length, schema.getAttributeCount());
    }

    /**
     * Tests the creation of new feature types, with CRS and all.
     * <p>
     * This test also ensures that the arcsde datastore is able of creating schemas where the
     * geometry attribute is not the last one. This is important since to do so, the ArcSDE
     * datastore must break the usual way of creating schemas with the ArcSDE Java API, in which one
     * first creates the (non spatially enabled) "table" with all the non spatial attributes and
     * finally creates the "layer", adding the spatial attribute to the previously created table.
     * So, this test ensures the datastore correctly works arround this limitation.
     * </p>
     * 
     * @throws IOException DOCUMENT ME!
     * @throws SchemaException DOCUMENT ME!
     * @throws SeException
     */
    public void testCreateSchema() throws IOException, SchemaException, SeException {
        final String typeName;
        {
            SessionPool connectionPool = testData.getConnectionPool();
            ISession session = connectionPool.getSession();
            final String user;
            user = session.getUser();
            session.dispose();
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

    // ///////////////// HELPER FUNCTIONS ////////////////////////

    /**
     * checks for the existence of <code>table</code> in <code>featureTypes</code>.
     * <code>table</code> must be a full qualified sde feature type name. (i.e "TEST_POINT" ==
     * "SDE.SDE.TEST_POINT")
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

}
