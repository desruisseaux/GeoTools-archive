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
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.map.event.MapLayerListEvent;

/**
 * 
 * @author Johann Sorel
 */
class VolatileMultiBufferPane extends MapBufferPane {

    private final JLayeredPane pane = new JLayeredPane();
    private final MapContext buffercontext = new OneLayerContext();
    private Map<MapLayer, VolatileImage> stock = new HashMap<MapLayer, VolatileImage>();
    private BufferComponent component = new BufferComponent();
    private DefaultMap2D map;
    private MapContext oldcontext = null;
    private Thread thread = null;
    private boolean mustupdate = false;
    private boolean complete = false;

    VolatileMultiBufferPane(DefaultMap2D map) {
        this.map = map;
        pane.setLayout(new BufferLayout());
        pane.add(component, new Integer(0));
    }

    private int getBufferSize() {
        return pane.getComponentCount();
    }

    public synchronized void renderOn(Graphics2D ig, MapLayer layer) {
        if (map.context != null && map.mapArea != null && map.mapRectangle.width > 0 && map.mapRectangle.height > 0) {
            
            buffercontext.clearLayerList();
            buffercontext.addLayer(layer);
            
            try {
                buffercontext.setCoordinateReferenceSystem(map.context.getCoordinateReferenceSystem());
            } catch (Exception e) {
            }
            
            map.renderer.setContext(buffercontext);
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

    private void mergeBuffer() {
        MapContext context = map.context;
        MapLayer[] layers = context.getLayers();

        if (layers.length > 0) {
            VolatileImage img = createBackBuffer();
            Graphics2D g2d = (Graphics2D) img.getGraphics();

            for (int i = 1,  n = layers.length; i < n; i++) {
                System.out.println("la");
                
                g2d.drawImage(stock.get(layers[i]), null,component);
            }

            component.setBuffer(img);

        } else {
            component.setBuffer(null);
        }



    }

    public void redraw(boolean complete) {
        this.complete = complete;

        if (thread != null && thread.isAlive()) {
            mustupdate = true;
        } else {
            mustupdate = true;
            thread = new DrawingThread();
            thread.start();
        }

    }

    public void layerChanged(MapLayerListEvent event) {
        MapLayer layer = event.getLayer();
        VolatileImage buffer = createBackBuffer();
        renderOn((Graphics2D)buffer.getGraphics(),layer);
        stock.put(layer, buffer);

        mergeBuffer();
    }

    public void layerDeleted(MapLayerListEvent event) {
        MapLayer layer = event.getLayer();
        stock.remove(layer);

        mergeBuffer();
    }

    public void layerAdded(MapLayerListEvent event) {
        MapLayer layer = event.getLayer();
        VolatileImage buffer = createBackBuffer();
        renderOn((Graphics2D)buffer.getGraphics(),layer);
        stock.put(layer, buffer);

        mergeBuffer();
    }

    public void layerMoved(MapLayerListEvent event) {
        mergeBuffer();
    }

    @Override
    public JComponent getComponent() {
        return pane;
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

    //-----------------------PRIVATES CLASSES-----------------------------------
    private class DrawingThread extends Thread {

        @Override
        public void run() {

            raiseNB();
            while (mustupdate) {
                mustupdate = false;

                MapContext context = map.context;
                if (context != null) {
                    if (complete || getBufferSize() != context.getLayerCount() || context != oldcontext) {
                        oldcontext = context;
                        complete = false;
                        int contextsize = map.context.getLayerCount();

                        stock.clear();
                        for (int i = contextsize - 1; i >= 0 && !mustupdate; i--) {
                            MapLayer layer = map.context.getLayer(i);
                            VolatileImage buffer = createBackBuffer();
                            renderOn((Graphics2D)buffer.getGraphics(),layer);
                            stock.put(layer, buffer);
                        }

                    } else {

                        Set<MapLayer> keyset = stock.keySet();
                        MapLayer[] keys = keyset.toArray(new MapLayer[keyset.size()]);
                        int contextsize = keys.length;

                        for (int i = contextsize - 1; i >= 0 && !mustupdate; i--) {
                            VolatileImage buffer = createBackBuffer();
                            renderOn((Graphics2D)buffer.getGraphics(),keys[i]);
                            stock.put(keys[i],buffer);
                        }

                    }

                    mergeBuffer();
                }
            }
            lowerNB();

        }
    }

    private class BufferComponent extends JComponent {

        private VolatileImage img;

        public void setBuffer(VolatileImage buf) {
            img = buf;
            revalidate();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (img != null) {
                g.drawImage(img, 0, 0, this);
            }
        }
    }
}


