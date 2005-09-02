/* This file is part of the GeoWidgets project.
 * (C) 2005, Matthias Basler
 * This library is distributed under the GNU Lesser General Public License.
 * See http://www.opensource.org/licenses/lgpl-license.php or read
 * the license file for details.
 */
package org.geowidgets.crs.widgets.swing;

import java.awt.Color;

import org.geowidgets.crs.model.EPSGEntry;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.operation.Conversion;

/** Allows the user to select a projection (a transformation
 * from geographic to cartesian coordinates) or to create a custom
 * projection based on available standard transformations with
 * custom parameters.
 * @author Matthias Basler
 */
public class JProjectionAssemblyPanel extends _AssemblyPanel {
    private static final long serialVersionUID = -5130834299587708096L;

    /** Creates a new widget to select coordinate systems
     * @param c the color of the customPanel or null to use the defaults */
    public JProjectionAssemblyPanel(Color c) {
        super("x.PRO", "x.PRO.Custom", //$NON-NLS-1$//$NON-NLS-2$
                c, Conversion.class, null);
        this.initCustomPanel();//this.custom
    }

    protected void initCustomPanel() {//JPanel custom
        //We accept only standard projections so we don't need to fill the panel "custom".
        this.setCustomPossible(false);
    }

    /** @return the object selected or specified by the user.
     * @throws FactoryException if construction of the selected object failed. */
    public Conversion getConversion() throws FactoryException {
        Object temp = this.dropdown.getSelectedItem();
        EPSGEntry entry = (temp == null) ? EPSGEntry.OTHER : (EPSGEntry) temp;
        if (this.isCustom) {//Should not happen
            //We do not accept custom projections (yet)
            return null;
        } else {
            try {
                return model.createConversion(entry.getCode());
            } catch (NoSuchAuthorityCodeException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    /** Programmatically changes the selected/custom object. 
     * @param conv the new conversion (projection) to select from the list or to
     * add to the dropdown if it is not yet in the list. */
    public void setConversion(Conversion conv) {
        EPSGEntry entry = model.getEntryFor(conv);
        super.setToObject(entry);
    }
}
