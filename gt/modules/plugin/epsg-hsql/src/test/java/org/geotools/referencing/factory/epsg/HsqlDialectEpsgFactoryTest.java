package org.geotools.referencing.factory.epsg;

import java.sql.Connection;
import java.util.Set;

import org.geotools.factory.Hints;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import javax.sql.DataSource;
import junit.framework.TestCase;

public class HsqlDialectEpsgFactoryTest extends TestCase {
	private HsqlDialectEpsgFactory factory;
	
	protected void setUp() throws Exception {
		super.setUp();
		DataSource datasource = HsqlEpsgDatabase.createDataSource();
		Connection connection = datasource.getConnection();
		Hints hints = new Hints( Hints.BUFFER_POLICY, "none" );		
		factory = new HsqlDialectEpsgFactory( hints, connection );
	}
	
	public void testCreation() throws Exception {
		assertNotNull(factory);
		CoordinateReferenceSystem epsg4326 = factory.createCoordinateReferenceSystem("EPSG:4326");
		CoordinateReferenceSystem code4326 = factory.createCoordinateReferenceSystem("4326");
		
		assertEquals("4326 equals EPSG:4326", code4326, epsg4326);
        assertSame("4326 == EPSG:4326", code4326, epsg4326);		
	}
    
    public void testAuthorityCodes() throws Exception {
        Set authorityCodes = factory.getAuthorityCodes(CoordinateReferenceSystem.class);

        assertNotNull(authorityCodes);
        assertTrue(authorityCodes.size() > 3000);
    }

}
