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

import org.geotools.gui.swing.map.map2d.strategy.SingleVolatileImageStrategy;
import org.geotools.gui.swing.map.map2d.strategy.RenderingStrategy;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.event.EventListenerList;
import org.geotools.gui.swing.map.map2d.event.Map2DContextEvent;
import org.geotools.gui.swing.map.map2d.listener.Map2DListener;
import org.geotools.gui.swing.map.map2d.event.Map2DMapAreaEvent;
import org.geotools.gui.swing.map.map2d.listener.StrategyListener;
import org.geotools.gui.swing.map.map2d.overLayer.OverLayer;
import org.geotools.gui.swing.map.map2d.overLayer.InformationOverLayer;
import org.geotools.map.MapContext;
import org.geotools.map.event.MapLayerListEvent;
import org.geotools.map.event.MapLayerListListener;
import org.geotools.renderer.GTRenderer;

/**
 *
 * @author Johann Sorel
 */
public class JDefaultMap2D extends JPanel implements Map2D {

    protected final EventListenerList MAP2DLISTENERS = new EventListenerList();
    protected final Map2D THIS_MAP;
    protected MapContext context;
    protected Envelope mapArea;
    protected RenderingStrategy renderingStrategy;
    private final InformationOverLayer informationOverLayer = new InformationOverLayer();
    private final MapLayerListListener mapLayerListlistener = new MapLayerListListen();
    private final StrategyListener strategylisten = new StrategyListen();
    private final JLayeredPane mapOverLayerPane = new JLayeredPane();
    private final JLayeredPane userOverLayerPane = new JLayeredPane();
    private final JLayeredPane mainOverLayerPane = new JLayeredPane();
    private int nextMapoverLayerIndex = 1;
    private int drawingNumber = 0;
    private Rectangle oldRect = null;
    private Envelope oldMapArea = null;
    private OverLayer backLayer;
    
    private List<OverLayer> userOverLayers = new ArrayList<OverLayer>();

    public JDefaultMap2D() {
        this.THIS_MAP = this;

        setLayout(new BorderLayout());
        mapOverLayerPane.setLayout(new BufferLayout());
        userOverLayerPane.setLayout(new BufferLayout());
        mainOverLayerPane.setLayout(new BufferLayout());

        mainOverLayerPane.add(informationOverLayer, new Integer(3));
        mainOverLayerPane.add(userOverLayerPane, new Integer(2));
        mainOverLayerPane.add(mapOverLayerPane, new Integer(1));

        add(BorderLayout.CENTER, mainOverLayerPane);
        setRenderingStrategy(new SingleVolatileImageStrategy());


        setOpaque(false);
    }

