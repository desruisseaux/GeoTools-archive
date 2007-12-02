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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.MouseInputListener;
import org.geotools.data.DataStore;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.memory.MemoryDataStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.gui.swing.map.MapConstants;
import org.geotools.gui.swing.map.MapConstants.EDIT_STATE;
import org.geotools.gui.swing.map.map2d.event.Map2DEditLayerEvent;
import org.geotools.gui.swing.map.map2d.event.Map2DEditStateEvent;
import org.geotools.gui.swing.map.map2d.listener.EditableMap2DListener;
import org.geotools.gui.swing.map.map2d.overLayer.OverLayer;
import org.geotools.gui.swing.misc.GeometryClassFilter;
import org.geotools.map.DefaultMapLayer;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.map.event.MapLayerListEvent;
import org.geotools.map.event.MapLayerListListener;
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
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;

/**
 *
 * @author Johann Sorel
 */
public class JDefaultEditableMap2D extends JDefaultSelectableMap2D implements EditableMap2D {

    protected MapConstants.EDIT_STATE editState = MapConstants.EDIT_STATE.NONE;
    private static final Coordinate[] EMPTY_COORDINATE_ARRAY = new Coordinate[0];
    private final BufferComponent editedPane = new BufferComponent();
    private final MouseListen mouseInputListener;
    private final MapLayerListListener mapLayerListlistener;
    private final MapLayer memoryLayer;
    private MapLayer editionLayer = null;
    private final Style editionStyle;

    public JDefaultEditableMap2D() {
        super();

        mouseInputListener = new MouseListen();
        mapLayerListlistener = new MapLayerListListen();
        addMouseListener(mouseInputListener);
        addMouseMotionListener(mouseInputListener);

        addMapOverLayer(editedPane);

        editionStyle = buildEditionStyle();
        memoryLayer = createMemoryLayer();

    }

