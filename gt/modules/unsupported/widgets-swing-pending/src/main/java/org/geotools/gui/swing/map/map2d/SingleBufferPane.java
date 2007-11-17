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
import javax.swing.JComponent;
import org.geotools.map.MapContext;
import org.geotools.map.event.MapLayerListEvent;

/**
 *
 * @author Johann Sorel
 */
public class SingleBufferPane extends MapBufferPane {

    private Thread thread = null;
    private BufferComponent comp = new BufferComponent();
    private DefaultMap2D map;
    private boolean mustupdate = false;

    SingleBufferPane(DefaultMap2D map) {
        this.map = map;
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

    public void redraw() {
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
    
    
    private class DrawingThread extends Thread{
              
        @Override
        public void run(){
            
            raiseNB();
            while (mustupdate) {
                mustupdate = false;
                MapContext context = map.context;
                if (context != null) {                    
                    System.out.println("->update");
                    comp.setBuffer(map.createBufferImage(context));
                }                
            }
            lowerNB();            
        }      
        
    };
    
    
    
    
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
    };
}
    
    