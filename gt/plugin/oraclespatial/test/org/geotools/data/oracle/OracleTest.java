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
package org.geotools.data.oracle;

import java.io.FileInputStream;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import oracle.jdbc.OracleConnection;

import org.geotools.data.DataSource;
import org.geotools.data.DataSourceMetaData;
import org.geotools.data.DefaultQuery;
import org.geotools.data.Query;
import org.geotools.data.jdbc.ConnectionPoolManager;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureType;
import org.geotools.filter.AbstractFilter;
import org.geotools.filter.CompareFilter;
import org.geotools.filter.Expression;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.GeometryFilter;
import org.geotools.filter.LikeFilter;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
/**
 * Tests the OracleDataSource. The oracle datasource does not have a publically available instance,
 * so the sql script in oraclespatial/tests/unit/testData named testData.sql needs to be run on
 * your oracle install.  test.properties in the same directory should be set to the correct
 * properties for your instance. Once you have set up your oracle database, ran the testData.sql
 * script  and set the test.properties, you should remove the exclude lines at the bottom of the
 * oraclespatial project.xml so that maven runs the test.
 *
 * @author geoghegs
 * @version $Revision: 1.10 $ Last Modified: $Date: 2003/11/05 00:52:35 $
 */
public class OracleTest extends TestCase {
    private OracleConnection conn;
    private FilterFactory filterFactory;
    private GeometryFactory jtsFactory;
    private Properties properties;
    private DataSource ds;

    public OracleTest(String testName) {
        super(testName);
    }

    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    public static Test suite() {
        return new TestSuite(OracleTest.class);
    }

    /**
     * Does nothing, to keep maven happy when all tests are commented out.
     */
    public void testDummy() {
    }

    public void setUp() throws Exception {
        properties = new Properties();
        properties.load(new FileInputStream("test.properties"));
        OracleDataSourceFactory dsFact = new OracleDataSourceFactory();
        OracleConnectionFactory conFact = new OracleConnectionFactory(properties.getProperty("host"),
                properties.getProperty("port"), properties.getProperty("instance"));
        conn = (OracleConnection) conFact.getConnectionPool(properties.getProperty("user"),
                properties.getProperty("passwd")).getConnection();
        ds = dsFact.createDataSource(properties);
        filterFactory = FilterFactory.createFilterFactory();
        jtsFactory = new GeometryFactory();        
    }

    public void tearDown() throws Exception {
        conn.close();
        ConnectionPoolManager manager = ConnectionPoolManager.getInstance();
        manager.closeAll();
    }

    public void testGetFeatures() throws Exception {
        FeatureCollection collection = ds.getFeatures(Query.ALL);
        assertEquals(5, collection.size());
    }

    public void testMaxFeatures() throws Exception {
        DefaultQuery query = new DefaultQuery();
        query.setTypeName("ORA_TEST_POINTS");
        query.setMaxFeatures(3);
        FeatureCollection collection = ds.getFeatures(query);
        assertEquals(3, collection.size());
    }

    public void testLikeGetFeatures() throws Exception {
        LikeFilter likeFilter = filterFactory.createLikeFilter();
        Expression pattern = filterFactory.createLiteralExpression("*");
        Expression attr = filterFactory.createAttributeExpression(null, "NAME");
        likeFilter.setPattern(pattern, "*", "?", "\\");
        likeFilter.setValue(attr);
        FeatureCollection collection = ds.getFeatures(likeFilter);
        assertEquals(5, collection.size());
        pattern = filterFactory.createLiteralExpression("*5");
        likeFilter.setPattern(pattern, "*", "?", "\\");
        collection = ds.getFeatures(likeFilter);
        assertEquals(1, collection.size());
    }

    public void testAttributeFilter() throws Exception {
        //OracleDataSource ds = new OracleDataSource(conn,"ORA_TEST_POINTS");
        CompareFilter attributeEquality = filterFactory.createCompareFilter(AbstractFilter.COMPARE_EQUALS);
        Expression attribute = filterFactory.createAttributeExpression(ds.getSchema(), "NAME");
        Expression literal = filterFactory.createLiteralExpression("point 1");
        attributeEquality.addLeftValue(attribute);
        attributeEquality.addRightValue(literal);
        FeatureCollection collection = ds.getFeatures(attributeEquality);
        assertEquals(1, collection.size());
    }

