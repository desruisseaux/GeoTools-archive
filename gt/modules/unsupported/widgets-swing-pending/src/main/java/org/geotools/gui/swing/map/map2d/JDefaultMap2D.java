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
import org.geotools.gui.swing.map.map2d.overLayer.MapDecoration;
import org.geotools.gui.swing.map.map2d.overLayer.InformationDecoration;
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
    
    private static final MapDecoration[] EMPTY_OVERLAYER_ARRAY = {};
    private final InformationDecoration informationDecoration = new InformationDecoration();
    private final MapLayerListListener mapLayerListlistener = new MapLayerListListen();
    private final List<MapDecoration> userDecorations = new ArrayList<MapDecoration>();
    private final StrategyListener strategylisten = new StrategyListen();
    private final JLayeredPane mapDecorationPane = new JLayeredPane();
    private final JLayeredPane userDecorationPane = new JLayeredPane();
    private final JLayeredPane mainDecorationPane = new JLayeredPane();
    private int nextMapDecorationIndex = 1;
    private int drawingNumber = 0;
    private Rectangle oldRect = null;
    private Envelope oldMapArea = null;
    private MapDecoration backDecoration;
    
    

    public JDefaultMap2D() {
        this.THIS_MAP = this;

        setLayout(new BorderLayout());
        mapDecorationPane.setLayout(new BufferLayout());
        userDecorationPane.setLayout(new BufferLayout());
        mainDecorationPane.setLayout(new BufferLayout());

        mainDecorationPane.add(informationDecoration, new Integer(3));
        mainDecorationPane.add(userDecorationPane, new Integer(2));
        mainDecorationPane.add(mapDecorationPane, new Integer(1));

        add(BorderLayout.CENTER, mainDecorationPane);
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
        informationDecoration.setDrawing(drawingNumber > 0);
    }

    private void lowerDrawingNumber() {
        if (drawingNumber > 0) {
            drawingNumber--;
        }
        informationDecoration.setDrawing(drawingNumber > 0);
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
    public InformationDecoration getInformationLayer() {
        return informationDecoration;
    }

    public void setBackDecoration(MapDecoration back) {

        if (backDecoration != null) {
            mainDecorationPane.remove(backDecoration.geComponent());
        }
        backDecoration = back;

        if (back != null) {
            mainDecorationPane.add(backDecoration.geComponent(), new Integer(0));
        }

        mainDecorationPane.revalidate();
        mainDecorationPane.repaint();
    }

    public MapDecoration getBackDecoration() {
        return backDecoration;
    }

    public void addDecoration(MapDecoration layer){
                
        if(layer != null && !userDecorations.contains(layer)){
            layer.setMap2D(THIS_MAP);
            userDecorations.add(layer);                        
            userDecorationPane.add(layer.geComponent(),new Integer( userDecorations.indexOf(layer) ));
            userDecorationPane.revalidate();
            userDecorationPane.repaint();
        }
    }
    
    public void addDecoration(int index,MapDecoration layer){
                
        if(layer != null && !userDecorations.contains(layer)){
            layer.setMap2D(THIS_MAP);
            userDecorations.add(index,layer);                        
            userDecorationPane.add(layer.geComponent(),new Integer( userDecorations.indexOf(layer) ));
            userDecorationPane.revalidate();
            userDecorationPane.repaint();
        }
    }
    
    public int getDecorationIndex(MapDecoration layer){
        return userDecorations.indexOf(layer);
    }
    
    public void removeDecoration(MapDecoration layer){
        if(layer != null && userDecorations.contains(layer)){
            layer.setMap2D(null);
            userDecorations.remove(layer);
            userDecorationPane.remove(layer.geComponent());            
            userDecorationPane.revalidate();
            userDecorationPane.repaint();
        }        
    }
    
    public MapDecoration[] getDecorations(){
        return userDecorations.toArray(EMPTY_OVERLAYER_ARRAY);
    }
    
    protected void addMapDecoration(MapDecoration over) {
        mapDecorationPane.add(over.geComponent(), new Integer(nextMapDecorationIndex));
        nextMapDecorationIndex++;
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

        if(stratege == null){
            throw new NullPointerException();
        }
        
        if (stratege != null) {

            GTRenderer ren = null;

            if (renderingStrategy != null) {
                ren = renderingStrategy.getRenderer();
                renderingStrategy.setContext(null);
                mapDecorationPane.remove(renderingStrategy.getComponent());
                renderingStrategy.removeStrategyListener(strategylisten);
            }
            renderingStrategy = stratege;
            renderingStrategy.addStrategyListener(strategylisten);

            if (ren != null) {
                renderingStrategy.setRenderer(ren);
            }
            renderingStrategy.setContext(context);
            renderingStrategy.setMapArea(mapArea);
            mapDecorationPane.add(renderingStrategy.getComponent(), new Integer(0));
            mapDecorationPane.revalidate();

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

