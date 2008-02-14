/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.gui.swing.map.map2d.handler;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import java.awt.Cursor;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.ImageIcon;
import org.geotools.data.DataStore;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.gui.swing.icon.IconBundle;
import org.geotools.gui.swing.map.map2d.EditableMap2D;
import org.geotools.gui.swing.map.map2d.TempMemoryDataStore;
import org.geotools.gui.swing.map.map2d.event.Map2DContextEvent;
import org.geotools.gui.swing.map.map2d.event.Map2DEditLayerEvent;
import org.geotools.gui.swing.map.map2d.event.Map2DMapAreaEvent;
import org.geotools.gui.swing.map.map2d.listener.EditableMap2DListener;
import org.geotools.gui.swing.map.map2d.listener.Map2DListener;
import org.geotools.gui.swing.map.map2d.listener.StrategyListener;
import org.geotools.gui.swing.map.map2d.strategy.RenderingStrategy;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.DefaultMapLayer;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;

/**
 *
 * @author johann sorel
 */
abstract class AbstractEditionHandler implements EditionHandler {

    protected final ImageIcon ICON;
    protected final String title;
    protected static final Coordinate[] EMPTY_COORDINATE_ARRAY = new Coordinate[0];
    protected final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();
    protected final SpecialMouseListener mouseInputListener;
    protected EditableMap2D map2D = null;
    protected boolean installed = false;
    protected Cursor CUR_EDIT;
    
    protected MapLayer memoryLayer;
    protected MapLayer edgesLayer;
    protected final EditListen editListen = new EditListen();

    public AbstractEditionHandler() {
        buildCursors();
        mouseInputListener = createMouseListener();
        ICON = createIcon();
        title = createTitle();
    }

    protected abstract SpecialMouseListener createMouseListener();

    protected abstract ImageIcon createIcon();

    protected abstract String createTitle();

    protected void buildCursors() {
        Toolkit tk = Toolkit.getDefaultToolkit();
        ImageIcon eci_edit = IconBundle.getResource().getIcon("16_edit");

        BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        img.getGraphics().drawImage(eci_edit.getImage(), 0, 0, null);
        CUR_EDIT = tk.createCustomCursor(img, new java.awt.Point(7, 1), "edit");
    }

