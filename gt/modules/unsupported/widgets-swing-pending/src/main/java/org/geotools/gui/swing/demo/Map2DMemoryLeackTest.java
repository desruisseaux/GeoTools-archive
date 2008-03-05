/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2007, GeoTools Project Managment Committee (PMC)
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
package org.geotools.gui.swing.demo;

import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.apache.commons.collections.map.SingletonMap;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.gui.swing.map.map2d.JDefaultEditableMap2D;
import org.geotools.gui.swing.map.map2d.JDefaultMap2D;
import org.geotools.gui.swing.map.map2d.control.JMap2DInfoBar;
import org.geotools.gui.swing.map.map2d.control.JMap2DNavigationBar;
import org.geotools.gui.swing.map.map2d.strategy.MergeBufferedImageStrategy;
import org.geotools.gui.swing.map.map2d.strategy.SingleBufferedImageStrategy;
import org.geotools.gui.swing.map.map2d.strategy.SingleVolatileImageStrategy;
import org.geotools.gui.swing.misc.Render.RandomStyleFactory;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.DefaultMapLayer;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.styling.Style;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 *
 * @author johann sorel
 */
public class Map2DMemoryLeackTest {

    private final RandomStyleFactory RANDOM_STYLE_FACTORY = new RandomStyleFactory();
    private JDefaultEditableMap2D map = new JDefaultEditableMap2D();
    private final JMap2DInfoBar info = new JMap2DInfoBar();
    private final JMap2DNavigationBar nav = new JMap2DNavigationBar();
    private final MapContext context = buildContext();
    private JPanel pan = new JPanel(new BorderLayout());

    public static void main(String[] args) {
        new StrategyMemoryLeackTest();
    }

    public Map2DMemoryLeackTest() {

        JFrame frm = new JFrame();
        frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frm.setSize(800, 600);
        frm.setLocationRelativeTo(null);

        


        info.setMap(map);
        nav.setMap(map);
        info.setFloatable(false);
        nav.setFloatable(false);

        pan.add(BorderLayout.NORTH, nav);
        pan.add(BorderLayout.CENTER, map.getComponent());
        pan.add(BorderLayout.SOUTH, info);

        frm.setContentPane(pan);
        frm.setVisible(true);

        map.getRenderingStrategy().setContext(context);


        Thread t = new Thread() {

            int val = 0;

            @Override
            public void run() {

                while (true) {

                    map.dispose();
                    pan.remove(map.getComponent());
                    map = new JDefaultEditableMap2D();
                    
                    pan.add(BorderLayout.CENTER, map.getComponent());
                    map.getRenderingStrategy().setContext(context);
        
                    info.setMap(map);
                    nav.setMap(map);
                    
                    try {
                        map.getRenderingStrategy().setMapArea(map.getRenderingStrategy().getContext().getLayerBounds());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        sleep(1800);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        };

        t.start();

    }

    private MapContext buildContext() {
        MapContext context = null;
        MapLayer layer;

        try {
            context = new DefaultMapContext(DefaultGeographicCRS.WGS84);
            DataStore store = DataStoreFinder.getDataStore(new SingletonMap("url", DemoAll.class.getResource("/org/geotools/gui/swing/demo/shape/test_polygon.shp")));
            FeatureSource<SimpleFeatureType, SimpleFeature> fs = store.getFeatureSource(store.getTypeNames()[0]);
            Style style = RANDOM_STYLE_FACTORY.createRandomVectorStyle(fs);
            layer = new DefaultMapLayer(fs, style);
            layer.setTitle("demo_polygon.shp");
            context.addLayer(layer);

            store = DataStoreFinder.getDataStore(new SingletonMap("url", DemoAll.class.getResource("/org/geotools/gui/swing/demo/shape/test_ligne.shp")));
            fs = store.getFeatureSource(store.getTypeNames()[0]);
            style = RANDOM_STYLE_FACTORY.createRandomVectorStyle(fs);
            layer = new DefaultMapLayer(fs, style);
            layer.setTitle("demo_line.shp");
            context.addLayer(layer);

            store = DataStoreFinder.getDataStore(new SingletonMap("url", DemoAll.class.getResource("/org/geotools/gui/swing/demo/shape/test_point.shp")));
            fs = store.getFeatureSource(store.getTypeNames()[0]);
            style = RANDOM_STYLE_FACTORY.createRandomVectorStyle(fs);
            layer = new DefaultMapLayer(fs, style);
            layer.setTitle("demo_point.shp");
            context.addLayer(layer);
            context.setTitle("DemoContext");

            store = DataStoreFinder.getDataStore(new SingletonMap("url", DemoAll.class.getResource("/org/geotools/gui/swing/demo/shape/test_ligne.shp")));
            fs = store.getFeatureSource(store.getTypeNames()[0]);
            style = RANDOM_STYLE_FACTORY.createRandomVectorStyle(fs);
            layer = new DefaultMapLayer(fs, style);
            layer.setTitle("demo_line.shp");
            context.addLayer(layer);

            store = DataStoreFinder.getDataStore(new SingletonMap("url", DemoAll.class.getResource("/org/geotools/gui/swing/demo/shape/test_point.shp")));
            fs = store.getFeatureSource(store.getTypeNames()[0]);
            style = RANDOM_STYLE_FACTORY.createRandomVectorStyle(fs);
            layer = new DefaultMapLayer(fs, style);
            layer.setTitle("demo_point.shp");
            context.addLayer(layer);
            context.setTitle("DemoContext");

            store = DataStoreFinder.getDataStore(new SingletonMap("url", DemoAll.class.getResource("/org/geotools/gui/swing/demo/shape/test_ligne.shp")));
            fs = store.getFeatureSource(store.getTypeNames()[0]);
            style = RANDOM_STYLE_FACTORY.createRandomVectorStyle(fs);
            layer = new DefaultMapLayer(fs, style);
            layer.setTitle("demo_line.shp");
            context.addLayer(layer);

            store = DataStoreFinder.getDataStore(new SingletonMap("url", DemoAll.class.getResource("/org/geotools/gui/swing/demo/shape/test_point.shp")));
            fs = store.getFeatureSource(store.getTypeNames()[0]);
            style = RANDOM_STYLE_FACTORY.createRandomVectorStyle(fs);
            layer = new DefaultMapLayer(fs, style);
            layer.setTitle("demo_point.shp");
            context.addLayer(layer);
            context.setTitle("DemoContext");
        } catch (Exception ex) {
            ex.printStackTrace();
        }


        return context;
    }
}
