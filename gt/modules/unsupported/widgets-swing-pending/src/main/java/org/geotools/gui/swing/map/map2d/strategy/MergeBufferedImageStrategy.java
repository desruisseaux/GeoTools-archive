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
import org.geotools.gui.swing.map.map2d.*;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.map.event.MapLayerListEvent;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.shape.ShapefileRenderer;

/**
 * Optimize Strategy for edition. high memory needed
 * @author Johann Sorel
 */
public class MergeBufferedImageStrategy extends AbstractRenderingStrategy {

    private final JLayeredPane pane = new JLayeredPane();
    private final MapContext buffercontext = new OneLayerContext();
    private final GraphicsConfiguration GC = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
    private Map<MapLayer, BufferedImage> stock = new HashMap<MapLayer, BufferedImage>();
    private BufferComponent comp = new BufferComponent();
    private MapContext oldcontext = null;
    private Envelope oldMapArea = null;
    private Rectangle oldRect = null;
    private Thread thread = null;
    private boolean mustupdate = false;
    private boolean complete = false;
    private int nbthread = 0;

    /**
     * create a default MergeBufferedImageStrategy
     */
    public MergeBufferedImageStrategy() {
        this(new ShapefileRenderer());
    }

    /**
     * create a default MergeBufferedImageStrategy with a specific GTRenderer
     * @param renderer
     */
    public MergeBufferedImageStrategy(GTRenderer renderer) {
        this.renderer = renderer;
        opimizeRenderer();
        pane.setLayout(new BufferLayout());
        pane.add(comp, new Integer(0));
    }

