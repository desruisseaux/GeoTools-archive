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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.gui.swing.map.map2d.event.Map2DContextEvent;
import org.geotools.gui.swing.map.map2d.event.Map2DMapAreaEvent;
import org.geotools.gui.swing.map.map2d.listener.StrategyListener;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.map.event.MapLayerListEvent;
import org.geotools.map.event.MapLayerListListener;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.shape.ShapefileRenderer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

/**
 * Not optimize Strategy, use a single bufferedImage. slow.
 * Must repaint everything each time.
 * @author Johann Sorel
 */
public class SingleBufferedImageStrategy implements RenderingStrategy {

    private final MapLayerListListener mapLayerListlistener = new MapLayerListListen();
    private final EventListenerList listeners = new EventListenerList();
    private MapContext context = new DefaultMapContext(DefaultGeographicCRS.WGS84);
    private GTRenderer renderer = new ShapefileRenderer();
    private Envelope oldCompMapArea = new Envelope(0, 0, 1, 1);
    private Envelope compMapArea = new Envelope(0, 0, 1, 1);
    private ReferencedEnvelope oldAreaOfInterest = null;
    private Rectangle oldRect = null;
    private final DrawingThread thread = new DrawingThread();
    private final BufferComponent comp = new BufferComponent();
    private final MapContext buffercontext = new OneLayerContext();
    private final GraphicsConfiguration GC = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
    private boolean mustupdate = false;
    private double rotation = 0d;
    private boolean autorefresh = true;
    private boolean isRendering = false;

    /**
     * create a default SingleBufferedImageStrategy
     */
    public SingleBufferedImageStrategy() {
        this(new ShapefileRenderer());
    }

    /**
     * create a default SingleBufferedImageStrategy with a specific GTRenderer
     * @param renderer
     */
    public SingleBufferedImageStrategy(GTRenderer renderer) {

        setRenderer(renderer);
        opimizeRenderer();

        comp.addComponentListener(new ComponentListener() {

            public void componentResized(ComponentEvent arg0) {
                setMapArea(compMapArea);
            }

            public void componentMoved(ComponentEvent arg0) {
            }

            public void componentShown(ComponentEvent arg0) {
                setMapArea(compMapArea);
            }

            public void componentHidden(ComponentEvent arg0) {
            }
        });

        thread.start();

    }

