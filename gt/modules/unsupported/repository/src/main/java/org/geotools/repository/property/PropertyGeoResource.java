package org.geotools.catalog.property;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import org.geotools.catalog.AbstractFileGeoResource;
import org.geotools.catalog.FeatureSourceGeoResource;

import org.geotools.data.property.PropertyDataStore;
import org.geotools.data.property.PropertyFeatureSource;
import org.geotools.util.ProgressListener;

public class PropertyGeoResource extends FeatureSourceGeoResource {

	public PropertyGeoResource( PropertyService service, String name ) {
		super( service, name );
	}

	public boolean canResolve(Class adaptee) {
		if ( adaptee == null) 
			return false;
	
		
		if ( adaptee.isAssignableFrom( File.class ) ) {
			return true;
		}
		
		return super.canResolve( adaptee );
	}
	
	public Object resolve(Class adaptee, ProgressListener monitor) throws IOException {
		if ( adaptee == null ) 
			return null;
		
		if ( adaptee.isAssignableFrom( File.class ) ) {
			PropertyService service = (PropertyService) parent( monitor );
			return new File( service.directory, getName() + ".properties" );
		}
		
		return super.resolve( adaptee, monitor );
	}
	
}
