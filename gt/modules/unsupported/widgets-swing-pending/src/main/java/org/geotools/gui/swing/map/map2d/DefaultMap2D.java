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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.plaf.DimensionUIResource;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.map.event.MapLayerListEvent;
import org.geotools.map.event.MapLayerListListener;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.shape.ShapefileRenderer;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

/**
 *
 * @author Johann Sorel
 */
public class DefaultMap2D extends JPanel implements Map2D {

    protected MapLayerListListener mapLayerListlistener;
    protected GTRenderer renderer;
    protected MapContext context;
    protected Envelope mapArea;
    protected MapContext buffercontext = new DefaultMapContext(DefaultGeographicCRS.WGS84);
    //protected List<BufferedImage> bufferLayer = new ArrayList<BufferedImage>();
    //protected BufferedImage bufferImage;

    private Rectangle mapRectangle;
    private Rectangle oldRect = null;
    private Envelope oldMapArea = null;
    private boolean changed = true;
    private boolean reset = true;
    private JLayeredPane layerpane = new JLayeredPane();
    protected List<LayerComponent> comps = new ArrayList<LayerComponent>();

    public DefaultMap2D() {
        this(new ShapefileRenderer());
    }

    public DefaultMap2D(GTRenderer renderer) {
        this.renderer = renderer;
        mapLayerListlistener = new MapLayerListListen(this);
        setLayout(new GridLayout(1, 1));
        add(layerpane);
    }

    protected void setChanged(boolean val) {
        changed = val;
    }

    protected void drawError(Graphics2D g, String error) {
        g.setColor(Color.RED);
        g.setStroke(new BasicStroke(3));
        if (error == null || error.equals("")) {
            error = "Error";
        }
        g.drawString(error, 20, 20);
    }

    protected Envelope fixAspectRatio(Rectangle r, Envelope mapArea) {
        double mapWidth = mapArea.getWidth(); /* get the extent of the map */
        double mapHeight = mapArea.getHeight();
        double scaleX = r.getWidth() / mapArea.getWidth(); /*
                                                             * calculate the new
                                                             * scale
                                                             */

        double scaleY = r.getHeight() / mapArea.getHeight();
        double scale = 1.0; // stupid compiler!

        if (scaleX < scaleY) { /* pick the smaller scale */
            scale = scaleX;
        } else {
            scale = scaleY;
        }

        /* calculate the difference in width and height of the new extent */
        double deltaX = /* Math.abs */ ((r.getWidth() / scale) - mapWidth);
        double deltaY = /* Math.abs */ ((r.getHeight() / scale) - mapHeight);

        /*
         * System.out.println("delta x " + deltaX); System.out.println("delta y " +
         * deltaY);
         */

        /* create the new extent */
        Coordinate ll = new Coordinate(mapArea.getMinX() - (deltaX / 2.0), mapArea.getMinY() - (deltaY / 2.0));
        Coordinate ur = new Coordinate(mapArea.getMaxX() + (deltaX / 2.0), mapArea.getMaxY() + (deltaY / 2.0));

        return new Envelope(ll, ur);
    }

