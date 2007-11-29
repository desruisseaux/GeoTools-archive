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
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.event.MouseInputListener;
import org.geotools.data.DataStore;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.memory.MemoryDataStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.gui.swing.map.MapConstants;
import org.geotools.gui.swing.map.MapConstants.EDIT_STATE;
import org.geotools.gui.swing.map.map2d.event.Map2DEditLayerEvent;
import org.geotools.gui.swing.map.map2d.event.Map2DEditStateEvent;
import org.geotools.gui.swing.map.map2d.listener.EditableMap2DListener;
import org.geotools.gui.swing.misc.FacilitiesFactory;
import org.geotools.map.DefaultMapLayer;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.map.event.MapLayerListEvent;
import org.geotools.map.event.MapLayerListListener;
import org.geotools.referencing.CRS;
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
import org.opengis.filter.Filter;

/**
 *
 * @author Johann Sorel
 */
public class JDefaultEditableMap2D extends JDefaultSelectableMap2D implements EditableMap2D {

    private static final Coordinate[] EMPTY_COORDINATE_ARRAY = new Coordinate[0];
    private final FacilitiesFactory FF = new FacilitiesFactory();
    private final BufferComponent editedPane = new BufferComponent();
    private final MouseListen mouseInputListener;
    private final MapLayerListListener mapLayerListlistener;
    protected MapConstants.EDIT_STATE editState = MapConstants.EDIT_STATE.NONE;
    private MapContext editionContext = new OneLayerContext();
    private MapLayer editionLayer = null;
    private MapLayer copyLayer = null;
    private Style editionStyle = null;

    public JDefaultEditableMap2D() {
        this(new ShapefileRenderer());
    }

