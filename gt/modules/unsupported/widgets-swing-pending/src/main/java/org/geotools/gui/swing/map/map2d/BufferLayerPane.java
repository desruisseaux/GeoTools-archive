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
import org.geotools.map.MapLayer;

/**
 * 
 * @author Johann Sorel
 */
class BufferLayerPane extends JLayeredPane{
   
    private int i = 0;
    private List<MapLayer>layers = new ArrayList<MapLayer>();
    private List<LayerComponent> comps = new ArrayList<LayerComponent>();
        
    BufferLayerPane(){
        setLayout(new BorderLayout());
    }
    
    void removeComp(int index){
        System.out.println("DELETE");
        remove(comps.get(index));
        comps.remove(index);
        layers.remove(index);        
        
        int size = comps.size();
        for( i=index ; i<size; i++){
            moveToFront(comps.get(i));
        }
    }
    
    void removeComp(MapLayer layer){
        System.out.println("DELETE");
        int index = layers.indexOf(layer);
        this.removeComp(index);
    }
    
    void addComp(MapLayer layer, BufferedImage img, int index){
        System.out.println("ADD");
        
        int size = comps.size();
        for (i = index; i < size; i++) {
            moveToBack(comps.get(index));
        }
        
        LayerComponent comp = new LayerComponent();
        comp.img = img;
        comps.add(index,comp);
        layers.add(index,layer);
        add(comp, BorderLayout.CENTER);  
        setLayer(comp, index);
        revalidate();
        repaint();
    }
    
    void changeComp(int index,BufferedImage img) {
        System.out.println("CHANGE");
        LayerComponent comp = comps.get(index);
        comp.img = img;
        comp.repaint();
    }
    
    void fireMove(int depart, int arrivee){
        System.out.println("MOVE");
        
        LayerComponent comp = comps.get(depart);
        comps.remove(depart);
        comps.add(arrivee, comp);
        
        removeAll();
        for (LayerComponent com : comps) {
            //add(com, context.indexOf(com.layer));
        }
        revalidate();
        repaint();        
    }
    
    
}




