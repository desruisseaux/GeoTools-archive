package org.geotools.referencing;

import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.CoordinateSystemAxis;

//JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests if the CRS utility class is functioning correctly when using HSQL datastore.
 * 
 * @author Jody
 */
public class CRSTest extends  TestCase {
    
    public void testCorrectAxisOrder() throws NoSuchAuthorityCodeException, FactoryException{
        CoordinateReferenceSystem crs = CRS.decode("EPSG:4326");
        
        assertEquals( 2, crs.getCoordinateSystem().getDimension() );
        
        CoordinateSystemAxis axis0  = crs.getCoordinateSystem().getAxis(0);
        assertEquals( "Lat", axis0.getAbbreviation() );
        
        CoordinateSystemAxis axis1  = crs.getCoordinateSystem().getAxis(1);
        assertEquals( "Long", axis1.getAbbreviation() );        
    }    
    public void testSystemPropertyToForceXY() throws NoSuchAuthorityCodeException, FactoryException{
        System.setProperty("org.geotools.referencing.forceXY", "true");
        
        CoordinateReferenceSystem crs = CRS.decode("EPSG:4326");
        assertEquals( 2, crs.getCoordinateSystem().getDimension() );
        
        CoordinateSystemAxis axis0  = crs.getCoordinateSystem().getAxis(0);
        assertEquals( "forceXY did not work", "Long", axis0.getAbbreviation() );
        
        CoordinateSystemAxis axis1  = crs.getCoordinateSystem().getAxis(1);
        assertEquals( "forceXY did not work", "Lat", axis1.getAbbreviation() );
        
        System.setProperty("org.geotools.referencing.forceXY", "false");        
    }    
}
