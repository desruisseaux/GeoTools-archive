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

import com.vividsolutions.jts.geom.Envelope;
import java.awt.Component;
import org.geotools.gui.swing.map.Map;
import org.geotools.gui.swing.map.map2d.listener.Map2DListener;
import org.geotools.gui.swing.map.map2d.strategy.RenderingStrategy;
import org.geotools.map.MapContext;

/**
 * Map2D interface, used for mapcontext viewing
 * 
 * @author Johann Sorel
 */
public interface Map2D extends Map{
    
    /**
     * Set the mapcontext of the map2D
     * @param context : use null to remove the mapcontext
     */
    public void setContext(MapContext context);
        
    /**
     * get related MapContext
     * @return MapContext or null if map2d has no related MapContext object
     */
    public MapContext getContext();
        
    /**
     * set the maparea to look at
     * @param mapArea
     */
    public void setMapArea(Envelope mapArea);
    
    /**
     * get the actual maparea
     * @return Envelope or null if no MapArea
     */
    public Envelope getMapArea();
    
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
     * use for a complete redrawing of the map2d widget
     */
    public void refresh();
    
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
    
    
    
}
