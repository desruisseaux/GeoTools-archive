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
import org.geotools.gui.swing.map.MapConstants;
import org.geotools.gui.swing.map.map2d.Map2D;

/**
 *
 * @author Johann Sorel
 */
public class Map2DEditStateEvent extends EventObject{

    private MapConstants.EDIT_STATE oldstate = null;
    private MapConstants.EDIT_STATE newstate = null;
    
    
    public Map2DEditStateEvent(Map2D map, MapConstants.EDIT_STATE oldone, MapConstants.EDIT_STATE newone){
        super(map);
        oldstate = oldone;
        newstate = newone;
    }

    public MapConstants.EDIT_STATE getPreviousState() {
        return oldstate;
    }

    public MapConstants.EDIT_STATE getNewState() {
        return newstate;
    }
    
    
    
    
}
