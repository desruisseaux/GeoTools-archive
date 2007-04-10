package org.geotools.data.jdbc;

import java.io.IOException;

import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.data.store.ContentState;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.opengis.filter.Filter;

public class JDBCFeatureSource extends ContentFeatureSource {

    public JDBCFeatureSource(ContentEntry entry) {
        super(entry);
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
        return null;
    }

 

    protected FeatureCollection filtered(ContentState state, Filter filter) {
        return null;
    }

}
