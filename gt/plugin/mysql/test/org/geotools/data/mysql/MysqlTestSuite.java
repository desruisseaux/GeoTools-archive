/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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
 */
package org.geotools.data.mysql;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.geotools.data.DataSourceException;
import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureStore;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.filter.AbstractFilter;
import org.geotools.filter.CompareFilter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.filter.IllegalFilterException;
import org.geotools.filter.expression.Expression;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.WKTWriter;

//import org.geotools.data.mysql;

public class MysqlTestSuite extends TestCase {
   private static final FilterFactory filterFactory = FilterFactoryFinder.createFilterFactory();

     /** Standard logging instance */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.defaultcore");

    private static String FEATURE_TABLE = "STREET_LAMP";

    private static String GEOM_TABLE = "STREET_LAMP_LOC";

    private static int NUM_TEST_BULBS = 4;

    /** Well Known Text writer (from JTS). */
    private static WKTWriter geometryWriter = new WKTWriter();

    private static AttributeTypeFactory attFactory = AttributeTypeFactory.newInstance();

        private AttributeType[] lampAttr= { attFactory.newAttributeType("NUM_BULBS", Integer.class),
    				attFactory.newAttributeType("LOCATION", Geometry.class)
					    };

    private MysqlConnection db;

    private FeatureStore mysql = null;

    private FeatureCollection collection = FeatureCollections.newCollection();

    private CompareFilter tFilter;
    
    public MysqlTestSuite(String testName){
        super(testName);
    }

    public static void main(String[] args) {

        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {

        LOGGER.info("starting suite...");
        TestSuite suite = new TestSuite(MysqlTestSuite.class);
	suite.addTest(new TestSuite(MysqlConTestSuite.class));
	suite.addTest(new TestSuite(MysqlGeomColTestSuite.class));
        LOGGER.info("made suite...");
        return suite;
    }
    
    public void setUp() throws Exception {
	LOGGER.info("creating MysqlConnection connection...");
	db = new MysqlConnection ( "localhost","3306","test_Feature"); 
	LOGGER.info("created new db connection");
	MySQLDataStoreFactory factory = new MySQLDataStoreFactory();
	Map params = new HashMap();
	params.put( MySQLDataStoreFactory.DATABASE.key, FEATURE_TABLE );
	params.put( MySQLDataStoreFactory.DBTYPE.key, "mysql");
	params.put( MySQLDataStoreFactory.HOST.key, "localhost");
	params.put( MySQLDataStoreFactory.PORT.key, "3306");
	
	MySQLDataStore data = (MySQLDataStore) factory.createDataStore( params );
	LOGGER.info("created new datastore");
	mysql = (FeatureStore) data.getFeatureSource( FEATURE_TABLE );
	
	//create a filter that is always true, just to pass into getFeatures
	try {
	    tFilter = filterFactory.createCompareFilter(AbstractFilter.COMPARE_EQUALS);
	    Integer testInt = new Integer(5);
	    Expression testLiteral = filterFactory.createLiteralExpression(testInt);
	    tFilter.addLeftValue(testLiteral);
	    tFilter.addRightValue(testLiteral);
	} catch (IllegalFilterException e) {
	    fail("Illegal Filter Exception " + e);
	}
    }
    	
     public void testGet() throws IOException {
        LOGGER.info("starting type enforcement tests...");
        try {
	       	    collection = mysql.getFeatures( tFilter).collection();
	     assertEquals(4, collection.size());
	} catch(DataSourceException e) {
            LOGGER.info("...threw data source exception: " + e.getMessage());    
	    fail();
        }
        LOGGER.info("...ending type enforcement tests");
	}

    //TODO: update to new feature api.
    /*    public void testAdd() {
	int feaID = 8;
	int numBulbs = 4;
	int geomID;
	AttributeType[] lampAttr= { new AttributeTypeDefault("NUM_BULBS", Integer.class),
				    new AttributeTypeDefault("LOCATION", Geometry.class)
					};
	Feature[] features = new Feature[1];
	Geometry geom = new Point(new Coordinate(6, 10), new PrecisionModel(), 1);
	Object[] attributes = { new Integer(numBulbs), geom};
	try{
	    FlatFeatureFactory factory = new FlatFeatureFactory(new FeatureTypeFlat(lampAttr));
	    features[0] = factory.create(attributes, String.valueOf(feaID));
	    collection = new FeatureCollectionDefault();
	    collection.addFeatures(features);
	    mysql.addFeatures(collection);
	} catch(DataSourceException e){
	    LOGGER.info("threw data source exception");
	    fail();
	} catch(SchemaException e){
	    fail();
	    LOGGER.info("trouble creating feature type");
	} catch(IllegalFeatureException e){
	    fail("illegal feature " + e);
	}

	//clean up...basically a delete, but without using a delete.
	try{
	    Connection dbConnection = db.getConnection();
	    Statement statement = dbConnection.createStatement();
	    ResultSet result = statement.executeQuery("SELECT * FROM " + FEATURE_TABLE + " WHERE ID = " + feaID);
	    result.next();
	    assertEquals(result.getInt(1), feaID);
	    assertEquals(result.getInt(2), numBulbs);
	    geomID = result.getInt(3);
	    statement.executeUpdate("DELETE FROM " + FEATURE_TABLE + " WHERE ID = " + feaID);
	    result = statement.executeQuery("SELECT * FROM " + GEOM_TABLE + " WHERE GID = " + geomID);
	    result.next();
	    assertTrue(result.getString(6).equals(geometryWriter.write(geom)));
	    statement.executeUpdate("DELETE FROM " + GEOM_TABLE + " WHERE GID = " + geomID);
	    result.close();
	    statement.close();
	    dbConnection.close();
	} catch(SQLException e){
	   
	    LOGGER.info("we had some sql trouble " + e.getMessage());
	    fail();
	}
    
	}*/

    public void testRemove() throws IOException {
	try {
	    FeatureCollection delFeatures = mysql.getFeatures(tFilter).collection();
	    mysql.removeFeatures(tFilter);
	    collection = mysql.getFeatures(tFilter).collection();
	    assertEquals(0, collection.size());
	    mysql.addFeatures( DataUtilities.reader(delFeatures));
	    collection = mysql.getFeatures(tFilter).collection();
	    assertEquals(4, collection.size());
	} catch (DataSourceException e){
	    fail("Data source exception " + e);
	}
    }
    
    public void testModify() throws IOException {
	Integer tBulbs = new Integer(NUM_TEST_BULBS);
	Integer restBulbs = new Integer(NUM_TEST_BULBS - 2);
	Geometry geom = new Point(new Coordinate(6, 10), new PrecisionModel(), 1);
	try {
	    mysql.modifyFeatures(lampAttr[0], tBulbs, tFilter);
	    //mysql.modifyFeatures(lampAttr[1], geom, tFilter);
	    //do a geom test when we figure out how to get the filters to work
	    collection = mysql.getFeatures(tFilter).collection();
	    assertEquals(tBulbs, 
			 (Integer) collection.features().next().getAttribute("NUM_BULBS"));
	    mysql.modifyFeatures(lampAttr[0], restBulbs, tFilter);
	    collection = mysql.getFeatures(tFilter).collection();
	    assertEquals(restBulbs, 
	    	 (Integer) collection.features().next().getAttribute("NUM_BULBS"));
	} catch (DataSourceException e) {
	    fail("Data source Exception " + e);
	} 
    }
    
}

