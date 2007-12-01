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
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.RepaintManager;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.map.event.MapLayerListEvent;
import org.geotools.map.event.MapLayerListListener;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.shape.ShapefileRenderer;

/**
 *
 * @author Johann Sorel
 */
public class SingleVolatileImageStrategy extends AbstractRenderingStrategy {

    
    private final GraphicsConfiguration GC = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
    private final MapContext buffercontext = new OneLayerContext();
    private final BufferComponent comp = new BufferComponent(this);
    private int nbthread = 0;

    public SingleVolatileImageStrategy() {
        this(new ShapefileRenderer());
    }

    public SingleVolatileImageStrategy(GTRenderer renderer) {
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

    private synchronized void renderOn(Graphics2D ig) {

        Rectangle newRect = comp.getBounds();
        Rectangle mapRectangle = new Rectangle(newRect.width, newRect.height);

        if (context != null && mapArea != null && mapRectangle.width > 0 && mapRectangle.height > 0) {
            getRenderer().setContext(context);
            try {
                getRenderer().paint((Graphics2D) ig, mapRectangle, mapArea);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized VolatileImage createBackBuffer() {

        Rectangle newRect = comp.getBounds();
        Rectangle mapRectangle = new Rectangle(newRect.width, newRect.height);


        if (context != null && mapArea != null && mapRectangle.width > 0 && mapRectangle.height > 0) {
            return GC.createCompatibleVolatileImage(mapRectangle.width, mapRectangle.height, VolatileImage.TRANSLUCENT);
        } else {
            return GC.createCompatibleVolatileImage(1, 1, VolatileImage.TRANSLUCENT);
        }
    }

    private void fit() {
        if (context != null && mapArea != null) {
            comp.refresh();
        }
    }

    protected void deletedLayer(MapLayerListEvent event) {
        fit();
    }

    protected void changedLayer(MapLayerListEvent event) {
        fit();
    }

    protected void addedLayer(MapLayerListEvent event) {
        fit();
    }

    protected void movedLayer(MapLayerListEvent event) {
        fit();
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

    //--------------------RenderingStrategy-------------------------------------
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

    public void reset() {
        fit();
    }

    public JComponent getComponent() {
        return comp;
    }

    //------------------------PRIVATES CLASSES----------------------------------
    private class BufferComponent extends JComponent {

        public Boolean ACTIF = false;
        private GraphicsConfiguration GC;
        private SingleVolatileImageStrategy pane;
        private VolatileImage buffer;
        private RepaintingThread repainter;
        private RerenderingThread rerenderer;
        private boolean update = true;

        BufferComponent(SingleVolatileImageStrategy bufpane) {
            this.pane = bufpane;
            RepaintManager.currentManager(this).setDoubleBufferingEnabled(true);
            setDoubleBuffered(true);

            GC = this.getGraphicsConfiguration();
        }

        public void refresh() {
            update = true;
            repaint();
        }

        private void startRender(Graphics g) {

            if (repainter != null && repainter.isAlive()) {
                repainter.setActive(true);
            } else {
                repainter = new RepaintingThread(this);
                repainter.setActive(true);
                repainter.start();
            }

            rerenderer = new RerenderingThread(this);
            rerenderer.setGraphics(g);
            rerenderer.start();

        }

        @Override
        public void paintComponent(Graphics g) {


            if (update || buffer == null || buffer.validate(GC) == VolatileImage.IMAGE_INCOMPATIBLE) {
                update = false;

                buffer = pane.createBackBuffer();

                do {
                    int valCode = buffer.validate(GC);
                    if (valCode == VolatileImage.IMAGE_INCOMPATIBLE) {
                        buffer = pane.createBackBuffer(); // recreate the hardware accelerated image.
                    }

                    final Graphics offscreenGraphics = buffer.getGraphics();
                    startRender(offscreenGraphics);

                    g.drawImage(buffer, 0, 0, this);

                } while (buffer.contentsLost());

            } else {
                g.drawImage(buffer, 0, 0, this);
            }

        }

        private class RerenderingThread extends Thread {

            private BufferComponent comp = null;
            private Graphics g;

            RerenderingThread(BufferComponent comp) {
                this.comp = comp;
                setOpaque(false);
            }

            public void setGraphics(Graphics g) {
                this.g = g;
            }

            public void run() {
                pane.renderOn((Graphics2D) g);

                if (repainter != null) {
                    repainter.setActive(false);
                }
            }
        }

        private class RepaintingThread extends Thread {

            private BufferComponent comp = null;
            private boolean marche = false;

            RepaintingThread(BufferComponent comp) {
                this.comp = comp;
            }

            public void setActive(boolean val) {
                marche = val;

                if (!val) {
                    comp.repaint();
                }
            }

            @Override
            public void run() {

                raiseNB();
                while (marche) {
                    try {
                        sleep(300);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(SingleVolatileImageStrategy.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    comp.repaint();
                }
                lowerNB();
            }
        }
    }
    
    
}





