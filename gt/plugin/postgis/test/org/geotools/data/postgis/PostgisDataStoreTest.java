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
package org.geotools.data.postgis;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Logger;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.geotools.data.DefaultQuery;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureResults;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Query;
import org.geotools.data.SchemaNotFoundException;
import org.geotools.data.Transaction;
import org.geotools.data.jdbc.ConnectionPool;
import org.geotools.data.jdbc.ConnectionPoolManager;
import org.geotools.data.jdbc.JDBCTransactionState;
import org.geotools.data.jdbc.fidmapper.BasicFIDMapper;
import org.geotools.data.jdbc.fidmapper.TypedFIDMapper;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.filter.AbstractFilter;
import org.geotools.filter.CompareFilter;
import org.geotools.filter.Expression;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.IllegalFilterException;
import org.geotools.filter.LiteralExpression;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;


/**
 * Test for postgis.  Uses a publically available instance of postgis.
 *
 * @author Chris Holmes, TOPP
 */
public class PostgisDataStoreTest extends TestCase {
    private static final boolean WKB_ENABLED = true;
    /** The logger for the filter module. */
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.postgis");
    private static String FEATURE_TABLE = "testset"; 
    private static String TEST_NS = "http://www.geotools.org/data/postgis";
    private static GeometryFactory geomFac = new GeometryFactory();
    private FilterFactory filterFac = FilterFactory.createFilterFactory();    
    private FeatureCollection collection = FeatureCollections.newCollection();
    private FeatureType schema;
    private int srid = -1;
    private PostgisConnectionFactory connFactory;
    private PostgisDataStore dstore;
    private ConnectionPool connPool;
    private CompareFilter tFilter;
    private int addId = 32;
    private org.geotools.filter.GeometryFilter geomFilter;

    public PostgisDataStoreTest(String testName) {
        super(testName);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        LOGGER.info("starting suite...");

        TestSuite suite = new TestSuite(PostgisDataStoreTest.class);
        LOGGER.info("made suite...");

        return suite;
    }

    public void setUp() {
        //LOGGER.info("creating postgis connection...");
        connFactory = new PostgisConnectionFactory("feathers.leeds.ac.uk",
                "5432", "postgis_test");

        //connFactory = new PostgisConnectionFactory("localhost", "5432", 
        //   "testdb");
        //LOGGER.info("created new db connection");
        //connFactory.setLogin("postgres", "postgres");
        connFactory.setLogin("postgis_ro", "postgis_ro");

        //LOGGER.info("set the login");
        //LOGGER.info("created new datasource");
        try {
            // LOGGER.fine("getting connection pool");
            connPool = connFactory.getConnectionPool();
            dstore = new PostgisDataStore(connPool, TEST_NS);
            dstore.setWKBEnabled(WKB_ENABLED);
            
	   		dstore.setFIDMapper("testset", new TypedFIDMapper(new BasicFIDMapper("gid", 255, true), "testset"));

            //LOGGER.fine("about to create ds");
            //postgis = new PostgisDataSource(connection, FEATURE_TABLE);
            //LOGGER.fine("created de");
            schema = dstore.getSchema(FEATURE_TABLE);
        } catch (Exception e) {
            LOGGER.info("exception while making schema" + e.getMessage());
        }
    }

    protected void tearDown() {
        ConnectionPoolManager.getInstance().closeAll();
    }

    //todo assert on schema.
    public void testFeatureTypes() throws Exception {
        String[] types = dstore.getTypeNames();
        FeatureType schema1 = dstore.getSchema(types[0]);

        //FeatureType schema2 = dstore.getSchema(types[1]);
        //need to figure out spatial_ref_system and geometry_columns
        LOGGER.fine("first schemas are: \n" + schema1); // + "\n" + schema2);

        try {
            String badSchema = "bad-schema23";
            dstore.getSchema(badSchema);
            fail("should not have schema " + badSchema);
        } catch (SchemaNotFoundException e) {
            LOGGER.fine("succesfully caught exception: " + e);

            //catch the proper exception
        }
        
        // make sure the CRS is specified
        assertNotNull(schema1.getDefaultGeometry().getCoordinateSystem());
    }

