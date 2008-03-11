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
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.geotools.arcsde.ArcSDEDataStoreFactory;
import org.geotools.arcsde.ArcSdeException;
import org.geotools.arcsde.pool.ArcSDEConnectionPool;
import org.geotools.arcsde.pool.ArcSDEPooledConnection;
import org.geotools.arcsde.pool.UnavailableArcSDEConnectionException;
import org.geotools.data.DataSourceException;

import com.esri.sde.sdk.client.SDEPoint;
import com.esri.sde.sdk.client.SeColumnDefinition;
import com.esri.sde.sdk.client.SeCoordinateReference;
import com.esri.sde.sdk.client.SeDelete;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeExtent;
import com.esri.sde.sdk.client.SeFilter;
import com.esri.sde.sdk.client.SeInsert;
import com.esri.sde.sdk.client.SeLayer;
import com.esri.sde.sdk.client.SeObjectId;
import com.esri.sde.sdk.client.SeQuery;
import com.esri.sde.sdk.client.SeQueryInfo;
import com.esri.sde.sdk.client.SeRegistration;
import com.esri.sde.sdk.client.SeRow;
import com.esri.sde.sdk.client.SeShape;
import com.esri.sde.sdk.client.SeShapeFilter;
import com.esri.sde.sdk.client.SeSqlConstruct;
import com.esri.sde.sdk.client.SeState;
import com.esri.sde.sdk.client.SeTable;
import com.esri.sde.sdk.client.SeVersion;

/**
 * Exercises the ArcSDE Java API to ensure our assumptions are correct.
 * 
 * <p>
 * Some of this tests asserts the information from the documentation found on <a
 * href="http://arcsdeonline.esri.com">arcsdeonline </a>, and others are needed
 * to validate our assumptions in the API behavior due to the very little
 * documentation ESRI provides about the less obvious things.
 * </p>
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/plugin/arcsde/datastore/src/test/java/org/geotools/arcsde/data/ArcSDEJavaApiTest.java $
 * @version $Id$
 */
public class ArcSDEJavaApiTest extends TestCase {
    /** package logger */
    private static Logger LOGGER = org.geotools.util.logging.Logging
            .getLogger(ArcSDEJavaApiTest.class.getPackage().getName());

    /** utility to load test parameters and build a datastore with them */
    private static TestData testData;

    private ArcSDEPooledConnection conn;

    private ArcSDEConnectionPool pool;

