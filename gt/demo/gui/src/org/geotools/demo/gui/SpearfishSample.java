/*
 * Created on 3-apr-2004
 */
package org.geotools.demo.gui;

import java.awt.Color;
import java.awt.Font;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.geotools.data.FeatureSource;
import org.geotools.data.coverage.grid.Format;
import org.geotools.data.coverage.grid.GridCoverageReader;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.gc.GridCoverage;
import org.geotools.gui.swing.StyledMapPane;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.parameter.ParameterGroup;
import org.geotools.renderer.j2d.RenderedMapScale;
import org.geotools.styling.ColorMap;
import org.geotools.styling.Graphic;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.Mark;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.Symbolizer;
import org.geotools.styling.TextSymbolizer;
import org.opengis.parameter.OperationParameterGroup;

/**
 * Simple map viewer
 * @author wolf
 */
public class SpearfishSample {

    private static URL getResource(String path) {
        return SpearfishSample.class.getClassLoader().getResource(path);
    }

    public static void main(String[] args) throws Exception {
        // Prepare feature sources		
        // ... digital elevation dem. 
        URL demURL = getResource("org/geotools/sampleData/spearfish_dem.asc.gz");
        
        // ... If you are working with files from the local file system, use the 
        // following line instead
        // URL demURL = (new java.io.File("/path/to/spearfish/dem")).toURL();
        // create the grid coverage reader
        URL url = ArcGridReader.class.getClassLoader().getResource("org/geotools/sampleData/spearfish_dem.asc.gz");
        Format f = new org.geotools.data.arcgrid.ArcGridFormat();  //GridFormatFinder.findFormat(url);  //should this work also?
        GridCoverageReader reader = f.getReader(url);
        
        //get the parameters and set them
        OperationParameterGroup paramDescriptor = f.getReadParameters();
        ParameterGroup params = (ParameterGroup) paramDescriptor.createValue();
        params.getValue( "Compressed" ).setValue( true );
        params.getValue( "GRASS" ).setValue( true );
        
        //read the grid
        if (reader.hasMoreGridCoverages()) {                 //not yet implemented
            System.out.println("Reader has a GC to read");   
        }
        GridCoverage gcDem = reader.read( params );
        
        // ... roads
        URL roadsURL = getResource("org/geotools/sampleData/roads.shp");
        ShapefileDataStore dsRoads = new ShapefileDataStore(roadsURL);
        FeatureSource fsRoads = dsRoads.getFeatureSource("roads");
        // ... streams
        URL streamsURL = getResource("org/geotools/sampleData/streams.shp");
        ShapefileDataStore dsStreams = new ShapefileDataStore(streamsURL);
        FeatureSource fsStreams = dsStreams.getFeatureSource("streams");
        // ... bug sites
        URL bugURL = getResource("org/geotools/sampleData/bugsites.shp");
        ShapefileDataStore dsBugs = new ShapefileDataStore(bugURL);
        FeatureSource fsBugs = dsBugs.getFeatureSource("bugsites");
        // ... arch sites
        URL archURL = getResource("org/geotools/sampleData/archsites.shp");
        ShapefileDataStore dsArch = new ShapefileDataStore(archURL);
        FeatureSource fsArch = dsArch.getFeatureSource("archsites");
        // ... restricted aread
        URL restrictedURL = getResource("org/geotools/sampleData/rstrct.shp");
        ShapefileDataStore dsRectricted = new ShapefileDataStore(restrictedURL);
        FeatureSource fsRestricted = dsRectricted.getFeatureSource("rstrct");

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
        // ... dem style
        ColorMap cm =
            sb.createColorMap(
                new double[] { 1000, 1200, 1400, 1600, 2000 },
                new Color[] {
                    new Color(0, 255, 0),
                    new Color(255, 255, 0),
                    new Color(255, 127, 0),
                    new Color(191, 127, 63),
                    new Color(255, 255, 255)},
                ColorMap.TYPE_RAMP);
        RasterSymbolizer rsDem = sb.createRasterSymbolizer(cm, 1);
        Style demStyle = sb.createStyle(rsDem);
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
        TextSymbolizer tsArch = sb.createTextSymbolizer(Color.BLACK, font, "CAT_DESC");
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
        map.addLayer( gcDem, demStyle);
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
        frame.setTitle("Spearfish map");
        frame.setContentPane(mapPane.createScrollPane());
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(640, 480);
        frame.show();
    }
}
