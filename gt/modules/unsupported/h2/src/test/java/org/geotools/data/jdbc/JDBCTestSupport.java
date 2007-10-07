package org.geotools.data.jdbc;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.apache.commons.dbcp.BasicDataSource;
import org.geotools.filter.FilterCapabilities;
import org.geotools.filter.FilterFactoryImpl;
import org.h2.tools.DeleteDbFiles;
import org.h2.tools.Server;
import org.opengis.filter.ExcludeFilter;
import org.opengis.filter.Id;
import org.opengis.filter.IncludeFilter;
import org.opengis.filter.PropertyIsBetween;
import org.opengis.filter.PropertyIsLike;
import org.opengis.filter.PropertyIsNull;

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
	    
	    //set up the data source
		dataSource.setUrl("jdbc:h2:geotools");
		dataSource.setDriverClassName("org.h2.Driver");
		dataSource.setPoolPreparedStatements(false);
	
		//turn up logging
		ConsoleHandler handler = new ConsoleHandler();
		handler.setLevel(Level.FINE);
		Logger.getLogger("org.geotools.data.jdbc").setLevel( Level.FINE );
		Logger.getLogger("org.geotools.data.jdbc").addHandler( handler );
	}
	
    /**
     * Runs an sql string aginst the database.
     * 
     * @param input The sql.
     */
    static void run( String input ) throws Exception {
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

    protected JDBCDataStore dataStore;
    
    protected void setUp() throws Exception {
        super.setUp();
        
        //spatially enable the database
        
        try {
            run( getClass().getResourceAsStream( "h2.sql" ) );
        }
        catch( Exception e ) {
            
        }
        
        FilterCapabilities filterCapabilities = new FilterCapabilities();

        filterCapabilities.addAll(FilterCapabilities.LOGICAL_OPENGIS);
        filterCapabilities.addAll(FilterCapabilities.SIMPLE_COMPARISONS_OPENGIS);
        filterCapabilities.addType(PropertyIsNull.class);
        filterCapabilities.addType(PropertyIsBetween.class);
        filterCapabilities.addType(Id.class);
        filterCapabilities.addType(IncludeFilter.class);
        filterCapabilities.addType(ExcludeFilter.class);
        filterCapabilities.addType(PropertyIsLike.class);

        //create the dataStore
        dataStore = new JDBCDataStore();
        dataStore.setNamespaceURI("http://www.geotools.org/test");
        dataStore.setDataSource( dataSource );
        dataStore.setDatabaseSchema("geotools");
        dataStore.setFilterFactory(new FilterFactoryImpl());
        dataStore.setGeometryFactory(new GeometryFactory());
        dataStore.setFilterCapabilities(filterCapabilities);
        
        //drop old data
        try {
            run ( "DROP TABLE \"geotools\".\"ft1\"; COMMIT;" );    
        }
        catch( Exception e ) {
            //e.printStackTrace();
        }
        try {
            run ( "DROP SCHEMA \"geotools\"; COMMIT;" );    
        }
        catch( Exception e ) {
            //e.printStackTrace();
        }
        
        //create some data
        StringBuffer sb = new StringBuffer().append( "CREATE SCHEMA \"geotools\";" ); 
		
		sb.append( "CREATE TABLE \"geotools\".\"ft1\" " )
			.append( "(\"id\" int AUTO_INCREMENT(1) PRIMARY KEY , " )
			.append( "\"geometry\" OTHER, \"intProperty\" int, " ) 
			.append( "\"doubleProperty\" double, \"stringProperty\" varchar);" );
		
		sb.append( "INSERT INTO \"geotools\".\"ft1\" VALUES (")
			.append( "0,setSRID(GeometryFromText('POINT(0 0)'),4326), 0, 0.0,'zero');");
	
		sb.append( "INSERT INTO \"geotools\".\"ft1\" VALUES (")
			.append( "1,setSRID(GeometryFromText('POINT(1 1)'),4326), 1, 1.1,'one');");

		sb.append( "INSERT INTO \"geotools\".\"ft1\" VALUES (")
			.append( "2,setSRID(GeometryFromText('POINT(2 2)'),4326), 2, 2.2,'two');");

		
		run( sb.toString() );
    }
    
    protected void tearDown() throws Exception {
    	super.tearDown();
    	
    	dataStore.dispose();
    	
    	StringBuffer sb = new StringBuffer("DROP TABLE \"geotools\".\"ft1\";")
			.append( "DROP SCHEMA \"geotools\";" ); 
			
		run( sb.toString() );
		
		Thread.sleep(100);
    }
}