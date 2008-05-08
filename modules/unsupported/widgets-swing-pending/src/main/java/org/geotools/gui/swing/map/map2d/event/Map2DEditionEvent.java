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

import org.geotools.gui.swing.map.map2d.SelectableMap2D;
import org.geotools.gui.swing.map.map2d.SelectableMap2D.SELECTION_FILTER;
import org.geotools.gui.swing.map.map2d.handler.SelectionHandler;

import com.vividsolutions.jts.geom.Geometry;
import org.geotools.gui.swing.map.map2d.handler.EditionHandler;
import org.geotools.map.MapLayer;

/**
 * Edition Event generated by a EditableMap2D
 * @author Johann Sorel
 */
public class Map2DEditionEvent extends EventObject{

    private final MapLayer oldLayer;
    private final MapLayer newLayer;
    private final EditionHandler oldHandler;
    private final EditionHandler newHandler;
    
    
    /**
     * create a Map2DEditionEvent
     * @param map : Map2D source componant
     * @param oldlayer : old edited MapLayer, can be null
     * @param newlayer : new edited MapLayer, can be null
     * @param handler : SelectionHandler, can't be null
     */
    public Map2DEditionEvent(SelectableMap2D map, MapLayer oldlayer, MapLayer newlayer, EditionHandler handler){
        super(map);
        this.oldLayer = oldlayer;
        this.newLayer = newlayer;
        this.oldHandler = handler;
        this.newHandler = handler;
    }
    
    /**
     * create a Map2DEditionEvent
     * @param map : Map2D source componant
     * @param layer : edited MapLayer, can be null
     * @param oldhandler : old editionHandler, can't be null
     * @param newhandler : new editionHandler, can't be null
     */
    public Map2DEditionEvent(SelectableMap2D map, MapLayer layer, EditionHandler oldhandler, EditionHandler newhandler){
        super(map);
        this.oldLayer = layer;
        this.newLayer = layer;
        this.oldHandler = oldhandler;
        this.newHandler = newhandler;
    }
    
    /**
     * 
     * @return MapLayer , can be null
     */
    public MapLayer getEditedLayer(){
        return newLayer;
    }
    
    /**
     * 
     * @return  MapLayer, can be null
     */
    public MapLayer getPreviousEditedLayer(){
        return oldLayer;
    }
   
    /**
     * 
     * @return EditionHandler, can't be null
     */
    public EditionHandler getEditionHandler(){
        return newHandler;
    }
    
    /**
     * 
     * @return EditionHandler, can't be null
     */
    public EditionHandler getPreviousEditionHandler(){
        return oldHandler;
    }
    
}
