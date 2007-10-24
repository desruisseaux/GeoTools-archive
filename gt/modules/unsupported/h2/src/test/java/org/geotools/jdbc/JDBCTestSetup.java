package org.geotools.jdbc;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;

import javax.sql.DataSource;

/**
 * Sets up the test harness for a database.
 * <p>
 * The responsibilities of the test harness are the following:
 * <ol>
 *   <li>Create and configure the {@link DataSource} used to connect to the 
 *   underlying database
 *   <li>Populate the underlying database with the data used by the tests.
 * </ol>
 * </p>
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public abstract class JDBCTestSetup {

    DataSource dataSource = null;
    
    public DataSource getDataSource() {
        return dataSource;
    }
    
    public void setUp() throws Exception {
        if ( dataSource == null ) {
            dataSource = createDataSource();
        }
    }
    
    protected void initializeDatabase() throws Exception {
        
    }
    
    protected void setUpData() throws Exception {
        
    }
    
    public void tearDown() throws Exception {
    }
    
    /**
     * Runs an sql string aginst the database.
     * 
     * @param input The sql.
     */
    protected void run( String input ) throws Exception {
        run( new ByteArrayInputStream( input.getBytes() ) );
    }
    
    /**
     * Runs an sql script against the database.
     * 
     * @param script Input stream to the sql script to run.
     */
    protected void run(InputStream script) throws Exception {
        //load the script
        BufferedReader reader = 
            new BufferedReader(new InputStreamReader( script ) );

        //connect
        Connection conn = dataSource.getConnection();

        try {
            Statement st = conn.createStatement();

            try {
                String line = null;

                while ((line = reader.readLine()) != null) {
                    st.execute(line);
                }

                reader.close();    
            }
            finally {
                st.close();
            }
            
        } finally {
            conn.close();
        }
    }
    
    
    protected abstract DataSource createDataSource();
    
    protected abstract SQLDialect createSQLDialect();
}
