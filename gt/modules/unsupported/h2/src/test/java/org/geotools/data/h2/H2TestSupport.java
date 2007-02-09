package org.geotools.data.h2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

import junit.framework.TestCase;

import org.geotools.feature.simple.SimpleTypeFactoryImpl;
import org.h2.tools.DeleteDbFiles;
import org.h2.tools.Server;

/**
 * Test support class for tests which run of a live h2 instance.
 * 
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 */
public class H2TestSupport extends TestCase {

	/**
	 * Embedded server instance
	 */
	Server server;
	/**
	 * The datastore
	 */
	H2DataStore dataStore;
	
	protected void setUp() throws Exception {
		super.setUp();
		
		//create the server instance
		server = Server.createTcpServer( new String[]{} );
		server.start();
		
		H2ConnectionPoolDataSource cpDataSource 
			= new H2ConnectionPoolDataSource("jdbc:h2:geotools");
		
		//connect
		Connection conn = 
			cpDataSource.getPooledConnection().getConnection();
	   
		try {
			//create a schema
			Statement st = conn.createStatement();
			try {
				st.execute( "CREATE SCHEMA \"geotools\";" );	
			}
			catch( Exception ignore ) {}
			finally {
				st.close();	
			}
			
			//create a table
			st = conn.createStatement();
			try { 
				st.execute( 
		    		"CREATE TABLE \"geotools\".\"featureType1\" (" + 
		    		 " \"id\" int primary key, \"intProperty\" int," +
		    		 " \"doubleProperty\" double, \"stringProperty\" varchar" +
					");"
				);
			}
			catch( Exception ignore ) {
				ignore.printStackTrace();
			}
			finally {
				st.close();
			}
		    
			//clear the table
			st = conn.createStatement();
			st.execute( "DELETE FROM \"geotools\".\"featureType1\";" );
			st.close();
			
		    //insert some data
		    PreparedStatement pst = conn.prepareStatement( 
	    		"INSERT INTO \"geotools\".\"featureType1\" VALUES ( ?, ?, ?, ? );"
    		);
		    pst.setInt( 1 , 1  );
		    pst.setInt( 2 , 1  );
		    pst.setDouble( 3, 1.1 );
		    pst.setString( 4, "one" );
		    pst.execute();
		    
		    pst.setInt( 1 , 2  );
		    pst.setInt( 2 , 2  );
		    pst.setDouble( 3, 2.2 );
		    pst.setString( 4, "two" );
		    pst.execute();
		    
		    pst.setInt( 1 , 3  );
		    pst.setInt( 2 , 3  );
		    pst.setDouble( 3, 3.3 );
		    pst.setString( 4, "three" );
		    pst.execute();
		    
		    pst.close();
		    
		    //create the datastore
		    H2Content content = new H2Content( cpDataSource );
		    content.setDatabaseSchema( "geotools" );
		    
		    dataStore = new H2DataStore( content );
		    dataStore.setNamespaceURI( "http://www.geotools.org/test" );
		    dataStore.setTypeFactory( new SimpleTypeFactoryImpl() );
		    
		}
		finally {
			conn.close();	
		}

	}
	
	protected void tearDown() throws Exception {
		
		Connection conn = dataStore.getConnectionPoolDataSource()
			.getPooledConnection().getConnection();
		
		try {
			//drop the table
		    Statement st = conn.createStatement();
		    st.addBatch( "DROP TABLE \"geotools\".\"featureType1\";" );
		    st.addBatch( "DROP SCHEMA \"geotools\";" );
		    st.executeBatch();
		}
		finally {
			conn.close();
		}
		
		//stop the server
		server.stop();
		Thread.sleep( 100 );
		
		//kill the files
		String dir = System.getProperty( "user.dir" );
		DeleteDbFiles.execute( dir , "geotools", true );
	}
}
