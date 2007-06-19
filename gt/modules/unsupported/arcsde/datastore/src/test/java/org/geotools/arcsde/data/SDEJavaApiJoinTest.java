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
package org.geotools.arcsde.data;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.geotools.arcsde.pool.ArcSDEConnectionPool;
import org.geotools.arcsde.pool.ArcSDEPooledConnection;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.feature.AttributeType;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.filter.ExpressionBuilder;
import org.geotools.referencing.CRS;
import org.opengis.feature.Feature;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.esri.sde.sdk.client.SDEPoint;
import com.esri.sde.sdk.client.SeColumnDefinition;
import com.esri.sde.sdk.client.SeCoordinateReference;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeInsert;
import com.esri.sde.sdk.client.SeLayer;
import com.esri.sde.sdk.client.SeQuery;
import com.esri.sde.sdk.client.SeQueryInfo;
import com.esri.sde.sdk.client.SeRow;
import com.esri.sde.sdk.client.SeShape;
import com.esri.sde.sdk.client.SeSqlConstruct;
import com.esri.sde.sdk.client.SeTable;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;

/**
 * ArcSDEDAtaStore test case for a master-child joining
 * <p>
 * This test will create an sde layer (table + spatial table) as master and a
 * business table as child:
 * 
 * <pre><code>
 *  -----------------------------------------------
 *  |            GT_SDE_TEST_MASTER               |
 *  -----------------------------------------------
 *  |  ID(int)  | NAME (string)  | SHAPE (Point)  |
 *  -----------------------------------------------
 *  |     1     |   name1        |  POINT(1, 1)   |
 *  -----------------------------------------------
 *  |     2     |   name2        |  POINT(2, 2)   |
 *  -----------------------------------------------
 *  |     3     |   name3        |  POINT(3, 3)   |
 *  -----------------------------------------------
 * 
 *  ---------------------------------------------------------------------
 *  |                     GT_SDE_TEST_CHILD                             |
 *  ---------------------------------------------------------------------
 *  | ID(int)   | MASTER_ID      | NAME (string)  | DESCRIPTION(string  |
 *  ---------------------------------------------------------------------
 *  |    1      |      1         |   child1       |    description1     |
 *  ---------------------------------------------------------------------
 *  |    2      |      2         |   child2       |    description2     |
 *  ---------------------------------------------------------------------
 *  |    3      |      2         |   child3       |    description3     |
 *  ---------------------------------------------------------------------
 *  |    4      |      3         |   child4       |    description4     |
 *  ---------------------------------------------------------------------
 *  |    5      |      3         |   child5       |    description5     |
 *  ---------------------------------------------------------------------
 *  |    6      |      3         |   child6       |    description6     |
 *  ---------------------------------------------------------------------
 * </code>
 * &lt;/re&gt;
 * </p>
 * <p>
 *  The following are rules that may help you in correctly specifying an SQL
 *  query that will work with the ArcSDE Java API. This rules was collected
 *  empirically based on some of the tests of this test suite. Be aware that
 *  ArcSDE Java API only supports &quot;queries&quot; of the following form:
 * <code>
 * SELECT &lt;list of qualified column names&gt; 
 *  FROM &lt;list of qualified table names&gt; 
 *  WHERE &lt;any where clause supported by the RDBMS&gt; 
 *  [ORDER BY &lt;qualified column names&gt;]
 * </code>
 *  Rules to create SQL QUERIES:
 * <ul>
 * <li>
 * Use full qualified table names. Queries that usually would work against the 
 *  underlying RDBMS will not work through the ArcSDE Java API if you do not fully
 *  qualify table names.
 * <li>
 * Do not use table aliases, or SHAPE field is fetched as int instead of as geometry.
 * <li>
 * Specifying a GROUP BY clause seems incompatible with using the SHAPE field. If
 *  you specify a GROUP BY clause, ArcSDE will return the plain SHAPE field (int) instead
 *  of a geometry.
 * <li>
 * And the &lt;strong&gt;most important&lt;/strong&gt; one: &lt;strong&gt;SET THE SPATIAL COLUMN AS
 *  THE LAST ONE&lt;/strong&gt;. This is most likely a bug in the ArcSDE Java API, since if you
 *  do not set the shape field as the last one in the select items list an IndexOutOfBoundsException
 *  is thrown by
 * <code>
 * SeRow.fetch()
 * </code>
 * </ul> 
 * </p>
 * 
 *  @author Gabriel Roldan, Axios Engineering
 *  @source $URL$
 *  @version $Id$
 *  @since 2.3.x
 * 
 */
