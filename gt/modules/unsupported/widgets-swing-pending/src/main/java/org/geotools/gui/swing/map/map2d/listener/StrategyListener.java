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

package org.geotools.gui.swing.map.map2d.listener;

import java.util.EventListener;

import org.geotools.gui.swing.map.map2d.event.Map2DContextEvent;
import org.geotools.gui.swing.map.map2d.event.Map2DMapAreaEvent;

/**
 * StrategyListener used to listen to RenderingStrategy events 
 * @author Johann Sorel
 */
public interface StrategyListener extends EventListener{

    /**
     * set the actual state of the strategy
     * @param rendering : true if the strategy strat working, false when it stops
     */
    public void setRendering(boolean rendering);    
    /**
     * called when Map2d MapArea changed
     * @param event : Map2DMapAreaEvent
     */
    public void mapAreaChanged(Map2DMapAreaEvent event);    
    /**
     * called when MapContext changed
     * @param event : Map2DContextEvent
     */
    public void mapContextChanged(Map2DContextEvent event);
    
}
