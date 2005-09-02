package test.crs;

import org.geotools.referencing.FactoryFinder;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.cs.EllipsoidalCS;

public class Test_WGS84CRS {

    /** If that test fails the user likely has the wrong geoapi-tigerXXX.jar
     * in the classpath. Only use the edited one from Geotools, not the one from
     * GeoAPI. (State 2005-07-26) 
     */
    public static void main(String[] args) throws Exception{
        System.out.println("Test 1: Creating the WGS84 geographic CRS and getting its CS.");
        CRSAuthorityFactory crsAFactory = FactoryFinder.getCRSAuthorityFactory("EPSG", null);
        GeographicCRS crs = crsAFactory.createGeographicCRS("4326");
        EllipsoidalCS cs = (EllipsoidalCS)crs.getCoordinateSystem();
        System.out.println("Finished.");

    }

}
