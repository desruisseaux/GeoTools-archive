/* This file is part of the GeoWidgets project.
 * (C) 2005, Matthias Basler
 * This library is distributed under the GNU Lesser General Public License.
 * See http://www.opensource.org/licenses/lgpl-license.php or read
 * the license file for details.
 */
package org.geowidgets.crs.widgets.propertysheet;

import org.geowidgets.crs.model.EPSGEntry;
import org.geowidgets.framework.basewidgets.propertysheet.GeneralComboLabelProvider;

/** A label provider that returns the code of each EPSG entry as the label.
 *
 * @author Matthias Basler
 */
public class EPSGCodeLabelProvider extends GeneralComboLabelProvider{
    
    /** Creates a new label provider that uses the EPSGEntry's code as
     * the display value and uses the <code>toString()</code> method if the
     * object is not an EPSGEntry. */
    public EPSGCodeLabelProvider(){
        super();
    }
    
    /** @return the code of the EPSG entry as the label. */
    public String getText(Object element) {
        if ( element instanceof EPSGEntry){
            return (element == null)? "" : ((EPSGEntry)element).getCode(); //$NON-NLS-1$
        } else {
            return super.getText(element);
        }
            
    }    
}
