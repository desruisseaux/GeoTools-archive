/* This file is part of the GeoWidgets project.
 * (C) 2005, Matthias Basler
 * This library is distributed under the GNU Lesser General Public License.
 * See http://www.opensource.org/licenses/lgpl-license.php or read
 * the license file for details.
 */
package org.geowidgets.crs.widgets.propertysheet.propertysources;

import java.util.List;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.geowidgets.framework.Res;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.Conversion;
import org.opengis.util.InternationalString;

//TODO Later support custom projections (operation method + custom parameters)
/** A PropertySource for projections ("Conversions"). <p/>
 * As for 2005-08-21 only default projections are supported.
 * No parameter setting/editing is possible. */
public class PROJ_PropertySource extends _CRS_PropertySource<Conversion> {
    protected static final String PROPERTY_METHOD = "OperationMethod"; //$NON-NLS-1$
    protected static final String PROPERTY_SCOPE = "Scope"; //$NON-NLS-1$

    protected IPropertyDescriptor[] propertyDescriptors;
    
    /** Creates a new PropertySource for describing a projection.
     * @param conv the initial object to be described.
     * @param showNameField if <code>true</code>, the first row will show the
     * object's name. If <code>false</code> this row is ommitted. This might
     * be useful if the parent object already shows this object's name as one
     * of its attributes.
     */
    public PROJ_PropertySource(Conversion conv, boolean showNameField) {
        super("2D", null, showNameField); //$NON-NLS-1$
        setProjection(conv);
    }    

    /** Changes the currently selected object and keeps its
     * parameters synchronized.
     * @param object the new value for the Conversion described hereby. */
    public void setProjection(Conversion object){
        if (object == null) return;
        this.object = object;
        //TODO Set children ... later
    }        

    public void createObjectFromCode(String code) throws FactoryException{
        setProjection(crsModel.createConversion(code));
    }

    public void createObjectFromParameters() throws FactoryException{
        // We do not yet support custom projections.
    }

//  Get/SetValue----------------------------------------------------------------
    public Object getPropertyValue(Object id) {
        if (id.equals(PROPERTY_NAME))
            return getName(this.object);
        else if (id.equals(PROPERTY_CODE))
            return getCode(this.object);
        else if (id.equals(PROPERTY_METHOD))
            return getName(this.object.getMethod());
        else if (id.equals(PROPERTY_SCOPE)){
            InternationalString scope = this.object.getScope(); 
            return (scope == null)? UNKNOWN : scope.toString();
        } else if (id.equals(PROPERTY_AREA))
            return getAreaDescription(this.object.getValidArea());
        else return null;
    }

    public void setPropertyValue(Object id, Object value) {
        try{
            super.setNameOrCode(id, value);//Checks for name and code
            //Otherwise there are currently only read-only properties.
        } catch (FactoryException e){
            LOGGER.throwing("EL_PropertySource", "setPropertyValue", e); //$NON-NLS-1$ //$NON-NLS-2$
        } 
    }    
    
//  Descriptors-----------------------------------------------------------------  
    public IPropertyDescriptor[] getPropertyDescriptors() {
        if (propertyDescriptors == null) {
            List<IPropertyDescriptor> result = getBasicPropertyDescriptors(false);
            
            result.add(new PropertyDescriptor(
                    PROPERTY_METHOD, Res.get(Res.CRS, "x.METHOD"))); //$NON-NLS-1$
            result.add(new PropertyDescriptor(
                    PROPERTY_SCOPE, Res.get(Res.CRS, "x.Scope"))); //$NON-NLS-1$
            result.add(new PropertyDescriptor(
                    PROPERTY_AREA, Res.get(Res.CRS, "x.AreaOfUse"))); //$NON-NLS-1$
            
            propertyDescriptors = result.toArray(new IPropertyDescriptor[result.size()]);
        }
        return propertyDescriptors;
    }    
}
