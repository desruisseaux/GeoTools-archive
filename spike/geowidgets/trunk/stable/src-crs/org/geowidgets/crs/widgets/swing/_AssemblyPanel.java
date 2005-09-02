/* This file is part of the GeoWidgets project.
 * (C) 2005, Matthias Basler
 * This library is distributed under the GNU Lesser General Public License.
 * See http://www.opensource.org/licenses/lgpl-license.php or read
 * the license file for details.
 */
package org.geowidgets.crs.widgets.swing;

import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.event.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.*;

import org.MaBaUtils.ui.ComfortGBC;
import org.geowidgets.crs.model.*;
import org.geowidgets.framework.GWFactoryFinder;
import org.geowidgets.framework.Res;
import org.geowidgets.framework.ui.CRS_SwingUIFactory;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.IdentifiedObject;

/** Base class for all assembly panels. These are widgets that allow the user
 * to select an object from a list of available object or to assemble a customized
 * object by selecting and entering the object's parts and parameters. <p>
 * Subclasses will have to fill the JPanel <code>this.custom</code> during 
 * initialization with components to manually select the object's
 * parts and parameters. <p>
 * Alternatively this.custum can remain empty and
 * <code>setCustomPossible(false)</code> can be set if the user shall
 * only select one of the standard objects in the list. In this case the widget
 * works as a simple labeled dropdown with feedback via information label.
 * @author Matthias Basler
 */
public abstract class _AssemblyPanel extends JPanel {
    protected static final CRS_SwingUIFactory UI = GWFactoryFinder.getCRS_SwingUIFactory();
    protected static final Logger LOGGER = GWFactoryFinder.getLoggerFactory().getLogger();
    protected JComboBox dropdown;
    protected JScrollPane sc;
    protected JTextArea standard = UI.createInformationJTextArea();
    protected JPanel custom;
    protected JPanel p_Buttons = new JPanel(new GridBagLayout());
    /** The "OK" button that is shown if setButtonsVisible(true) is called. */
    public JButton b_OK = new JButton(Res.get("b.OK")); //$NON-NLS-1$
    /** The "Cancel" button that is shown if setButtonsVisible(true) is called. */
    public JButton b_Cancel = new JButton(Res.get("b.Cancel")); //$NON-NLS-1$

    protected boolean customPossible = true;
    protected boolean isCustom = true;
    protected int lastSelected = -1;

    protected static final ICRSModel model = GeoTools_CRSModel.getDefault();
    protected Color customPanelColor;
    protected Class<? extends IdentifiedObject> cl;
    protected String hints;

    /** Creates a combination of labeled dropdown, information label and "custom panel"
     * where the user can define a custom object, if he/she doesn't like the
     * default objects.
     * @param labelText Text to show left of the dropdown.
     * @param panelText Text to show on top of the "custom panel", e.g. "Custom Ellipsoid"
     * @param customPanelColor Background color of the "custom panel"
     * @param cl class that this assembly panel shows, such as Ellipsoid or CoordinateSystem
     * @param hints additional information about the type of objects to be selected/edited
     * Currently used to distinguish between "2D" and "3D" for some objects. Can be <code>null</code>
     * for all other objects.
     * */
    public _AssemblyPanel(String labelText, String panelText,
            Color customPanelColor, Class<? extends IdentifiedObject> cl, String hints) {
        List<EPSGEntry> ddContent;
        try {
            ddContent = model.getSupportedObjects(cl, hints);
        } catch (FactoryException fe) {
            LOGGER.log(Level.WARNING, "err.GetSupportedObjects", fe); //$NON-NLS-1$
            ddContent = new Vector<EPSGEntry>();//Empty list
        }
        customPanelColor = (customPanelColor == null) ? UI
                .getCustomPanelColor(cl) : customPanelColor;
        this.initAssemblyWidget(labelText, panelText, ddContent,
                customPanelColor);
        this.customPanelColor = customPanelColor;
        this.cl = cl;
        this.hints = hints;
        this.addComponentListener(new ComponentListener() {
            public void componentHidden(ComponentEvent e) {
            }

            public void componentShown(ComponentEvent e) {
            }

            public void componentMoved(ComponentEvent e) {
            }

            public void componentResized(ComponentEvent e) {
                invalidate();
            }
        });
    }

