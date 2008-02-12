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
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.event.MouseInputListener;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.FileDataStoreFactorySpi;
import org.geotools.data.memory.MemoryDataStore;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.indexed.IndexedShapefileDataStoreFactory;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.gui.swing.icon.IconBundle;
import org.geotools.gui.swing.map.MapConstants;
import org.geotools.gui.swing.map.MapConstants.EDIT_STATE;
import org.geotools.gui.swing.map.map2d.decoration.MapDecoration;
import org.geotools.gui.swing.map.map2d.event.Map2DContextEvent;
import org.geotools.gui.swing.map.map2d.event.Map2DEditLayerEvent;
import org.geotools.gui.swing.map.map2d.event.Map2DEditStateEvent;
import org.geotools.gui.swing.map.map2d.event.Map2DMapAreaEvent;
import org.geotools.gui.swing.map.map2d.listener.EditableMap2DListener;
import org.geotools.gui.swing.map.map2d.strategy.RenderingStrategy;
import org.geotools.gui.swing.map.map2d.strategy.SingleBufferedImageStrategy;
import org.geotools.gui.swing.misc.GeometryClassFilter;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.DefaultMapLayer;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.map.event.MapLayerListEvent;
import org.geotools.map.event.MapLayerListListener;
import org.geotools.referencing.crs.DefaultGeocentricCRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
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
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;


/**
 * Default implementation of EditableMap2D
 * @author Johann Sorel
 */
public class JDefaultEditableMap2D_temp extends JDefaultSelectableMap2D implements EditableMap2D {

    /**
     * Edition state of the map widget
     */
    protected MapConstants.EDIT_STATE editState = MapConstants.EDIT_STATE.NONE;
    private static final Coordinate[] EMPTY_COORDINATE_ARRAY = new Coordinate[0];
    private final MouseListen mouseInputListener;
    private final MapLayerListListener mapLayerListlistener;
    private final SingleBufferedImageStrategy memoryStrategy = new SingleBufferedImageStrategy();
    private final MapContext memoryMapContext = new DefaultMapContext(DefaultGeographicCRS.WGS84);
    private final BufferComponent memoryPane = new BufferComponent();
    private MapLayer memoryLayer = null;
    private MapLayer edgesLayer = null;
    private MapLayer editionLayer = null;
    protected Cursor CUR_EDIT;
    protected Color editionStyleColor = Color.RED;

