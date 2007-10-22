package org.geotools.demo.data;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.factory.GeoTools;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.collection.AbstractFeatureVisitor;
import org.geotools.gui.swing.ProgressWindow;
import org.opengis.feature.Feature;

import com.vividsolutions.jts.geom.Geometry;

/**
 * How to Read a Shapefile.
 * <p>
 * Please visit the GeoTools User Guide: <a
 * href="http://docs.codehaus.org/display/GEOTDOC/04+How+to+Read+a+Shapefile">
 * 
 * @author Jody Garnett (Refractions Research)
 * 
 */
public class ShapefileRead {

	public static void main(String[] args) throws Exception {
		System.out.println("Welcome to GeoTools:" + GeoTools.getVersion());
		
		File file;
		if (args.length == 0){
			JFileChooser chooser = new JFileChooser();
			chooser.setFileFilter( new FileFilter(){
                public boolean accept( File f ) {
                    return f.isDirectory() || f.getPath().endsWith("shp") || f.getPath().endsWith("SHP");
                }
                public String getDescription() {
                    return "Shapefiles";
                }			    
			});
			int returnVal = chooser.showOpenDialog( null );
			
			if(returnVal != JFileChooser.APPROVE_OPTION) {
				System.exit(0);
			}
			file = chooser.getSelectedFile();
			
			System.out.println("You chose to open this file: " +
					file.getName());								
		}
		else {
			file = new File( args[0] );
		}
		if (!file.exists()){
			System.exit(1);
		}
		
		Map<String,Object> connect = new HashMap<String,Object>();
		connect.put("url", file.toURI().toURL());

		DataStore dataStore = DataStoreFinder.getDataStore(connect);
		String[] typeNames = dataStore.getTypeNames();
		String typeName = typeNames[0];

		System.out.println("Reading content " + typeName);

		FeatureSource featureSource = dataStore.getFeatureSource(typeName);
		FeatureCollection collection = featureSource.getFeatures();
		
		class DistanceVisitor extends AbstractFeatureVisitor {
			int length =0;
			public void visit(Feature feature) {
				Geometry geometry = (Geometry) feature.getDefaultGeometryProperty().getValue();
				length += geometry.getLength();
			}
		};
		DistanceVisitor distance = new DistanceVisitor();
		
		collection.accepts( distance, new ProgressWindow(null));
		System.out.println("Total length " + distance.length );
	}
}