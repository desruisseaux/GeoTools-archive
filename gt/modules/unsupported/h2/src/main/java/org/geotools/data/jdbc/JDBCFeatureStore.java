package org.geotools.data.jdbc;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureStore;
import org.geotools.data.store.ContentEntry;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.Filter;

public class JDBCFeatureStore extends JDBCFeatureSource
    implements FeatureStore {

    public JDBCFeatureStore(ContentEntry entry) throws IOException {
        super(entry);
        
    }

    public Set addFeatures(FeatureCollection collection) throws IOException {
        all(entry.getState(transaction)).addAll(collection);
       
        //gather up all the ids
        Set ids = new TreeSet();
        for ( Iterator i = collection.iterator(); i.hasNext(); ) {
            SimpleFeature feature = (SimpleFeature) i.next();
            String fid = (String) feature.getUserData().get( "fid" );
            if ( fid != null ) {
                ids.add( fid );
            }
        }
        
        return ids;
    }

    public void setFeatures(FeatureReader reader) throws IOException {
        FeatureCollection features = all( getState() );
        features.clear();
        
        while( reader.hasNext() ) {
            features.add( reader.next() );
        }
    }
    
    public void modifyFeatures(AttributeDescriptor type, Object value,
            Filter filter) throws IOException {
        modifyFeatures( new AttributeDescriptor[]{ type }, new Object[]{ value }, filter );
    }
   
    public void modifyFeatures(AttributeDescriptor[] type, Object[] value,
            Filter filter) throws IOException {
        
        if ( filter == null ) {
            String msg = "Must specify a filter, must not be null.";
            throw new IllegalArgumentException( msg );
        }
        
        JDBCFeatureCollection features = filtered(getState(), filter);
        features.update(type, value);
    }

    public void removeFeatures(Filter filter) throws IOException {
        if ( filter == null ) {
            String msg = "Must specify a filter, must not be null.";
            throw new IllegalArgumentException( msg );
        }
        
        filtered( getState(), filter ).clear();
    }
}
