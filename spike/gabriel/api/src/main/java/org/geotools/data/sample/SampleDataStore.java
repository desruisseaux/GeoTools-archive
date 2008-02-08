package org.geotools.data.sample;

import java.io.IOException;
import java.util.List;

import org.geotools.data.DataStore;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureWriter;
import org.geotools.data.LockingManager;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.feature.SchemaException;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;

public class SampleDataStore implements DataStore {

    /**
     * @since 2.5
     */
    public List<Name> getNames() throws IOException {
        return null;
    }

    public String[] getTypeNames() throws IOException {
        return null;
    }

    public SimpleFeatureType getSchema(String typeName) throws IOException {
        return null;
    }

    /**
     * @since 2.5
     */
    public SimpleFeatureType getSchema(Name name) throws IOException {
        return null;
    }

    public FeatureSource getView(Query query) throws IOException, SchemaException {
        return new SampleFeatureLocking();
    }

    public FeatureReader getFeatureReader(Query query, Transaction transaction) throws IOException {
        return new SampleFeatureReader();
    }

    public FeatureSource getFeatureSource(String typeName) throws IOException {
        return new SampleFeatureLocking();
    }

    public FeatureWriter getFeatureWriter(String typeName, Filter filter, Transaction transaction)
            throws IOException {
        return new SampleFeatureWriter();
    }

    public FeatureWriter getFeatureWriter(String typeName, Transaction transaction)
            throws IOException {
        return new SampleFeatureWriter();
    }

    public FeatureWriter getFeatureWriterAppend(String typeName, Transaction transaction)
            throws IOException {
        return new SampleFeatureWriter();
    }

    public void createSchema(SimpleFeatureType featureType) throws IOException {
    }

    public void dispose() {
    }


    public LockingManager getLockingManager() {
        return null;
    }


    public void updateSchema(String typeName, SimpleFeatureType featureType) throws IOException {
    }

}