    private void opimizeRenderer() {

        if (renderer != null) {
            Map rendererParams = new HashMap();
            rendererParams.put("optimizedDataLoadingEnabled", new Boolean(true));
            rendererParams.put("maxFiltersToSendToDatastore", new Integer(20));
            //rendererParams.put(ShapefileRenderer.TEXT_RENDERING_KEY, ShapefileRenderer.TEXT_RENDERING_STRING);
            // rendererParams.put(ShapefileRenderer.TEXT_RENDERING_KEY, ShapefileRenderer.TEXT_RENDERING_OUTLINE);
            rendererParams.put(ShapefileRenderer.SCALE_COMPUTATION_METHOD_KEY, ShapefileRenderer.SCALE_OGC);
            renderer.setRendererHints(rendererParams);

            RenderingHints rh;
            rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            rh.add(new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON));
            rh.add(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED));
            rh.add(new RenderingHints(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED));
            rh.add(new RenderingHints(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF));
            rh.add(new RenderingHints(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE));
            renderer.setJava2DHints(rh);
        }

    }

    private int getBufferSize() {
        return pane.getComponentCount();
    }

    private void mergeBuffer() {
        MapLayer[] layers = context.getLayers();

        if (layers.length > 0) {
            BufferedImage img = createBufferImage(layers[0]);
            Graphics2D g2d = (Graphics2D) img.getGraphics();

            for (int i = 1,  n = layers.length; i < n; i++) {
                
                if(stock.containsKey(layers[i])){
                g2d.drawImage(stock.get(layers[i]), null, 0, 0);
                }
            }

            comp.setBuffer(img);

        } else {
            comp.setBuffer(null);
        }



    }

    private synchronized void raiseNB() {
        nbthread++;
        if (nbthread == 1) {
            fireRenderingEvent(true);
        }
    }

    private synchronized void lowerNB() {
        nbthread--;
        if (nbthread == 0) {
            fireRenderingEvent(false);
        }
    }

    private void checkAspect(boolean changed){
                
        Rectangle newRect = comp.getBounds();

        if (!newRect.equals(oldRect)) {
            changed = true;
            oldRect = newRect;
        }

        if ( mapArea != null ) {

            if (!(mapArea.equals(oldMapArea)) && !(Double.isNaN(mapArea.getMinX()))) {
                changed = true;
                oldMapArea = mapArea;
                context.setAreaOfInterest(mapArea, context.getCoordinateReferenceSystem());
            }

            if (changed) {
                changed = false;
                fit();
            }
        }
    }
    
    private void fit(){
        this.complete = true;

        if (thread != null && thread.isAlive()) {
            mustupdate = true;
        } else {
            mustupdate = true;
            thread = new DrawingThread();
            thread.start();
        }
    }
    

    //------------------Rendering Strategy--------------------------------------
    public synchronized BufferedImage createBufferImage(MapLayer layer) {

        if (context != null) {
            try {
                buffercontext.setCoordinateReferenceSystem(context.getCoordinateReferenceSystem());
            } catch (Exception e) {
            }

            buffercontext.addLayer(layer);
            BufferedImage buf = createBufferImage(buffercontext);
            buffercontext.clearLayerList();
            return buf;
        } else {
            return null;
        }

    }

    public synchronized BufferedImage createBufferImage(MapContext context) {

        Rectangle newRect = comp.getBounds();
        Rectangle mapRectangle = new Rectangle(newRect.width, newRect.height);

        if (context != null && mapArea != null && mapRectangle.width > 0 && mapRectangle.height > 0) {
            //NOT OPTIMIZED
//            BufferedImage buf = new BufferedImage(mapRectangle.width, mapRectangle.height, BufferedImage.TYPE_INT_ARGB);
//            Graphics2D ig = buf.createGraphics();
            //GC ACCELERATION 
            BufferedImage buf = GC.createCompatibleImage(mapRectangle.width, mapRectangle.height, BufferedImage.TRANSLUCENT);
            Graphics2D ig = buf.createGraphics();

            renderer.setContext(context);
            renderer.paint((Graphics2D) ig, mapRectangle, mapArea);
            return buf;
        } else {
            return null;
        }

    }
    
    public BufferedImage getBufferImage() {
        return comp.getBuffer();
    }

    public void reset() {
        checkAspect(true);
    }

    public JComponent getComponent() {
        return pane;
    }

    //-----------------Abstract Rendering Strategy------------------------------
     @Override
    public void setMapArea(Envelope area) {
        super.setMapArea(area);
        checkAspect(false);
    }
    
    protected void deletedLayer(MapLayerListEvent event) {
        MapLayer layer = event.getLayer();
        stock.remove(layer);
        mergeBuffer();
    }

    protected void changedLayer(MapLayerListEvent event) {
        MapLayer layer = event.getLayer();
        BufferedImage buffer = createBufferImage(layer);
        stock.put(layer, buffer);
        mergeBuffer();
    }

    protected void addedLayer(MapLayerListEvent event) {
        MapLayer layer = event.getLayer();
        BufferedImage buffer = createBufferImage(layer);
        stock.put(layer, buffer);
        mergeBuffer();
    }

    protected void movedLayer(MapLayerListEvent event) {
        mergeBuffer();
    }

    //-----------------------PRIVATES CLASSES-----------------------------------
    private class DrawingThread extends Thread {

        @Override
        public void run() {

            raiseNB();
            while (mustupdate) {
                mustupdate = false;

                if (context != null) {
                    if (complete || getBufferSize() != context.getLayerCount() || context != oldcontext) {
                        oldcontext = context;
                        complete = false;
                        int contextsize = context.getLayerCount();

                        stock.clear();
                        for (int i = contextsize - 1; i >= 0 && !mustupdate; i--) {
                            MapLayer layer = context.getLayer(i);
                            BufferedImage buffer = createBufferImage(layer);
                            stock.put(layer, buffer);
                            mergeBuffer();
                        }

                    } else {

                        Set<MapLayer> keyset = stock.keySet();
                        MapLayer[] keys = keyset.toArray(new MapLayer[keyset.size()]);
                        int contextsize = keys.length;

                        for (int i = contextsize - 1; i >= 0 && !mustupdate; i--) {
                            stock.put(keys[i], createBufferImage(keys[i]));
                            mergeBuffer();
                        }

                    }

                    //mergeBuffer();
                }
            }
            lowerNB();

        }
    }

    private class BufferComponent extends JComponent {

        private BufferedImage img;

        public void setBuffer(BufferedImage buf) {
            img = buf;
            revalidate();
            repaint();
        }

        public BufferedImage getBuffer(){
            return img;
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            if (img != null) {
                g.drawImage(img, 0, 0, this);
            }
        }
    }
}


