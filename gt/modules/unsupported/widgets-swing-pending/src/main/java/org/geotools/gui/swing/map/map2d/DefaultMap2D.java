/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JPanel;
import org.geotools.feature.FeatureCollection;
import org.geotools.filter.IllegalFilterException;
import org.geotools.gui.swing.map.MapConstants;
import org.geotools.gui.swing.map.MapConstants.MAP_TYPE;
import org.geotools.gui.swing.map.map2d.event.HighlightChangeListener;
import org.geotools.gui.swing.map.map2d.event.SelectionChangeListener;
import org.geotools.gui.swing.map.map2d.event.SelectionChangedEvent;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.map.event.MapLayerListEvent;
import org.geotools.map.event.MapLayerListListener;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.lite.LabelCache;
import org.geotools.renderer.lite.LabelCacheDefault;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.Graphic;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.Mark;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.StyleFactory;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;



////////////////////////////////////////////////////////////////////////////////
//---------------------MouseListen--------------------------------------------//
////////////////////////////////////////////////////////////////////////////////

/**
 *
 * @author Johann Sorel, Ian Turton
 */
public class DefaultMap2D extends JPanel implements Map2D{

    MapConstants.MAP_TYPE TYPE = MapConstants.MAP_TYPE.MAP_2D;
    
    final MouseListen MOUSE_LISTEN;
    final MapLayerListListen MAPLAYERLIST_LISTEN;  
    final HighlightChangeListen HIGHLIGHT_LISTEN;
    final SelectionChangeListener SELECTION_LISTEN;
        
    GTRenderer renderer;
    GTRenderer highlightRenderer;
    GTRenderer selectionRenderer;
    MapContext context;
    Envelope mapArea;
        
    LabelCache labelCache = new LabelCacheDefault();
    MapConstants.MAP_STATE state = MapConstants.MAP_STATE.PAN;
    double zoomFactor = 2.0;
    boolean canClick = true;
    boolean reset = true;
    FilterFactory2 ff;
    GeometryFactory gf = new GeometryFactory();
    
    
    public DefaultMap2D() {
        this(null, true, null, null);
    }

    /**
     * create a basic JMapPane
     *
     * @param render -
     *            how to draw the map
     * @param context -
     *            the map context to display
     */
    public DefaultMap2D(GTRenderer render, MapContext context) {
        this(null, true, render, context);
    }

    /**
     * full constructor extending JPanel
     *
     * @param layout -
     *            layout (probably shouldn't be set)
     * @param isDoubleBuffered -
     *            a Swing thing I don't really understand
     * @param render -
     *            what to draw the map with
     * @param context -
     *            what to draw
     */
    public DefaultMap2D(LayoutManager layout, boolean isDoubleBuffered,GTRenderer render, MapContext context) {
        super(layout, isDoubleBuffered);

        MOUSE_LISTEN = new MouseListen(this);
        MAPLAYERLIST_LISTEN = new MapLayerListListen(this);
        HIGHLIGHT_LISTEN = new HighlightChangeListen(this);
        SELECTION_LISTEN = new SelectionChangeListen(this);
        setSelectionManager(new DefaultSelectionManager(selectionLayer));
        setHighlightManager(new DefaultHighlightManager(highlightLayer));
        
        this.addMouseListener(MOUSE_LISTEN);
        this.addMouseMotionListener(MOUSE_LISTEN);
        
        ff = (FilterFactory2) org.geotools.factory.CommonFactoryFinder.getFilterFactory(null);
        setRenderer(render);        
        setContext(context);
        
        highlightSD = new StyleSetDescriptor();
        highlightSD.lineStyle = setupStyle(MapConstants.MAP_GEOMETRIE.LINE, Color.red);
        highlightSD.pointStyle = setupStyle(MapConstants.MAP_GEOMETRIE.POINT, Color.red);
        highlightSD.polygonStyle = setupStyle(MapConstants.MAP_GEOMETRIE.POLYGON, Color.red);
        highlightSD.polygonStyle = null;        
        selectionSD = new StyleSetDescriptor();
        selectionSD.lineStyle = setupStyle(MapConstants.MAP_GEOMETRIE.LINE, Color.cyan);
        selectionSD.pointStyle = setupStyle(MapConstants.MAP_GEOMETRIE.POINT, Color.cyan);
        selectionSD.polygonStyle = setupStyle(MapConstants.MAP_GEOMETRIE.POLYGON, Color.cyan);
        selectionSD.polygonStyle = null;          
        
        setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
    }
    
    
    
