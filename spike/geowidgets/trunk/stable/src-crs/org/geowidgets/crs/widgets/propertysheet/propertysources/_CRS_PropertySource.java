/* This file is part of the GeoWidgets project.
 * (C) 2005, Matthias Basler
 * This library is distributed under the GNU Lesser General Public License.
 * See http://www.opensource.org/licenses/lgpl-license.php or read
 * the license file for details.
 */
package org.geowidgets.crs.widgets.propertysheet.propertysources;

import java.util.*;
import java.util.logging.Logger;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.ui.views.properties.*;
import org.geowidgets.crs.model.EPSGEntry;
import org.geowidgets.crs.model.ICRSModel;
import org.geowidgets.crs.widgets.propertysheet.EPSGCodeLabelProvider;
import org.geowidgets.framework.GWFactoryFinder;
import org.geowidgets.framework.Res;
import org.geowidgets.framework.basewidgets.propertysheet.GeneralComboPropertyDescriptor;
import org.geowidgets.units.model.IUnitModel;
import org.opengis.metadata.Identifier;
import org.opengis.metadata.extent.Extent;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.util.InternationalString;

/** Base class for all CRS-relate PropertySources. Contains lots of convenience
 * methods. For example the class automatically can create the name and code
 * descriptors and get/set their values. It also contains methods to get the
 * name and EPSG code from an object.
 *
 * @author Matthias Basler
 * @param <T> Some CRS-related object type (e.g. GeographicCRS, Ellipsoid, ...)
 */
