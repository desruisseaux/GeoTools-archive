/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package org.geotools.data.dir;

import org.geotools.catalog.CatalogEntry;
import org.geotools.catalog.QueryRequest;
import org.geotools.data.AbstractFileDataStore;
import org.geotools.data.DataStore;
import org.geotools.data.DefaultTypeEntry;
import org.geotools.data.FeatureLock;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureWriter;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.LockingManager;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.TypeEntry;
import org.geotools.data.view.DefaultView;
import org.geotools.feature.FeatureType;
import org.geotools.feature.SchemaException;
import org.geotools.filter.Filter;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * <p>
 * This datastore represents methods of reading an enture directory. It
 * propagates actual reading / writing of the data to the dataStore  which
 * reads / writes the requested format.
 * </p>
 *
 * @author dzwiers
 */
public class DirectoryDataStore implements DataStore, LockingManager {
    // the directory for this ds
    private File dir;

    // map of featureTypes to dataStore instances
    private Map dataStores;

    // suffix order to attempt to store new featureTypes
    private String[] createOrder;

    /** List<TypeEntry> subclass control provided by createContents.
     * <p>
     * Access via entries(), creation by createContents.
     */
    private List contents = null;   
    
    // should not be used
    private DirectoryDataStore() {
    }

    /** List of TypeEntry entries - one for each featureType provided by this Datastore */
    public List entries() {
        if( contents == null ) {
            contents = createContents();
        }
        return Collections.unmodifiableList( contents );
    }
    
    /**
     * Create TypeEntries based on typeName.
     * <p>
     * This method is lazyly called to create a List of TypeEntry for
     * each FeatureCollection in this DataStore.
     * </p>
     * @return List<TypeEntry>.
     */
    protected List createContents() {
        String typeNames[];
        try {
            typeNames = getTypeNames();
            List list = new ArrayList( typeNames.length );
            for( int i=0; i<typeNames.length; i++){
                list.add( createTypeEntry( typeNames[i] ));
            }
            return Collections.unmodifiableList( list );
        }
        catch (IOException help) {
            // Contents are not available at this time!
            //LOGGER.warning( "Could not aquire getTypeName() to build contents" );
            return null;
        }
    }
    /**
     * Create a TypeEntry for the requested typeName.
     * <p>
     * Default implementation is not that smart, subclass is free to override.
     * This method should expand to take in the namespace URI.
     * Or featureType schema - see AbstractDataStore2.
     * </p>
     */
    protected TypeEntry createTypeEntry( final String typeName ) {
        URI namespace;
        try {
            namespace = getSchema( typeName ).getNamespaceURI();
        } catch (IOException e) {
            namespace = null;
        }
        // can optimize with a custom JDBCTypeEntry to allow
        // access to database metadata.
        return new DefaultTypeEntry( this, namespace, typeName );
    }

    /**
     * Metadata search through entries. 
     * 
     * @see org.geotools.catalog.Discovery#search(org.geotools.catalog.QueryRequest)
     * @param queryRequest
     * @return List of matching TypeEntry
     */
    public List search( QueryRequest queryRequest ) {
        if( queryRequest == QueryRequest.ALL ) {
            return entries();
        }
        List queryResults = new ArrayList();
CATALOG: for( Iterator i=entries().iterator(); i.hasNext(); ) {
            CatalogEntry entry = (CatalogEntry) i.next();
METADATA:   for( Iterator m=entry.metadata().values().iterator(); m.hasNext(); ) {
                if( queryRequest.match( m.next() ) ) {
                    queryResults.add( entry );
                    break METADATA;
                }
            }
        }
        return queryResults;
    }
    // This is the *better* implementation of getview from AbstractDataStore
    public FeatureSource getView(final Query query)
        throws IOException, SchemaException {
        return new DefaultView( this.getFeatureSource( query.getTypeName() ), query );
    }
    
