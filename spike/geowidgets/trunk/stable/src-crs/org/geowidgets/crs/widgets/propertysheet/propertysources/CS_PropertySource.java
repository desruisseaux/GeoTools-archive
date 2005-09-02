/* This file is part of the GeoWidgets project.
 * (C) 2005, Matthias Basler
 * This library is distributed under the GNU Lesser General Public License.
 * See http://www.opensource.org/licenses/lgpl-license.php or read
 * the license file for details.
 */
package org.geowidgets.crs.widgets.propertysheet.propertysources;

import java.util.List;

import org.eclipse.ui.views.properties.*;
import org.geowidgets.framework.Res;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.cs.*;

//TODO Extend class to support 3D coordinate systems and custom axes.
/** A PropertySource for Coordinate Systems objects. It uses generics in order
 * to be specific for EllipsoidalCS or CartesianCS when needed.
 * As for 2005-08-21 only 2D CS support is supported.
 *
 * @author Matthias Basler
 * @param <CS> The specific type of CS to be handled (e.g. CartesianCS)
 */
public class CS_PropertySource<CS extends CoordinateSystem> extends
        _CRS_PropertySource<CS> {
    protected static final String PROPERTY_AXIS1 = "Axis1"; //$NON-NLS-1$
    protected static final String PROPERTY_AXIS2 = "Axis2"; //$NON-NLS-1$

    protected IPropertyDescriptor[] propertyDescriptors;

    /** Creates a new PropertySource for describing a CS object.
     * @param cs the initial object to be described.
     * @param prov the parent PropertySource povider, in case child objects
     * need to get modified.  
     * @param showNameField if <code>true</code>, the first row will show the
     * object's name. If <code>false</code> this row is ommitted. This might
     * be useful if the parent object already shows this object's name as one
     * of its attributes.
     */
    public CS_PropertySource(CS cs, IPropertySourceProvider prov, boolean showNameField) {
        super("2D", prov, showNameField); //$NON-NLS-1$
        setCS(cs);
    }

    /*
     public CS_PropertySource(CS cs) {
     super("2D"); //$NON-NLS-1$
     setCS(cs);
     }*/

    //  Value-----------------------------------------------------------------------        
    /** @return the currently selected coordinate system. */
    public CS getCS() {
        return this.object;
    }

    /** Changes the currently selected object and keeps its
     * parameters synchronized.
     * @param object the new value for the CS described hereby. */
    public void setCS(CS object) {
        if (object == null) return;
        this.object = object;
        //TODO Set children ... later
    }

    @SuppressWarnings("unchecked")//$NON-NLS-1$
    public void createObjectFromCode(String code) throws FactoryException {
        if (EllipsoidalCS.class.isAssignableFrom(this.object.getClass())) {
            setCS((CS) crsModel.createEllipsoidalCS(code));
        } else if (CartesianCS.class.isAssignableFrom(this.object.getClass())) {
            setCS((CS) crsModel.createCartesianCS(code));
        }
    }

    public void createObjectFromParameters() throws FactoryException {
        /* We don't support custom CS yet.
         if (EllipsoidalCS.class.isAssignableFrom(this.cs.getClass())){
         this.cs = (CS)model.createEllipsoidalCS(name);
         } else if (CartesianCS.class.isAssignableFrom(this.cs.getClass())){
         this.cs = (CS)model.createCartesianCS(name);
         }*/
    }

    //  Get/SetValue----------------------------------------------------------------
    public Object getPropertyValue(Object id) {
        if (id.equals(PROPERTY_NAME)) return getName(this.object);
        else if (id.equals(PROPERTY_CODE)) return getCode(this.object);
        else if (id.equals(PROPERTY_AXIS1)) return this.object.getAxis(0).getDirection()
                .name();
        else if (id.equals(PROPERTY_AXIS2)) return this.object.getAxis(1).getDirection()
                .name();
        else return null;
    }

    public void setPropertyValue(Object id, Object value) {
        try {
            super.setNameOrCode(id, value);//Checks for name and code
            //Otherwise there are currently only read-only properties.
        } catch (FactoryException e) {
            LOGGER.throwing("CS_PropertySource", "setPropertyValue", e); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    //  Descriptors-----------------------------------------------------------------  
    public IPropertyDescriptor[] getPropertyDescriptors() {
        if (propertyDescriptors == null) {
            List<IPropertyDescriptor> result = getBasicPropertyDescriptors(false);

            result.add(new PropertyDescriptor(PROPERTY_AXIS1,
                    Res.get(Res.CRS, "x.Axis.0"))); //$NON-NLS-1$
            result.add(new PropertyDescriptor(PROPERTY_AXIS2,
                    Res.get(Res.CRS, "x.Axis.1"))); //$NON-NLS-1$

            propertyDescriptors = result.toArray(new IPropertyDescriptor[result.size()]);
        }
        return propertyDescriptors;
    }
}
