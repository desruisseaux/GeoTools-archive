/*
 * Created on 1-july-2004
 */
package org.geotools.demo.gui;

import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.gui.swing.StyledMapPane;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.renderer.j2d.RenderedMapScale;
import org.geotools.styling.Graphic;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.Mark;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.Symbolizer;
import org.geotools.styling.TextSymbolizer;

/**
 * Simple map viewer based on SpearfishSample. Change database params for 
 * your own system.
 *
 * @author wolf
 * @author rschulz
 * @source $URL$
 */
public class SpearfishPostGIS {

    public static void main(String[] args) throws Exception {
        
        //create the PostGISDataStore
        Map params = new HashMap();
        params.put("dbtype", "postgis");
        params.put("host", "localhost");
        params.put("port", new Integer(5432));
        params.put("database", "spearfish");
        params.put("user", "postgres");
        params.put("passwd", "");
        DataStore pgDatastore = DataStoreFinder.getDataStore(params);
        
        // Prepare feature sources		
        // ... roads
        FeatureSource fsRoads = pgDatastore.getFeatureSource("roads");
        // ... streams
        FeatureSource fsStreams = pgDatastore.getFeatureSource("streams");
        // ... bug sites
        FeatureSource fsBugs = pgDatastore.getFeatureSource("bugsites");
        // ... arch sites
        FeatureSource fsArch = pgDatastore.getFeatureSource("archsites");
        // ... restricted aread
        FeatureSource fsRestricted = pgDatastore.getFeatureSource("rstrct");

        // Prepare styles
        StyleBuilder sb = new StyleBuilder();
        // ... streams style
        LineSymbolizer lsStream = sb.createLineSymbolizer(Color.BLUE, 3);
        Style streamsStyle = sb.createStyle(lsStream);
        // ... roads style
        LineSymbolizer ls1 = sb.createLineSymbolizer(Color.YELLOW, 1);
        LineSymbolizer ls2 = sb.createLineSymbolizer(Color.BLACK, 5);
        Style roadsStyle = sb.createStyle();
        roadsStyle.addFeatureTypeStyle(sb.createFeatureTypeStyle(null, sb.createRule(ls2)));
        roadsStyle.addFeatureTypeStyle(sb.createFeatureTypeStyle(null, sb.createRule(ls1)));
        // ... bugs style
        Mark redCircle = sb.createMark(StyleBuilder.MARK_CIRCLE, Color.RED, Color.BLACK, 0);
        Graphic grBugs = sb.createGraphic(null, redCircle, null);
        PointSymbolizer psBugs = sb.createPointSymbolizer(grBugs);
        Style bugsStyle = sb.createStyle(psBugs);
        // ... archeological sites style
        Mark yellowTri = sb.createMark(StyleBuilder.MARK_TRIANGLE, Color.YELLOW, Color.BLACK, 0);
        Graphic grArch = sb.createGraphic(null, yellowTri, null, 1, 15, 0);
        PointSymbolizer psArch = sb.createPointSymbolizer(grArch);
        org.geotools.styling.Font font = sb.createFont(new Font("Arial", Font.PLAIN, 12));
        TextSymbolizer tsArch = sb.createTextSymbolizer(Color.BLACK, font, "cat_desc");
        tsArch.setHalo(sb.createHalo(Color.WHITE, 1, 2));
        Rule archRule = sb.createRule(new Symbolizer[] {psArch, tsArch});
        Style archStyle = sb.createStyle();
        archStyle.addFeatureTypeStyle(sb.createFeatureTypeStyle(null, archRule));
        // ... restricted area style
        PolygonSymbolizer restrictedSymb =
            sb.createPolygonSymbolizer(Color.LIGHT_GRAY, Color.BLACK, 0);
        restrictedSymb.getFill().setOpacity(sb.literalExpression(0.7));
        Style restrictedStyle = sb.createStyle(restrictedSymb);

        // Build the map
        MapContext map = new DefaultMapContext();
        map.addLayer(fsStreams, streamsStyle);
        map.addLayer(fsRoads, roadsStyle);
        map.addLayer(fsRestricted, restrictedStyle);
        map.addLayer(fsBugs, bugsStyle);
        map.addLayer(fsArch, archStyle);

        // Show the map
        StyledMapPane mapPane = new StyledMapPane();
        mapPane.setMapContext(map);
        mapPane.getRenderer().addLayer(new RenderedMapScale());
        JFrame frame = new JFrame();
        frame.setTitle("Spearfish PostGIS map");
        frame.setContentPane(mapPane.createScrollPane());
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(640, 480);
        frame.show();
    }
}
