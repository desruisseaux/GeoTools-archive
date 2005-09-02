/* This file is part of the GeoWidgets project.
 * (C) 2005, Matthias Basler
 * This library is distributed under the GNU Lesser General Public License.
 * See http://www.opensource.org/licenses/lgpl-license.php or read
 * the license file for details.
 */
package org.geowidgets.framework.basewidgets.propertysheet;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;
import org.geowidgets.framework.Res;

/** A property descriptor to support the editing of numerical floating point values. */
public class FloatingPointPropertyDescriptor extends TextPropertyDescriptor {

    /** Creates a property descriptor for numerical floating point values.
     * @param id the id of the property
     * @param displayName the name to display for the property
     * @param minValue the lower inclusive limit of acceptable values.
     * @param maxValue the upper inclusive limit of acceptable values. */
    public FloatingPointPropertyDescriptor(Object id, String displayName,
            final double minValue, final double maxValue) {
        super(id, displayName);
        this.setValidator(new ICellEditorValidator() {
            public String isValid(Object value) {
                try {
                    Double d = new Double((String) value);
                    if (d >= minValue && d <= maxValue) return null;
                    else return Res.get("err.PositiveNumberExpected"); //$NON-NLS-1$
                } catch (Exception e) {
                    return Res.get("err.PositiveNumberExpected");} //$NON-NLS-1$
            }
        });
    }

    public CellEditor createPropertyEditor(Composite parent) {
        CellEditor editor = new FloatingPointCellEditor(parent);
        if (getValidator() != null) editor.setValidator(getValidator());
        return editor;
    }
}
