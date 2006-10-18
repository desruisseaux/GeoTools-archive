/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.demo.data;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JFileChooser;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.data.DataStoreFactorySpi.Param;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.AttributeType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.feature.GeometryAttributeType;

import com.vividsolutions.jts.geom.Geometry;


/**
 * A demo of basic reading abilities exemplified by the shapefile access system. 
 * The demo opens a file, gets the feature type, reads the first ten features
 * and outputs their contents to the standard output.
 *
 * @author aaime
 * @source $URL$
 */
public class ShapeReader {
	
    private static URL getResource(String path) {
        return ShapeReader.class.getResource(path);
    }

    public static void main(String[] args) {
        try {
        	URL url = null;
        	if( args.length == 1 ){
        		url = getResource( args[0] );
        	}
        	else {
        		// What shapefile (or database) do you want?
        		url = openURL();
        	}
            // Open a connection to the shapefile (or database...)
            DataStore dataStore = openShapefile3( url );
            
            String typeName = dataStore.getTypeNames()[0];
            FeatureSource featureSource = dataStore.getFeatureSource(typeName);
            FeatureCollection featureCollection = featureSource.getFeatures();

            FeatureType featureType = featureSource.getSchema();
            System.out.println("FID\t");

            // print out the normal (non geometry) attributes
            //
            for (int i = 0; i < featureType.getAttributeCount(); i++) {
                AttributeType attributeType = featureType.getAttributeType(i);

                if ( !(attributeType instanceof GeometryAttributeType)) {
                    System.out.print(attributeType.getType().getName() + "\t");
                }
            }

            System.out.println();
            // print out the geometry attributes
            //
            for (int i = 0; i < featureType.getAttributeCount(); i++) {
                AttributeType at = featureType.getAttributeType(i);

                if ( at instanceof GeometryAttributeType) {
                    System.out.print(at.getName() + "\t");
                }
            }

            System.out.println();

            // now print out the first 10 features (non geometric attribute)
            //
            Iterator iterator = featureCollection.iterator();
            try {
	            for( int count=0; iterator.hasNext(); count++) {
	                Feature feature = (Feature) iterator.next();
	                System.out.print(feature.getID() + "\t");
	
	                for (int i = 0; i < feature.getNumberOfAttributes(); i++) {
	                    Object attribute = feature.getAttribute(i);
	
	                    if (!(attribute instanceof Geometry)) {
	                        System.out.print(attribute + "\t");
	                    }
	                }
	                System.out.println();
	                if( count == 10) break; // only 10 please
	            }
            }
            finally {
            	featureCollection.close( iterator );
            }
            System.out.println();

            // and finally print out every geometry in wkt format
            iterator = featureCollection.iterator();
            try {
	            for( int count=0; iterator.hasNext(); count++) {
	                Feature feature = (Feature) iterator.next();
	                System.out.print(feature.getID() + "\t");
	                System.out.println(feature.getDefaultGeometry());
	                System.out.println();
	                
	                if( count == 10) break; // only 10
	            }
            }
            finally {
            	featureCollection.close( iterator );
            }
        } catch (Exception e) {
            System.out.println("Ops! Something went wrong :-(");
            e.printStackTrace();
        }

        System.exit(0);
    }

    /**
     * This method will prompt the user for a shapefile.
     * <p>
     * This method will call System.exit() if the user presses cancel.
     * </p>
     * 
     * @return url to selected shapefile.
     * @throws MalformedURLException
     */
	public static URL openURL() throws MalformedURLException {
		URL shapeURL = null;
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileFilter(new SimpleFileFilter("shp", "Shapefile"));

		int result = fileChooser.showOpenDialog(null);

		if (result == JFileChooser.APPROVE_OPTION) {
		    File f = fileChooser.getSelectedFile();
		    shapeURL = f.toURL();
		} else {
			System.out.println("Goodbye");
			System.exit(0);
		}
		return shapeURL;
	}
	/**
	 * This method will open a shapefile directly using the ShapefileDatastore.
	 * <p>
	 * Please note this is "wrong" and limits your code to working with
	 * shapefiles for no reason. Look at openShapefile2 for a better example.
	 * @see #openShapefile2
	 * @param shapeURL An URL for the shapefile
	 * @return the ShapefileDataStore which contains the shapefile data.
	 * @throws MalformedURLException
	 */
    public static ShapefileDataStore openShapefile( URL shapeURL ) throws MalformedURLException{
    	return new ShapefileDataStore(shapeURL);
    }
    /**
     * You can use the DataStoreFinder to find additional kinds of data access (including shapefile).
     * <p>
     * This will check the available jars on your class path, and return the first one that can
     * work with the provided url. This example goes through each data store, geoserver actually
     * includes two datastores that can read shapefiles (one with indexing one without).
     * </p>
     * <p>
     * For your amusement we print out the parameters for the datastore used.
     * </p>
     * @param url
     * @return DataStore for the provided URL
     * @throws IOException If connection could not be made
     */
    public static DataStore openShapefile2( URL url ) throws IOException{
    	// Step 1: Connection Parameters
    	// We will place the url into a Map, some datastores require several paramters
    	// for now we will just use one.
    	Map params = new HashMap();
    	params.put("url", url );
    	
    	DataStoreFactorySpi found = null;
    	// Step 2: grab the list of available datastores
    	for( Iterator iterator = DataStoreFinder.getAvailableDataStores(); iterator.hasNext(); ){
    		DataStoreFactorySpi factory = (DataStoreFactorySpi) iterator.next();    		
    		if( factory.canProcess( params )){
    			System.out.println( factory.getDisplayName() +":("+factory.getDescription()+")");
    			found = factory;
    		}
    	}
    	if( found != null ) {
    		System.out.println( "Using "+found.getDisplayName() );
    		Param[] parameters = found.getParametersInfo();
    		for( int i=0; i<parameters.length;i++){
    			System.out.print( parameters[i] );
    		}
    		return found.createDataStore( params );
    	}
    	throw new IOException("Could not connect to:"+params );
    }
    /**
     * You can use the DataStoreFinder to find additional kinds of data access (including shapefile).
     * <p>
     * This will check the available jars on your class path, and return the first one that can
     * work with the provided url.
     * </p>
     * @param url
     * @return DataStore for the provided URL
     * @throws IOException If connection could not be made
     */
    public static DataStore openShapefile3( URL url ) throws IOException{
    	// Step 1: Connection Parameters
    	// We will place the url into a Map, some datastores require several paramters
    	// for now we will just use one.    	
    	Map params = new HashMap();
    	params.put("url", url );
    	
    	// Step 2: ask the DataStoreFinder for the "best" match
    	return DataStoreFinder.getDataStore( params );
    }
}
