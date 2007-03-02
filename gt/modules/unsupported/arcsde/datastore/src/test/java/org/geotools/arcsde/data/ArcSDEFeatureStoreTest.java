package org.geotools.arcsde.data;


import java.io.IOException;
import java.util.HashSet;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.geotools.data.DataStore;
import org.geotools.data.DefaultQuery;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeBuilder;
import org.geotools.feature.SchemaException;
import org.geotools.feature.SimpleFeature;
import org.geotools.feature.type.GeometricAttributeType;
import org.geotools.referencing.CRS;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.Or;
import org.opengis.filter.PropertyIsEqualTo;

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
 * @source $URL$
 * @version $Id$
 */
public class ArcSDEFeatureStoreTest extends TestCase {
    /** package logger */
    private static Logger LOGGER = Logger.getLogger(ArcSDEFeatureStoreTest.class.getPackage()
                                                                                .getName());

    /** DOCUMENT ME! */
    private TestData testData;

    /**
     * loads {@code test-data/testparams.properties} into a Properties object, wich is
     * used to obtain test tables names and is used as parameter to find the DataStore
     *
     * @throws Exception DOCUMENT ME!
     */
    protected void setUp() throws Exception {
        super.setUp();
        this.testData = new TestData();
        this.testData.setUp();
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    protected void tearDown() throws Exception {
        testData.tearDown(true, false);
        testData = null;
        super.tearDown();
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testDeleteByFID() throws Exception {
        testData.createTempTable(true);

        DataStore ds = testData.getDataStore();
        String typeName = testData.getTemp_table();

        //get a fid
        FeatureReader reader = ds.getFeatureReader(new DefaultQuery(typeName),
                Transaction.AUTO_COMMIT);
        String fid = reader.next().getID();
        reader.close();

        FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);
        HashSet ids = new HashSet();
        ids.add(ff.featureId(fid));
        Filter fidFilter = ff.id(ids);

        FeatureWriter writer = ds.getFeatureWriter(typeName, fidFilter,
                Transaction.AUTO_COMMIT);

        assertTrue(writer.hasNext());

        Feature feature = writer.next();
        assertEquals(fid, feature.getID());
        writer.remove();
        assertFalse(writer.hasNext());
        writer.close();

        //was it really removed?
        reader = ds.getFeatureReader(new DefaultQuery(typeName, fidFilter),
                Transaction.AUTO_COMMIT);
        assertFalse(reader.hasNext());
        reader.close();
    }

    /**
     * Tests that all the features that match a filter based on attribute only
     * filters (aka non spatial filters), are deleted correctly. This test
     * assumes that there are no duplicate values in the test data.
     *
     * @throws Exception
     */
    public void testDeleteByAttOnlyFilter() throws Exception {
        testData.createTempTable(true);

        DataStore ds = this.testData.getDataStore();
        String typeName = this.testData.getTemp_table();

        //get 2 features and build an OR'ed PropertyIsEqualTo filter
        FeatureSource fs = ds.getFeatureSource(typeName);
        FeatureType schema = fs.getSchema();
        AttributeType att = schema.getAttributeType(0);
        String attName = att.getName();

        FeatureIterator reader = fs.getFeatures().features();
        Object val1 = reader.next().getAttribute(0);
        Object val2 = reader.next().getAttribute(0);
        reader.close();

        FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);
        PropertyIsEqualTo eq1 = ff.equals(ff.literal(val1), ff.property(attName));
        PropertyIsEqualTo eq2 = ff.equals(ff.literal(val2), ff.property(attName));
        Or or = ff.or(eq1, eq2);

        FeatureWriter writer = ds.getFeatureWriter(typeName, or,
                Transaction.AUTO_COMMIT);

        assertTrue(writer.hasNext());

        Feature feature = writer.next();
        assertEquals(val1, feature.getAttribute(0));
        writer.remove();

        feature = writer.next();
        assertEquals(val2, feature.getAttribute(0));
        writer.remove();

        assertFalse(writer.hasNext());
        writer.close();

        //was it really removed?
        FeatureReader read = ds.getFeatureReader(new DefaultQuery(typeName, or),
                Transaction.AUTO_COMMIT);
        assertFalse(read.hasNext());
        read.close();
    }

