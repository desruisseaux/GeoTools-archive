package org.geotools.referencing.factory.epsg;

import javax.sql.DataSource;

import junit.framework.TestCase;

import org.geotools.factory.Hints;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class HsqlDialectEpsgMediatorTest extends TestCase {
    private HsqlDialectEpsgMediator mediator;
    
    protected void setUp() throws Exception {
        super.setUp();
        DataSource datasource = HsqlEpsgDatabase.createDataSource();
        Hints hints = new Hints( Hints.BUFFER_POLICY, "none" );     
        mediator = new HsqlDialectEpsgMediator(80, hints, datasource);
    }
    
    public void testCreation() throws Exception {
        assertNotNull(mediator);
        CoordinateReferenceSystem epsg4326 = mediator.createCoordinateReferenceSystem("EPSG:4326");
        CoordinateReferenceSystem code4326 = mediator.createCoordinateReferenceSystem("4326");
        
        assertNotNull(epsg4326);
        assertEquals("4326 equals EPSG:4326", code4326, epsg4326);
        assertSame("4326 == EPSG:4326", code4326, epsg4326);       
    }

}
