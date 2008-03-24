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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSpinner;
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
public class AffineMapTest {

    private final RandomStyleFactory RANDOM_STYLE_FACTORY = new RandomStyleFactory();
    private JDefaultEditableMap2D map = new JDefaultEditableMap2D();
    private final JMap2DInfoBar info = new JMap2DInfoBar();
    private final JMap2DNavigationBar nav = new JMap2DNavigationBar();
    private final MapContext context = buildContext();
    private JPanel pan = new JPanel(new BorderLayout());

    public static void main(String[] args) {
        new AffineMapTest();
    }

    public AffineMapTest() {

        JFrame frm = new JFrame();
        frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frm.setSize(800, 600);
        frm.setLocationRelativeTo(null);

        JPanel panaffine = new JPanel(new GridLayout(0, 1));
        
        final JSpinner sp0 = new JSpinner();
        final JSpinner sp1 = new JSpinner();
        final JSpinner sp2 = new JSpinner();
        final JSpinner sp3 = new JSpinner();
        final JSpinner sp4 = new JSpinner();
        final JSpinner sp5 = new JSpinner();
        
        JButton but = new JButton("test");
        
        but.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                AffineTransform aff = new AffineTransform();
                aff.rotate(  Math.toRadians((Integer)sp0.getValue()) );
                aff.scale((Integer)sp1.getValue(),(Integer)sp2.getValue());
                aff.translate((Integer)sp3.getValue(),(Integer)sp4.getValue());
//                AffineTransform aff = new AffineTransform((Integer)sp0.getValue(),(Integer)sp1.getValue(),(Integer)sp2.getValue(),(Integer)sp3.getValue(),(Integer)sp4.getValue(),(Integer)sp5.getValue());
                map.getRenderingStrategy().setAffineTransform(aff);
            }
        });
        
        
        
        panaffine.add(sp0);
        panaffine.add(sp1);
        panaffine.add(sp2);
        panaffine.add(sp3);
        panaffine.add(sp4);
        panaffine.add(sp5);
        panaffine.add(but);
        


        info.setMap(map);
        nav.setMap(map);
        info.setFloatable(false);
        nav.setFloatable(false);

        pan.add(BorderLayout.NORTH, nav);
        pan.add(BorderLayout.CENTER, map.getComponent());
        pan.add(BorderLayout.WEST,panaffine);
        pan.add(BorderLayout.SOUTH, info);

        frm.setContentPane(pan);
        frm.setVisible(true);

        map.setRenderingStrategy(new SingleBufferedImageStrategy());
        map.getRenderingStrategy().setContext(context);


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
        } catch (Exception ex) {
            ex.printStackTrace();
        }


        return context;
    }
}