    protected synchronized BufferedImage createBufferImage(MapLayer layer, Rectangle dr) {                
        buffercontext.clearLayerList();
        
         try {
            buffercontext.setCoordinateReferenceSystem(context.getCoordinateReferenceSystem());
         } catch (Exception e) {
         }
        
        buffercontext.addLayer(layer);
        BufferedImage buf = new BufferedImage(dr.width, dr.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D ig = buf.createGraphics();
        renderer.setContext(buffercontext);
        renderer.paint((Graphics2D) ig, dr, mapArea);

        return buf;
    }

    protected void redraw() {
        
        
        
        if ((renderer == null) || (mapArea == null)) {
            return;
        }

        Rectangle r = getBounds();
        mapRectangle = new Rectangle(r.width, r.height);

        if (!r.equals(oldRect) || reset) {
            if (!r.equals(oldRect)) {
                try {
                    mapArea = context.getLayerBounds();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            /* either the viewer size has changed or we've done a reset */
            changed = true; /* note we need to redraw */
            reset = false;
            oldRect = r; /* store what the current size is */
            mapArea = fixAspectRatio(r, mapArea);
        }

        if (!mapArea.equals(oldMapArea)) { /* did the map extent change? */
            changed = true;
            oldMapArea = mapArea;
            //          when we tell the context that the bounds have changed WMSLayers
            // can refresh them selves
            context.setAreaOfInterest(mapArea, context.getCoordinateReferenceSystem());
        }

        if (changed ) {
            
            changed = false;

            
            if(comps.size() != context.getLayerCount() ){
                System.out.println("REDRAW-Changed >>>>> FULL");
                
                comps.clear();
                layerpane.removeAll();

                new Thread() {
                @Override
                public void run() {

                            MapLayer[] layers = context.getLayers();
                            for (MapLayer layer : layers) {

                                LayerComponent comp = new LayerComponent();
                                comp.setSize(getWidth(), getHeight());
                                comp.img = createBufferImage(layer, mapRectangle);
                                comp.layer = layer;
                                comps.add(comp);
                                layerpane.add(comp, new Integer(context.indexOf(layer)));

                                try{ sleep(100); }catch(Exception e){}
                            }

                        }
                    }.start();
                
                
            }else{
                System.out.println("REDRAW-Changed >>>>> UPDATE");
                
                new Thread() {
                @Override
                public void run() {

                            for (LayerComponent comp : comps) {

                                comp.setSize(getWidth(), getHeight());
                                comp.img = createBufferImage(comp.layer, mapRectangle);
                                comp.repaint();

                                try{ sleep(100); }catch(Exception e){}
                            }

                        }
                    }.start();
                
                
            }
            
            



        }else{
            System.out.println("REDRAW-No-Changed");
        }

    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        redraw();
    }

    //    @Override
//    protected void paintComponent(Graphics g) {
//        super.paintComponent(g);
//
//        if ((renderer == null) || (mapArea == null)) {
//            return;
//        }
//
//        Rectangle r = getBounds();
//        Rectangle mapRectangle = new Rectangle(r.width, r.height);
//
//        if (!r.equals(oldRect) || reset) {
//            if (!r.equals(oldRect)) {
//                try {
//                    mapArea = context.getLayerBounds();
//                } catch (IOException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
//            }
//            /* either the viewer size has changed or we've done a reset */
//            changed = true; /* note we need to redraw */
//            reset = false;
//            oldRect = r; /* store what the current size is */
//            mapArea = fixAspectRatio(r, mapArea);
//        }
//
//        if (!mapArea.equals(oldMapArea)) { /* did the map extent change? */
//            changed = true;
//            oldMapArea = mapArea;
//            //          when we tell the context that the bounds have changed WMSLayers
//            // can refresh them selves
//            context.setAreaOfInterest(mapArea, context.getCoordinateReferenceSystem());
//        }
//                
//        if (changed) {
//            changed = false;
//
//            buffercontext.clearLayerList();
//            try {
//                buffercontext.setCoordinateReferenceSystem(context.getCoordinateReferenceSystem());
//            } catch (Exception e) {
//            }
//
//            bufferLayer.clear();
//            for (MapLayer layer : context.getLayers()) {
//                buffercontext.addLayer(layer);
//                BufferedImage buf = new BufferedImage(mapRectangle.width, mapRectangle.height, BufferedImage.TYPE_INT_ARGB);
//                Graphics2D ig = buf.createGraphics();
//                renderer.setContext(buffercontext);
//                renderer.paint((Graphics2D) ig, mapRectangle, mapArea);
//                bufferLayer.add(buf);
//                buffercontext.clearLayerList();
//            }
//
//            final Graphics2D g2d = (Graphics2D)g;
//            
//            for (BufferedImage buf : bufferLayer) {
//                g2d.drawImage(buf, 0, 0, this);                
//            }
//
//
//        //        bufferImage = new BufferedImage(mapRectangle.width, mapRectangle.height,BufferedImage.TYPE_INT_ARGB);
////        Graphics2D graph = bufferImage.createGraphics();
////        
////        for(BufferedImage buf : bufferLayer){
////            graph.drawImage(buf, 0, 0, this);
////        }
////            
////        ((Graphics2D) g).drawImage(bufferImage, 0, 0, this);
//
//
//        //            bufferImage = new BufferedImage(mapRectangle.width, mapRectangle.height,
////                    BufferedImage.TYPE_INT_ARGB);
////
////            Graphics2D ig = bufferImage.createGraphics();
////            renderer.setContext(context);
////            
////            try{
////                renderer.paint((Graphics2D) ig, mapRectangle, mapArea);
////            }catch(Exception e){
////                drawError((Graphics2D) ig, e.getMessage());
////            }
//
//        }
//
//
//
//
//    //((Graphics2D) g).drawImage(bufferImage, 0, 0, this);
//
//    }

    void fireDelete(MapLayerListEvent event) {
        System.out.println("DELETE");
        int index = event.getFromIndex();
        LayerComponent comp = comps.get(index);
        layerpane.remove(comp);
        comps.remove(index);

        int size = comps.size();
        for (int i = index; i < size; i++) {
            layerpane.moveToFront(comps.get(index));
        }
        //layerpane.repaint();
    }

    void fireChange(MapLayerListEvent event) {
        System.out.println("CHANGE");
        int index = event.getFromIndex();
        LayerComponent comp = comps.get(index);
        comp.img = createBufferImage(event.getLayer(), mapRectangle);
        comp.repaint();
    }

    void fireAdd(MapLayerListEvent event){
        System.out.println("ADD");
        MapLayer layer = event.getLayer();
        int index = context.indexOf(layer);
        
        int size = comps.size();
        for (int i = index; i < size; i++) {
            layerpane.moveToBack(comps.get(index));
        }
        
        LayerComponent comp = new LayerComponent();
        comp.setSize(getWidth(), getHeight());
        comp.img = createBufferImage(layer, mapRectangle);
        comp.layer = layer;
        comps.add(index,comp);
        layerpane.add(comp, new Integer(index));  
        layerpane.revalidate();
        layerpane.repaint();
    }
    
    void fireMove(MapLayerListEvent event){
        System.out.println("MOVE");
        
        MapLayer layer = event.getLayer();
        int depart = event.getFromIndex();
        int arrivee = event.getToIndex();
        
        LayerComponent comp = comps.get(depart);
        comps.remove(depart);
        comps.add(arrivee, comp);
        
        layerpane.removeAll();
        for (LayerComponent com : comps) {
            layerpane.add(com, context.indexOf(com.layer));
        }
        layerpane.revalidate();
        layerpane.repaint();
        
    }
    
    //-----------------------MAP2D----------------------------------------------    

    public void setContext(MapContext context) {
        if (this.context != null) {
            this.context.removeMapLayerListListener(mapLayerListlistener);
        }

        this.context = context;

        if (context != null) {
            this.context.addMapLayerListListener(mapLayerListlistener);
        }

        if (renderer != null) {
            renderer.setContext(this.context);
        }

        refresh();
    }

    public MapContext getContext() {
        return context;
    }

    public void setRenderer(GTRenderer renderer) {
        this.renderer = renderer;
    }

    public GTRenderer getRenderer() {
        return renderer;
    }

    public void setMapArea(Envelope mapArea) {
        this.mapArea = mapArea;
    }

    public Envelope getMapArea() {
        return mapArea;
    }

    public void setCoordinateReferenceSystem(CoordinateReferenceSystem crs) {
        if (context != null) {
            try {
                context.setCoordinateReferenceSystem(crs);
            } catch (TransformException ex) {
                ex.printStackTrace();
            } catch (FactoryException ex) {
                ex.printStackTrace();
            }
        }
    }

    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        if (context != null) {
            return context.getCoordinateReferenceSystem();
        } else {
            return null;
        }
    }

    public void refresh() {
        reset = true;
        //repaint();
        redraw();
    }

    public JPanel getComponent() {
        return this;
    }
}


////////////////////////////////////////////////////////////////////////////////
//---------------------MapLayerListListen-------------------------------------//
////////////////////////////////////////////////////////////////////////////////

class MapLayerListListen implements MapLayerListListener {

    private final DefaultMap2D map;

    MapLayerListListen(DefaultMap2D map) {
        this.map = map;
    }

    public void layerAdded(MapLayerListEvent event) {
        
        MapContext context = map.getContext();

        if (context.getLayers().length == 1) {
            try {
                map.setMapArea(context.getLayerBounds());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        map.fireAdd(event);
    }

    public void layerRemoved(MapLayerListEvent event) {
        map.fireDelete(event);
    }

    public void layerChanged(MapLayerListEvent event) {
        map.fireChange(event);
    }

    public void layerMoved(MapLayerListEvent event) {
        map.fireMove(event);
//        map.setChanged(true);
//        map.refresh();
    }
}



