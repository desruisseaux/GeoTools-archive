/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, GeoTools Project Managment Committee (PMC)
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

import org.geotools.gui.swing.map.map2d.handler.NavigationHandler;
import org.geotools.gui.swing.map.map2d.listener.NavigableMap2DListener;

/**
 * interface for map2d widget how handle Navigation
 * @author Johann Sorel
 */
public interface NavigableMap2D extends Map2D{
        
    /**
     * get to the previous maparea is there was one
     */
    public void previousMapArea();
    
    /**
     * get to the next maparea is there is one
     */
    public void nextMapArea();
    
    /**
     * the NavigationHandler is managing the selection decoration and the related listeners.
     * @param handler
     */
    public void setNavigationHandler(NavigationHandler handler);
    
    /**
     * 
     * @return NavigationHandler
     */
    public NavigationHandler getNavigationHandler();
    
      
    /**
     * add a NavigableMap2DListener
     * @param listener : NavigableMap2DListener to add
     */
    public void addNavigableMap2DListener(NavigableMap2DListener listener);
    
    /**
     * remove a NavigableMap2DListener
     * @param listener : NavigableMap2DListener to remove
     */
    public void removeNavigableMap2DListener(NavigableMap2DListener listener);
    
    /**
     * get an array of NavigableMap2DListener
     * @return array of NavigableMap2DListener
     */
    public NavigableMap2DListener[] getNavigableMap2DListeners();
    
}
