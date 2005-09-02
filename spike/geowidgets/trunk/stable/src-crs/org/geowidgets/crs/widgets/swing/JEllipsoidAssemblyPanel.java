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

import org.MaBaUtils.ui.ComfortGBC;
import org.geowidgets.crs.model.EPSGEntry;
import org.geowidgets.framework.Res;
import org.geowidgets.framework.validation.IValidator;
import org.geowidgets.framework.validation.impl.*;
import org.geowidgets.framework.validation.swing.JValidatedTextComponent;
import org.geowidgets.units.model.IUnitModel;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.datum.Ellipsoid;

/** JEllipsoidAssemblyWidget
 * 
 * @author Matthias Basler
 */
public class JEllipsoidAssemblyPanel extends _AssemblyPanel {
    private static final long serialVersionUID = 3783996814048300581L;
    protected static final String ERR_PARAMS = Res.get(Res.WIDGETS, "err.ElParams"); //$NON-NLS-1$
    protected JUnitComboBox cb_EUnit;
    protected JTextField t_Ea, t_Eb, t_Flat;
    protected JValidatedTextComponent<JTextField> val_Ea, val_Eb, val_Flat;

    /** Creates a new widget to select ellipsoids
     * @param c the color of the customPanel or null to use the defaults */
    public JEllipsoidAssemblyPanel(Color c) {
        super("x.EL", "x.EL.Custom", //$NON-NLS-1$//$NON-NLS-2$
                c, Ellipsoid.class, null);
        this.initCustomPanel(this.custom);
    }

    protected void initCustomPanel(JPanel custom) {
        custom.setLayout(new GridBagLayout());
        ComfortGBC labelGBC = UI.getLabelGBC();
        ComfortGBC comboGBC = UI.getComboGBC();

        custom.add(UI.createJLabel(Res.CRS, "x.UnitLinear"), labelGBC.setPos(0, 2));//$NON-NLS-1$
        custom.add(UI.createJLabel(Res.CRS, "x.a"), labelGBC.setPos(0, 3));//$NON-NLS-1$
        custom.add(UI.createJLabel(Res.CRS, "x.b"), labelGBC.setPos(0, 4));//$NON-NLS-1$
        custom.add(UI.createJLabel(Res.CRS, "x.InvFlat"), labelGBC.setPos(0, 5));//$NON-NLS-1$

        cb_EUnit = new JUnitComboBox(IUnitModel.UNIT_LINEAR, true);
        AbstractFormatter formatter = new NumberFormatter();        
        t_Ea = UI.customize(new JFormattedTextField(formatter));
        t_Eb = UI.customize(new JFormattedTextField(formatter));
        t_Flat = UI.customize(new JFormattedTextField(formatter));

        custom.add(cb_EUnit, comboGBC.setPos(1, 2));
        custom.add(t_Ea, comboGBC.setPos(1, 3));
        custom.add(t_Eb, comboGBC.setPos(1, 4));
        custom.add(t_Flat, comboGBC.setPos(1, 5));

        //Validation
        Color badColor = new Color(255, 50, 50);
        this.val_Ea = new JValidatedTextComponent<JTextField>(t_Ea,
                new SimpleTextFieldValidator(ERR_PARAMS, t_Ea, 1.0, Double.MAX_VALUE),
                new BackgroundColorNotifier(t_Ea, badColor, null));

        IValidator combiVal = new CombinedValidator(ERR_PARAMS,
                CombinedValidator.COMBINE_OR, //$NON-NLS-1$
                new SimpleTextFieldValidator(ERR_PARAMS, t_Eb, 1.0, Double.MAX_VALUE),
                new SimpleTextFieldValidator(ERR_PARAMS, t_Flat, 1.0, Double.MAX_VALUE));
        this.val_Eb = new JValidatedTextComponent<JTextField>(t_Eb, combiVal,
                new BackgroundColorNotifier(t_Eb, badColor, null));
        this.val_Flat = new JValidatedTextComponent<JTextField>(t_Flat, combiVal,
                new BackgroundColorNotifier(t_Flat, badColor, null));
    }

    /** @return the object selected or specified by the user.
     * @throws FactoryException if construction of the selected object failed. */
    public Ellipsoid getEllipsoid() throws FactoryException {
        Object temp = this.dropdown.getSelectedItem();
        EPSGEntry entry = (temp == null) ? EPSGEntry.OTHER : (EPSGEntry) temp;
        if (this.isCustom) {
            //Check parameters using the validators
            if (!this.val_Ea.getValidator().getPreviousResult()
                    && !this.val_Eb.getValidator().getPreviousResult())
                throw new FactoryException(ERR_PARAMS);
            return model.createEllipsoid(entry.getName(), this.t_Ea.getText(), this.t_Eb
                    .getText(), this.t_Flat.getText(), this.cb_EUnit.getSelectedUnit());
        } else {
            try {
                return model.createEllipsoid(entry.getCode());
            } catch (NoSuchAuthorityCodeException e) {
                e.printStackTrace();
                return null;
            }//Should not happen.
        }
    }

    /** Programmatically changes the selected/custom object. 
     * @param el the new ellipsoid to select from the list or to
     * add to the dropdown if it is not yet in the list. */
    public void setEllipsoid(Ellipsoid el) {
        EPSGEntry entry = model.getEntryFor(el);
        super.setToObject(entry);
        this.t_Ea.setText(Double.toString(el.getSemiMajorAxis()));
        if (el.isIvfDefinitive()) this.t_Flat.setText(Double.toString(el
                .getInverseFlattening()));
        else this.t_Eb.setText(Double.toString(el.getSemiMinorAxis()));
        this.cb_EUnit.setSelectedUnit(el.getAxisUnit());
    }
}
