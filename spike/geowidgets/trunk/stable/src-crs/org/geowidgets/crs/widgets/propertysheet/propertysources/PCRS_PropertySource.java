/* This file is part of the GeoWidgets project.
 * (C) 2005, Matthias Basler
 * This library is distributed under the GNU Lesser General Public License.
 * See http://www.opensource.org/licenses/lgpl-license.php or read
 * the license file for details.
 */
package org.geowidgets.crs.widgets.propertysheet.propertysources;

import java.util.List;

import org.eclipse.ui.views.properties.*;
import org.geowidgets.crs.model.EPSGEntry;
import org.geowidgets.framework.Res;
import org.geowidgets.framework.basewidgets.propertysheet.GeneralComboPropertyDescriptor;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengis.referencing.cs.CartesianCS;
import org.opengis.referencing.cs.EllipsoidalCS;
import org.opengis.referencing.operation.Conversion;
import org.opengis.util.InternationalString;

/** A PropertySource for projected CRS objects. */
public class PCRS_PropertySource extends _CRS_PropertySource<ProjectedCRS> {
    protected static final String PROPERTY_SCOPE = "Scope"; //$NON-NLS-1$
    protected static final String PROPERTY_PROJ = "PROJ"; //$NON-NLS-1$
    protected static final String PROPERTY_CS = "CS"; //$NON-NLS-1$
    protected static final String PROPERTY_GCRS = "GCRS"; //$NON-NLS-1$

    //Parameters:
    protected Conversion conv;
    protected CartesianCS cs;
    protected GeographicCRS gCRS; 
    
    /** Creates a new PropertySource for describing a ProjectedCRS object.
     * @param pCRS the initial object to be described.
     * @param prov the parent PropertySource povider, in case child objects
     * need to get modified.  
     * @param showNameField if <code>true</code>, the first row will show the
     * object's name. If <code>false</code> this row is ommitted. This might
     * be useful if the parent object already shows this object's name as one
     * of its attributes.
     */
    public PCRS_PropertySource(ProjectedCRS pCRS, IPropertySourceProvider prov,
            boolean showNameField) {
        super("2D", prov, showNameField); //$NON-NLS-1$
        setCRS(pCRS);
    }

//  Value-----------------------------------------------------------------------
    
    /** Changes the currently selected object and keeps its
     * parameters synchronized.
     * @param object the new value for the ProjectedCRS described hereby. */
    public void setCRS(ProjectedCRS object){
        if (object == null) return;
        this.object = object;
        this.entry = EPSGEntry.getEntryFor(object);//super.getName(el);
        this.conv = object.getConversionFromBase();
        this.cs = (CartesianCS)object.getCoordinateSystem();
        this.gCRS = (GeographicCRS)object.getBaseCRS();
    }
    
    public void createObjectFromCode(String code) throws FactoryException{
        setCRS(crsModel.createProjectedCRS(code));
    }

    public void createObjectFromParameters() throws FactoryException{
        //If the last selected object was a standard object, switch to "Custom..."
        if (!this.entry.isCustom()) this.entry = EPSGEntry.OTHER;
        setCRS(crsModel.createProjectedCRS(this.entry.getName(),
                this.gCRS, this.cs, this.conv));        
    }
    
//  Get/SetValue----------------------------------------------------------------
    public Object getPropertyValue(Object id) {
        if (id.equals(PROPERTY_NAME))
            return getName(this.object);
        else if (id.equals(PROPERTY_CODE))
            return getCode(this.object);
        else if (id.equals(PROPERTY_PROJ))
            return this.object.getConversionFromBase();
        else if (id.equals(PROPERTY_CS))
            return (CartesianCS)this.object.getCoordinateSystem();
        else if (id.equals(PROPERTY_GCRS))
            return (GeographicCRS)object.getBaseCRS();
        else if (id.equals(PROPERTY_SCOPE)){
            InternationalString scope = this.object.getScope(); 
            return (scope == null)? UNKNOWN : scope.toString();
        } else if (id.equals(PROPERTY_AREA))
            return getAreaDescription(this.object.getValidArea());
        else return null;
    }

    public void setPropertyValue(Object id, Object value) {
        try{
            //Checks for name and code. If work was already done there, exit.
            if (super.setNameOrCode(id, value)) return;
            if (id.equals(PROPERTY_PROJ)){
                Conversion temp = updateChild(Conversion.class, this.conv, value);
                if (temp != this.conv){
                    this.conv = temp;
                    this.createObjectFromParameters();
                }
            } else if (id.equals(PROPERTY_CS)) {
                CartesianCS temp = updateChild(CartesianCS.class, this.cs, value);
                if (temp != this.cs){
                    this.cs = temp;
                    this.createObjectFromParameters();
                }
            } else if (id.equals(PROPERTY_GCRS)){
                GeographicCRS temp = updateChild(GeographicCRS.class, this.gCRS, value);
                if (temp != this.gCRS){
                    this.gCRS = temp;
                    this.createObjectFromParameters();
                }
            }
        } catch (FactoryException e){
            LOGGER.throwing("PCRS_PropertySource", "setPropertyValue", e); //$NON-NLS-1$ //$NON-NLS-2$
        } 
    }
    
//  Descriptors-----------------------------------------------------------------  
    public IPropertyDescriptor[] getPropertyDescriptors() {
        if (propertyDescriptors == null) {
            List<IPropertyDescriptor> result = getBasicPropertyDescriptors(true);
            
            List<EPSGEntry> convEntries = getSortedEntries(
                    Conversion.class, "2D", false); //$NON-NLS-1$
            PropertyDescriptor convDescriptor = new GeneralComboPropertyDescriptor<EPSGEntry>(
                    PROPERTY_PROJ, Res.get(Res.CRS, "x.PRO"), convEntries, false); //$NON-NLS-1$
            convDescriptor.setLabelProvider(super.LABEL_CRS);
            result.add(convDescriptor);
            List<EPSGEntry> csEntries = getSortedEntries(
                    EllipsoidalCS.class, "2D", false); //$NON-NLS-1$
            PropertyDescriptor csDescriptor = new GeneralComboPropertyDescriptor<EPSGEntry>(
                    PROPERTY_CS, Res.get(Res.CRS, "x.CS"), csEntries, false); //$NON-NLS-1$
            csDescriptor.setLabelProvider(super.LABEL_CRS);
            result.add(csDescriptor);
            List<EPSGEntry> gCRSEntries = getSortedEntries(
                    GeographicCRS.class, "2D", true); //$NON-NLS-1$
            PropertyDescriptor gdDescriptor = new GeneralComboPropertyDescriptor<EPSGEntry>(
                    PROPERTY_GCRS, Res.get(Res.CRS, "x.GCRS"), gCRSEntries, true); //$NON-NLS-1$
            gdDescriptor.setLabelProvider(super.LABEL_CRS);
            result.add(gdDescriptor);

            result.add(new PropertyDescriptor(
                    PROPERTY_SCOPE, Res.get(Res.CRS, "x.Scope"))); //$NON-NLS-1$
            result.add(new PropertyDescriptor(
                    PROPERTY_AREA, Res.get(Res.CRS, "x.AreaOfUse"))); //$NON-NLS-1$
            
            propertyDescriptors = result.toArray(new IPropertyDescriptor[result.size()]);
        }
        return propertyDescriptors;
    }
    
}
