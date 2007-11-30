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
import org.geotools.gui.swing.map.map2d.strategy.RenderingStrategy;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.image.VolatileImage;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.RepaintManager;
import org.geotools.map.MapContext;
import org.geotools.map.event.MapLayerListEvent;


/**
 *
 * @author Johann Sorel
 */
public class SingleVolatileImageStrategy implements RenderingStrategy {

    private final GraphicsConfiguration GC = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
    
    private BufferComponent comp = new BufferComponent(this);
    private JDefaultMap2D map;

    //optimize with hardware doublebuffer, also called backbuffer
    public SingleVolatileImageStrategy(JDefaultMap2D map) {
        this.map = map;

    }

    //----------------------Rendering-------------------------------------------
    public synchronized void renderOn(Graphics2D ig) {
        MapContext context = map.getContext();
        Envelope mapArea = map.getMapArea();
        
        Rectangle newRect = comp.getBounds();
        Rectangle mapRectangle = new Rectangle(newRect.width, newRect.height);
        
        if (context != null && mapArea != null && mapRectangle.width > 0 && mapRectangle.height > 0) {
            map.getRenderer().setContext(context);
            try {
                map.getRenderer().paint((Graphics2D) ig, mapRectangle, mapArea);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

//    public synchronized void stopRendering() {
//        map.renderer.stopRendering();
//    }

    public synchronized VolatileImage createBackBuffer() {
        MapContext context = map.getContext();
        Envelope mapArea = map.getMapArea();
        
        Rectangle newRect = comp.getBounds();
        Rectangle mapRectangle = new Rectangle(newRect.width, newRect.height);
        
        
        if (context != null && mapArea != null && mapRectangle.width > 0 && mapRectangle.height > 0) {
            return GC.createCompatibleVolatileImage(mapRectangle.width,mapRectangle.height, VolatileImage.TRANSLUCENT);
        } else {
            return GC.createCompatibleVolatileImage(1, 1, VolatileImage.TRANSLUCENT);
        }
    }

    private void fit() {
        MapContext context = map.getContext();
        if (context != null && map.getMapArea() != null) {
            comp.refresh();
        }
    }

    public void redraw(boolean complete) {
        fit();
    }

    public void layerChanged(MapLayerListEvent event) {
        fit();
    }

    public void layerDeleted(MapLayerListEvent event) {
        fit();
    }

    public void layerAdded(MapLayerListEvent event) {
        fit();
    }

    public void layerMoved(MapLayerListEvent event) {
        fit();
    }

    public JComponent getComponent() {
        return comp;
    }

    public void raiseNB() {
        map.raiseDrawingNumber();
    }

    public void lowerNB() {
        map.lowerDrawingNumber();
    }

    //------------------------PRIVATES CLASSES----------------------------------
}
class BufferComponent extends JComponent {

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

//        buffer = pane.createBackBuffer();
//        do {
//            GraphicsConfiguration gc = this.getGraphicsConfiguration();
//            int valCode = buffer.validate(gc);
//            // This means the device doesn't match up to this hardware accelerated image.
//            if (valCode == VolatileImage.IMAGE_INCOMPATIBLE) {
//                buffer = pane.createBackBuffer(); // recreate the hardware accelerated image.
//            }
//
//            final Graphics offscreenGraphics = buffer.getGraphics();
//
//            startRender(offscreenGraphics);
//
//        } while (buffer.contentsLost());
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

        //pane.stopRendering();
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

            while (marche) {
                try {
                    sleep(300);
                } catch (InterruptedException ex) {
                    Logger.getLogger(SingleVolatileImageStrategy.class.getName()).log(Level.SEVERE, null, ex);
                }
                comp.repaint();
            }
        }
    }
}
