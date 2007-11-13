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
import java.util.Observable;
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
public class BufferPane extends JLayeredPane {

    final State STATE = new State();
    
    private Map<MapLayer,BufferComponent> stock = new HashMap<MapLayer,BufferComponent>();
    private DefaultMap2D map;
    
    BufferPane(DefaultMap2D map){
        this.map = map;
        setLayout(new BufferLayout());
    }
    
    boolean isDrawing(){
        if(STATE.getNB()>0){
            return true;
        }else{
            return false;
        }
    }
    
    
    int getBufferSize(){
        return getComponentCount();
    }
    
       
    void fit(){
        int contextsize = map.context.getLayerCount();
        
        stock.clear();
        removeAll();
        for(int i=0;i<contextsize;i++){
            final int val = i;
            STATE.raiseNB();
            new Thread(){
                public void run(){
                    MapLayer layer = map.context.getLayer(val);
                    BufferedImage buffer = map.createBufferImage(layer);            
                    BufferComponent comp = new BufferComponent();
                    comp.setBuffer( buffer);
                    stock.put(layer, comp);            
                    add(comp,new Integer(val)); 
                    STATE.lowerNB();
                }
            }.start();
        }
        
        revalidate();
        repaint();
    }
    
    void update(){
        Set<MapLayer> keys = stock.keySet();
        
        for(final MapLayer key : keys){
            STATE.raiseNB();
            new Thread(){
                public void run(){
                    stock.get(key).setBuffer(map.createBufferImage(key));
                    STATE.lowerNB();
                }
            }.start();
            
        }
    }
    
    
    void changed(MapLayerListEvent event){
        MapLayer layer = event.getLayer();
        BufferedImage buffer = map.createBufferImage(layer);        
        stock.get(layer).setBuffer(buffer);        
    }
    
    void deleted(MapLayerListEvent event){
        MapLayer layer = event.getLayer();
        MapContext context = map.context;
        BufferComponent comp = stock.remove(layer);
        
        int index = event.getFromIndex();
        int size = context.getLayerCount();
        
        remove(comp);  
        for(int i=index ; i<size; i++){
            BufferComponent moving = stock.get(context.getLayer(i));
            int position = getLayer(moving);
            setLayer(moving, position-1);
        }
        
        
    }
    
    void added(MapLayerListEvent event){
        MapLayer layer = event.getLayer();
        MapContext context = map.context;
        int index = event.getToIndex();
        
        BufferComponent comp = new BufferComponent();
        comp.setBuffer(map.createBufferImage(layer));
        
        int size = context.getLayerCount();
        for(int i=index+1 ; i<size; i++){
            BufferComponent moving = stock.get(context.getLayer(i));
            int position = getLayer(moving);
            setLayer(moving, position+1);
        }
        
        stock.put(layer, comp);
        add(comp,new Integer(index));
        
        revalidate();         
        repaint();
    }
    
    void moved(MapLayerListEvent event){
        MapContext context = map.context;
        int to = event.getToIndex();
        int from = event.getFromIndex();
        int min = (from < to) ? from : to ;
                
        int size = context.getLayerCount();
        for(int i=min ; i<size; i++){
            BufferComponent moving = stock.get(context.getLayer(i));
            setLayer(moving, i);
        }
        
        revalidate();         
        repaint();
    }
    
    
}


class BufferComponent extends JComponent{

    private BufferedImage img;
        
    public void setBuffer(BufferedImage buf){
        img = buf;
        repaint();        
    }
    
    
    @Override
    protected void paintComponent(Graphics g) {
        if(img!=null){
            g.drawImage(img, 0, 0, this);
        }
    }
    
    
}

class State extends Observable{
        
        private int nb = 0;
        
        public int getNB(){
            return nb;
        }
        
        public void raiseNB(){
           nb++;
           if(nb == 1){
               setChanged();
               notifyObservers(1);
           }
           
        }
        
        public void lowerNB(){
            nb--;
            if(nb==0){
                setChanged();
                notifyObservers(0);
            }
        }
        
                
    };

