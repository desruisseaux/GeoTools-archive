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
import org.geotools.map.MapLayer;

/**
 *
 * @author Johann Sorel
 */
public class Map2DEditLayerEvent extends EventObject{

    private MapLayer oldLayer = null;
    private MapLayer newLayer = null;
    
    
    public Map2DEditLayerEvent(Map2D map, MapLayer oldone, MapLayer newone){
        super(map);
        oldLayer = oldone;
        newLayer = newone;
    }

    public MapLayer getPreviousEditLayer() {
        return oldLayer;
    }

    public MapLayer getNewEditLayer() {
        return newLayer;
    }
    
    
    
    
}
