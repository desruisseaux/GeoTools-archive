/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, GeoTools Project Managment Committee (PMC)
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
 */
package org.geotools.data.store;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.vividsolutions.jts.geom.GeometryFactory;
import org.opengis.feature.simple.SimpleFeatureFactory;
import org.opengis.feature.simple.SimpleTypeFactory;
import org.opengis.feature.type.TypeName;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.FeatureWriter;
import org.geotools.data.LockingManager;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.collection.DelegateFeatureReader;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.feature.SchemaException;


/**
 * Abstract implementation of DataStore.
 * <p>
 * Subclasses must implement the following methods.
 * <ul>
 *   <li>{@link #createTypeNames()}
 *   <li>{@link #createFeatureSource(ContentEntry)}
 * </ul>
 *
 * @author Jody Garnett, Refractions Research Inc.
 * @author Justin Deoliveira, The Open Planning Project
 */
public abstract class ContentDataStore implements DataStore {
	/**
	 * logging instance
	 */
	static final Logger LOGGER = Logger.getLogger( "org.geotools.data" );
	
	/**
     * Map<TypeName,ContentEntry> one for each kind of content served up.
     */
    final Map entries;

    /**
     * Factory used to create feature types
     */
    protected SimpleTypeFactory typeFactory;

    /**
     * Factory used to create features
     */
    protected SimpleFeatureFactory featureFactory;

    /**
     * Factory used to create filters
     */
    protected FilterFactory filterFactory;

    /**
     * Factory used to create geometries
     */
    protected GeometryFactory geometryFactory;

    /**
     * Application namespace uri of the datastore
     */
    protected String namespaceURI;

    public ContentDataStore() {
        this.entries = new HashMap();
    }

    //
    // Property accessors
    //
    public void setTypeFactory(SimpleTypeFactory typeFactory) {
        this.typeFactory = typeFactory;
    }

    public SimpleTypeFactory getTypeFactory() {
        return typeFactory;
    }

    public void setFeatureFactory(SimpleFeatureFactory featureFactory) {
        this.featureFactory = featureFactory;
    }

    public SimpleFeatureFactory getFeatureFactory() {
        return featureFactory;
    }

    public void setFilterFactory(FilterFactory filterFactory) {
        this.filterFactory = filterFactory;
    }

    public FilterFactory getFilterFactory() {
        return filterFactory;
    }

    public GeometryFactory getGeometryFactory() {
        return geometryFactory;
    }

    public void setGeometryFactory(GeometryFactory geometryFactory) {
        this.geometryFactory = geometryFactory;
    }

    public void setNamespaceURI(String namespaceURI) {
        this.namespaceURI = namespaceURI;
    }

    public String getNamespaceURI() {
        return namespaceURI;
    }

    //
    // DataStore API
    //

    /**
     * This method delegates to {@link #createTypeNames()}.
     *
     * @see DataStore#getTypeNames()
     */
    public final String[] getTypeNames() throws IOException {
        List typeNames = createTypeNames();
        String[] names = new String[typeNames.size()];

        for (int i = 0; i < typeNames.size(); i++) {
            TypeName typeName = (TypeName) typeNames.get(i);
            names[i] = typeName.getLocalPart();
        }

        return names;
    }

    /**
     * Calls through to <code>getFeatureSource(typeName).getSchema()</code>
     *
     * @see DataStore#getSchema(String)
     */
    public final FeatureType getSchema(String typeName)
        throws IOException {
        return getFeatureSource(typeName).getSchema();
    }

    /**
     * Delegates to {@link #getFeatureSource(TypeName, Transaction)}.
     *
     * @see DataStore#getFeatureSource(String)
     */
    public final FeatureSource getFeatureSource(String typeName)
        throws IOException {
        return getFeatureSource(name(typeName), Transaction.AUTO_COMMIT);
    }

    /**
     * Returns a feature source for a feature type and transaction.
     * <p>
     * The resulting feature source is cached in the state of the entry for
     * the type.
     * </p>
     *
     * @param typeName The entry name.
     * @param tx A transaction.
     *
     * @return The feature source for the name and transaction.
     *
     */
    public final FeatureSource getFeatureSource(TypeName typeName, Transaction tx)
        throws IOException {
    	
        ContentEntry entry = ensureEntry(typeName);

        ContentFeatureSource featureSource = createFeatureSource(entry);
        featureSource.setTransaction(tx);
        
        return featureSource;
    }

    /**
     * Delegates to {@link #query(Query, Transaction)} and wraps the result in
     * a {@link DelegateFeatureReader}.
     *
     * @see DataStore#getFeatureReader(Query, Transaction)
     */
    public final FeatureReader getFeatureReader(Query query, Transaction transaction)
        throws IOException {
        FeatureCollection collection = query(query, transaction);

        return new DelegateFeatureReader(collection.getSchema(), collection.features());
    }

