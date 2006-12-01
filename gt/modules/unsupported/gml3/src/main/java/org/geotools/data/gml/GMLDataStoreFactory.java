package org.geotools.data.gml;

import java.io.IOException;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.xml.Configuration;

/**
 * Datastore factory for gml datastore.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class GMLDataStoreFactory implements DataStoreFactorySpi {

	/**
	 * The location of the instance document.
	 */
	public Param LOCATION = 
		new Param( "location", String.class, "Instance document location", true );
	
	/**
	 * The application schema configuration
	 */
	public Param CONFIGURATION =
		new Param( "configuration", Class.class, "Application schema configuration", false );
	
		
	public DataStore createDataStore(Map params) throws IOException {
		String location = (String) LOCATION.lookUp( params );
		Class configuration = (Class) CONFIGURATION.lookUp( params );
	
		if ( location != null ) {
			if ( configuration != null ) {
				try {
					return new GMLDataStore( location, (Configuration) configuration.newInstance() );
				} 
				catch( Exception e ){
					throw (IOException) new IOException().initCause( e );
				}
			}
			
			return new GMLDataStore( location );
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
			LOCATION, CONFIGURATION
		};
	}

	public boolean canProcess(Map params) {
		return params.containsKey( LOCATION.key );
	}

	public boolean isAvailable() {
		return true;
	}

	public Map getImplementationHints() {
		return null;
	}

}
