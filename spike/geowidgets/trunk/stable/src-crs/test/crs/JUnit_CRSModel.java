package test.crs;

import java.util.List;

import javax.units.Unit;

import junit.framework.TestCase;

import org.geowidgets.crs.model.*;
import org.geowidgets.units.model.IUnitModel;
import org.geowidgets.units.model.Units_UnitModel;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengis.referencing.cs.*;
import org.opengis.referencing.datum.*;
import org.opengis.referencing.operation.Conversion;

/** Tests the implementation of ICRSModel. */
public class JUnit_CRSModel extends TestCase {
    ICRSModel model;

    protected void setUp() throws Exception {
        super.setUp();
        System.out.println("************************************");
        model = GeoTools_CRSModel.getDefault();
    }

    /** Test method for 'GeoTools_CRSModel.getDefault()'
     */
    public void testGetDefault() {
        assertNotNull("CRS model is null.", model);
    }

    
    /** Test method for 'GeoTools_CRSModel.getSupportedObjects(Class, String)'
     * and 'GeoTools_CRSModel.getDefaultObject(Class, String)'
     */
    public void testGetSupportedObjects() {
            checkEPSGEntries(Ellipsoid.class, null);
            checkEPSGEntries(PrimeMeridian.class, null);
            checkEPSGEntries(GeodeticDatum.class, null);
            checkEPSGEntries(EllipsoidalCS.class, null);
            checkEPSGEntries(EllipsoidalCS.class, "3D");
            checkEPSGEntries(CartesianCS.class, null);
            checkEPSGEntries(CartesianCS.class, "3D");
            checkEPSGEntries(Conversion.class, null);
            checkEPSGEntries(GeographicCRS.class, null);
            checkEPSGEntries(GeographicCRS.class, "3D");
            checkEPSGEntries(ProjectedCRS.class, null);
    }
    
    protected void checkEPSGEntries(Class cl, String hints){
        //Test default object
        EPSGEntry entry = model.getDefaultEntry(cl, hints);
        String classString = "Default entry for class " + cl.getName();
        assertNotNull(classString + "is null.", entry);
        System.out.println(classString + " is: (" + entry.getCode() + "): " + entry.getName());
        //Test supported objects
        try{
            List<EPSGEntry> list = model.getSupportedObjects(cl, hints);
            classString = "List of default objects for class " + cl.getName();
            assertNotNull(classString + "is null.", list);
            System.out.println(classString + " contains " + list.size() + " entries.");
        } catch (FactoryException e){
            e.printStackTrace();
            fail("Factory exception thrown when trying to get supported objects for \n  " + cl.getName());
        }
    }

    /** Test method for 'GeoTools_CRSModel.getFormattedDescription(Class, String)'
     * Tests if the descriptions of the default objects can be constructed.
     */
    public void testGetFormattedDescription() {
        checkDescription(Ellipsoid.class);
        checkDescription(PrimeMeridian.class);
        checkDescription(GeodeticDatum.class);
        checkDescription(EllipsoidalCS.class);
        checkDescription(CartesianCS.class);
        checkDescription(Conversion.class);
        checkDescription(GeographicCRS.class);
        checkDescription(ProjectedCRS.class);
    }

    protected void checkDescription(Class cl){
        String code = "[not yet determined]";
        try{
            EPSGEntry entry = model.getDefaultEntry(cl, null);
            String desc = model.getFormattedDescription(cl, entry.getCode());
            System.out.println("Description for \"" + entry.getName() + "\":");
            System.out.println(desc);
        } catch (FactoryException e){
            e.printStackTrace();
            fail("Factory exception thrown when trying to get description for \n  "
                    + code + " of class " + cl.getName());
        }        
    }
    
    /** Test method for 'GeoTools_CRSModel.getEntryFor(IdentifiedObject)'
     */
    public void testGetEntryFor() {
        System.out.println();
        System.out.println("\"getEntryFor(IdentifiedObject)\" is not yet tested.");
    }

