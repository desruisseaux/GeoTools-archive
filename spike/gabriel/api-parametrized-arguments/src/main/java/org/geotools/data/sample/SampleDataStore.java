package org.geotools.data.sample;

import java.io.IOException;
import java.util.ArrayList;
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
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;

/**
 * Intent: to assert no generics are used in arguments nor return types, but
 * they're narrowed to the SimpleFeature/Type specializations
 * 
 * @author Gabriel Roldan (TOPP)
 * @version $Id$
 * @since 2.5.x
 * @source $URL$
 */
public class SampleDataStore implements DataStore {

    /**
     * @since 2.5
     */
    public List<Name> getNames() throws IOException {
        return null;
    }

    public String[] getTypeNames() throws IOException {
        List<Name> names = getNames();
        List<String> typeNamesOldStyle = new ArrayList<String>(names.size());
        for (Name featureName : names) {
            typeNamesOldStyle.add(featureName.getLocalPart());
        }
        return typeNamesOldStyle.toArray(new String[names.size()]);
    }

    /**
     * @since 2.5
     */
    public SimpleFeatureType getSchema(Name name) throws IOException {
        return getSchema(name.getLocalPart());
    }

    public SimpleFeatureType getSchema(String typeName) throws IOException {
        return null;
    }

    public FeatureReader getFeatureReader(Query query, Transaction transaction) throws IOException {
        return new SampleFeatureReader();
    }

    /**
     * @since 2.5
     */
    public FeatureSource getFeatureSource(Name name) throws IOException {
        return getFeatureSource(name.getLocalPart());
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

    public LockingManager getLockingManager() {
        return null;
    }

    public FeatureSource getView(Query query) throws IOException, SchemaException {
        return new SampleFeatureLocking();
    }

    public void updateSchema(String typeName, SimpleFeatureType featureType) throws IOException {
    }

    public void createSchema(SimpleFeatureType featureType) throws IOException {
    }

    public void dispose() {
    }

    public void createSchema(FeatureType featureType) throws IOException {
    }

    public void updateSchema(String typeName, FeatureType featureType) throws IOException {
    }
}
