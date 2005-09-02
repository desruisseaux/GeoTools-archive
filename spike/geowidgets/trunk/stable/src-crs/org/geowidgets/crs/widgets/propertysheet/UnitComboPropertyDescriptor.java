/* This file is part of the GeoWidgets project.
 * (C) 2005, Matthias Basler
 * This library is distributed under the GNU Lesser General Public License.
 * See http://www.opensource.org/licenses/lgpl-license.php or read
 * the license file for details.
 */
package org.geowidgets.crs.widgets.propertysheet;

import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.geowidgets.framework.GWFactoryFinder;
import org.geowidgets.framework.Res;
import org.geowidgets.framework.basewidgets.propertysheet.GeneralComboCellEditor;
import org.geowidgets.framework.basewidgets.propertysheet.GeneralComboLabelProvider;
import org.geowidgets.units.model.IUnitModel;

/** A PropertyDescriptor preconfigured for managing and displaying units.
 *
 * @author Matthias Basler
 */
public class UnitComboPropertyDescriptor extends PropertyDescriptor {
    /** All units: linear and angular. */
    public static int UNIT_ALL = IUnitModel.UNIT_ALL;
    /** Linear units only. */
    public static int UNIT_LINEAR = IUnitModel.UNIT_LINEAR;
    /** Angular units only. */
    public static int UNIT_ANGULAR = IUnitModel.UNIT_ANGULAR;

    protected static IUnitModel model = GWFactoryFinder.getUnitModel();
    protected List<String> entries;

    /** Creats a new PropertyDescriptor preconfigured for managing and displaying
     * units.
     * @param id the property's ID (name) 
     * @param unitType what types of units are to be shown in the dropdown
     * Use the <code>IUnitModel.UNIT_XXX</code> codes, such as f.e.
     * <code>UNIT_LINEAR</code> or <code>UNIT_ANGULAR</code>.
     * (This object contains copies of these constants.)
     */
    public UnitComboPropertyDescriptor(Object id, int unitType) {
        super(id, getLabel(unitType));
        this.entries = model.getSupportedUnits(unitType);
    }

    protected static String getLabel(int unitType) {
        if (unitType == model.UNIT_LINEAR) return Res.get(Res.CRS, "x.UnitLinear"); //$NON-NLS-1$
        else if (unitType == model.UNIT_ANGULAR) return Res.get(Res.CRS, "x.UnitAngular"); //$NON-NLS-1$
        else return Res.get(Res.CRS, "x.Unit"); //$NON-NLS-1$
    }

    public CellEditor createPropertyEditor(Composite parent) {
        CellEditor editor = new GeneralComboCellEditor<String>(
                parent, entries, false, this.getLabelProvider());
        if (getValidator() != null) editor.setValidator(getValidator());
        return editor;
    }

    public ILabelProvider getLabelProvider() {
        if (isLabelProviderSet()) return super.getLabelProvider();
        else return new GeneralComboLabelProvider();
    }
}