    /**
     * Runs this test suite
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(ArcSDEJavaApiTest.class);
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
        suite.addTestSuite(ArcSDEJavaApiTest.class);

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
        boolean cleanTestTable = true;
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
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // facilitates running a single test at a time (eclipse lets you do this
        // and it's very useful)
        if (testData == null) {
            oneTimeSetUp();
        }
        this.pool = testData.getDataStore().getConnectionPool();
        this.conn = this.pool.getConnection();
    }

    /**
     * DOCUMENT ME!
     * 
     * @throws Exception
     *             DOCUMENT ME!
     */
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if (conn != null) {
            try {
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        conn = null;
        pool = null;
    }

    public void testNullSQLConstruct() throws Exception {
        String[] columns = { TestData.TEST_TABLE_COLS[0] };
        SeSqlConstruct sql = null;

        try {
            SeQuery rowQuery = new SeQuery(conn, columns, sql);
            rowQuery.prepareQuery();
            rowQuery.execute();
            fail("A null SeSqlConstruct should have thrown an exception!");
        } catch (SeException e) {
            assertTrue(true);
        }
    }

    public void testEmptySQLConstruct() throws Exception {
        String typeName = testData.getTemp_table();
        String[] columns = { TestData.TEST_TABLE_COLS[0] };
        SeSqlConstruct sql = new SeSqlConstruct(typeName);

        SeQuery rowQuery = new SeQuery(conn, columns, sql);
        try {
            rowQuery.prepareQuery();
            rowQuery.execute();
        } finally {
            rowQuery.close();
        }
    }

    /**
     * DOCUMENT ME!
     * 
     * @throws Exception
     *             DOCUMENT ME!
     */
    public void testGetBoundsWhileFetchingRows() throws Exception {
        try {
            final String typeName = testData.getTemp_table();
            final String[] columns = { TestData.TEST_TABLE_COLS[0] };
            final SeSqlConstruct sql = new SeSqlConstruct(typeName);

            SeQueryInfo qInfo = new SeQueryInfo();
            qInfo.setConstruct(sql);

            // add a bounding box filter and verify both spatial and non spatial
            // constraints affects the COUNT statistics
            SeExtent extent = new SeExtent(-180, -90, -170, -80);

            SeLayer layer = conn.getLayer(typeName);
            SeShape filterShape = new SeShape(layer.getCoordRef());
            filterShape.generateRectangle(extent);

            SeShapeFilter bboxFilter = new SeShapeFilter(typeName, layer.getSpatialColumn(),
                    filterShape, SeFilter.METHOD_ENVP, true);
            SeFilter[] spatFilters = { bboxFilter };

            for (int i = 0; i < 26; i++) {
                LOGGER.fine("Running iteration #" + i);

                SeQuery rowQuery = new SeQuery(conn, columns, sql);
                rowQuery.setSpatialConstraints(SeQuery.SE_OPTIMIZE, false, spatFilters);
                rowQuery.prepareQuery();
                rowQuery.execute();

                // fetch some rows
                rowQuery.fetch();
                rowQuery.fetch();
                rowQuery.fetch();

                SeQuery countQuery = new SeQuery(conn, columns, sql);
                countQuery.setSpatialConstraints(SeQuery.SE_OPTIMIZE, true, spatFilters);

                final int expCount = 2;

                SeTable.SeTableStats tableStats = countQuery.calculateTableStatistics("POP_ADMIN",
                        SeTable.SeTableStats.SE_COUNT_STATS, qInfo, 0);

                rowQuery.fetch();
                rowQuery.fetch();

                int resultCount = tableStats.getCount();

                assertEquals(expCount, resultCount);

                rowQuery.close();
                countQuery.close();
            }
            LOGGER.fine("TEST PASSED");
        } catch (SeException e) {
            LOGGER.warning(e.getSeError().getErrDesc());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 
     * @param whereClause
     *            where clause, may be null
     * @param spatFilters
     *            spatial filters, may be null
     * @return the sde calculated counts for the given filter
     * @throws Exception
     */
    private int getTempTableCount(final String whereClause, final SeFilter[] spatFilters)
            throws Exception {
        String typeName = testData.getTemp_table();
        String[] columns = { "INT32_COL" };

        SeSqlConstruct sql = new SeSqlConstruct(typeName);
        if (whereClause != null) {
            sql.setWhere(whereClause);
        }
        SeQuery query = new SeQuery(conn, columns, sql);
        SeQueryInfo qInfo = new SeQueryInfo();
        qInfo.setConstruct(sql);

        if (spatFilters != null) {
            query.setSpatialConstraints(SeQuery.SE_OPTIMIZE, true, spatFilters);
        }

        SeTable.SeTableStats tableStats = query.calculateTableStatistics("INT32_COL",
                SeTable.SeTableStats.SE_COUNT_STATS, qInfo, 0);

        int actualCount = tableStats.getCount();
        query.close();
        return actualCount;
    }

    /**
     * DOCUMENT ME!
     * 
     * @throws Exception
     *             DOCUMENT ME!
     */
    public void testCalculateCount() throws Exception {
        try {
            String typeName = testData.getTemp_table();
            String where = "INT32_COL < 5";
            int expCount = 4;
            int actualCount;

            actualCount = getTempTableCount(where, null);
            assertEquals(expCount, actualCount);

            // add a bounding box filter and verify both spatial and non spatial
            // constraints affects the COUNT statistics
            SeExtent extent = new SeExtent(-180, -90, -170, -80);

            SeLayer layer = conn.getLayer(typeName);
            SeShape filterShape = new SeShape(layer.getCoordRef());
            filterShape.generateRectangle(extent);

            SeShapeFilter bboxFilter = new SeShapeFilter(typeName, layer.getSpatialColumn(),
                    filterShape, SeFilter.METHOD_ENVP, true);
            SeFilter[] spatFilters = { bboxFilter };

            expCount = 1;

            actualCount = getTempTableCount(where, spatFilters);

            assertEquals(expCount, actualCount);
        } catch (SeException e) {
            LOGGER.warning(e.getSeError().getErrDesc());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Ensures a point SeShape behaves as expected.
     * 
     * @throws SeException
     *             if it is thrown while constructing the SeShape
     */
    public void testPointFormat() throws SeException {
        int numPts = 1;
        SDEPoint[] ptArray = new SDEPoint[numPts];
        ptArray[0] = new SDEPoint(3000, 100);

        SeShape point = new SeShape();
        point.generatePoint(numPts, ptArray);

        int numParts = 0;
        double[][][] coords = point.getAllCoords();

        assertEquals("Num of parts invalid", numPts, coords.length);

        for (; numParts < numPts; numParts++) {
            assertEquals("Num subparts invalid", 1, coords[numParts].length);
        }

        for (; numParts < numPts; numParts++) {
            int numSubParts = 0;

            for (; numSubParts < coords[numParts].length; numParts++) {
                assertEquals("Num of points invalid", 2, coords[numParts][numSubParts].length);
            }
        }
    }

    /**
     * Ensures a multipoint SeShape behaves as expected.
     * 
     * @throws SeException
     *             if it is thrown while constructing the SeShape
     */
    public void testMultiPointFormat() throws SeException {
        int numPts = 4;
        SDEPoint[] ptArray = new SDEPoint[numPts];
        ptArray[0] = new SDEPoint(3000, 100);
        ptArray[1] = new SDEPoint(3000, 300);
        ptArray[2] = new SDEPoint(4000, 300);
        ptArray[3] = new SDEPoint(4000, 100);

        SeShape point = new SeShape();
        point.generatePoint(numPts, ptArray);

        double[][][] coords = point.getAllCoords();
        assertEquals("Num of parts invalid", numPts, coords.length);

        int numParts = 0;

        for (; numParts < numPts; numParts++) {
            assertEquals("Num subparts invalid", 1, coords[numParts].length);
        }

        for (; numParts < numPts; numParts++) {
            int numSubParts = 0;

            for (; numSubParts < coords[numParts].length; numParts++) {
                assertEquals("Num of points invalid", 2, coords[numParts][numSubParts].length);
            }
        }
    }

    /**
     * Ensures a linestring SeShape behaves as expected.
     * 
     * @throws SeException
     *             if it is thrown while constructing the SeShape
     */
    public void testLineStringFormat() throws SeException {
        int numPts = 4;
        SDEPoint[] ptArray = new SDEPoint[numPts];
        ptArray[0] = new SDEPoint(3000, 100);
        ptArray[1] = new SDEPoint(3000, 300);
        ptArray[2] = new SDEPoint(4000, 300);
        ptArray[3] = new SDEPoint(4000, 100);

        SeShape point = new SeShape();
        int numParts = 1;
        int[] partOffsets = { 0 }; // index of each part's start in the gobal
        // coordinate array
        point.generateLine(numPts, numParts, partOffsets, ptArray);

        double[][][] coords = point.getAllCoords();

        assertEquals("Num of parts invalid", 1, coords.length);

        assertEquals("Num subparts invalid", 1, coords[0].length);

        assertEquals("Num of points invalid", 2 * numPts, coords[0][0].length);
    }

    /**
     * Ensures a multilinestring SeShape behaves as expected.
     * 
     * @throws SeException
     *             if it is thrown while constructing the SeShape
     */
    public void testMultiLineStringFormat() throws SeException {
        int numPts = 4;
        SDEPoint[] ptArray = new SDEPoint[numPts];
        ptArray[0] = new SDEPoint(3000, 100);
        ptArray[1] = new SDEPoint(3000, 300);
        ptArray[2] = new SDEPoint(4000, 300);
        ptArray[3] = new SDEPoint(4000, 100);

        SeShape point = new SeShape();
        int numParts = 2;
        int[] partOffsets = { 0, 2 }; // index of each part's start in the
        // gobal coordinate array
        point.generateLine(numPts, numParts, partOffsets, ptArray);

        double[][][] coords = point.getAllCoords();

        assertEquals("Num of parts invalid", numParts, coords.length);

        assertEquals("Num subparts invalid", 1, coords[0].length);
        assertEquals("Num subparts invalid", 1, coords[1].length);

        assertEquals("Num of points invalid", numPts, coords[0][0].length);
        assertEquals("Num of points invalid", numPts, coords[1][0].length);
    }

    /**
     * Ensures a polygon SeShape behaves as expected, building a simple polygon
     * and a polygon with a hole.
     * 
     * @throws SeException
     *             if it is thrown while constructing the SeShape
     */
    public void testPolygonFormat() throws SeException {
        /*
         * Generate an area shape composed of two polygons, the first with a
         * hole
         */
        int numPts = 4;
        int numParts = 1;
        int[] partOffsets = new int[numParts];
        partOffsets[0] = 0;

        SDEPoint[] ptArray = new SDEPoint[numPts];

        // simple polygon
        ptArray[0] = new SDEPoint(1600, 1200);
        ptArray[1] = new SDEPoint(2800, 1650);
        ptArray[2] = new SDEPoint(1800, 2000);
        ptArray[3] = new SDEPoint(1600, 1200);

        SeShape polygon = new SeShape();
        polygon.generatePolygon(numPts, numParts, partOffsets, ptArray);

        double[][][] coords = polygon.getAllCoords();

        assertEquals("Num of parts invalid", numParts, coords.length);
        assertEquals("Num subparts invalid", 1, coords[0].length);
        assertEquals("Num of points invalid", 2 * 4, coords[0][0].length);

        numPts = 14;
        numParts = 1;
        ptArray = new SDEPoint[numPts];
        partOffsets = new int[numParts];
        partOffsets[0] = 0;

        // part one
        ptArray[0] = new SDEPoint(100, 1100);
        ptArray[1] = new SDEPoint(1500, 1100);
        ptArray[2] = new SDEPoint(1500, 1900);
        ptArray[3] = new SDEPoint(100, 1900);
        ptArray[4] = new SDEPoint(100, 1100);

        // Hole - sub part of part one
        ptArray[5] = new SDEPoint(200, 1200);
        ptArray[6] = new SDEPoint(200, 1500);
        ptArray[7] = new SDEPoint(500, 1500);
        ptArray[8] = new SDEPoint(500, 1700);
        ptArray[9] = new SDEPoint(800, 1700);
        ptArray[10] = new SDEPoint(800, 1500);
        ptArray[11] = new SDEPoint(500, 1500);
        ptArray[12] = new SDEPoint(500, 1200);
        ptArray[13] = new SDEPoint(200, 1200);

        polygon = new SeShape();
        polygon.generatePolygon(numPts, numParts, partOffsets, ptArray);

        coords = polygon.getAllCoords();

        assertEquals("Num of parts invalid", numParts, coords.length);
        assertEquals("Num subparts invalid", 2, coords[0].length);

        // first part of first polygon (shell) has 5 points
        assertEquals("Num of points invalid", 2 * 5, coords[0][0].length);

        // second part of first polygon (hole) has 9 points
        assertEquals("Num of points invalid", 2 * 9, coords[0][1].length);
    }

    /**
     * Ensures a multipolygon SeShape behaves as expected.
     * 
     * @throws SeException
     *             if it is thrown while constructing the SeShape
     */
    public void testMultiPolygonFormat() throws SeException {
        /*
         * Generate an area shape composed of two polygons, the first with a
         * hole
         */
        int numPts = 18;
        int numParts = 2;
        int[] partOffsets = new int[numParts];
        partOffsets[0] = 0;
        partOffsets[1] = 14;

        SDEPoint[] ptArray = new SDEPoint[numPts];

        // part one
        ptArray[0] = new SDEPoint(100, 1100);
        ptArray[1] = new SDEPoint(1500, 1100);
        ptArray[2] = new SDEPoint(1500, 1900);
        ptArray[3] = new SDEPoint(100, 1900);
        ptArray[4] = new SDEPoint(100, 1100);

        // Hole - sub part of part one
        ptArray[5] = new SDEPoint(200, 1200);
        ptArray[6] = new SDEPoint(200, 1500);
        ptArray[7] = new SDEPoint(500, 1500);
        ptArray[8] = new SDEPoint(500, 1700);
        ptArray[9] = new SDEPoint(800, 1700);
        ptArray[10] = new SDEPoint(800, 1500);
        ptArray[11] = new SDEPoint(500, 1500);
        ptArray[12] = new SDEPoint(500, 1200);
        ptArray[13] = new SDEPoint(200, 1200);

        // part two
        ptArray[14] = new SDEPoint(1600, 1200);
        ptArray[15] = new SDEPoint(2800, 1650);
        ptArray[16] = new SDEPoint(1800, 2000);
        ptArray[17] = new SDEPoint(1600, 1200);

        SeShape multipolygon = new SeShape();
        multipolygon.generatePolygon(numPts, numParts, partOffsets, ptArray);

        double[][][] coords = multipolygon.getAllCoords();

        assertEquals("Num of parts invalid", numParts, coords.length);

        // the first polygon has 2 parts
        assertEquals("Num subparts invalid", 2, coords[0].length);

        // the second polygon has only 1 part
        assertEquals("Num subparts invalid", 1, coords[1].length);

        // first part of first polygon (shell) has 5 points
        assertEquals("Num of points invalid", 2 * 5, coords[0][0].length);

        // second part of first polygon (hole) has 9 points
        assertEquals("Num of points invalid", 2 * 9, coords[0][1].length);

        // second polygon (shell with no holes) has 4 points
        assertEquals("Num of points invalid", 2 * 4, coords[1][0].length);
    }

    /**
     * Creates an ArcSDE table, "EXAMPLE", and adds a spatial column, "SHAPE",
     * to it.
     * 
     * <p>
     * This code is directly taken from the createBaseTable mehtod of the
     * arcsdeonline "Working with layers" example, to verify that it works prior
     * to blame the gt implementation.
     * </p>
     * 
     * @throws SeException
     *             DOCUMENT ME!
     * @throws IOException
     *             DOCUMENT ME!
     * @throws UnavailableArcSDEConnectionException
     *             DOCUMENT ME!
     */
    public void testCreateBaseTable() throws SeException, IOException,
            UnavailableArcSDEConnectionException {
        SeLayer layer = new SeLayer(conn);
        SeTable table = null;

        try {
            /*
             * Create a qualified table name with current user's name and the
             * name of the table to be created, "EXAMPLE".
             */
            String tableName = (conn.getUser() + ".EXAMPLE");
            table = new SeTable(conn, tableName);
            layer.setTableName("EXAMPLE");

            try {
                table.delete();
            } catch (Exception e) {
                LOGGER.warning(e.getMessage());
            }

            SeColumnDefinition[] colDefs = new SeColumnDefinition[7];

            /*
             * Define the columns and their attributes for the table to be
             * created. NOTE: The valid range/values of size and scale
             * parameters vary from one database to another.
             */
            boolean isNullable = true;
            colDefs[0] = new SeColumnDefinition("INT32_COL", SeColumnDefinition.TYPE_INTEGER, 10,
                    0, isNullable);
            colDefs[1] = new SeColumnDefinition("INT16_COL", SeColumnDefinition.TYPE_SMALLINT, 4,
                    0, isNullable);
            colDefs[2] = new SeColumnDefinition("FLOAT32_COL", SeColumnDefinition.TYPE_FLOAT, 5, 2,
                    isNullable);
            colDefs[3] = new SeColumnDefinition("FLOAT64_COL", SeColumnDefinition.TYPE_DOUBLE, 15,
                    4, isNullable);
            colDefs[4] = new SeColumnDefinition("STRING_COL", SeColumnDefinition.TYPE_STRING, 25,
                    0, isNullable);
            colDefs[5] = new SeColumnDefinition("DATE_COL", SeColumnDefinition.TYPE_DATE, 1, 0,
                    isNullable);
            colDefs[6] = new SeColumnDefinition("INT64_COL", SeColumnDefinition.TYPE_INTEGER, 10,
                    0, isNullable);

            /*
             * Create the table using the DBMS default configuration keyword.
             * Valid keywords are defined in the dbtune table.
             */
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("\n--> Creating a table using DBMS Default Keyword");
            }
            table.create(colDefs, testData.getConfigKeyword());
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(" - Done.");
            }
            /*
             * Define the attributes of the spatial column
             */
            layer.setSpatialColumnName("SHAPE");

            /*
             * Set the type of shapes that can be inserted into the layer. Shape
             * type can be just one or many. NOTE: Layers that contain more than
             * one shape type can only be accessed through the C and Java APIs
             * and Arc Explorer Java 3.x. They cannot be seen from ArcGIS
             * desktop applications.
             */
            layer.setShapeTypes(SeLayer.SE_NIL_TYPE_MASK | SeLayer.SE_POINT_TYPE_MASK
                    | SeLayer.SE_LINE_TYPE_MASK | SeLayer.SE_SIMPLE_LINE_TYPE_MASK
                    | SeLayer.SE_AREA_TYPE_MASK | SeLayer.SE_MULTIPART_TYPE_MASK);
            layer.setGridSizes(1100.0, 0.0, 0.0);
            layer.setDescription("Layer Example");

            SeExtent ext = new SeExtent(0.0, 0.0, 10000.0, 10000.0);
            layer.setExtent(ext);

            /*
             * Define the layer's Coordinate Reference
             */
            SeCoordinateReference coordref = TestData.getGenericCoordRef();
            layer.setCoordRef(coordref);

            /*
             * Spatially enable the new table...
             */
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("\n--> Adding spatial column \"SHAPE\"...");
            }
            layer.setCreationKeyword(testData.getConfigKeyword());
            layer.create(3, 4);
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(" - Done.");
            }
        } catch (SeException e) {
            LOGGER.throwing(this.getClass().getName(), "testCreateBaseTable", e);
            throw e;
        }
    } // End method createBaseTable

    /**
     * Creates an ArcSDE table, "EXAMPLE", and adds a spatial column, "SHAPE",
     * to it.
     * 
     * <p>
     * This code is directly taken from the createBaseTable mehtod of the
     * arcsdeonline "Working with layers" example, to verify that it works prior
     * to blame the gt implementation.
     * </p>
     * 
     * @throws SeException
     *             DOCUMENT ME!
     * @throws IOException
     *             DOCUMENT ME!
     * @throws UnavailableArcSDEConnectionException
     *             DOCUMENT ME!
     */
    public void testCreateNonStandardSchema() throws SeException, IOException,
            UnavailableArcSDEConnectionException {
        final SeLayer layer = new SeLayer(conn);
        /*
         * Create a qualified table name with current user's name and the name
         * of the table to be created, "EXAMPLE".
         */
        final String tableName = (conn.getUser() + ".NOTENDSWITHGEOM");
        final SeTable table = new SeTable(conn, tableName);
        try {
            layer.setTableName("NOTENDSWITHGEOM");

            try {
                table.delete();
            } catch (Exception e) {
                // intentionally blank
            }

            /*
             * Create the table using the DBMS default configuration keyword.
             * Valid keywords are defined in the dbtune table.
             */
            if (LOGGER.isLoggable(Level.FINE)) {
                System.out.println("\n--> Creating a table using DBMS Default Keyword");
            }
            SeColumnDefinition[] tmpCols = new SeColumnDefinition[] { new SeColumnDefinition("tmp",
                    SeColumnDefinition.TYPE_STRING, 5, 0, true) };
            table.create(tmpCols, testData.getConfigKeyword());
            if (LOGGER.isLoggable(Level.FINE)) {
                System.out.println(" - Done.");
            }
            SeColumnDefinition[] colDefs = new SeColumnDefinition[7];

            /*
             * Define the columns and their attributes for the table to be
             * created. NOTE: The valid range/values of size and scale
             * parameters vary from one database to another.
             */
            boolean isNullable = true;
            colDefs[0] = new SeColumnDefinition("INT32_COL", SeColumnDefinition.TYPE_INTEGER, 10,
                    0, isNullable);
            colDefs[1] = new SeColumnDefinition("INT16_COL", SeColumnDefinition.TYPE_SMALLINT, 4,
                    0, isNullable);
            colDefs[2] = new SeColumnDefinition("FLOAT32_COL", SeColumnDefinition.TYPE_FLOAT, 5, 2,
                    isNullable);
            colDefs[3] = new SeColumnDefinition("FLOAT64_COL", SeColumnDefinition.TYPE_DOUBLE, 15,
                    4, isNullable);
            colDefs[4] = new SeColumnDefinition("STRING_COL", SeColumnDefinition.TYPE_STRING, 25,
                    0, isNullable);
            colDefs[5] = new SeColumnDefinition("DATE_COL", SeColumnDefinition.TYPE_DATE, 1, 0,
                    isNullable);
            colDefs[6] = new SeColumnDefinition("INT64_COL", SeColumnDefinition.TYPE_INTEGER, 10,
                    0, isNullable);

            table.addColumn(colDefs[0]);
            table.addColumn(colDefs[1]);
            table.addColumn(colDefs[2]);
            table.addColumn(colDefs[3]);
            table.dropColumn(tmpCols[0].getName());

            /*
             * Define the attributes of the spatial column
             */
            layer.setSpatialColumnName("SHAPE");

            /*
             * Set the type of shapes that can be inserted into the layer. Shape
             * type can be just one or many. NOTE: Layers that contain more than
             * one shape type can only be accessed through the C and Java APIs
             * and Arc Explorer Java 3.x. They cannot be seen from ArcGIS
             * desktop applications.
             */
            layer.setShapeTypes(SeLayer.SE_NIL_TYPE_MASK | SeLayer.SE_POINT_TYPE_MASK
                    | SeLayer.SE_LINE_TYPE_MASK | SeLayer.SE_SIMPLE_LINE_TYPE_MASK
                    | SeLayer.SE_AREA_TYPE_MASK | SeLayer.SE_MULTIPART_TYPE_MASK);
            layer.setGridSizes(1100.0, 0.0, 0.0);
            layer.setDescription("Layer Example");

            SeExtent ext = new SeExtent(0.0, 0.0, 10000.0, 10000.0);
            layer.setExtent(ext);

            /*
             * Define the layer's Coordinate Reference
             */
            SeCoordinateReference coordref = new SeCoordinateReference();
            coordref.setXY(0, 0, 100);
            layer.setCoordRef(coordref);

            /*
             * Spatially enable the new table...
             */
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("\n--> Adding spatial column \"SHAPE\"...");
            }
            layer.setCreationKeyword(testData.getConfigKeyword());

            layer.create(3, 4);
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(" - Done.");
            }

            table.addColumn(colDefs[4]);
            table.addColumn(colDefs[5]);
            table.addColumn(colDefs[6]);
            // } catch (SeException e) {
            // LOGGER.throwing(this.getClass().getName(),
            // "testCreateNonStandardSchema", e);
            // throw e;
        } finally {
            try {
                table.delete();
            } catch (Exception e) {
                // intentionally blank
            }

            try {
                layer.delete();
            } catch (Exception e) {
                // intentionally blank
            }
        }
    } // End method createBaseTable

    public void testDeleteById() throws DataSourceException, UnavailableArcSDEConnectionException,
            SeException {

        final String typeName = testData.getTemp_table();
        final SeQuery query = new SeQuery(conn, new String[] { "ROW_ID", "INT32_COL" },
                new SeSqlConstruct(typeName));
        query.prepareQuery();
        query.execute();

        final int rowId;
        try {
            SeRow row = query.fetch();
            rowId = row.getInteger(0).intValue();
        } finally {
            query.close();
        }

        SeDelete delete = new SeDelete(conn);
        delete.byId(typeName, new SeObjectId(rowId));

        final String whereClause = "ROW_ID=" + rowId;
        final SeSqlConstruct sqlConstruct = new SeSqlConstruct(typeName, whereClause);
        final SeQuery deletedQuery = new SeQuery(conn, new String[] { "ROW_ID" }, sqlConstruct);

        deletedQuery.prepareQuery();
        deletedQuery.execute();

        SeRow row = deletedQuery.fetch();
        assertNull(whereClause + " should have returned no records as it was deleted", row);
    }

    /**
     * Does a query over a non autocommit transaction return the added/modified
     * features and hides the deleted ones?
     * 
     * @throws DataSourceException
     */
    public void testTransactionStateRead() throws Exception {
        // connection with a transaction in progress
        final ArcSDEPooledConnection transConn;

        final SeTable tempTable = testData.getTempTable();
        // final SeLayer tempLayer = testData.getTempLayer();

        testData.truncateTempTable();

        {
            final ArcSDEConnectionPool connPool = testData.getConnectionPool();
            transConn = connPool.getConnection();
            // start a transaction on transConn
            transConn.setTransactionAutoCommit(0);
            transConn.startTransaction();
        }

        // flag to rollback or not at finally{}
        boolean commited = false;

        try {
            SeInsert insert = new SeInsert(transConn);
            final String[] columns = { "INT32_COL", "STRING_COL" };
            final String tableName = tempTable.getName();
            insert.intoTable(tableName, columns);
            insert.setWriteMode(true);
            SeRow row = insert.getRowToSet();
            row.setInteger(0, Integer.valueOf(50));
            row.setString(1, "inside transaction");

            insert.execute();
            // IMPORTANT to call close for the diff to take effect
            insert.close();

            final SeSqlConstruct sqlConstruct = new SeSqlConstruct(tableName);
            // the query over the transaction connection
            SeQuery transQuery = new SeQuery(transConn, columns, sqlConstruct);

            // transaction is not committed, so transQuery should give the
            // inserted
            // record and query don't
            transQuery.prepareQuery();
            transQuery.execute();
            SeRow transRow = transQuery.fetch();
            // querying over a transaction in progress does give diff
            assertNotNull(transRow);
            // assertEquals(Integer.valueOf(50), transRow.getInteger(0));
            transQuery.close();

            // commit transaction
            transConn.commitTransaction();
            commited = true;

            SeQuery query = new SeQuery(this.conn, columns, sqlConstruct);
            query.prepareQuery();
            query.execute();
            assertNotNull(query.fetch());
            query.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (!commited) {
                transConn.rollbackTransaction();
            }
            transConn.close();
            // conn.close(); closed at tearDown
        }
    }

    /**
     * Creates a versioned table with two versions, the default one and another
     * one, makes edits over the default one, checks states are consistent in
     * both
     * 
     * @throws Exception
     */
    public void testEditVersionedTable_DefaultVersion() throws Exception {
        final SeTable versionedTable = createVersionedTable();

        // create a new version
        SeVersion defaultVersion;
        SeVersion newVersion;
        {
            defaultVersion = new SeVersion(conn, SeVersion.SE_QUALIFIED_DEFAULT_VERSION_NAME);
            defaultVersion.getInfo();

            newVersion = new SeVersion(conn, SeVersion.SE_QUALIFIED_DEFAULT_VERSION_NAME);
            // newVersion.getInfo();
            newVersion.setName(conn.getUser() + ".GeoToolsTestVersion");
            newVersion.setParentName(defaultVersion.getName());
            newVersion.setDescription(defaultVersion.getName()
                    + " child for GeoTools ArcSDE unit tests");
            // do not require ArcSDE to create a unique name if the required
            // version already exists
            boolean uniqueName = false;
            try {
                newVersion.create(uniqueName, newVersion);
            } catch (SeException e) {
                int sdeError = e.getSeError().getSdeError();
                if (sdeError != -177) {
                    throw e;
                }
                // "VERSION ALREADY EXISTS", ignore and continue..
                newVersion.getInfo();
            }
        }

        // edit default version
        SeState defVersionState = new SeState(conn, defaultVersion.getStateId());
        // create a new state as a child of the current one, the current one
        // must be closed
        if (defVersionState.isOpen()) {
            defVersionState.close();
        }
        SeState newState1 = new SeState(conn);
        newState1.create(defVersionState.getId());

        try {
            conn.startTransaction();
            insertIntoVersionedTable(newState1, versionedTable.getName(), "name 1 state 1");
            insertIntoVersionedTable(newState1, versionedTable.getName(), "name 2 state 1");
            
            newState1.close();

            SeState newState2 = new SeState(conn);
            SeObjectId parentStateId = newState1.getId();
            newState2.create(parentStateId);

            insertIntoVersionedTable(newState2, versionedTable.getName(), "name 1 state 2");

            // Change the version's state pointer to the last edit state.
            newVersion.changeState(newState2.getId());

            // Trim the state tree.
            newState2.trimTree(parentStateId, newState2.getId());
            conn.commitTransaction();
        } catch (SeException e) {
            new ArcSdeException(e).printStackTrace();
            throw e;
        }
    }

    private void insertIntoVersionedTable(SeState state, String tableName, String nameField)
            throws SeException {
        SeInsert insert = new SeInsert(conn);
        
        SeObjectId differencesId = new SeObjectId(SeState.SE_NULL_STATE_ID);
        insert.setState(state.getId(), differencesId, SeState.SE_STATE_DIFF_NOCHECK);
        
        insert.intoTable(tableName, new String[] { "NAME" });
        SeRow row = insert.getRowToSet();
        row.setString(0, "NAME 1");
        insert.execute();
        insert.close();
    }

    /**
     * Creates a versioned table with a name column and a point SHAPE column
     * 
     * @return the versioned table created
     * @throws Exception
     *             any exception thrown by sde
     */
    private SeTable createVersionedTable() throws Exception {
        SeLayer layer = new SeLayer(conn);
        SeTable table;

        /*
         * Create a qualified table name with current user's name and the name
         * of the table to be created, "EXAMPLE".
         */
        String tableName = (conn.getUser() + ".VERSIONED_EXAMPLE");
        table = new SeTable(conn, tableName);
        layer.setTableName("VERSIONED_EXAMPLE");

        try {
            table.delete();
        } catch (Exception e) {
            LOGGER.warning(e.getMessage());
        }

        SeColumnDefinition[] colDefs = new SeColumnDefinition[1];
        boolean isNullable = true;
        colDefs[0] = new SeColumnDefinition("NAME", SeColumnDefinition.TYPE_STRING, 25, 0,
                isNullable);

        table.create(colDefs, testData.getConfigKeyword());
        layer.setSpatialColumnName("SHAPE");

        layer.setShapeTypes(SeLayer.SE_NIL_TYPE_MASK | SeLayer.SE_POINT_TYPE_MASK);
        layer.setGridSizes(1100.0, 0.0, 0.0);
        layer.setDescription("Layer Example");

        SeExtent ext = new SeExtent(0.0, 0.0, 10000.0, 10000.0);
        layer.setExtent(ext);

        /*
         * Define the layer's Coordinate Reference
         */
        SeCoordinateReference coordref = TestData.getGenericCoordRef();
        layer.setCoordRef(coordref);

        /*
         * Spatially enable the new table...
         */
        layer.setCreationKeyword(testData.getConfigKeyword());
        layer.create(3, 4);

        // register the table as versioned
        SeRegistration registration = new SeRegistration(conn, tableName);
        registration.setMultiVersion(true);
        registration.alter();

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine(" - Done.");
        }
        return table;
    }
}