    private Style buildEditionStyle() {

        StyleBuilder sb = new StyleBuilder();

        Fill fill = sb.createFill(Color.RED, 0.4f);
        Stroke stroke = sb.createStroke(Color.RED, 2);
        stroke.setOpacity(sb.literalExpression(0.6f));

        PolygonSymbolizer pls = sb.createPolygonSymbolizer(stroke, fill);

        Mark mark = sb.createMark("cross", fill, stroke);
        Graphic gra = sb.createGraphic();
        gra.setOpacity(sb.literalExpression(0.6f));
        gra.setMarks(new Mark[]{mark});
        gra.setSize(sb.literalExpression(14));
        PointSymbolizer ps = sb.createPointSymbolizer(gra);

        LineSymbolizer ls = sb.createLineSymbolizer(stroke);

        Rule r1 = sb.createRule(new Symbolizer[]{ps});
        r1.setFilter(new GeometryClassFilter(Point.class, MultiPoint.class));
        Rule r2 = sb.createRule(new Symbolizer[]{ls});
        r2.setFilter(new GeometryClassFilter(LineString.class, MultiLineString.class));
        Rule r3 = sb.createRule(new Symbolizer[]{pls});
        r3.setFilter(new GeometryClassFilter(Polygon.class, MultiPolygon.class));
        Rule r4 = sb.createRule(new Symbolizer[]{ps});
        r4.setFilter(new GeometryClassFilter(Polygon.class, MultiPolygon.class));



        Style stl = sb.createStyle();
        stl.addFeatureTypeStyle(sb.createFeatureTypeStyle(null, new Rule[]{r1, r2, r3}));
        stl.addFeatureTypeStyle(sb.createFeatureTypeStyle(null, new Rule[]{r4}));

//        SLDTransformer st = new SLDTransformer();
//
//            try {
//                String xml = st.transform(stl);            
//                JOptionPane.showInputDialog("haha",xml);
//            } catch (TransformerException ex) {
//                ex.printStackTrace();
//            }

        return stl;
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

    //--------------------Geometry Edition--------------------------------------
    private Point createPoint(Coordinate coord) {
        return GEOMETRY_FACTORY.createPoint(coord);
    }

    private MultiPoint createMultiPoint(List<Geometry> geoms) {
        List<Point> lst = new ArrayList<Point>();
        for (Geometry go : geoms) {
            if (go instanceof Point) {
                lst.add((Point) go);
            }
        }
        return GEOMETRY_FACTORY.createMultiPoint(lst.toArray(new Point[lst.size()]));
    }

    private LineString createLine(List<Coordinate> coords) {
        return GEOMETRY_FACTORY.createLineString(coords.toArray(EMPTY_COORDINATE_ARRAY));
    }

    private LinearRing createLinearRing(List<Coordinate> coords) {
        if (!(coords.get(0).equals2D(coords.get(coords.size() - 1)))) {
            Coordinate coo = new Coordinate(coords.get(0));
            coords.add(coo);
        }

        return GEOMETRY_FACTORY.createLinearRing(coords.toArray(EMPTY_COORDINATE_ARRAY));
    }

    private Polygon createPolygon(List<Coordinate> coords) {
        LinearRing ring = createLinearRing(coords);
        return GEOMETRY_FACTORY.createPolygon(ring, null);
    }

    private MultiPolygon createMultiPolygon(List<Geometry> geoms) {
        List<Polygon> lst = new ArrayList<Polygon>();
        for (Geometry go : geoms) {
            if (go instanceof Polygon) {
                lst.add((Polygon) go);
            }
        }
        return GEOMETRY_FACTORY.createMultiPolygon(lst.toArray(new Polygon[lst.size()]));
    }

    private MultiLineString createMultiLine(List<Geometry> geoms) {
        List<LineString> lst = new ArrayList<LineString>();
        for (Geometry go : geoms) {
            if (go instanceof LineString) {
                lst.add((LineString) go);
            }
        }
        return GEOMETRY_FACTORY.createMultiLineString(lst.toArray(new LineString[lst.size()]));
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

    private void validateModifiedGeometry(Geometry geo, SimpleFeature sf) {

        FeatureStore store;
        if (editionLayer.getFeatureSource() instanceof FeatureStore) {

            store = (FeatureStore) editionLayer.getFeatureSource();
            DefaultTransaction transaction = new DefaultTransaction("trans_maj");



            store.setTransaction(transaction);
            FilterFactory ff = CommonFactoryFinder.getFilterFactory(GeoTools.getDefaultHints());
            Filter filter = ff.id(Collections.singleton(ff.featureId(sf.getID())));
            FeatureType schema = store.getSchema();

            SimpleFeatureType featureType = editionLayer.getFeatureSource().getSchema();
            AttributeDescriptor geomAttribut = featureType.getDefaultGeometry();

            try {
                store.modifyFeatures(geomAttribut, geo, filter);
                transaction.commit();
            } catch (IOException ex) {
                ex.printStackTrace();
                try {
                    transaction.rollback();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


        }



    }

    private void stopEdition() {
        mouseInputListener.fireStateChange();
    }

    //---------------------Memory Layer-----------------------------------------
    private MapLayer createMemoryLayer() {

        MemoryDataStore mds = new MemoryDataStore();
        SimpleFeatureType featureType = null;

        MapLayer layer = null;

        try {
            featureType = DataUtilities.createType("memory", "geom:Geometry");
        } catch (SchemaException se) {
            se.printStackTrace();
        }

        if (featureType != null) {
            try {
                mds.createSchema(featureType);
                FeatureSource fs = ((DataStore) mds).getFeatureSource(((DataStore) mds).getTypeNames()[0]);
                layer = new DefaultMapLayer(fs, editionStyle);
            } catch (IOException se) {
                se.printStackTrace();
            }
        }

        return layer;
    }

    private void setMemoryLayerGeometry(List<Geometry> geoms) {

        if (mapArea != null && memoryLayer != null) {

            for (Geometry geom : geoms) {


                SimpleFeatureType featureType = memoryLayer.getFeatureSource().getSchema();
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
                DataStore data = memoryLayer.getFeatureSource().getDataStore();

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

        repaintMemoryLayer();
    }

    private void clearMemoryLayer() {

        try {
            FeatureStore fst = (FeatureStore) memoryLayer.getFeatureSource();
            fst.removeFeatures(Filter.INCLUDE);
        } catch (Exception e) {
            e.printStackTrace();
        }

        repaintMemoryLayer();
    }

    private void repaintMemoryLayer() {

        if (memoryLayer != null) {
            editedPane.setBuffer(getRenderingStrategy().createBufferImage(memoryLayer));
        } else {
            editedPane.setBuffer(null);
        }

    }

    //---------------------MAP 2D-----------------------------------------------
    @Override
    public void setMapArea(Envelope mapArea) {
        super.setMapArea(mapArea);

        repaintMemoryLayer();
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
            clearMemoryLayer();
            stopEdition();
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
        private boolean hasEditionGeometry = false;
        private boolean hasGeometryChanged = false;
        private SimpleFeature editFeature = null;
        //private List<Integer> editedNodes = new ArrayList<Integer>();
        private Map<Geometry,Integer[]> editedNodes = new HashMap<Geometry,Integer[]>();

        private void fireStateChange() {
            coords.clear();
            geoms.clear();
            nbRightClick = 0;
            inCreation = false;
            hasEditionGeometry = false;
            hasGeometryChanged = false;
            editFeature = null;
            editedNodes.clear();
            clearMemoryLayer();
            setMemoryLayerGeometry(geoms);
        }

        private void updateCreationGeoms() {
            int size = coords.size();

            if (inCreation) {
                if (geoms.size() > 0) {
                    geoms.remove(geoms.size() - 1);
                }
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

        private void grabGeometry(int mx, int my) {
            Geometry geo = mousePositionToGeometry(mx, my);
            Filter flt = getFeatureInGeometry(geo, editionLayer);

            FeatureCollection editgeoms = null;
            try {
                editgeoms = editionLayer.getFeatureSource().getFeatures(flt);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (editgeoms != null) {
                FeatureIterator fi = editgeoms.features();
                if (fi.hasNext()) {
                    SimpleFeature sf = fi.next();
                    Object obj = sf.getDefaultGeometry();

                    if (obj instanceof Geometry) {
                        hasEditionGeometry = true;
                        geoms.add((Geometry) obj);
                        editFeature = sf;
                    }
                }
            }
        }

        private void grabGeometryNode(int mx, int my) {
            editedNodes.clear();

            Geometry geo = geoms.get(0);
            Geometry mouseGeo = mousePositionToGeometry(mx, my);

            for (int i = 0,  n = geo.getNumGeometries(); i < n; i++) {
                Geometry subgeo = geo.getGeometryN(i);

                if (subgeo.intersects(mouseGeo)) {
                    Coordinate[] coos = subgeo.getCoordinates();

                    for (int j = 0,  m = coos.length; j < m; j++) {
                        Coordinate coo = coos[j];
                        Point p = createPoint(coo);
                        if (p.intersects(mouseGeo)) {

                            if ((j == 0 || j == m - 1) && (geo instanceof Polygon || geo instanceof MultiPolygon)) {
                                editedNodes.put(subgeo, new Integer[]{0,m-1});
                            } else {
                                editedNodes.put(subgeo, new Integer[]{j});
                            }
                        }
                    }
                }

            }


        }

        private void dragGeometryNode(int mx, int my) {
            Coordinate mouseCoord = toMapCoord(mx, my);

            Geometry geo = geoms.get(0);

            Set<Geometry> set = editedNodes.keySet();
            
            
            for( Iterator<Geometry> ite = set.iterator() ; ite.hasNext(); ){
                Geometry subgeo = ite.next();
                Integer[] nodeIndexes = editedNodes.get(subgeo);
                
                for (int index : nodeIndexes) {
                    subgeo.getCoordinates()[index].x = mouseCoord.x;
                    subgeo.getCoordinates()[index].y = mouseCoord.y;
                }
                
                subgeo.geometryChanged();
            }
            
//                        
//            
//            for (int editedNodeIndex : editedNodes) {
//                geo.getCoordinates()[editedNodeIndex].x = mouseCoord.x;
//                geo.getCoordinates()[editedNodeIndex].y = mouseCoord.y;
//                geo.geometryChanged();
//            }

            clearMemoryLayer();
            setMemoryLayerGeometry(geoms);

        }

        private void validateGeometryEdit() {
            if (!geoms.isEmpty() && hasGeometryChanged) {
                validateModifiedGeometry(geoms.get(0), editFeature);
            }
            hasEditionGeometry = false;
            hasGeometryChanged = false;
            editFeature = null;
            editedNodes.clear();
            inCreation = false;
        }

        public void mouseClicked(MouseEvent e) {

            if (actionState == MapConstants.ACTION_STATE.EDIT) {
                int button = e.getButton();
                Geometry geo = null;

                switch (editState) {

                    case EDIT:
                        if (button == MouseEvent.BUTTON1) {
                            if (!hasEditionGeometry) {
                                grabGeometry(e.getX(), e.getY());
                            }
                        } else if (button == MouseEvent.BUTTON3) {
                            validateGeometryEdit();
                            geoms.clear();
                            coords.clear();
                        }
                        clearMemoryLayer();
                        setMemoryLayerGeometry(geoms);
                        break;

                    case POINT:
                        geo = createPoint(toMapCoord(e.getX(), e.getY()));
                        editAddGeometry(new Geometry[]{geo});
                        clearMemoryLayer();
                        setMemoryLayerGeometry(geoms);
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
                        clearMemoryLayer();
                        setMemoryLayerGeometry(geoms);
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
                        clearMemoryLayer();
                        setMemoryLayerGeometry(geoms);
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
                                    if (geoms.size() > 0) {
                                        geoms.remove(geoms.size() - 1);
                                    }
                                    geo = createLine(coords);
                                    geoms.add(geo);
                                } else if (coords.size() > 0) {
                                    if (geoms.size() > 0) {
                                        geoms.remove(geoms.size() - 1);
                                    }
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
                        clearMemoryLayer();
                        setMemoryLayerGeometry(geoms);
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
                        clearMemoryLayer();
                        setMemoryLayerGeometry(geoms);
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
                                    if (geoms.size() > 0) {
                                        geoms.remove(geoms.size() - 1);
                                    }
                                    geo = createPolygon(coords);
                                    geoms.add(geo);
                                } else if (coords.size() > 0) {
                                    if (geoms.size() > 0) {
                                        geoms.remove(geoms.size() - 1);
                                    }
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
                        clearMemoryLayer();
                        setMemoryLayerGeometry(geoms);
                        break;

                }

            }
        }

        public void mousePressed(MouseEvent e) {

            if (actionState == MapConstants.ACTION_STATE.EDIT) {
                int button = e.getButton();

                switch (editState) {
                    case EDIT:
                        if (button == MouseEvent.BUTTON1) {

                            if (hasEditionGeometry) {
                                grabGeometryNode(e.getX(), e.getY());
                            }

                        }
                        break;
                }
            }
        }

        public void mouseReleased(MouseEvent e) {

            if (actionState == MapConstants.ACTION_STATE.EDIT) {
                int button = e.getButton();

                switch (editState) {
                    case EDIT:
                        editedNodes.clear();
                        break;
                }
            }

        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }

        public void mouseDragged(MouseEvent e) {

            if (actionState == MapConstants.ACTION_STATE.EDIT) {
                int button = e.getButton();

                switch (editState) {
                    case EDIT:

                        if (hasEditionGeometry && !editedNodes.isEmpty()) {
                            hasGeometryChanged = true;
                            dragGeometryNode(e.getX(), e.getY());
                        }

                        break;
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

    private class BufferComponent extends JComponent implements OverLayer {

        private BufferedImage img;
        private Rectangle oldone = null;
        private Rectangle newone = null;

        public void setBuffer(BufferedImage buf) {
            img = buf;
            repaint();
        }

        @Override
        public void paintComponent(Graphics g) {
            newone = getBounds();

            if (!newone.equals(oldone)) {
                oldone = newone;
                repaintMemoryLayer();
            } else if (img != null) {
                g.drawImage(img, 0, 0, this);
            }
        }

        public void refresh() {
        }

        public JComponent geComponent() {
            return this;
        }
    }
}

