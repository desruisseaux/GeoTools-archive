package org.geotools.data.jdbc;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import junit.framework.TestCase;

import org.apache.commons.dbcp.BasicDataSource;
import org.geotools.filter.FilterFactoryImpl;
import org.h2.tools.DeleteDbFiles;
import org.h2.tools.Server;

import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * Test support class for jdbc test cases.
 * <p>
 * This test class fires up a live instance of an h2 database to provide a 
 * live database to work with.
 * </p>
 *
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 */
public class JDBCTestSupport extends TestCase {
	
	/**
	 * data source
	 */
	static BasicDataSource dataSource = new BasicDataSource();
	static {
		dataSource.setUrl("jdbc:h2:geotools");
		dataSource.setDriverClassName("org.h2.Driver");
		dataSource.setPoolPreparedStatements(false);
	}
	
	/**
     * Embedded server instance, created statically to live over the life
     * of many test cases, with a shutdown hook to cleanup
     */
    static Server server;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                if (server != null) {
                    //stop the server
                    server.stop();

                    //kill the files
                    try {
                    	//bug with h2? if hte log file doesn't get 
                    	// deleted the subsequent run crashes, if 
                    	// you execute twice, then the log file gets
                    	// deleted
                    	DeleteDbFiles.main(null);
                    	DeleteDbFiles.main(null);
                    } catch (SQLException e) {
                    }
                }
            }
        }));
    }

    /**
     * Runs an sql string aginst the database.
     * 
     * @param input The sql.
     */
    static void run( String input ) throws Exception {
    	JDBCDataStore.LOGGER.info( input );
    	run( new ByteArrayInputStream( input.getBytes() ) );
    }
    
    /**
     * Runs an sql script against the database.
     * 
     * @param script Input stream to the sql script to run.
     */
    static void run(InputStream script) throws Exception {
        //load the script
        BufferedReader reader = 
        	new BufferedReader(new InputStreamReader( script ) );

        //connect
        Connection conn = dataSource.getConnection();

        try {
            Statement st = conn.createStatement();

            String line = null;

            while ((line = reader.readLine()) != null) {
            	st.execute(line);
            }

            reader.close();

            st.close();
        } finally {
            conn.close();
        }
    }

    protected JDBCDataStore dataStore;
    
    protected void setUp() throws Exception {
        super.setUp();

        if (server == null) {
            //create the server instance
            server = Server.createTcpServer(new String[] {  });
            server.start();

            //spatialy enable it
            run( getClass().getResourceAsStream( "h2.sql" ) );
        }
        
        //create the dataStore
        dataStore = new JDBCDataStore();
        dataStore.setNamespaceURI("http://www.geotools.org/test");
        dataStore.setDataSource( dataSource );
        dataStore.setDatabaseSchema("geotools");
        dataStore.setFilterFactory(new FilterFactoryImpl());
        dataStore.setGeometryFactory(new GeometryFactory());
        
        //create some data
        StringBuffer sb = new StringBuffer().append( "CREATE SCHEMA \"geotools\";" ); 
		
		sb.append( "CREATE TABLE \"geotools\".\"ft1\" " )
			.append( "(\"id\" int AUTO_INCREMENT(1) primary key , " )
			.append( "\"geometry\" OTHER, \"intProperty\" int, " ) 
			.append( "\"doubleProperty\" double, \"stringProperty\" varchar);" );
		
		sb.append( "INSERT INTO \"geotools\".\"ft1\" VALUES (")
			.append( "0,GeometryFromText('POINT(0 0)'), 0, 0.0,'zero');");
	
		sb.append( "INSERT INTO \"geotools\".\"ft1\" VALUES (")
			.append( "1,GeometryFromText('POINT(1 1)'), 1, 1.1,'one');");

		sb.append( "INSERT INTO \"geotools\".\"ft1\" VALUES (")
			.append( "2,GeometryFromText('POINT(2 2)'), 2, 2.2,'two');");

		
		run( sb.toString() );
		

    }
    
    protected void tearDown() throws Exception {
    	super.tearDown();
    	
    	StringBuffer sb = new StringBuffer("DROP TABLE \"geotools\".\"ft1\";")
			.append( "DROP SCHEMA \"geotools\";" ); 
			
		run( sb.toString() );
    }
}