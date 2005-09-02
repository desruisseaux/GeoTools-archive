/* This file is part of the GeoWidgets project.
 * (C) 2005, Matthias Basler
 * This library is distributed under the GNU Lesser General Public License.
 * See http://www.opensource.org/licenses/lgpl-license.php or read
 * the license file for details.
 */
package org.geowidgets.crs.widgets.swt;

import java.util.Collections;
import java.util.List;

import javax.units.Unit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.geowidgets.framework.GWFactoryFinder;
import org.geowidgets.framework.ui.GeneralSWTUIFactory;
import org.geowidgets.units.model.IUnitModel;
import org.opengis.referencing.FactoryException;

/** A dropdown field that shows available units, lets the user select one
 * of these and can be queried for the selected <code>javax.units.Unit</code>.
 * 
 * @author Matthias Basler
 */
public class UnitComboBox {
    protected GeneralSWTUIFactory uiFactory = GWFactoryFinder.getGeneralSWTUIFactory();
    protected Combo combo;

    protected int unitType;
    protected boolean sorted;
    protected Unit customUnit;
    String customName;
    protected IUnitModel model = GWFactoryFinder.getUnitModel();

    /** Constructs a new SWT dropdown list for linear and/or angular units
     * @param parent the parent UI element
     * @param unitType use the IUnitModel.UNIT_XXX constants
     * @param sorted display an alphabetically sorted list?
     */
    public UnitComboBox(Composite parent, int unitType, boolean sorted) {
        this.combo = new Combo(parent, SWT.DROP_DOWN);
        uiFactory.customizeJCombo(this.combo);
        //Fill the dropdown
        String def = model.getDefaultUnit(unitType);

        List<String> units = model.getSupportedUnits(unitType);
        if (sorted) Collections.sort(units);
        int i = 0;
        for (String unit : units) {
            combo.add(unit);
            if (unit.equals(def)) combo.select(i);
            ++i;
        }

        this.unitType = unitType;
        this.sorted = sorted;

    }

    /** @return the unit which the user selected in the dropdown. */
    public Unit getSelectedUnit() {
        String u = combo.getItem(combo.getSelectionIndex());
        if (u == this.customName) return this.customUnit;
        try {
            return model.getUnit(u);
        } catch (FactoryException e) {//Should not happen 
            e.printStackTrace();
            return null;
        }
    }

    /** Sets the dropdown to a specified object (given by its name) If the
     * given unit is not in the list it is appended as an additional element
     * in the list, thus avoiding to trow an exception. The unit's
     * <code>toString</code> method is then used to get the unit's "name".
     * @param u the new unit to display or include into the list */
    public void setSelectedUnit(Unit u) {
        /* We test each unit object from the dropdown for similarity. Since
         * units do not carry a name this is the only useful kind of testing
         * if a unit is already in the list. */
        boolean success = false;
        for (int i = 0; i < combo.getItemCount(); ++i) {
            if (this.getSelectedUnit().equals(u)) {
                success = true;
                combo.select(i);
                break;
            }
        }
        if (!success) {
            this.customName = u.toString();
            this.customUnit = u;
            combo.add(this.customName);
            combo.select(combo.getItemCount());
        }
    }

    /** @return the dropdown list used within this widget. */
    public Combo getCombo() {
        return this.combo;
    }
}
