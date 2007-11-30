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
import com.vividsolutions.jts.geom.LinearRing;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.event.MouseInputListener;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.filter.IllegalFilterException;
import org.geotools.geometry.GeometryBuilder;
import org.geotools.gui.swing.map.map2d.overLayer.SelectionOverLayer;
import org.geotools.gui.swing.misc.FacilitiesFactory;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.map.event.MapLayerListEvent;
import org.geotools.map.event.MapLayerListListener;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.shape.ShapefileRenderer;
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
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;

/**
 *
 * @author Johann Sorel
 */
public class JDefaultSelectableMap2D extends JDefaultNavigableMap2D implements SelectableMap2D {

    protected final GeometryFactory gf = new GeometryFactory();
    private static final FacilitiesFactory FF = new FacilitiesFactory();
    private final MapContext selectionMapContext = new DefaultMapContext(DefaultGeographicCRS.WGS84);
    private final MouseInputListener mouseInputListener;
    private final MapLayerListListener mapLayerListlistener;
    private final SelectionOverLayer selectionPane = new SelectionOverLayer();
    private final BufferComponent selectedPane = new BufferComponent();
    private final FilterFactory2 ff = (FilterFactory2) CommonFactoryFinder.getFilterFactory(null);
    GeometryBuilder geometryBuilder = new GeometryBuilder(DefaultGeographicCRS.WGS84);
    private final Map<MapLayer, MapLayer> copies = new HashMap<MapLayer, MapLayer>();
    private Style selectionStyle = null;

    public JDefaultSelectableMap2D() {
        this(new ShapefileRenderer());
    }

    public JDefaultSelectableMap2D(GTRenderer renderer) {
        super(renderer);
        mouseInputListener = new MouseListen();
        mapLayerListlistener = new MapLayerListListen();
        addMouseListener(mouseInputListener);
        addMouseMotionListener(mouseInputListener);
        layerPane.add(selectedPane, new Integer(NEXT_OVER_LAYER_INDEX));
        NEXT_OVER_LAYER_INDEX++;
        layerPane.add(selectionPane, new Integer(NEXT_OVER_LAYER_INDEX));
        NEXT_OVER_LAYER_INDEX++;

        buildSelectionStyle();
    }

    private void buildSelectionStyle() {

        StyleBuilder sb = new StyleBuilder();

        Fill fill = sb.createFill(Color.GREEN, 0f);
        Stroke stroke = sb.createStroke(Color.GREEN, 2);
        stroke.setOpacity(sb.literalExpression(1f));

        PolygonSymbolizer pls = sb.createPolygonSymbolizer(stroke, fill);

        fill = sb.createFill(Color.GREEN, 0f);
        stroke = sb.createStroke(Color.GREEN, 0);

        Mark mark = sb.createMark("cross", fill, stroke);
        Graphic gra = sb.createGraphic();
        gra.setOpacity(sb.literalExpression(1f));
        gra.setMarks(new Mark[]{mark});
        gra.setSize(sb.literalExpression(13));
        PointSymbolizer ps = sb.createPointSymbolizer(gra);

        LineSymbolizer ls = sb.createLineSymbolizer(stroke);

        selectionStyle = sb.createStyle();
        selectionStyle.addFeatureTypeStyle(sb.createFeatureTypeStyle(pls));
        selectionStyle.addFeatureTypeStyle(sb.createFeatureTypeStyle(ps));
        selectionStyle.addFeatureTypeStyle(sb.createFeatureTypeStyle(ls));

    }

    private void applyStyleFilter(Filter f) {

        for (FeatureTypeStyle fts : selectionStyle.getFeatureTypeStyles()) {
            for (Rule r : fts.getRules()) {
                r.setFilter(f);
            }
        }
        updateOverLayer();
    }

    private void updateOverLayer() {
        selectedPane.setBuffer(createBufferImage(selectionMapContext));
    }

    private Coordinate toMapCoord(double mx, double my, double width, double height, Rectangle bounds) {
        double mapX = ((mx * width) / (double) bounds.width) + mapArea.getMinX();
        double mapY = (((bounds.getHeight() - my) * height) / (double) bounds.height) + mapArea.getMinY();
        return new Coordinate(mapX, mapY);
    }

