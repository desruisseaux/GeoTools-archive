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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.geotools.gui.swing.map.map2d.overLayer.WaitingOverLayer;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.map.event.MapLayerListEvent;
import org.geotools.map.event.MapLayerListListener;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.shape.ShapefileRenderer;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

/**
 *
 * @author Johann Sorel
 */
public class DefaultMap2D extends JPanel implements Map2D, Observer {

//    protected GraphicsConfiguration GC ;
    
    protected final Map2D THIS_MAP;
    protected int NEXT_OVER_LAYER_INDEX = 12;
    private final MapLayerListListener mapLayerListlistener;
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
    protected WaitingOverLayer waitingPane = new WaitingOverLayer();

    public DefaultMap2D() {
        this(new ShapefileRenderer());
    }

    public DefaultMap2D(GTRenderer renderer) {
        this.renderer = renderer;
        this.THIS_MAP = this;
        mapLayerListlistener = new MapLayerListListen();
        setLayout(new GridLayout(1, 1));
        layerPane.setLayout(new BufferLayout());
        layerPane.add(bufferPane, new Integer(11));
        layerPane.add(waitingPane, new Integer(NEXT_OVER_LAYER_INDEX));
        NEXT_OVER_LAYER_INDEX++;
        bufferPane.STATE.addObserver(this);
        add(layerPane);
        
//        GraphicsEnvironment gEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
//        GraphicsDevice[] devices = gEnv.getScreenDevices();
//
//        
//        if (devices.length>0) {
//            GC = devices[0].getDefaultConfiguration();
//            }
//        
        
        opimizeRenderer();
    }

    private void opimizeRenderer() {
        
        
        
        
        Map rendererParams = new HashMap();
        rendererParams.put("optimizedDataLoadingEnabled", new Boolean(true));
        rendererParams.put("maxFiltersToSendToDatastore", new Integer(20));
        rendererParams.put(ShapefileRenderer.SCALE_COMPUTATION_METHOD_KEY, ShapefileRenderer.SCALE_OGC);
        RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);
        renderer.setJava2DHints(rh);
        renderer.setRendererHints(rendererParams);

//if(){
//rendererParams.put(ShapefileRenderer.TEXT_RENDERING_KEY, ShapefileRenderer.TEXT_RENDERING_STRING);
//} else {
// rendererParams.put(ShapefileRenderer.TEXT_RENDERING_KEY, ShapefileRenderer.TEXT_RENDERING_OUTLINE);
//    }

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

    protected BufferedImage createBufferImage(MapLayer layer) {


        buffercontext.clearLayerList();

        try {
            buffercontext.setCoordinateReferenceSystem(context.getCoordinateReferenceSystem());
        } catch (Exception e) {
        }

        buffercontext.addLayer(layer);
        renderer.setContext(buffercontext);
        
        //NOT OPTIMIZED
        BufferedImage buf = new BufferedImage(mapRectangle.width, mapRectangle.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D ig = buf.createGraphics();
        
        //GC ACCELERATION        
//        BufferedImage buf = GC.createCompatibleImage(mapRectangle.width, mapRectangle.height,BufferedImage.TYPE_INT_ARGB);
//        Graphics2D ig = buf.createGraphics();
                
        renderer.paint((Graphics2D) ig, mapRectangle, mapArea);

        return buf;
    }

    protected BufferedImage createBufferImage(MapContext context) {
        BufferedImage buf = new BufferedImage(mapRectangle.width, mapRectangle.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D ig = buf.createGraphics();
        renderer.setContext(context);
        renderer.paint((Graphics2D) ig, mapRectangle, mapArea);
        return buf;
    }

    protected void redraw(boolean withRepaint) {

        if ((renderer == null) || (mapArea == null)) {
            return;
        }

        Rectangle r = getBounds();
        mapRectangle = new Rectangle(r.width, r.height);

        if (!r.equals(oldRect) || reset) {
            if (reset) {
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

            if (mapArea == null) {
                bufferPane.fit();
                return;
            }

            mapArea = fixAspectRatio(r, mapArea);
        }

        if (!mapArea.equals(oldMapArea)) { /* did the map extent change? */
            changed = true;
            oldMapArea = mapArea;
            //          when we tell the context that the bounds have changed WMSLayers
            // can refresh them selves
            context.setAreaOfInterest(mapArea, context.getCoordinateReferenceSystem());
        }

        if (changed) {
            changed = false;

            if (bufferPane.getBufferSize() != context.getLayerCount()) {
                bufferPane.fit();
            } else {
                bufferPane.update();
            }
        }

        if (withRepaint) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    revalidate();
                    repaint();
                }
            });
        }


    }

    @Override
    public void paintComponent(Graphics g) {
        redraw(false);
        super.paintComponent(g);
    }

    void fireDelete(MapLayerListEvent event) {
        bufferPane.deleted(event);
    }

    void fireChange(MapLayerListEvent event) {
        bufferPane.changed(event);
    }

    void fireAdd(MapLayerListEvent event) {
        bufferPane.added(event);
    }

    void fireMove(MapLayerListEvent event) {
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
        redraw(true);
    }

    public JPanel getComponent() {
        return this;
    }

    public void update(Observable o, Object arg) {
        int val = (Integer) arg;
        if (val > 0) {
            waitingPane.setDrawing(true);
        } else {
            waitingPane.setDrawing(false);
        }
    }

    //---------------------- PRIVATE CLASSES------------------------------------    
    private class MapLayerListListen implements MapLayerListListener {

        public void layerAdded(MapLayerListEvent event) {

            if (context.getLayers().length == 1) {
                try {
                    setMapArea(context.getLayerBounds());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            fireAdd(event);
        }

        public void layerRemoved(MapLayerListEvent event) {
            fireDelete(event);
        }

        public void layerChanged(MapLayerListEvent event) {
            fireChange(event);
        }

        public void layerMoved(MapLayerListEvent event) {
            fireMove(event);
        }
    }
}