    /**
     * Creates a new DirectoryDataStore object.
     *
     * @param f File the directory
     * @param co list of file suffixes in order of preference for creating new
     *        FTs
     *
     * @throws MalformedURLException
     * @throws IOException
     */
    public DirectoryDataStore(File f, String[] co)
        throws MalformedURLException, IOException {
        dir = f;
        createOrder = co;
        dataStores = new HashMap();

        // load list of dataStores by typeName
        File[] children = f.listFiles();

        for (int i = 0; i < children.length; i++) {
            if (children[i].isFile()) {
                AbstractFileDataStore afds = (AbstractFileDataStore) FileDataStoreFinder
                    .getDataStore(children[i].toURL());

                if (afds != null) {
                    dataStores.put(afds.getTypeNames()[0], afds);
                }
            }
        }
    }

    /**
     * @see org.geotools.data.DataStore#getTypeNames()
     */
    public String[] getTypeNames() throws IOException {
        Set l = new HashSet();
        Iterator i = dataStores.values().iterator();

        while (i.hasNext()) {
            AbstractFileDataStore afds = (AbstractFileDataStore) i.next();
            String[] strs = afds.getTypeNames();

            if (strs != null) {
                for (int j = 0; j < strs.length; j++)
                    l.add(strs[j]);
            }
        }

        return (String[]) l.toArray(new String[l.size()]);
    }

    /**
     * @see org.geotools.data.DataStore#getSchema(java.lang.String)
     */
    public FeatureType getSchema(String typeName) throws IOException {
        AbstractFileDataStore afds = (AbstractFileDataStore) dataStores.get(typeName);

        if (afds != null) {
            return afds.getSchema();
        }

        return null;
    }

    /**
     * @see org.geotools.data.DataStore#createSchema(org.geotools.feature.FeatureType)
     */
    public void createSchema(FeatureType featureType) throws IOException {
        boolean notDone = true;
        int i = 0;

        while (notDone && (i < createOrder.length)) {
            File f = new File(dir, featureType.getTypeName() + createOrder[i]);

            if (!f.exists()) {
                AbstractFileDataStore afds = (AbstractFileDataStore) FileDataStoreFinder
                    .getDataStore(f.toURL());

                if (afds != null) {
                    afds.createSchema(featureType);
                    dataStores.put(featureType.getTypeName(), afds);
                    notDone = false;
                }
            }

            i++;
        }
    }

    /**
     * @see org.geotools.data.DataStore#updateSchema(java.lang.String,
     *      org.geotools.feature.FeatureType)
     */
    public void updateSchema(String typeName, FeatureType featureType)
        throws IOException {
        AbstractFileDataStore afds = (AbstractFileDataStore) dataStores.get(typeName);

        if (afds != null) {
            afds.updateSchema(featureType);
        }
    }

    /**
     * @see org.geotools.data.DataStore#getFeatureSource(java.lang.String)
     */
    public FeatureSource getFeatureSource(String typeName)
        throws IOException {
        AbstractFileDataStore afds = (AbstractFileDataStore) dataStores.get(typeName);

        if (afds != null) {
            return afds.getFeatureSource();
        }

        return null;
    }

    /**
     * @see org.geotools.data.DataStore#getFeatureReader(org.geotools.data.Query,
     *      org.geotools.data.Transaction)
     */
    public FeatureReader getFeatureReader(Query query, Transaction transaction)
        throws IOException {
        AbstractFileDataStore afds = (AbstractFileDataStore) dataStores.get(query
                .getTypeName());

        if (afds != null) {
            return afds.getFeatureReader(query, transaction);
        }

        return null;
    }

    /**
     * @see org.geotools.data.DataStore#getFeatureWriter(java.lang.String,
     *      org.geotools.filter.Filter, org.geotools.data.Transaction)
     */
    public FeatureWriter getFeatureWriter(String typeName, Filter filter,
        Transaction transaction) throws IOException {
        AbstractFileDataStore afds = (AbstractFileDataStore) dataStores.get(typeName);

        if (afds != null) {
            return afds.getFeatureWriter(filter, transaction);
        }

        return null;
    }