    //tests todo: bad retyping. post filters. 
    public void testGetReader() throws Exception {
        String testTable = FEATURE_TABLE;
        LOGGER.fine("testTable " + testTable + " has schema "
            + dstore.getSchema(testTable));

        FeatureReader reader = dstore.getFeatureReader(schema, Filter.NONE,
                Transaction.AUTO_COMMIT);
        int numFeatures = count(reader);
        assertEquals("Number of features off:", 6, numFeatures);
    }

    public void testFilter() throws Exception {
        CompareFilter test1 = null;

        try {
            test1 = filterFac.createCompareFilter(AbstractFilter.COMPARE_EQUALS);

            Integer testInt = new Integer(0);
            Expression testLiteral = filterFac.createLiteralExpression(testInt);
            test1.addLeftValue(testLiteral);
            test1.addRightValue(filterFac.createAttributeExpression(schema,
                    "pcedflag"));
        } catch (IllegalFilterException e) {
            fail("Illegal Filter Exception " + e);
        }

        Query query = new DefaultQuery(FEATURE_TABLE, test1);
        FeatureReader reader = dstore.getFeatureReader(schema, test1,
                Transaction.AUTO_COMMIT);
        assertEquals("Number of filtered features off:", 2, count(reader));
    }

    public void testGeomFilter() throws Exception {
        org.geotools.filter.GeometryFilter gf = filterFac.createGeometryFilter(AbstractFilter.GEOMETRY_BBOX);
        Envelope env = new Envelope(428500, 430000, 428500, 440000);
        LiteralExpression right = filterFac.createBBoxExpression(env);
        gf.addRightGeometry(right);
        gf.addLeftGeometry(filterFac.createAttributeExpression(schema,
                "the_geom"));

        FeatureReader reader = dstore.getFeatureReader(schema, gf,
                Transaction.AUTO_COMMIT);
        assertEquals("Number of geom filtered features off:", 2, count(reader));
    }

    int count(FeatureReader reader)
        throws NoSuchElementException, IOException, IllegalAttributeException {
        int count = 0;

        try {
            while (reader.hasNext()) {
                reader.next();
                count++;
            }
        } finally {
            reader.close();
        }

        return count;
    }

    int count(FeatureWriter writer)
        throws NoSuchElementException, IOException, IllegalAttributeException {
        int count = 0;

        try {
            while (writer.hasNext()) {
                writer.next();
                count++;
            }
        } finally {
            writer.close();
        }

        return count;
    }

    public void testGetFeatureWriter() throws Exception {
        Transaction trans = new DefaultTransaction();
        JDBCTransactionState state = new JDBCTransactionState(connPool);
        trans.putState(connPool, state);

        FeatureWriter writer = dstore.getFeatureWriter("testset", Filter.NONE,
                trans);

        //count(writer);
        assertEquals(6, count(writer));

        try {
            assertFalse(writer.hasNext());
        } catch (IOException expected) {
        }

        //TODO: test that writer.next is an empty feature.
        //try {
        //    writer.next();
        //    fail("Should not be able to use a closed writer");
        //} catch (IOException expected) {
        //}
    }

    public void testBadTypeName() throws Exception {
        try {
            String badType = "badType43";
            FeatureWriter writer = dstore.getFeatureWriter(badType,
                    Filter.NONE, Transaction.AUTO_COMMIT);
            fail("should not have type " + badType);
        } catch (SchemaNotFoundException e) {
            LOGGER.fine("succesfully caught exception: " + e);

            //catch the proper exception
        }
    }

    public void testOptimizedBounds() throws Exception {
        FeatureSource source = dstore.getFeatureSource(FEATURE_TABLE);
        CompareFilter test1 = null;

        try {
            test1 = filterFac.createCompareFilter(AbstractFilter.COMPARE_EQUALS);

            Integer testInt = new Integer(0);
            Expression testLiteral = filterFac.createLiteralExpression(testInt);
            test1.addLeftValue(testLiteral);
            test1.addRightValue(filterFac.createAttributeExpression(schema,
                    "pcedflag"));
        } catch (IllegalFilterException e) {
            fail("Illegal Filter Exception " + e);
        }

        Query query = new DefaultQuery(FEATURE_TABLE, test1);
        Envelope bounds = source.getBounds(query);
        LOGGER.info("bounds on query " + query + " is " + bounds);

        Envelope fBounds = source.getBounds();
        LOGGER.info("Bounds of source is " + fBounds);

        FeatureResults results = source.getFeatures(query);
        LOGGER.info("bounds from feature results is " + results.getBounds());
    }

