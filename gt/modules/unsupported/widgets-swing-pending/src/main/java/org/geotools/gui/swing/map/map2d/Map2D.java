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

import com.vividsolutions.jts.geom.Coordinate;
import java.awt.Component;
import org.geotools.gui.swing.map.Map;
import org.geotools.gui.swing.map.map2d.decoration.InformationDecoration;
import org.geotools.gui.swing.map.map2d.decoration.MapDecoration;
import org.geotools.gui.swing.map.map2d.listener.Map2DListener;
import org.geotools.gui.swing.map.map2d.strategy.RenderingStrategy;

/**
 * Map2D interface, used for mapcontext viewing
 * 
 * @author Johann Sorel
 */
public interface Map2D extends Map{
   
    
    /**
     * set the rendering strategy
     * @param strategy : throw nullpointexception if strategy is null
     */
    public void setRenderingStrategy(RenderingStrategy strategy);
    
    /**
     * get the map2d rendering strategy
     * @return RenderingStrategy : should never return null;
     */
    public RenderingStrategy getRenderingStrategy();
                        
    /**
     * get the visual component 
     * @return Component
     */
    public Component getComponent();
    
    /**
     * add a Map2DListener
     * @param listener : Map2Dlistener to add
     */
    public void addMap2DListener(Map2DListener listener);
        
    /**
     * remove a Map2DListener
     * @param listener : Map2DListener to remove
     */
    public void removeMap2DListener(Map2DListener listener);
    
    /**
     * 
     * @return array of Map2DListener
     */
    public Map2DListener[] getMap2DListeners();
    
    
    //---------------------Basic functions--------------------------------------
    
    /**
     * transform a mouse coordinate in JTS Coordinate using the CRS of the mapcontext
     * @param mx : x coordinate of the mouse on the map (in pixel)
     * @param my : y coordinate of the mouse on the map (in pixel)
     * @return JTS Coordinate
     */
    public Coordinate toMapCoord(int mx, int my);
    
        
    //----------------------Over/Sub/information layers-------------------------
    
    /**
     * get the top InformationDecoration of the map2d widget
     * @return InformationDecoration
     */
    public InformationDecoration getInformationLayer();

    /**
     * set the decoration behind the map
     * @param back : MapDecoration, use null to remove the decoration
     */
    public void setBackDecoration(MapDecoration back);

    /**
     * get the decoration behind the map
     * @return MapDecoration : or null if no back decoration
     */
    public MapDecoration getBackDecoration();

    /**
     * add a Decoration between the map and the information top decoration
     * @param deco : MapDecoration to add
     */
    public void addDecoration(MapDecoration deco);

    /**
     * insert a MapDecoration at a specific index
     * @param index : index where to isert the decoration
     * @param deco : MapDecoration to add
     */
    public void addDecoration(int index, MapDecoration deco);

    /**
     * get the index of a MapDecoration
     * @param deco : MapDecoration to find
     * @return index of the MapDecoration
     * @throw ClassCastException or NullPointerException
     */
    public int getDecorationIndex(MapDecoration deco);

    /**
     * remove a MapDecoration
     * @param deco : MapDecoration to remove
     */
    public void removeDecoration(MapDecoration deco);

    /**
     * get an array of all MapDecoration
     * @return array of MapDecoration
     */
    public MapDecoration[] getDecorations();

    
    
}