    public JDefaultEditableMap2D(GTRenderer renderer) {
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

    private MapLayer createCopyLayer(MapLayer editionLayer) {

        MemoryDataStore mds = new MemoryDataStore();


        Class jtsClass = null;
        SimpleFeatureType featureType = null;

        jtsClass = editionLayer.getFeatureSource().getSchema().getDefaultGeometry().getType().getBinding();
        if (jtsClass != null) {
            try {
                featureType = DataUtilities.createType("memory", "geom:Geometry");
//                if (jtsClass.equals(Point.class)) {
//                    featureType = DataUtilities.createType("memory", "geom:Point");
//                } else if (jtsClass.equals(MultiPoint.class)) {
//                    featureType = DataUtilities.createType("memory", "geom:MultiPoint");
//                } else if (jtsClass.equals(LineString.class)) {
//                    featureType = DataUtilities.createType("memory", "geom:LineString");
//                } else if (jtsClass.equals(MultiLineString.class)) {
//                    featureType = DataUtilities.createType("memory", "geom:MultiLineString");
//                } else if (jtsClass.equals(Polygon.class)) {
//                    featureType = DataUtilities.createType("memory", "geom:Polygon");
//                } else if (jtsClass.equals(MultiPolygon.class)) {
//                    featureType = DataUtilities.createType("memory", "geom:MultiPolygon");
//                }
            } catch (SchemaException se) {
                se.printStackTrace();
            }
        }

        try {
            mds.createSchema(featureType);
            FeatureSource fs = ((DataStore) mds).getFeatureSource(((DataStore) mds).getTypeNames()[0]);
            return new DefaultMapLayer(fs, editionStyle);
        } catch (IOException se) {
            se.printStackTrace();
        }

        return null;
    }

    private void updateOverLayer() {

        if (copyLayer != null) {
            editedPane.setBuffer(createBufferImage(copyLayer));
        } else {
            editedPane.setBuffer(null);
        }

    }

    private Coordinate toMapCoord(double mx, double my, double width, double height, Rectangle bounds) {
        double mapX = ((mx * width) / (double) bounds.width) + mapArea.getMinX();
        double mapY = (((bounds.getHeight() - my) * height) / (double) bounds.height) + mapArea.getMinY();
        return new Coordinate(mapX, mapY);
    }

    private Coordinate toMapCoord(int mx, int my) {
        Rectangle bounds = getBounds();
        double width = mapArea.getWidth();
        double height = mapArea.getHeight();
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

    private Point createPoint(Coordinate coord) {
        return gf.createPoint(coord);
    }

    private MultiPoint createMultiPoint(List<Geometry> geoms) {
        List<Point> lst = new ArrayList<Point>();
        for(Geometry go : geoms){
            if(go instanceof Point){
                lst.add((Point)go);
            }
        }
        return gf.createMultiPoint(lst.toArray(new Point[lst.size()]));        
    }
    
    private LineString createLine(List<Coordinate> coords) {
        return gf.createLineString(coords.toArray(EMPTY_COORDINATE_ARRAY));
    }

    private LinearRing createLinearRing(List<Coordinate> coords) {
        if (!(coords.get(0).equals2D(coords.get(coords.size() - 1)))) {
            Coordinate coo = new Coordinate(coords.get(0));
            coords.add(coo);
        }

        return gf.createLinearRing(coords.toArray(EMPTY_COORDINATE_ARRAY));
    }

    private Polygon createPolygon(List<Coordinate> coords) {
        LinearRing ring = createLinearRing(coords);
        return gf.createPolygon(ring, null);
    }

    private MultiPolygon createMultiPolygon(List<Geometry> geoms) {
        List<Polygon> lst = new ArrayList<Polygon>();
        for(Geometry go : geoms){
            if(go instanceof Polygon){
                lst.add((Polygon)go);
            }
        }
        return gf.createMultiPolygon(lst.toArray(new Polygon[lst.size()]));
    }

    private MultiLineString createMultiLine(List<Geometry> geoms) {
        List<LineString> lst = new ArrayList<LineString>();
        for(Geometry go : geoms){
            if(go instanceof LineString){
                lst.add((LineString)go);
            }
        }
        return gf.createMultiLineString(lst.toArray(new LineString[lst.size()]));        
    }

    private void editAddGeometry(Geometry[] geoms) {

        if (mapArea != null && editionLayer != null) {

            for (Geometry geom : geoms) {


                SimpleFeatureType featureType = editionLayer.getFeatureSource().getSchema();
                FeatureCollection collection = FeatureCollections.newCollection();
                Object[] values = new Object[featureType.getAttributeCount()];

                AttributeDescriptor geomAttribut = featureType.getDefaultGeometry();

                List<AttributeDescriptor> lst = featureType.getAttributes();
                for (int i = 0,  n = lst.size(); i < n; i++) {
                    AttributeDescriptor desc = lst.get(i);

                    if (desc.equals(geomAttribut)) {
                        values[i] = geom;
                    } else {
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

    }

    private void updateMemoryGeometry(List<Geometry> geoms) {

        if (mapArea != null && copyLayer != null) {

            for (Geometry geom : geoms) {


                SimpleFeatureType featureType = copyLayer.getFeatureSource().getSchema();
                FeatureCollection collection = FeatureCollections.newCollection();
                Object[] values = new Object[featureType.getAttributeCount()];

                AttributeDescriptor geomAttribut = featureType.getDefaultGeometry();

                List<AttributeDescriptor> lst = featureType.getAttributes();
                for (int i = 0,  n = lst.size(); i < n; i++) {
                    AttributeDescriptor desc = lst.get(i);

                    if (desc.equals(geomAttribut)) {
                        values[i] = geom;
                    } else {
                        values[i] = desc.getDefaultValue();
                    }
                }

                //featureType.
                SimpleFeature sf = SimpleFeatureBuilder.build(featureType, values, null);
                collection.add(sf);

                //commit in shape
                DataStore data = copyLayer.getFeatureSource().getDataStore();

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

    }

    private void cleanMemoryGeometry() {

        copyLayer = createCopyLayer(editionLayer);
        updateOverLayer();
    }

    private void stopEdition() {

    }

    private void fireEditStateChanged(MapConstants.EDIT_STATE oldone, MapConstants.EDIT_STATE newone) {
        Map2DEditStateEvent mce = new Map2DEditStateEvent(this, oldone, newone);

        EditableMap2DListener[] lst = getEditableMap2DListeners();

        for (EditableMap2DListener l : lst) {
            l.mapEditStateChanged(mce);
        }

    }

    private void fireEditLayerChanged(MapLayer oldone, MapLayer newone) {
        Map2DEditLayerEvent mce = new Map2DEditLayerEvent(this, oldone, newone);

        EditableMap2DListener[] lst = getEditableMap2DListeners();

        for (EditableMap2DListener l : lst) {
            l.mapEditLayerChanged(mce);
        }

    }

    //---------------------MAP 2D-----------------------------------------------
    @Override
    public void setMapArea(Envelope mapArea) {
        super.setMapArea(mapArea);

    //updateOverLayer();
    }

    @Override
    public void setContext(MapContext newcontext) {

        if (this.context != null) {
            this.context.removeMapLayerListListener(mapLayerListlistener);
        }

        if (newcontext != null) {
            newcontext.addMapLayerListListener(mapLayerListlistener);
        }

        super.setContext(newcontext);
    }

    //--------------------EDITABLE MAP2D----------------------------------------
    public void setEditedMapLayer(MapLayer layer) {


        if (editionLayer != layer) {
            fireEditLayerChanged(editionLayer, layer);
            mouseInputListener.fireStateChange();
            editionLayer = layer;
            copyLayer = FF.duplicateLayer(layer);
            copyLayer.setStyle(editionStyle);

            copyLayer = createCopyLayer(editionLayer);
        }

    }

    public MapLayer getEditedMapLayer() {
        return editionLayer;
    }

    public void setEditState(EDIT_STATE state) {

        if (editState != state) {
            fireEditStateChanged(editState, state);
            mouseInputListener.fireStateChange();
            editState = state;
        }

    }

    public EDIT_STATE getEditState() {
        return editState;
    }

    public void addEditableMap2DListener(EditableMap2DListener listener) {
        MAP2DLISTENERS.add(EditableMap2DListener.class, listener);
    }

    public void removeEditableMap2DListener(EditableMap2DListener listener) {
        MAP2DLISTENERS.remove(EditableMap2DListener.class, listener);
    }

    public EditableMap2DListener[] getEditableMap2DListeners() {
        return MAP2DLISTENERS.getListeners(EditableMap2DListener.class);
    }

    //---------------------PRIVATE CLASSES--------------------------------------
    private class MouseListen implements MouseInputListener {

        private final List<Coordinate> coords = new ArrayList<Coordinate>();
        private final List<Geometry> geoms = new ArrayList<Geometry>();
        private int nbRightClick = 0;
        private boolean inCreation = false;

        private void fireStateChange() {
            coords.clear();
            geoms.clear();
            nbRightClick = 0;
        }

        private void updateCreationGeoms() {
            int size = coords.size();

            if (inCreation) {
                if(geoms.size()>0)
                geoms.remove(geoms.size() - 1);
            }
            inCreation = true;

            switch (size) {
                case 0:
                    break;
                case 1:
                    geoms.add(createPoint(coords.get(0)));
                    break;
                case 2:
                    geoms.add(createLine(coords));
                    break;
                default:
                    geoms.add(createLine(coords));
                    break;
            }
        }

        public void mouseClicked(MouseEvent e) {

            if (actionState == MapConstants.ACTION_STATE.EDIT) {
                int button = e.getButton();
                Geometry geo = null;

                switch (editState) {


                    case POINT:
                        geo = createPoint(toMapCoord(e.getX(), e.getY()));
                        editAddGeometry(new Geometry[]{geo});
                        break;

                    case MULTI_POINT:
                        if (button == MouseEvent.BUTTON1) {
                            geo = createPoint(toMapCoord(e.getX(), e.getY()));
                            geoms.add(geo);
                            updateCreationGeoms();

                        } else if (button == MouseEvent.BUTTON3) {
                            if (geoms.size() > 0) {
                                geo = createMultiPoint(geoms);
                                editAddGeometry(new Geometry[]{geo});
                                geoms.clear();
                            }
                            coords.clear();
                        }
                        break;


                    case LINE:
                        if (button == MouseEvent.BUTTON1) {
                            coords.add(toMapCoord(e.getX(), e.getY()));
                            updateCreationGeoms();
                        } else if (button == MouseEvent.BUTTON3) {
                            inCreation = false;
                            if (coords.size() > 1) {
                                geo = createLine(coords);
                                editAddGeometry(new Geometry[]{geo});
                                geoms.clear();
                            }
                            coords.clear();
                        }
                        break;


                    case MULTI_LINE:
                        if (button == MouseEvent.BUTTON1) {
                            nbRightClick = 0;
                            coords.add(toMapCoord(e.getX(), e.getY()));
                            updateCreationGeoms();

                        } else if (button == MouseEvent.BUTTON3) {
                            nbRightClick++;
                            if (nbRightClick == 1) {
                                inCreation = false;
                                if (coords.size() > 1) {                                    
                                    if(geoms.size()>0) geoms.remove(geoms.size() - 1);
                                    geo = createLine(coords);
                                    geoms.add(geo);
                                }else if(coords.size() > 0){
                                    if(geoms.size()>0) geoms.remove(geoms.size() - 1);
                                }
                            } else {
                                if (geoms.size() > 0) {
                                    geo = createMultiLine(geoms);
                                    editAddGeometry(new Geometry[]{geo});
                                    nbRightClick = 0;
                                    geoms.clear();
                                }
                            }
                            coords.clear();
                        }
                        break;

                    case POLYGON:
                        if (button == MouseEvent.BUTTON1) {
                            coords.add(toMapCoord(e.getX(), e.getY()));
                            updateCreationGeoms();
                        } else if (button == MouseEvent.BUTTON3) {
                            inCreation = false;
                            if (coords.size() > 2) {
                                geo = createPolygon(coords);
                                editAddGeometry(new Geometry[]{geo});                                
                                geoms.clear();
                            }
                            coords.clear();
                        }
                        break;

                    case MULTI_POLYGON:
                        if (button == MouseEvent.BUTTON1) {
                            nbRightClick = 0;
                            coords.add(toMapCoord(e.getX(), e.getY()));
                            updateCreationGeoms();
                        } else if (button == MouseEvent.BUTTON3) {
                            nbRightClick++;
                            if (nbRightClick == 1) {
                                inCreation = false;
                                if (coords.size() > 2) {
                                    if(geoms.size()>0) geoms.remove(geoms.size() - 1);
                                    geo = createPolygon(coords);
                                    geoms.add(geo);
                                }else if(coords.size() > 0){
                                    if(geoms.size()>0) geoms.remove(geoms.size() - 1);
                                }
                            } else {
                                if (geoms.size() > 0) {
                                    geo = createMultiPolygon(geoms);
                                    editAddGeometry(new Geometry[]{geo});
                                    nbRightClick = 0;                                    
                                    geoms.clear();                                    
                                }
                            }
                            coords.clear();
                        }
                        break;

                }
                cleanMemoryGeometry();
                updateMemoryGeometry(geoms);
                updateOverLayer();
            }
        }

        public void mousePressed(MouseEvent e) {

            if (actionState == MapConstants.ACTION_STATE.EDIT) {
                int button = e.getButton();

                switch (editState) {

                }
            }
        }

        public void mouseReleased(MouseEvent e) {
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }

        public void mouseDragged(MouseEvent e) {

            if (actionState == MapConstants.ACTION_STATE.EDIT) {

                switch (editState) {
                }
            }
        }

        public void mouseMoved(MouseEvent e) {

            if (actionState == MapConstants.ACTION_STATE.EDIT) {

                switch (editState) {
                }
            }

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

