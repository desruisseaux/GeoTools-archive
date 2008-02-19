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
package org.geotools.gui.swing.map.map2d.strategy;

import java.awt.image.BufferedImage;

import javax.swing.JComponent;

import org.geotools.gui.swing.map.map2d.listener.StrategyListener;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.renderer.GTRenderer;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Rendering Strategy is used to specify a memory management
 * technic and different solutions to answer a need (low memory, high drawing speed, smooth edition ...)
 * @author Johann Sorel
 */
public interface RenderingStrategy {
    
    /**
     * set the GTRenderer used
     * @param renderer : GTRenderer to use, can not be null
     */
    public void setRenderer(GTRenderer renderer);
    
    /**
     * get the GTRenderer used
     * @return GTRenderer
     */
    public GTRenderer getRenderer();
    
    /**
     * create a BufferedImage of the layer, using the mapcontext CRS and MapArea
     * @param layer : Maplayer to make the BufferedImage
     * @return BufferedImage
     */
    public BufferedImage createBufferImage(MapLayer layer);
    
    /**
     * create a BufferedImage of a MapContext, using the mapcontext CRS and MapArea
     * @param context : MapContext to make the BufferedImage
     * @return BufferedImage
     */
    public BufferedImage createBufferImage(MapContext context);
    
    /**
     * create a bufferedImage of what is actually visible on the map widget
     * @return BufferedImage
     */
    public BufferedImage getBufferImage();

    /**
     * set the MapContext
     * @param context : can not be null 
     */
    public void setContext(MapContext context);
    
    /**
     * get the MapContext
     * @return MapContext or null if no mapContext
     */
    public MapContext getContext();
    
    /**
     * set the maparea to look at
     * @param area : can not be null 
     */
    public void setMapArea(Envelope area);
    
    /**
     * get the maparea to look at
     * @return Envelope or null if no MapArea
     */
    public Envelope getMapArea();
            
    /**
     * use for a complete reset of the strategy
     */
    public void refresh();
        
    /**
     * get the visual component 
     * @return JComponent
     */
    public JComponent getComponent();
    
    /**
     * add a StrategyListener
     * @param listener : StrategyListener to add
     */
    public void addStrategyListener(StrategyListener listener);
    
    /**
     * remove a StrategyListener
     * @param listener : StrategyListener to remove
     */
    public void removeStrategyListener(StrategyListener listener);
    
    /**
     * get an array of StrategyListener
     * @return array of StrategyListener
     */
    public StrategyListener[] getStrategyListeners();
    
    /**
     * set map rotation
     * @param d
     */
    public void setRotation(double d);
    
    /**
     * get map rotation
     * @return double
     */
    public double getRotation();
    
    /**
     * to enable automatic refreshing of the map, if not you must call
     * manualy the refresh method
     * @param refresh 
     */
    public void setAutoRefreshEnabled(boolean refresh);
    
    /**
     * to see if the strategy is in auto refresh mode
     * @return boolean
     */
    public boolean isAutoRefresh();
    
    /**
     * true if the strategy is currently painting
     * @return boolean
     */
    public boolean isPainting();
    
        
}
