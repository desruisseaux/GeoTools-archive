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
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/spike/gabriel/api-nogenerics/src/main/java/org/geotools/data/sample/SampleDataStore.java $
 */
public class SampleDataStore implements DataStore {

    /**
     * @see org.geotools.data.DataStore#createSchema(org.opengis.feature.simple.SimpleFeatureType)
     */
    public void createSchema(SimpleFeatureType featureType) throws IOException {
    }

    /**
     * CHECK: overloaded addition due to lack of arguments parametrization
     * 
     * @see org.geotools.data.FeatureData#createSchema(org.opengis.feature.type.FeatureType)
     */
    public void createSchema(FeatureType featureType) throws IOException {
    }

    /**
     * @see org.geotools.data.DataStore#updateSchema(java.lang.String,
     *      org.opengis.feature.simple.SimpleFeatureType)
     */
    public void updateSchema(String typeName, SimpleFeatureType featureType) throws IOException {
    }

    /**
     * CHECK: overloaded addition due to lack of arguments parametrization
     * 
     * @see org.geotools.data.FeatureData#updateSchema(java.lang.String,
     *      org.opengis.feature.type.FeatureType)
     */
    public void updateSchema(String typeName, FeatureType featureType) throws IOException {
    }

    /**
     * @see org.geotools.data.DataStore#dispose()
     */
    public void dispose() {
    }

    /**
     * @see org.geotools.data.DataStore#getFeatureReader(org.geotools.data.Query,
     *      org.geotools.data.Transaction)
     */
    public FeatureReader getFeatureReader(Query query, Transaction transaction) throws IOException {
        return null;
    }

    /**
     * @see org.geotools.data.DataStore#getFeatureSource(org.opengis.feature.type.Name)
     */
    public FeatureSource getFeatureSource(Name name) throws IOException {
        return null;
    }

    /**
     * @see org.geotools.data.DataStore#getFeatureSource(java.lang.String)
     */
    public FeatureSource getFeatureSource(String typeName) throws IOException {
        return null;
    }

    /**
     * @see org.geotools.data.DataStore#getFeatureWriter(java.lang.String,
     *      org.opengis.filter.Filter, org.geotools.data.Transaction)
     */
    public FeatureWriter getFeatureWriter(String typeName, Filter filter, Transaction transaction)
            throws IOException {
        return null;
    }

    /**
     * @see org.geotools.data.DataStore#getFeatureWriter(java.lang.String,
     *      org.geotools.data.Transaction)
     */
    public FeatureWriter getFeatureWriter(String typeName, Transaction transaction)
            throws IOException {
        return null;
    }

    /**
     * @see org.geotools.data.DataStore#getFeatureWriterAppend(java.lang.String,
     *      org.geotools.data.Transaction)
     */
    public FeatureWriter getFeatureWriterAppend(String typeName, Transaction transaction)
            throws IOException {
        return null;
    }

    /**
     * @see org.geotools.data.DataStore#getLockingManager()
     */
    public LockingManager getLockingManager() {
        return null;
    }

    /**
     * @see org.geotools.data.DataStore#getSchema(org.opengis.feature.type.Name)
     */
    public SimpleFeatureType getSchema(Name name) throws IOException {
        return null;
    }

    /**
     * @see org.geotools.data.DataStore#getSchema(java.lang.String)
     */
    public SimpleFeatureType getSchema(String typeName) throws IOException {
        return null;
    }

    /**
     * @see org.geotools.data.DataStore#getTypeNames()
     */
    public String[] getTypeNames() throws IOException {
        return null;
    }

    /**
     * @see org.geotools.data.DataStore#getView(org.geotools.data.Query)
     */
    public FeatureSource getView(Query query) throws IOException, SchemaException {
        return null;
    }

    /**
     * @see org.geotools.data.FeatureData#getNames()
     */
    public List<Name> getNames() throws IOException {
        return null;
    }

}
