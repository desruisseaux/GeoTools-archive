package org.geotools.data.h2;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.geotools.data.Query;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentState;
import org.geotools.feature.FeatureType;
import org.opengis.feature.simple.SimpleTypeFactory;
import org.opengis.filter.Filter;

import com.vividsolutions.jts.geom.Envelope;

public class H2ContentState extends ContentState {

	protected H2ContentState(ContentEntry entry) {
		super(entry);
	}

	public H2DataStore getDataStore() {
		return (H2DataStore) entry.getDataStore();
	}
	
	/**
     * Builds the primary key for the entry.
     */
    public PrimaryKey primaryKey() throws Exception {
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
