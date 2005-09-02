package test.crs;

import java.util.List;

import org.geowidgets.crs.model.EPSGEntry;
import org.geowidgets.crs.model.ICRSModel;
import org.geowidgets.framework.GWFactoryFinder;
import org.opengis.referencing.datum.Ellipsoid;

/** Tests the functionality of the model underlying the CRS assembly widgets. */
public class Test_GeoTools_CRSModel {

    public static void main(String[] args) throws Exception{
        System.out.println("Test 1: Getting all ellipsoids");
        ICRSModel model = GWFactoryFinder.getCRSModel();
        try{
            List<EPSGEntry> ellipsoids = model.getSupportedObjects(Ellipsoid.class, null);
            for (EPSGEntry s : ellipsoids){
                System.out.print(s + "; ");
            }
            System.out.println();
        } catch (Exception e){e.printStackTrace();}
        
        System.out.println("Test 2: Create ellipsoid \"GRS 1980\"");
        try{
            Ellipsoid e = model.createEllipsoid("GRS 1980");
            System.out.println("Created ellipsoid " + e.getIdentifiers().iterator().next().getCode());
        } catch (Exception e){e.printStackTrace();}
        
        System.out.println("Test 3: Give me info about ellipsoid \"Krassowsky 1940\"");
        System.out.println("We use the \"object for name\" method here, although it is not recommended.");
        try{
            String s = model.getFormattedDescription(Ellipsoid.class, "Krassowsky 1940");
            System.out.println(s);
        } catch (Exception e){e.printStackTrace();}

    }

}
