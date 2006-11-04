/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, GeoTools Project Managment Committee (PMC)
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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.geotools.data.DataStore;
import org.geotools.data.EmptyFeatureWriter;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureWriter;
import org.geotools.data.FilteringFeatureWriter;
import org.geotools.data.InProcessLockingManager;
import org.geotools.data.LockingManager;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.view.DefaultView;
import org.geotools.feature.FeatureType;
import org.geotools.feature.SchemaException;
import org.opengis.filter.Filter;


/**
 * Represents a stating point for implementing your own DataStore.
 *
 * <p>
 * The goal is to have this class provide <b>everything</b> else if you only
 * need to provide:
 * </p>
 *
 * <ul>
 * <li>
 * Set getContents() - set of TypeEntry
 * </li>
 * <li>
 * FeatureReader getFeatureReader( typeName )
 * </li>
 * </ul>
 * 
 * To support writing:
 * <ul>
 * <li>set isWritable to true
 * <li>implement FeatureWriter getFeatureWriter( typeName )
 * </ul>
 *
 * <p>
 * All remaining functionality is implemented against these methods, including
 * Transaction and Locking Support. These implementations will not be optimal
 * but they will work.
 * </p>
 *
 * To support custom query optimizations:
 * <ul>
 * <li> Filter getUnsupportedFilter(String typeName, Filter filter)
 * <li> FeatureReader getFeatureReader(String typeName, Query query)
 * </ul>
 *
 * To provide high-level writing optimizations:
 * <ul>
 * <li> Override createFeatureSource to use your own custom FeatureSource
 * </ul>
 * 
 * To provide low-level writing optimizations:
 * <ul>
 * <li> FeatureWriter getFeatureWriterAppend( typeName, transaction )
 * <li> FeatureWriter getFeatureWriterAppend( typeName, Filter, transaction )
 * <li> 
 * </ul>
 *
 * To provide high-level writing optimizations:
 * <ul>
 * <li> Stop using FeatureWriter completely
 * <li> Override createFeatureStore to use your own custom FeatureStore
 * </ul>
 * <p>
 * Pleae note that there may be a better place for you to start out from, (like
 * JDBCDataStore).
 * </p>
 *
 * @author jgarnett
 * @source $URL$
 */
public class AbstractDataStore2 implements DataStore {
    /** The logger for the filter module. */
    protected static final Logger LOGGER = Logger.getLogger("org.geotools.data");

    /**
     * Manages InProcess locks for FeatureLocking implementations.
     *
     * <p>
     * May be null if subclass is providing real locking.
     * </p>
     */
    private InProcessLockingManager lockingManager;

    /** Default (Writeable) DataStore */
    public AbstractDataStore2() {
        lockingManager = createLockingManager();
    }

    /** List<ActiveTypeEntry> subclass control provided by createContents */
    private List contents = null;
    
    /**
     * Currently returns an InProcessLockingManager.
     *
     * <p>
     * Subclasses that implement real locking may override this method to
     * return <code>null</code>.
     * </p>
     *
     * @return InProcessLockingManager or null.
     */
    protected InProcessLockingManager createLockingManager() {
        return new InProcessLockingManager();
    }
    
    /** List of ActiveTypeEntry entries - one for each featureType provided by this Datastore */
    public List entries() {
        if( contents == null ) {
            contents = createContents();
        }
        return Collections.unmodifiableList( contents );
    }
    
    /**
     * Subclass must overrride to connet to contents.
     * <p>
     * An implementation that has any doubt about its contents should aquire
     * them during object creation (where an IOException can be thrown).
     * </p>
     * <p>
     * This method is lazyly called to create a List of ActiveTypeEntry for
     * each FeatureCollection in this DataStore.
     * </p>
     * @return List<ActiveTypeEntry>.
     */
    protected List createContents() {
        throw new UnsupportedOperationException("createContent not implemented");
    }
    
    /** Convience method for retriving all the names from the Catalog Entires */
    public String[] getTypeNames() {
        List all = entries();
        String names[] = new String[ all.size() ];
        int index = 0;
        for( Iterator i=all.iterator(); i.hasNext(); index++ ) {
            ActiveTypeEntry entry = (ActiveTypeEntry) i.next();
            names[ index ] = entry.getTypeName();
        }
        return names;
    }
    public ActiveTypeEntry entry( String typeName ) {
        if( typeName == null ) return null;
        for( Iterator i=entries().iterator(); i.hasNext(); ) {
            ActiveTypeEntry entry = (ActiveTypeEntry) i.next();
            if( typeName.equals( entry.getTypeName() ) ) {
                return entry;
            }            
        }
        return null;
    }
    /** Retrive schema information for typeName */
    public FeatureType getSchema(String typeName)
        throws IOException{
        return entry( typeName ).getSchema();
    }

