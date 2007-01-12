package org.geotools.data.postgis;

import java.io.IOException;
import java.util.logging.Logger;

import org.geotools.data.DataStore;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureWriter;
import org.geotools.data.LockingManager;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.store.Content;
import org.geotools.data.store.ContentDataStore;
import org.geotools.feature.FeatureType;
import org.geotools.feature.SchemaException;
import org.opengis.filter.Filter;

public class PostgisDataStore extends ContentDataStore {

    public PostgisDataStore(PostGISContent content) {
        super(content);
        
    }	
    
}