    /** Test method for 'GeoTools_CRSModel.createXXX(code)' methods
     */
    public void testCreateFromCode() {
        Class cl = Object.class; 
        try{
            cl = Ellipsoid.class;
            checkObject(model.createEllipsoid(model.getDefaultEntry(cl, null).getCode()), cl);
            cl = PrimeMeridian.class;
            checkObject(model.createPrimeMeridian(model.getDefaultEntry(cl, null).getCode()), cl);
            cl = GeodeticDatum.class;
            checkObject(model.createGeodeticDatum(model.getDefaultEntry(cl, null).getCode()), cl);
            cl = EllipsoidalCS.class;
            checkObject(model.createEllipsoidalCS(model.getDefaultEntry(cl, null).getCode()), cl);
            cl = CartesianCS.class;
            checkObject(model.createCartesianCS(model.getDefaultEntry(cl, null).getCode()), cl);
            cl = GeographicCRS.class;
            checkObject(model.createGeographicCRS(model.getDefaultEntry(cl, null).getCode()), cl);
            cl = ProjectedCRS.class;
            checkObject(model.createProjectedCRS(model.getDefaultEntry(cl, null).getCode()), cl);
            cl = Conversion.class;
            checkObject(model.createConversion(model.getDefaultEntry(cl, null).getCode()), cl);
        } catch (FactoryException e){
            e.printStackTrace();
            fail("Factory exception thrown creating " + cl.getName() + " from code."); 
        }
    }
    
    protected void checkObject(IdentifiedObject o, Class cl){
        String classString = "Instantiated object for class" + cl.getName(); 
        assertNotNull(classString + "is null.", o);
        System.out.println(classString + " is: " + o.getName().getCode());
    }    

    /** Test method for 'GeoTools_CRSModel.createXXX(...)' methods
     */
    public void testCreate() {
        Unit lin = null, ang = null;
        try{
            IUnitModel m = Units_UnitModel.getDefault();
            lin = m.getUnit(m.getDefaultUnit(IUnitModel.UNIT_LINEAR));
            ang = m.getUnit(m.getDefaultUnit(IUnitModel.UNIT_ANGULAR));
        } catch (FactoryException e){
            e.printStackTrace();
            fail("Failing to create the units needed to build CRS objects.");
        }
        try{
            Ellipsoid o = model.createEllipsoid("Custom", "65432", "54321", null, lin);
            System.out.println(o.getSemiMinorAxis());
        } catch (FactoryException e){fail(e, "ellipsoid");}
        try{
            PrimeMeridian o = model.createPrimeMeridian("Custom", 12.0, ang);
            System.out.println(o.getGreenwichLongitude());
        } catch (FactoryException e){fail(e, "prime meridian");}
        try{
            Ellipsoid el = model.getDefaultObject(Ellipsoid.class, null);
            PrimeMeridian pm = model.getDefaultObject(PrimeMeridian.class, null);
            GeodeticDatum o = model.createGeodeticDatum("Custom", el, pm);
            System.out.println(o.getEllipsoid().getName().getCode());
        } catch (FactoryException e){fail(e, "geodetic datum");}
        try{
            GeodeticDatum gd = model.getDefaultObject(GeodeticDatum.class, null);
            EllipsoidalCS cs = model.getDefaultObject(EllipsoidalCS.class, null);
            GeographicCRS o = model.createGeographicCRS("Custom", gd, cs);
            System.out.println(o.toWKT());
        } catch (FactoryException e){fail(e, "geographic CRS");}
        try{
            CoordinateSystemAxis axis1 = model.createCoordinateSystemAxis("Custom", "EAST", AxisDirection.EAST, ang);
            CoordinateSystemAxis axis2 = model.createCoordinateSystemAxis("Custom", "NORTH", AxisDirection.NORTH, ang);
            EllipsoidalCS eCS = model.createEllipsoidalCS("Custom", axis1, axis2);
            axis1 = model.createCoordinateSystemAxis("Custom", "Right", AxisDirection.DISPLAY_RIGHT, lin);
            axis2 = model.createCoordinateSystemAxis("Custom", "Up", AxisDirection.DISPLAY_UP, lin);
            CartesianCS cCS = model.createCartesianCS("Custom", axis1, axis2);
            System.out.println(eCS.getAxis(1).getDirection().name());
            System.out.println(cCS.getAxis(1).getDirection().name());
        } catch (FactoryException e){fail(e, "coordinate system or axis");}        
        try{
            GeographicCRS crs = model.getDefaultObject(GeographicCRS.class, null);
            CartesianCS cs = model.getDefaultObject(CartesianCS.class, null);
            Conversion conv = model.getDefaultObject(Conversion.class, null);
            ProjectedCRS o = model.createProjectedCRS("Custom", crs, cs, conv);
            System.out.println(o.toWKT());
        } catch (FactoryException e){fail(e, "projected CRS");}        
    }
    
    protected void fail(Exception e, String className){
        e.printStackTrace();
        fail("Factory exception thrown creating " + className + " from parameters.");         
    }
}
