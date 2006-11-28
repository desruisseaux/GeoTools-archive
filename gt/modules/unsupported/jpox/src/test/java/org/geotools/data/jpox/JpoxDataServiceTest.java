package org.geotools.data.jpox;

import java.io.File;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import junit.framework.TestCase;


public class JpoxDataServiceTest extends TestCase {

	private static PersistenceManagerFactory pmf;
	private static PersistenceManager pm;

	protected void setUp() throws Exception {
		
		super.setUp();
	}
	
	public void testAccess() {
		fail( "Not yet implemented" );
	}

	public void testDescribe() {
		fail( "Not yet implemented" );
	}

	public void testGetTypes() {
		fail( "Not yet implemented" );
	}

	private static void init() {
		pmf = JDOHelper.getPersistenceManagerFactory( new File( "src/test/resources/jdo.properties" ) );
		pm = pmf.getPersistenceManager();
	}
	
	
}
