/* This file is part of the GeoWidgets project.
 * (C) 2005, Matthias Basler
 * This library is distributed under the GNU Lesser General Public License.
 * See http://www.opensource.org/licenses/lgpl-license.php or read
 * the license file for details.
 */
package org.geowidgets.crs.widgets.swing;

import java.util.*;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.units.Unit;

import org.geowidgets.framework.GWFactoryFinder;
import org.geowidgets.framework.ui.GeneralSwingUIFactory;
import org.geowidgets.units.model.IUnitModel;
import org.opengis.referencing.FactoryException;

/** A dropdown field that shows available units, lets the user select one
 * of these and can be queried for the selected <code>javax.units.Unit</code>.
 * 
 * @author Matthias Basler
 */
public class JUnitComboBox extends JComboBox {
    private static final long serialVersionUID = 3257854281171677235L;
    protected GeneralSwingUIFactory uiFactory 
            = GWFactoryFinder.getGeneralSwingUIFactory();
    protected int unitType;
    protected boolean sorted;
    protected Unit customUnit;
    String customName;
    protected IUnitModel model = GWFactoryFinder.getUnitModel();

    /** Creates a new dropdown, filled with units to select one from.
     * @param unitType One of the IUnitModel.UNIT_XXX constants,
     * e.g. IUnitModel.UNIT_LINEAR.
     * @param sorted If the dropdown list shall be sorted by name. */
    public JUnitComboBox(int unitType, boolean sorted) {
        super();
        uiFactory.customize(this);

        //Filling the model
        List<String> l = model.getSupportedUnits(unitType);
        if (sorted) Collections.sort(l);
        super.setModel(new DefaultComboBoxModel(new Vector<String>(l)));

        String def = model.getDefaultUnit(unitType);
        this.setSelectedItem(def);

        this.unitType = unitType;
        this.sorted = sorted;
    }

    /** @return the unit which the user selected in the dropdown. */
    public Unit getSelectedUnit() {
        String u = super.getSelectedItem().toString();
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
     * @param u the new unit to select from /insert into the dropdown */
    public void setSelectedUnit(Unit u) {
        /* We test each unit object from the dropdown for similarity. Since
         * units do not carry a name this is one working method of testing
         * if a unit is already in the list.
         * Alternatively we could use the model.getName(...) function
         * and set the dropdown to this name... */
        boolean success = false;
        try{
            for (int i = 0; i < this.getItemCount(); ++i) {
                if (model.getUnit((String)this.getItemAt(i)).equals(u)) {
                    success = true;
                    this.setSelectedIndex(i);
                    break;
                }
            }
        } catch (Exception e){} //No problem...
        if (!success) {
            this.customName = u.toString();
            this.customUnit = u;
            ((DefaultComboBoxModel) this.getModel()).addElement(this.customName);
            //Vector<String> v = getList(this.model, this.unitType, this.sorted);
            //v.add(this.customName);
            //super.setModel(new DefaultComboBoxModel(v));
            super.setSelectedItem(this.customName);
        }
    }
}
