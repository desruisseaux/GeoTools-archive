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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import java.awt.event.ComponentEvent;
import org.geotools.gui.swing.map.map2d.*;
import java.awt.Rectangle;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;
import javax.swing.event.EventListenerList;
import org.geotools.gui.swing.map.map2d.event.Map2DContextEvent;
import org.geotools.gui.swing.map.map2d.event.Map2DMapAreaEvent;
import org.geotools.gui.swing.map.map2d.listener.StrategyListener;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.map.event.MapLayerListEvent;
import org.geotools.map.event.MapLayerListListener;
import org.geotools.referencing.crs.DefaultGeographicCRS;

/**
 * Abstract rendering strategy
 * @author Johann Sorel
 */
public abstract class AbstractRenderingStrategy implements RenderingStrategy {

    private final MapLayerListListener mapLayerListlistener = new MapLayerListListen();
    private final EventListenerList STRATEGY_LISTENERS = new EventListenerList();
    private MapContext context = new DefaultMapContext(DefaultGeographicCRS.WGS84);
    private Envelope oldMapArea = null;
    private Rectangle oldRect = null;
    private final JComponent comp;
    private boolean autorefresh = true;
    private boolean isPainting = false;
    protected Envelope mapArea = new Envelope(1, 10, 1, 10);

    /**
     * create a default AbstractStrategy 
     * @param renderer
     */
    public AbstractRenderingStrategy() {
        comp = init();
        comp.addComponentListener(new ComponentListener() {

            public void componentResized(ComponentEvent arg0) {
                setMapArea(mapArea);
            }

            public void componentMoved(ComponentEvent arg0) {
            }

            public void componentShown(ComponentEvent arg0) {
                setMapArea(mapArea);
            }

            public void componentHidden(ComponentEvent arg0) {
            }
        });
    }

    protected abstract JComponent init();

    protected Envelope fixAspectRatio(Rectangle rect, Envelope area) {

        double mapWidth = area.getWidth(); /* get the extent of the map */
        double mapHeight = area.getHeight();
        double scaleX = rect.getWidth() / area.getWidth(); /*
         * calculate the new
         * scale
         */

        double scaleY = rect.getHeight() / area.getHeight();
        double scale = 1.0; // stupid compiler!

        if (scaleX < scaleY) { /* pick the smaller scale */
            scale = scaleX;
        } else {
            scale = scaleY;
        }

        /* calculate the difference in width and height of the new extent */
        double deltaX = /* Math.abs */ ((rect.getWidth() / scale) - mapWidth);
        double deltaY = /* Math.abs */ ((rect.getHeight() / scale) - mapHeight);


        /* create the new extent */
        Coordinate ll = new Coordinate(area.getMinX() - (deltaX / 2.0), area.getMinY() - (deltaY / 2.0));
        Coordinate ur = new Coordinate(area.getMaxX() + (deltaX / 2.0), area.getMaxY() + (deltaY / 2.0));

        return new Envelope(ll, ur);
    }

    protected void fit() {

        if (checkAspect()) {
            testRefresh();
        }
    }

    protected void testRefresh() {
        if (autorefresh) {
            refresh();
        }
    }

    protected boolean checkAspect() {
        boolean changed = false;

        Rectangle newRect = comp.getBounds();

        if (!newRect.equals(oldRect) || !mapArea.equals(oldMapArea)) {
            changed = true;
            oldRect = newRect;
            oldMapArea = mapArea;
        }

        return changed;
    }

    protected synchronized boolean isValidEnvelope(Envelope env) {
        boolean ok = false;
        

        if (env.isNull()) {
            ok = false;
        } else {

            if (  Double.isNaN(env.getMinX()) ||  Double.isNaN(env.getMinY()) ||  Double.isNaN(env.getMaxX()) ||  Double.isNaN(env.getMaxY()) ) {
                ok = false;
            }else{
                ok = true;
            }
            

        }
        return ok;

    }

    protected void setPainting(boolean b) {
        isPainting = b;
        fireRenderingEvent(isPainting);
    }

    //------------------TRIGGERS------------------------------------------------
    private void fireRenderingEvent(boolean isRendering) {
        StrategyListener[] lst = getStrategyListeners();

        for (StrategyListener l : lst) {
            l.setRendering(isRendering);
        }
    }

    protected void fireMapAreaChanged(Envelope oldone, Envelope newone) {

        Map2DMapAreaEvent mce = new Map2DMapAreaEvent(this, oldone, newone);

        StrategyListener[] lst = getStrategyListeners();

        for (StrategyListener l : lst) {
            l.mapAreaChanged(mce);
        }

    }

    protected void fireMapContextChanged(MapContext oldcontext, MapContext newContext) {
        Map2DContextEvent mce = new Map2DContextEvent(this, oldcontext, newContext);

        StrategyListener[] lst = getStrategyListeners();

        for (StrategyListener l : lst) {
            l.mapContextChanged(mce);
        }

    }

    //-----------------------RenderingStrategy----------------------------------
    public abstract BufferedImage createBufferImage(MapLayer layer);

    public abstract BufferedImage createBufferImage(MapContext context);

    public abstract BufferedImage getBufferImage();

    public abstract void refresh();

    public final JComponent getComponent() {
        return comp;
    }

    public final void setContext(MapContext context) {

        if (context == null) {
            throw new NullPointerException("Context can't be null");
        }

        if (this.context != context) {
            this.context.removeMapLayerListListener(mapLayerListlistener);

            MapContext oldContext = this.context;
            this.context = context;
            this.context.addMapLayerListListener(mapLayerListlistener);

            refresh();

            fireMapContextChanged(oldContext, this.context);
        }
    }

    public final MapContext getContext() {
        return context;
    }

    public final void setMapArea(Envelope area) {

        if (area == null) {
            throw new NullPointerException("Area can't be null.");
        }

        Envelope oldenv = mapArea;
        mapArea = fixAspectRatio(comp.getBounds(), area);

        fit();
        fireMapAreaChanged(oldenv, mapArea);

    }

    public final Envelope getMapArea() {
        return mapArea;
    }

    public final void addStrategyListener(StrategyListener listener) {
        STRATEGY_LISTENERS.add(StrategyListener.class, listener);
    }

    public final void removeStrategyListener(StrategyListener listener) {
        STRATEGY_LISTENERS.remove(StrategyListener.class, listener);
    }

    public final StrategyListener[] getStrategyListeners() {
        return STRATEGY_LISTENERS.getListeners(StrategyListener.class);
    }

    public final void setAutoRefreshEnabled(boolean ref) {
        autorefresh = ref;
    }

    public final boolean isAutoRefresh() {
        return autorefresh;
    }

    public final boolean isPainting() {
        return isPainting;
    }


    //----------------------Layer events----------------------------------------
    protected abstract void layerAdd(MapLayerListEvent event);

    protected abstract void layerRemove(MapLayerListEvent event);

    protected abstract void layerChange(MapLayerListEvent event);

    protected abstract void layerMove(MapLayerListEvent event);

    //------------------------PRIVATES CLASSES----------------------------------
    private final class MapLayerListListen implements MapLayerListListener {

        public void layerAdded(MapLayerListEvent event) {
            layerAdd(event);
        }

        public void layerRemoved(MapLayerListEvent event) {
            layerRemove(event);
        }

        public void layerChanged(MapLayerListEvent event) {
            layerChange(event);
        }

        public void layerMoved(MapLayerListEvent event) {
            layerMove(event);
        }
    }
}
    

    