    private void findFeature(Geometry geometry, MapLayer layer) throws IndexOutOfBoundsException {
        org.opengis.filter.spatial.BinarySpatialOperator f = null;

        if ((context == null) || (layer==null)) {
            return ;
        }

        try {
            String name = layer.getFeatureSource().getSchema()
                    .getDefaultGeometry().getLocalName();

            if (name == "") {
                name = "the_geom";
            }

            try {
                f = ff.contains(ff.property(name), ff.literal(geometry));
                if(selectionManager!=null) {
                    System.out.println("selection changed");
                    selectionManager.fireSelectionChanged(this, f);

                }
            } catch (IllegalFilterException e) {
                e.printStackTrace();
            }

        } catch (IllegalFilterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ;
    }
    
    private Style setupStyle(MapConstants.MAP_GEOMETRIE geom, Color color) {
        StyleFactory sf = org.geotools.factory.CommonFactoryFinder.getStyleFactory(null);
        StyleBuilder sb = new StyleBuilder();

        org.geotools.styling.Style s = sf.createStyle();
        s.setTitle("selection");

        // TODO parameterise the color
        PolygonSymbolizer ps = sb.createPolygonSymbolizer(color);
        ps.setStroke(sb.createStroke(color));

        LineSymbolizer ls = sb.createLineSymbolizer(color);
        Graphic h = sb.createGraphic();
        h.setMarks(new Mark[] { sb.createMark("square", color) });

        PointSymbolizer pts = sb.createPointSymbolizer(h);

        // Rule r = sb.createRule(new Symbolizer[]{ps,ls,pts});
        switch (geom) {
        case POLYGON :
            s = sb.createStyle(ps);
            break;
        case POINT:
            s = sb.createStyle(pts);
            break;
        case LINE:
            s = sb.createStyle(ls);
        }

        return s;
    }

    
    private Envelope fixAspectRatio(Rectangle r, Envelope mapArea) {
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
        double deltaX = /* Math.abs */((r.getWidth() / scale) - mapWidth);
        double deltaY = /* Math.abs */((r.getHeight() / scale) - mapHeight);

        /*
         * System.out.println("delta x " + deltaX); System.out.println("delta y " +
         * deltaY);
         */

        /* create the new extent */
        Coordinate ll = new Coordinate(mapArea.getMinX() - (deltaX / 2.0),  mapArea.getMinY() - (deltaY / 2.0));
        Coordinate ur = new Coordinate(mapArea.getMaxX() + (deltaX / 2.0),  mapArea.getMaxY() + (deltaY / 2.0));

        return new Envelope(ll, ur);
    }
    
    void processDrag(int x1, int y1, int x2, int y2) {
        // System.out.println("processing drag from " + x1 + "," + y1 + " -> "
        // + x2 + "," + y2);
        if ((x1 == x2) && (y1 == y2)) {
            if (isClickable()) {
                MOUSE_LISTEN.mouseClicked(new MouseEvent(this, 0, new Date().getTime(), 0,x1, y1, y2, false));
            }
            return;
        }

        Rectangle bounds = this.getBounds();

        double mapWidth = mapArea.getWidth();
        double mapHeight = mapArea.getHeight();

        double startX = ((x1 * mapWidth) / (double) bounds.width)
                + mapArea.getMinX();
        double startY = (((bounds.getHeight() - y1) * mapHeight) / (double) bounds.height)
                + mapArea.getMinY();
        double endX = ((x2 * mapWidth) / (double) bounds.width)
                + mapArea.getMinX();
        double endY = (((bounds.getHeight() - y2) * mapHeight) / (double) bounds.height)
                + mapArea.getMinY();

        if (state == MapConstants.MAP_STATE.PAN) {
            // move the image with the mouse
            // calculate X offsets from start point to the end Point
            double deltaX1 = endX - startX;

            // System.out.println("deltaX " + deltaX1);
            // new edges
            double left = mapArea.getMinX() - deltaX1;
            double right = mapArea.getMaxX() - deltaX1;

            // now for Y
            double deltaY1 = endY - startY;

            // System.out.println("deltaY " + deltaY1);
            double bottom = mapArea.getMinY() - deltaY1;
            double top = mapArea.getMaxY() - deltaY1;
            Coordinate ll = new Coordinate(left, bottom);
            Coordinate ur = new Coordinate(right, top);

            mapArea = fixAspectRatio(this.getBounds(), new Envelope(ll, ur));
        } else if (state == MapConstants.MAP_STATE.ZOOM_IN) {
            // make the dragged rectangle (in map coords) the new BBOX
            double left = Math.min(startX, endX);
            double right = Math.max(startX, endX);
            double bottom = Math.min(startY, endY);
            double top = Math.max(startY, endY);
            Coordinate ll = new Coordinate(left, bottom);
            Coordinate ur = new Coordinate(right, top);

            mapArea = fixAspectRatio(this.getBounds(), new Envelope(ll, ur));
        } else if (state == MapConstants.MAP_STATE.ZOOM_OUT) {
            // make the dragged rectangle in screen coords the new map size?
            double left = Math.min(startX, endX);
            double right = Math.max(startX, endX);
            double bottom = Math.min(startY, endY);
            double top = Math.max(startY, endY);
            double nWidth = (mapWidth * mapWidth) / (right - left);
            double nHeight = (mapHeight * mapHeight) / (top - bottom);
            double deltaX1 = left - mapArea.getMinX();
            double nDeltaX1 = (deltaX1 * nWidth) / mapWidth;
            double deltaY1 = bottom - mapArea.getMinY();
            double nDeltaY1 = (deltaY1 * nHeight) / mapHeight;
            Coordinate ll = new Coordinate(mapArea.getMinX() - nDeltaX1,mapArea.getMinY() - nDeltaY1);
            double deltaX2 = mapArea.getMaxX() - right;
            double nDeltaX2 = (deltaX2 * nWidth) / mapWidth;
            double deltaY2 = mapArea.getMaxY() - top;
            double nDeltaY2 = (deltaY2 * nHeight) / mapHeight;
            Coordinate ur = new Coordinate(mapArea.getMaxX() + nDeltaX2,mapArea.getMaxY() + nDeltaY2);
            mapArea = fixAspectRatio(this.getBounds(), new Envelope(ll, ur));
        } else if (state == MapConstants.MAP_STATE.SELECT && selectionLayer !=null) {
            double left = Math.min(startX, endX);
            double right = Math.max(startX, endX);
            double bottom = Math.min(startY, endY);
            double top = Math.max(startY, endY);


            String name = selectionLayer.getFeatureSource().getSchema().getDefaultGeometry().getLocalName();

            if (name == "") {
                name = "the_geom";
            }
            Filter bb = ff.bbox(ff.property(name), left, bottom, right, top,
                    getContext().getCoordinateReferenceSystem().toString());
            //System.out.println(bb.toString());
            if(selectionManager!=null) {
                selectionManager.fireSelectionChanged(this, bb);
            }
            /*FeatureCollection fc;
            selection = null;
            try {
                fc = selectionLayer.getFeatureSource().getFeatures(bb);
                selection = fc;
            } catch (IOException e) {
                e.printStackTrace();
            }
*/
        }

        repaint();
    }
    
    void drawRectangle(Graphics graphics) {
        // undraw last box/draw new box
        int left = Math.min(startX, lastX);
        int right = Math.max(startX, lastX);
        int top = Math.max(startY, lastY);
        int bottom = Math.min(startY, lastY);
        int width = right - left;
        int height = top - bottom;
        // System.out.println("drawing rect("+left+","+bottom+","+ width+","+
        // height+")");
        graphics.drawRect(left, bottom, width, height);
    }
    
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if ((renderer == null) || (mapArea == null)) {
            return;
        }

        Rectangle r = getBounds();
        Rectangle dr = new Rectangle(r.width, r.height);

        if (!r.equals(oldRect) || reset) {
        	if(!r.equals(oldRect)) {
        		try {
					mapArea=context.getLayerBounds();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
            /* either the viewer size has changed or we've done a reset */
            changed = true; /* note we need to redraw */
            reset = false; /* forget about the reset */
            oldRect = r; /* store what the current size is */
            mapArea = fixAspectRatio(r, mapArea);
        }

        if (!mapArea.equals(oldMapArea)) { /* did the map extent change? */
            changed = true;
            oldMapArea = mapArea;
//          when we tell the context that the bounds have changed WMSLayers
            // can refresh them selves
            context.setAreaOfInterest(mapArea, context
                    .getCoordinateReferenceSystem());
        }

        if (changed) { /* if the map changed then redraw */
            changed = false;
            baseImage = new BufferedImage(dr.width, dr.height,
                    BufferedImage.TYPE_INT_ARGB);

            Graphics2D ig = baseImage.createGraphics();
            /* System.out.println("rendering"); */
            renderer.setContext(context);
            labelCache.clear(); // work around anoying labelcache bug


            // draw the map
            renderer.paint((Graphics2D) ig, dr, mapArea);
        }

        ((Graphics2D) g).drawImage(baseImage, 0, 0, this);

        if ((selectionFeature != null) && (selectionFeature.size() > 0)) {
            // paint selection

            String type = selectionLayer.getFeatureSource().getSchema()
            .getDefaultGeometry().getType().getBinding().getName();
            /*String type = selection.getDefaultGeometry().getGeometryType();*/
            /*System.out.println(type);*/
            if (type == null)
                type = "polygon";

            /* String type = "point"; */

            if (type.toLowerCase().endsWith("polygon")) {
                selectionStyle = selectionSD.polygonStyle;
            } else if (type.toLowerCase().endsWith("point")) {
                selectionStyle = selectionSD.pointStyle;
            } else if (type.toLowerCase().endsWith("line")) {
                selectionStyle = selectionSD.lineStyle;
            }

            selectionContext = new DefaultMapContext(DefaultGeographicCRS.WGS84);

            selectionContext.addLayer(selectionFeature, selectionStyle);
            selectionRenderer.setContext(selectionContext);

            selectImage = new BufferedImage(dr.width, dr.height,BufferedImage.TYPE_INT_ARGB);

            Graphics2D ig = selectImage.createGraphics();
            /* System.out.println("rendering selection"); */
            selectionRenderer.paint((Graphics2D) ig, dr, mapArea);

            ((Graphics2D) g).drawImage(selectImage, 0, 0, this);
        }

        if (highlightEnable && (highlightFeature != null)
                && (highlightFeature.size() > 0)) {
            /*
             * String type = selection.getDefaultGeometry().getGeometryType();
             * System.out.println(type); if(type==null) type="polygon";
             */
            String type = highlightLayer.getFeatureSource().getSchema()
            .getDefaultGeometry().getType().getBinding().getName();
            /*String type = selection.getDefaultGeometry().getGeometryType();*/
            //System.out.println(type);
            if (type == null)
                type = "polygon";

            /* String type = "point"; */
            Style highlightStyle = null;
            if (type.toLowerCase().endsWith("polygon")) {
                highlightStyle = highlightSD.polygonStyle;
            } else if (type.toLowerCase().endsWith("point")) {
                highlightStyle = highlightSD.pointStyle;
            } else if (type.toLowerCase().endsWith("line")) {
                highlightStyle = highlightSD.lineStyle;
            }


            MapContext highlightContext = new DefaultMapContext(DefaultGeographicCRS.WGS84);
            highlightContext.addLayer(highlightFeature, highlightStyle);
            highlightRenderer.setContext(highlightContext);
            highlightRenderer.paint((Graphics2D) g, dr, mapArea);
        }
    }
    
    
    
    
    //---------------------GENERAL----------------------------------------------     
    public MAP_TYPE getMapType() {
        return TYPE;
    }
    
    public void setRenderer(GTRenderer renderer){
        
        Map hints = new HashMap();
        if (renderer instanceof StreamingRenderer) {
            hints = renderer.getRendererHints();
            if (hints == null) {
                hints = new HashMap();
            }
            if (hints.containsKey(StreamingRenderer.LABEL_CACHE_KEY)) {
                labelCache = (LabelCache) hints
                        .get(StreamingRenderer.LABEL_CACHE_KEY);
            } else {
                hints.put(StreamingRenderer.LABEL_CACHE_KEY, labelCache);
            }
            renderer.setRendererHints(hints);
        }
        
        this.renderer = renderer;
        this.highlightRenderer = new StreamingRenderer();
        this.selectionRenderer = new StreamingRenderer();
        
        if (this.context != null) {
            this.renderer.setContext(this.context);
        }
    }    
    public GTRenderer getRenderer(){
        return renderer;
    }    
        
    public void setContext(MapContext context){
        if (this.context != null) {
            this.context.removeMapLayerListListener(MAPLAYERLIST_LISTEN);
        }

        this.context = context;

        if (context != null) {
            this.context.addMapLayerListListener(MAPLAYERLIST_LISTEN);
        }

        if (renderer != null) {
            renderer.setContext(this.context);
        }
    }    
    public MapContext getContext(){
        return context;
    }
    
    public void setMapArea(Envelope mapArea){
        this.mapArea = mapArea;
    }
    public Envelope getMapArea(){
        return mapArea;
    }
   
    public void setState(MapConstants.MAP_STATE state){
        this.state = state;
    }
    public MapConstants.MAP_STATE getState(){
        return state;
    }
    
    public void setZoomFactor(double zoomFactor) {
        this.zoomFactor = zoomFactor;
    }    
    public double getZoomFactor() {
        return zoomFactor;
    }
        
    public void reset(){
        reset = true;
    }
    public boolean isReset(){
        return reset;
    }
    
    public void setClickable(boolean clickable) {
        this.canClick = clickable;
    }
    public boolean isClickable(){
        return canClick;
    }
    
    @Override
    public Cursor getCursor(){
        return super.getCursor();
    }    
    @Override
    public void setCursor(Cursor cursor){
        super.setCursor(cursor);
    }    
    
    public void refresh(){
        repaint();
    }
    
    //------------------------HIGHLIGHT-----------------------------------------
    MapLayer highlightLayer;
    HighlightManager highlightManager;
    StyleSetDescriptor highlightSD; 
    FeatureCollection highlightFeature;
    final boolean highlightable = true;
    boolean highlightEnable = true;    
    
    public void setHighlightManager(HighlightManager highlightManager){
        this.highlightManager = highlightManager;
        this.highlightManager.addHighlightChangeListener(HIGHLIGHT_LISTEN);
        this.addMouseMotionListener(this.highlightManager);    
    }
    public HighlightManager getHighlightManager(){
        return highlightManager;
    }
        
    public void setHighlightEnabled(boolean highlight) {
        this.highlightEnable = highlight;
    }
    public boolean isHighlightEnabled() {
        return highlightEnable;
    }
    public boolean isMapHighlightable(){
        return highlightable;
    }
            
    public void setHighlightLayer(MapLayer highlightLayer){
        this.highlightLayer = highlightLayer;

        if (highlightManager != null) {
            highlightManager.setHighlightLayer(highlightLayer);
        }        
    }
    public MapLayer getHighlightLayer() {
        return highlightLayer;
    }
    
    public void setHighLightDescriptor( StyleSetDescriptor descriptor){
        highlightSD = descriptor;
    }
    public StyleSetDescriptor getHighLightDescriptor(){
        return highlightSD;
    }
    
            
    //---------------------SELECTION--------------------------------------------     
    MapLayer selectionLayer;
    MapContext selectionContext;
    SelectionManager selectionManager;
    StyleSetDescriptor selectionSD;
    FeatureCollection selectionFeature;
    Style selectionStyle;
    final boolean selectable = true;
    boolean selectionEnable = false;
    
    
    public void setSelectionManager(SelectionManager selectionManager){
        this.selectionManager = selectionManager;
        this.selectionManager.addSelectionChangeListener(SELECTION_LISTEN);
    }
    public SelectionManager getSelectionManager(){
        return selectionManager;
    }
    
    public void setSelectionEnabled(boolean selection){
        selectionEnable = selection;
    }
    public boolean isSelectionEnabled(){
        return selectionEnable;
    }    
    public boolean isMapSelectable(){
        return selectable;
    }
    
    public void setSelectionLayer(MapLayer selectionLayer){
        this.selectionLayer = selectionLayer;
        if(selectionManager!=null) {
            selectionManager.setSelectionLayer(selectionLayer);
        }        
    }
    public MapLayer getSelectionLayer(){
        return selectionLayer;
    }
            
    public void setSelectionDescriptor( StyleSetDescriptor descriptor){
        selectionSD = descriptor;
    }
    public StyleSetDescriptor getSelectionDescriptor(){
        return selectionSD;
    }
    
    public void doSelection(double x, double y, MapLayer layer){        
        Geometry geometry = gf.createPoint(new Coordinate(x, y));
        // org.opengis.geometry.Geometry geometry = new Point();
        findFeature(geometry, layer);
    }
    public void doSelection(double topx, double topy, double bottomx, double bottomy ){}
    
    public void setSelection(FeatureCollection selection){
        this.selectionFeature = selection;
        repaint();
    }
    public FeatureCollection getSelection(){
        return selectionFeature;
    }
    
    
    //--------------------EDITION-----------------------------------------------
    
    
    //----------------Variables partagees----------------------------------------
    
    boolean changed = false;
    int startX;
    int startY;
    int lastX;
    int lastY;    
    Rectangle oldRect = null;
    Envelope oldMapArea = null;
    BufferedImage baseImage;
    BufferedImage selectImage;

   
    
}
class MouseListen implements MouseListener,MouseMotionListener{
    private final DefaultMap2D map;    
    MouseListen(DefaultMap2D map){
        this.map = map;
    }
    
    public void mouseClicked(MouseEvent e) {
         // TODO Auto-generated method stub
        // System.out.println("before area "+mapArea+"\nw:"+mapArea.getWidth()+"
        // h:"+mapArea.getHeight());
        Rectangle bounds = map.getBounds();
        double x = (double) (e.getX());
        double y = (double) (e.getY());
        double width = map.mapArea.getWidth();
        double height = map.mapArea.getHeight();
        double width2 = map.mapArea.getWidth() / 2.0;
        double height2 = map.mapArea.getHeight() / 2.0;

        double mapX = ((x * width) / (double) bounds.width) + map.mapArea.getMinX();
        double mapY = (((bounds.getHeight() - y) * height) / (double) bounds.height) + map.mapArea.getMinY();

        /*
         * System.out.println(""+x+"->"+mapX);
         * System.out.println(""+y+"->"+mapY);
         */

        /*
         * Coordinate ll = new Coordinate(mapArea.getMinX(), mapArea.getMinY());
         * Coordinate ur = new Coordinate(mapArea.getMaxX(), mapArea.getMaxY());
         */
        double zlevel = 1.0;

        switch (map.state) {
        case PAN:
            zlevel = 1.0;
            break;
        case ZOOM_IN:
            zlevel = map.zoomFactor;
            break;
        case ZOOM_OUT:
            zlevel = 1.0 / map.zoomFactor;
            break;
        case SELECT:
            map.doSelection(mapX, mapY, map.selectionLayer);
            return;
        default:
            return;
        }

        Coordinate ll = new Coordinate(mapX - (width2 / zlevel), mapY - (height2 / zlevel));
        Coordinate ur = new Coordinate(mapX + (width2 / zlevel), mapY + (height2 / zlevel));

        map.setMapArea( new Envelope(ll, ur) );
        // System.out.println("after area "+mapArea+"\nw:"+mapArea.getWidth()+"
        // h:"+mapArea.getHeight());
        map.repaint();
    
    }
    public void mousePressed(MouseEvent e) {
        map.startX = e.getX();
        map.startY = e.getY();
        map.lastX = 0;
        map.lastY = 0;
    
    }
    public void mouseReleased(MouseEvent e) {
        int endX = e.getX();
        int endY = e.getY();

        if ((map.getState() == MapConstants.MAP_STATE.ZOOM_IN) || (map.getState() == MapConstants.MAP_STATE.ZOOM_OUT)) {
            map.drawRectangle(map.getGraphics());
        }

        map.processDrag( map.startX, map.startY, endX, endY);
        map.lastX = 0;
        map.lastY = 0;
    
    }
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseDragged(MouseEvent e) {   
        Graphics graphics = map.getGraphics();
        int x = e.getX();
        int y = e.getY();

        if (map.state == MapConstants.MAP_STATE.PAN) {
            // move the image with the mouse
            if ((map.lastX > 0) && (map.lastY > 0)) {
                int dx = map.lastX - map.startX;
                int dy = map.lastY - map.startY;
                // System.out.println("translate "+dx+","+dy);
                graphics.clearRect(0, 0, map.getWidth(), map.getHeight());
                ((Graphics2D) graphics).drawImage(map.baseImage, dx, dy, map);
            }

            map.lastX = x;
            map.lastY = y;
        } else if ((map.state == MapConstants.MAP_STATE.ZOOM_IN) || (map.state == MapConstants.MAP_STATE.ZOOM_OUT)) {
            graphics.setXORMode(Color.RED);

            if ((map.lastX > 0) && (map.lastY > 0)) {
                map.drawRectangle(graphics);
            }

            // draw new box
            map.lastX = x;
            map.lastY = y;
            map.drawRectangle(graphics);
        } else if (map.state == MapConstants.MAP_STATE.SELECT && map.selectionLayer != null) {

            // construct a new bbox filter
            Rectangle bounds = map.getBounds();
            Envelope mapArea = map.getMapArea();
            double mapWidth = mapArea.getWidth();
            double mapHeight = mapArea.getHeight();

            double x1 = ((map.startX * mapWidth) / (double) bounds.width) + mapArea.getMinX();
            double y1 = (((bounds.getHeight() - map.startY) * mapHeight) / (double) bounds.height) + mapArea.getMinY();
            double x2 = ((x * mapWidth) / (double) bounds.width) + mapArea.getMinX();
            double y2 = (((bounds.getHeight() - y) * mapHeight) / (double) bounds.height) + mapArea.getMinY();
            double left = Math.min(x1, x2);
            double right = Math.max(x1, x2);
            double bottom = Math.min(y1, y2);
            double top = Math.max(y1, y2);


            String name = map.selectionLayer.getFeatureSource().getSchema().getDefaultGeometry().getLocalName();

            if (name == "") {
                name = "the_geom";
            }
            Filter bb = map.ff.bbox(map.ff.property(name), left, bottom, right, top,map.context.getCoordinateReferenceSystem().toString());
            if(map.selectionManager!=null) {
                map.selectionManager.fireSelectionChanged(map, bb);
            }

            graphics.setXORMode(Color.green);

            /*
             * if ((lastX > 0) && (lastY > 0)) { drawRectangle(graphics); }
             */

            // draw new box
            map.lastX = x;
            map.lastY = y;
            map.drawRectangle(graphics);
    }
    
        
    
}    
    public void mouseMoved(MouseEvent e) {}    
}

////////////////////////////////////////////////////////////////////////////////
//---------------------MapLayerListListen-------------------------------------//
////////////////////////////////////////////////////////////////////////////////

class MapLayerListListen implements MapLayerListListener{
    private final DefaultMap2D map;    
    MapLayerListListen(DefaultMap2D map){
        this.map = map;
    }
        
    public void layerAdded(MapLayerListEvent event) {
        map.changed = true;

        MapContext context = map.getContext();
        
        if (context.getLayers().length == 1) { 
            try {
                map.setMapArea(context.getLayerBounds());
            } catch (IOException e) {
                e.printStackTrace();
            }
            map.reset();
        }

        map.repaint();
    }
    public void layerRemoved(MapLayerListEvent event) {
        map.changed = true;
        map.repaint();
    }
    public void layerChanged(MapLayerListEvent event) {
        map.changed = true;
        map.repaint();
    }
    public void layerMoved(MapLayerListEvent event) {
        map.changed = true;
        map.repaint();
    }    
}

////////////////////////////////////////////////////////////////////////////////
//---------------------HighlightChangeListen----------------------------------//
////////////////////////////////////////////////////////////////////////////////


class HighlightChangeListen implements HighlightChangeListener{
    private final DefaultMap2D map;    
    HighlightChangeListen(DefaultMap2D map){
        this.map = map;
    }

    public void highlightChanged(org.geotools.gui.swing.map.map2d.event.HighlightChangedEvent e) {   
        Filter f = e.getFilter();

        try {
            map.highlightFeature = map.highlightLayer.getFeatureSource().getFeatures(f);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        map.repaint();
    }    
}


////////////////////////////////////////////////////////////////////////////////
//---------------------SelectionChangeListen----------------------------------//
////////////////////////////////////////////////////////////////////////////////


class SelectionChangeListen implements SelectionChangeListener{
    private final DefaultMap2D map;    
    SelectionChangeListen(DefaultMap2D map){
        this.map = map;
    }

    public void selectionChanged(SelectionChangedEvent e) {
        try {
            map.selectionFeature = map.selectionLayer.getFeatureSource().getFeatures(e.getFilter());
            map.repaint();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }    
}
   

