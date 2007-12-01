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
import java.io.IOException;
import javax.swing.event.EventListenerList;
import org.geotools.gui.swing.map.map2d.listener.StrategyListener;
import org.geotools.map.MapContext;
import org.geotools.map.event.MapLayerListEvent;
import org.geotools.map.event.MapLayerListListener;
import org.geotools.renderer.GTRenderer;

/**
 *
 * @author Johann Sorel
 */
public abstract class AbstractRenderingStrategy implements RenderingStrategy{

    
    protected final MapLayerListListener mapLayerListlistener = new MapLayerListListen();
    protected final EventListenerList listeners = new EventListenerList();
    protected MapContext context = null;
    protected Envelope mapArea = null;
    protected GTRenderer renderer = null;
    
    protected abstract void deletedLayer(MapLayerListEvent event);

    protected abstract void changedLayer(MapLayerListEvent event);

    protected abstract void addedLayer(MapLayerListEvent event);

    protected abstract void movedLayer(MapLayerListEvent event);
    
    
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
        if (this.context != null) {
            this.context.removeMapLayerListListener(mapLayerListlistener);
        }

        this.context = context;

        if (context != null) {
            this.context.addMapLayerListListener(mapLayerListlistener);
        }

        reset();
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
    
    
    //--------------------private classes---------------------------------------
    
    private class MapLayerListListen implements MapLayerListListener {

        public void layerAdded(MapLayerListEvent event) {

            if (context.getLayers().length == 1) {
                try {
                    setMapArea(context.getLayerBounds());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            addedLayer(event);
        }

        public void layerRemoved(MapLayerListEvent event) {
            deletedLayer(event);
        }

        public void layerChanged(MapLayerListEvent event) {
            changedLayer(event);
        }

        public void layerMoved(MapLayerListEvent event) {
            movedLayer(event);
        }
    }
    
}
