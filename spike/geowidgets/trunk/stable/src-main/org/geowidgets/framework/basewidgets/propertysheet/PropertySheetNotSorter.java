/* This file is part of the GeoWidgets project.
 * (C) 2005, Matthias Basler
 * This library is distributed under the GNU Lesser General Public License.
 * See http://www.opensource.org/licenses/lgpl-license.php or read
 * the license file for details.
 */
package org.geowidgets.framework.basewidgets.propertysheet;

import org.eclipse.ui.views.properties.IPropertySheetEntry;
import org.geowidgets.crs.widgets.propertysheet.eclipse.PropertySheetCategory;
import org.geowidgets.crs.widgets.propertysheet.eclipse.PropertySheetSorter;

/** A property sheet sorter that does simply nothing. The properties are used
 * in exactly the order in which they are specified in the PropertySources'
 * <code>getPropertyDescriptors()</code> function.
 *
 * @author Matthias Basler
 */
public class PropertySheetNotSorter extends PropertySheetSorter {

    /** Creates a new object that does not sort the entries it is supposed
     * to sort. In some circumstances this is a good idea. */
    public PropertySheetNotSorter() {
    }

    //Does simply nothing.
    public void sort(IPropertySheetEntry[] entries) {

    }

    //Does simply nothing.
    public void sort(PropertySheetCategory[] categories) {

    }
}
