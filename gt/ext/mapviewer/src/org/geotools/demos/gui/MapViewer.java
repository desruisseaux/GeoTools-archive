/*
 *    Geotools2 - OpenSource mapping toolkit
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
 */
package org.geotools.demos.gui;

// J2SE dependencies
import java.net.URL;
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;

// User interface
import javax.swing.JFrame;
import javax.swing.JApplet;
import javax.swing.JLabel;
import java.awt.Container;
import java.awt.BorderLayout;
import java.awt.Color;

// Geotools dependencies
import org.geotools.data.DataSourceException;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.gui.swing.StatusBar;
import org.geotools.gui.swing.StyledMapPane;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.DefaultMapLayer;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.renderer.j2d.RenderedMapScale;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.SLDParser;

// JTS dependencies
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;


/**
 * Load and display a shape file. At the difference of the {@link MapViewer} demo, this demo
 * use {@link MapPane} with the {@linkplain Renderer J2D renderer}. This renderer has the
 * following advantages:
 * <ul>
 *   <li>faster;</li>
 *   <li>progressive rendering of tiled image;</li>
 *   <li>supports arbitrary map projections (different geometries to be rendered on the same
 *       map can have different coordinate systems);</li>
 *   <li>supports zooms, translations and rotations through mouse drag, mouse wheel, keyboard
 *       and contextual menu localized in English, French, Portuguese and partially in Spanish
 *       and Greek;</li>
 *   <li>provides a magnifier (accessible from the contextual menu, right button click);</li>
 *   <li>arbitrary amount of offscreen buffering;</li>
 *   <li>can display scroll bars;</li>
 *   <li>can display a status bar with mouse coordinates in an arbitrary coordinate system
 *       (it doesn't have to be the same coordinate system than the renderer one);</li>
 *   <li>has a more precise scale factor taking in account the physical size of the output
 *       device (when this information is available);</li>
 * </ul>
 *
 * The inconvenient is a more complex renderer, which is more difficult to modify for new users.
 * <br><br>
 * NOTE: While not essential, it is recommanded to run this demos in server mode, with:
 * <blockquote><pre>
 * java -server org.geotools.demos.MapViewer <I>thefile.shp</I>
 * </pre></blockquote>
 *
 * Note that this class extends <code>JApplet</code> so it can <strong>also</strong> be tested
 * as an applet. If you don't want it to be an applet, remove the {@link #init()} method.
 *
 * @author Martin Desruisseaux
 * @version $Id: MapViewer.java,v 1.5 2004/04/10 16:03:18 aaime Exp $
 */
public class MapViewer extends JApplet {
    /**
     * Run the test from the command line. If arguments are provided, then the first
     * argument is understood as the filename of the shapefile to load.
     *
     * @throws IOException is a I/O error occured.
     * @throws DataSource if an error occured while reading the data source.
     */
    public static void main(final String[] args) throws Exception {
        final MapContext context;
        switch (args.length) {
            default: // Fall through
            case  2: context=MapViewer.loadContext(new File(args[0]).toURL(), new File(args[1]).toURL()); break;
            case  1: context=MapViewer.loadContext(new File(args[0]).toURL(), null); break;
            case  0: context=MapViewer.loadContext(); break;
        }
        MapViewer.showMapPane(context);
    }
    
    /**
     * Load the data from the shapefile <code>&quot;testData/statepop.shp&quot;</code>.
     * This file must be on the class path.
     *
     * @return Context The data from the shape file.
     * @throws FileNotFoundException if the shape file was not found.
     * @throws IOException is some other kind of I/O error occured.
     * @throws DataSource if an error occured while reading the data source.
     */
    protected static MapContext loadContext() throws IOException, DataSourceException {
        return loadContext(MapViewer.class.getClassLoader().getResource("org/geotools/sampleData/statepop.shp"), null);
    }
    
    /**
     * Load the data from the specified shapefile and construct a {@linkplain Context context}
     * with a default style.
     *
     * @param  url The url of the shapefile to load.
     * @return Context The data from the shape file.
     * @throws IOException is a I/O error occured.
     * @throws DataSource if an error occured while reading the data source.
     */
    protected static MapContext loadContext(final URL url, final URL sld) throws IOException, DataSourceException {
        
        // Load the file
        if (url == null) {
            throw new FileNotFoundException("Resource not found");
        }
        final DataStore store = new ShapefileDataStore(url);
        final FeatureSource features = store.getFeatureSource(store.getTypeNames()[0]);
        
        // Create the style
        final Style style;
        if(sld != null){
            SLDParser styleReader = new SLDParser(StyleFactory.createStyleFactory(),sld);
            style = styleReader.readXML()[0];
        }
        else
        {
            final StyleBuilder builder = new StyleBuilder();
            
            Class geometryClass = features.getSchema().getDefaultGeometry().getType();
            if(LineString.class.isAssignableFrom(geometryClass) || MultiLineString.class.isAssignableFrom(geometryClass)) {
                style = builder.createStyle(builder.createLineSymbolizer());
            } else if(Point.class.isAssignableFrom(geometryClass) || MultiPoint.class.isAssignableFrom(geometryClass)) {
                style = builder.createStyle(builder.createPointSymbolizer());
            } else {
                style = builder.createStyle(builder.createPolygonSymbolizer(
                Color.ORANGE, Color.BLACK, 1));
            }
        }
        
        
        // Create the context
        MapContext context = new DefaultMapContext();
        MapLayer layer = new DefaultMapLayer(features, style);
        layer.setTitle("The shapefile");
        context.addLayer(layer);
        context.setTitle("Hello World");
        return context;
    }
    
    /**
     * Create and show the map pane. This method is used for running the map viewer
     * as a standalone application only.
     *
     * @param context The context to show.
     */
    protected static void showMapPane(final MapContext context) throws Exception {
        // Create the map pane and add a map scale layer to it.
        final StyledMapPane mapPane = new StyledMapPane();
        mapPane.setMapContext(context);
        mapPane.setPaintingWhileAdjusting(false);
        mapPane.getRenderer().addLayer(new RenderedMapScale());
        
        // Create the frame, add the map pane and a status bar.
        final JFrame frame = new JFrame(context.getTitle());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        final Container container = frame.getContentPane();
        container.setLayout(new BorderLayout());
        container.add(mapPane.createScrollPane(), BorderLayout.CENTER);
        container.add(new StatusBar(mapPane),     BorderLayout.SOUTH);
        frame.pack();
        frame.show();
    }

    /**
     * Initialize the applet. This method is used for running the map viewer as an applet only.
     */
    public void init() {
        final Container container = getContentPane();
        container.setLayout(new BorderLayout());
        try {
            final StyledMapPane mapPane = new StyledMapPane();
            mapPane.setMapContext(MapViewer.loadContext());
            mapPane.setPaintingWhileAdjusting(false);
            mapPane.getRenderer().addLayer(new RenderedMapScale());
            container.add(mapPane.createScrollPane(), BorderLayout.CENTER);
            container.add(new StatusBar(mapPane),     BorderLayout.SOUTH);
        } catch (Exception exception) {
            container.add(new JLabel(exception.getLocalizedMessage()), BorderLayout.CENTER);
        }
    }
}