    protected Envelope fixAspectRatio(Rectangle r, Envelope mapArea) {

        if (mapArea == null && context != null) {
            try {
                mapArea = context.getLayerBounds();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        if (mapArea == null) {
            return null;
        }

        double mapWidth = mapArea.getWidth(); /* get the extent of the map */
        double mapHeight = mapArea.getHeight();
        double scaleX = r.getWidth() / mapArea.getWidth(); /*
         * calculate the new
         * scale
         */

        double scaleY = r.getHeight() / mapArea.getHeight();
        double scale = 1.0; // stupid compiler!

        if (scaleX < scaleY) { /* pick the smaller scale */
            scale = scaleX;
        } else {
            scale = scaleY;
        }

        /* calculate the difference in width and height of the new extent */
        double deltaX = /* Math.abs */ ((r.getWidth() / scale) - mapWidth);
        double deltaY = /* Math.abs */ ((r.getHeight() / scale) - mapHeight);

        /*
         * System.out.println("delta x " + deltaX); System.out.println("delta y " +
         * deltaY);
         */

        /* create the new extent */
        Coordinate ll = new Coordinate(mapArea.getMinX() - (deltaX / 2.0), mapArea.getMinY() - (deltaY / 2.0));
        Coordinate ur = new Coordinate(mapArea.getMaxX() + (deltaX / 2.0), mapArea.getMaxY() + (deltaY / 2.0));

        return new Envelope(ll, ur);
    }

    private void rectangleChanged(Rectangle newRect) {

        Envelope newArea = fixAspectRatio(newRect, mapArea);
        fireMapAreaChanged(this.mapArea, newArea);
        this.mapArea = newArea;
        getRenderingStrategy().setMapArea(mapArea);
    }

    private void checkAspect() {

        Rectangle newRect = getBounds();

        if (!newRect.equals(oldRect)) {
            oldRect = newRect;
            rectangleChanged(newRect);
        }

        if (mapArea != null) {

            if (!(mapArea.equals(oldMapArea)) && !(Double.isNaN(mapArea.getMinX()))) {
                oldMapArea = mapArea;
                context.setAreaOfInterest(mapArea, context.getCoordinateReferenceSystem());
            }

        }
    }

    private void raiseDrawingNumber() {
        drawingNumber++;
        informationOverLayer.setDrawing(drawingNumber > 0);
    }

    private void lowerDrawingNumber() {
        if (drawingNumber > 0) {
            drawingNumber--;
        }
        informationOverLayer.setDrawing(drawingNumber > 0);
    }

    private void fireMapAreaChanged(Envelope oldone, Envelope newone) {
        Map2DMapAreaEvent mce = new Map2DMapAreaEvent(this, oldone, newone);

        Map2DListener[] lst = getMap2DListeners();

        for (Map2DListener l : lst) {
            l.mapAreaChanged(mce);
        }

    }

    private void fireMapContextChanged(MapContext oldcontext, MapContext newContext) {
        Map2DContextEvent mce = new Map2DContextEvent(this, oldcontext, newContext);

        Map2DListener[] lst = getMap2DListeners();

        for (Map2DListener l : lst) {
            l.mapContextChanged(mce);
        }

    }

    @Override
    public void paintComponent(Graphics g) {
        checkAspect();
        super.paintComponent(g);
    }

    //----------------------Over/Sub/information layers-------------------------
    public InformationOverLayer getInformationLayer() {
        return informationOverLayer;
    }

    public void setBackLayer(OverLayer back) {

        if (backLayer != null) {
            mainOverLayerPane.remove(backLayer.geComponent());
        }
        backLayer = back;

        if (back != null) {
            mainOverLayerPane.add(backLayer.geComponent(), new Integer(0));
        }

        mainOverLayerPane.revalidate();
        mainOverLayerPane.repaint();
    }

    public OverLayer getBackLayer() {
        return backLayer;
    }

    public void addOverLayer(OverLayer layer){
        if(layer != null && !userOverLayers.contains(layer)){
            layer.setMap2D(THIS_MAP);
            userOverLayers.add(layer);
            userOverLayerPane.add(layer.geComponent(),new Integer(0));
            userOverLayerPane.revalidate();
            userOverLayerPane.repaint();
        }
    }
    
    public void removeOverLayer(OverLayer layer){
        if(layer != null){
            layer.setMap2D(null);
            userOverLayerPane.remove(layer.geComponent());
            userOverLayers.remove(layer);
            userOverLayerPane.revalidate();
            userOverLayerPane.repaint();
        }
        
    }
    
    protected void addMapOverLayer(OverLayer over) {
        mapOverLayerPane.add(over.geComponent(), new Integer(nextMapoverLayerIndex));
        nextMapoverLayerIndex++;
    }

    //-----------------------MAP2D----------------------------------------------    
    public void setContext(MapContext context) {
        if (this.context != null) {
            this.context.removeMapLayerListListener(mapLayerListlistener);
        }

        fireMapContextChanged(this.context, context);
        this.context = context;
        getRenderingStrategy().setContext(context);

        if (context != null) {
            this.context.addMapLayerListListener(mapLayerListlistener);
        }

//        if (renderingStrategy != null) {
//            renderingStrategy.getRenderer().setContext(context);
//            renderer.setContext(this.context);
//        }

        refresh();
    }

    public MapContext getContext() {
        return context;
    }

    public void setMapArea(Envelope newArea) {

        if (newArea != null) {
            Rectangle newRect = getBounds();
            Envelope fitArea = fixAspectRatio(newRect, newArea);
            fireMapAreaChanged(mapArea, fitArea);
            mapArea = fitArea;
            getRenderingStrategy().setMapArea(fitArea);
        }
    }

    public Envelope getMapArea() {
        return mapArea;
    }

    public void setRenderingStrategy(RenderingStrategy stratege) {

        if (stratege != null) {

            GTRenderer ren = null;

            if (renderingStrategy != null) {
                ren = renderingStrategy.getRenderer();
                renderingStrategy.setContext(null);
                mapOverLayerPane.remove(renderingStrategy.getComponent());
                renderingStrategy.removeStrategyListener(strategylisten);
            }
            renderingStrategy = stratege;
            renderingStrategy.addStrategyListener(strategylisten);

            if (ren != null) {
                renderingStrategy.setRenderer(ren);
            }
            renderingStrategy.setContext(context);
            renderingStrategy.setMapArea(mapArea);
            mapOverLayerPane.add(renderingStrategy.getComponent(), new Integer(0));
            mapOverLayerPane.revalidate();

        }
    }

    public RenderingStrategy getRenderingStrategy() {
        return renderingStrategy;
    }

    public void refresh() {
        getRenderingStrategy().reset();
    }

    public JPanel getComponent() {
        return this;
    }

    public void addMap2DListener(Map2DListener listener) {
        MAP2DLISTENERS.add(Map2DListener.class, listener);
    }

    public void removeMap2DListener(Map2DListener listener) {
        MAP2DLISTENERS.remove(Map2DListener.class, listener);
    }

    public Map2DListener[] getMap2DListeners() {
        return MAP2DLISTENERS.getListeners(Map2DListener.class);
    }

    //---------------------- PRIVATE CLASSES------------------------------------    
    private class MapLayerListListen implements MapLayerListListener {

        public void layerAdded(MapLayerListEvent event) {

            if (context.getLayers().length == 1) {
                try {
                    setMapArea(context.getLayerBounds());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void layerRemoved(MapLayerListEvent event) {
        }

        public void layerChanged(MapLayerListEvent event) {
        }

        public void layerMoved(MapLayerListEvent event) {
        }
    }

    private class StrategyListen implements StrategyListener {

        public void setRendering(boolean rendering) {
            if (rendering) {
                raiseDrawingNumber();
            } else {
                lowerDrawingNumber();
            }
        }
    }
}

