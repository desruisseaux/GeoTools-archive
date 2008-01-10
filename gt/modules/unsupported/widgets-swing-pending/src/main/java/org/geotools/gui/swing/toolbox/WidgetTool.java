/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2007, GeoTools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */

package org.geotools.gui.swing.toolbox;

import org.geotools.gui.swing.toolbox.tooltree.*;
import javax.swing.JComponent;

/**
 * A widget Tool is a tool wich has a JComponent to configure it
 * widget tool are top level tools because they offer an interface for the user
 * @author johann sorel
 */
public interface WidgetTool extends Tool{

    public final WidgetToolListener[] EMPTY_TREETOOLLISTENER_ARRAY = {};
    
    
    /**
     * get the configuration component/panel of the tool
     * @return JComponent
     */
    public JComponent getComponent();
    
    
    /**
     * add WidgetToolListener
     * @param listener
     */
    public void addWidgetToolListener(WidgetToolListener listener);

    /**
     * remove WidgetToolListener
     * @param listener to remove
     */
    public void removeWidgetToolListener(WidgetToolListener listener);

    /**
     * get WidgetToolListener list
     * @return the listener's table
     */
    public WidgetToolListener[] getWidgetToolListeners();
    
}
