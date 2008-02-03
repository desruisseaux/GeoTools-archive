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
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.event.MouseInputListener;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.geometry.jts.JTS;
import org.geotools.gui.swing.icon.IconBundle;
import org.geotools.gui.swing.map.map2d.decoration.MapDecoration;
import org.geotools.gui.swing.map.map2d.decoration.SelectionDecoration;
import org.geotools.gui.swing.map.map2d.event.Map2DContextEvent;
import org.geotools.gui.swing.map.map2d.event.Map2DMapAreaEvent;
import org.geotools.gui.swing.map.map2d.event.Map2DSelectionEvent;
import org.geotools.gui.swing.map.map2d.listener.SelectableMap2DListener;
import org.geotools.gui.swing.map.map2d.strategy.RenderingStrategy;
import org.geotools.gui.swing.map.map2d.strategy.SingleBufferedImageStrategy;
import org.geotools.gui.swing.misc.FacilitiesFactory;
import org.geotools.gui.swing.misc.GeometryClassFilter;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.map.event.MapLayerListEvent;
import org.geotools.map.event.MapLayerListListener;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Fill;
import org.geotools.styling.Graphic;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.Mark;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.Symbolizer;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

/**
 * Default implementation of navigableMap2D
 * @author Johann Sorel
 */
public class JDefaultSelectableMap2D extends JDefaultNavigableMap2D implements SelectableMap2D {

    /**
     * Geometry factory for JTS geometry creation
     */
    protected final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();
    /**
     * Facilities factory to duplicate MapLayers
     */
    protected final FacilitiesFactory FACILITIES_FACTORY = new FacilitiesFactory();
    /**
     * Style builder for sld style creation
     */
    protected final StyleBuilder STYLE_BUILDER = new StyleBuilder();
    private final RenderingStrategy selectionStrategy = new SingleBufferedImageStrategy();
    private final MapContext selectionMapContext = new DefaultMapContext(DefaultGeographicCRS.WGS84);
    private final MouseInputListener mouseInputListener;
    private final MapLayerListListener mapLayerListlistener;
    private final SelectionDecoration selectionPane = new SelectionDecoration();
    private final BufferComponent selectedPane = new BufferComponent();
    private final FilterFactory2 ff = (FilterFactory2) CommonFactoryFinder.getFilterFactory(null);
    private final Map<MapLayer, MapLayer> copies = new HashMap<MapLayer, MapLayer>();
    private Color selectionStyleColor = Color.GREEN;
    private Geometry selectionGeometrie = null;
    protected Cursor CUR_SELECT;

    /**
     * create a default JDefaultSelectableMap2D
     */
    public JDefaultSelectableMap2D() {
        super();
        mouseInputListener = new MouseListen();
        mapLayerListlistener = new MapLayerListListen();
        addMouseListener(mouseInputListener);
        addMouseMotionListener(mouseInputListener);

        selectionStrategy.setContext(selectionMapContext);


        addMapDecoration(selectedPane);
        addMapDecoration(selectionPane);

        buildCursors();
    }

    private void buildCursors() {
        Toolkit tk = Toolkit.getDefaultToolkit();
        ImageIcon ico_select = IconBundle.getResource().getIcon("16_select");


        BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        img.getGraphics().drawImage(ico_select.getImage(), 0, 0, null);
        CUR_SELECT = tk.createCustomCursor(img, new java.awt.Point(1, 1), "select");

    }

    /**
     * transform a mouse coordinate in JTS Coordinate using the CRS of the mapcontext
     * @param mx : x coordinate of the mouse on the map (in pixel)
     * @param my : y coordinate of the mouse on the map (in pixel)
     * @return JTS Coordinate
     */
    protected Coordinate toMapCoord(int mx, int my) {
        Envelope mapArea = renderingStrategy.getMapArea();

        Rectangle bounds = getBounds();
        double width = mapArea.getWidth();
        double height = mapArea.getHeight();
        return toMapCoord(mx, my, width, height, bounds);
    }