public class SDEJavaApiJoinTest extends TestCase {
    /** package logger */
    private static Logger LOGGER = Logger
            .getLogger(SDEJavaApiJoinTest.class.getPackage().getName());

    /** Helper class that provides config loading and test data for unit tests */
    private TestData testData;

    /** an ArcSDEDataStore created on setUp() to run tests against */
    private ArcSDEDataStore store;

    /**
     * Flag to create tables only once by test suite run since we use them for
     * read only purposes
     */
    private static boolean tablesCreated;

    private static final String MASTER_UNQUALIFIED = "GT_SDE_TEST_MASTER";

    private static final String CHILD_UNQUALIFIED = "GT_SDE_TEST_CHILD";

    private String MASTER;

    private String CHILD;

    private String masterChildSql;

    private CoordinateReferenceSystem testCrs;

    /**
     * loads {@code testData/testparams.properties} into a Properties object,
     * wich is used to obtain test tables names and is used as parameter to find
     * the DataStore
     * 
     * @throws Exception
     *             DOCUMENT ME!
     */
    protected void setUp() throws Exception {
        super.setUp();
        this.testCrs = CRS.decode("EPSG:4326");
        this.testData = new TestData();
        this.testData.setUp();
        this.store = this.testData.getDataStore();

        ArcSDEPooledConnection conn = store.getConnectionPool().getConnection();
        MASTER = conn.getDatabaseName() + "." + conn.getUser() + "." + MASTER_UNQUALIFIED;
        CHILD = conn.getDatabaseName() + "." + conn.getUser() + "." + CHILD_UNQUALIFIED;
        conn.close();

        /**
         * Remember, shape field has to be the last one
         */
        masterChildSql = "SELECT " + MASTER_UNQUALIFIED + ".ID, " + MASTER_UNQUALIFIED + ".NAME, "
                + CHILD_UNQUALIFIED + ".DESCRIPTION, " + MASTER_UNQUALIFIED + ".SHAPE " + "FROM "
                + MASTER_UNQUALIFIED + ", " + CHILD_UNQUALIFIED + " WHERE " + CHILD_UNQUALIFIED
                + ".MASTER_ID = " + MASTER_UNQUALIFIED + ".ID ORDER BY " + MASTER_UNQUALIFIED
                + ".ID";

        if (!tablesCreated) {
            createMasterTable();
            createChildTable();
            tablesCreated = true;
        }
    }

    /**
     * DOCUMENT ME!
     * 
     * @throws Exception
     *             DOCUMENT ME!
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        this.testData.tearDown(true, true);
        this.testData = null;
    }

    /*
     * public void testBorehole() throws Exception { final String typeName =
     * "JoinedBoreholes"; final String definitionQuery = "SELECT " + " B.QS,
     * B.NUMB, B.BSUFF, B.RT, B.BGS_ID, B.NAME, B.ORIGINAL_N, B.CONFIDENTI,
     * B.LENGTHC," + " G.LITHOSTRAT, G.LITHOLOGY_, G.BASE_BED_C, G.DRILLED_DE,
     * G.DRILLED__1, B.SHAPE" + " FROM SCO.LOUGHBOROUGH_BORES B,
     * SCO.LOUGHBOROUGH_BORE_GEOL G" + " WHERE (B.QS = G.QS AND B.NUMB = G.NUMB
     * AND B.BSUFF = G.BSUFF AND B.RT = G.RT)" + " ORDER BY B.QS, B.RT, B.NUMB,
     * B.BSUFF";
     * 
     * try { store.registerView(typeName, definitionQuery); } catch (Exception
     * e) { e.printStackTrace(); throw e; }
     * 
     * SimpleFeatureType type = (SimpleFeatureType) store.getSchema(typeName);
     * assertNotNull(type);
     * 
     * FeatureSource fs = store.getFeatureSource(typeName); assertNotNull(fs);
     * int count = fs.getCount(Query.ALL); final int expected = 16479;
     * assertEquals(expected, count); }
     */

