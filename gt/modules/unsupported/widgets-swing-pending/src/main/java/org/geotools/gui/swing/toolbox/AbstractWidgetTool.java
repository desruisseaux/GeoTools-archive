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
import javax.swing.JPanel;
import javax.swing.event.EventListenerList;

/**
 * @author johann sorel
 */
public abstract class AbstractWidgetTool extends JPanel implements WidgetTool{

    protected final EventListenerList LISTENERS = new EventListenerList();

    public JComponent getComponent() {
        return this;
    }
    
    protected void fireObjectCreation(Object obj){
        WidgetToolListener[] listeners = getWidgetToolListeners();
        
        for(WidgetToolListener listener : listeners){
            listener.objectCreated(obj);
        }
    }
    
    /**
     * add TreeToolListener
     * @param listener
     */
    public void addWidgetToolListener(WidgetToolListener listener) {
        LISTENERS.add(WidgetToolListener.class,listener);
    }

    /**
     * remove TreeToolListener
     * @param listener to remove
     */
    public void removeWidgetToolListener(WidgetToolListener listener) {
        LISTENERS.remove(WidgetToolListener.class,listener);
    }

    /**
     * get TreeToolListener list
     * @return the listener's table
     */
    public WidgetToolListener[] getWidgetToolListeners() {
        return LISTENERS.getListeners(WidgetToolListener.class);
    }
    
}
