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
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectBody;

import org.geotools.arcsde.ArcSdeException;
import org.geotools.arcsde.pool.ArcSDEPooledConnection;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.feature.FeatureCollection;
import org.geotools.filter.text.cql2.CQL;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.Filter;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;

import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeQuery;
import com.esri.sde.sdk.client.SeQueryInfo;
import com.esri.sde.sdk.client.SeRow;
import com.esri.sde.sdk.client.SeShape;
import com.esri.sde.sdk.client.SeSqlConstruct;
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
 *  &#064;author Gabriel Roldan, Axios Engineering
 *  &#064;source $URL$
 *  &#064;version $Id$
 *  &#064;since 2.3.x
 * 
 */
public class SDEJavaApiJoinTest extends TestCase {
    /** package logger */
    private static Logger LOGGER = org.geotools.util.logging.Logging
            .getLogger(SDEJavaApiJoinTest.class.getPackage().getName());

    /** Helper class that provides config loading and test data for unit tests */
    private static TestData testData;

    /** an ArcSDEDataStore created on setUp() to run tests against */
    private ArcSDEDataStore store;

    /**
     * Builds a test suite for all this class' tests with per suite
     * initialization directed to {@link #oneTimeSetUp()} and per suite clean up
     * directed to {@link #oneTimeTearDown()}
     * 
     * @return
     */
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(SDEJavaApiJoinTest.class);

