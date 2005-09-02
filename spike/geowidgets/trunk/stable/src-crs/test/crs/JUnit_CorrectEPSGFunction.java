package test.crs;

import org.geotools.referencing.FactoryFinder;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.cs.EllipsoidalCS;

import junit.framework.TestCase;

public class JUnit_CorrectEPSGFunction extends TestCase {
    
    /** If that test fails the user likely has the wrong geoapi-tigerXXX.jar
     * in the classpath. Only use the edited one from Geotools
     * (in http://lists.refractions.net/geotools/geoapi/jars/)
     * not the one from GeoAPI. (State 2005-07-26) 
     */    
    public void testEPSGFunction() {
        try{
            System.out.println("************************************");
            System.out.println("Creating the WGS84 geographic CRS and getting its CS.");
            CRSAuthorityFactory crsAFactory = FactoryFinder.getCRSAuthorityFactory("EPSG", null);
            GeographicCRS crs = crsAFactory.createGeographicCRS("4326");
            EllipsoidalCS cs = (EllipsoidalCS)crs.getCoordinateSystem();
            System.out.println("Finished EPSG test.");
            assertNotNull("Returned CS is null.", cs);
        } catch (FactoryException e){
            e.printStackTrace();
            fail("Factory exception thrown trying to create a CRS from code in Geotools.\n"
                    + " It is likely you use the wrong geoapi jar or some other library is missing.");
        }
    }
}
