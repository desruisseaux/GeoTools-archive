/* This file is part of the GeoWidgets project.
 * (C) 2005, Matthias Basler
 * This library is distributed under the GNU Lesser General Public License.
 * See http://www.opensource.org/licenses/lgpl-license.php or read
 * the license file for details.
 */
package org.geowidgets.crs.widgets.swing;

import java.awt.*;

import javax.swing.JPanel;

import org.MaBaUtils.ui.ComfortGBC;
import org.geowidgets.crs.model.EPSGEntry;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengis.referencing.cs.CartesianCS;
import org.opengis.referencing.operation.Conversion;

/** This widget is used to let the user choose a CRS
 * or build its own from CS, GeographicCRS and Projection.
 * 
 * @author Matthias Basler
 */
public class JProjectedCRSAssemblyPanel extends _AssemblyPanel {
    private static final long serialVersionUID = 3257005453816116276L;
    int dim;
    protected JCSAssemblyPanel csPanel;
    protected JGeographicCRSAssemblyPanel crsPanel;
    protected JProjectionAssemblyPanel proPanel;

    /** Creates a new widget to choose a CRS or build its own from CS,
     * GeographicCRS and Projection.
     * @param c the color of the customPanel or null to use the defaults
     * @param dim number of dimensions (2 or 3) */
    public JProjectedCRSAssemblyPanel(Color c, int dim) {
        super("x.PCRS", "x.PCRS.Custom", //$NON-NLS-1$//$NON-NLS-2$
                c, ProjectedCRS.class, Integer.toString(dim) + "D"); //$NON-NLS-1$
        this.dim = dim;
        this.initCustomPanel(this.custom);
    }

    protected void initCustomPanel(JPanel custom) {
        custom.setLayout(new GridBagLayout());
        ComfortGBC comboGBC = UI.getComboGBC();

        csPanel = new JCSAssemblyPanel(null, CartesianCS.class, 2);
        csPanel.setBackground(this.customPanelColor);
        int h = csPanel.dropdown.getPreferredSize().height;//Do not change height
        csPanel.dropdown.setPreferredSize(new Dimension(300, h));
        proPanel = new JProjectionAssemblyPanel(null);
        proPanel.setBackground(this.customPanelColor);
        crsPanel = new JGeographicCRSAssemblyPanel(null, this.dim);
        crsPanel.setBackground(this.customPanelColor);
        //crsPanel.setCustom(false);
        custom.add(csPanel, comboGBC.setPos(0, 0));
        custom.add(proPanel, comboGBC.setPos(0, 1));
        custom.add(crsPanel, comboGBC.setPos(0, 2));
    }

    /** @return the object selected or specified by the user.
     * @throws FactoryException if construction of the selected object failed. */
    public ProjectedCRS getCRS() throws FactoryException {
        Object temp = this.dropdown.getSelectedItem();
        EPSGEntry entry = (temp == null) ? EPSGEntry.OTHER : (EPSGEntry) temp;
        if (this.isCustom) {
            GeographicCRS gCRS = this.crsPanel.getCRS();
            CartesianCS cCS = (CartesianCS) this.csPanel.getCS();
            Conversion conv = this.proPanel.getConversion();
            return model.createProjectedCRS(entry.getName(), gCRS, cCS, conv);
        } else {
            try {
                return model.createProjectedCRS(entry.getCode());
            } catch (NoSuchAuthorityCodeException e) {
                e.printStackTrace();
                return null;
            }//Should not happen.
        }
    }

    /** Programmatically changes the selected/custom object. 
     * @param crs the new coordinate reference system to select from the list or to
     * add to the dropdown if it is not yet in the list. */
    public void setCRS(ProjectedCRS crs) {
        EPSGEntry entry = model.getEntryFor(crs);
        super.setToObject(entry);

        Conversion conv = crs.getConversionFromBase();
        this.proPanel.setConversion(conv);
        this.csPanel.setCS(crs.getCoordinateSystem());
        this.crsPanel.setCRS((GeographicCRS) crs.getBaseCRS());
    }
}