    /**
     * create a default JDefaultEditableMap2D
     */
    public JDefaultEditableMap2D_temp() {
        super();

        mouseInputListener = new MouseListen();
        mapLayerListlistener = new MapLayerListListen();
        addMouseListener(mouseInputListener);
        addMouseMotionListener(mouseInputListener);
        addMapDecoration(memoryPane);

        buildCursors();


        //create the memory layers----------------------------------------------
        
        memoryLayer = new DefaultMapLayer(FeatureCollections.newCollection(), createStyle());
        edgesLayer = new DefaultMapLayer(FeatureCollections.newCollection(), createPointStyle());
        

//        File memory_edit = new File("memory_edit.shp");
//        Map<String, URL> params = null;
//
//        try {
//            params = Collections.singletonMap("url", memory_edit.toURI().toURL());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        if (memory_edit.exists()) {
//            System.out.println("existe");
//            try {
//                DataStore sds = DataStoreFinder.getDataStore(params);
//                FeatureSource fs = sds.getFeatureSource(sds.getTypeNames()[0]);
//                memoryLayer = new DefaultMapLayer(fs, createStyle());
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        } else {
//            try {
//                
//                
//                
//                FileDataStoreFactorySpi factory = new IndexedShapefileDataStoreFactory();
//                ShapefileDataStore myData = (ShapefileDataStore) factory.createNewDataStore(params);
//                String buffer = "geom:Geometry";
//                SimpleFeatureType featureType = DataUtilities.createType("memory", buffer);
//                myData.createSchema(featureType);
//                myData.dispose();
//                DataStore sds = DataStoreFinder.getDataStore(params);
//                FeatureSource fs = sds.getFeatureSource(sds.getTypeNames()[0]);
//                memoryLayer = new DefaultMapLayer(fs, createStyle());
//            } catch (Exception e) {
//                e.printStackTrace();
//            }


//            System.out.println("cree");
//            MemoryDataStore mds = new MemoryDataStore();
//            SimpleFeatureType featureType = null;
//            MapLayer layer = null;
//            try {
//                featureType = DataUtilities.createType("memory", "geom:Geometry");
//            } catch (SchemaException se) {
//                se.printStackTrace();
//            }
//
//            if (featureType != null) {
//                try {
//                    mds.createSchema(featureType);
//                    FeatureSource fs = ((DataStore) mds).getFeatureSource(((DataStore) mds).getTypeNames()[0]);
//                    layer = new DefaultMapLayer(fs, createStyle());
//                } catch (IOException se) {
//                    se.printStackTrace();
//                }
//            }
//            memoryLayer = layer;
//        }



//        File edges_edit = new File("memory_edit.shp");
//        try {
//            params = Collections.singletonMap("url", edges_edit.toURI().toURL());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        if (memory_edit.exists()) {
//            try{
//            System.out.println("existe");
//            DataStore sds = DataStoreFinder.getDataStore(params);
//            FeatureSource fs = sds.getFeatureSource(sds.getTypeNames()[0]);
//            edgesLayer = new DefaultMapLayer(fs, createStyle());
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        } else {
//            try{
//            FileDataStoreFactorySpi factory = new IndexedShapefileDataStoreFactory();
//            ShapefileDataStore myData = (ShapefileDataStore) factory.createNewDataStore(params);
//            String buffer = "geom:Geometry";
//            SimpleFeatureType featureType = DataUtilities.createType("memory", buffer);
//            myData.createSchema(featureType);
//            myData.dispose();
//            DataStore sds = DataStoreFinder.getDataStore(params);
//                FeatureSource fs = sds.getFeatureSource(sds.getTypeNames()[0]);
//                edgesLayer = new DefaultMapLayer(fs, createStyle());
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }



//
//        mds = new MemoryDataStore();
//        featureType = null;
//        layer = null;
//        try {
//            featureType = DataUtilities.createType("memory", "geom:Point");
//        } catch (SchemaException se) {
//            se.printStackTrace();
//        }
//
//        if (featureType != null) {
//            try {
//                mds.createSchema(featureType);
//                FeatureSource fs = ((DataStore) mds).getFeatureSource(((DataStore) mds).getTypeNames()[0]);
//                layer = new DefaultMapLayer(fs, createPointStyle());
//            } catch (IOException se) {
//                se.printStackTrace();
//            }
//        }
//        edgesLayer = layer;


