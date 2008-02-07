package org.geotools.data;

import java.io.IOException;

import org.geotools.feature.SchemaException;
import org.opengis.feature.Feature;

import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;

public interface DataAccess<T extends FeatureType, F extends Feature> {

    void createSchema(T featureType) throws IOException;
    
    void updateSchema(String typeName, T featureType)
        throws IOException;
    
    T getSchema(String typeName) throws IOException;
    
    FeatureSource<T,F> getView(Query query) throws IOException, SchemaException;
    
    FeatureSource<T,F> getFeatureSource(String typeName) throws IOException;

    FeatureReader<T,F> getFeatureReader(Query query, Transaction transaction)
        throws IOException;
    
    FeatureWriter<T,F> getFeatureWriter(String typeName, Filter filter, Transaction transaction)
        throws IOException;

    FeatureWriter<T,F> getFeatureWriter(String typeName, Transaction transaction)
        throws IOException;

    FeatureWriter<T,F> getFeatureWriterAppend(String typeName, Transaction transaction)
        throws IOException;
}
