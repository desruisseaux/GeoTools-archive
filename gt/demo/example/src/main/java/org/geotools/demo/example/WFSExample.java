package org.geotools.demo.example;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DefaultQuery;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.geometry.jts.JTS;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.spatial.Intersects;

import com.vividsolutions.jts.geom.Envelope;

public class WFSExample {
	/**
	 * Before running this application please install and start geoserver on your local machine.
	 * @param args
	 */
	public static void main( String[] args ){
		String getCapabilities =
			"http://localhost:8080/geoserver/wfs?service=WFS&request=GetCapabilities";
		if( args.length != 0 ){
			getCapabilities = args[0];
		}		
		try {
			supressInfo();
			dataAccess( getCapabilities );
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void supressInfo(){
		Logger.getLogger("org.geotools.gml").setLevel( Level.SEVERE );
		Logger.getLogger("net.refractions.xml").setLevel( Level.SEVERE);
	}
	
	public static void dataAccess( String getCapabilities ) throws Exception {
		// Step 1 - connection parameters
		//
		Map connectionParameters = new HashMap();
		connectionParameters.put("WFSDataStoreFactory:GET_CAPABILITIES_URL", getCapabilities );
		
		// Step 2 - connection
		DataStore data = DataStoreFinder.getDataStore( connectionParameters );
		
		// Step 3 - discouvery
		String typeNames[] = data.getTypeNames();
		String typeName = typeNames[0];
		FeatureType schema = data.getSchema( typeName );
		System.out.println( "Schema Attributes:"+schema.getAttributeCount() );
		
		// Step 4 - target
		FeatureSource source = data.getFeatureSource( typeName );
		System.out.println( "Metadata Bounds:"+ source.getBounds() );

		// Step 5 - query
		String geomName = schema.getDefaultGeometry().getName();
		Envelope bbox = new Envelope( -100.0, -70, 25, 40 );
		
        FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2( GeoTools.getDefaultHints() );
		Object polygon = JTS.toGeometry( bbox );
        Intersects filter = ff.intersects( ff.property( geomName ), ff.literal( polygon ) );
		
		Query query = new DefaultQuery( typeName, filter, new String[]{ geomName } );
		FeatureCollection features = source.getFeatures( query );

		Envelope bounds = new Envelope();
		Iterator iterator = features.iterator();
		try {
			while( iterator.hasNext() ){
				Feature feature = (Feature) iterator.next();
				
				bounds.expandToInclude( feature.getBounds() );
			}
			System.out.println( "Calculated Bounds:"+ bounds );
		}
		finally {
			features.close( iterator );
		}
	}

	public static void dataUpdate( String getCapabilities ) throws Exception {
		// Step 1 - connection parameters
		//
		Map connectionParameters = new HashMap();
		connectionParameters.put("WFSDataStoreFactory:GET_CAPABILITIES_URL", getCapabilities );
		
		// Step 2 - connection
		DataStore data = DataStoreFinder.getDataStore( connectionParameters );
		
		// Step 3 - discouvery
		String typeNames[] = data.getTypeNames();
		String typeName = typeNames[0];
		FeatureType schema = data.getSchema( typeName );
		System.out.println( "Schema Attributes:"+schema.getAttributeCount() );
		
		// Step 4 - target
		FeatureSource source = data.getFeatureSource( typeName );
		System.out.println( "Metadata Bounds:"+ source.getBounds() );

		// Step 5 - query
		FilterFactory ff = CommonFactoryFinder.getFilterFactory( GeoTools.getDefaultHints() );
		
		DefaultQuery query = new DefaultQuery( typeName, Filter.INCLUDE );
		query.setMaxFeatures(2);
		FeatureCollection features = source.getFeatures( query );

		String fid = null;
		Iterator iterator = features.iterator();
		try {
			while( iterator.hasNext() ){
				Feature feature = (Feature) iterator.next();
				fid = feature.getID();
			}
		}
		finally {
			features.close( iterator );
		}
		// step 6 modify
		Transaction t = new DefaultTransaction();

		FeatureStore store = (FeatureStore) source;
		store.setTransaction( t );
		Filter filter = ff.id( Collections.singleton( ff.featureId(fid)));
		try {
			store.removeFeatures( filter );
		}
		finally {
			t.rollback();
		}
	}

}