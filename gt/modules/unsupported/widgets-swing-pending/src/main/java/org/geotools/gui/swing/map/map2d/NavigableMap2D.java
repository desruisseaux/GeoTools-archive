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

import org.geotools.gui.swing.map.MapConstants;
import org.geotools.gui.swing.map.map2d.listener.NavigableMap2DListener;

/**
 * interface for map2d widget how handle Navigation
 * @author Johann Sorel
 */
public interface NavigableMap2D extends Map2D{
    
    /**
     * set the action state. Pan, ZoomIn, ZoomOut ...
     * @param state : MapConstants.ACTION_STATE
     */
    public void setActionState(MapConstants.ACTION_STATE state);
    
    /**
     * get the actual action state
     * @return MapConstants.ACTION_STATE
     */
    public MapConstants.ACTION_STATE getActionState();
    
    /**
     * set the zoom factor. It is used when zooming in or out.
     * @param zoomFactor
     */
    public void setZoomFactor(double zoomFactor);
    
    /**
     * get the zoom factor
     * @return double value of the zoom factor
     */
    public double getZoomFactor();
      
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
