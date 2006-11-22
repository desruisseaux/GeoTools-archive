package org.geotools.data.gml;

import java.io.IOException;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;

/**
 * Datastore factory for gml datastore.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class GMLDataStoreFactory implements DataStoreFactorySpi {

	/**
	 * The application schema namespace.
	 */
	public Param NAMESPACE = new Param( "namespace", String.class, "Application schema namespace", true );
	/**
	 * The location of the instance document.
	 */
	public Param LOCATION = 
		new Param( "location", String.class, "Instance document location", true );
	/**
	 * The location of the application schema.
	 */
	public Param SCHEMA_LOCATION = 
		new Param( "schemaLocation", String.class, "Application schema location", false );

	
	public DataStore createDataStore(Map params) throws IOException {
		String namespace = (String) NAMESPACE.lookUp( params );
		String location = (String) LOCATION.lookUp( params );
		String schemaLocation = (String) SCHEMA_LOCATION.lookUp( params );
		
		if ( location != null ) {
			return new GMLDataStore( namespace, location, schemaLocation );
		}
		
		return null;
	}

	public DataStore createNewDataStore(Map params) throws IOException {
		throw new UnsupportedOperationException();
	}

	public String getDisplayName() {
		return "GML";
	}

	public String getDescription() {
		return "Geographic Markup Language";
	}

	public Param[] getParametersInfo() {
		return new Param[] {
			NAMESPACE, LOCATION, SCHEMA_LOCATION	
		};
	}

	public boolean canProcess(Map params) {
		return params.containsKey( NAMESPACE.key ) && params.containsKey( LOCATION.key );
	}

	public boolean isAvailable() {
		return true;
	}

	public Map getImplementationHints() {
		return null;
	}

}
