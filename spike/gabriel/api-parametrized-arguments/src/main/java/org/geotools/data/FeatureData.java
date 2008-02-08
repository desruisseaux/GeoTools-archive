package org.geotools.data;

import java.io.IOException;
import java.util.List;

import org.geotools.feature.SchemaException;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;

/**
 * Access to Feature data, this interface represents a file or service
 * advertising feature content for your use.
 * <p>
 * This class covers the generic case of working with Features, you may also be
 * interested in the DataStore subclass that works with SimpleFeature (and
 * SimpleFeatureType).
 * <p>
 * 
 * @author Jody Garnett
 * 
 * @param <T>
 *            FeatureType describing contents
 * @param <F>
 *            Feature content provided
 */
public interface FeatureData {

    void createSchema(FeatureType featureType) throws IOException;

    void updateSchema(String typeName, FeatureType featureType) throws IOException;

    List<Name> getNames() throws IOException;

    FeatureType getSchema(Name name) throws IOException;

    Source getView(Query query) throws IOException, SchemaException;

    /**
     * Read/write access based on capabilities, client code need an instanceof
     * Store/Locking check as needed
     * 
     * @param name
     * @return
     * @throws IOException
     */
    Source getFeatureSource(Name name) throws IOException;

    /**
     * Write access; if available
     * 
     * @param name
     * @return
     * @throws IOException
     */
    // Store<T,F> getStore(Name name) throws IOException;
    /**
     * Locking support; if available.
     * 
     * @param name
     * @return
     * @throws IOException
     */
    // Locking<T,F> getLocking(Name name ) throws IOException;
}
