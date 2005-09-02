/* This file is part of the GeoWidgets project.
 * (C) 2005, Matthias Basler
 * This library is distributed under the GNU Lesser General Public License.
 * See http://www.opensource.org/licenses/lgpl-license.php or read
 * the license file for details.
 */
package org.geowidgets.framework.basewidgets.swing;

import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import org.geowidgets.framework.GWFactoryFinder;
import org.geowidgets.framework.ui.GeneralSwingUIFactory;

/** A panel used to visuably group UI elements according to their use.
 * 
 * @author Matthias Basler
 */
public abstract class _JGroupPanel extends JPanel{
    /** Swing UI factory. */
    protected static final GeneralSwingUIFactory UI
            = GWFactoryFinder.getGeneralSwingUIFactory();
    
    /** Creates a new grouping panel without title. */
    public _JGroupPanel(){
        UI.customize(this);
        this.setBorder(UI.getDefaultPanelBorder());
    }
    
    /** Creates a new grouping panel with title. 
     * @param title the panel's title. */
    public _JGroupPanel(String title){
        UI.customize(this);
        this.setBorder(new TitledBorder(UI.getDefaultPanelBorder(), title));
    }
    
}