    public void install(EditableMap2D map) {
        installed = true;
        map2D = map;

        //create the memory layers----------------------------------------------

        TempMemoryDataStore mds = new TempMemoryDataStore();
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
                layer = new DefaultMapLayer(fs, map2D.createStyle());
            } catch (IOException se) {
                se.printStackTrace();
            }
        }
        memoryLayer = layer;


        mds = new TempMemoryDataStore();
        featureType = null;
        layer = null;
        try {
            featureType = DataUtilities.createType("memory", "geom:Point");
        } catch (SchemaException se) {
            se.printStackTrace();
        }

        if (featureType != null) {
            try {
                mds.createSchema(featureType);
                FeatureSource fs = ((DataStore) mds).getFeatureSource(((DataStore) mds).getTypeNames()[0]);
                layer = new DefaultMapLayer(fs, map2D.createPointStyle());
            } catch (IOException se) {
                se.printStackTrace();
            }
        }
        edgesLayer = layer;

        map2D.setMemoryLayers(new MapLayer[]{memoryLayer,edgesLayer});
    }

    public void installListeners(EditableMap2D map2d) {
        map2D.addMap2DListener(editListen);
        map2D.getRenderingStrategy().addStrategyListener(editListen);
        map2D.addEditableMap2DListener(editListen);
        map2D.getComponent().addMouseListener(mouseInputListener);
        map2D.getComponent().addMouseMotionListener(mouseInputListener);
    }

    public void uninstallListeners() {
        map2D.removeMap2DListener(editListen);
        map2D.getRenderingStrategy().removeStrategyListener(editListen);
        map2D.removeEditableMap2DListener(editListen);
        map2D.getComponent().removeMouseListener(mouseInputListener);
        map2D.getComponent().removeMouseMotionListener(mouseInputListener);
    }

    public void uninstall() {
        map2D.setMemoryLayers(new MapLayer[0]);
        map2D = null;
        installed = false;
    }

    public boolean isInstalled() {
        return installed;
    }

    public String getTitle() {
        return title;
    }

    public ImageIcon getIcon() {
        return ICON;
    }

    //--------------------Geometry Edition--------------------------------------
    /**
     *  transform a mouse coordinate in JTS Geometry using the CRS of the mapcontext
     * @param mx : x coordinate of the mouse on the map (in pixel)
     * @param my : y coordinate of the mouse on the map (in pixel)
     * @return JTS geometry (corresponding to a square of 6x6 pixel around mouse coordinate)
     */
    protected Geometry mousePositionToGeometry(int mx, int my) {
        Coordinate[] coord = new Coordinate[5];
        int taille = 4;

        coord[0] = map2D.toMapCoord(mx - taille, my - taille);
        coord[1] = map2D.toMapCoord(mx - taille, my + taille);
        coord[2] = map2D.toMapCoord(mx + taille, my + taille);
        coord[3] = map2D.toMapCoord(mx + taille, my - taille);
        coord[4] = coord[0];

        LinearRing lr1 = GEOMETRY_FACTORY.createLinearRing(coord);
        return GEOMETRY_FACTORY.createPolygon(lr1, null);
    }

    protected Point createPoint(Coordinate coord) {
        return GEOMETRY_FACTORY.createPoint(coord);
    }

    protected MultiPoint createMultiPoint(List<Geometry> geoms) {
        List<Point> lst = new ArrayList<Point>();
        for (Geometry go : geoms) {
            if (go instanceof Point) {
                lst.add((Point) go);
            }
        }
        return GEOMETRY_FACTORY.createMultiPoint(lst.toArray(new Point[lst.size()]));
    }

    protected LineString createLine(List<Coordinate> coords) {
        return GEOMETRY_FACTORY.createLineString(coords.toArray(EMPTY_COORDINATE_ARRAY));
    }

    protected LinearRing createLinearRing(List<Coordinate> coords) {
        if (!(coords.get(0).equals2D(coords.get(coords.size() - 1)))) {
            Coordinate coo = new Coordinate(coords.get(0));
            coords.add(coo);
        }

        return GEOMETRY_FACTORY.createLinearRing(coords.toArray(EMPTY_COORDINATE_ARRAY));
    }

    protected Polygon createPolygon(List<Coordinate> coords) {
        LinearRing ring = createLinearRing(coords);
        return GEOMETRY_FACTORY.createPolygon(ring, null);
    }

    protected MultiPolygon createMultiPolygon(List<Geometry> geoms) {
        List<Polygon> lst = new ArrayList<Polygon>();
        for (Geometry go : geoms) {
            if (go instanceof Polygon) {
                lst.add((Polygon) go);
            }
        }
        return GEOMETRY_FACTORY.createMultiPolygon(lst.toArray(new Polygon[lst.size()]));
    }

    protected MultiLineString createMultiLine(List<Geometry> geoms) {
        List<LineString> lst = new ArrayList<LineString>();
        for (Geometry go : geoms) {
            if (go instanceof LineString) {
                lst.add((LineString) go);
            }
        }
        return GEOMETRY_FACTORY.createMultiLineString(lst.toArray(new LineString[lst.size()]));
    }

    protected synchronized void editAddGeometry(Geometry[] geoms) {

        MapLayer editionLayer = map2D.getEditedMapLayer();

        if (editionLayer != null) {

            for (Geometry geom : geoms) {

                SimpleFeatureType featureType = editionLayer.getFeatureSource().getSchema();
                FeatureCollection collection = FeatureCollections.newCollection();
                Object[] values = new Object[featureType.getAttributeCount()];

                AttributeDescriptor geomAttribut = featureType.getDefaultGeometry();

                geom = map2D.projectGeometry(geom, editionLayer);

                List<AttributeDescriptor> lst = featureType.getAttributes();
                for (int i = 0,  n = lst.size(); i < n; i++) {
                    AttributeDescriptor desc = lst.get(i);

                    if (desc.equals(geomAttribut)) {
                        values[i] = geom;
                    } else {
                        values[i] = desc.getDefaultValue();
                    }
                }

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

                    String name = editionLayer.getFeatureSource().getSchema().getTypeName();
                    try {
                        FeatureSource source = editionLayer.getFeatureSource().getDataStore().getFeatureSource(name);
                        store = (FeatureStore) source;
                    } catch (IOException e) {
                        // Tell it the name of the shapefile it should look for in our DataStore
                        store = (FeatureStore) data.getFeatureSource(featureName);
                    }



                    // Then set the transaction for that FeatureStore
                    store.setTransaction(transaction);

                    store.addFeatures(collection);
                    transaction.commit();
                } catch (Exception eek) {
                    eek.printStackTrace();
                    try {
                        store.getTransaction().rollback();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } finally {
                    transaction.close();
                }


            }

        }

    }

    protected synchronized void validateModifiedGeometry(Geometry geo, String ID) {

        MapLayer editionLayer = map2D.getEditedMapLayer();

        FeatureStore store;
        if (editionLayer.getFeatureSource() instanceof FeatureStore) {

            String name = editionLayer.getFeatureSource().getSchema().getTypeName();
            try {
                FeatureSource source = editionLayer.getFeatureSource().getDataStore().getFeatureSource(name);
                store = (FeatureStore) source;
            } catch (IOException e) {
                store = (FeatureStore) editionLayer.getFeatureSource();
            }

            DefaultTransaction transaction = new DefaultTransaction("trans_maj");

            store.setTransaction(transaction);
            FilterFactory ff = CommonFactoryFinder.getFilterFactory(GeoTools.getDefaultHints());
            Filter filter = ff.id(Collections.singleton(ff.featureId(ID)));


            SimpleFeatureType featureType = editionLayer.getFeatureSource().getSchema();
            AttributeDescriptor geomAttribut = featureType.getDefaultGeometry();

            geo = map2D.projectGeometry(geo, editionLayer);

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
            } finally {
                transaction.close();
            }



        }

    }

    protected void stopEdition() {
        mouseInputListener.fireStateChange();
    }

    //---------------------Memory Layer-----------------------------------------
    protected synchronized void setMemoryLayerGeometry(List<Geometry> geoms) {

        if (memoryLayer != null) {

            //memory layer--------------------------
            FeatureCollection collection = FeatureCollections.newCollection();

            for (Geometry geom : geoms) {

                //geom = projectGeometry(geom, memoryLayer);
                SimpleFeatureType featureType = memoryLayer.getFeatureSource().getSchema();
                Object[] values = new Object[featureType.getAttributeCount()];
                AttributeDescriptor geomAttribut = featureType.getDefaultGeometry();
                List<AttributeDescriptor> lst = featureType.getAttributes();

                for (int i = 0,  n = lst.size(); i < n; i++) {
                    AttributeDescriptor desc = lst.get(i);
                    values[i] = (desc.equals(geomAttribut)) ? geom : desc.getDefaultValue();
                }

                SimpleFeature sf = SimpleFeatureBuilder.build(featureType, values, null);
                collection.add(sf);
            }


            //commit
            FeatureStore store = (FeatureStore) memoryLayer.getFeatureSource();
            try {
                store.addFeatures(collection);
            } catch (Exception eek) {
                eek.printStackTrace();
            }


            //edges layer --------------------------------
            collection = FeatureCollections.newCollection();
            for (Geometry geom : geoms) {

                Coordinate[] coords = geom.getCoordinates();
                for (Coordinate coord : coords) {

                    //geom = projectGeometry(geom, memoryLayer);
                    SimpleFeatureType featureType = edgesLayer.getFeatureSource().getSchema();
                    Object[] values = new Object[featureType.getAttributeCount()];
                    AttributeDescriptor geomAttribut = featureType.getDefaultGeometry();

                    List<AttributeDescriptor> lst = featureType.getAttributes();
                    for (int i = 0,  n = lst.size(); i < n; i++) {
                        AttributeDescriptor desc = lst.get(i);

                        if (desc.equals(geomAttribut)) {
                            values[i] = GEOMETRY_FACTORY.createPoint(coord);
                        } else {
                            values[i] = desc.getDefaultValue();
                        }
                    }

                    //featureType.
                    SimpleFeature sf = SimpleFeatureBuilder.build(featureType, values, null);
                    collection.add(sf);

                }

                //commit 
                store = (FeatureStore) edgesLayer.getFeatureSource();
                try {
                    store.addFeatures(collection);
                } catch (Exception eek) {
                    eek.printStackTrace();
                }

            }
        }

        map2D.repaintMemoryDecoration();
    }

    protected synchronized void clearMemoryLayer() {

        try {
            FeatureStore fst = (FeatureStore) memoryLayer.getFeatureSource();
            fst.removeFeatures(Filter.INCLUDE);
            fst = (FeatureStore) edgesLayer.getFeatureSource();
            fst.removeFeatures(Filter.INCLUDE);
        } catch (Exception e) {
            e.printStackTrace();
        }

        map2D.repaintMemoryDecoration();
    }

    protected synchronized void reprojectEditionLayer() {

        List<Geometry> geoms = new ArrayList<Geometry>();
        List<Geometry> geomsOut = new ArrayList<Geometry>();

        try {
            FeatureCollection col = memoryLayer.getFeatureSource().getFeatures();
            FeatureIterator ite = col.features();

            while (ite.hasNext()) {
                SimpleFeature sf = ite.next();
                geoms.add((Geometry) sf.getDefaultGeometry());
            }
            ite.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        for (Geometry geo : geoms) {
            geomsOut.add(map2D.projectGeometry(geo, map2D.getRenderingStrategy().getContext().getCoordinateReferenceSystem(), map2D.getRenderingStrategy().getContext().getCoordinateReferenceSystem()));
            //geomsOut.add(map2D.projectGeometry(geo, memoryMapContext.getCoordinateReferenceSystem(), map2D.getRenderingStrategy().getContext().getCoordinateReferenceSystem()));
        }

        clearMemoryLayer();
        setMemoryLayerGeometry(geomsOut);

    }

    

    //----------------Private classes-------------------------------------------
    protected class EditListen implements EditableMap2DListener, Map2DListener, StrategyListener {

        public void mapEditLayerChanged(Map2DEditLayerEvent event) {
            mouseInputListener.fireStateChange();
            clearMemoryLayer();
            stopEdition();
        }

        public void mapStrategyChanged(RenderingStrategy oldStrategy, RenderingStrategy newStrategy) {
            if (oldStrategy != null) {
                oldStrategy.removeStrategyListener(this);
            }
            if (newStrategy != null) {
                newStrategy.addStrategyListener(this);
            }
        }

        public void setRendering(boolean rendering) {
        }

        public void mapAreaChanged(Map2DMapAreaEvent event) {
        }

        public void mapContextChanged(Map2DContextEvent event) {
        }

        public void editionHandlerChanged(EditionHandler handler) {
        }
    }
}
