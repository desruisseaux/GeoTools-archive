/* This file is part of the GeoWidgets project.
 * (C) 2005, Matthias Basler
 * This library is distributed under the GNU Lesser General Public License.
 * See http://www.opensource.org/licenses/lgpl-license.php or read
 * the license file for details.
 */
package org.geowidgets.crs.widgets.swing;

import java.awt.Color;
import java.awt.GridBagLayout;

import javax.swing.JPanel;

import org.MaBaUtils.ui.ComfortGBC;
import org.geowidgets.crs.model.EPSGEntry;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.datum.*;

/** JEllipsoidAssemblyWidget
 * 
 * @author Matthias Basler
 */
public class JGeodeticDatumAssemblyPanel extends _AssemblyPanel {
    private static final long serialVersionUID = 3762811584606580793L;
    protected JPrimeMeridianAssemblyPanel pmPanel;
    protected JEllipsoidAssemblyPanel elPanel;

    /** Creates a new widget to select geodetic datums
     * @param c the color of the customPanel or null to use the defaults */
    public JGeodeticDatumAssemblyPanel(Color c) {
        super("x.GD", "x.GD.Custom",//$NON-NLS-1$//$NON-NLS-2$
                c, GeodeticDatum.class, null);
        this.initCustomPanel(this.custom);
    }

    protected void initCustomPanel(JPanel custom) {
        custom.setLayout(new GridBagLayout());
        ComfortGBC comboGBC = UI.getComboGBC();

        pmPanel = new JPrimeMeridianAssemblyPanel(null);
        pmPanel.setBackground(this.customPanelColor);
        elPanel = new JEllipsoidAssemblyPanel(null);
        elPanel.setBackground(this.customPanelColor);
        custom.add(pmPanel, comboGBC.setPos(0, 0));
        custom.add(elPanel, comboGBC.setPos(0, 1));
    }

    /** @return the object selected or specified by the user. 
     * @throws FactoryException if construction of the selected object failed. */
    public GeodeticDatum getGeodeticDatum() throws FactoryException {
        Object temp = this.dropdown.getSelectedItem();
        EPSGEntry entry = (temp == null) ? EPSGEntry.OTHER : (EPSGEntry) temp;
        if (this.isCustom) {
            Ellipsoid el = this.elPanel.getEllipsoid();
            PrimeMeridian pm = this.pmPanel.getPrimeMeridian();
            return model.createGeodeticDatum(entry.getName(), el, pm); //$NON-NLS-1$
        } else {
            try {
                return model.createGeodeticDatum(entry.getCode());
            } catch (NoSuchAuthorityCodeException e) {
                e.printStackTrace();
                return null;
            }//Should not happen.
        }
    }

    /** Programmatically changes the selected/custom object. 
     * @param gd the new geodetic datum to select from the list or to
     * add to the dropdown if it is not yet in the list. */
    public void setGeodeticDatum(GeodeticDatum gd) {
        EPSGEntry entry = model.getEntryFor(gd);
        super.setToObject(entry);
        this.elPanel.setEllipsoid(gd.getEllipsoid());
        this.pmPanel.setPrimeMeridian(gd.getPrimeMeridian());
    }
}
