/* This file is part of the GeoWidgets project.
 * (C) 2005, Matthias Basler
 * This library is distributed under the GNU Lesser General Public License.
 * See http://www.opensource.org/licenses/lgpl-license.php or read
 * the license file for details.
 */
package org.geowidgets.crs.widgets.swing;

import java.awt.Color;
import java.awt.GridBagLayout;

import javax.swing.*;
import javax.units.Unit;

import org.MaBaUtils.ui.ComfortGBC;
import org.geowidgets.crs.model.EPSGEntry;
import org.geowidgets.framework.Res;
import org.geowidgets.units.model.IUnitModel;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CoordinateSystemAxis;

/** JEllipsoidAssemblyWidget
 * 
 * @author Matthias Basler
 */
public class JAxisAssemblyPanel extends _AssemblyPanel {
    private static final long serialVersionUID = -7893589222277432131L;
    int unitType;
    protected JTextField t_AxisAbb;
    protected JAxisDirComboBox cb_AxisDir;
    protected JUnitComboBox cb_AxisUnit;

    /** Creates a new widget to select coordinate axes
     * @param c the color of the customPanel or null to use the defaults
     * @param unitType show axes with linear/angular/both units
     * @param axisNum number of this axis, e.g. in a coordinate system (0, 1 or 2)
     */
    public JAxisAssemblyPanel(Color c, int unitType, int axisNum) {
        super("x.Axis." + axisNum, "x.Axis.Custom",//$NON-NLS-1$//$NON-NLS-2$
                c, CoordinateSystemAxis.class, null);
        this.unitType = unitType;
        this.initCustomPanel(this.custom);

    }

    protected void initCustomPanel(JPanel custom) {
        custom.setLayout(new GridBagLayout());
        ComfortGBC labelGBC = UI.getLabelGBC();
        ComfortGBC comboGBC = UI.getComboGBC();

        String uName = (this.unitType == IUnitModel.UNIT_ANGULAR) ? "x.UnitAngular" : "x.UnitLinear"; //$NON-NLS-1$ //$NON-NLS-2$
        custom.add(UI.createJLabel(Res.CRS, "x.Abb"), labelGBC.setPos(0, 1));//$NON-NLS-1$
        custom.add(UI.createJLabel(Res.CRS, "x.AxisDir"), labelGBC.setPos(0, 2));//$NON-NLS-1$
        custom.add(UI.createJLabel(Res.CRS, uName), labelGBC.setPos(0, 3));//$NON-NLS-1$      

        t_AxisAbb = UI.customize(new JTextField());
        cb_AxisDir = new JAxisDirComboBox();
        UI.customize(cb_AxisDir);
        cb_AxisUnit = new JUnitComboBox(this.unitType, true);
        UI.customize(cb_AxisUnit);

        custom.add(t_AxisAbb, comboGBC.setPos(1, 1));
        custom.add(cb_AxisDir, comboGBC.setPos(1, 2));
        custom.add(cb_AxisUnit, comboGBC.setPos(1, 3));
    }

    /** Does nothing, since there are no standard axes to select one from." */
    protected void setToDefault() {
    }

    protected boolean updateInformationLabel(JTextArea standard,
            String dropdownText) {
        return true; //Nothing to do here either
    }

    /** @return the object selected or specified by the user.
     * @throws FactoryException  */
    public CoordinateSystemAxis getAxis() throws FactoryException {
        Object temp = this.dropdown.getSelectedItem();
        String axis = (temp == null) ? EPSGEntry.OTHER.toString() : temp
                .toString();

        String abb = this.t_AxisAbb.getText();
        if (abb == null) abb = ""; //$NON-NLS-1$ //NOTE Any better solution, e.g. throw error?

        AxisDirection direction = this.cb_AxisDir.getSelectedAxisDirection();
        Unit unit = this.cb_AxisUnit.getSelectedUnit();

        return model.createCoordinateSystemAxis(axis, abb, direction, unit); //$NON-NLS-1$
    }

    /** Programmatically changes the selected/custom object. 
     * @param axis the new axis to select from the list or to
     * add to the dropdown if it is not yet in the list. */
    public void setAxis(CoordinateSystemAxis axis) {
        this.dropdown.setToolTipText(axis.getName().getCode());
        this.t_AxisAbb.setText(axis.getAbbreviation());
        this.cb_AxisDir.setAxisDirection(axis.getDirection());
        this.cb_AxisUnit.setSelectedUnit(axis.getUnit());
    }
}
