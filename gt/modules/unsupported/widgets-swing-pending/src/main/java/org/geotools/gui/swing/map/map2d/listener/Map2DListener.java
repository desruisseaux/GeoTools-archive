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

import org.geotools.gui.swing.map.map2d.event.Map2DMapAreaEvent;
import org.geotools.gui.swing.map.map2d.event.Map2DContextEvent;
import java.util.EventListener;

/**
 * Map2DListener used to listen to Map2D events 
 * @author Johann Sorel
 */
public interface Map2DListener extends EventListener{

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
