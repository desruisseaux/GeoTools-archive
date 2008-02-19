package org.geotools.data;

import java.io.IOException;
import java.util.List;

import org.geotools.feature.FeatureCollection;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;

public interface DataAccess<T extends FeatureType, F extends Feature> {

    /**
     * Information about this service.
     * <p>
     * This method offers access to a summary of header or metadata
     * information describing the service.
     * </p>
     * Subclasses may return a specific ServiceInfo instance that has
     * additional information (such as FilterCapabilities). 
     * @return SeviceInfo
     */
    ServiceInfo getInfo();

    /**
     * Creates storage for a new <code>featureType</code>.
     *
     * <p>
     * The provided <code>featureType</code> we be accessable by the typeName
     * provided by featureType.getTypeName().
     * </p>
     *
     * @param featureType FetureType to add to DataStore
     *
     * @throws IOException If featureType cannot be created
     */
    void createSchema(T featureType) throws IOException;

    
    void updateSchema(Name typeName, T featureType)
        throws IOException;
    
    List<Name> getNames() throws IOException;
        
    T getSchema(Name name) throws IOException;
    
    FeatureSource<T,F> getFeatureSource(Name typeName) throws IOException;

    /**
     * Disposes of this data store and releases any resource that it is using.
     * <p>
     * A <code>DataStore</code> cannot be used after <code>dispose</code> has
     * been called, neither can any data access object it helped create, such
     * as {@link FeatureReader}, {@link FeatureSource} or {@link FeatureCollection}.
     * <p>
     * This operation can be called more than once without side effects.
     * <p>
     * There is no thread safety assurance associated with this method. For example,
     * client code will have to make sure this method is not called while retrieving/saving data
     * from/to the storage, or be prepared for the consequences.
     */
    void dispose();

    //FeatureSource<T,F> getView(Query query) throws IOException, SchemaException;

    //FeatureReader<T,F> getFeatureReader(Query query, Transaction transaction)
    //    throws IOException;
    
    //FeatureWriter<T,F> getFeatureWriter(Name typeName, Filter filter, Transaction transaction)
    //    throws IOException;

    //FeatureWriter<T,F> getFeatureWriter(Name typeName, Transaction transaction)
    //    throws IOException;

    //FeatureWriter<T,F> getFeatureWriterAppend(Name typeName, Transaction transaction)
    //    throws IOException;
}
