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

import com.vividsolutions.jts.geom.*;
import junit.framework.TestCase;
import org.geotools.data.*;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.*;
import org.geotools.filter.*;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.*;

import java.io.IOException;
import java.util.logging.Logger;


/**
 * Unit tests for transaction support
 *
 * @author Gabriel Roldan, Axios Engineering
 * @source $URL$
 * @version $Id$
 */
public class ArcSDEOldStyleFiltersTest extends TestCase {
    /** package logger */
    private static Logger LOGGER = Logger.getLogger(ArcSDEOldStyleFiltersTest.class.getPackage()
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

        FilterFactory ff = FilterFactoryFinder.createFilterFactory();
        Filter fidFilter = ff.createFidFilter(fid);

        FeatureWriter writer = ds.getFeatureWriter(typeName, fidFilter,
                Transaction.AUTO_COMMIT);

        assertTrue(writer.hasNext());

        org.opengis.feature.simple.SimpleFeature feature = writer.next();
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
        SimpleFeatureType schema = fs.getSchema();
        AttributeDescriptor att = schema.getAttribute(0);
        String attName = att.getLocalName();

        FeatureIterator reader = fs.getFeatures().features();
        Object val1 = reader.next().getAttribute(0);
        Object val2 = reader.next().getAttribute(0);
        reader.close();

        final org.opengis.filter.FilterFactory ff = CommonFactoryFinder.getFilterFactory( null );



        LogicFilter or = ff.createLogicFilter(FilterType.LOGIC_OR);
        CompareFilter eq = ff.createCompareFilter(FilterType.COMPARE_EQUALS);
        eq.addLeftValue(ff.createLiteralExpression(val1));
        eq.addRightValue(ff.createAttributeExpression(schema, attName));
        or.addFilter(eq);
        eq = ff.createCompareFilter(FilterType.COMPARE_EQUALS);
        eq.addLeftValue(ff.createLiteralExpression(val2));
        eq.addRightValue(ff.createAttributeExpression(schema, attName));
        or.addFilter(eq);

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
        type = FeatureTypeFactory.newFeatureType(atts, typeName);

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
        final int[] featureAddedEventCount = { 0 };

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