    private void opimizeRenderer() {

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

    private Envelope fixAspectRatio(Rectangle rect, Envelope area) {

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

    private void fit() {

        if (checkAspect()) {
            testRefresh();
        }
    }

    private void testRefresh() {
        if (autorefresh) {
            refresh();
        }
    }

    private boolean checkAspect() {
        boolean changed = false;

        Rectangle newRect = comp.getBounds();

        ReferencedEnvelope newAreaOfInterest = new ReferencedEnvelope(compMapArea, context.getCoordinateReferenceSystem());

        if (!newRect.equals(oldRect) || !newAreaOfInterest.equals(oldAreaOfInterest)) {
            changed = true;
            oldRect = newRect;
            oldAreaOfInterest = newAreaOfInterest;
        }

        return changed;
    }
    private boolean lock = false;

    public void setLock(boolean val) {
        lock = val;
    }

    public boolean isLock() {
        return lock;
    }

    //------------------TRIGGERS------------------------------------------------
    private void fireRenderingEvent(boolean isRendering) {
        this.isRendering = isRendering;
        StrategyListener[] lst = getStrategyListeners();

        for (StrategyListener l : lst) {
            l.setRendering(isRendering);
        }
    }

    private void fireMapAreaChanged(Envelope oldone, Envelope newone) {
        
        Map2DMapAreaEvent mce = new Map2DMapAreaEvent(this, oldone, newone);

        StrategyListener[] lst = getStrategyListeners();

        for (StrategyListener l : lst) {
            l.mapAreaChanged(mce);
        }

    }

    private void fireMapContextChanged(MapContext oldcontext, MapContext newContext) {
        Map2DContextEvent mce = new Map2DContextEvent(this, oldcontext, newContext);

        StrategyListener[] lst = getStrategyListeners();

        for (StrategyListener l : lst) {
            l.mapContextChanged(mce);
        }

    }

    //-----------------------RenderingStrategy----------------------------------
    public synchronized BufferedImage createBufferImage(MapLayer layer) {

        try {
            buffercontext.setCoordinateReferenceSystem(context.getCoordinateReferenceSystem());
        } catch (Exception e) {
        }

        buffercontext.addLayer(layer);
        BufferedImage buf = createBufferImage(buffercontext);
        buffercontext.clearLayerList();
        return buf;

    }

    public synchronized BufferedImage createBufferImage(MapContext context) {


        synchronized (renderer) {
            Rectangle newRect = comp.getBounds();
            Rectangle mapRectangle = new Rectangle(newRect.width, newRect.height);

            if (context != null && compMapArea != null && mapRectangle.width > 0 && mapRectangle.height > 0) {
                //NOT OPTIMIZED
//            BufferedImage buf = new BufferedImage(mapRectangle.width, mapRectangle.height, BufferedImage.TYPE_INT_ARGB);
//            Graphics2D ig = buf.createGraphics();
                //GraphicsConfiguration ACCELERATION 
                BufferedImage buf = GC.createCompatibleImage(mapRectangle.width, mapRectangle.height, BufferedImage.TRANSLUCENT);
                Graphics2D ig = buf.createGraphics();

                renderer.stopRendering();
                renderer.setContext(context);
                renderer.paint((Graphics2D) ig, mapRectangle, compMapArea);

                return buf;
            } else {
                return null;
            }
        }

    }

    public BufferedImage getBufferImage() {
        return comp.getBuffer();
    }

    public JComponent getComponent() {
        return comp;
    }

    public void refresh() {
        try {
            oldCompMapArea = compMapArea;
            compMapArea = fixAspectRatio(comp.getBounds(), compMapArea);
            
            if(!oldCompMapArea.equals(compMapArea)){
                fireMapAreaChanged(oldCompMapArea, compMapArea);
                oldCompMapArea = compMapArea;
            }
        } catch (Exception e) {
        }

        mustupdate = true;
        thread.wake();
    }

    public void setRenderer(GTRenderer renderer) {
        this.renderer = renderer;
    }

    public GTRenderer getRenderer() {
        return renderer;
    }

    public void setContext(MapContext context) {

        if (context == null) {
            throw new NullPointerException("Context can't be null");
        }


        if (this.context != context) {
            this.context.removeMapLayerListListener(mapLayerListlistener);

            MapContext oldContext = this.context;
            this.context = context;
            this.context.addMapLayerListListener(mapLayerListlistener);
            fit();

            fireMapContextChanged(oldContext, this.context);
        }
    }

    public MapContext getContext() {
        return context;
    }

    public void setMapArea(Envelope area) {
        
        if (area == null) {
            throw new NullPointerException("Area can't be null.");
        }

        Envelope oldenv = compMapArea;
        Envelope env = fixAspectRatio(comp.getBounds(), area);
        compMapArea = env;

        fit();
        fireMapAreaChanged(oldenv, env);

    }

    public Envelope getMapArea() {
        return compMapArea;
    }

    public void addStrategyListener(StrategyListener listener) {
        listeners.add(StrategyListener.class, listener);
    }

    public void removeStrategyListener(StrategyListener listener) {
        listeners.remove(StrategyListener.class, listener);
    }

    public StrategyListener[] getStrategyListeners() {
        return listeners.getListeners(StrategyListener.class);
    }

    public void setRotation(double d) {
        rotation = d;
    }

    public double getRotation() {
        return rotation;
    }

    public void setAutoRefreshEnabled(boolean ref) {
        autorefresh = ref;
    }

    public boolean isAutoRefresh() {
        return autorefresh;
    }

    public boolean isPainting() {
        return isRendering;
    }

    //------------------------PRIVATES CLASSES----------------------------------
    private class MapLayerListListen implements MapLayerListListener {

        public void layerAdded(MapLayerListEvent event) {
            testRefresh();
        }

        public void layerRemoved(MapLayerListEvent event) {
            testRefresh();
        }

        public void layerChanged(MapLayerListEvent event) {
            testRefresh();
        }

        public void layerMoved(MapLayerListEvent event) {
            testRefresh();
        }
    }

    private class DrawingThread extends Thread {

        @Override
        public void run() {

            while (true) {

                while (isLock()) {
                    try {
                        sleep(25);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
                setLock(true);

                if (mustupdate) {
                    fireRenderingEvent(true);
                    comp.setBuffer(createBufferImage(context));
                    mustupdate = false;
                    fireRenderingEvent(false);

                }

                setLock(false);
                block();
            }
        }

        public synchronized void wake() {
            notifyAll();
        }

        private synchronized void block() {
            try {
                wait();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    private class BufferComponent extends JComponent {

        private BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

        public void setBuffer(BufferedImage buf) {
            img = buf;
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    repaint();
                }
            });

        }

        public BufferedImage getBuffer() {
            return img;
        }

        @Override
        public void paintComponent(Graphics g) {
            if (img != null) {
                g.drawImage(img, 0, 0, this);
            }
        }
        }
}
    

    