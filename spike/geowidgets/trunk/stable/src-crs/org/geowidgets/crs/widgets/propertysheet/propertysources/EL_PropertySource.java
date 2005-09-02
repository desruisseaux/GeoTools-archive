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
import org.opengis.referencing.datum.Ellipsoid;

/** A PropertySource for Ellipsoid objects. *
 * @author Matthias Basler
 */
public class EL_PropertySource extends _CRS_PropertySource<Ellipsoid>{
    protected static final String PROPERTY_A = "a"; //$NON-NLS-1$
    protected static final String PROPERTY_B = "b"; //$NON-NLS-1$
    protected static final String PROPERTY_INVFLAT = "InvFlat"; //$NON-NLS-1$
    protected static final String PROPERTY_UNIT = "Unit"; //$NON-NLS-1$

    protected IPropertyDescriptor[] propertyDescriptors;
    protected boolean custom;
    //Parameters: 
    protected double a, b, invFlat;
    protected Unit lUnit;
    protected boolean isIvfDefinitive;
    
    /** Creates a new PropertySource for describing a CS object.
     * @param el the initial object to be described.
     * @param showNameField if <code>true</code>, the first row will show the
     * object's name. If <code>false</code> this row is ommitted. This might
     * be useful if the parent object already shows this object's name as one
     * of its attributes.
     */
    public EL_PropertySource(Ellipsoid el, boolean showNameField) {
        super("2D", null, showNameField); //$NON-NLS-1$
        setEllipsoid(el);
    }
    /*
    public EL_PropertySource(Ellipsoid el) {
        super(null); //$NON-NLS-1$
        setEllipsoid(el);
    }*/
    
//  Value-----------------------------------------------------------------------
    
    /** Changes the currently selected object and keeps its
     * parameters synchronized.
     * @param object the new value for the Ellipsoid described hereby. */
    public void setEllipsoid(Ellipsoid object){
        if (object == null) return;
        this.object = object;
        this.entry = EPSGEntry.getEntryFor(object);//super.getName(el);
        this.a = object.getSemiMajorAxis();
        this.b = object.getSemiMinorAxis();
        this.invFlat = object.getInverseFlattening();
        this.isIvfDefinitive = object.isIvfDefinitive();
        this.lUnit = object.getAxisUnit();
    }
    
    protected void createObjectFromCode(String code) throws FactoryException{
        setEllipsoid(crsModel.createEllipsoid(code));
    }

    protected void createObjectFromParameters() throws FactoryException{
        //If the last selected object was a standard object, switch to "Custom..."
        if (!this.entry.isCustom()) this.entry = EPSGEntry.OTHER;
        if (this.isIvfDefinitive) this.b = 0;
        else this.invFlat = 0;
        setEllipsoid(crsModel.createEllipsoid(this.entry.getName(), this.a, this.b, this.invFlat, this.lUnit));
    }
    
//  Get/SetValue----------------------------------------------------------------
    
    public Object getPropertyValue(Object id) {
        if (id.equals(PROPERTY_NAME))
            return getName(this.object);
        else if (id.equals(PROPERTY_CODE))
            return getCode(this.object);
        else if (id.equals(PROPERTY_A))
            return this.object.getSemiMajorAxis();
        else if (id.equals(PROPERTY_B))
            return this.object.getSemiMinorAxis();
        else if (id.equals(PROPERTY_INVFLAT))
            return this.object.getInverseFlattening();
        else if (id.equals(PROPERTY_UNIT))
            try{
                return uModel.getUnitName(this.object.getAxisUnit());
            } catch (FactoryException fe) {return UNKNOWN;}
        else return null;
    }

    public void setPropertyValue(Object id, Object value) {
        try{
            super.setNameOrCode(id, value);//Checks for name and code
            if (id.equals(PROPERTY_A)){
                this.a = (Double)value;
                this.createObjectFromParameters();
            } else if (id.equals(PROPERTY_B)){
                this.b = (Double)value;
                this.isIvfDefinitive = false;
                this.createObjectFromParameters();                
            } else if (id.equals(PROPERTY_INVFLAT)){
                this.invFlat = (Double)value;
                this.isIvfDefinitive = true;
                this.createObjectFromParameters();                               
            } else if (id.equals(PROPERTY_UNIT)){
                this.lUnit = uModel.getUnit(value.toString());
                this.createObjectFromParameters();                               
            }
        } catch (FactoryException e){
            LOGGER.throwing("EL_PropertySource", "setPropertyValue", e); //$NON-NLS-1$ //$NON-NLS-2$
        } 
    }
    
//  Descriptors-----------------------------------------------------------------  
    public IPropertyDescriptor[] getPropertyDescriptors() {
        if (propertyDescriptors == null) {
            List<IPropertyDescriptor> result = getBasicPropertyDescriptors(true);
            
            result.add(new FloatingPointPropertyDescriptor(
                    PROPERTY_A, Res.get(Res.CRS, "x.a"), //$NON-NLS-1$
                    0, Double.MAX_VALUE));
            result.add(new FloatingPointPropertyDescriptor(
                    PROPERTY_B, Res.get(Res.CRS, "x.b"), //$NON-NLS-1$
                    0, Double.MAX_VALUE));
            result.add(new FloatingPointPropertyDescriptor(
                    PROPERTY_INVFLAT, Res.get(Res.CRS, "x.1f"), //$NON-NLS-1$
                    0, Double.MAX_VALUE));
            result.add(new UnitComboPropertyDescriptor(
                    PROPERTY_UNIT, IUnitModel.UNIT_LINEAR));
            
            propertyDescriptors = result.toArray(new IPropertyDescriptor[result.size()]);
        }
        return propertyDescriptors;
    }
}
