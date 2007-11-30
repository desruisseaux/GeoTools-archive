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
import org.geotools.gui.swing.map.map2d.strategy.RenderingStrategy;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.map.event.MapLayerListEvent;
import org.geotools.renderer.GTRenderer;

/**
 *
 * @author Johann Sorel
 */
public class SingleBufferedImageStrategy extends AbstractRenderingStrategy {

    private Thread thread = null;
    private BufferComponent comp = new BufferComponent();
    private boolean mustupdate = false;

    //optimize with hardware doublebuffer, also called backbuffer
    public SingleBufferedImageStrategy() {
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

    public JComponent getComponent() {
        return comp;
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

    private int nb = 0;

    public void raiseNB() {
       //map.raiseDrawingNumber();
    }

    public void lowerNB() {
        //map.lowerDrawingNumber();
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
    };
         

        private class BufferComponent extends JComponent {

            private BufferedImage img;

            public void setBuffer(BufferedImage buf) {
                img = buf;
                repaint();
            }
            
        @Override
            public void paintComponent(Graphics g) {
                if (img != null) {
                    g.drawImage(img, 0, 0, this);
                }
            }       
        };

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

   
        
        
        
}
    
    