        TestSetup wrapper = new TestSetup(suite) {
            protected void setUp() throws IOException, SeException, NoSuchAuthorityCodeException,
                    FactoryException {
                oneTimeSetUp();
            }

            protected void tearDown() {
                oneTimeTearDown();
            }
        };
        return wrapper;
    }

    /**
     * Initialization code for the whole test suite
     * 
     * @throws IOException
     * @throws SeException
     * @throws FactoryException
     * @throws NoSuchAuthorityCodeException
     */
    public static void oneTimeSetUp() throws IOException, SeException,
            NoSuchAuthorityCodeException, FactoryException {
        testData = new TestData();
        testData.setUp();

        ArcSDEPooledConnection conn = testData.getConnectionPool().getConnection();
        try {
            InProcessViewSupportTestData.setUp(conn);
        } finally {
            conn.close();
        }
    }

    /**
     * Tear down code for the whole suite
     */
    public static void oneTimeTearDown() {
        final boolean cleanTestTable = true;
        final boolean cleanPool = true;
        testData.tearDown(cleanTestTable, cleanPool);
        testData = null;
    }

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
        if (testData == null) {
            oneTimeSetUp();
        }
        this.store = testData.getDataStore();
    }

    /**
     * DOCUMENT ME!
     * 
     * @throws Exception
     *             DOCUMENT ME!
     */
    protected void tearDown() throws Exception {
        super.tearDown();
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
        SelectBody select;
        try {
            select = ViewRegisteringFactoryHelper.parseSqlQuery(plainSql);
            fail("should complain on union");
        } catch (UnsupportedOperationException e) {
            // OK
        }
        plainSql = "SELECT * FROM t1 INNER JOIN t2 ON t1.id = t2.parent_id";
        try {
            select = ViewRegisteringFactoryHelper.parseSqlQuery(plainSql);
            store.registerView(typeName, (PlainSelect) select);
            fail("should complain on join");
        } catch (UnsupportedOperationException e) {
            // OK
        }
        plainSql = "SELECT f1,f2,f3 FROM t1 GROUP BY f1,f2";
        try {
            select = ViewRegisteringFactoryHelper.parseSqlQuery(plainSql);
            store.registerView(typeName, (PlainSelect) select);
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
            select = ViewRegisteringFactoryHelper.parseSqlQuery(plainSql);
            store.registerView(typeName, (PlainSelect) select);
            fail("should complain on limit");
        } catch (UnsupportedOperationException e) {
            // OK
        }
    }

    /**
     * Fail if tried to register the same view name more than once
     */
    public void testRegisterDuplicateViewName() throws IOException {
        final String plainSQL = InProcessViewSupportTestData.masterChildSql;

        SelectBody select = ViewRegisteringFactoryHelper.parseSqlQuery(plainSQL);
        store.registerView(InProcessViewSupportTestData.typeName, (PlainSelect) select);
        try {
            store.registerView(InProcessViewSupportTestData.typeName, (PlainSelect) select);
            fail("Expected IAE on duplicate view name");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    public void testRegisterViewListedInGetTypeNames() throws IOException {
        final String plainSQL = InProcessViewSupportTestData.masterChildSql;

        SelectBody select = ViewRegisteringFactoryHelper.parseSqlQuery(plainSQL);
        store.registerView(InProcessViewSupportTestData.typeName, (PlainSelect) select);

        List publishedTypeNames = Arrays.asList(store.getTypeNames());
        assertTrue(publishedTypeNames.contains(InProcessViewSupportTestData.typeName));
    }

    public void testRegisterViewBuildsCorrectFeatureType() throws IOException {
        final String plainSQL = "SELECT " + InProcessViewSupportTestData.MASTER_UNQUALIFIED
                + ".*, " + InProcessViewSupportTestData.CHILD_UNQUALIFIED + ".DESCRIPTION FROM "
                + InProcessViewSupportTestData.MASTER_UNQUALIFIED + ", "
                + InProcessViewSupportTestData.CHILD_UNQUALIFIED + " WHERE "
                + InProcessViewSupportTestData.CHILD_UNQUALIFIED + ".MASTER_ID = "
                + InProcessViewSupportTestData.MASTER_UNQUALIFIED + ".ID";

        SelectBody select = ViewRegisteringFactoryHelper.parseSqlQuery(plainSQL);
        store.registerView(InProcessViewSupportTestData.typeName, (PlainSelect) select);

        SimpleFeatureType type = store.getSchema(InProcessViewSupportTestData.typeName);
        assertNotNull(type);

        assertEquals(InProcessViewSupportTestData.typeName, type.getTypeName());

        assertEquals(4, type.getAttributeCount());
        List atts = type.getAttributes();
        assertEquals(4, atts.size());
        AttributeDescriptor att1 = (AttributeDescriptor) atts.get(0);
        AttributeDescriptor att2 = (AttributeDescriptor) atts.get(1);
        AttributeDescriptor att3 = (AttributeDescriptor) atts.get(2);
        AttributeDescriptor att4 = (AttributeDescriptor) atts.get(3);

        assertEquals("ID", att1.getLocalName());
        assertEquals("NAME", att2.getLocalName());
        assertEquals("SHAPE", att3.getLocalName());
        assertEquals("DESCRIPTION", att4.getLocalName());

        assertEquals(Integer.class, att1.getType().getBinding());
        assertEquals(String.class, att2.getType().getBinding());
        assertEquals(Point.class, att3.getType().getBinding());
        assertEquals(String.class, att4.getType().getBinding());
    }

    public void testViewBounds() throws IOException {
        SelectBody select = ViewRegisteringFactoryHelper
                .parseSqlQuery(InProcessViewSupportTestData.masterChildSql);
        store.registerView(InProcessViewSupportTestData.typeName, (PlainSelect) select);

        FeatureSource fs = store.getFeatureSource(InProcessViewSupportTestData.typeName);
        assertNotNull(fs);
        Envelope bounds = fs.getBounds();
        assertNotNull(bounds);
        assertEquals(1D, bounds.getMinX(), 0);
        assertEquals(1D, bounds.getMinY(), 0);
        assertEquals(3D, bounds.getMaxX(), 0);
        assertEquals(3D, bounds.getMaxY(), 0);
    }

    public void testViewBoundsQuery() throws Exception {
        SelectBody select = ViewRegisteringFactoryHelper
                .parseSqlQuery(InProcessViewSupportTestData.masterChildSql);
        store.registerView(InProcessViewSupportTestData.typeName, (PlainSelect) select);

        FeatureSource fs = store.getFeatureSource(InProcessViewSupportTestData.typeName);
        assertNotNull(fs);

        String cqlQuery = "NAME='name2' OR DESCRIPTION='description4'";
        Filter filter = (Filter) CQL.toFilter(cqlQuery);
        DefaultQuery query = new DefaultQuery(InProcessViewSupportTestData.typeName, filter);

        Envelope bounds = fs.getBounds(query);

        assertNotNull(bounds);
        assertEquals(2D, bounds.getMinX(), 0);
        assertEquals(2D, bounds.getMinY(), 0);
        assertEquals(3D, bounds.getMaxX(), 0);
        assertEquals(3D, bounds.getMaxY(), 0);
    }

    public void testViewCount() throws Exception {
        SelectBody select = ViewRegisteringFactoryHelper
                .parseSqlQuery(InProcessViewSupportTestData.masterChildSql);
        store.registerView(InProcessViewSupportTestData.typeName, (PlainSelect) select);

        FeatureSource fs = store.getFeatureSource(InProcessViewSupportTestData.typeName);
        assertNotNull(fs);
        int count = fs.getCount(Query.ALL);
        final int expected = 7;
        assertEquals(expected, count);
    }

    public void testViewCountQuery() throws Exception {
        SelectBody select = ViewRegisteringFactoryHelper
                .parseSqlQuery(InProcessViewSupportTestData.masterChildSql);
        store.registerView(InProcessViewSupportTestData.typeName, (PlainSelect) select);

        FeatureSource fs = store.getFeatureSource(InProcessViewSupportTestData.typeName);
        assertNotNull(fs);

        String cqlQuery = "NAME='name2' OR DESCRIPTION='description4'";
        Filter filter = (Filter) CQL.toFilter(cqlQuery);
        DefaultQuery query = new DefaultQuery(InProcessViewSupportTestData.typeName, filter);

        int count = fs.getCount(query);
        final int expected = 3;
        assertEquals(expected, count);
    }

    public void testReadView() throws Exception {
        SelectBody select = ViewRegisteringFactoryHelper
                .parseSqlQuery(InProcessViewSupportTestData.masterChildSql);
        store.registerView(InProcessViewSupportTestData.typeName, (PlainSelect) select);

        FeatureSource fs = store.getFeatureSource(InProcessViewSupportTestData.typeName);

        DefaultQuery query = new DefaultQuery(InProcessViewSupportTestData.typeName,
                Filter.INCLUDE, null);
        FeatureCollection fc = fs.getFeatures(query);
        int fcCount = fc.size();
        int itCount = 0;
        final int expectedCount = 7;
        Iterator it = fc.iterator();
        while (it.hasNext()) {
            SimpleFeature f = (SimpleFeature) it.next();
            assertNotNull(f);
            itCount++;
        }
        fc.close(it);
        assertEquals(expectedCount, fcCount);
        assertEquals(expectedCount, itCount);
    }

    public void testQueryView() throws Exception {
        SelectBody select = ViewRegisteringFactoryHelper
                .parseSqlQuery(InProcessViewSupportTestData.masterChildSql);
        store.registerView(InProcessViewSupportTestData.typeName, (PlainSelect) select);

        String cqlQuery = "NAME='name2' OR DESCRIPTION='description6'";
        Filter filter = (Filter) CQL.toFilter(cqlQuery);
        DefaultQuery query = new DefaultQuery(InProcessViewSupportTestData.typeName, filter);

        FeatureSource fs = store.getFeatureSource(InProcessViewSupportTestData.typeName);
        FeatureCollection fc = fs.getFeatures(query);
        int fcCount = fc.size();
        int itCount = 0;
        final int expectedCount = 3;
        Iterator it = fc.iterator();
        while (it.hasNext()) {
            SimpleFeature f = (SimpleFeature) it.next();
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
        String[] tables = { InProcessViewSupportTestData.MASTER, InProcessViewSupportTestData.CHILD };
        sqlConstruct.setTables(tables);
        String where = InProcessViewSupportTestData.CHILD + ".MASTER_ID = "
                + InProcessViewSupportTestData.MASTER + ".ID";
        sqlConstruct.setWhere(where);

        // tricky part is that SHAPE column must always be the last one
        String[] propertyNames = {
                "(SELECT AVG(ID) AS myid2 FROM " + InProcessViewSupportTestData.CHILD + ") AS AVG",
                InProcessViewSupportTestData.MASTER + ".NAME AS MNAME",
                InProcessViewSupportTestData.CHILD + ".ID",
                InProcessViewSupportTestData.CHILD + ".NAME",
                InProcessViewSupportTestData.CHILD + ".DESCRIPTION",
                InProcessViewSupportTestData.MASTER + ".SHAPE" };
        final int shapeIndex = 5;
        final int expectedCount = 7;

        SeQuery query = new SeQuery(conn);

        SeQueryInfo queryInfo = new SeQueryInfo();
        queryInfo.setConstruct(sqlConstruct);
        queryInfo.setColumns(propertyNames);
        queryInfo.setByClause(" ORDER BY " + InProcessViewSupportTestData.CHILD + ".ID DESC");
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
        } catch (SeException e) {
            LOGGER.log(Level.SEVERE, "", new ArcSdeException(e));
            throw e;
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
        String[] tables = { InProcessViewSupportTestData.MASTER + " AS MASTER",
                InProcessViewSupportTestData.CHILD + " AS CHILD" };
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
        String[] tables = { InProcessViewSupportTestData.MASTER, InProcessViewSupportTestData.CHILD };
        sqlConstruct.setTables(tables);
        String where = InProcessViewSupportTestData.CHILD + ".MASTER_ID = "
                + InProcessViewSupportTestData.MASTER + ".ID";
        sqlConstruct.setWhere(where);

        // tricky part is that SHAPE column must always be the last one
        String[] propertyNames = { InProcessViewSupportTestData.MASTER + ".ID",
                InProcessViewSupportTestData.CHILD + ".NAME" /*
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

        String groupBy = InProcessViewSupportTestData.MASTER + ".ID, "
                + InProcessViewSupportTestData.CHILD + ".NAME, "
                + InProcessViewSupportTestData.MASTER + ".SHAPE";

        queryInfo.setByClause(" GROUP BY " + groupBy + " ORDER BY "
                + InProcessViewSupportTestData.CHILD + ".NAME DESC");

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
        final String plainQuery = "SELECT " + InProcessViewSupportTestData.MASTER + ".ID, "
                + InProcessViewSupportTestData.MASTER + ".SHAPE, "
                + InProcessViewSupportTestData.CHILD + ".NAME  FROM "
                + InProcessViewSupportTestData.MASTER + " INNER JOIN "
                + InProcessViewSupportTestData.CHILD + " ON " + InProcessViewSupportTestData.CHILD
                + ".MASTER_ID = " + InProcessViewSupportTestData.MASTER + ".ID";

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

}
