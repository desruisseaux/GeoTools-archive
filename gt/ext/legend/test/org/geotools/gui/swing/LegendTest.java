/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */
package org.geotools.gui.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.io.File;
import java.net.URL;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JSplitPane;

import junit.framework.Test;
import junit.framework.TestCase;

import org.geotools.cs.CoordinateSystem;
import org.geotools.cs.CoordinateSystemFactory;
import org.geotools.cs.Ellipsoid;
import org.geotools.cs.GeographicCoordinateSystem;
import org.geotools.cs.HorizontalDatum;
import org.geotools.cs.Projection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.gui.swing.sldeditor.SLDEditor;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.DefaultMapLayer;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.styling.BasicLineStyle;
import org.geotools.styling.BasicPolygonStyle;
import org.geotools.styling.SLDParser;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.StyleFactoryFinder;
import org.geotools.units.Unit;
import org.geotools.resources.TestData;


/**
 *
 * @version $Id$
 * @author iant
 */
public class LegendTest extends TestCase {
    /**
     * {@code true} for enabling {@code println} statements. By default {@code true}
     * when running from the command line, and {@code false} when running by Maven.
     */
    private static boolean verbose;

    /**
     * The context which contains this maps data
     */
    public LegendTest(String testName) {
        super(testName);
    }

    public void testLegend() throws Exception {
        MapLayer[] layers;
        MapContext context;

        URL shpData2 = TestData.url(this, "testPoint.shp");
        URL shpData1 = TestData.url(this, "testLine.shp");
        URL shpData0 = TestData.url(this, "testPolygon.shp");

        URL[] data = new URL[] { shpData0, shpData1, shpData2 };

        File sldFile = TestData.file(this, "color.sld");
        SLDParser sld = null;
        
        SLDEditor.propertyEditorFactory.setInExpertMode(true);

        sld = new SLDParser(StyleFactoryFinder.createStyleFactory(), sldFile);

        Style[] styles = sld.readXML();
        if (verbose) {
            System.out.println("user style num: " + styles.length);
        }

        context = new DefaultMapContext();

        ShapefileDataStore[] dataStores = new ShapefileDataStore[data.length];
        layers = new MapLayer[data.length];

        for (int i = 0; i < data.length; i++) {
            HashMap params = new HashMap();
            params.put("url", data[i].toString());
            dataStores[i] = new ShapefileDataStore(data[i]);
        }

        StyleFactory styleFactory = StyleFactoryFinder.createStyleFactory();
        BasicPolygonStyle style = new BasicPolygonStyle(styleFactory.getDefaultFill(),
                styleFactory.getDefaultStroke());
        style.setTitle("Leeds ED Poly");

        BasicLineStyle lineStyle = new BasicLineStyle(styleFactory.getDefaultStroke());
        lineStyle.setTitle("UK Motorway Basic");
        lineStyle.getFeatureTypeStyles()[0].getRules()[0].setTitle("Motorway");

        if (verbose) {
            System.out.println("Loading Data");
        }
        layers[0] = new DefaultMapLayer(dataStores[0].getFeatureSource(dataStores[0].getTypeNames()[0]), styles[0], "Leeds Ward Layer");
        layers[1] = new DefaultMapLayer(dataStores[1].getFeatureSource(dataStores[1].getTypeNames()[0]), lineStyle, "Leeds ED Layer");
        layers[2] = new DefaultMapLayer(dataStores[2].getFeatureSource(dataStores[2].getTypeNames()[0]), styles[1], "UK MotorWays Basic");

        if (verbose) {
            System.out.println("create Coodinate System....1");
        }
        Ellipsoid airy1830 = Ellipsoid.createEllipsoid("Airy1830", 6377563.396, 6356256.910, Unit.METRE);
        if (verbose) {
            System.out.println("create Coodinate System....2" + airy1830.toString());
        }
        GeographicCoordinateSystem geogCS = CoordinateSystemFactory.getDefault()
                .createGeographicCoordinateSystem("Airy1830", new HorizontalDatum("Airy1830", airy1830));
        if (verbose) {
            System.out.println("create Coodinate System....3" + geogCS.toString());
        }
        Projection p = new Projection("Great_Britian_National_Grid", "Transverse_Mercator",
                airy1830, new Point2D.Double(49, -2), new Point2D.Double(400000, -100000));

        if (verbose) {
            System.out.println("create Coodinate System....4" + p.toString());
        }
        CoordinateSystem projectCS = CoordinateSystemFactory.getDefault()
                .createProjectedCoordinateSystem("Great_Britian_National_Grid", geogCS, p);

        if (verbose) {
            System.out.println("create Context");
        }
        context.addLayers(layers);
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }
        if (verbose) {
            System.out.println("creating Map Pane");
        }
        // Create MapPane
        StyledMapPane mapPane = new StyledMapPane();
        mapPane.setMapContext(context);
        if (verbose) {
            System.out.println("Creating Map Pane Done");
        }
        mapPane.setBackground(Color.WHITE);
        mapPane.setPreferredSize(new Dimension(800, 800));

        // Create Menu for tools
        JMenuBar menuBar = new javax.swing.JMenuBar();
        if (verbose) {
            System.out.println("creating MenuBar for Map Pane...");
        }
        // Create frame
        JFrame frame = new JFrame();
        frame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent evt) {
                    System.exit(0);
                }
            });

        frame.getContentPane().setLayout(new BorderLayout());
        if (verbose) {
            System.out.println("Add Map Pane into Frame");
        }
        JSplitPane splitPane = new JSplitPane();
        splitPane.add(new Legend(context, "LegendTest"), JSplitPane.LEFT);
        splitPane.add(mapPane, JSplitPane.RIGHT);
        splitPane.setDividerLocation(200);
        frame.setContentPane(splitPane);            

        frame.setTitle("Map Viewer");
        frame.setSize(600, 400);
        frame.setVisible(true);
        Thread.currentThread().sleep(500);
        frame.dispose();
    }

    public static void main(java.lang.String[] args) {
        verbose = true;
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        return new junit.framework.TestSuite(LegendTest.class);
    }
}
