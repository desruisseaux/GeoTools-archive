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
import org.opengis.referencing.datum.*;
import org.opengis.util.InternationalString;

/** A PropertySource for geodetic datums. */
public class GD_PropertySource extends _CRS_PropertySource<GeodeticDatum>{
    protected static final String PROPERTY_PM = "PM"; //$NON-NLS-1$
    protected static final String PROPERTY_EL = "EL"; //$NON-NLS-1$
    protected static final String PROPERTY_ORIGIN = "Origin"; //$NON-NLS-1$
    
    protected IPropertyDescriptor[] propertyDescriptors;
    //Parameters: 
    protected PrimeMeridian pm;
    protected Ellipsoid el;
    
    /** Creates a new PropertySource for describing a GeodeticDatum object.
     * @param gd the initial object to be described.
     * @param prov the parent PropertySource povider, in case child objects
     * need to get modified.  
     * @param showNameField if <code>true</code>, the first row will show the
     * object's name. If <code>false</code> this row is ommitted. This might
     * be useful if the parent object already shows this object's name as one
     * of its attributes.
     */    
    public GD_PropertySource(GeodeticDatum gd, IPropertySourceProvider prov,
            boolean showNameField) {
        super("2D", prov, showNameField); //$NON-NLS-1$
        setDatum(gd);
    }
    
    /*public GD_PropertySource(GeodeticDatum gd) {
        super(null);
        setDatum(gd);
    }*/

//  Value-----------------------------------------------------------------------    
    /** Changes the currently selected object and keeps its
     * parameters synchronized.
     * @param object the new value for the GeodeticDatum described hereby. */
    public void setDatum(GeodeticDatum object){
        if (object == null) return;
        this.object = object;
        this.entry = EPSGEntry.getEntryFor(object);//super.getName(el);
        this.pm = object.getPrimeMeridian();
        this.el = object.getEllipsoid();
    }
    
    protected void createObjectFromCode(String code) throws FactoryException{
        setDatum(crsModel.createGeodeticDatum(code));
    }

    protected void createObjectFromParameters() throws FactoryException {
        //If the last selected object was a standard object, switch to "Custom..."
        if (!this.entry.isCustom()) this.entry = EPSGEntry.OTHER;
        setDatum(crsModel.createGeodeticDatum(this.entry.getName(), this.el, this.pm));
    }
    
//  Get/SetValue----------------------------------------------------------------
    public Object getPropertyValue(Object id) {
        if (id.equals(PROPERTY_NAME))
            return getName(this.object);
        else if (id.equals(PROPERTY_CODE))
            return getCode(this.object);
        else if (id.equals(PROPERTY_PM))
            return this.object.getPrimeMeridian();
        else if (id.equals(PROPERTY_EL))
            return this.object.getEllipsoid();
        else if (id.equals(PROPERTY_ORIGIN)){
            InternationalString anchor = this.object.getAnchorPoint();
            if (anchor == null) return UNKNOWN;
            String origin = anchor.toString(); //$NON-NLS-1$
            int i = origin.indexOf("Fundamental point: "); //$NON-NLS-1$
            if (i == 0) origin = origin.substring(19);
            //If that didn't work, maybe this helps:
            origin.replace("Fundamental point:", ""); //$NON-NLS-1$ //$NON-NLS-2$
            return origin;
        } else if (id.equals(PROPERTY_AREA))
            return getAreaDescription(this.object.getValidArea());
        else return null;
    }

    public void setPropertyValue(Object id, Object value) {
        try{
            super.setNameOrCode(id, value);//Checks for name and code
            if (id.equals(PROPERTY_PM)){
                PrimeMeridian temp = updateChild(PrimeMeridian.class, this.pm, value);
                if (temp != this.pm){
                    this.pm = temp;     
                    this.createObjectFromParameters();
                }
            } else if (id.equals(PROPERTY_EL)){
                Ellipsoid temp = updateChild(Ellipsoid.class, this.el, value);
                if (temp != this.el){
                    this.el = temp;
                    this.createObjectFromParameters();
                }
            }
        } catch (FactoryException e){
            LOGGER.throwing("GD_PropertySource", "setPropertyValue", e); //$NON-NLS-1$ //$NON-NLS-2$
        } 
    }    

//  Descriptors-----------------------------------------------------------------  
    public IPropertyDescriptor[] getPropertyDescriptors() {
        if (propertyDescriptors == null) {
            List<IPropertyDescriptor> result = getBasicPropertyDescriptors(true);

            List<EPSGEntry> csEntries = getSortedEntries(
                    PrimeMeridian.class, null, true); //$NON-NLS-1$
            PropertyDescriptor pmDescriptor = new GeneralComboPropertyDescriptor<EPSGEntry>(
                    PROPERTY_PM, Res.get(Res.CRS, "x.PM"), csEntries, false); //$NON-NLS-1$
            pmDescriptor.setLabelProvider(super.LABEL_CRS);
            result.add(pmDescriptor);
            List<EPSGEntry> gdEntries = getSortedEntries(
                    Ellipsoid.class, null, true); //$NON-NLS-1$
            PropertyDescriptor elDescriptor = new GeneralComboPropertyDescriptor<EPSGEntry>(
                    PROPERTY_EL, Res.get(Res.CRS, "x.EL"), gdEntries, true); //$NON-NLS-1$
            elDescriptor.setLabelProvider(super.LABEL_CRS);
            result.add(elDescriptor);
            
//          Old solution: Name in the child entries 
            /*PropertyDescriptor pmDescriptor = new PropertyDescriptor(
                    PROPERTY_PM, Res.get(Res.CRS, "x.PM")); //$NON-NLS-1$
            pmDescriptor.setLabelProvider(super.LABEL_CRS);
            result.add(pmDescriptor);
            PropertyDescriptor elDescriptor = new PropertyDescriptor(
                    PROPERTY_EL, Res.get(Res.CRS, "x.EL")); //$NON-NLS-1$
            elDescriptor.setLabelProvider(super.LABEL_CRS);
            result.add(elDescriptor);*/
            
            //Read only:
            result.add(new PropertyDescriptor(
                    PROPERTY_ORIGIN, Res.get(Res.CRS, "x.GD.Origin"))); //$NON-NLS-1$
            result.add(new PropertyDescriptor(
                    PROPERTY_AREA, Res.get(Res.CRS, "x.AreaOfUse"))); //$NON-NLS-1$
            
            propertyDescriptors = result.toArray(new IPropertyDescriptor[result.size()]);
        }
        return propertyDescriptors;
    }

}