    public void testGetFeaturesWriterModify()
        throws IOException, IllegalAttributeException {
        Transaction trans = new DefaultTransaction();
        JDBCTransactionState state = new JDBCTransactionState(connPool);
        trans.putState(connPool, state);

        FeatureWriter writer = dstore.getFeatureWriter(FEATURE_TABLE,
                Filter.NONE, trans);
        int attKeyPos = 0;
        Integer attKey = new Integer(10);
        String attName = "name";
        String newAttVal = "LS 503";
        Feature feature;

        while (writer.hasNext()) {
            feature = writer.next();

            if (feature.getAttribute(attKeyPos).equals(attKey)) {
                LOGGER.info("changing name of feature " + feature);
                ;
                feature.setAttribute(attName, newAttVal);
                writer.write();
            }
        }

        //writer.close();
        FeatureReader reader = dstore.getFeatureReader(schema, Filter.NONE,
                trans);

        while (reader.hasNext()) {
            feature = reader.next();

            if (feature.getAttribute(attKeyPos).equals(attKey)) {
                LOGGER.fine("checking feature " + feature);
                ;

                Object modAtt = feature.getAttribute(attName);

                //LOGGER.fine("modified attribute is " + modAtt);
                assertEquals("attribute was not changed", newAttVal,
                    (String) modAtt);
            }
        }

        //feature = (Feature) data.features( "road" ).get( "road.rd1" );
        //assertEquals( "changed", feature.getAttribute("name") );
        state.rollback();
    }

    public void testGetFeaturesWriterModifyGeometry()
        throws IOException, IllegalAttributeException {
        FeatureWriter writer = dstore.getFeatureWriter("road", Filter.NONE,
                Transaction.AUTO_COMMIT);
        Feature feature;
        Coordinate[] points = {
            new Coordinate(59, 59), new Coordinate(17, 17),
            new Coordinate(49, 39), new Coordinate(57, 67),
            new Coordinate(79, 79)
        };
        LineString geom = geomFac.createLineString(points);

        while (writer.hasNext()) {
            feature = writer.next();
            LOGGER.info("looking at feature " + feature);

            if (feature.getAttribute(0).equals("asphalt")) {
                LOGGER.info("changing name and geom");
                feature.setAttribute("the_geom", geom);
                writer.write();
            }
        }

        //feature = (Feature) data.features( "road" ).get( "road.rd1" );
        //assertEquals( "changed", feature.getAttribute("name") );
        writer.close();
    }

    public void testGetFeaturesWriterModifyMultipleAtts()
        throws IOException, IllegalAttributeException {
        FeatureWriter writer = dstore.getFeatureWriter("road", Filter.NONE,
                Transaction.AUTO_COMMIT);
        Feature feature;
        Coordinate[] points = {
            new Coordinate(32, 44), new Coordinate(62, 51),
            new Coordinate(45, 35), new Coordinate(55, 65),
            new Coordinate(73, 75)
        };
        LineString geom = geomFac.createLineString(points);

        while (writer.hasNext()) {
            feature = writer.next();
            LOGGER.info("looking at feature " + feature);

            if (feature.getAttribute(0).equals("asphalt")) {
                LOGGER.info("changing name and geom");
                feature.setAttribute("the_geom", geom);
                feature.setAttribute("name", "trick");
                writer.write();
            }
        }

        //feature = (Feature) data.features( "road" ).get( "road.rd1" );
        //assertEquals( "changed", feature.getAttribute("name") );
        writer.close();
    }

