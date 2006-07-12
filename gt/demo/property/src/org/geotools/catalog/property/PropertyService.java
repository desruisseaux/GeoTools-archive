package org.geotools.catalog.property;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.swing.Icon;

import org.geotools.catalog.AbstractFileService;
import org.geotools.catalog.Catalog;
import org.geotools.catalog.DataStoreService;
import org.geotools.catalog.GeoResource;
import org.geotools.catalog.ServiceInfo;
import org.geotools.catalog.defaults.DefaultServiceInfo;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.property.PropertyDataStore;
import org.geotools.data.property.PropertyDataStoreFactory;
import org.geotools.util.ProgressListener;

/**
 * Service handle for PropertyDataStore.
 *  
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 */
public class PropertyService extends DataStoreService {

	File directory;
	
	public PropertyService(Catalog parent, Map params, File directory) {
		super(parent, params);
		this.directory = directory;
	}

	public boolean canResolve(Class adaptee) {
		if ( adaptee != null && adaptee.isAssignableFrom( File.class ) ) 
			return true;
		
		return super.canResolve( adaptee );
	}
	
	public Object resolve(Class adaptee, ProgressListener monitor) throws IOException {
		if ( adaptee != null && adaptee.isAssignableFrom( File.class ) ) {
			return directory;
		}
		
		return super.resolve( adaptee, monitor );
	}
	
	protected DataStoreFactorySpi createDataStoreFactory() {
		return new PropertyDataStoreFactory();
	}

	protected GeoResource createGeoResource(String typeName, DataStore dataStore) {
		return new PropertyGeoResource( this, typeName );
	}

	public URI getIdentifier() {
		return directory.toURI();
	}

}