    /**
     * Assert that the datastore complains on views with non supported features
     */
    public void testRegisterIllegalView() throws IOException {
        final String typeName = "badQuery";
        String plainSql;
        plainSql = "(SELECT * FROM mytable) UNION (SELECT * FROM mytable2 WHERE mytable2.col = 9)";
        try {
            store.registerView(typeName, plainSql);
            fail("should complain on union");
        } catch (UnsupportedOperationException e) {
            // OK
        }
        plainSql = "SELECT * FROM t1 INNER JOIN t2 ON t1.id = t2.parent_id";
        try {
            store.registerView(typeName, plainSql);
            fail("should complain on join");
        } catch (UnsupportedOperationException e) {
            // OK
        }
        plainSql = "SELECT f1,f2,f3 FROM t1 GROUP BY f1,f2";
        try {
            store.registerView(typeName, plainSql);
            fail("should complain on group by");
        } catch (UnsupportedOperationException e) {
            // OK
        }
        /*
         * Looks like jsqlparser is not parsing the INTO directive plainSql =
         * "SELECT f1,f2 INTO TEMP FROM t1"; try{ store.registerView(typeName,
         * plainSql); fail("should complain on into");
         * }catch(UnsupportedOperationException e){ //OK }
         */
        plainSql = "SELECT f1,f2,f3 FROM t1 LIMIT 10";
        try {
            store.registerView(typeName, plainSql);
            fail("should complain on limit");
        } catch (UnsupportedOperationException e) {
            // OK
        }
    }

    public void testRegisterView() throws IOException {
        final String plainSQL = "SELECT " + MASTER_UNQUALIFIED + ".*, " + CHILD_UNQUALIFIED
                + ".DESCRIPTION FROM " + MASTER_UNQUALIFIED + ", " + CHILD_UNQUALIFIED + " WHERE "
                + CHILD_UNQUALIFIED + ".MASTER_ID = " + MASTER_UNQUALIFIED + ".ID";

        final String typeName = "MasterChildTest";
        store.registerView(typeName, plainSQL);

        FeatureType type = store.getSchema(typeName);
        assertNotNull(type);

        assertEquals(typeName, type.getTypeName());

        assertEquals(4, type.getAttributeCount());
        List atts = Arrays.asList(type.getAttributeTypes());
        assertEquals(4, atts.size());
        AttributeType att1 = (AttributeType) atts.get(0);
        AttributeType att2 = (AttributeType) atts.get(1);
        AttributeType att3 = (AttributeType) atts.get(2);
        AttributeType att4 = (AttributeType) atts.get(3);

        assertEquals("ID", att1.getName());
        assertEquals("NAME", att2.getName());
        assertEquals("SHAPE", att3.getName());
        assertEquals("DESCRIPTION", att4.getName());

        assertEquals(Integer.class, att1.getType());
        assertEquals(String.class, att2.getType());
        assertEquals(Point.class, att3.getType());
        assertEquals(String.class, att4.getType());
    }

    public void testViewBounds() throws IOException {
        final String typeName = "MasterChildTest";
        store.registerView(typeName, masterChildSql);

        FeatureSource fs = store.getFeatureSource(typeName);
        assertNotNull(fs);
        Envelope bounds = fs.getBounds();
        assertNotNull(bounds);
        assertEquals(1D, bounds.getMinX(), 0);
        assertEquals(1D, bounds.getMinY(), 0);
        assertEquals(3D, bounds.getMaxX(), 0);
        assertEquals(3D, bounds.getMaxY(), 0);
    }

    public void testViewBoundsQuery() throws Exception {
        final String typeName = "MasterChildTest";
        store.registerView(typeName, masterChildSql);

        FeatureSource fs = store.getFeatureSource(typeName);
        assertNotNull(fs);

        String cqlQuery = "NAME='name2' OR DESCRIPTION='description4'";
        Filter filter = (Filter) ExpressionBuilder.parse(cqlQuery);
        DefaultQuery query = new DefaultQuery(typeName, filter);

        Envelope bounds = fs.getBounds(query);

        assertNotNull(bounds);
        assertEquals(2D, bounds.getMinX(), 0);
        assertEquals(2D, bounds.getMinY(), 0);
        assertEquals(3D, bounds.getMaxX(), 0);
        assertEquals(3D, bounds.getMaxY(), 0);
    }

