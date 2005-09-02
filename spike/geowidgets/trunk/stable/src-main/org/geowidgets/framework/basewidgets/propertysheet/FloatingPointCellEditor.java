/* This file is part of the GeoWidgets project.
 * (C) 2005, Matthias Basler
 * This library is distributed under the GNU Lesser General Public License.
 * See http://www.opensource.org/licenses/lgpl-license.php or read
 * the license file for details.
 */
package org.geowidgets.framework.basewidgets.propertysheet;

import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Composite;

/** A cell editor to edit floating point values. */
public class FloatingPointCellEditor extends TextCellEditor {

    /** Creates a new cell editor for the editing of floating point numbers. 
     * @param parent the parent UI element. */
    public FloatingPointCellEditor(Composite parent) {
        super(parent);
    }

    /** Creates a new cell editor for the editing of floating point numbers. 
     * @param parent the parent UI element. 
     * @param style the SWT style bits to use. */
    public FloatingPointCellEditor(Composite parent, int style) {
        super(parent, style);
    }

    protected Object doGetValue() {
        return new Double((String) super.doGetValue());
    }

    protected void doSetValue(Object value) {
        try {
            super.doSetValue(((Double) value).toString());
        } catch (NumberFormatException e) {
        } //TODO What to do? Ignore?
    }

}