    /**
     * Subclass should implement to provide for creation.
     *
     * @param featureType Requested FeatureType
     *
     * @throws IOException Subclass may throw IOException
     * @throws UnsupportedOperationException Subclass may implement
     */
    public void createSchema(FeatureType featureType) throws IOException {
        throw new UnsupportedOperationException("Schema creation not supported");
    }
    /**
     * Subclass should implement to provide modification support.
     */
    public void updateSchema(String typeName, FeatureType featureType)
        throws IOException {
        throw new UnsupportedOperationException("Schema modification not supported");
    }

    // Jody - This is my recomendation for DataStore in order to support CS reprojection and override
    /**
     * Create a FeatureSource that represents your Query.
     * <p>
     * If we can make this part of the public API, we can phase out FeatureResults.
     * (and reduce the number of classes people need to know about).
     * </p>
     */
    public FeatureSource getView(final Query query)
        throws IOException, SchemaException {
        return new DefaultView( getFeatureSource( query.getTypeName() ), query );
    }
    /**
     * Aqure FeatureSource for indicated typeName.
     * <p>
     * Note this API is not sufficient; Namespace needs to be used as well.
     * </p>
     */
    public FeatureSource getFeatureSource( final String typeName )
        throws IOException {        
        return entry( typeName ).getFeatureSource();        
    }
    
        
    /**
     * Access a FeatureReader providing access to Feature information.
     * <p>
     * This implementation passes off responsibility to the following overrideable methods:
     * <ul>
     * <li>getFeatureReader(String typeName) - subclass *required* to implement
     * </ul>
     * </p>
     * <p>If you can handle some aspects of Query natively (say expressions or reprojection) override the following:
     * <li>
     * <li>getFeatureReader(typeName, query) - override to handle query natively
     * <li>getUnsupportedFilter(typeName, filter) - everything you cannot handle natively
     * <li>getFeatureReader(String typeName) - you must implement this, but you could point it back to getFeatureReader( typeName, Query.ALL );
     * </ul>
     * </p>
     */
    public FeatureReader getFeatureReader(Query query, Transaction transaction) throws IOException {
        String typeName = query.getTypeName();
        return entry( typeName ).reader( query, transaction );
    }

    /* (non-Javadoc)
     * @see org.geotools.data.DataStore#getFeatureWriter(java.lang.String, org.geotools.filter.Filter, org.geotools.data.Transaction)
     */
    public FeatureWriter getFeatureWriter(String typeName, Filter filter,
        Transaction transaction) throws IOException {
        if (filter == null) {
            throw new NullPointerException("getFeatureReader requires Filter: "
                + "did you mean Filter.INCLUDE?");
        }

        if (filter == Filter.EXCLUDE) {
            FeatureType featureType = getSchema(typeName);

            return new EmptyFeatureWriter(featureType);
        }

        FeatureWriter writer = getFeatureWriter(typeName, transaction);

        if (filter != Filter.INCLUDE) {
            writer = new FilteringFeatureWriter(writer, filter);
        }

        return writer;
    }
    /**
     * TODO summary sentence for getFeatureWriter ...
     * 
     * @see org.geotools.data.DataStore#getFeatureWriter(java.lang.String, org.geotools.data.Transaction)
     * @param typeName
     * @param transaction
     * @return FeatureWriter
     * @throws IOException
     */
    public FeatureWriter getFeatureWriter( String typeName, Transaction transaction ) throws IOException {
        return entry( typeName ).writer( transaction );
    }
    /**
     * FeatureWriter setup to add new content.
     * 
     * @see org.geotools.data.DataStore#getFeatureWriterAppend(java.lang.String, org.geotools.data.Transaction)
     * @param typeName
     * @param transaction
     * @return FeatureWriter already skipped to the end
     * @throws IOException
     */
    public FeatureWriter getFeatureWriterAppend( String typeName, Transaction transaction ) throws IOException {
        return entry( typeName ).createAppend( transaction );
    }
    
    /**
     * Locking manager used for this DataStore.
     *
     * <p>
     * By default AbstractDataStore makes use of InProcessLockingManager.
     * </p>
     *
     *
     * @see org.geotools.data.DataStore#getLockingManager()
     */
    public LockingManager getLockingManager() {
        return lockingManager;
    }
}