    /**
     * The default implementation of this method throws a
     * {@link UnsupportedOperationException}, subclasses should implement to
     * support schema creation.
     *
     * @see DataStore#createSchema(FeatureType)
     */
    public void createSchema(FeatureType featureType)
        throws IOException {
        throw new UnsupportedOperationException();
    }

    public final FeatureWriter getFeatureWriter(String typeName, Filter filter,
        Transaction transaction) throws IOException {
        return null;
    }

    public final FeatureWriter getFeatureWriterAppend(String typeName, Transaction transaction)
        throws IOException {
        return null;
    }

    public final LockingManager getLockingManager() {
        return null;
    }

    public final FeatureSource getView(Query query) throws IOException, SchemaException {
        return null;
    }

    public final void updateSchema(String typeName, FeatureType featureType)
        throws IOException {
    }

    public final FeatureWriter getFeatureWriter(String typeName, Transaction transaction)
        throws IOException {
        return null;
    }

    //
    // Internal API
    //

    /**
     * Creates a set of qualified names corresponding to the types that the
     * datastore provides.
     * <p>
     * Namespaces may be left <code>null</code> for data stores which do not
     * support namespace qualified type names.
     * </p>
     *
     * @return A list of {@link TypeName}.
     *
     * @throws IOException Any errors occuring connecting to data.
     */
    protected abstract List /*<TypeName>*/ createTypeNames()
        throws IOException;

    /**
     * Instantiates new feature source for the entry.
     * <p>
     * Subclasses should override this method to return a specific subclass of
     * {@link ContentFeatureSource}.
     * </p>
     * @param entry The entry.
     *
     * @return An new instance of {@link ContentFeatureSource} for the entry.
     */
    protected abstract ContentFeatureSource createFeatureSource(ContentEntry entry);

    /**
     * Instantiates a new conent state for the entry.
     * <p>
     * Subclasses may override this method to return a specific subclass of 
     * {@link ContentState}.
     * </p>
     * @param entry The entry.
     * 
     * @return A new instance of {@link ContentState} for the entry.
     *
     */
    protected ContentState createContentState(ContentEntry entry) {
    	return new ContentState( entry );
    }
    
    /**
     * Helper method to wrap a non-qualified name.
     */
    final protected TypeName name(String typeName) {
        return new org.geotools.feature.type.TypeName(typeName);
    }

    /**
     * Helper method to look up an entry in the datastore.
     * <p>
     * This method will create a new instance of {@link ContentEntry} if one
     * does not exist.
     * </p>
     * <p>
     * In the event that the name does not map to an entry
     * and one cannot be created <code>null</code> will be returned. Note that
     * {@link #ensureEntry(TypeName)} will throw an exception in this case.
     * </p>
     *
     * @param The name of the entry.
     *
     * @return The entry, or <code>null</code> if it does not exist.
     */
    final protected ContentEntry entry(TypeName name) throws IOException {
        ContentEntry entry = null;

        //do we already know about the entry
        if (!entries.containsKey(name)) {
            //is this type available?
            List typeNames = createTypeNames();

            if (typeNames.contains(name)) {
                //yes, create an entry for it
                synchronized (this) {
                    if (!entries.containsKey(name)) {
                        entry = new ContentEntry(this, name);
                        entries.put(name, entry);
                    }
                }

                entry = (ContentEntry) entries.get(name);
            }
        }

        return (ContentEntry) entries.get(name);
    }

    /**
     * Helper method to look up an entry in the datastore which throws an
     * {@link IOException} in the event that the entry does not exist.
     *
     * @param name The name of the entry.
     *
     * @return The entry.
     *
     * @throws IOException If hte entry does not exist, or if there was an error
     * looking it up.
     */
    final protected ContentEntry ensureEntry(TypeName name)
        throws IOException {
        ContentEntry entry = entry(name);

        if (entry == null) {
            throw new IOException("Schema '" + name + "' does not exist.");
        }

        return entry;
    }

    /**
     * Helper method for returning the feature collection for a
     * query / transaction pair.
     * <p>
     * The implementation of this method delegates to
     * {@link ContentFeatureSource#getFeatures(Query)}
     * </p>
     *
     * @param query The query to make against the datastore.
     * @param transaction A transaction
     *
     * @return A FeatureCollection matching the query.
     *
     * @throws IOException Any errors that occur when interacting with the data.
     */
    final protected FeatureCollection query(Query query, Transaction transaction)
        throws IOException {
        FeatureSource source = getFeatureSource(query.getTypeName());

        //TODO: transaction should be moved up to FeatureSource
        if (source instanceof FeatureStore) {
            ((FeatureStore) source).setTransaction(transaction);
        }

        return source.getFeatures(query);
    }
}
