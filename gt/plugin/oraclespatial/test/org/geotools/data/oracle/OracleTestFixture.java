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

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import oracle.jdbc.OracleConnection;

import org.geotools.data.jdbc.ConnectionPool;

/**
 * Capture in one spot the location of *your* oracle instance.
 * <p>
 * This class is used by all test cases to provide a connection
 * to the database.
 * </p>
 * @author jgarnett
 */
public class OracleTestFixture {	
	    public OracleConnection connection;
	    
		private Properties properties;

		/** schema name incase you need access to types - like SDO */
		private String schemaName;

		/** Connection pool - incase you want to test a EPSG authority */
		private ConnectionPool cPool;
	    
	    /**
	     * OracleTestFixture used in JUnit 
	     * <p>
	     * Connects to database and grabs the contents of the first row.
	     * </p>
	     * Example:
	     * <code><pre>
	     * protected void setUp() throws Exception {
	     *         super.setUp();
	     *         fixture = new OracleTestFixture();
	     *         geom = fixture.geom;
	     * }
	     * </pre></code>
	     */
	    public OracleTestFixture()  throws Exception {
	       // make connection to DB
	       // DriverManager.registerDriver( new oracle.jdbc.driver.OracleDriver() );
	       
	       properties = new Properties();
	       properties.load(this.getClass().getResourceAsStream("test.properties"));
	       
	       schemaName = properties.getProperty("schema");
	       
	       OracleConnectionFactory fact = new OracleConnectionFactory(properties.getProperty("host"), 
	                properties.getProperty("port"), properties.getProperty("instance"));
	       fact.setLogin(properties.getProperty("user"), properties.getProperty("passwd"));
	       
	       cPool = fact.getConnectionPool();	       
	       System.out.println( "Connect to"+ properties );
	       //connection = (OracleConnection) cPool.getConnection();
	       
	        	       
	       // connection = (OracleConnection) DriverManager.getConnection  ("jdbc:oracle:thin:@hydra:1521:rrdev","dblasby","dave2000");
	       connection = (OracleConnection) DriverManager.getConnection  ("jdbc:oracle:thin:@hydra:1521:dev","egouge","emily2004");
	       System.out.println( connection.getTypeMap());
	    }	    
	    public void close() throws SQLException{
	        connection.close();
	    }    
	}