    public void testViewCount() throws Exception {
        final String typeName = "MasterChildTest";
        store.registerView(typeName, masterChildSql);

        FeatureSource fs = store.getFeatureSource(typeName);
        assertNotNull(fs);
        int count = fs.getCount(Query.ALL);
        final int expected = 7;
        assertEquals(expected, count);
    }

    public void testViewCountQuery() throws Exception {
        final String typeName = "MasterChildTest";
        store.registerView(typeName, masterChildSql);

        FeatureSource fs = store.getFeatureSource(typeName);
        assertNotNull(fs);

        String cqlQuery = "NAME='name2' OR DESCRIPTION='description4'";
        Filter filter = (Filter) ExpressionBuilder.parse(cqlQuery);
        DefaultQuery query = new DefaultQuery(typeName, filter);

        int count = fs.getCount(query);
        final int expected = 3;
        assertEquals(expected, count);
    }

    public void testReadView() throws Exception {
        final String typeName = "MasterChildTest";
        store.registerView(typeName, masterChildSql);

        FeatureSource fs = store.getFeatureSource(typeName);

        DefaultQuery query = new DefaultQuery(typeName, Filter.INCLUDE, null);
        FeatureCollection fc = fs.getFeatures(query);
        int fcCount = fc.size();
        int itCount = 0;
        final int expectedCount = 7;
        Iterator it = fc.iterator();
        while (it.hasNext()) {
            Feature f = (Feature) it.next();
            assertNotNull(f);
            itCount++;
        }
        fc.close(it);
        assertEquals(expectedCount, fcCount);
        assertEquals(expectedCount, itCount);
    }

    public void testQueryView() throws Exception {
        final String typeName = "MasterChildTest";
        store.registerView(typeName, masterChildSql);

        String cqlQuery = "NAME='name2' OR DESCRIPTION='description6'";
        Filter filter = (Filter) ExpressionBuilder.parse(cqlQuery);
        DefaultQuery query = new DefaultQuery(typeName, filter);

        FeatureSource fs = store.getFeatureSource(typeName);
        FeatureCollection fc = fs.getFeatures(query);
        int fcCount = fc.size();
        int itCount = 0;
        final int expectedCount = 3;
        Iterator it = fc.iterator();
        while (it.hasNext()) {
            Feature f = (Feature) it.next();
            assertNotNull(f);
            itCount++;
        }
        assertEquals(expectedCount, fcCount);
        assertEquals(expectedCount, itCount);
    }

    /**
     * Meant as example to be sure we're using the ArcSDE java api correctly
     * 
     * @throws Exception
     */
    public void testApiOrderBy() throws Exception {
        ArcSDEPooledConnection conn = store.getConnectionPool().getConnection();

        SeSqlConstruct sqlConstruct = new SeSqlConstruct();
        String[] tables = { MASTER, CHILD };
        sqlConstruct.setTables(tables);
        String where = CHILD + ".MASTER_ID = " + MASTER + ".ID";
        sqlConstruct.setWhere(where);

        // tricky part is that SHAPE column must always be the last one
        String[] propertyNames = { "(SELECT AVG(ID) AS myid2 FROM " + CHILD + ") AS AVG",
                MASTER + ".NAME AS MNAME", CHILD + ".ID", CHILD + ".NAME", CHILD + ".DESCRIPTION",
                MASTER + ".SHAPE" };
        final int shapeIndex = 5;
        final int expectedCount = 7;

        SeQuery query = new SeQuery(conn);

        SeQueryInfo queryInfo = new SeQueryInfo();
        queryInfo.setConstruct(sqlConstruct);
        queryInfo.setColumns(propertyNames);
        queryInfo.setByClause(" ORDER BY " + CHILD + ".ID DESC");
        final int[] expectedShapeIndicators = { SeRow.SE_IS_NOT_NULL_VALUE, // child7
                SeRow.SE_IS_REPEATED_FEATURE, // child6
                SeRow.SE_IS_REPEATED_FEATURE, // child5
                SeRow.SE_IS_REPEATED_FEATURE, // child4
                SeRow.SE_IS_NOT_NULL_VALUE, // child3
                SeRow.SE_IS_REPEATED_FEATURE, // child2
                SeRow.SE_IS_NOT_NULL_VALUE // child1
        };
        try {
            query.prepareQueryInfo(queryInfo);
            query.execute();
            SeRow row = query.fetch();
            int count = 0;
            while (row != null) {
                // duplicate shapes are not returned by arcsde.
                // in that case indicator has the value
                // SeRow.SE_IS_REPEATED_FEATURE
                int indicator = row.getIndicator(shapeIndex);

                assertEquals("at index " + count, expectedShapeIndicators[count], indicator);

                if (SeRow.SE_IS_NOT_NULL_VALUE == indicator) {
                    Object shape = row.getObject(shapeIndex);
                    assertTrue(shape.getClass().getName(), shape instanceof SeShape);
                }

                count++;
                row = query.fetch();
            }
            assertEquals(expectedCount, count);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            conn.close();
        }
    }

