/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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

package org.geotools.gui.swing.map.map2d;

import org.geotools.gui.swing.map.map2d.event.SelectionChangeListener;
import org.geotools.map.MapLayer;
import org.opengis.filter.Filter;

/**
 *
 * @author johann sorel
 */
public interface SelectionManager {

    /**
     * add a SelectionChangedListener to this manager, note this need not
     * be a map which is why we pass a filter back not a map point.
     * @param l - the listener
     */
    public void addSelectionChangeListener(SelectionChangeListener l);

    /**
     * Remove a selectionlistener
     * @param l - the listener
     */
    public void removeSelectionChangeListener(SelectionChangeListener l);

    /**
     * get an array of SelectionChangeListeners
     * @return array of SelectionChangeListener
     */
    public SelectionChangeListener[] getSelectionChangeListeners();
    
    /**
     * get the selected layer
     * @return - the layer
     */
    public MapLayer getSelectionLayer();

    /**
     * sets the selection layer
     * @param selectionLayer - the layer
     */
    public void setSelectionLayer(MapLayer selectionLayers);
    
    
    public void fireSelectionChanged(Object source, Filter filter);
        
}
