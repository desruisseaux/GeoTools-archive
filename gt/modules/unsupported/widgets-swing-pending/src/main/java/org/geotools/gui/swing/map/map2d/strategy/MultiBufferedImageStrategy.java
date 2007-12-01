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

import org.geotools.gui.swing.map.map2d.*;
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
import org.geotools.renderer.GTRenderer;

/**
 * 
 * @author Johann Sorel
 */
public class MultiBufferedImageStrategy extends AbstractRenderingStrategy {

    private final JLayeredPane pane = new JLayeredPane();
    private Map<MapLayer, BufferComponent> stock = new HashMap<MapLayer, BufferComponent>();
    private MapContext oldcontext = null;
    private Thread thread = null;
    private boolean mustupdate = false;
    private boolean complete = false;

    public MultiBufferedImageStrategy() {
        pane.setLayout(new BufferLayout());
    }

    private int getBufferSize() {
        return pane.getComponentCount();
    }


    public void reset() {
        this.complete = true;
        
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
        BufferedImage buffer = createBufferImage(layer);
        stock.get(layer).setBuffer(buffer);

    }

    public void layerDeleted(MapLayerListEvent event) {
        MapLayer layer = event.getLayer();
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
        int index = event.getToIndex();

        BufferComponent comp = new BufferComponent();
        comp.setBuffer(createBufferImage(layer));

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
        int to = event.getToIndex();
        int from = event.getFromIndex();
        int min = (from < to) ? from : to;

        int size = context.getLayerCount();
        for (int i = min; i < size; i++) {
            BufferComponent moving = stock.get(context.getLayer(i));
            pane.setLayer(moving, i);
        }

    }

    public JComponent getComponent() {
        return pane;
    }
    private int nb = 0;

    public void raiseNB() {
        //map.raiseDrawingNumber();
    }

    public void lowerNB() {
        //map.lowerDrawingNumber();
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
                        pane.removeAll();
                        for (int i = contextsize - 1; i >= 0 && !mustupdate; i--) {
                            MapLayer layer = context.getLayer(i);
                            BufferedImage buffer = createBufferImage(layer);
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
                            stock.get(keys[i]).setBuffer(createBufferImage(keys[i]));
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

    public void setRenderer(GTRenderer renderer) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public GTRenderer getRenderer() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public BufferedImage createBufferImage(MapLayer layer) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public BufferedImage createBufferImage(MapContext context) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void deletedLayer(MapLayerListEvent event) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void changedLayer(MapLayerListEvent event) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void addedLayer(MapLayerListEvent event) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void movedLayer(MapLayerListEvent event) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}


