package org.geotools.jdbc;

import java.sql.Connection;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import junit.framework.TestCase;
import junit.framework.TestResult;

import org.geotools.filter.FilterCapabilities;
import org.geotools.filter.FilterFactoryImpl;
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
public abstract class JDBCTestSupport extends TestCase {
	
	/**
	 * data source
	 */
	static {
	    //turn up logging
		ConsoleHandler handler = new ConsoleHandler();
		handler.setLevel(Level.FINE);
		Logger.getLogger("org.geotools.data.jdbc").setLevel( Level.FINE );
		Logger.getLogger("org.geotools.data.jdbc").addHandler( handler );
	}
	
	protected JDBCTestSetup setup;
    protected JDBCDataStore dataStore;
    
    /**
     * Override to check if a database connection can be obtained, if not 
     * tests are ignored.
     */
    public void run(TestResult result) {
        try {
            JDBCTestSetup setup = createTestSetup();
            DataSource dataSource = setup.createDataSource();
            Connection cx = dataSource.getConnection();
            cx.close();
        }
        catch ( Throwable t ) {
            return;
        }
        
        super.run( result );
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        
        //create the test harness
        if ( setup == null ) {
            setup = createTestSetup(); 
        }
        
        setup.setUp();
        
        //do any further setup
        setUpInternal();
        
        //initialize the database
        setup.initializeDatabase();
        
        //initialize the data
        setup.setUpData();
        
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
        dataStore.setSQLDialect( setup.createSQLDialect() );
        dataStore.setNamespaceURI("http://www.geotools.org/test");
        dataStore.setDataSource( setup.getDataSource() );
        dataStore.setDatabaseSchema("geotools");
        dataStore.setFilterFactory(new FilterFactoryImpl());
        dataStore.setGeometryFactory(new GeometryFactory());
        dataStore.setFilterCapabilities(filterCapabilities);
    }
    
    protected abstract JDBCTestSetup createTestSetup();
   
    protected final void setUpInternal() throws Exception {
       
    }
    
    protected void tearDown() throws Exception {
    	super.tearDown();
    	
    	setup.tearDown();
    	
    	dataStore.dispose();
    }
}