    public void testGetFeaturesWriterAdd()
        throws IOException, IllegalAttributeException {
        Transaction trans = new DefaultTransaction();
        JDBCTransactionState state = new JDBCTransactionState(connPool);
        trans.putState(connPool, state);

        FeatureWriter writer = dstore.getFeatureWriter(FEATURE_TABLE,
                Filter.NONE, trans);
        int count = 0;

        while (writer.hasNext()) {
            Feature feature = writer.next();
            count++;
        }

        assertEquals("Checking num features before add", 6, count);
        assertFalse(writer.hasNext());

        Feature feature = writer.next();
        Object[] atts = getTestAtts("testAdd");
        feature.setAttributes(atts);
        writer.write();
        assertFalse(writer.hasNext());

        //assertEquals( fixture.roadFeatures.length+1, data.features( "road" ).size() );
        writer.close();

        FeatureReader reader = dstore.getFeatureReader(schema, Filter.NONE,
                trans);
        int numFeatures = count(reader);
        assertEquals("Wrong number of features after add", 7, numFeatures);
        state.rollback();
    }

    private Object[] getTestAtts(String name) {
        Coordinate[] points = {
            new Coordinate(45, 45), new Coordinate(45, 55),
            new Coordinate(55, 55), new Coordinate(55, 45),
            new Coordinate(45, 45)
        };
        GeometryFactory gf = new GeometryFactory();
        LinearRing shell = gf.createLinearRing(points);
        Polygon[] testPolys = { gf.createPolygon(shell,null) };
        MultiPolygon the_geom = gf.createMultiPolygon(testPolys);
        Integer gID = new Integer(addId);
        Double area = new Double(100.0);
        Double perimeter = new Double(40.0);
        Integer testb_ = new Integer(22);
        Integer testb_id = new Integer(4833);
        Integer code = new Integer(0);

        Object[] attributes = {
            gID, area, perimeter, testb_, testb_id, name, code, code, the_geom
        };

        return attributes;
    }

    public void testGetFeatureWriterRemove()
        throws IOException, IllegalAttributeException {
        Transaction trans = new DefaultTransaction();
        JDBCTransactionState state = new JDBCTransactionState(connPool);
        trans.putState(connPool, state);

        FeatureWriter writer = dstore.getFeatureWriter(FEATURE_TABLE,
                Filter.NONE, trans);

        FeatureReader reader = dstore.getFeatureReader(schema, Filter.NONE,
                trans);
        int numFeatures = count(reader);

        //assertEquals("Wrong number of features before delete", 6, numFeatures);
        Feature feature;

        while (writer.hasNext()) {
            feature = writer.next();

            if (feature.getAttribute(0).equals(new Integer(4))) {
                LOGGER.info("deleting feature " + feature);
                writer.remove();
            }
        }

        writer.close();
        reader = dstore.getFeatureReader(schema, Filter.NONE, trans);
        numFeatures = count(reader);
        assertEquals("Wrong number of features after add", 5, numFeatures);
        state.rollback();
    }

    public void testFIDCreation() throws Exception{
        
        FeatureStore store=(FeatureStore) dstore.getFeatureSource(dstore.getTypeNames()[0]);
        final FeatureType type=store.getSchema();
        Object[] attrs=new Object[type.getAttributeCount()];
        for( int i = 0; i < attrs.length; i++ ) {
            attrs[i]=type.getAttributeType(i).createDefaultValue();
        }
        final Feature feature=type.create(attrs);
        Class geomType=type.getDefaultGeometry().getType();
        
        GeometryFactory f=new GeometryFactory();
        LinearRing ring=f.createLinearRing(new Coordinate[]{new Coordinate(10,10),new Coordinate(10,10),new Coordinate(10,10),new Coordinate(10,10)});
        Polygon poly=f.createPolygon(ring, new LinearRing[]{});
        Geometry geom=f.createMultiPolygon(new Polygon[]{poly});
        feature.setDefaultGeometry(geom);
        String fid=feature.getID();
        Set fids = store.addFeatures(new FeatureReader(){

            boolean more = true;

            public FeatureType getFeatureType() {
                return feature.getFeatureType();
            }

            public Feature next() throws NoSuchElementException {
                more = false;
                return feature;
            }

            public boolean hasNext() {
                return more;
            }

            public void close() {
                // do nothing
            }

        });
        FeatureReader reader = store
                .getFeatures(
                        FilterFactory.createFilterFactory().createFidFilter(
                                (String) fids.iterator().next())).reader();
        try {
            Feature f2 = reader.next();
            assertNotNull(f);
        } finally {
            if (reader != null)
                reader.close();
        }
    }
    
    //assertEquals( fixture.roadFeatures.length-1, data.features( "road" ).size() );
}
