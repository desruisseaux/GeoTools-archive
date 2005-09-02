/* This file is part of the GeoWidgets project.
 * (C) 2005, Matthias Basler
 * This library is distributed under the GNU Lesser General Public License.
 * See http://www.opensource.org/licenses/lgpl-license.php or read
 * the license file for details.
 */
package org.geowidgets.framework.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

/** This class is responsible for delivering general visible elements and their
 * attributes, such as layout constants and colors. This ensures that all widgets
 * get a consistent look and feel but allows to customize this look and feel.
 * 
 * @author Matthias Basler
 */
public class GeneralSWTUIFactory {
    protected static GeneralSWTUIFactory me = new GeneralSWTUIFactory();

    protected GeneralSWTUIFactory() {
    }

    /** @return the default look and feel for the CRS widgets. */
    public static GeneralSWTUIFactory getDefault() {
        return me;
    }
//******************************************************************************
//Layout
    protected GridLayout defaultLayout = new GridLayout();

    /** @return the default LayoutManager, which is GridBagLayout. It is highly
     * recommended not to overwrite this, since there are a lot of dependencies. */
    public Layout getDefaultLayout() {
        return defaultLayout;
    }

    /** @return the width of the border around each UI element (i.e. the insets).*/
    public int getTrimWidth() {
        return 2;
    }

    protected static GridData COMBO_GRIDDATA = new GridData(SWT.FILL,
            SWT.CENTER, true, false, 1, 1);

    /** @return the GridData preconfigured for text fields and dropdows
     * such as in typical label-textfield or label-dropdown combinations. */
    public GridData getComboGridData() {
        return COMBO_GRIDDATA;
    }

//Colors
    protected static final Color labelColor = new Color(null, 0, 0, 0);//Black
    protected static final Color textColor = new Color(null, 0, 0, 0);//Black
    protected static final Color descColor = new Color(null, 0, 0, 128);//Blue

    /** @return the color to use for labels. */
    protected final Color getLabelColor() {
        return descColor;
    }

    /** @return the color to use for text fields, dropdowns and such. */
    protected final Color getTextColor() {
        return textColor;
    }

    /** @return the color to use for descriptions. */
    protected final Color getDescriptionColor() {
        return descColor;
    }

//Sizes
    /** @return the minimum recommended size for text fields, dropdowns and the like. */
    public Point getMinTextFieldSize() {
        return new Point(60, 18);
    }

//Standard UI elements
    /** Applies a consistent look and fell to the label. Override to change
     * certain aspects (such as standard font or color) of all labels.
     * @return the customized label
     * @param label the label to get customized. Its text must be preserved.*/
    public Label customizeJLabel(Label label) {
        label.setForeground(this.labelColor);
        label.setSize(getMinTextFieldSize());
        return label;
    }

    /** Applies a consistent look and fell to the text field. Override to change
     * certain aspects (such as standard font or color) of all text fields.
     * @return the customized text field
     * @param textField the text field to get customized. Its text must be preserved.*/
    public Text customizeJTextField(Text textField) {
        textField.setForeground(this.textColor);
        textField.setSize(getMinTextFieldSize());
        return textField;
    }

    /** Applies a consistent look and fell to the dropdown. Override to change
     * certain aspects (such as standard font or color) of all dropdown lists.
     * Be sure to call this <b>before</b> further changing attributes or
     * your changes might get overwritten. <p/>
     * This implementation sets the number of visible rows to 10 and cares
     * about automatic tooltip updating.
     * @return the customized dropdown list.
     * @param combo the dropdown to get customized. Its content must be preserved.*/
    public Combo customizeJCombo(Combo combo) {
        combo.setForeground(this.textColor);
        combo.setSize(getMinTextFieldSize());
        combo.setVisibleItemCount(10);
        if (combo.getSelectionIndex() != -1)
            combo.setToolTipText(combo.getItem(combo.getSelectionIndex())
                    .toString());
        combo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                Combo source = (Combo) e.getSource();
                source.setToolTipText(source
                        .getItem(source.getSelectionIndex()).toString());
            }
        });
        combo.setLayoutData(getComboGridData());
        return combo;
    }
}
