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
import javax.swing.event.EventListenerList;
import org.geotools.gui.swing.map.map2d.event.Map2DContextEvent;
import org.geotools.gui.swing.map.map2d.listener.Map2DListener;
import org.geotools.gui.swing.map.map2d.event.Map2DMapAreaEvent;
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

    public static enum BUFFER_TYPE {

        SINGLE_BUFFER,
        MULTI_BUFFER,
        MERGE_BUFFER,
        SINGLE_VOLATILE
    }
    protected GraphicsConfiguration GC;
    protected final EventListenerList MAP2DLISTENERS = new EventListenerList();
    protected final Map2D THIS_MAP;
    protected int NEXT_OVER_LAYER_INDEX = 12;
    protected GTRenderer renderer;
    protected MapContext context;
    protected Envelope mapArea;
    protected JLayeredPane layerPane = new JLayeredPane();
    protected RenderingStrategy bufferPane = new MultiBufferedImageStrategy(this);
    protected WaitingOverLayer waitingPane = new WaitingOverLayer();
    private final MapLayerListListener mapLayerListlistener;
    private final MapContext buffercontext = new OneLayerContext();
    private BUFFER_TYPE type = null;
    protected Rectangle mapRectangle = new Rectangle(1, 1);
    private Rectangle oldRect = null;
    private Envelope oldMapArea = null;

    public DefaultMap2D() {
        this(new ShapefileRenderer());
    }

    public DefaultMap2D(GTRenderer renderer) {
        this.renderer = renderer;
        this.THIS_MAP = this;
        mapLayerListlistener = new MapLayerListListen();
        setLayout(new GridLayout(1, 1));
        layerPane.setLayout(new BufferLayout());

        setMapBufferType(BUFFER_TYPE.SINGLE_VOLATILE);

        layerPane.add(waitingPane, new Integer(NEXT_OVER_LAYER_INDEX));
        NEXT_OVER_LAYER_INDEX++;
        add(layerPane);

        GC = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();


//        GraphicsEnvironment gEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
//        GraphicsDevice[] devices = gEnv.getScreenDevices();
//
//        
//        if (devices.length>0) {
//            GC = devices[0].getDefaultConfiguration();
//            }
//        

    //opimizeRenderer();
    }

    private void opimizeRenderer() {

        Map rendererParams = new HashMap();
        rendererParams.put("optimizedDataLoadingEnabled", new Boolean(true));
        rendererParams.put("maxFiltersToSendToDatastore", new Integer(20));
        rendererParams.put(ShapefileRenderer.SCALE_COMPUTATION_METHOD_KEY, ShapefileRenderer.SCALE_OGC);
        renderer.setRendererHints(rendererParams);

        RenderingHints rh;
        rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        renderer.setJava2DHints(rh);
        rh = new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        renderer.setJava2DHints(rh);
        rh = new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        renderer.setJava2DHints(rh);
        rh = new RenderingHints(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
        renderer.setJava2DHints(rh);
        rh = new RenderingHints(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        renderer.setJava2DHints(rh);
        rh = new RenderingHints(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
        renderer.setJava2DHints(rh);


//if(){
//rendererParams.put(ShapefileRenderer.TEXT_RENDERING_KEY, ShapefileRenderer.TEXT_RENDERING_STRING);
//} else {
// rendererParams.put(ShapefileRenderer.TEXT_RENDERING_KEY, ShapefileRenderer.TEXT_RENDERING_OUTLINE);
//    }

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

        if (context != null && mapArea != null && mapRectangle.width > 0 && mapRectangle.height > 0) {
            try {
                buffercontext.setCoordinateReferenceSystem(context.getCoordinateReferenceSystem());
            } catch (Exception e) {
            }

            buffercontext.addLayer(layer);
            renderer.setContext(buffercontext);

            //NOT OPTIMIZED
//            BufferedImage buf = new BufferedImage(mapRectangle.width, mapRectangle.height, BufferedImage.TYPE_INT_ARGB);
//            Graphics2D ig = buf.createGraphics();

            //GC ACCELERATION        
            BufferedImage buf = GC.createCompatibleImage(mapRectangle.width, mapRectangle.height, BufferedImage.TRANSLUCENT);
            Graphics2D ig = buf.createGraphics();

            renderer.paint((Graphics2D) ig, mapRectangle, mapArea);
            return buf;
        } else {
            return null;
        }

    }

    

    protected synchronized BufferedImage createBufferImage(MapContext context) {

        if (context != null && mapArea != null && mapRectangle.width > 0 && mapRectangle.height > 0) {
            BufferedImage buf = new BufferedImage(mapRectangle.width, mapRectangle.height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D ig = buf.createGraphics();
            renderer.setContext(context);
            renderer.paint((Graphics2D) ig, mapRectangle, mapArea);
            return buf;
        } else {
            return null;
        }

    }

    private void redraw(boolean complete) {
        boolean changed = false;

        if (renderer != null && mapArea != null) {

            Rectangle newRect = getBounds();
            mapRectangle = new Rectangle(newRect.width, newRect.height);

            if (!newRect.equals(oldRect)) {
                changed = true;
                oldRect = newRect;
                mapArea = fixAspectRatio(newRect, mapArea);
            }

            if (!(mapArea.equals(oldMapArea)) && !(Double.isNaN(mapArea.getMinX()))) {
                changed = true;
                oldMapArea = mapArea;
                context.setAreaOfInterest(mapArea, context.getCoordinateReferenceSystem());
            }

            if (changed || complete) {
                changed = false;
                //System.out.println("la2");
                bufferPane.redraw(complete);
            }
        }
    }

    public void setMapBufferType(BUFFER_TYPE type) {
        if (this.type != type) {
            this.type = type;
            bufferPane.deleteObserver(this);
            layerPane.remove(bufferPane.getComponent());

            switch (type) {
                case SINGLE_BUFFER:
                    bufferPane = new SingleBufferedImageStrategy(this);
                    break;
                case MULTI_BUFFER:
                    bufferPane = new MultiBufferedImageStrategy(this);
                    break;
                case MERGE_BUFFER:
                    bufferPane = new MergeBufferedImageStrategy(this);
                    break;
                case SINGLE_VOLATILE:
                    bufferPane = new SingleVolatileImageStrategy(this);
                    break;
            }

            bufferPane.addObserver(this);
            bufferPane.redraw(false);

            layerPane.add(bufferPane.getComponent(), new Integer(11));
        }
    }

    public BUFFER_TYPE getBufferType() {
        return type;
    }

    @Override
    public void paintComponent(Graphics g) {
        redraw(false);
        super.paintComponent(g);
    }

    private void fireDelete(MapLayerListEvent event) {
        bufferPane.layerDeleted(event);
    }

    private void fireChange(MapLayerListEvent event) {
        bufferPane.layerChanged(event);
    }

    private void fireAdd(MapLayerListEvent event) {
        bufferPane.layerAdded(event);
    }

    private void fireMove(MapLayerListEvent event) {
        bufferPane.layerMoved(event);
    }

    private void fireMapAreaChanged(Envelope oldone, Envelope newone) {
        Map2DMapAreaEvent mce = new Map2DMapAreaEvent(this, oldone, newone);

        Map2DListener[] lst = getMap2DListeners();

        for (Map2DListener l : lst) {
            l.mapAreaChanged(mce);
        }

    }

    private void fireMapContextChanged(MapContext oldcontext, MapContext newContext) {
        Map2DContextEvent mce = new Map2DContextEvent(this, oldcontext, newContext);

        Map2DListener[] lst = getMap2DListeners();

        for (Map2DListener l : lst) {
            l.mapContextChanged(mce);
        }

    }

    //-----------------------MAP2D----------------------------------------------    
    public void setContext(MapContext context) {
        if (this.context != null) {
            this.context.removeMapLayerListListener(mapLayerListlistener);
        }

        fireMapContextChanged(this.context, context);
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

        if (mapArea != null) {
            Rectangle r = getBounds();
            Envelope newArea = fixAspectRatio(r, mapArea);

            fireMapAreaChanged(this.mapArea, newArea);
            this.mapArea = newArea;
            redraw(false);
        }
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

    public void addMap2DListener(Map2DListener listener) {
        MAP2DLISTENERS.add(Map2DListener.class, listener);
    }

    public void removeMap2DListener(Map2DListener listener) {
        MAP2DLISTENERS.remove(Map2DListener.class, listener);
    }

    public Map2DListener[] getMap2DListeners() {
        return MAP2DLISTENERS.getListeners(Map2DListener.class);
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