    public void testBBoxFilter() throws Exception {
        //OracleDataSource ds = new OracleDataSource(conn,"ORA_TEST_POINTS");
        GeometryFilter filter = filterFactory.createGeometryFilter(AbstractFilter.GEOMETRY_BBOX);
        Expression right = filterFactory.createBBoxExpression(new Envelope(-180, 180, -90, 90));
        Expression left = filterFactory.createAttributeExpression(ds.getSchema(), "SHAPE");
        filter.addLeftGeometry(left);
        filter.addRightGeometry(right);
        FeatureCollection collection = ds.getFeatures(filter);
        assertEquals(5, collection.size());
        right = filterFactory.createBBoxExpression(new Envelope(15, 35, 0, 15));
        filter.addRightGeometry(right);
        collection.clear();
        collection = ds.getFeatures(filter);
        assertEquals(2, collection.size());
    }

    public void testPointGeometryConversion() throws Exception {
        //OracleDataSource ds = new OracleDataSource(conn,"ORA_TEST_POINTS");
        CompareFilter filter = filterFactory.createCompareFilter(AbstractFilter.COMPARE_EQUALS);
        Expression left = filterFactory.createAttributeExpression(ds.getSchema(), "NAME");
        Expression right = filterFactory.createLiteralExpression("point 1");
        filter.addLeftValue(left);
        filter.addRightValue(right);
        FeatureCollection collection = ds.getFeatures(filter);
        assertEquals(1, collection.size());
        Feature feature = (Feature) collection.iterator().next();
        Geometry geom = feature.getDefaultGeometry();
        assertEquals(Point.class.getName(), geom.getClass().getName());
        Point point = (Point) geom;
        assertEquals(10.0, point.getX(), 0.001);
        assertEquals(10.0, point.getY(), 0.001);
    }

    public void testGetBBox() throws Exception {
        //OracleDataSource ds = new OracleDataSource(conn,"ORA_TEST_POINTS");
        Envelope expected = new Envelope(-180, 180, -90, 90);
        Envelope actual = ds.getBounds();
        assertEquals(expected, actual);
    }

    public void testAddFeatures() throws Exception {
        //OracleDataSource ds = new OracleDataSource(conn,"ORA_TEST_POINTS");
        String name = "add_name";
        BigDecimal intval = new BigDecimal(60);
        Point point = jtsFactory.createPoint(new Coordinate(-15.0, -25));
        Feature feature = ds.getSchema().create(new Object[] { name, intval, point });
        FeatureCollection fc = FeatureCollections.newCollection();
        fc.add(feature);
        Set fids = ds.addFeatures(fc);

        // check the fid
        assertEquals("ORA_TEST_POINTS.6", (fids.toArray())[0]);

        // Select is directly from the DB
        Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery("SELECT * FROM ORA_TEST_POINTS WHERE ID = 6");

        if (rs.next()) {
            assertEquals(rs.getString("NAME"), name);

            // remove the feature
            statement.executeUpdate("DELETE FROM ORA_TEST_POINTS WHERE ID = 6");
        } else {
            fail("Feature was not added correctly");
        }
    }

    public void testRemoveFeatures() throws Exception {
        //OracleDataSource ds = new OracleDataSource(conn,"ORA_TEST_POINTS");
        FeatureCollection initial = ds.getFeatures(Query.ALL);
        GeometryFilter filter = filterFactory.createGeometryFilter(AbstractFilter.GEOMETRY_BBOX);
        Expression right = filterFactory.createBBoxExpression(new Envelope(15, 35, 0, 15));
        Expression left = filterFactory.createAttributeExpression(ds.getSchema(), "SHAPE");
        filter.addLeftGeometry(left);
        filter.addRightGeometry(right);
        FeatureCollection toRemove = ds.getFeatures(filter);
        assertEquals(2, toRemove.size());
        assertEquals(5, initial.size());
        ds.removeFeatures(filter);
        FeatureCollection postRemove = ds.getFeatures(Query.ALL);
        assertEquals(initial.size() - toRemove.size(), postRemove.size());

        // put them back in
        Statement statement = conn.createStatement();
        statement.executeUpdate("INSERT INTO ORA_TEST_POINTS VALUES ('point 2',20,2," +
            "MDSYS.SDO_GEOMETRY(2001,NULL,NULL," + "MDSYS.SDO_ELEM_INFO_ARRAY(1,1,1)," +
            "MDSYS.SDO_ORDINATE_ARRAY(20,10) ) )");
        statement.executeUpdate("INSERT INTO ORA_TEST_POINTS VALUES ('point 4',40,4," +
            "MDSYS.SDO_GEOMETRY(2001,NULL,NULL," + "MDSYS.SDO_ELEM_INFO_ARRAY(1,1,1)," +
            "MDSYS.SDO_ORDINATE_ARRAY(30,10) ) )");

        // Now do a filter that is unsupported by the encoder
        LikeFilter likeFilter = filterFactory.createLikeFilter();
        likeFilter.setValue(filterFactory.createAttributeExpression(ds.getSchema(), "NAME"));
        likeFilter.setPattern(filterFactory.createLiteralExpression("*5"), "*", ".", "!");
        toRemove = ds.getFeatures(likeFilter);
        assertEquals(1, toRemove.size());
        ds.removeFeatures(likeFilter);
        postRemove = ds.getFeatures(Query.ALL);
        assertEquals(initial.size() - toRemove.size(), postRemove.size());
        statement.execute("INSERT INTO ora_test_points VALUES ('point 5',50,5," +
            "MDSYS.SDO_GEOMETRY(2001,NULL,NULL," + "MDSYS.SDO_ELEM_INFO_ARRAY(1,1,1)," +
            "MDSYS.SDO_ORDINATE_ARRAY(-20,10)))");

        //Filter andFilter = likeFilter.and(gf);        
    }

