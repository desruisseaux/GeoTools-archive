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
import javax.swing.JComponent;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.map.event.MapLayerListEvent;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.shape.ShapefileRenderer;

/**
 *
 * @author Johann Sorel
 */
public class SingleBufferedImageStrategy extends AbstractRenderingStrategy {

    private Thread thread = null;
    private BufferComponent comp = new BufferComponent();
    private final MapContext buffercontext = new OneLayerContext();
    private final GraphicsConfiguration GC = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
    private boolean mustupdate = false;
    private Envelope oldMapArea = null;
    private Rectangle oldRect = null;
    private int nbthread = 0;

    public SingleBufferedImageStrategy() {
        this(new ShapefileRenderer());
    }

    public SingleBufferedImageStrategy(GTRenderer renderer) {
        this.renderer = renderer;
        opimizeRenderer();
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

    private void fit() {
        if (thread != null && thread.isAlive()) {
            mustupdate = true;
        } else {
            mustupdate = true;
            thread = new DrawingThread();
            thread.start();
        }

    }

    private void raiseNB() {
        nbthread++;
        if (nbthread == 1) {
            fireRenderingEvent(true);
        }
    }

    private void lowerNB() {
        nbthread--;
        if (nbthread == 0) {
            fireRenderingEvent(false);
        }
    }

    private void checkAspect(boolean changed) {

        Rectangle newRect = comp.getBounds();

        if (!newRect.equals(oldRect)) {
            changed = true;
            oldRect = newRect;
        }

        if (mapArea != null) {

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

    //-----------------------RenderingStrategy----------------------------------
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
        fit();
    }

    public JComponent getComponent() {
        return comp;
    }

    //------------------Abstract RenderingStrategy------------------------------
    @Override
    public void setMapArea(Envelope area) {
        super.setMapArea(area);
        checkAspect(false);
    }

    protected void changedLayer(MapLayerListEvent event) {
        fit();
    }

    protected void deletedLayer(MapLayerListEvent event) {
        fit();
    }

    protected void addedLayer(MapLayerListEvent event) {
        fit();
    }

    protected void movedLayer(MapLayerListEvent event) {
        fit();
    }

    //------------------------PRIVATES CLASSES----------------------------------
    private class DrawingThread extends Thread {

        @Override
        public void run() {
            raiseNB();
            while (mustupdate) {
                mustupdate = false;
                if (context != null && mapArea != null) {
                    comp.setBuffer(createBufferImage(context));
                }
            }
            lowerNB();
        }
    }

    private class BufferComponent extends JComponent {

        private BufferedImage img;

        public void setBuffer(BufferedImage buf) {
            img = buf;
            repaint();
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
    
    