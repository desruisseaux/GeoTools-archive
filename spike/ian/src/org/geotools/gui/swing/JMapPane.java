package org.geotools.gui.swing;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Iterator;

import javax.swing.JPanel;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.filter.GeometryFilter;
import org.geotools.filter.IllegalFilterException;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.renderer.GTRenderer;
import org.geotools.styling.Graphic;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.Mark;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.StyleFactoryFinder;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

public class JMapPane extends JPanel implements MouseListener, HighlightChangeListener {
    /**
     * what renders the map
     */
    GTRenderer renderer;

    /**
     * the map context to render
     */
    MapContext context;

    private MapContext selectionContext;

    /**
     * the area of the map to draw
     */
    Envelope mapArea;

    /**
     * the size of the pane last time we drew
     */
    private Rectangle oldRect = null;

    /**
     * the last map area drawn.
     */
    private Envelope oldMapArea = null;

    /**
     * the base image of the map
     */
    private BufferedImage baseImage;

    private BufferedImage selectImage;

    private Style selectionStyle;

    private int selectionLayer = -1;

    private MapLayer highlightLayer ;
    
    private HighlightManager highlightManager;
    
    private boolean highlight = true;

    FilterFactory ff = FilterFactoryFinder.createFilterFactory();

    GeometryFactory gf = new GeometryFactory();

    FeatureCollection selection, highlightFeature;

    public static final int Reset = 0;

    public static final int ZoomIn = 1;

    public static final int ZoomOut = 2;

    public static final int Pan = 3;

    public static final int Select = 4;

    private int state = ZoomIn;

    private double zoomFactor = 2.0;

    Style lineHighlightStyle;

    Style pointHighlightStyle;

    Style polygonHighlightStyle;

    Style polygonSelectionStyle;

    Style pointSelectionStyle;

    Style lineSelectionStyle;

    public JMapPane() {
        this(null, true, null, null);
    }

    public JMapPane(GTRenderer render, MapContext context) {
        this(null, true, render, context);
    }

    public JMapPane(LayoutManager layout, boolean isDoubleBuffered,
            GTRenderer render, MapContext context) {
        super(layout, isDoubleBuffered);
        setRenderer(render);

        setContext(context);
        
        this.addMouseListener(this);
        setHighlightManager(new HighlightManager(highlightLayer));
        
        lineHighlightStyle = setupStyle(LINE, Color.red);

        pointHighlightStyle = setupStyle(POINT, Color.red);

        polygonHighlightStyle = setupStyle(POLYGON, Color.red);

        polygonSelectionStyle = setupStyle(POLYGON, Color.cyan);

        pointSelectionStyle = setupStyle(POINT, Color.cyan);

        lineSelectionStyle = setupStyle(LINE, Color.cyan);

    }

    public GTRenderer getRenderer() {
        return renderer;
    }

    public void setRenderer(GTRenderer renderer) {
        this.renderer = renderer;

        if (this.context != null) {
            this.renderer.setContext(this.context);

        }
    }

    public MapContext getContext() {
        return context;
    }

    public void setContext(MapContext context) {

        this.context = context;

        if (renderer != null) {
            renderer.setContext(this.context);

        }
    }

    public Envelope getMapArea() {
        return mapArea;
    }

    public void setMapArea(Envelope mapArea) {
        this.mapArea = mapArea;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
        System.out.println("State: " + state);
    }

    public double getZoomFactor() {
        return zoomFactor;
    }

    public void setZoomFactor(double zoomFactor) {
        this.zoomFactor = zoomFactor;
    }

    public int getSelectionLayer() {
        return selectionLayer;
    }

    public void setSelectionLayer(int selectionLayer) {
        this.selectionLayer = selectionLayer;
    }

    public boolean isHighlight() {
        return highlight;
    }

    public void setHighlight(boolean highlight) {
        this.highlight = highlight;
    }

    public MapLayer getHighlightLayer() {
        return highlightLayer;
    }

    public void setHighlightLayer(MapLayer highlightLayer) {
        this.highlightLayer = highlightLayer;
        if(highlightManager!=null){
            highlightManager.setHighlightLayer(highlightLayer);
        }
    }

    public HighlightManager getHighlightManager() {
        return highlightManager;
    }

    public void setHighlightManager(HighlightManager highlightManager) {
        this.highlightManager = highlightManager;
        this.highlightManager.addHighlightChangeListener(this);
        this.addMouseMotionListener(this.highlightManager);
    }

    public Style getLineHighlightStyle() {
        return lineHighlightStyle;
    }

    public void setLineHighlightStyle(Style lineHighlightStyle) {
        this.lineHighlightStyle = lineHighlightStyle;
    }

    public Style getLineSelectionStyle() {
        return lineSelectionStyle;
    }

    public void setLineSelectionStyle(Style lineSelectionStyle) {
        this.lineSelectionStyle = lineSelectionStyle;
    }

    public Style getPointHighlightStyle() {
        return pointHighlightStyle;
    }

