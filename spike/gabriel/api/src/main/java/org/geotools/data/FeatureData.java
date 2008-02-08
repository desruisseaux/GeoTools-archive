package org.geotools.data;

import java.io.IOException;
import java.util.List;

import org.geotools.feature.SchemaException;
import org.opengis.feature.Feature;

import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;

/**
 * Access to Feature data, this interface represents a file or service
 * advertising feature content for your use.
 * <p>
 * This class covers the generic case of working with Features, you
 * may also be interested in the DataStore subclass that works with
 * SimpleFeature (and SimpleFeatureType).
 * <p>
 * @author Jody Garnett
 *
 * @param <T> FeatureType describing contents
 * @param <F> Feature content provided
 */
public interface FeatureData<T extends FeatureType, F extends Feature> {

    void createSchema(T featureType) throws IOException;
    
    void updateSchema(String typeName, T featureType)
        throws IOException;
    
    List<Name> getNames() throws IOException;
    T getSchema(Name name) throws IOException;
    
    Source<T,F> getView(Query query) throws IOException, SchemaException;
    
    /**
     * Read access
     * @param name
     * @return
     * @throws IOException
     */
    Source<T,F> getSource(Name name) throws IOException;
    
    /**
     * Write access; if available
     * @param name
     * @return
     * @throws IOException
     */
    Store<T,F> getStore(Name name) throws IOException;
    /**
     * Locking support; if available.
     * @param name
     * @return
     * @throws IOException
     */
    Locking<T,F> getLocking(Name name ) throws IOException;

}
