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

import java.awt.event.MouseMotionListener;
import org.geotools.gui.swing.map.map2d.event.HighlightChangeListener;
import org.geotools.map.MapLayer;

/**
 *
 * @author johann sorel
 */
public interface HighlightManager extends MouseMotionListener{

        
    /**
     * add a HighlightChangedListener to this manager, note this need not
     * be a map which is why we pass a filter back not a map point.
     * @param l - the listener
     */
    public void addHighlightChangeListener(HighlightChangeListener l);

    /**
     * Remove a highlightlistener
     * @param l - the listener
     */
    public void removeHightlightChangeListener(HighlightChangeListener l);

    /**
     * get an array of HighlightChangeListeners
     * @return array of HighlightChangeListener
     */
    public HighlightChangeListener[] getHighlightChangeListeners();
    
    /**
     * get the highlighted layer
     * @return - the layer
     */
    public MapLayer getHighlightLayer();

    /**
     * sets the highlighted layer
     * @param highlightLayer - the layer
     */
    public void setHighlightLayer(MapLayer highlightLayer);
    
    
}
