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
import java.awt.BorderLayout;
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
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.plaf.DimensionUIResource;
import org.geotools.gui.swing.map.map2d.decolayer.RedrawingPanel;
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
public class DefaultMap2D extends JPanel implements Map2D,Observer {

    protected MapLayerListListener mapLayerListlistener;
    protected GTRenderer renderer;
    protected MapContext context;
    protected Envelope mapArea;
    protected MapContext buffercontext = new OneLayerContext();

    private Rectangle mapRectangle;
    private Rectangle oldRect = null;
    private Envelope oldMapArea = null;
    private boolean changed = true;
    private boolean reset = true;
    protected JLayeredPane layerPane = new JLayeredPane();
    protected BufferPane bufferPane = new BufferPane(this);    
    protected RedrawingPanel waitingPane = new RedrawingPanel();

    public DefaultMap2D() {
        this(new ShapefileRenderer());
    }

    public DefaultMap2D(GTRenderer renderer) {
        this.renderer = renderer;
        mapLayerListlistener = new MapLayerListListen(this);
        setLayout(new GridLayout(1, 1));
        layerPane.setLayout(new BufferLayout());
        layerPane.add(bufferPane,new Integer(11));
        layerPane.add(waitingPane,new Integer(12));
        bufferPane.STATE.addObserver(this);
        add(layerPane);
    }

    protected void setChanged(boolean val) {
        changed = val;
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

    protected synchronized BufferedImage createBufferImage(MapLayer layer) {                
        buffercontext.clearLayerList();
        
         try {
            buffercontext.setCoordinateReferenceSystem(context.getCoordinateReferenceSystem());
         } catch (Exception e) {
         }
        
        buffercontext.addLayer(layer);
        BufferedImage buf = new BufferedImage(mapRectangle.width, mapRectangle.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D ig = buf.createGraphics();
        renderer.setContext(buffercontext);
        renderer.paint((Graphics2D) ig, mapRectangle, mapArea);

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
            
            if(bufferPane.getBufferSize() != context.getLayerCount() ){  
                bufferPane.fit();            
            }else{          
                bufferPane.update();                
            }            
        }
        
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        redraw();
    }

    
    void fireDelete(MapLayerListEvent event) {
        bufferPane.deleted(event);
    }

    void fireChange(MapLayerListEvent event) {
        bufferPane.changed(event);
    }

    void fireAdd(MapLayerListEvent event){
        bufferPane.added(event);
    }
    
    void fireMove(MapLayerListEvent event){
        bufferPane.moved(event);      
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
        redraw();
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
        repaint();
    }

    public JPanel getComponent() {
        return this;
    }

    public void update(Observable o, Object arg) {
        int val = (Integer)arg;
        if(val > 0){
            waitingPane.setDrawing(true);
            waitingPane.repaint();
        }
        else{
            waitingPane.setDrawing(false);
            waitingPane.repaint();
        }
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
    }
}