public abstract class _CRS_PropertySource<T extends IdentifiedObject> implements
        IPropertySource {
    protected static final String PROPERTY_NAME = "Name"; //$NON-NLS-1$
    protected static final String PROPERTY_CODE = "Info.Code"; //$NON-NLS-1$
    protected static final String PROPERTY_AREA = "Area"; //$NON-NLS-1$
    protected static final String UNKNOWN = Res.get("err.Unknown"); //$NON-NLS-1$
    protected static final ICRSModel crsModel = GWFactoryFinder.getCRSModel();
    protected static final IUnitModel uModel = GWFactoryFinder.getUnitModel();
    protected static final Logger LOGGER = GWFactoryFinder.getLoggerFactory().getLogger();
    /** Label provider for the nested properties. */
    protected static final LabelProvider LABEL_CRS = new CRSLabelProvider();

    protected IPropertySourceProvider prov;
    protected IPropertyDescriptor[] propertyDescriptors;
    protected boolean showNameField = true;
    protected String hints;
    //Parameters
    protected T object;
    protected EPSGEntry entry;

    /** Creates a new Property source that stores the PropertySourceProvider
     * in order to directly access possible child entries.
     * @param hints hints in addition to the Class (given through Generics).
     * These hints (e.g. "2D" and "3D" or <code>null</code>) determine which
     * elements are loaded into the list of supported objects.
     * @param prov the parent <code>IPropertySourceProvider</code>
     * @param showNameField if <code>false</code>, the field "Name" is not shown,
     * only the field "Code" and the appropriate parameter fields. You should
     * only use <code>false</code> if this is a child entry and the parent
     * alredy has a name field for this object. */
    public _CRS_PropertySource(String hints, IPropertySourceProvider prov,
            boolean showNameField) {
        this.hints = hints;
        this.prov = prov;
        this.showNameField = showNameField;
    }

    /** Creates a new PropertySource. Since no PropertySourceProvider need to
     * be given as parameter, it will not be able to access child entries.
     * This PropertySource will show the object's name as the first row.
     * @param hints hints hints in addition to the Class (given through Generics).
     * These hints (e.g. "2D" and "3D" or <code>null</code>) determine which
     * elements are loaded into the list of supported objects.
     */
    public _CRS_PropertySource(String hints) {
        this.hints = hints;
    }

    //  Value-----------------------------------------------------------------------        
    /** @return the current object described by this Propertysource. This
     * returns the same as {@link #getEditableValue()}, but with usually
     * no casting required. */
    public T getCurrentValue() {
        return this.object;
    }

    public Object getEditableValue() {
        return this.object;
    }

    /** @param id the ID describing which property is meant
     * @param value the property's new value
     * @return true if the id is covered by the ones checked here, so no
     * further checks need to be done. 
     * @throws FactoryException */
    public boolean setNameOrCode(final Object id, Object value) throws FactoryException {
        if (id.equals(PROPERTY_NAME)) {
            //We get either an EPSGProperty (default entry) or a name (custom) returned
            if (value instanceof String) {
                this.entry = new EPSGEntry(value.toString());
                createObjectFromParameters();
            } else {
                if (value == null) value = EPSGEntry.OTHER;
                EPSGEntry entry = ((EPSGEntry) value);
                if (entry.isCustom()) {
                    this.entry = new EPSGEntry(value.toString());
                    createObjectFromParameters();
                } else createObjectFromCode(entry.getCode());
            }
            return true;
        } else if (id.equals(PROPERTY_CODE)) {
            //If the user selected "Custom...", do nothing.
            if (value instanceof EPSGEntry) {
                EPSGEntry entry = (EPSGEntry) value;
                if (!entry.isCustom()) createObjectFromCode(entry.getCode());
            }
            return true;
        }
        return false;
    }

    /** Updates one of this object's child objects to a new value. 
     * @param <T> Some CRS-related oject
     * @param cl the object class. This determines the output's class
     * @param child the child object to get changed.
     * @param value an EPSG entry or String (in case of non-EPSG objects)
     * that holds the new code resp. name of the child object. 
     * @return the updated child object. (F.e. if child was an Ellipsoid, this
     * returns a new Ellipsoid based on the EPSG entry or the name in the
     * value argument.
     */
    public <T extends IdentifiedObject> T updateChild(final Class<T> cl, final T child,
            Object value) {
        if (value instanceof String) value = new EPSGEntry(value.toString());
        if (value instanceof EPSGEntry) {
            EPSGEntry entry = (EPSGEntry) value;
            IPropertySource ps = this.prov.getPropertySource(child);
            ps.setPropertyValue(PROPERTY_NAME, entry);
            return cl.cast(ps.getEditableValue());
        } else {
            return cl.cast(value);
        }
    }

    protected abstract void createObjectFromCode(String code) throws FactoryException;

    protected abstract void createObjectFromParameters() throws FactoryException;

    @SuppressWarnings("unused") //$NON-NLS-1$
    public boolean isPropertySet(Object id) {
        return false; //NOTE: Implement?
    }

    @SuppressWarnings("unused") //$NON-NLS-1$
    public void resetPropertyValue(Object id) {
    } //NOTE: Implement?

    //  Helper functions------------------------------------------------------------    
    /** @return the first identifier, which usually is the object's code
     * or null of there are no identifiers. 
     * @param o some georeferencing-related object, such as CRS, CS, datums, ... */
    protected static String getCode(final IdentifiedObject o) {
        Iterator<Identifier> it = o.getIdentifiers().iterator();
        if (!it.hasNext()) return EPSGEntry.OTHER.getCode();
        return it.next().getCode();
    }

    /** @return the primary name of the object, (often defined by the EPSG database).
     * @param o some georeferencing-related object, such as CRS, CS, datums, ... */
    protected static String getName(final IdentifiedObject o) {
        return o.getName().getCode();
    }

    protected String getAreaDescription(Extent ex) {
        if (ex != null) {
            InternationalString area = ex.getDescription();
            return (area == null) ? Res.get("err.Unknown") : area.toString(); //$NON-NLS-1$
        } else return Res.get("err.Unknown"); //$NON-NLS-1$
    }

    //  Descriptors-----------------------------------------------------------------  
    /** @param allowCustom if <code>true</code>, a an entry "Custom ..." is
     * added to the list as the first entry.
     * @return the property descriptors for name and code, which are used
     * for almost every CRS class. */
    public List<IPropertyDescriptor> getBasicPropertyDescriptors(boolean allowCustom) {
        List<IPropertyDescriptor> result = new ArrayList<IPropertyDescriptor>();

        List<EPSGEntry> entries = getSortedEntries(object.getClass(), hints, allowCustom);
        if (showNameField) {
            PropertyDescriptor textDescriptor = new GeneralComboPropertyDescriptor<EPSGEntry>(
                    PROPERTY_NAME, Res.get(Res.CRS, "x.Name"), entries, true); //$NON-NLS-1$
            result.add(textDescriptor);
        }

        //Create a copy, since we sort differently now.
        entries = new ArrayList<EPSGEntry>(entries);
        Collections.sort(entries, EPSGEntry.COMPARE_CODE);
        PropertyDescriptor codeDescriptor = new GeneralComboPropertyDescriptor<EPSGEntry>(
                PROPERTY_CODE, Res.get(Res.CRS, "x.EPSGCode"), entries, false, //$NON-NLS-1$
                new EPSGCodeLabelProvider());
        result.add(codeDescriptor);

        return result;
    }

    protected List<EPSGEntry> getSortedEntries(Class<? extends IdentifiedObject> cl,
            String hints, boolean allowCustom) {
        List<EPSGEntry> entries;
        try {
            entries = crsModel.getSupportedObjects(cl, hints); //$NON-NLS-1$
        } catch (Exception e) {
            entries = new ArrayList<EPSGEntry>();
        }
        Collections.sort(entries);
        if (allowCustom) entries.add(0, EPSGEntry.OTHER);
        return entries;
    }

    //  Label provider--------------------------------------------------------------
    /** Label provider for the nested properties. */
    public static class CRSLabelProvider extends LabelProvider {
        /** Returns the CRS object's name. */
        public String getText(Object element) {
            if (element instanceof IdentifiedObject) {
                return getName((IdentifiedObject) element);
            } else if (element instanceof _CRS_PropertySource) {
                //This is the case for nested properties...
                IdentifiedObject o = ((_CRS_PropertySource) element).getCurrentValue();
                return getName(o);
            } else return super.getText(element);
        }
    }
}