    protected Geometry mousePositionToGeometry(double mx, double my) {
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

            LinearRing lr1 = gf.createLinearRing(coord);
            return gf.createPolygon(lr1, null);
        }

        return null;
    }

    protected Filter getFeatureInGeometry(Geometry geom, MapLayer layer) {
        Filter f = null;


        try {
            String name = layer.getFeatureSource().getSchema().getDefaultGeometry().getLocalName();
            if (name == "") {
                name = "the_geom";
            }
            f = ff.intersects(ff.property(name), ff.literal(geom));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return f;
    }

    private void doMouseSelection(double mx, double my) {

        Geometry geometry = mousePositionToGeometry(mx, my);
        if (geometry != null) {
            findFeature(geometry);
        }
    }

    private void doMouseSelection(double mx, double my, double ex, double ey) {
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

            LinearRing lr1 = gf.createLinearRing(coord);
            Geometry geometry = gf.createPolygon(lr1, null);

            findFeature(geometry);
        }
    }

    
    //-------------------MAP2D--------------------------------------------------
    @Override
    public void setContext(MapContext context) {
        selectionMapContext.clearLayerList();
        copies.clear();

        if (this.context != null) {
            this.context.removeMapLayerListListener(mapLayerListlistener);
        }

        if (context != null) {
            context.addMapLayerListListener(mapLayerListlistener);
        }

        super.setContext(context);
    }

    @Override
    public void setMapArea(Envelope mapArea) {
        super.setMapArea(mapArea);

        updateOverLayer();
    }

    @Override
    protected void rectangleChanged(Rectangle newRect) {
        super.rectangleChanged(newRect);
        updateOverLayer();
    }

    
    
    //----------------------SELECTABLE MAP2D------------------------------------
    public void addSelectableLayer(MapLayer layer) {
        if (layer != null) {
            MapLayer copy = FF.duplicateLayer(layer);
            copy.setStyle(selectionStyle);
            selectionMapContext.addLayer(copy);
            copies.put(layer, copy);
        }
    }

    public void addSelectableLayer(MapLayer[] layers) {
        if (layers != null) {

            for (MapLayer layer : layers) {
                MapLayer copy = FF.duplicateLayer(layer);
                copy.setStyle(selectionStyle);
                selectionMapContext.addLayer(copy);
                copies.put(layer, copy);
            }
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

        Geometry geometry = gf.createPoint(new Coordinate(x, y));
        // org.opengis.geometry.Geometry geometry = new Point();
        findFeature(geometry);
    }

    protected SimpleFeature findFeature(Geometry geom, MapLayer layer) {
        Filter f = null;

        if ((context == null) || (selectionMapContext.getLayerCount() == 0) || (layer == null)) {
            return null;
        }

        try {
            String name = layer.getFeatureSource().getSchema().getDefaultGeometry().getLocalName();

            if (name == "") {
                name = "the_geom";
            }

            try {
                f = ff.intersects(ff.property(name), ff.literal(geom));
                applyStyleFilter(f);
                FeatureCollection col = layer.getFeatureSource().getFeatures(f);
                FeatureIterator fi = col.features();

                while (fi.hasNext()) {
                    SimpleFeature sf = fi.next();

                    for (Object obj : sf.getAttributes()) {
                        System.out.print(obj);
                    }
                    System.out.println("");
                }
                fi.close();

            } catch (Exception e) {
                e.printStackTrace();
            }




        } catch (IllegalFilterException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void findFeature(Geometry geometry) {
        Filter f = null;

        if ((context == null) || (selectionMapContext.getLayerCount() == 0)) {
            return;
        }

        try {

            for (MapLayer layer : selectionMapContext.getLayers()) {
                String name = layer.getFeatureSource().getSchema().getDefaultGeometry().getLocalName();

                if (name == "") {
                    name = "the_geom";
                }

                try {
                    f = ff.intersects(ff.property(name), ff.literal(geometry));
                    applyStyleFilter(f);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }



        } catch (IllegalFilterException e) {
            e.printStackTrace();
        }
        return;
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

    private class BufferComponent extends JComponent {

        private BufferedImage img;

        public void setBuffer(BufferedImage buf) {
            img = buf;
            repaint();
        }

        @Override
        public void paintComponent(Graphics g) {
            if (img != null) {
                g.drawImage(img, 0, 0, this);
            }
        }
        }
}





