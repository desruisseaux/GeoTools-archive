
package org.geotools.data.dir;

import java.io.IOException;

import org.geotools.data.AbstractDataStore;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.feature.FeatureType;
import org.geotools.filter.Filter;

/**
 * <p> 
 * DOCUMENT ME!
 * </p>
 * @author dzwiers
 *
 */
public abstract class AbstractFileDataStore extends AbstractDataStore {

    /**
     * @see org.geotools.data.DataStore#getSchema(java.lang.String)
     */
    public abstract FeatureType getSchema() throws IOException;

    protected abstract FeatureReader getFeatureReader()
        throws IOException;
    /**
     * @see org.geotools.data.DataStore#updateSchema(java.lang.String, org.geotools.feature.FeatureType)
     */
    public void updateSchema(FeatureType featureType)
            throws IOException{
                updateSchema(getSchema().getTypeName(),featureType);
            }

    /**
     * @see org.geotools.data.DataStore#getFeatureSource(java.lang.String)
     */
    public FeatureSource getFeatureSource() throws IOException{
        return getFeatureSource(getSchema().getTypeName());
    }

    /**
     * @see org.geotools.data.DataStore#getFeatureWriter(java.lang.String, org.geotools.filter.Filter, org.geotools.data.Transaction)
     */
    public FeatureWriter getFeatureWriter(Filter filter,
            Transaction transaction) throws IOException{
                return getFeatureWriter(getSchema().getTypeName(),filter,transaction);
            }

    /**
     * @see org.geotools.data.DataStore#getFeatureWriter(java.lang.String, org.geotools.data.Transaction)
     */
    public FeatureWriter getFeatureWriter(Transaction transaction) throws IOException {
        return getFeatureWriter(getSchema().getTypeName(),transaction);
    }

    /**
     * @see org.geotools.data.DataStore#getFeatureWriterAppend(java.lang.String, org.geotools.data.Transaction)
     */
    public FeatureWriter getFeatureWriterAppend(
            Transaction transaction) throws IOException{
        return getFeatureWriterAppend(getSchema().getTypeName(),transaction);
    }
}
