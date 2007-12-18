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

package org.geotools.gui.swing.map.map2d.event;

import java.util.EventObject;
import org.geotools.gui.swing.map.map2d.Map2D;
import org.geotools.map.MapContext;

/**
 * Map2DContextEvent Event generated by a Map2D
 * @author Johann Sorel
 */
public class Map2DContextEvent extends EventObject{

    private MapContext oldContext = null;
    private MapContext newContext = null;
    
    
    /**
     * create a Map2DContextEvent
     * @param map : Map2D source componant
     * @param oldone : previous MapContext
     * @param newone : new MapContext
     */
    public Map2DContextEvent(Map2D map, MapContext oldone, MapContext newone){
        super(map);
        oldContext = oldone;
        newContext = newone;
    }

    /**
     * get previous MapContext
     * @return MapContext or null if none
     */
    public MapContext getPreviousContext() {
        return oldContext;
    }

    /**
     * get new MapContext
     * @return MapContext or null if none
     */
    public MapContext getNewContext() {
        return newContext;
    }
    
    
    
    
}
