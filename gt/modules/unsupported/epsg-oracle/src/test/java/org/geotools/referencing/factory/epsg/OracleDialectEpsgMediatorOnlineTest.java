package org.geotools.referencing.factory.epsg;

import org.geotools.factory.Hints;
import org.geotools.referencing.factory.epsg.oracle.OracleOnlineTestCase;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class OracleDialectEpsgMediatorOnlineTest extends OracleOnlineTestCase {

    private OracleDialectEpsgMediator mediator;
    
    protected void connect() throws Exception {
        super.connect();
        Hints hints = new Hints(Hints.BUFFER_POLICY, "none");     
        mediator = new OracleDialectEpsgMediator(80, hints, datasource);
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
