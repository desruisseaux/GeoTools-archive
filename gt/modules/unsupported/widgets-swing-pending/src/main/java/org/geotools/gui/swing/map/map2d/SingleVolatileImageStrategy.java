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

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
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
class SingleVolatileImageStrategy extends RenderingStrategy {

    private BufferComponent comp = new BufferComponent(this);
    private JDefaultMap2D map;

    //optimize with hardware doublebuffer, also called backbuffer
    SingleVolatileImageStrategy(JDefaultMap2D map) {
        this.map = map;

    }

    //----------------------Rendering-------------------------------------------
    public synchronized void renderOn(Graphics2D ig) {
        if (map.context != null && map.mapArea != null && map.mapRectangle.width > 0 && map.mapRectangle.height > 0) {
            map.renderer.setContext(map.context);
            try {
                map.renderer.paint((Graphics2D) ig, map.mapRectangle, map.mapArea);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void stopRendering() {
        map.renderer.stopRendering();
    }

    public synchronized VolatileImage createBackBuffer() {
        if (map.context != null && map.mapArea != null && map.mapRectangle.width > 0 && map.mapRectangle.height > 0) {
            return map.GC.createCompatibleVolatileImage(map.mapRectangle.width, map.mapRectangle.height, VolatileImage.TRANSLUCENT);
        } else {
            return map.GC.createCompatibleVolatileImage(1, 1, VolatileImage.TRANSLUCENT);
        }
    }

    private void fit() {
        MapContext context = map.context;
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

    @Override
    public JComponent getComponent() {
        return comp;
    }
    private int nb = 0;

    public void raiseNB() {
        nb++;
        if (nb == 1) {
            setChanged();
            notifyObservers(1);
        }
    }

    public void lowerNB() {
        nb--;
        if (nb == 0) {
            setChanged();
            notifyObservers(0);
        }
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

        pane.stopRendering();
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