    public void setPointHighlightStyle(Style pointHighlightStyle) {
        this.pointHighlightStyle = pointHighlightStyle;
    }

    public Style getPointSelectionStyle() {
        return pointSelectionStyle;
    }

    public void setPointSelectionStyle(Style pointSelectionStyle) {
        this.pointSelectionStyle = pointSelectionStyle;
    }

    public Style getPolygonHighlightStyle() {
        return polygonHighlightStyle;
    }

    public void setPolygonHighlightStyle(Style polygonHighlightStyle) {
        this.polygonHighlightStyle = polygonHighlightStyle;
    }

    public Style getPolygonSelectionStyle() {
        return polygonSelectionStyle;
    }

    public void setPolygonSelectionStyle(Style polygonSelectionStyle) {
        this.polygonSelectionStyle = polygonSelectionStyle;
    }

    protected void paintComponent(Graphics g) {
        boolean changed = false;
        super.paintComponent(g);
        if (renderer == null || mapArea == null) {
            return;
        }
        Rectangle r = getBounds();
        Rectangle dr = new Rectangle(r.width, r.height);
        if (!r.equals(oldRect)) {
            changed = true;
            oldRect = r;

            double mapWidth = mapArea.getWidth();
            double mapHeight = mapArea.getHeight();
            double scaleX = r.getWidth() / mapArea.getWidth();
            double scaleY = r.getHeight() / mapArea.getHeight();
            double scale = 1.0;
            if (scaleX < scaleY) {
                scale = scaleX;
            } else {
                scale = scaleY;
            }
            double deltaX = Math.abs((r.getWidth() / scale) - mapWidth);
            double deltaY = Math.abs((r.getHeight() / scale) - mapHeight);
            /*
             * System.out.println("delta x "+deltaX); System.out.println("delta
             * y "+deltaY);
             */
            Coordinate ll = new Coordinate(mapArea.getMinX() - (deltaX / 2.0),
                    mapArea.getMinY() - (deltaY / 2.0));
            Coordinate ur = new Coordinate(mapArea.getMaxX() + (deltaX / 2.0),
                    mapArea.getMaxY() + (deltaY / 2.0));
            mapArea = new Envelope(ll, ur);
        }
        if (!mapArea.equals(oldMapArea)) {
            changed = true;
            oldMapArea = mapArea;
        }
        if (changed) {

            baseImage = new BufferedImage(dr.width, dr.height,
                    BufferedImage.TYPE_INT_ARGB);
            Graphics2D ig = baseImage.createGraphics();
            /*System.out.println("rendering");*/
            renderer.setContext(context);
            renderer.paint((Graphics2D) ig, dr, mapArea);
        }
        ((Graphics2D) g).drawImage(baseImage, 0, 0, this);
        if (selection != null && selection.size() > 0) {
            // paint selection
            /*
             * String type = selection.getDefaultGeometry().getGeometryType();
             * System.out.println(type); if(type==null) type="polygon";
             */
            String type = "polygon";
            if (type.equalsIgnoreCase("polygon")) {

                selectionStyle = polygonSelectionStyle;
            } else if (type.equalsIgnoreCase("point")) {

                selectionStyle = pointSelectionStyle;
            } else if (type.equalsIgnoreCase("line")) {

                selectionStyle = lineSelectionStyle;
            }

            selectionContext = new DefaultMapContext();

            selectionContext.addLayer(selection, selectionStyle);
            renderer.setContext(selectionContext);

            selectImage = new BufferedImage(dr.width, dr.height,
                    BufferedImage.TYPE_INT_ARGB);
            Graphics2D ig = selectImage.createGraphics();
            /*System.out.println("rendering selection");*/
            renderer.paint((Graphics2D) ig, dr, mapArea);

            ((Graphics2D) g).drawImage(selectImage, 0, 0, this);
        }
        if (highlight && highlightFeature!=null && highlightFeature.size()>0) {
            /*
             * String type = selection.getDefaultGeometry().getGeometryType();
             * System.out.println(type); if(type==null) type="polygon";
             */
            String type = "polygon";
            Style highlightStyle = null;
            if (type.equalsIgnoreCase("polygon")) {

                highlightStyle = polygonHighlightStyle;
            } else if (type.equalsIgnoreCase("point")) {

                highlightStyle = pointHighlightStyle;
            } else if (type.equalsIgnoreCase("line")) {

                highlightStyle = lineHighlightStyle;
            }

            MapContext highlightContext = new DefaultMapContext();

            highlightContext.addLayer(highlightFeature, highlightStyle);
            renderer.setContext(highlightContext);

            /*System.out.println("rendering highlight");*/
            renderer.paint((Graphics2D) g, dr, mapArea);

        }
    }

