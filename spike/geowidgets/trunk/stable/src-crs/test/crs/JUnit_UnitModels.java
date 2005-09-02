/* This file is part of the GeoWidgets project.
 * (C) 2005, Matthias Basler
 * This library is distributed under the GNU Lesser General Public License.
 * See http://www.opensource.org/licenses/lgpl-license.php or read
 * the license file for details.
 */
package test.crs;

import java.util.List;

import javax.units.Unit;

import junit.framework.TestCase;

import org.geowidgets.units.model.*;

/** Tests the two implementations of IUnitModel.*/
public class JUnit_UnitModels extends TestCase {
    IUnitModel epsgModel;
    IUnitModel unitModel;
    
    protected void setUp() throws Exception{
        super.setUp();
        System.out.println("************************************");
        try{
            this.epsgModel = EPSG_UnitModel.getDefault();
            this.unitModel = Units_UnitModel.getDefault();        
        } catch (Exception e) {fail("Error creating models");}
    }
    
    

    /** Instantiates the two IUnitModel implementations. <p/>
     * Test method for 'EPSG_UnitModel.getDefault()'
     */
    public void testGetDefault() {
        assertNotNull("EPSG unit model is null.", epsgModel);
        assertNotNull("Units unit model is null.", unitModel);
    }
    
    /** Test method for '_UnitCombiBoxModel.getSupportedUnits(int)'
     */
    public void testGetSupportedUnits() {
        List<String> epsglist1 = this.epsgModel.getSupportedUnits(IUnitModel.UNIT_ALL);
        List<String> epsglist2 = this.epsgModel.getSupportedUnits(IUnitModel.UNIT_LINEAR);
        List<String> epsglist3 = this.epsgModel.getSupportedUnits(IUnitModel.UNIT_ANGULAR);
        List<String> unitslist1 = this.unitModel.getSupportedUnits(IUnitModel.UNIT_ALL);
        List<String> unitslist2 = this.unitModel.getSupportedUnits(IUnitModel.UNIT_LINEAR);
        List<String> unitslist3 = this.unitModel.getSupportedUnits(IUnitModel.UNIT_ANGULAR);
        String err = "List of supported units cannot be null or empty.";
        assertTrue(err, epsglist1 != null & epsglist1.size() != 0);
        assertTrue(err, epsglist2 != null & epsglist2.size() != 0);
        assertTrue(err, epsglist3 != null & epsglist3.size() != 0);
        assertTrue(err, unitslist1 != null & unitslist1.size() != 0);
        assertTrue(err, unitslist2 != null & unitslist2.size() != 0);
        assertTrue(err, unitslist3 != null & unitslist3.size() != 0);
        assertTrue("Total units >= Angular + Linear units",
                epsglist1.size() >= epsglist2.size() + epsglist3.size());
        assertTrue("Total units >= Angular + Linear units",
                unitslist1.size() >= unitslist2.size() + unitslist3.size());
        System.out.println("Supported objects returned (all/linear/angular):");
        System.out.println("  EPSG: " + epsglist1.size() + "/"  + epsglist2.size() + "/" + epsglist3.size());
        System.out.println("  Units: " + unitslist1.size() + "/"  + unitslist2.size() + "/" + unitslist3.size());
    }    
    
    /** Test method for 'EPSG_UnitModel.getDefaultUnit(int)'
     */
    public void testGetDefaultUnit() {
        String defEPSG = this.epsgModel.getDefaultUnit(IUnitModel.UNIT_ALL);
        String defEPSG_L = this.epsgModel.getDefaultUnit(IUnitModel.UNIT_LINEAR);
        String defEPSG_A = this.epsgModel.getDefaultUnit(IUnitModel.UNIT_ANGULAR);
        String defUnits = this.unitModel.getDefaultUnit(IUnitModel.UNIT_ALL);
        String defUnits_L = this.unitModel.getDefaultUnit(IUnitModel.UNIT_LINEAR);
        String defUnits_A = this.unitModel.getDefaultUnit(IUnitModel.UNIT_ANGULAR);
        String err = "Default unit must not be null or empty.";
        assertTrue(err, defEPSG != null && !defEPSG.equals(""));
        assertTrue(err, defEPSG_L != null && !defEPSG_L.equals(""));
        assertTrue(err, defEPSG_A != null && !defEPSG_A.equals(""));
        assertTrue(err, defUnits != null && !defUnits.equals(""));
        assertTrue(err, defUnits_L != null && !defUnits_L.equals(""));
        assertTrue(err, defUnits_A != null && !defUnits_A.equals(""));
        System.out.println("Default unit's names (all/linear/angular):");
        System.out.println("  EPSG: " + defEPSG + "/"  + defEPSG_L + "/" + defEPSG_A);
        System.out.println("  Units: " + defUnits + "/"  + defUnits_L + "/" + defUnits_A);
    }
    
    /** Test method for '_UnitCombiBoxModel.getUnit(String)'
     */
    public void testGetUnit() {
        try{
            Unit u1 = this.epsgModel.getUnit(this.epsgModel.getDefaultUnit(IUnitModel.UNIT_ALL));
            Unit u2 = this.epsgModel.getUnit(this.epsgModel.getDefaultUnit(IUnitModel.UNIT_LINEAR));
            Unit u3 = this.epsgModel.getUnit(this.epsgModel.getDefaultUnit(IUnitModel.UNIT_ANGULAR));
            Unit u4 = this.unitModel.getUnit(this.unitModel.getDefaultUnit(IUnitModel.UNIT_ALL));
            Unit u5 = this.unitModel.getUnit(this.unitModel.getDefaultUnit(IUnitModel.UNIT_LINEAR));
            Unit u6 = this.unitModel.getUnit(this.unitModel.getDefaultUnit(IUnitModel.UNIT_ANGULAR));
            String err = "Default unit was not instantiated.";
            assertNotNull(err, u1);
            assertNotNull(err, u2);
            assertNotNull(err, u3);
            assertNotNull(err, u4);
            assertNotNull(err, u5);
            assertNotNull(err, u6);
            System.out.println("Default created unit's abbreviations (all/linear/angular):");
            System.out.println("  EPSG: " + u1 + "/"  + u2 + "/" + u3);
            System.out.println("  Units: " + u4 + "/"  + u5 + "/" + u6);            
        } catch (Exception e){
            e.printStackTrace();
            fail();
        }
    }
}
