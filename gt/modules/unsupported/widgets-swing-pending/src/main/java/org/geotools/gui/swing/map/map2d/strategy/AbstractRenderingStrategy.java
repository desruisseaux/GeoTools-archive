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

import com.vividsolutions.jts.geom.Envelope;
import javax.swing.event.EventListenerList;
import org.geotools.gui.swing.map.map2d.listener.StrategyListener;
import org.geotools.map.MapContext;
import org.geotools.renderer.GTRenderer;

/**
 *
 * @author Johann Sorel
 */
public abstract class AbstractRenderingStrategy implements RenderingStrategy{

    protected final EventListenerList listeners = new EventListenerList();
    protected MapContext context = null;
    protected Envelope mapArea = null;
    protected GTRenderer renderer = null;
    
    
    protected void fireRenderingEvent(boolean isRendering){
        
        StrategyListener[] lst = getStrategyListeners();

        for (StrategyListener l : lst) {
            l.setRendering(isRendering);
        }
    }
    
    
    public void setRenderer(GTRenderer renderer) {
        this.renderer = renderer;
    }

    public GTRenderer getRenderer() {
        return renderer;
    }
    
    public void setContext(MapContext context) {
        this.context = context;
    }

    public MapContext getContext() {
        return context;
    }

    public void setMapArea(Envelope area) {
        mapArea = area;
    }

    public Envelope getMapArea() {
        return mapArea;
    }

    
    public void addStrategyListener(StrategyListener listener){
       listeners.add(StrategyListener.class, listener);
    }
    
    public void removeStrategyListener(StrategyListener listener){
        listeners.remove(StrategyListener.class, listener);
    }
    
    public StrategyListener[] getStrategyListeners(){
        return listeners.getListeners(StrategyListener.class);
    }
    
}