    public FeatureCollection doSelection(double x, double y, int layer) {
        GeometryFilter f = null;
        FeatureCollection select = null;
        Geometry geometry = gf.createPoint(new Coordinate(x, y));
        try {
            f = ff.createGeometryFilter(GeometryFilter.GEOMETRY_CONTAINS);
            f.addRightGeometry(ff.createLiteralExpression(geometry));
        } catch (IllegalFilterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (layer == -1) {
            for (int i = 0; i < context.getLayers().length; i++) {
                FeatureCollection fx = findFeature(f, i);
                if (select != null) {
                    select.addAll(fx);
                } else {
                    select = fx;
                }
            }
        } else {
            select = findFeature(f, layer);
        }
        return select;
    }

    /**
     * @param f -
     *            a partial geometry filter. The geom name will be added
     * @param i -
     *            the index of the layer to search
     * @throws IndexOutOfBoundsException
     */
    private FeatureCollection findFeature(GeometryFilter f, int i)
            throws IndexOutOfBoundsException {
        FeatureCollection fcol = null;
        if(context!=null&&i>context.getLayers().length){
            return fcol;
        }
        MapLayer layer = context.getLayer(i);
        
        try {
            String name = layer.getFeatureSource().getSchema()
                    .getDefaultGeometry().getName();
            if (name == "")
                name = "the_geom";
            f.addLeftGeometry(ff.createAttributeExpression(name));
            // System.out.println("looking with " + f);
            FeatureCollection fc = layer.getFeatureSource().getFeatures(f);
            if (fc.size() > 0) {
                // selectionStyle.getFeatureTypeStyles()[0].getRules()[0].setFilter(f);
                selectionLayer = i;
            }
            if (fcol == null) {
                fcol = fc;
                // here we should set the defaultgeom type
            } else {
                fcol.addAll(fc);
            }
            /*
             * GeometryAttributeType gat =
             * layer.getFeatureSource().getSchema().getDefaultGeometry();
             * fcol.setDefaultGeometry((Geometry)gat.createDefaultValue());
             */

            Iterator fi = fc.iterator();
            while (fi.hasNext()) {
                Feature feat = (Feature) fi.next();
                /*System.out.println("selected "
                        + feat.getAttribute("STATE_NAME"));*/
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalFilterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }/*
             * catch (IllegalAttributeException e) { // TODO Auto-generated
             * catch block System.err.println(e.getMessage()); //
             * e.printStackTrace(); }
             */
        return fcol;
    }

    public void mouseClicked(MouseEvent e) {
        // TODO Auto-generated method stub
        // System.out.println("before area "+mapArea+"\nw:"+mapArea.getWidth()+"
        // h:"+mapArea.getHeight());
        Rectangle bounds = this.getBounds();
        double x = (double) (e.getX());
        double y = (double) (e.getY());
        double width = mapArea.getWidth();
        double height = mapArea.getHeight();
        double width2 = mapArea.getWidth() / 2.0;
        double height2 = mapArea.getHeight() / 2.0;

        double mapX = (x * width / (double) bounds.width) + mapArea.getMinX();
        double mapY = ((bounds.getHeight() - y) * height / (double) bounds.height)
                + mapArea.getMinY();
        /*
         * System.out.println(""+x+"->"+mapX);
         * System.out.println(""+y+"->"+mapY);
         */
        /*
         * Coordinate ll = new Coordinate(mapArea.getMinX(), mapArea.getMinY());
         * Coordinate ur = new Coordinate(mapArea.getMaxX(), mapArea.getMaxY());
         */
        double zlevel = 1.0;
        switch (state) {
        case Pan:
            zlevel = 1.0;
            break;
        case ZoomIn:
            zlevel = zoomFactor;
            break;
        case ZoomOut:
            zlevel = 1.0 / zoomFactor;
            break;
        case Select:
            selection=doSelection(mapX, mapY, selectionLayer);
            repaint();
            return;
        default:
            return;
        }
        Coordinate ll = new Coordinate(mapX - (width2 / zlevel), mapY
                - (height2 / zlevel));
        Coordinate ur = new Coordinate(mapX + (width2 / zlevel), mapY
                + (height2 / zlevel));

        mapArea = new Envelope(ll, ur);
        // System.out.println("after area "+mapArea+"\nw:"+mapArea.getWidth()+"
        // h:"+mapArea.getHeight());
        repaint();
    }

    public void mouseEntered(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    public void mouseExited(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    public void mousePressed(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    public void mouseReleased(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    private static final int POLYGON = 0;

    private static final int LINE = 1;

    private static final int POINT = 2;

    private org.geotools.styling.Style setupStyle(int type, Color color) {
        StyleFactory sf = StyleFactoryFinder.createStyleFactory();
        StyleBuilder sb = new StyleBuilder(sf, ff);

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
        switch (type) {
        case POLYGON:
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

    public void mouseDragged(MouseEvent e) {
        // TODO Auto-generated method stub
        
    }

    public void highlightChanged(HighlightChangedEvent e){
        // TODO Auto-generated method stub
        
        
        Filter f = e.getFilter();
        try {
            highlightFeature = highlightLayer.getFeatureSource().getFeatures(f);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        repaint();
        
    }
}
