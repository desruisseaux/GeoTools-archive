package org.geotools.test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import junit.framework.TestCase;

/**
 * Test support for test cases which require an "online" resource, such as an
 * external server or database.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 * 
 */
public abstract class OnlineTestCase extends TestCase {

	/**
	 * The test fixture, null if the fixture is not available.
	 */
	protected Properties fixture;

	/**
	 * Loads the test fixture for the test case.
	 * <p>
	 * The fixture is obtained via {@link #getFixtureId()}.
	 * </p>
	 */
	protected final void setUp() throws Exception {
		// load the fixture
		File base = new File( System.getProperty( "user.dir" ) + File.separator + "geotools" );
		File fixtureFile = new File( base, getFixtureId().replace( '.', File.separatorChar ) );
		
		if ( fixtureFile.exists() ) {
			InputStream input = new BufferedInputStream( new FileInputStream( fixtureFile ) );
			try {
				fixture = new Properties();
				fixture.load( input );
			}
			finally {
				input.close();
			}
			
			//call the setUp template method
			setUpInternal();
		}
	}	
	
	/**
	 * Template method for setup, called from {@link #setUp()}.
	 * <p>
	 * Subclasses should do all initialization here.
	 * </p>
	 * 
	 * @throws Exception
	 */
	protected void setUpInternal() throws Exception {
	}

	/**
	 * Override which checks if the fixture is available. If not the test is not 
	 * executed.
	 */
	protected void runTest() throws Throwable {
		// if the fixture was loaded, run
		if (fixture != null) {
			super.runTest();
		}

		// otherwise do nothing
	}

	/**
	 * The fixture id for the test case.
	 * <p>
	 * This name is hierachical, similar to a java package name. Example: "postgis.demo_bc"
	 * </p>
	 * @return
	 */
	protected abstract String getFixtureId();
	
}