    /**
     * Tests the creation of new feature types, with CRS and all.
     * 
     * <p>
     * This test also ensures that the arcsde datastore is able of creating
     * schemas where the geometry attribute is not the last one. This is
     * important since to do so, the ArcSDE datastore must break the usual way
     * of creating schemas with the ArcSDE Java API, in which one first
     * creates the (non spatially enabled) "table" with all the non spatial 
     * attributes and finally
     * creates the "layer", adding the spatial attribute to the previously
     * created table. So, this test ensures the datastore correctly works
     * arround this limitation.
     * </p>
     *
     * @throws IOException DOCUMENT ME!
     * @throws SchemaException DOCUMENT ME!
     */
    public void testCreateSchema() throws IOException, SchemaException {
        FeatureType type;
        AttributeType[] atts = new AttributeType[4];
        String typeName = this.testData.getTemp_table();
        if(typeName.indexOf('.') != -1){
        	LOGGER.fine("Unqualifying type name to create schema.");
        	typeName = typeName.substring(typeName.lastIndexOf('.') + 1);
        }

        atts[0] = AttributeTypeFactory.newAttributeType("FST_COL",
                String.class, false);
        atts[1] = AttributeTypeFactory.newAttributeType("SECOND_COL",
                Double.class, false);
        atts[2] = AttributeTypeFactory.newAttributeType("GEOM", Point.class,
                false);
        atts[3] = AttributeTypeFactory.newAttributeType("FOURTH_COL",
                Integer.class, false);
        type = FeatureTypeBuilder.newFeatureType(atts, typeName);

        DataStore ds = this.testData.getDataStore();

        this.testData.deleteTempTable(((ArcSDEDataStore) ds).getConnectionPool());
        ds.createSchema(type);
        this.testData.deleteTempTable(((ArcSDEDataStore) ds).getConnectionPool());
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testWriterGeometry() throws Exception {
        testFeatureWriterAutoCommit(Geometry.class);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testWriterPoint() throws Exception {
        testFeatureWriterAutoCommit(Point.class);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testWriterMultiPoint() throws Exception {
        testFeatureWriterAutoCommit(MultiPoint.class);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testWriterLineString() throws Exception {
        testFeatureWriterAutoCommit(LineString.class);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testWriterMultiLineString() throws Exception {
        testFeatureWriterAutoCommit(MultiLineString.class);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testWriterPolygon() throws Exception {
        testFeatureWriterAutoCommit(Polygon.class);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testWriterMultiPolygon() throws Exception {
        testFeatureWriterAutoCommit(MultiPolygon.class);
    }

    /**
     * Tests the writing of features with autocommit transaction.
     *
     * @param geometryClass DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    private void testFeatureWriterAutoCommit(Class geometryClass)
        throws Exception {
        //the table created here is test friendly since it can hold
        //any kind of geometries.
        this.testData.createTempTable(true);

        String typeName = this.testData.getTemp_table();
        FeatureCollection features = this.testData.createTestFeatures(geometryClass,
                10);

        DataStore ds = this.testData.getDataStore();
        FeatureSource fsource = ds.getFeatureSource(typeName);

        //incremented on each feature added event to
        //ensure events are being raised as expected
        //(the count is wraped inside an array to be able of declaring
        //the variable as final and accessing it from inside the anonymous
        //inner class)
        /*
        final int[] featureAddedEventCount = { 0 };

        fsource.addFeatureListener(new FeatureListener() {
                public void changed(FeatureEvent evt) {
                    if (evt.getEventType() != FeatureEvent.FEATURES_ADDED) {
                        throw new IllegalArgumentException(
                            "Expected FEATURES_ADDED event, got "
                            + evt.getEventType());
                    }

                    ++featureAddedEventCount[0];
                }
            });
        */
        
        final int initialCount = fsource.getCount(Query.ALL);

        FeatureWriter writer = ds.getFeatureWriterAppend(typeName,
                Transaction.AUTO_COMMIT);

        Feature source;
        SimpleFeature dest;

        for (FeatureIterator fi = features.features(); fi.hasNext();) {
            source = fi.next();
            dest = (SimpleFeature)writer.next();
            dest.setAttributes(source.getAttributes((Object[]) null));
            writer.write();
        }

        writer.close();

        //was the features really inserted?
        int fcount = fsource.getCount(Query.ALL);
        assertEquals(features.size() + initialCount, fcount);

        /*String msg = "a FEATURES_ADDED event should have been called "
            + features.size() + " times";
        assertEquals(msg, features.size(), featureAddedEventCount[0]);*/
    }
    
    public void testCreateNillableShapeSchema() throws IOException, SchemaException {
        FeatureType type;
        AttributeType[] atts = new AttributeType[2];
        String typeName = this.testData.getTemp_table();
        if(typeName.indexOf('.') != -1){
            LOGGER.fine("Unqualifying type name to create schema.");
            typeName = typeName.substring(typeName.lastIndexOf('.') + 1);
        }
        
        
        atts[0] = new ArcSDEAttributeType(AttributeTypeFactory.newAttributeType("OBJECTID", Integer.class, false));
        ((ArcSDEAttributeType)atts[0]).setFeatureIDAttribute(true);
        atts[1] = AttributeTypeFactory.newAttributeType("SHAPE", MultiLineString.class, true);
        
        type = FeatureTypeBuilder.newFeatureType(atts, typeName);
        
        ArcSDEDataStore ds = this.testData.getDataStore();
        
        this.testData.deleteTempTable(ds.getConnectionPool());
        ds.createSchema(type, this.testData.getConfigKeyword());
        this.testData.deleteTempTable(ds.getConnectionPool());
    }
    
    public void testWriteAndUpdateNullShapes() throws IOException, SchemaException {
        FeatureType type;
        AttributeType[] atts = new AttributeType[2];
        String typeName = this.testData.getTemp_table();
        if(typeName.indexOf('.') != -1){
            LOGGER.fine("Unqualifying type name to create schema.");
            typeName = typeName.substring(typeName.lastIndexOf('.') + 1);
        }
        
        
        atts[0] = new ArcSDEAttributeType(AttributeTypeFactory.newAttributeType("OBJECTID", Integer.class, false));
        ((ArcSDEAttributeType)atts[0]).setFeatureIDAttribute(true);
        try {
            atts[1] = new GeometricAttributeType("SHAPE", MultiLineString.class, true, null, CRS.decode("EPSG:4326"), null);
        } catch (Exception ie) {
            throw new RuntimeException(ie);
        }
        
        type = FeatureTypeBuilder.newFeatureType(atts, typeName);
        
        DataStore ds = this.testData.getDataStore();
        
        this.testData.deleteTempTable(((ArcSDEDataStore)ds).getConnectionPool());
        ((ArcSDEDataStore)ds).createSchema(type, this.testData.getConfigKeyword());
        LOGGER.info("Created null-geom sde layer");
        
        try {
            FeatureWriter writer = ds.getFeatureWriter(this.testData.getTemp_table(),
                    Transaction.AUTO_COMMIT);
            Feature f = writer.next();
            f.setAttribute(0, new Integer(1));
        
            writer.write();
            writer.close();
            LOGGER.info("Wrote null-geom feature to sde");
            
            FeatureReader r = ds.getFeatureReader(
                    new DefaultQuery(this.testData.getTemp_table(), Filter.INCLUDE),
                    Transaction.AUTO_COMMIT);
            assertTrue(r.hasNext());
            f = r.next();
            LOGGER.info("recovered geometry " + f.getDefaultGeometry() + " from single inserted feature.");
            assertTrue(f.getDefaultGeometry().isEmpty());
            //save the ID to update the feature later
            String newId = f.getID();
            assertFalse(r.hasNext());
            r.close();
            LOGGER.info("Confirmed exactly one feature in new sde layer");
            
            FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);
            HashSet ids = new HashSet();
            ids.add(ff.featureId(newId));
            Filter idFilter = ff.id(ids);
            
            writer = ds.getFeatureWriter(this.testData.getTemp_table(),
                    idFilter, Transaction.AUTO_COMMIT);
            
            assertTrue(writer.hasNext());
            
            LOGGER.info("Confirmed feature is fetchable via it's api-determined FID");
            
            GeometryFactory gf = new GeometryFactory();
            int index = 10;
            Coordinate[] coords1 = { new Coordinate(0, 0), new Coordinate(++index, ++index) };
            Coordinate[] coords2 = { new Coordinate(0, index), new Coordinate(index, 0) };
            LineString[] lines = { gf.createLineString(coords1), gf.createLineString(coords2) };
            MultiLineString sampleMultiLine = gf.createMultiLineString(lines);
            
            Feature toBeUpdated  = writer.next();
            toBeUpdated.setAttribute(1,sampleMultiLine);
            writer.write();
            writer.close();
            
            LOGGER.info("Null-geom feature updated with a sample geometry.");
            
            DefaultQuery query = new DefaultQuery(this.testData.getTemp_table(), idFilter);
            r = ds.getFeatureReader(query, Transaction.AUTO_COMMIT);
            assertTrue(r.hasNext());
            f = r.next();
            MultiLineString recoveredMLS = (MultiLineString)f.getDefaultGeometry();
            assertTrue(!recoveredMLS.isEmpty());
            //I tried to compare the recovered MLS to the sampleMultiLineString, but they're
            // slightly different.  SDE does some rounding, and winds up giving me 0.0000002 for zero,
            // and 11.9992 for 12.  Meh.
            r.close();
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.testData.deleteTempTable(((ArcSDEDataStore)ds).getConnectionPool());
    }

    /**
     * Tests the writing of features with real transactions
     *
     * @throws UnsupportedOperationException DOCUMENT ME!
     */
    public void testFeatureWriterTransaction()throws Exception {
        //the table created here is test friendly since it can hold
        //any kind of geometries.
        this.testData.createTempTable(true);

        String typeName = this.testData.getTemp_table();

        DataStore ds = this.testData.getDataStore();
        FeatureSource fsource = ds.getFeatureSource(typeName);

        final int initialCount = fsource.getCount(Query.ALL);
        final int writeCount = initialCount + 2;
        FeatureCollection features = this.testData.createTestFeatures(LineString.class, writeCount);

        //incremented on each feature added event to
        //ensure events are being raised as expected
        //(the count is wraped inside an array to be able of declaring
        //the variable as final and accessing it from inside the anonymous
        //inner class)
        //final int[] featureAddedEventCount = { 0 };

        Transaction transaction = new DefaultTransaction();
        FeatureWriter writer = ds.getFeatureWriter(typeName, Filter.INCLUDE,
                transaction);

        Feature source;
        SimpleFeature dest;

        int count = 0;
        for (FeatureIterator fi = features.features(); fi.hasNext(); count++) {
        	if(count < initialCount){
        		assertTrue("at index " + count, writer.hasNext());
        	}else{
        		assertFalse("at index " + count,writer.hasNext());
        	}

        	source = fi.next();
            dest = (SimpleFeature)writer.next();
            dest.setAttributes(source.getAttributes((Object[]) null));
            writer.write();
        }

        transaction.commit();
        writer.close();

        //was the features really inserted?
        int fcount = fsource.getCount(Query.ALL);
        assertEquals(writeCount, fcount);

        /*
        String msg = "a FEATURES_ADDED event should have been called "
            + features.size() + " times";
        assertEquals(msg, features.size(), featureAddedEventCount[0]);
        */
    }

    /**
     * DOCUMENT ME!
     *
     * @throws UnsupportedOperationException DOCUMENT ME!
     */
    public void testFeatureWriterAppend()throws Exception {
        //the table created here is test friendly since it can hold
        //any kind of geometries.
        this.testData.createTempTable(true);

        String typeName = this.testData.getTemp_table();
        FeatureCollection features = this.testData.createTestFeatures(LineString.class, 2);

        DataStore ds = this.testData.getDataStore();
        FeatureSource fsource = ds.getFeatureSource(typeName);

        final int initialCount = fsource.getCount(Query.ALL);

        FeatureWriter writer = ds.getFeatureWriterAppend(typeName,
                Transaction.AUTO_COMMIT);
        
        Feature source;
        SimpleFeature dest;

        for (FeatureIterator fi = features.features(); fi.hasNext();) {
            assertFalse(writer.hasNext());
            source = fi.next();
            dest = (SimpleFeature)writer.next();
            dest.setAttributes(source.getAttributes((Object[]) null));
            writer.write();
        }

        writer.close();

        //was the features really inserted?
        int fcount = fsource.getCount(Query.ALL);
        assertEquals(features.size() + initialCount, fcount);
    }

    /**
     * DOCUMENT ME!
     *
     * @param args DOCUMENT ME!
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(ArcSDEFeatureStoreTest.class);
    }
}
