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
package org.geotools.demo.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URL;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JMenuBar;

import org.geotools.ct.Adapters;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.gui.swing.MapPaneImpl;
import org.geotools.gui.swing.ToolMenu;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.DefaultMapLayer;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;


/**
 * A demonstration of a Map geotools-src\docs\sdocbook\resourcesViewer which uses geotools2.
 *
 * The shapefile to view is given as an agument for this class. If no arguments
 * are given, a shapefile from sample-data is used.
 *
 * @author Cameron Shorter
 * @author Andrea Aime
 * @version $Id$
 */
public class MapToolViewer {
    /** The class used for identifying for logging. */
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.demo.gui.MapToolViewer");

    /** Translates between coordinate systems */
    private Adapters adapters = Adapters.getDefault();

    /** The context which contains this maps data */
    private MapContext context;

    /**
     * Creates new form MapViewer
     *
     * @param fc DOCUMENT ME!
     * @param style DOCUMENT ME!
     */
    public MapToolViewer(FeatureCollection fc, Style style) {
        initComponents(createMapPane(fc, style));
    }

    /**
     * This method is called from within the constructor to initialize the
     * form.
     *
     * @param fc The feature collection you want to show
     * @param style The style applied to the feature collection
     *
     * @return A demo mapPane
     */
    private MapPaneImpl createMapPane(FeatureCollection fc, Style style)  {
        MapPaneImpl mapPane = null;
        MapLayer layer;

        // Create a Context
        context = new DefaultMapContext();
        layer = new DefaultMapLayer(fc, style);
        layer.setTitle("Test layer");
        context.addLayer(layer);
        
        try {
            // Create MapPane
            mapPane = new MapPaneImpl(context);
        } catch (Exception e) {
            // I'm sure they won't be thrown since I'm working with a FeatureCollection
        }
        mapPane.setBackground(Color.WHITE);
        mapPane.setPreferredSize(new Dimension(300, 300));

        return mapPane;
    }

    private void initComponents(MapPaneImpl mapPane) {
        // Create Menu for tools
        JMenuBar menuBar = new JMenuBar();

        // Create frame
        JFrame frame = new JFrame();
        frame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent evt) {
                    exitForm();
                }
            });

        ToolMenu toolMenu = new ToolMenu(mapPane.getToolList());
        menuBar.add(toolMenu);
        frame.setJMenuBar(menuBar);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(mapPane);
        frame.setTitle("Map Viewer");
        frame.pack();
        frame.show();
    }

    /**
     * Exit the Application
     *
     * @param evt the event
     */
    private void exitForm() {
        System.exit(0);
    }

    /**
     * The MapViewer main program.
     *
     * @param args the location of the shapefile you want to view
     *
     * @throws Exception DOCUMENT ME!
     */
    public static void main(String[] args) throws Exception {
        switch (args.length) {
            default: // Fall through
            case  1:
                mapPane(new File(args[0]).toURL()); 
                break;
            case  0: 
                mapPane(MapToolViewer.class.getClassLoader().getResource(
                    "org/geotools/sampleData/statepop.shp")); 
                break;
        }

    }

    public static void mapPane(URL dataUrl) throws Exception {

        ShapefileDataStore sds = new ShapefileDataStore(dataUrl);
        FeatureCollection fc = sds.getFeatureSource(sds.getTypeNames()[0]).getFeatures().collection();

        // create the style
        StyleBuilder sb = new StyleBuilder();
        Style simple = sb.createStyle(sb.createPolygonSymbolizer(
                    Color.LIGHT_GRAY, Color.BLACK, 1));

        // show the map
        new MapToolViewer(fc, simple);
    }
}
