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

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLayeredPane;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.map.event.MapLayerListEvent;

/**
 * 
 * @author Johann Sorel
 */
class BufferLayerPane extends JLayeredPane{
           
        
    List<LayerComponent> comps = new ArrayList<LayerComponent>();
    final DefaultMap2D map;
    
    
    BufferLayerPane(DefaultMap2D map){
        this.map = map;
        setLayout(new BorderLayout());
    }
    
    

    void fireDelete(MapLayerListEvent event) {        
        System.out.println("DELETE");
        int index = event.getFromIndex();
        LayerComponent comp = comps.get(index);
        remove(comp);
        comps.remove(index);

        int size = comps.size();
        for (int i = index; i < size; i++) {
            moveToFront(comps.get(index));
        }
    }

    void fireChange(MapLayerListEvent event) {        
        System.out.println("CHANGE");
        int index = event.getFromIndex();
        LayerComponent comp = comps.get(index);
        comp.img = map.createBufferImage(event.getLayer());
        comp.repaint();
    }

    void fireAdd(MapLayerListEvent event){
        System.out.println("ADD");
        MapLayer layer = event.getLayer();
        int index = map.context.indexOf(layer);
        
        int size = comps.size();
        for (int i = index; i < size; i++) {
            moveToBack(comps.get(index));
        }
        
        LayerComponent comp = new LayerComponent();
        comp.img = map.createBufferImage(layer);
        comp.layer = layer;
        comps.add(index,comp);
        add(comp, BorderLayout.CENTER);  
        setLayer(comp, index);
        revalidate();
        repaint();
    }
    
    void fireMove(MapLayerListEvent event){
        System.out.println("MOVE");
        
        
                comps.clear();
                removeAll();

                

                            MapLayer[] layers = map.context.getLayers();
                            for (final MapLayer layer : layers) {

                                
                                new Thread() {
                                @Override
                                public void run() {

                                                LayerComponent comp = new LayerComponent();
                                                comp.img = map.createBufferImage(layer);
                                                comp.layer = layer;
                                                comps.add(comp);
                                                add(comp,BorderLayout.CENTER);
                                                setLayer(comp, map.context.indexOf(layer));

                                        }
                                    }.start();  
                            }
 
        
        
        revalidate();
        repaint();  
        
        
        
//        MapLayer layer = event.getLayer();
//        int depart = event.getFromIndex();
//        int arrivee = event.getToIndex();
//        
//        LayerComponent comp = comps.get(depart);
//        comps.remove(depart);
//        comps.add(arrivee, comp);
//        
//        removeAll();
//        for (LayerComponent com : comps) {
//            add(com, map.context.indexOf(com.layer));
//        }
//        revalidate();
//        repaint();        
    }
    
}




