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
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.map.event.MapLayerListEvent;
import org.geotools.referencing.operation.matrix.AffineTransform2D;

/**
 * 
 * @author Johann Sorel
 */
class MergeBufferedImageStrategy extends RenderingStrategy {

    private final JLayeredPane pane = new JLayeredPane();
    private Map<MapLayer, BufferedImage> stock = new HashMap<MapLayer, BufferedImage>();
    private BufferComponent component = new BufferComponent();
    private DefaultMap2D map;
    private MapContext oldcontext = null;
    private Thread thread = null;
    private boolean mustupdate = false;
    private boolean complete = false;

    MergeBufferedImageStrategy(DefaultMap2D map) {
        this.map = map;
        pane.setLayout(new BufferLayout());
        pane.add(component,new Integer(0));
    }

    private int getBufferSize() {
        return pane.getComponentCount();
    }


    private void mergeBuffer(){
        MapContext context = map.context;
        MapLayer[] layers = context.getLayers();
        
        if(layers.length >0){
            BufferedImage img = map.createBufferImage(layers[0]);
            Graphics2D g2d = (Graphics2D) img.getGraphics();
            
            for(int i=1, n = layers.length; i<n ; i++){
                g2d.drawImage(stock.get(layers[i]), null, 0, 0);                
            }
            
            component.setBuffer(img);
            
        }else{
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
        BufferedImage buffer = map.createBufferImage(layer);
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
        BufferedImage buffer = map.createBufferImage(layer);       
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
                            BufferedImage buffer = map.createBufferImage(layer);
                            stock.put(layer, buffer);
                        }

                    } else {

                        Set<MapLayer> keyset = stock.keySet();
                        MapLayer[] keys = keyset.toArray(new MapLayer[keyset.size()]);
                        int contextsize = keys.length;

                        for (int i = contextsize - 1; i >= 0 && !mustupdate; i--) {
                            stock.put(keys[i], map.createBufferImage(keys[i]));
                        }

                    }
                    
                    mergeBuffer();
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


