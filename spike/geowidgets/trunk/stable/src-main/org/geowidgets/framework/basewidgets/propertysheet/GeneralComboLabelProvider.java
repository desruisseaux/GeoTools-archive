/* This file is part of the GeoWidgets project.
 * (C) 2005, Matthias Basler
 * This library is distributed under the GNU Lesser General Public License.
 * See http://www.opensource.org/licenses/lgpl-license.php or read
 * the license file for details.
 */
package org.geowidgets.framework.basewidgets.propertysheet;

import org.eclipse.jface.viewers.LabelProvider;

/** A LabelProvider that simply uses the <code>toString()</code> method
 * to determine an object's label.
 *
 * @author Matthias Basler
 */
public class GeneralComboLabelProvider extends LabelProvider {

    /** Creates a Label provider that uses the <code>toString()</code>
     * function of the provided objects as the object's labels. */
    public GeneralComboLabelProvider() {
        super();
    }

    /** Returns an element's name. If the element is an index within the list
     * given to this label provider, the name at the position given by the
     * index is returned using the <code>toString()</code> function. */
    public String getText(Object element) {
        return (element == null) ? "" : element.toString(); //$NON-NLS-1$
    }
}