    /** Called when the user selected another standard item in the dropdown. 
     * The function should care about showing information about this object
     * to the user.
     * @param standard the text area where to display the multiline description
     * @param epsgCode the code of the selected item in the dropdown
     * @return false if it is detected that this object cannot be found in the
     * database or that instanciating the object is likely to fail for other reasons.
     * Should not happen usually and currently has no effect (No undo etc.). */
    protected boolean updateInformationLabel(JTextArea standard, String epsgCode) {
        try {
            standard.setText(model.getFormattedDescription(this.cl, epsgCode));
            return true;
        } catch (FactoryException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Builds the widget's user interface. 
     * @param labelText Text to show left of the dropdown.
     * @param panelText Text to show on top of the "custom panel", e.g. "Custom Ellipsoid"
     * @param customPanelColor Background color of the "custom panel"
     * @param ddContent the List of names to show in the dropdown for the standard objects
     */
    protected void initAssemblyWidget(String labelText, String panelText,
            List<EPSGEntry> ddContent, Color customPanelColor) {
        this.setLayout(new GridBagLayout());
        ComfortGBC labelGBC = UI.getLabelGBC();
        ComfortGBC comboGBC = UI.getComboGBC();
        ComfortGBC panelGBC = UI.getPanelGBC();
        ComfortGBC buttonGBC = new ComfortGBC(0, 0).anchorNE().fillY().setSize(
                2, 1);

        //Button panel
        b_OK.setDefaultCapable(true);
        p_Buttons.setOpaque(false);
        p_Buttons.add(b_OK, comboGBC.setPos(0, 0));
        p_Buttons.add(b_Cancel, comboGBC.setPos(1, 0));
        p_Buttons.setVisible(false); //Not used  by default
        this.add(p_Buttons, buttonGBC);

        //Other components
        this.add(UI.createJLabel(Res.CRS, labelText), labelGBC.setPos(0, 1));

        if (ddContent == null) ddContent = new ArrayList<EPSGEntry>();
        Collections.sort(ddContent);
        ddContent.add(0, EPSGEntry.OTHER); //$NON-NLS-1$
        dropdown = UI.customize(new JComboBox(new Vector<EPSGEntry>(
                ddContent)));
        dropdown.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                Object o = dropdown.getSelectedItem();
                boolean custom = (o == null || ((EPSGEntry) o)
                        .equals(EPSGEntry.OTHER));
                setCustomInternal(custom);
            }
        });
        dropdown.setEditable(true);
        this.add(dropdown, comboGBC.setPos(1, 1));

        standard.setOpaque(false);
        this.add(standard, panelGBC.setPos(0, 2));//

