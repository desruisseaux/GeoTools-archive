package org.geotools.data.h2;

import java.io.IOException;

import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentState;
import org.geotools.feature.FeatureType;
import org.opengis.feature.simple.SimpleTypeFactory;

public class H2ContentState extends ContentState {

	protected H2ContentState(ContentEntry entry) {
		super(entry);
	}

	H2DataStore getDataStore() {
		return (H2DataStore) entry.getDataStore();
	}
	
	/**
     * Builds the primary key for the entry.
     */
    PrimaryKey primaryKey() throws Exception {
    	return H2Utils.primaryKey( this );
    }
    
    protected FeatureType buildFeatureType( SimpleTypeFactory factory ) throws IOException {
    	H2TypeBuilder builder = new H2TypeBuilder( factory );
    	builder.setNamespaceURI( getDataStore().getNamespaceURI() );
    	
    	try {
			return H2Utils.buildFeatureType( this, builder );
		} 
    	catch (Exception e) {
    		throw (IOException) new IOException().initCause( e );
		}
    }
	
}
