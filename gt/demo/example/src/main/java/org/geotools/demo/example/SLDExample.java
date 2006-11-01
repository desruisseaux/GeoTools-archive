package org.geotools.demo.example;

import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.data.ows.CRSEnvelope;
import org.geotools.data.ows.Layer;
import org.geotools.data.ows.WMSCapabilities;
import org.geotools.data.wms.WebMapServer;
import org.geotools.data.wms.request.GetMapRequest;
import org.geotools.data.wms.response.GetMapResponse;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.filter.FilterType;
import org.geotools.filter.GeometryFilter;
import org.geotools.gui.swing.JMapPane;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.SLD;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.StyleFactoryFinder;
import org.geotools.styling.Symbolizer;

import com.vividsolutions.jts.geom.Envelope;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.WindowConstants;

import org.geotools.data.FeatureSource;
import org.geotools.gui.swing.JMapPane;
import org.geotools.gui.swing.PanAction;
import org.geotools.gui.swing.ResetAction;
import org.geotools.gui.swing.SelectAction;
import org.geotools.gui.swing.ZoomInAction;
import org.geotools.gui.swing.ZoomOutAction;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.SLDParser;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.StyleFactoryFinder;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * This example also works against a local geoserver.
 * 
 * @author Jody Garnett
 */
public class SLDExample {

	public static void main(String args[]) {
		try {
			//supressInfo();
			localSLD();
		} catch (Exception ignore) {
			ignore.printStackTrace();
		}
	}
	public static void supressInfo(){
		Logger.getLogger("org.geotools.gml").setLevel( Level.SEVERE );
		Logger.getLogger("net.refractions.xml").setLevel( Level.SEVERE);
	}
	public static void localSLD() throws Exception {
		FeatureSource source = demoFeatureSource();
		Style style = demoStyle( source.getSchema().getTypeName() );

		show( source, style );
	}
	static FeatureSource demoFeatureSource() throws Exception {
		String getCapabilities =
			"http://localhost:8080/geoserver/wfs?service=WFS&request=GetCapabilities";
		
		Map connectionParameters = new HashMap();
		connectionParameters.put("WFSDataStoreFactory:GET_CAPABILITIES_URL", getCapabilities );
		
		DataStore data = DataStoreFinder.getDataStore( connectionParameters );
		String typeName = data.getTypeNames()[0];
		return data.getFeatureSource( typeName );		
	}
	
	static Style demoStyle(String typeName) throws Exception {
		StyleFactory sf = StyleFactoryFinder.createStyleFactory();
		FilterFactory ff = FilterFactoryFinder.createFilterFactory();
		
		Stroke stroke = sf.createStroke(
			ff.createLiteralExpression("#FF0000"),
			ff.createLiteralExpression(2));
		
		LineSymbolizer lineSymbolizer = sf.createLineSymbolizer();		
		lineSymbolizer.setStroke( stroke );
		
		Rule rule = sf.createRule();
		rule.setFilter( Filter.NONE );
		rule.setSymbolizers( new Symbolizer[]{ lineSymbolizer });
		
		FeatureTypeStyle type = sf.createFeatureTypeStyle();
		type.setFeatureTypeName(typeName);
		type.addRule( rule );
		
		Style style = sf.createStyle();
		style.addFeatureTypeStyle(type);
		
		return style;
	}
	public static void show(FeatureSource source, Style style) throws Exception {
		    JFrame frame = new JFrame("FOSS4G");
	        frame.setBounds(20,20,450,200);
	        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		    
	        JMapPane mp = new JMapPane();
	        frame.getContentPane().add( mp);
		    mp.setMapArea(source.getBounds());
		    
		    MapContext context = new DefaultMapContext();
		    context.setAreaOfInterest(source.getBounds());
		    context.addLayer( source, style );
		    //context.getLayerBounds();
		    
		    GTRenderer renderer = new StreamingRenderer();
		    HashMap hints = new HashMap();
		    hints.put("memoryPreloadingEnabled", Boolean.TRUE);
		    renderer.setRendererHints( hints );

		    mp.setRenderer(renderer);
		    mp.setContext(context);
		    
		    frame.setVisible(true);
	}
}