    /**
     * @see org.geotools.data.DataStore#getFeatureWriter(java.lang.String,
     *      org.geotools.data.Transaction)
     */
    public FeatureWriter getFeatureWriter(String typeName,
        Transaction transaction) throws IOException {
        AbstractFileDataStore afds = (AbstractFileDataStore) dataStores.get(typeName);

        if (afds != null) {
            return afds.getFeatureWriter(transaction);
        }

        return null;
    }

    /**
     * @see org.geotools.data.DataStore#getFeatureWriterAppend(java.lang.String,
     *      org.geotools.data.Transaction)
     */
    public FeatureWriter getFeatureWriterAppend(String typeName,
        Transaction transaction) throws IOException {
        AbstractFileDataStore afds = (AbstractFileDataStore) dataStores.get(typeName);

        if (afds != null) {
            return afds.getFeatureWriterAppend(transaction);
        }

        return null;
    }

    /**
     * @see org.geotools.data.DataStore#getLockingManager()
     */
    public LockingManager getLockingManager() {
        return this;
    }

    /**
     * @see org.geotools.data.LockingManager#exists(java.lang.String)
     */
    public boolean exists(String authID) {
        Iterator i = dataStores.values().iterator();

        while (i.hasNext()) {
            AbstractFileDataStore afds = (AbstractFileDataStore) i.next();

            if ((afds.getLockingManager() != null)
                    && afds.getLockingManager().exists(authID)) {
                return true;
            }
        }

        return false;
    }

    /**
     * @see org.geotools.data.LockingManager#release(java.lang.String,
     *      org.geotools.data.Transaction)
     */
    public boolean release(String authID, Transaction transaction)
        throws IOException {
        Iterator i = dataStores.values().iterator();

        while (i.hasNext()) {
            AbstractFileDataStore afds = (AbstractFileDataStore) i.next();

            if ((afds.getLockingManager() != null)
                    && afds.getLockingManager().exists(authID)) {
                return afds.getLockingManager().release(authID, transaction);
            }
        }

        return false;
    }

    /**
     * @see org.geotools.data.LockingManager#refresh(java.lang.String,
     *      org.geotools.data.Transaction)
     */
    public boolean refresh(String authID, Transaction transaction)
        throws IOException {
        Iterator i = dataStores.values().iterator();

        while (i.hasNext()) {
            AbstractFileDataStore afds = (AbstractFileDataStore) i.next();

            if ((afds.getLockingManager() != null)
                    && afds.getLockingManager().exists(authID)) {
                return afds.getLockingManager().refresh(authID, transaction);
            }
        }

        return false;
    }

    /**
     * @see org.geotools.data.LockingManager#unLockFeatureID(java.lang.String,
     *      java.lang.String, org.geotools.data.Transaction,
     *      org.geotools.data.FeatureLock)
     */
    public void unLockFeatureID(String typeName, String authID,
        Transaction transaction, FeatureLock featureLock)
        throws IOException {
        AbstractFileDataStore afds = (AbstractFileDataStore) dataStores.get(typeName);

        if ((afds != null) && (afds.getLockingManager() != null)) {
            afds.getLockingManager().unLockFeatureID(typeName, authID,
                transaction, featureLock);
        }
    }

    /**
     * @see org.geotools.data.LockingManager#lockFeatureID(java.lang.String,
     *      java.lang.String, org.geotools.data.Transaction,
     *      org.geotools.data.FeatureLock)
     */
    public void lockFeatureID(String typeName, String authID,
        Transaction transaction, FeatureLock featureLock)
        throws IOException {
        AbstractFileDataStore afds = (AbstractFileDataStore) dataStores.get(typeName);

        if ((afds != null) && (afds.getLockingManager() != null)) {
            afds.getLockingManager().lockFeatureID(typeName, authID,
                transaction, featureLock);
        }
    }
}