        // memory strategy------------------------------------------------------
        memoryMapContext.addLayer(memoryLayer);
        memoryMapContext.addLayer(edgesLayer);
        memoryStrategy.setContext(memoryMapContext);
        memoryStrategy.setAutoRefreshEnabled(false);


    }

    private void buildCursors() {
        Toolkit tk = Toolkit.getDefaultToolkit();
        ImageIcon eci_edit = IconBundle.getResource().getIcon("16_edit");


        BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        img.getGraphics().drawImage(eci_edit.getImage(), 0, 0, null);
        CUR_EDIT = tk.createCustomCursor(img, new java.awt.Point(7, 1), "edit");

    }

    private Style createPointStyle() {
        Fill fill = STYLE_BUILDER.createFill(editionStyleColor, 1f);
        Stroke stroke = STYLE_BUILDER.createStroke(editionStyleColor, 1);
        stroke.setOpacity(STYLE_BUILDER.literalExpression(1f));

        Mark mark = STYLE_BUILDER.createMark("cross", fill, stroke);
        Graphic gra = STYLE_BUILDER.createGraphic();
        gra.setOpacity(STYLE_BUILDER.literalExpression(1f));
        gra.setMarks(new Mark[]{mark});
        gra.setSize(STYLE_BUILDER.literalExpression(14));

        PointSymbolizer ps = STYLE_BUILDER.createPointSymbolizer(gra);

        Style pointSelectionStyle = STYLE_BUILDER.createStyle();
        pointSelectionStyle.addFeatureTypeStyle(STYLE_BUILDER.createFeatureTypeStyle(ps));

        return pointSelectionStyle;
    }

    private Style createStyle() {
        Fill fill = STYLE_BUILDER.createFill(editionStyleColor, 0.4f);
        Stroke stroke = STYLE_BUILDER.createStroke(editionStyleColor, 2);
        stroke.setOpacity(STYLE_BUILDER.literalExpression(0.6f));

        PolygonSymbolizer pls = STYLE_BUILDER.createPolygonSymbolizer(stroke, fill);

        Mark mark = STYLE_BUILDER.createMark("circle", fill, stroke);
        Graphic gra = STYLE_BUILDER.createGraphic();
        gra.setOpacity(STYLE_BUILDER.literalExpression(0.6f));
        gra.setMarks(new Mark[]{mark});
        gra.setSize(STYLE_BUILDER.literalExpression(14));
        PointSymbolizer ps = STYLE_BUILDER.createPointSymbolizer(gra);

        LineSymbolizer ls = STYLE_BUILDER.createLineSymbolizer(stroke);

        Rule r2 = STYLE_BUILDER.createRule(new Symbolizer[]{ls});
        r2.setFilter(new GeometryClassFilter(LineString.class, MultiLineString.class));
        Rule r3 = STYLE_BUILDER.createRule(new Symbolizer[]{pls});
        r3.setFilter(new GeometryClassFilter(Polygon.class, MultiPolygon.class));


        Style editionStyle = STYLE_BUILDER.createStyle();
        editionStyle.addFeatureTypeStyle(STYLE_BUILDER.createFeatureTypeStyle(null, new Rule[]{r2, r3}));

        return editionStyle;
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

    private synchronized void editAddGeometry(Geometry[] geoms) {

        Envelope mapArea = memoryStrategy.getMapArea();

        if (mapArea != null && editionLayer != null) {

            for (Geometry geom : geoms) {


                SimpleFeatureType featureType = editionLayer.getFeatureSource().getSchema();
                FeatureCollection collection = FeatureCollections.newCollection();
                Object[] values = new Object[featureType.getAttributeCount()];

                AttributeDescriptor geomAttribut = featureType.getDefaultGeometry();

                geom = projectGeometry(geom, editionLayer);

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

    private synchronized void validateModifiedGeometry(Geometry geo, SimpleFeature editfeature) {

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
            Filter filter = ff.id(Collections.singleton(ff.featureId(editfeature.getID())));


            SimpleFeatureType featureType = editionLayer.getFeatureSource().getSchema();
            AttributeDescriptor geomAttribut = featureType.getDefaultGeometry();

            geo = projectGeometry(geo, editionLayer);

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

    private void stopEdition() {
        mouseInputListener.fireStateChange();
    }

    //---------------------Memory Layer-----------------------------------------
    private synchronized void setMemoryLayerGeometry(List<Geometry> geoms) {


        

        Envelope mapArea = memoryStrategy.getMapArea();

        if (mapArea != null && memoryLayer != null) {

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
//            FeatureStore store = (FeatureStore) memoryLayer.getFeatureSource();
//            try {
//                store.addFeatures(collection);
//            } catch (Exception eek) {
//                eek.printStackTrace();
//            }
            DataStore data = memoryLayer.getFeatureSource().getDataStore();
            DefaultTransaction transaction = null;
            FeatureStore store = null;
            try {
                String featureName = data.getTypeNames()[0];
                transaction = new DefaultTransaction();
                store = (FeatureStore) data.getFeatureSource(featureName);
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
//                store = (FeatureStore) edgesLayer.getFeatureSource();
//                try {
//                    store.addFeatures(collection);
//                } catch (Exception eek) {
//                    eek.printStackTrace();
//                }
                data = edgesLayer.getFeatureSource().getDataStore();
                transaction = null;
                store = null;
                try {
                    String featureName = data.getTypeNames()[0];
                    transaction = new DefaultTransaction();
                    store = (FeatureStore) data.getFeatureSource(featureName);
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

        repaintMemoryLayer();
    }

    private synchronized void clearMemoryLayer() {

//        try {
//            FeatureStore fst = (FeatureStore) memoryLayer.getFeatureSource();
//            fst.removeFeatures(Filter.INCLUDE);
//            fst = (FeatureStore) edgesLayer.getFeatureSource();
//            fst.removeFeatures(Filter.INCLUDE);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        repaintMemoryLayer();

        

        FeatureStore store;
        Filter filter = Filter.INCLUDE;

        store = (FeatureStore) memoryLayer.getFeatureSource();
        DefaultTransaction transaction = new DefaultTransaction("trans_maj");
        store.setTransaction(transaction);

        try {
            store.removeFeatures(filter);
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


        store = (FeatureStore) edgesLayer.getFeatureSource();
        transaction = new DefaultTransaction("trans_maj");
        store.setTransaction(transaction);

        try {
            store.removeFeatures(filter);
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

    private synchronized void repaintMemoryLayer() {
        memoryStrategy.refresh();
    }

    private synchronized void reprojectEditionLayer() {

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
            geomsOut.add(projectGeometry(geo, memoryMapContext.getCoordinateReferenceSystem(), getRenderingStrategy().getContext().getCoordinateReferenceSystem()));
        }

        clearMemoryLayer();
        setMemoryLayerGeometry(geomsOut);


    }

    //---------------------defaultMap2D override--------------------------------
    @Override
    protected void setRendering(boolean render) {
        super.setRendering(render);

        MapContext context = getRenderingStrategy().getContext();



        if (context != null && context.getCoordinateReferenceSystem() != null) {

            if (!context.getCoordinateReferenceSystem().equals(memoryMapContext.getCoordinateReferenceSystem())) {
                reprojectEditionLayer();
                try {
                    memoryMapContext.setCoordinateReferenceSystem(context.getCoordinateReferenceSystem());
                } catch (TransformException ex) {
                    ex.printStackTrace();
                } catch (FactoryException ex) {
                    ex.printStackTrace();
                }
            }
        }

        if (context != null && context.getAreaOfInterest() != null) {
            memoryMapContext.setAreaOfInterest(context.getAreaOfInterest());
        }

        memoryStrategy.refresh();
    }

    @Override
    protected void mapAreaChanged(Map2DMapAreaEvent event) {
        super.mapAreaChanged(event);

        MapContext context = getRenderingStrategy().getContext();


        if (context != null && context.getCoordinateReferenceSystem() != null) {

            if (!context.getCoordinateReferenceSystem().equals(memoryMapContext.getCoordinateReferenceSystem())) {
                reprojectEditionLayer();
                try {
                    memoryMapContext.setCoordinateReferenceSystem(context.getCoordinateReferenceSystem());
                } catch (TransformException ex) {
                    ex.printStackTrace();
                } catch (FactoryException ex) {
                    ex.printStackTrace();
                }
            }
        }

        if (context != null && context.getAreaOfInterest() != null) {
            memoryMapContext.setAreaOfInterest(context.getAreaOfInterest());
        }
    }

    @Override
    protected void mapContextChanged(Map2DContextEvent event) {

        MapContext context = event.getNewContext();

        if (context != null && context.getCoordinateReferenceSystem() != null) {
            try {
                memoryMapContext.setCoordinateReferenceSystem(context.getCoordinateReferenceSystem());

                if (context.getAreaOfInterest() != null) {
                    memoryMapContext.setAreaOfInterest(context.getAreaOfInterest());
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
        private Map<Geometry, Integer[]> editedNodes = new HashMap<Geometry, Integer[]>();

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
                        Geometry geom = (Geometry) obj;
                        geom = projectGeometry(geom, editionLayer.getFeatureSource().getSchema().getCRS(), memoryMapContext.getCoordinateReferenceSystem());
                        geoms.add((Geometry) geom.clone());
                        editFeature = sf;
                    }
                }
                fi.close();
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
                                editedNodes.put(subgeo, new Integer[]{0, m - 1});
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


            for (Iterator<Geometry> ite = set.iterator(); ite.hasNext();) {
                Geometry subgeo = ite.next();
                Integer[] nodeIndexes = editedNodes.get(subgeo);

                for (int index : nodeIndexes) {
                    subgeo.getCoordinates()[index].x = mouseCoord.x;
                    subgeo.getCoordinates()[index].y = mouseCoord.y;
                }

                subgeo.geometryChanged();
            }

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


                switch (editState) {
                    case EDIT:

                        if (hasEditionGeometry && !editedNodes.isEmpty()) {
                            hasGeometryChanged = true;
                            dragGeometryNode(e.getX(), e.getY());
                        }

                        editedNodes.clear();
                        break;
                }
            }

        }

        public void mouseEntered(MouseEvent e) {
            switch (actionState) {
                case EDIT:
                    setCursor(CUR_EDIT);
                    break;
            }
        }

        public void mouseExited(MouseEvent e) {
        }

        public void mouseDragged(MouseEvent e) {

            if (actionState == MapConstants.ACTION_STATE.EDIT) {

                switch (editState) {
                    case EDIT:


//                        if (hasEditionGeometry && !editedNodes.isEmpty()) {
//                            hasGeometryChanged = true;
//                            dragGeometryNode(e.getX(), e.getY());
//                        }

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

    private class BufferComponent extends JComponent implements MapDecoration {

        public BufferComponent() {
            setLayout(new BorderLayout());
            add(memoryStrategy.getComponent());
        }

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

