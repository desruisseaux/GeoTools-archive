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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.WKTReader;

//import org.geotools.data.mysql;

public class MysqlConTestSuite extends TestCase {

     /** Standard logging instance */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.defaultcore");
    private MysqlConnection db;

    //DataSource mysql = null;

    //FeatureCollection collection = new FeatureCollectionDefault();
    public static final String TEST_USER = "cholmes";
    public static final String TEST_PWORD = "seven";

    public MysqlConTestSuite(String testName){
        super(testName);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {

        LOGGER.info("starting suite...");
        TestSuite suite = new TestSuite(MysqlConTestSuite.class);
        LOGGER.info("made suite...");
        return suite;
    }
    
    public void setUp() {
	LOGGER.info("creating MysqlConnection connection...");
	db = new MysqlConnection ("localhost","3306","test_Feature"); 
	LOGGER.info("created new db connection");
	
    }

    public void testLogin() {
	db.setLogin(TEST_USER,TEST_PWORD);
	LOGGER.info("set the login");
	assertEquals(TEST_USER, db.getLoginUser());
	assertEquals(TEST_PWORD, db.getLoginPassword());
	
    }


    public void testConnection(){
	String wkt;
	Geometry curGeo = null;
	GeometryFactory geometryFactory = new GeometryFactory();
	WKTReader geometryReader = new WKTReader(geometryFactory);
	LOGGER.info("Connecting to Mysql database"); 
	try {
	    Connection dbConnection = db.getConnection();
	    LOGGER.info("connected");
	    Statement statement = dbConnection.createStatement();
	    statement.executeUpdate("CREATE TABLE Con_Test(a int, b varchar(50))");
	    statement.executeUpdate("INSERT INTO Con_Test values(5, 'road')");
	    ResultSet rs = statement.executeQuery("SELECT * FROM Con_Test");
	    
	    rs.next();
	    assertEquals(5, rs.getInt(1));
	    assertEquals("road", rs.getString(2));
	    statement.executeUpdate("DROP TABLE Con_Test");
	    LOGGER.info("created and dropped, dude");
	    rs.close();
	    statement.close();
	    dbConnection.close();
   
	} catch(SQLException ex) {
            System.err.println("==> SQLException: ");
	    while (ex != null) {
		System.out.println("Message:   " + ex.getMessage ());
		System.out.println("SQLState:  " + ex.getSQLState ());
		System.out.println("ErrorCode: " + ex.getErrorCode ());
		ex = ex.getNextException();
		System.out.println("");
	    }
	    fail("SQL exception was thrown");	    

        }
    }



    
}
