/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.jdbc;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;


/**
 * Sets up the test harness for a database.
 * <p>
 * The responsibilities of the test harness are the following:
 * <ol>
 *   <li>Create and configure the {@link DataSource} used to connect to the
 *   underlying database
 *   <li>Provide the dialect used to communicate with the underlying database
 *   <li>Populate the underlying database with the data used by the tests.
 * </ol>
 * </p>
 *
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public abstract class JDBCTestSetup {
    static final Logger LOGGER = org.geotools.util.logging.Logging.getLogger(
            "org.geotools.data.jdbc");
    DataSource dataSource = null;

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setUp() throws Exception {
        if (dataSource == null) {
            dataSource = createDataSource();
        }
    }

    protected void initializeDatabase() throws Exception {
    }

    protected void setUpData() throws Exception {
    }

    protected void setUpDataStore(JDBCDataStore dataStore) {
    }

    public void tearDown() throws Exception {
    }
    
    /**
     * Runs an sql string aginst the database.
     *
     * @param input The sql.
     */
    protected void run(String input) throws Exception {
        run(new ByteArrayInputStream(input.getBytes()));
    }
    
    /**
     * Executes {@link #run(String)} ignoring any exceptions.
    */
    protected void runSafe( String input ) { 
        try {
            run( input );
        }
        catch( Exception ignore ) {
            // ignore.printStackTrace(System.out);
        }
    }

    /**
     * Runs an sql script against the database.
     *
     * @param script Input stream to the sql script to run.
     */
    protected void run(InputStream script) throws Exception {
        //load the script
        BufferedReader reader = new BufferedReader(new InputStreamReader(script));

        //connect
        Connection conn = dataSource.getConnection();

        try {
            Statement st = conn.createStatement();

            try {
                String line = null;

                while ((line = reader.readLine()) != null) {
                    LOGGER.fine(line);
                    st.execute(line);
                }

                reader.close();
            } finally {
                st.close();
            }
        } finally {
            conn.close();
        }
    }

    /**
     * This method is used whenever referencing the name
     * of a feature type / table name.
     * <p>
     * Subclasses should override this is in case where databases
     * can not respect case properly and need to force either 
     * upper or lower case. 
     * </p>
     *
     */
    protected String typeName( String raw ) {
        return raw;
    }
    
    /**
     * This method is used whenever referencing the name
     * of an attribute / column name.
     * <p>
     * Subclasses should override this is in case where databases
     * can not respect case properly and need to force either 
     * upper or lower case. 
     * </p>
     */
    protected String attributeName( String raw ) {
        return raw;
    }
    
    /**
     * Creates a data source by reading properties from a file called 'db.properties', 
     * located paralell to the test setup instance.
     */
    protected final DataSource createDataSource() throws IOException {
        Properties db = new Properties();
        db.load( getClass().getResourceAsStream( "db.properties") );

        BasicDataSource dataSource = new BasicDataSource();
        
        dataSource.setDriverClassName(db.getProperty( "driver") );
        dataSource.setUrl( db.getProperty( "url") );
        
        if ( db.containsKey( "username") ) {
            dataSource.setUsername(db.getProperty("username"));    
        }
        if ( db.containsKey( "password") ) {
            dataSource.setPassword(db.getProperty("password"));
        }
        
        dataSource.setPoolPreparedStatements(false);
        dataSource.setAccessToUnderlyingConnectionAllowed(true);
        
        initializeDataSource( dataSource, db );
        return dataSource;
    }
    
    protected void initializeDataSource( BasicDataSource ds, Properties db ) {
        
    }

    protected abstract SQLDialect createSQLDialect(JDBCDataStore dataStore);
}
