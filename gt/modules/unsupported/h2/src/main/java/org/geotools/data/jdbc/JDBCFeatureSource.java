package org.geotools.data.jdbc;

import java.io.IOException;

import org.geotools.data.jdbc.collection.JDBCFeatureCollection;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.data.store.ContentState;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.opengis.filter.Filter;

public class JDBCFeatureSource extends ContentFeatureSource {

	PrimaryKey primaryKey;
	
    public JDBCFeatureSource(ContentEntry entry) throws IOException {
        super(entry);
        
        //figure out the primary key
    	try {
			primaryKey = JDBCUtils.primaryKey( entry.getName(), (JDBCDataStore) entry.getDataStore() );
		} 
    	catch (Exception e) {
    		throw (IOException) new IOException().initCause( e );
		}
	}

    public PrimaryKey getPrimaryKey() {
		return primaryKey;
	}
    
    protected FeatureType buildFeatureType() throws IOException {
        
        try {
            return JDBCUtils.buildFeatureType( 
                entry.getName(), (JDBCDataStore) entry.getDataStore() );
        } 
        catch (Exception e) {
            throw (IOException) new IOException().initCause( e );
        }
    }
    
    protected FeatureCollection all(ContentState state) {
        return new JDBCFeatureCollection( this, (JDBCState) state );
    }

    protected FeatureCollection filtered(ContentState state, Filter filter) {
    	return new JDBCFeatureCollection( this, (JDBCState) state, filter );
    }

}