    /**
     *  transform a mouse coordinate in JTS Geometry using the CRS of the mapcontext
     * @param mx : x coordinate of the mouse on the map (in pixel)
     * @param my : y coordinate of the mouse on the map (in pixel)
     * @return JTS geometry (corresponding to a square of 6x6 pixel around mouse coordinate)
     */
    protected Geometry mousePositionToGeometry(double mx, double my) {
        Envelope mapArea = renderingStrategy.getMapArea();

        if (mapArea != null) {
            Rectangle bounds = getBounds();
            double width = mapArea.getWidth();
            double height = mapArea.getHeight();
            Coordinate[] coord = new Coordinate[5];

            int taille = 3;

            coord[0] = toMapCoord(mx - taille, my - taille, width, height, bounds);
            coord[1] = toMapCoord(mx - taille, my + taille, width, height, bounds);
            coord[2] = toMapCoord(mx + taille, my + taille, width, height, bounds);
            coord[3] = toMapCoord(mx + taille, my - taille, width, height, bounds);
            coord[4] = coord[0];

            LinearRing lr1 = GEOMETRY_FACTORY.createLinearRing(coord);
            return GEOMETRY_FACTORY.createPolygon(lr1, null);
        }

        return null;
    }

    /**
     * create a filter corresponding to the layer features intersecting the geom
     * @param geom : the intersect JTS geometry used by the filter
     * @param layer : MapLayer for which the filter is made
     * @return Filter
     */
    protected Filter getFeatureInGeometry(Geometry geom, MapLayer layer) {
        Filter f = null;

        geom = projectGeometry(geom, layer);

        try {
            String name = layer.getFeatureSource().getSchema().getDefaultGeometry().getLocalName();
            if (name.equals("")) {
                name = "the_geom";
            }
            f = ff.intersects(ff.property(name), ff.literal(geom));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return f;
    }

    private Style createStyle(MapLayer layer) {


        Class jtsClass = layer.getFeatureSource().getSchema().getDefaultGeometry().getType().getBinding();

        if (jtsClass.equals(Point.class) || jtsClass.equals(MultiPoint.class)) {
            Fill fill = STYLE_BUILDER.createFill(selectionStyleColor, 0.6f);
            Stroke stroke = STYLE_BUILDER.createStroke(selectionStyleColor, 2);
            stroke.setOpacity(STYLE_BUILDER.literalExpression(1f));

            Mark mark = STYLE_BUILDER.createMark("circle", fill, stroke);
            Graphic gra = STYLE_BUILDER.createGraphic();
            gra.setOpacity(STYLE_BUILDER.literalExpression(1f));
            gra.setMarks(new Mark[]{mark});
            gra.setSize(STYLE_BUILDER.literalExpression(15));

            PointSymbolizer ps = STYLE_BUILDER.createPointSymbolizer(gra);

            Style pointSelectionStyle = STYLE_BUILDER.createStyle();
            pointSelectionStyle.addFeatureTypeStyle(STYLE_BUILDER.createFeatureTypeStyle(ps));

            return pointSelectionStyle;

        } else if (jtsClass.equals(LineString.class) || jtsClass.equals(MultiLineString.class)) {
            Fill fill = STYLE_BUILDER.createFill(selectionStyleColor, 0.6f);
            Stroke stroke = STYLE_BUILDER.createStroke(selectionStyleColor, 2);
            stroke.setOpacity(STYLE_BUILDER.literalExpression(1f));

            Mark mark = STYLE_BUILDER.createMark("circle", fill, stroke);
            Graphic gra = STYLE_BUILDER.createGraphic();
            gra.setOpacity(STYLE_BUILDER.literalExpression(1f));
            gra.setMarks(new Mark[]{mark});
            gra.setSize(STYLE_BUILDER.literalExpression(5));

            PointSymbolizer ps = STYLE_BUILDER.createPointSymbolizer(gra);
            LineSymbolizer ls = STYLE_BUILDER.createLineSymbolizer(stroke);

            Rule r1 = STYLE_BUILDER.createRule(new Symbolizer[]{ps});
            Rule r2 = STYLE_BUILDER.createRule(new Symbolizer[]{ls});

            Style lineSelectionStyle = STYLE_BUILDER.createStyle();
            lineSelectionStyle.addFeatureTypeStyle(STYLE_BUILDER.createFeatureTypeStyle(null, new Rule[]{r1, r2}));

            return lineSelectionStyle;

        } else if (jtsClass.equals(Polygon.class) || jtsClass.equals(MultiPolygon.class)) {
            Fill fill = STYLE_BUILDER.createFill(selectionStyleColor, 0.6f);
            Stroke stroke = STYLE_BUILDER.createStroke(selectionStyleColor, 2);
            stroke.setOpacity(STYLE_BUILDER.literalExpression(1f));

            PolygonSymbolizer pls = STYLE_BUILDER.createPolygonSymbolizer(stroke, fill);

            Mark mark = STYLE_BUILDER.createMark("circle", fill, stroke);
            Graphic gra = STYLE_BUILDER.createGraphic();
            gra.setOpacity(STYLE_BUILDER.literalExpression(1f));
            gra.setMarks(new Mark[]{mark});
            gra.setSize(STYLE_BUILDER.literalExpression(5));
            PointSymbolizer ps = STYLE_BUILDER.createPointSymbolizer(gra);


            Rule r1 = STYLE_BUILDER.createRule(new Symbolizer[]{ps});
            Rule r3 = STYLE_BUILDER.createRule(new Symbolizer[]{pls});

            Style polySelectionStyle = STYLE_BUILDER.createStyle();
            polySelectionStyle.addFeatureTypeStyle(STYLE_BUILDER.createFeatureTypeStyle(null, new Rule[]{r1, r3}));

            return polySelectionStyle;

        }


        Fill fill = STYLE_BUILDER.createFill(selectionStyleColor, 0.4f);
        Stroke stroke = STYLE_BUILDER.createStroke(selectionStyleColor, 2);
        stroke.setOpacity(STYLE_BUILDER.literalExpression(0.6f));

        PolygonSymbolizer pls = STYLE_BUILDER.createPolygonSymbolizer(stroke, fill);

        Mark mark = STYLE_BUILDER.createMark("circle", fill, stroke);
        Graphic gra = STYLE_BUILDER.createGraphic();
        gra.setOpacity(STYLE_BUILDER.literalExpression(0.6f));
        gra.setMarks(new Mark[]{mark});
        gra.setSize(STYLE_BUILDER.literalExpression(14));
        PointSymbolizer ps = STYLE_BUILDER.createPointSymbolizer(gra);

        LineSymbolizer ls = STYLE_BUILDER.createLineSymbolizer(stroke);

        Rule r1 = STYLE_BUILDER.createRule(new Symbolizer[]{ps});
        r1.setFilter(new GeometryClassFilter(Point.class, MultiPoint.class));
        Rule r2 = STYLE_BUILDER.createRule(new Symbolizer[]{ls});
        r2.setFilter(new GeometryClassFilter(LineString.class, MultiLineString.class));
        Rule r3 = STYLE_BUILDER.createRule(new Symbolizer[]{pls});
        r3.setFilter(new GeometryClassFilter(Polygon.class, MultiPolygon.class));


        Style LineSelectionStyle = STYLE_BUILDER.createStyle();
        LineSelectionStyle.addFeatureTypeStyle(STYLE_BUILDER.createFeatureTypeStyle(null, new Rule[]{r1, r2, r3}));

        return LineSelectionStyle;
    }

    private void applyStyleFilter(Style style, Filter f) {

        for (FeatureTypeStyle fts : style.getFeatureTypeStyles()) {
            for (Rule r : fts.getRules()) {

                Filter nf = STYLE_BUILDER.getFilterFactory().and(r.getFilter(), f);

                r.setFilter(f);
            }
        }

    }

    private Coordinate toMapCoord(double mx, double my, double width, double height, Rectangle bounds) {
        Envelope mapArea = renderingStrategy.getMapArea();

        double mapX = ((mx * width) / (double) bounds.width) + mapArea.getMinX();
        double mapY = (((bounds.getHeight() - my) * height) / (double) bounds.height) + mapArea.getMinY();
        return new Coordinate(mapX, mapY);
    }

    private void doMouseSelection(double mx, double my) {

        Geometry geometry = mousePositionToGeometry(mx, my);
        if (geometry != null) {
            doSelection(geometry);
        }
    }

    private void doMouseSelection(double mx, double my, double ex, double ey) {
        Envelope mapArea = renderingStrategy.getMapArea();

        if (mapArea != null) {
            Rectangle bounds = getBounds();
            double width = mapArea.getWidth();
            double height = mapArea.getHeight();
            Coordinate[] coord = new Coordinate[5];

            coord[0] = toMapCoord(mx, my, width, height, bounds);
            coord[1] = toMapCoord(mx, ey, width, height, bounds);
            coord[2] = toMapCoord(ex, ey, width, height, bounds);
            coord[3] = toMapCoord(ex, my, width, height, bounds);
            coord[4] = coord[0];

            LinearRing lr1 = GEOMETRY_FACTORY.createLinearRing(coord);
            Geometry geometry = GEOMETRY_FACTORY.createPolygon(lr1, null);

            doSelection(geometry);
        }
    }

    protected Geometry projectGeometry(Geometry geom, MapLayer layer) {
        MathTransform transform = null;

        MapContext context = getRenderingStrategy().getContext();
        CoordinateReferenceSystem contextCRS = context.getCoordinateReferenceSystem();
        CoordinateReferenceSystem layerCRS = layer.getFeatureSource().getSchema().getCRS();

        if (layerCRS == null) {
            layerCRS = contextCRS;
        }


        if (!contextCRS.equals(layerCRS)) {
            try {
                transform = CRS.findMathTransform(contextCRS, layerCRS, true);
                geom = JTS.transform(geom, transform);
            } catch (Exception ex) {
                System.out.println("Error using default layer CRS, searching for a close CRS");

                try {
                    Integer epsgId = CRS.lookupEpsgCode(layerCRS, true);
                    if (epsgId != null) {
                        System.out.println("Close CRS found, will replace original CRS for convertion");
                        CoordinateReferenceSystem newCRS = CRS.decode("EPSG:" + epsgId);
                        layerCRS = newCRS;
                        transform = CRS.findMathTransform(contextCRS, layerCRS);
                    } else {
                        System.out.println("No close CRS found, will force convert");
                        try {
                            transform = CRS.findMathTransform(contextCRS, layerCRS, true);
                        } catch (Exception e2) {
                            e2.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Search Error, no close CRS found, will force convertion");

                    try {
                        transform = CRS.findMathTransform(contextCRS, layerCRS, true);
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
                ex.printStackTrace();
            }
        }

        return geom;
    }

    protected Geometry projectGeometry(Geometry geom, CoordinateReferenceSystem inCRS, CoordinateReferenceSystem outCRS) {
        MathTransform transform = null;

        MapContext context = getRenderingStrategy().getContext();

        if (outCRS == null) {
            outCRS = inCRS;
        }



        if (!inCRS.equals(outCRS)) {
            try {
                transform = CRS.findMathTransform(inCRS, outCRS, true);
                geom = JTS.transform(geom, transform);
            } catch (Exception ex) {
                System.out.println("Error using default layer CRS, searching for a close CRS");

                try {
                    Integer epsgId = CRS.lookupEpsgCode(outCRS, true);
                    if (epsgId != null) {
                        System.out.println("Close CRS found, will replace original CRS for convertion");
                        CoordinateReferenceSystem newCRS = CRS.decode("EPSG:" + epsgId);
                        outCRS = newCRS;
                        transform = CRS.findMathTransform(inCRS, outCRS);
                    } else {
                        System.out.println("No close CRS found, will force convert");
                        try {
                            transform = CRS.findMathTransform(inCRS, outCRS, true);
                        } catch (Exception e2) {
                            e2.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Search Error, no close CRS found, will force convertion");

                    try {
                        transform = CRS.findMathTransform(inCRS, outCRS, true);
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
                ex.printStackTrace();
            }
        }

        return geom;
    }

    private void fireSelectionChanged(Geometry geo) {
        Map2DSelectionEvent mce = new Map2DSelectionEvent(this, geo);

        SelectableMap2DListener[] lst = getSelectableMap2DListeners();

        for (SelectableMap2DListener l : lst) {
            l.mapSelectionChanged(mce);
        }

    }

    @Override
    protected void setRendering(boolean render) {
        super.setRendering(render);

        MapContext context = getRenderingStrategy().getContext();

        if (context != null && context.getCoordinateReferenceSystem() != null) {
            try {
                selectionMapContext.setCoordinateReferenceSystem(context.getCoordinateReferenceSystem());
            } catch (TransformException ex) {
                ex.printStackTrace();
            } catch (FactoryException ex) {
                ex.printStackTrace();
            }
        }

        if (context != null && context.getAreaOfInterest() != null) {
            selectionMapContext.setAreaOfInterest(context.getAreaOfInterest());
        }
        
        selectionStrategy.refresh();

    }

    //---------------------JDefaultMap2D override-------------------------------    



    @Override
    protected void mapAreaChanged(Map2DMapAreaEvent event) {
        super.mapAreaChanged(event);

        MapContext context = getRenderingStrategy().getContext();

        if (context != null && context.getCoordinateReferenceSystem() != null) {

            try {
                selectionMapContext.setCoordinateReferenceSystem(context.getCoordinateReferenceSystem());
            } catch (TransformException ex) {
                ex.printStackTrace();
            } catch (FactoryException ex) {
                ex.printStackTrace();
            }
        }


        selectionStrategy.setMapArea(event.getNewMapArea());
    }

    @Override
    protected void crsChanged(PropertyChangeEvent arg0) {

        MapContext context = getRenderingStrategy().getContext();

        if (context != null && context.getCoordinateReferenceSystem() != null) {
            try {
                selectionMapContext.setCoordinateReferenceSystem(context.getCoordinateReferenceSystem());
            } catch (TransformException ex) {
                ex.printStackTrace();
            } catch (FactoryException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    protected void mapContextChanged(Map2DContextEvent event) {
        selectionMapContext.clearLayerList();
        copies.clear();

        MapContext context = event.getNewContext();

        if (context != null && context.getCoordinateReferenceSystem() != null) {
            try {
                selectionMapContext.setCoordinateReferenceSystem(context.getCoordinateReferenceSystem());
                if (context.getAreaOfInterest() != null) {
                    selectionMapContext.setAreaOfInterest(context.getAreaOfInterest());
                }
            } catch (TransformException ex) {
                ex.printStackTrace();
            } catch (FactoryException ex) {
                ex.printStackTrace();
            }
        }

        if (event.getPreviousContext() != null) {
            event.getPreviousContext().removeMapLayerListListener(mapLayerListlistener);
        }

        if (event.getNewContext() != null) {
            event.getNewContext().addMapLayerListListener(mapLayerListlistener);
        }

        super.mapContextChanged(event);
    }

    //----------------------SELECTABLE MAP2D------------------------------------

    private void addSelectableLayerNU(MapLayer layer) {
        if (layer != null) {
            MapLayer copy = FACILITIES_FACTORY.duplicateLayer(layer);

            copy.setStyle(createStyle(layer));

            if (selectionGeometrie != null) {
                try {
                    Filter f = getFeatureInGeometry(selectionGeometrie, copy);
                    applyStyleFilter(copy.getStyle(), f);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                applyStyleFilter(copy.getStyle(), Filter.EXCLUDE);
            }

            selectionMapContext.addLayer(copy);

            if (selectionMapContext.getLayerCount() == 1) {
                selectionStrategy.setMapArea(renderingStrategy.getMapArea());
            }

            copies.put(layer, copy);
        }
    }

    public void addSelectableLayer(MapLayer layer) {
        addSelectableLayerNU(layer);
        selectionStrategy.refresh();
    }

    public void addSelectableLayer(MapLayer[] layers) {
        if (layers != null) {
            for (MapLayer layer : layers) {
                addSelectableLayerNU(layer);
            }
            selectionStrategy.refresh();
        }
    }

    public void removeSelectableLayer(MapLayer layer) {
        MapLayer copy = copies.remove(layer);
        selectionMapContext.removeLayer(copy);
    }

    public MapLayer[] getSelectableLayer() {
        return copies.keySet().toArray(new MapLayer[0]);

//        return selectionMapContext.getLayers();
    //return selectableLayers.toArray(new MapLayer[selectableLayers.size()]);
    }

    public boolean isLayerSelectable(MapLayer layer) {
        return copies.containsKey(layer);
//        return (selectionMapContext.indexOf(layer) == -1) ? false : true ;
//        return selectableLayers.contains(layer);
    }

    public void doSelection(double x, double y) {

        Geometry geometry = GEOMETRY_FACTORY.createPoint(new Coordinate(x, y));
        // org.opengis.geometry.Geometry geometry = new Point();
        doSelection(geometry);
    }

    public void doSelection(Geometry geometry) {

        selectionGeometrie = geometry;

        MapContext context = renderingStrategy.getContext();
        Filter f = null;

        if ((context == null) || (selectionMapContext.getLayerCount() == 0)) {
            return;
        }

        for (MapLayer layer : selectionMapContext.getLayers()) {

            try {
                f = getFeatureInGeometry(geometry, layer);
                applyStyleFilter(layer.getStyle(), f);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        selectionStrategy.refresh();
        
        fireSelectionChanged(geometry);

    }

    public void addSelectableMap2DListener(SelectableMap2DListener listener) {
        MAP2DLISTENERS.add(SelectableMap2DListener.class, listener);
    }

    public void removeSelectableMap2DListener(SelectableMap2DListener listener) {
        MAP2DLISTENERS.remove(SelectableMap2DListener.class, listener);
    }

    public SelectableMap2DListener[] getSelectableMap2DListeners() {
        return MAP2DLISTENERS.getListeners(SelectableMap2DListener.class);
    }

    //---------------------PRIVATE CLASSES--------------------------------------        

    private class MouseListen implements MouseInputListener {

        int startX = 0;
        int startY = 0;
        int lastX = 0;
        int lastY = 0;

        private void drawRectangle(boolean view, boolean fill) {
            int left = Math.min(startX, lastX);
            int right = Math.max(startX, lastX);
            int top = Math.max(startY, lastY);
            int bottom = Math.min(startY, lastY);
            int width = right - left;
            int height = top - bottom;
            selectionPane.setFill(fill);
            selectionPane.setCoord(left, bottom, width, height, view);
        //graphics.drawRect(left, bottom, width, height);
        }

        public void mouseClicked(MouseEvent e) {

            switch (actionState) {
                case SELECT:
                    doMouseSelection(e.getX(), e.getY());
                    return;
            }


        }

        public void mousePressed(MouseEvent e) {
            startX = e.getX();
            startY = e.getY();
            lastX = 0;
            lastY = 0;

        }

        public void mouseReleased(MouseEvent e) {
            lastX = e.getX();
            lastY = e.getY();

            switch (actionState) {
                case SELECT:
                    drawRectangle(false, true);
                    doMouseSelection(startX, startY, lastX, lastY);
                    return;
            }
        }

        public void mouseEntered(MouseEvent e) {
            switch (actionState) {
                case SELECT:
                    setCursor(CUR_SELECT);
                    break;
            }
        }

        public void mouseExited(MouseEvent e) {
        }

        public void mouseDragged(MouseEvent e) {
            lastX = e.getX();
            lastY = e.getY();

            switch (actionState) {
                case SELECT:
                    drawRectangle(true, true);
                    return;
            }

        }

        public void mouseMoved(MouseEvent e) {
        }
    }

    private class MapLayerListListen implements MapLayerListListener {

        public void layerAdded(MapLayerListEvent event) {
        }

        public void layerRemoved(MapLayerListEvent event) {
            removeSelectableLayer(event.getLayer());
        }

        public void layerChanged(MapLayerListEvent event) {
        }

        public void layerMoved(MapLayerListEvent event) {
        }
    }

    private class BufferComponent extends JComponent implements MapDecoration {

        private BufferedImage img;
        private Rectangle oldone = null;
        private Rectangle newone = null;

        public BufferComponent() {
            setLayout(new BorderLayout());
            add(selectionStrategy.getComponent());
        }

//        public void setBuffer(BufferedImage buf) {
//            img = buf;
//            repaint();
//        }

//        @Override
//        public void paintComponent(Graphics g) {
//            newone = getBounds();
//            if (img != null) {
//                g.drawImage(img, 0, 0, this);
//            }
//
//        }
        public void refresh() {
        }

        public JComponent geComponent() {
            return this;
        }

        public void setMap2D(Map2D map) {

        }

        public Map2D getMap2D() {
            return null;
        }
        }
}





