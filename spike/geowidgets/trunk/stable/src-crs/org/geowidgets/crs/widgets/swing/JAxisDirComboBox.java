/* This file is part of the GeoWidgets project.
 * (C) 2005, Matthias Basler
 * This library is distributed under the GNU Lesser General Public License.
 * See http://www.opensource.org/licenses/lgpl-license.php or read
 * the license file for details.
 */
package org.geowidgets.crs.widgets.swing;

import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import org.geowidgets.crs.model.IAxisDirectionModel;
import org.geowidgets.framework.GWFactoryFinder;
import org.opengis.referencing.cs.AxisDirection;

/** A dropdown field that shows available units, lets the user select one
 * of these and can be queried for the selected <code>javax.units.Unit</code>.
 * 
 * @author Matthias Basler
 */
public class JAxisDirComboBox extends JComboBox {
    private static final long serialVersionUID = 6857689353842366748L;

    protected static final IAxisDirectionModel MODEL 
            = GWFactoryFinder.getAxisDirectionModel();

    AxisDirection customDir;
    String customName;

    /** Creates a new dropdown, filled with axis directions to select one from. */
    public JAxisDirComboBox() {
        super(MODEL.getSupportedAxisDirections());
    }

    /** @return the unit which the user selected in the dropdown. */
    public AxisDirection getSelectedAxisDirection() {
        if (super.getSelectedItem().equals(this.customName))
            return this.customDir;
        return MODEL.getAxisDirection(super.getSelectedIndex());
    }

    /** Programmatically changes the selected/custom object. 
     * @param dir the new axis direction to select from the list or to
     * add to the dropdown if it is not yet in the list. */
    public void setAxisDirection(AxisDirection dir) {
        /* We test each unit object from the dropdown for similarity.
         * Usually there should be a suitable candidate in the list. */
        boolean success = false;
        for (int i = 0; i < this.getItemCount(); ++i) {
            if (this.getSelectedAxisDirection().equals(dir)) {
                success = true;
                this.setSelectedIndex(i);
                break;
            }
        }
        if (!success) {
            this.customName = dir.toString();
            this.customDir = dir;
            Vector<String> items = MODEL.getSupportedAxisDirections();
            super.setModel(new DefaultComboBoxModel(items));
            super.addItem(this.customName);
            super.setSelectedItem(this.customName);
        }
    }
}
