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
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.cs.EllipsoidalCS;
import org.opengis.referencing.datum.GeodeticDatum;

/** Allows the user to select a projected coordinate reference system
 * or to create one from its parts (e.g. a geographic CRS, a projection).
 * @author Matthias Basler
 */
public class JGeographicCRSAssemblyPanel extends _AssemblyPanel {
    private static final long serialVersionUID = -6435132214220446972L;
    int dim;
    protected JCSAssemblyPanel csPanel;
    protected JGeodeticDatumAssemblyPanel gdPanel;

    /** Creates a new widget to select geographic CRS
     * @param c the color of the customPanel or null to use the defaults
     * @param dim number of dimensions (2 or 3) */
    public JGeographicCRSAssemblyPanel(Color c, int dim) {
        super("x.GCRS", "x.GCRS.Custom", //$NON-NLS-1$//$NON-NLS-2$
                c, GeographicCRS.class, Integer.toString(dim) + "D"); //$NON-NLS-1$
        this.dim = dim;
        this.initCustomPanel(this.custom);
    }

    protected void initCustomPanel(JPanel custom) {
        custom.setLayout(new GridBagLayout());
        ComfortGBC comboGBC = UI.getComboGBC();

        csPanel = new JCSAssemblyPanel(null, EllipsoidalCS.class, 2);
        csPanel.setBackground(this.customPanelColor);
        gdPanel = new JGeodeticDatumAssemblyPanel(null);
        gdPanel.setBackground(this.customPanelColor);
        custom.add(csPanel, comboGBC.setPos(0, 0));
        custom.add(gdPanel, comboGBC.setPos(0, 1));
    }

    /** @return the object selected or specified by the user.
     * @throws FactoryException if construction of the selected object failed. */
    public GeographicCRS getCRS() throws FactoryException {
        Object temp = this.dropdown.getSelectedItem();
        EPSGEntry entry = (temp == null) ? EPSGEntry.OTHER : (EPSGEntry) temp;
        if (this.isCustom) {
            EllipsoidalCS eCS = (EllipsoidalCS) this.csPanel.getCS();
            GeodeticDatum gd = this.gdPanel.getGeodeticDatum();
            return model.createGeographicCRS(entry.getName(), gd, eCS); //$NON-NLS-1$
        } else {
            try {
                return model.createGeographicCRS(entry.getCode()); //$NON-NLS-1$
            } catch (NoSuchAuthorityCodeException e) {
                e.printStackTrace();
                return null;
            }//Should not happen.
        }
    }

    /** Programmatically changes the selected/custom object. 
     * @param crs the new geographic CRS to select from the list or to
     * add to the dropdown if it is not yet in the list. */
    public void setCRS(GeographicCRS crs) {
        EPSGEntry entry = model.getEntryFor(crs);
        super.setToObject(entry);
        this.csPanel.setCS(crs.getCoordinateSystem());
        this.gdPanel.setGeodeticDatum((GeodeticDatum) crs.getDatum());
    }
}
