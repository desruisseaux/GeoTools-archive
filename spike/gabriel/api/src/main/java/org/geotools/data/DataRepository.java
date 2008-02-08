package org.geotools.data;

import java.io.IOException;
import java.util.List;

import org.geotools.feature.SchemaException;
import org.opengis.feature.Feature;

import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;

public interface DataRepository<T extends FeatureType, F extends Feature> {

    void createSchema(T featureType) throws IOException;
    
    void updateSchema(String typeName, T featureType)
        throws IOException;
    
    List<Name> getNames() throws IOException;
    
    T getSchema(String typeName) throws IOException;
    
    T getSchema(Name name) throws IOException;
    
    Source<T,F> getView(Query query) throws IOException, SchemaException;
    
    Source<T,F> getFeatureSource(String typeName) throws IOException;

    Reader<T,F> getFeatureReader(Query query, Transaction transaction)
        throws IOException;
    
    Writer<T,F> getFeatureWriter(String typeName, Filter filter, Transaction transaction)
        throws IOException;

    Writer<T,F> getFeatureWriter(String typeName, Transaction transaction)
        throws IOException;

    Writer<T,F> getFeatureWriterAppend(String typeName, Transaction transaction)
        throws IOException;
}