    /**
     * Using table alias leads to ArcSDE returning SHAPE id instead of SHAPE
     * geometry.
     * 
     * @throws Exception
     */
    public void testApiAlias() throws Exception {
        ArcSDEPooledConnection conn = store.getConnectionPool().getConnection();

        SeSqlConstruct sqlConstruct = new SeSqlConstruct();
        String[] tables = { MASTER + " AS MASTER", CHILD + " AS CHILD" };
        sqlConstruct.setTables(tables);
        String where = "CHILD.MASTER_ID = MASTER.ID";
        sqlConstruct.setWhere(where);

        // tricky part is that SHAPE column must always be the last one
        String[] propertyNames = { "MASTER.ID", "CHILD.NAME", "MASTER.SHAPE" };

        final int shapeIndex = 2;
        final int expectedCount = 7;

        SeQuery query = new SeQuery(conn);

        SeQueryInfo queryInfo = new SeQueryInfo();
        queryInfo.setConstruct(sqlConstruct);
        queryInfo.setColumns(propertyNames);

        try {
            query.prepareQueryInfo(queryInfo);
            query.execute();
            SeRow row = query.fetch();
            int count = 0;
            while (row != null) {
                // we would expect SeShape being returned from shapeIndex, but
                // ArcSDE returns shape id
                if (SeRow.SE_IS_NOT_NULL_VALUE == row.getIndicator(shapeIndex)) {
                    Object shape = row.getObject(shapeIndex);
                    // assertTrue(shape.getClass().getName(), shape instanceof
                    // SeShape);
                    assertFalse(shape.getClass().getName(), shape instanceof SeShape);
                }
                count++;
                row = query.fetch();
            }
            assertEquals(expectedCount, count);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            conn.close();
        }
    }

    /**
     * Meant as example to be sure we're using the ArcSDE java api correctly
     * Nasty thing about group by is that is seems that we cannot include/use
     * the geometry column :(
     * 
     * @throws Exception
     */
    public void testApiGroupBy() throws Exception {
        ArcSDEPooledConnection conn = store.getConnectionPool().getConnection();

        SeSqlConstruct sqlConstruct = new SeSqlConstruct();
        String[] tables = { MASTER, CHILD };
        sqlConstruct.setTables(tables);
        String where = CHILD + ".MASTER_ID = " + MASTER + ".ID";
        sqlConstruct.setWhere(where);

        // tricky part is that SHAPE column must always be the last one
        String[] propertyNames = { MASTER + ".ID", CHILD + ".NAME" /*
                                                                     * , MASTER +
                                                                     * ".SHAPE"
                                                                     */
        };

        final int shapeIndex = 5;
        final int expectedCount = 6;

        SeQuery query = new SeQuery(conn);

        SeQueryInfo queryInfo = new SeQueryInfo();
        queryInfo.setConstruct(sqlConstruct);
        queryInfo.setColumns(propertyNames);

        String groupBy = MASTER + ".ID, " + CHILD + ".NAME, " + MASTER + ".SHAPE";

        queryInfo.setByClause(" GROUP BY " + groupBy + " ORDER BY " + CHILD + ".NAME DESC");

        final int[] expectedShapeIndicators = { SeRow.SE_IS_NOT_NULL_VALUE, // child6
                // (&&
                // child7)
                SeRow.SE_IS_REPEATED_FEATURE, // child5
                SeRow.SE_IS_REPEATED_FEATURE, // child4
                SeRow.SE_IS_NOT_NULL_VALUE, // child3
                SeRow.SE_IS_REPEATED_FEATURE, // child2
                SeRow.SE_IS_NOT_NULL_VALUE // child1
        };
        try {
            query.prepareQueryInfo(queryInfo);
            query.execute();
            SeRow row = query.fetch();
            int count = 0;
            while (row != null) {
                // duplicate shapes are not returned by arcsde.
                // in that case indicator has the value
                // SeRow.SE_IS_REPEATED_FEATURE
                // int indicator = row.getIndicator(shapeIndex);

                // assertEquals("at index " + count,
                // expectedShapeIndicators[count], indicator);

                count++;
                row = query.fetch();
            }
            assertEquals(expectedCount, count);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            conn.close();
        }
    }

