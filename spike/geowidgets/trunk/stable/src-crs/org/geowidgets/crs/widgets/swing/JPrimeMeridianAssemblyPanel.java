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
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.text.NumberFormatter;
import javax.units.Unit;

import org.MaBaUtils.C;
import org.MaBaUtils.ui.ComfortGBC;
import org.geowidgets.crs.model.EPSGEntry;
import org.geowidgets.framework.Res;
import org.geowidgets.framework.validation.impl.BackgroundColorNotifier;
import org.geowidgets.framework.validation.impl.SimpleTextFieldValidator;
import org.geowidgets.framework.validation.swing.JValidatedTextComponent;
import org.geowidgets.units.model.IUnitModel;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.datum.PrimeMeridian;

/** JEllipsoidAssemblyWidget
 * 
 * @author Matthias Basler
 */
public class JPrimeMeridianAssemblyPanel extends _AssemblyPanel {
    private static final long serialVersionUID = 2487929851004322326L;
    protected static final String ERR_PARAMS = Res.get(Res.WIDGETS, "err.PMParam"); //$NON-NLS-1$
    protected JUnitComboBox cb_PMUnit;
    protected JTextField t_GWL;
    protected JValidatedTextComponent<JTextField> val_GWL;

    /** Creates a new widget to select prime meridians
     * @param c the color of the customPanel or null to use the defaults */
    public JPrimeMeridianAssemblyPanel(Color c) {
        super("x.PM", "x.PM.Custom",//$NON-NLS-1$//$NON-NLS-2$
                c, PrimeMeridian.class, null);
        this.initCustomPanel(this.custom);
    }

    protected void initCustomPanel(JPanel custom) {
        custom.setLayout(new GridBagLayout());
        ComfortGBC labelGBC = UI.getLabelGBC();
        ComfortGBC comboGBC = UI.getComboGBC();

        custom.add(UI.createJLabel(Res.CRS, "x.UnitAngular"), labelGBC.setPos(0, 2));//$NON-NLS-1$
        custom.add(UI.createJLabel(Res.CRS, "x.GreenwichLong2"), labelGBC.setPos(0, 3));//$NON-NLS-1$      

        cb_PMUnit = new JUnitComboBox(IUnitModel.UNIT_ANGULAR, true);
        AbstractFormatter formatter = new NumberFormatter();//DecimalFormat.getNumberInstance()
        t_GWL = UI.customize(new JFormattedTextField(formatter));

        custom.add(cb_PMUnit, comboGBC.setPos(1, 2));
        custom.add(t_GWL, comboGBC.setPos(1, 3));

        //Validation
        Color badColor = new Color(255, 50, 50);
        this.val_GWL = new JValidatedTextComponent<JTextField>(t_GWL,
                new SimpleTextFieldValidator(ERR_PARAMS, t_GWL, Double.MIN_VALUE,
                        Double.MAX_VALUE), new BackgroundColorNotifier(t_GWL, badColor,
                        null));

    }

    /** @return the object selected or specified by the user.
     * @throws FactoryException if construction of the selected object failed. */
    public PrimeMeridian getPrimeMeridian() throws FactoryException {
        Object temp = this.dropdown.getSelectedItem();
        EPSGEntry entry = (temp == null) ? EPSGEntry.OTHER : (EPSGEntry) temp;
        if (this.isCustom) {
            double lon = C.getdoubleX(this.t_GWL.getText(), 0);
            Unit aUnit = this.cb_PMUnit.getSelectedUnit();
            return model.createPrimeMeridian(entry.getName(), lon, aUnit);
        } else {
            try {
                return model.createPrimeMeridian(entry.getCode());
            } catch (NoSuchAuthorityCodeException e) {
                e.printStackTrace();
                return null;
            }//Should not happen.
        }
    }

    /** Programmatically changes the selected/custom object. 
     * @param pm the new coordinate system to select from the list or to
     * add to the dropdown if it is not yet in the list. */
    public void setPrimeMeridian(PrimeMeridian pm) {
        EPSGEntry entry = model.getEntryFor(pm);
        super.setToObject(entry);
        this.t_GWL.setText(Double.toString(pm.getGreenwichLongitude()));
        this.cb_PMUnit.setSelectedUnit(pm.getAngularUnit());
    }
}