        custom = UI.createCustomPanel(panelText, customPanelColor);
        this.add(custom, panelGBC.setPos(0, 2));
    }

    /** @param customPossible if false the user cannot switch to "custom mode".
     * Use this in case the user shall only select one of the stamdard objects
     * from the dropdown. <p>
     * If the widget is currently in custom mode and <code>customPossible</code>
     * gets set to false, the widget will switch to standard mode immediately. */
    public void setCustomPossible(boolean customPossible) {
        this.customPossible = customPossible;
        if (this.isCustom && !customPossible) this.setCustom(false);
        if (!customPossible) this.dropdown.removeItemAt(0);
        else this.dropdown.insertItemAt(EPSGEntry.OTHER, 0);
    }

    /** @return if the widget is currently configured not to switch to
     * custom mode. If <code>true</code> the user can only select a standard object. */
    public boolean isCustomPossible() {
        return this.customPossible;
    }

    /** @return <code>false</code> if the user selected one of the standard objects
     * from the dropdown or <code>true</code> if he/she selected "Custom ...". */
    public boolean isCustom() {
        return this.isCustom;
    }

    /** @param custom if <code>true</code>, makes the custom panel visible and
     * the standard panel invisible, if <code>false</code> it is vice versa. */
    protected void setCustomInternal(boolean custom) {
        this.isCustom = custom;
        if (!custom) {
            Object o = dropdown.getSelectedItem();
            if (o != null) {
                String code = ((EPSGEntry) o).getCode();
                if (code != null) updateInformationLabel(standard, code);
            }
        }
        this.standard.setVisible(!custom);
        this.custom.setVisible(custom);
        this.validate();
    }

    /** Programmatically changes between standard and custom mode.
     * This is usually only used to set the mode at initialization time.
     * After that the the user will use the dropdown to either choose a standard
     * item or "Custom ...". <p>
     * @param custom use <code>true</code> to set to custom mode, <code>false</code>
     * for standard mode. Note: Setting the widget to custom mode when
     * <code>isCustomPossible()</code> is <code>false</code> has no effect. */
    public void setCustom(boolean custom) {
        if (custom && this.customPossible) {
            this.lastSelected = this.dropdown.getSelectedIndex();
            this.isCustom = true;
        } else {
            if (lastSelected == -1) this.setToDefault();
            else this.dropdown.setSelectedIndex(this.lastSelected);
            this.isCustom = false;
        }
    }

    /** Select the default standard item in the dropdown in case the widget
     * is to be started in non-custom mode. */
    protected void setToDefault() {
        EPSGEntry entry = model.getDefaultEntry(this.cl, this.hints);
        this.dropdown.setSelectedItem(entry);

    }

    /** Sets the dropdown to a specified object (given by its name)
     * @return <code>false</code> if there is no such object contained in the
     * dropdown's current list of objects, i.e. if the given String does not
     * represent a standard object.
     * @param entry the object to show in the list. */
    public boolean setToObject(EPSGEntry entry) {
        dropdown.setSelectedItem(entry);
        try {
            if (((EPSGEntry) dropdown.getSelectedItem()).equals(entry)) {
                this.dropdown.setToolTipText(entry.getName());
                return true;
            } else return false;
        } catch (ClassCastException e) {
            return false;
        }
    }

    /** Checks if an entry with the given name exists in the dropdown list.
     * @param name the name to look for in the dropdown's current list
     * @return the found entry or null, of no such entry was found
     */
    public EPSGEntry findEntry(String name) {
        for (int i = 0; i < this.dropdown.getItemCount(); ++i) {
            try {
                EPSGEntry entry = (EPSGEntry) this.dropdown.getItemAt(i);
                if (entry.getName().equals(name)) return entry;
            } catch (ClassCastException e) {
            }
        }
        return null; //Name not found in list. Nothing found.
    }

    /** Every assembly panel can have an "OK" and a "Cancel" button.
     * These buttons can be used by the panel's parent component
     * to determine when the user confirms his/her selection.
     * (Then, for example, assembled object could get read out and, 
     * ifsuccessful, the dialog could get closed.) Use <code>setParent</code>
     * to register the parent for events of this button.
     * @param visible if true, shows "OK" and "Cancel" button
     * @see #addActionListener(ActionListener al)
     * @see #removeActionListener(ActionListener al) */
    public void setButtonsVisible(boolean visible) {
        this.p_Buttons.setVisible(visible);
    }

    public void setBackground(Color backColor) {
        super.setBackground(backColor);
        if (this.standard != null) this.standard.setBackground(backColor);
    }

//--Action listeners------------------------------------------------------------

    /** Sets a parent in order to register button actions to it.
     * If the user presses the "OK" or "Cancel" buttons (if visible) this
     * parent gets notified and might f.e. get the result and/or close the dialog.
     * @param al the action listener to register
     * @see #setButtonsVisible(boolean visible) */
    public void addActionListener(ActionListener al) {
        if (al != null) { //Remove old listeners
            this.b_OK.addActionListener(al);
            this.b_Cancel.addActionListener(al);
        }
    }

    /** Removes an action listener from the list of classes that get notified
     * for button events.
     * @param al the action listener to remove
     * @see #setButtonsVisible(boolean visible) */
    public void removeActionListener(ActionListener al) {
        if (al != null) { //Remove old listeners
            this.b_OK.removeActionListener(al);
            this.b_Cancel.removeActionListener(al);
        }
    }

}