/* This file is part of the GeoWidgets project.
 * (C) 2005, Matthias Basler
 * This library is distributed under the GNU Lesser General Public License.
 * See http://www.opensource.org/licenses/lgpl-license.php or read
 * the license file for details.
 */
package org.geowidgets.crs.widgets.propertysheet.propertysources;

import java.util.List;

import javax.units.Unit;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.geowidgets.crs.model.EPSGEntry;
import org.geowidgets.crs.widgets.propertysheet.UnitComboPropertyDescriptor;
import org.geowidgets.framework.Res;
import org.geowidgets.framework.basewidgets.propertysheet.FloatingPointPropertyDescriptor;
import org.geowidgets.units.model.IUnitModel;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.datum.PrimeMeridian;

/** A PropertySource for prime meridian objects. */
public class PM_PropertySource extends _CRS_PropertySource<PrimeMeridian>{
    protected static final String PROPERTY_LONGITUDE = "Longitude"; //$NON-NLS-1$
    protected static final String PROPERTY_UNIT = "Unit"; //$NON-NLS-1$

    protected IPropertyDescriptor[] propertyDescriptors;
    protected boolean custom;
    //Parameters: 
    protected double longitude;
    protected Unit aUnit;
    
    /** Creates a new PropertySource for describing a PrimeMeridian object.
     * @param pm the initial object to be described.
     * @param showNameField if <code>true</code>, the first row will show the
     * object's name. If <code>false</code> this row is ommitted. This might
     * be useful if the parent object already shows this object's name as one
     * of its attributes.
     */
    public PM_PropertySource(PrimeMeridian pm, boolean showNameField) {
        super("2D", null, showNameField); //$NON-NLS-1$
        setPrimeMeridian(pm);
    }
    /*
    public PM_PropertySource(PrimeMeridian pm) {
        super(null);
        setPrimeMeridian(pm);
    }*/
//  Value-----------------------------------------------------------------------    
    /** Changes the currently selected object and keeps its
     * parameters synchronized.
     * @param object the new value for the PrimeMeridian described hereby. */
    public void setPrimeMeridian(PrimeMeridian object){
        if (object == null) return;
        this.object = object;
        this.entry = EPSGEntry.getEntryFor(object);//super.getName(el);
        this.longitude = object.getGreenwichLongitude();
        this.aUnit = object.getAngularUnit();
    }
    
    protected void createObjectFromCode(String code) throws FactoryException{
        setPrimeMeridian(crsModel.createPrimeMeridian(code));
    }

    protected void createObjectFromParameters() throws FactoryException {
        //If the last selected object was a standard object, switch to "Custom..."
        if (!this.entry.isCustom()) this.entry = EPSGEntry.OTHER;
        setPrimeMeridian(crsModel.createPrimeMeridian(
                this.entry.getName(), this.longitude, this.aUnit));
    }

//  Get/SetValue----------------------------------------------------------------
    
    public Object getPropertyValue(Object id) {
        if (id.equals(PROPERTY_NAME))
            return getName(this.object);
        else if (id.equals(PROPERTY_CODE))
            return getCode(this.object);
        else if (id.equals(PROPERTY_LONGITUDE))
            return this.object.getGreenwichLongitude();
        else if (id.equals(PROPERTY_UNIT))
            try{
                return uModel.getUnitName(this.object.getAngularUnit());
            } catch (FactoryException fe) {return UNKNOWN;} //$NON-NLS-1$
        else return null;
    }
    
    public void setPropertyValue(Object id, Object value) {
        try{
            super.setNameOrCode(id, value);//Checks for name and code
            if (id.equals(PROPERTY_LONGITUDE)){
                this.longitude = (Double)value;
                this.createObjectFromParameters();                             
            } else if (id.equals(PROPERTY_UNIT)){
                this.aUnit = uModel.getUnit(value.toString());
                this.createObjectFromParameters();                               
            }
        } catch (FactoryException e){
            LOGGER.throwing("PM_PropertySource", "setPropertyValue", e); //$NON-NLS-1$ //$NON-NLS-2$
        } 
    }
    
//  Descriptors-----------------------------------------------------------------  
    public IPropertyDescriptor[] getPropertyDescriptors() {
        if (propertyDescriptors == null) {
            List<IPropertyDescriptor> result = getBasicPropertyDescriptors(true);
            
            result.add(new FloatingPointPropertyDescriptor(
                    PROPERTY_LONGITUDE, Res.get(Res.CRS, "x.GreenwichLong"), //$NON-NLS-1$
                    -360.0, 360.0));
            result.add(new UnitComboPropertyDescriptor(
                    PROPERTY_UNIT, IUnitModel.UNIT_ANGULAR));
            
            propertyDescriptors = result.toArray(new IPropertyDescriptor[result.size()]);
        }
        return propertyDescriptors;
    }
}
