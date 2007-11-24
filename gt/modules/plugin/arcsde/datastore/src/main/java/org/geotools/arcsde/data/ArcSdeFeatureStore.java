package org.geotools.arcsde.data;

import java.io.IOException;
import java.util.Set;

import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureStore;
import org.geotools.data.Transaction;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.Filter;

public class ArcSdeFeatureStore extends ArcSdeFeatureSource implements FeatureStore {

    public ArcSdeFeatureStore(SimpleFeatureType featureType, ArcSDEDataStore arcSDEDataStore) {
        super(featureType, arcSDEDataStore);
    }

    public Transaction getTransaction() {
        return transaction;
    }

    /**
     * @see FeatureStore#setTransaction(Transaction)
     */
    public void setTransaction(final Transaction transaction) {
        if(transaction == null){
            throw new NullPointerException("mean Transaction.AUTO_COMMIT?");
        }
        super.transaction = transaction;
    }


    public Set addFeatures(final FeatureCollection collection) throws IOException {
        return null;
    }

    public void modifyFeatures(final AttributeDescriptor[] type, final Object[] value, final Filter filter)
            throws IOException {
        // TODO Auto-generated method stub

    }

    public void modifyFeatures(final AttributeDescriptor type, final Object value, final Filter filter)
            throws IOException {
        // TODO Auto-generated method stub

    }

    public void removeFeatures(final Filter filter) throws IOException {
        // TODO Auto-generated method stub

    }

    public void setFeatures(final FeatureReader reader) throws IOException {
        // TODO Auto-generated method stub

    }

}