    /**
     * Meant as example to be sure we're using the ArcSDE java api correctly. We
     * can execute a plain sql query, but shapes are not returned by ArcSDE.
     * Instead, the SHAPE field contains the SHAPE id, just like in the real
     * business table.
     * 
     * @throws Exception
     */
    public void testApiPlainSql() throws Exception {
        ArcSDEPooledConnection conn = store.getConnectionPool().getConnection();

        final SeQuery query = new SeQuery(conn);
        final String plainQuery = "SELECT " + MASTER + ".ID, " + MASTER + ".SHAPE, " + CHILD
                + ".NAME  FROM " + MASTER + " INNER JOIN " + CHILD + " ON " + CHILD
                + ".MASTER_ID = " + MASTER + ".ID";

        final int shapeIndex = 1;
        final int expectedCount = 7;
        try {
            query.prepareSql(plainQuery);
            query.execute();
            SeRow row = query.fetch();
            int count = 0;
            while (row != null) {
                Object shape = row.getObject(shapeIndex);
                assertTrue(shape instanceof Integer); // returns int instead
                // of shape
                count++;
                row = query.fetch();
            }
            assertEquals(expectedCount, count);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            conn.close();
        }
    }

    private void createMasterTable() throws Exception {
        ArcSDEConnectionPool connPool = store.getConnectionPool();
        ArcSDEPooledConnection conn = connPool.getConnection();

        SeTable table = new SeTable(conn, MASTER);

        try {
            table.delete();
        } catch (SeException e) {
            // no-op, table didn't existed
        }

        SeColumnDefinition[] colDefs = new SeColumnDefinition[2];

        SeLayer layer = new SeLayer(conn);
        layer.setTableName(MASTER);

        colDefs[0] = new SeColumnDefinition("ID", SeColumnDefinition.TYPE_INTEGER, 10, 0, false);
        colDefs[1] = new SeColumnDefinition("NAME", SeColumnDefinition.TYPE_STRING, 255, 0, false);

        table.create(colDefs, "DEFAULTS");

        layer.setSpatialColumnName("SHAPE");
        layer.setShapeTypes(SeLayer.SE_POINT_TYPE_MASK);
        layer.setGridSizes(1100.0, 0.0, 0.0);
        layer.setDescription("Geotools sde pluing join support testing master table");
        SeCoordinateReference coordref = new SeCoordinateReference();
        coordref.setCoordSysByDescription(testCrs.toWKT());
        layer.create(3, 4);

        insertMasterData(conn, layer);
        conn.close();
        LOGGER.info("successfully created master table " + layer.getQualifiedName());
    }

    private void createChildTable() throws Exception {
        ArcSDEConnectionPool connPool = store.getConnectionPool();
        ArcSDEPooledConnection conn = connPool.getConnection();

        SeTable table = new SeTable(conn, CHILD);
        try {
            table.delete();
        } catch (SeException e) {
            // no-op, table didn't existed
        }

        SeColumnDefinition[] colDefs = new SeColumnDefinition[4];

        colDefs[0] = new SeColumnDefinition("ID", SeColumnDefinition.TYPE_INTEGER, 10, 0, false);
        colDefs[1] = new SeColumnDefinition("MASTER_ID", SeColumnDefinition.TYPE_INTEGER, 10, 0,
                false);
        colDefs[2] = new SeColumnDefinition("NAME", SeColumnDefinition.TYPE_STRING, 255, 0, false);
        colDefs[3] = new SeColumnDefinition("DESCRIPTION", SeColumnDefinition.TYPE_STRING, 255, 0,
                false);

        table.create(colDefs, "DEFAULTS");

        /*
         * SeRegistration tableRegistration = new SeRegistration(conn, CHILD);
         * tableRegistration.setRowIdColumnType(SeRegistration.SE_REGISTRATION_ROW_ID_COLUMN_TYPE_USER);
         * tableRegistration.setRowIdColumnName("ID");
         * tableRegistration.alter();
         */
        insertChildData(conn, table);
        conn.close();

        LOGGER.info("successfully created child table " + CHILD);
    }

