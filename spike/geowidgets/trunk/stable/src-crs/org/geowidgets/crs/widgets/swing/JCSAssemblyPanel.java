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
import org.geowidgets.framework.Res;
import org.geowidgets.units.model.IUnitModel;
import org.opengis.referencing.*;
import org.opengis.referencing.cs.*;

/** This widget allows to select or assemble coordinate system. <p>
 * Currently this widget supports ellipsoidal and cartesian CS in 2D and 3D forms.
 * @author Matthias Basler
 */
public class JCSAssemblyPanel extends _AssemblyPanel {
    private static final long serialVersionUID = 5085442240260997972L;
    Class csType;
    int dim;
    protected JAxisAssemblyPanel axPanel0, axPanel1, axPanel2;

    /** Creates a new widget to select coordinate systems
     * @param c the color of the customPanel or null to use the defaults
     * @param csType the exact class of the coordinate system (e.g. EllipsoidalCS)
     * @param dim number of dimensions (2 or 3) */
    public JCSAssemblyPanel(Color c, Class<? extends IdentifiedObject> csType, int dim) {
        super("x.CS", "x.CS.Custom",//$NON-NLS-1$//$NON-NLS-2$
                c, csType, Integer.toString(dim) + "D"); //$NON-NLS-1$
        this.csType = csType;
        this.dim = dim;
        this.setToDefault();//We always start with the predefined CS. It's much easier.
        this.initCustomPanel(this.custom);
    }

    protected void initCustomPanel(JPanel custom) {
        custom.setLayout(new GridBagLayout());
        ComfortGBC comboGBC = UI.getComboGBC();

        int unitType = (CartesianCS.class.isAssignableFrom(cl)) ?
                IUnitModel.UNIT_LINEAR : IUnitModel.UNIT_ANGULAR;
        axPanel0 = new JAxisAssemblyPanel(null, unitType, 0);
        axPanel0.setBackground(this.customPanelColor);
        custom.add(axPanel0, comboGBC.setPos(0, 0));
        axPanel1 = new JAxisAssemblyPanel(null, unitType, 1);
        axPanel1.setBackground(this.customPanelColor);
        custom.add(axPanel1, comboGBC.setPos(0, 1));
        if (this.dim == 3) {
            axPanel2 = new JAxisAssemblyPanel(null, unitType, 2);
            axPanel2.setBackground(this.customPanelColor);
            custom.add(axPanel1, comboGBC.setPos(0, 2));
        }
    }

    /** @return the object selected or specified by the user.
     * @throws FactoryException if construction of the selected object failed. */
    public CoordinateSystem getCS() throws FactoryException {
        Object temp = this.dropdown.getSelectedItem();
        EPSGEntry entry = (temp == null) ? EPSGEntry.OTHER
                : (EPSGEntry) temp;
        if (this.isCustom) {
            CoordinateSystemAxis axis0 = this.axPanel0.getAxis();
            CoordinateSystemAxis axis1 = this.axPanel1.getAxis();
            CoordinateSystemAxis axis2 = null;
            if (dim == 3) axis2 = this.axPanel2.getAxis();

            if (this.csType.equals(EllipsoidalCS.class)) {
                return (dim == 3) ? model.createEllipsoidalCS(entry.getName(),
                        axis0, axis1) : model.createEllipsoidalCS(entry
                        .getName(), axis0, axis1, axis2);
            } else if (this.csType.equals(CartesianCS.class)) {
                return (dim == 3) ? model.createCartesianCS(entry.getName(),
                        axis0, axis1) : model.createCartesianCS(
                        entry.getName(), axis0, axis1, axis2);
            } else throw new FactoryException(Res.get("err.UnknownType")); //$NON-NLS-1$
        } else {
            try {
                if (this.csType.equals(EllipsoidalCS.class)) {
                    return model.createEllipsoidalCS(entry.getCode());
                } else if (this.csType.equals(CartesianCS.class)) {
                    return model.createCartesianCS(entry.getCode());
                } else throw new FactoryException(Res.get("err.UnknownType")); //$NON-NLS-1$
            } catch (NoSuchAuthorityCodeException e) {
                e.printStackTrace();
                return null;
            }//Should not happen.
        }
    }

    /** Programmatically changes the selected/custom object. 
     * @param cs the new coordinate system to select from the list or to
     * add to the dropdown if it is not yet in the list. */
    public void setCS(CoordinateSystem cs) {
        EPSGEntry entry = model.getEntryFor(cs);
        super.setToObject(entry);
        if (cs.getDimension() != this.dim) {
            this.dim = cs.getDimension();
            this.initCustomPanel(this.custom);//Rebuild widget
        }
        this.axPanel0.setAxis(cs.getAxis(0));
        this.axPanel1.setAxis(cs.getAxis(1));
        if (dim == 3) this.axPanel2.setAxis(cs.getAxis(2));
    }
}
