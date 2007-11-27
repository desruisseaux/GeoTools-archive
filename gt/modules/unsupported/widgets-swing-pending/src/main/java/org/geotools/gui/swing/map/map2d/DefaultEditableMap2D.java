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
import com.vividsolutions.jts.geom.LinearRing;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.event.MouseInputListener;
import org.geotools.data.DataStore;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureStore;
import org.geotools.data.Transaction;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.map.MapLayer;
import org.geotools.map.event.MapLayerListEvent;
import org.geotools.map.event.MapLayerListListener;
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
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;

/**
 *
 * @author Johann Sorel
 */
public class DefaultEditableMap2D extends DefaultSelectableMap2D implements EditableMap2D {

    private final BufferComponent editedPane = new BufferComponent();
    private final MouseInputListener mouseInputListener;
    private final MapLayerListListener mapLayerListlistener;
    private MapLayer editionLayer = null;
    private Style editionStyle = null;

    public DefaultEditableMap2D() {
        this(new ShapefileRenderer());
    }

    public DefaultEditableMap2D(GTRenderer renderer) {
        super(renderer);

        mouseInputListener = new MouseListen();
        mapLayerListlistener = new MapLayerListListen();
        addMouseListener(mouseInputListener);
        addMouseMotionListener(mouseInputListener);
        layerPane.add(editedPane, new Integer(NEXT_OVER_LAYER_INDEX));
        NEXT_OVER_LAYER_INDEX++;

        buildEditionStyle();

    }

    private void buildEditionStyle() {

        StyleBuilder sb = new StyleBuilder();

        Fill fill = sb.createFill(Color.RED, 0f);
        Stroke stroke = sb.createStroke(Color.RED, 2);
        stroke.setOpacity(sb.literalExpression(0.6f));

        PolygonSymbolizer pls = sb.createPolygonSymbolizer(stroke, fill);

        Mark mark = sb.createMark("cross", fill, stroke);
        Graphic gra = sb.createGraphic();
        gra.setOpacity(sb.literalExpression(0.6f));
        gra.setMarks(new Mark[]{mark});
        gra.setSize(sb.literalExpression(12));
        PointSymbolizer ps = sb.createPointSymbolizer(gra);

        LineSymbolizer ls = sb.createLineSymbolizer(stroke);

        editionStyle = sb.createStyle();
        editionStyle.addFeatureTypeStyle(sb.createFeatureTypeStyle(pls));
        editionStyle.addFeatureTypeStyle(sb.createFeatureTypeStyle(ps));
        editionStyle.addFeatureTypeStyle(sb.createFeatureTypeStyle(ls));

    }

    private void applyStyleFilter(Filter f) {

        for (FeatureTypeStyle fts : editionStyle.getFeatureTypeStyles()) {
            for (Rule r : fts.getRules()) {
                r.setFilter(f);
            }
        }
    }

    private void updateOverLayer() {
        editedPane.setBuffer(createBufferImage(editionLayer));
    }

    private Coordinate toMapCoord(double mx, double my, double width, double height, Rectangle bounds) {
        double mapX = ((mx * width) / (double) bounds.width) + mapArea.getMinX();
        double mapY = (((bounds.getHeight() - my) * height) / (double) bounds.height) + mapArea.getMinY();
        return new Coordinate(mapX, mapY);
    }

    private void doMouseSelection(double mx, double my) {
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
            LinearRing[] lr2 = {lr1};
            Geometry geometry = gf.createPolygon(lr1, lr2);

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
            LinearRing[] lr2 = {lr1};
            Geometry geometry = gf.createPolygon(lr1, lr2);

            findFeature(geometry);
        }
    }

    private void stopEdition() {

    }

    //---------------------MAP 2D-----------------------------------------------
    @Override
    public void setMapArea(Envelope mapArea) {
        super.setMapArea(mapArea);

        //updateOverLayer();
    }

    //--------------------EDITABLE MAP2D---------------------------------------
    
    public void setEditedMapLayer(MapLayer layer) {
        editionLayer = layer;
    }

    public MapLayer getEditedMapLayer() {
        return editionLayer;
    }
    
    
    private void createPoint(int mx, int my) {
        if (mapArea != null && editionLayer != null) {
            
            //stuff to build the geometry
            Rectangle bounds = getBounds();
            double width = mapArea.getWidth();
            double height = mapArea.getHeight();
            Coordinate coord = toMapCoord(mx, my, width, height, bounds);
            Geometry geometry = gf.createPoint(coord);


            SimpleFeatureType featureType = editionLayer.getFeatureSource().getSchema();
            FeatureCollection collection = FeatureCollections.newCollection();
            Object[] values = new Object[featureType.getAttributeCount()];
            
            AttributeDescriptor geomAttribut = featureType.getDefaultGeometry();
            
            List<AttributeDescriptor> lst = featureType.getAttributes();
            for (int i=0, n=lst.size(); i<n; i++) {
                AttributeDescriptor desc = lst.get(i);
                
                if(desc.equals(geomAttribut)){
                    values[i] = geometry;
                }else{
                    values[i] = desc.getDefaultValue();
                }
            }

            //featureType.
            SimpleFeature sf = SimpleFeatureBuilder.build(featureType, values, null);
            collection.add(sf);

            //commit in shape
            DataStore data = editionLayer.getFeatureSource().getDataStore();

            DefaultTransaction transaction = null;
             FeatureStore store = null;
            try {
                String featureName = data.getTypeNames()[0]; // there is only one in a shapefile

                // Create the DefaultTransaction Object
                transaction = new DefaultTransaction();

                // Tell it the name of the shapefile it should look for in our DataStore
                store = (FeatureStore) data.getFeatureSource(featureName);
                
                // Then set the transaction for that FeatureStore
                store.setTransaction(transaction);

                store.addFeatures(collection);
                store.getTransaction().commit();
                store.getTransaction().close();
            } catch (Exception eek) {
                eek.printStackTrace();
                try {
                    store.getTransaction().rollback();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            

        }
    }

    //---------------------PRIVATE CLASSES--------------------------------------
    private class MouseListen implements MouseInputListener {

        public void mouseClicked(MouseEvent e) {

            switch (actionState) {
                case EDIT:
                    createPoint(e.getX(), e.getY());
                    return;
            }

        }

        public void mousePressed(MouseEvent e) {
        }

        public void mouseReleased(MouseEvent e) {
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }

        public void mouseDragged(MouseEvent e) {
        }

        public void mouseMoved(MouseEvent e) {
        }
    }

    private class MapLayerListListen implements MapLayerListListener {

        public void layerRemoved(MapLayerListEvent event) {

            if (editionLayer == event.getLayer()) {
                stopEdition();
            }
        }

        public void layerChanged(MapLayerListEvent event) {
        }

        public void layerMoved(MapLayerListEvent event) {
        }

        public void layerAdded(MapLayerListEvent event) {
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

