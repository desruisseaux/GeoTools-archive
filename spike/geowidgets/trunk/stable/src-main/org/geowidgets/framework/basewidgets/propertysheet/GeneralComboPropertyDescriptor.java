/* This file is part of the GeoWidgets project.
 * (C) 2005, Matthias Basler
 * This library is distributed under the GNU Lesser General Public License.
 * See http://www.opensource.org/licenses/lgpl-license.php or read
 * the license file for details.
 */
package org.geowidgets.framework.basewidgets.propertysheet;

import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

/** A label that handles all kind of objects (thanks to Java5 Generics) and
 * uses a dropdown list to display the values. 
 * @param <T> the object class of the objects managed by this PropertyDescriptor.*/
public class GeneralComboPropertyDescriptor<T extends Object> extends PropertyDescriptor {
    /** The list of objects stored whose labels are to be uses in the cell editor. */
    protected List<T> entries;
    /** if <code>true</code>, the user can type any value into the combo. */
    protected boolean editable;

    /** A property descriptor that manages a list of objects of the class
     * specified by the literal.
     * @param id the property ID
     * @param displayName the property's label
     * @param entries the objects to select from
     * @param editable if <code>true</code>, the user can type any value into
     * the combo. In case of such a custom value a <code>String</code> is returned.
     */
    public GeneralComboPropertyDescriptor(Object id, String displayName,
            final List<T> entries, boolean editable) {
        super(id, displayName);
        this.entries = entries;
        this.editable = editable;
    }

    /** A property descriptor that manages a list of objects of the class
     * specified by the literal. A custom label provider can be used. 
     * @param id the property ID
     * @param displayName the property's label
     * @param entries the objects to select from
     * @param editable if <code>true</code>, the user can type any value into
     * the combo. In case of such a custom value a <code>String</code> is returned.
     * @param labelProvider determines how to read out the label String from the objects.
     */
    public GeneralComboPropertyDescriptor(Object id, String displayName,
            final List<T> entries, boolean editable, ILabelProvider labelProvider) {
        this(id, displayName, entries, editable);
        this.setLabelProvider(labelProvider);
    }

    public CellEditor createPropertyEditor(Composite parent) {
        CellEditor editor = new GeneralComboCellEditor<T>(
                parent, entries, editable, this.getLabelProvider());
        if (getValidator() != null) editor.setValidator(getValidator());
        return editor;
    }

    public ILabelProvider getLabelProvider() {
        if (isLabelProviderSet()) return super.getLabelProvider();
        else return new GeneralComboLabelProvider();
    }
}
