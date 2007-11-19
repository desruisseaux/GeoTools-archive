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
import java.awt.image.BufferedImage;
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
class MultiBufferPane extends MapBufferPane {

    private final JLayeredPane pane = new JLayeredPane();
    private Map<MapLayer, BufferComponent> stock = new HashMap<MapLayer, BufferComponent>();
    private DefaultMap2D map;
    private MapContext oldcontext = null;
    private Thread thread = null;
    private boolean mustupdate = false;
    private boolean complete = false;

    MultiBufferPane(DefaultMap2D map) {
        this.map = map;
        pane.setLayout(new BufferLayout());
    }

    private int getBufferSize() {
        return pane.getComponentCount();
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
        BufferedImage buffer = map.createBufferImage(layer);
        stock.get(layer).setBuffer(buffer);

    }

    public void layerDeleted(MapLayerListEvent event) {
        MapLayer layer = event.getLayer();
        MapContext context = map.context;
        BufferComponent comp = stock.remove(layer);

        int index = event.getFromIndex();
        int size = context.getLayerCount();

        pane.remove(comp);
        for (int i = index; i < size; i++) {
            BufferComponent moving = stock.get(context.getLayer(i));
            int position = pane.getLayer(moving);
            pane.setLayer(moving, position - 1);
        }

    }

    public void layerAdded(MapLayerListEvent event) {
        MapLayer layer = event.getLayer();
        MapContext context = map.context;
        int index = event.getToIndex();

        BufferComponent comp = new BufferComponent();
        comp.setBuffer(map.createBufferImage(layer));

        int size = context.getLayerCount();
        for (int i = index + 1; i < size; i++) {
            BufferComponent moving = stock.get(context.getLayer(i));
            int position = pane.getLayer(moving);
            pane.setLayer(moving, position + 1);
        }

        stock.put(layer, comp);
        pane.add(comp, new Integer(index));

    }

    public void layerMoved(MapLayerListEvent event) {
        MapContext context = map.context;
        int to = event.getToIndex();
        int from = event.getFromIndex();
        int min = (from < to) ? from : to;

        int size = context.getLayerCount();
        for (int i = min; i < size; i++) {
            BufferComponent moving = stock.get(context.getLayer(i));
            pane.setLayer(moving, i);
        }

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
                        pane.removeAll();
                        for (int i = contextsize - 1; i >= 0 && !mustupdate; i--) {
                            MapLayer layer = map.context.getLayer(i);
                            BufferedImage buffer = map.createBufferImage(layer);
                            BufferComponent comp = new BufferComponent();
                            comp.setBuffer(buffer);
                            stock.put(layer, comp);
                            pane.add(comp, new Integer(i));
                        }

                    } else {

                        Set<MapLayer> keyset = stock.keySet();
                        MapLayer[] keys = keyset.toArray(new MapLayer[keyset.size()]);
                        int contextsize = keys.length;

                        for (int i = contextsize - 1; i >= 0 && !mustupdate; i--) {
                            stock.get(keys[i]).setBuffer(map.createBufferImage(keys[i]));
                        }

                    }
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

        @Override
        protected void paintComponent(Graphics g) {
            if (img != null) {
                g.drawImage(img, 0, 0, this);
            }
        }
    }
}