    /**
     * <pre>
     * <code>
     *  -----------------------------------------------
     *  |            GT_SDE_TEST_MASTER               |
     *  -----------------------------------------------
     *  |  ID(int)  | NAME (string)  | SHAPE (Point)  |
     *  -----------------------------------------------
     *  |     1     |   name1        |  POINT(1, 1)   |
     *  -----------------------------------------------
     *  |     2     |   name2        |  POINT(2, 2)   |
     *  -----------------------------------------------
     *  |     3     |   name3        |  POINT(3, 3)   |
     *  -----------------------------------------------
     * </code>
     * </pre>
     * 
     * @param conn
     * @throws Exception
     */
    private void insertMasterData(ArcSDEPooledConnection conn, SeLayer layer) throws Exception {
        SeInsert insert = null;

        SeCoordinateReference coordref = layer.getCoordRef();
        final String[] columns = { "ID", "NAME", "SHAPE" };

        for (int i = 1; i < 4; i++) {
            insert = new SeInsert(conn);
            insert.intoTable(layer.getName(), columns);
            insert.setWriteMode(true);

            SeRow row = insert.getRowToSet();
            SeShape shape = new SeShape(coordref);
            SDEPoint[] points = { new SDEPoint(i, i) };
            shape.generatePoint(1, points);

            row.setInteger(0, new Integer(i));
            row.setString(1, "name" + i);
            row.setShape(2, shape);
            insert.execute();
        }
        conn.commitTransaction();
    }

    /**
     * <pre>
     * <code>
     *  ---------------------------------------------------------------------
     *  |                     GT_SDE_TEST_CHILD                             |
     *  ---------------------------------------------------------------------
     *  | ID(int)   | MASTER_ID      | NAME (string)  | DESCRIPTION(string  |
     *  ---------------------------------------------------------------------
     *  |    1      |      1         |   child1       |    description1     |
     *  ---------------------------------------------------------------------
     *  |    2      |      2         |   child2       |    description2     |
     *  ---------------------------------------------------------------------
     *  |    3      |      2         |   child3       |    description3     |
     *  ---------------------------------------------------------------------
     *  |    4      |      3         |   child4       |    description4     |
     *  ---------------------------------------------------------------------
     *  |    5      |      3         |   child5       |    description5     |
     *  ---------------------------------------------------------------------
     *  |    6      |      3         |   child6       |    description6     |
     *  ---------------------------------------------------------------------
     *  |    7      |      3         |   child6       |    description7     | 
     *  ---------------------------------------------------------------------
     * </code>
     * </pre>
     * 
     * Note last row has the same name than child6, for testing group by.
     * 
     * @param conn
     * @param table
     * @throws Exception
     */
    private void insertChildData(ArcSDEPooledConnection conn, SeTable table) throws Exception {
        SeInsert insert = null;

        final String[] columns = { "ID", "MASTER_ID", "NAME", "DESCRIPTION" };

        int childId = 0;

        for (int master = 1; master < 4; master++) {
            for (int child = 0; child < master; child++) {
                childId++;

                insert = new SeInsert(conn);
                insert.intoTable(table.getName(), columns);
                insert.setWriteMode(true);

                SeRow row = insert.getRowToSet();

                row.setInteger(0, new Integer(childId));
                row.setInteger(1, new Integer(master));
                row.setString(2, "child" + (childId));
                row.setString(3, "description" + (childId));
                insert.execute();
            }
        }
        // add the 7th row to test group by
        SeRow row = insert.getRowToSet();

        row.setInteger(0, new Integer(7));
        row.setInteger(1, new Integer(3));
        row.setString(2, "child6");
        row.setString(3, "description7");
        insert.execute();

        conn.commitTransaction();
    }
}