    public void testModifyFeatures() throws Exception {
        //OracleDataSource ds = new OracleDataSource(conn,"ORA_TEST_POINTS");
        GeometryFilter gf = filterFactory.createGeometryFilter(AbstractFilter.GEOMETRY_BBOX);
        Expression right = filterFactory.createBBoxExpression(new Envelope(15, 35, 0, 15));
        Expression left = filterFactory.createAttributeExpression(ds.getSchema(), "SHAPE");
        gf.addLeftGeometry(left);
        gf.addRightGeometry(right);
        ds.modifyFeatures(ds.getSchema().getAttributeType("NAME"), "modified", gf);
        FeatureCollection modFeatures = ds.getFeatures(gf);

        for (Iterator iter = modFeatures.iterator(); iter.hasNext();) {
            Feature modFeature = (Feature) iter.next();
            assertEquals("modified", modFeature.getAttribute("NAME"));
        }

        // reset them
        Statement statement = conn.createStatement();
        statement.executeUpdate("UPDATE ORA_TEST_POINTS SET NAME = 'point 2' WHERE ID = 2");
        statement.executeUpdate("UPDATE ORA_TEST_POINTS SET NAME = 'point 4' WHERE ID = 4");
    }

    public void testTransaction() throws Exception {
        FeatureCollection fc = ds.getFeatures();
        assertEquals(5, fc.size());

        ds.setAutoCommit(false);

        CompareFilter filter = filterFactory.createCompareFilter(AbstractFilter.COMPARE_EQUALS);
        filter.addLeftValue(filterFactory.createAttributeExpression(null, "NAME"));
        filter.addRightValue(filterFactory.createLiteralExpression("point 2"));
        ds.removeFeatures(filter);

        ds.rollback();
        ds.setAutoCommit(true);

        fc = ds.getFeatures();
        assertEquals(5, fc.size());

        // make sure getFeatures has original view of data mid transaction.
        ds.setAutoCommit(false);
        ds.removeFeatures(filter);
        fc = ds.getFeatures();
        assertEquals(5, fc.size());
        ds.rollback();
        fc = ds.getFeatures();
        assertEquals(5, fc.size());

        ds.setAutoCommit(false);
        ds.removeFeatures(filter);
        ds.commit();

        fc = ds.getFeatures();
        assertEquals(4, fc.size());

        // restore the state of the DB
        Statement statement = conn.createStatement();
        statement.executeUpdate("INSERT INTO ORA_TEST_POINTS VALUES ('point 2',20,2," +
            "MDSYS.SDO_GEOMETRY(2001,NULL,NULL," + "MDSYS.SDO_ELEM_INFO_ARRAY(1,1,1)," +
            "MDSYS.SDO_ORDINATE_ARRAY(20,10) ) )");
    }

    public void testMetaData() throws Exception {
        DataSourceMetaData meta = ds.getMetaData();
        assertTrue(meta.supportsGetBbox());
        assertTrue(meta.hasFastBbox());
        assertTrue(meta.supportsModify());
        assertTrue(meta.supportsSetFeatures());
        assertTrue(meta.supportsAdd());
        assertTrue(meta.supportsRemove());
        assertTrue(meta.supportsRollbacks());
        assertFalse(meta.supportsAbort());
    }

    public void testPropertySubset() throws Exception {
        DefaultQuery query = new DefaultQuery();
        query.setPropertyNames(new String[] { "NAME", "SHAPE" });
        FeatureCollection fc = ds.getFeatures(query);
        assertEquals(5, fc.size());

        FeatureIterator iter = fc.features();
        assertTrue(iter.hasNext());
        Feature feature = iter.next();
        FeatureType type = feature.getFeatureType();
        assertEquals(2, type.getAttributeCount());
    }
    
    
}
