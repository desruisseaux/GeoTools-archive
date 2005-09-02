package test.crs;

import java.util.List;
import java.util.Set;

import javax.units.Unit;

import org.geotools.referencing.FactoryFinder;
import org.geowidgets.framework.Res;
import org.geowidgets.units.model.IUnitModel;
import org.geowidgets.units.model.Units_UnitModel;
import org.opengis.referencing.cs.CSAuthorityFactory;
import org.opengis.referencing.datum.DatumAuthorityFactory;
import org.opengis.util.InternationalString;


/** Use this test to determine if the connection to the EPSG database works.
 * This requires that the gt2-epsg-access.jar is in the class path and that
 * the EPSG database is installed as "EPSG".
 */
public class Test_Units {

    public synchronized static void main(String[] args) throws Exception{
        System.out.println("Test 1: Available datum factories");
        Set<DatumAuthorityFactory> dFactories = FactoryFinder.getDatumAuthorityFactories();
        for (DatumAuthorityFactory dFactory : dFactories){
            System.out.println("- " + dFactory.getAuthority().getTitle().toString());
        }
        if (dFactories.size() == 0){
            System.err.println("No datum factories found." +
                    "Check for the gt2-epsg-access.jar or gt2-epsg-hsql.jar plugin");
            return;
        }
        System.out.println("Test 2: Finding unit for 9036");
        CSAuthorityFactory csFactory = FactoryFinder.getCSAuthorityFactory("EPSG", null);
        if (csFactory == null){
            System.err.println("No EPSG CS factory found.");
            return;
        }
        try{
            Unit u = csFactory.createUnit("9036");
            System.out.println(u.toString());
        } catch (Exception e){e.printStackTrace();}
        
        System.out.println("Test 3: Finding unit \"kilometre\"");
        try{
            Unit u = csFactory.createUnit("kilometre");
            System.out.println(u.toString());
        } catch (Exception e){e.printStackTrace();}
        
        System.out.println("Test 4: Finding available codes");
        try{
            Set<String> codes = csFactory.getAuthorityCodes(Unit.class);
            if (codes.size() == 0) System.err.println("No codes found");
            for (String code : codes){
                System.out.print(code + "; ");
            }
            System.out.println();
        } catch (Exception e){e.printStackTrace();}
        
        System.out.println("Test 5: Finding available codes for angular units only");
        System.out.println("... not yet supported. Must be done by client.");
        /**
        try{
            Set<String> codes = csFactory.getAuthorityCodes(AngularUnit.class);
            if (codes.size() == 0) System.err.println("No codes found");
            for (String code : codes){
                System.out.print(code + "; ");
            }
            System.out.println();
        } catch (Exception e){e.printStackTrace();}        
        */
        
        System.out.println("Test 6: Finding the name for code 9036");
        try{
            InternationalString name = csFactory.getDescriptionText("9036");
            System.out.println(name.toString());
        } catch (Exception e){e.printStackTrace();}
        
        System.out.println("Test 7: Using UnitComboBoxModel to find available codes for angular units");
        IUnitModel model = Units_UnitModel.getDefault();
        List<String> codes = model.getSupportedUnits(model.UNIT_ANGULAR);
        for (String code : codes){
            System.out.print(code + "; ");
        }
        System.out.println();
        
        System.out.println("Test 8: Using UnitComboBoxModel to get Unit object for \"Kilometer\"");
        Unit u = model.getUnit("Kilometer");
        System.out.println(u.toString());
        
        System.out.println("Test 9: Testing Resource bundle function. Following error message is intended!");
        System.out.println(Res.get(Res.WIDGETS, "err.CreateUnit", "Kilometer")); //$NON-NLS-1$
    }
}
