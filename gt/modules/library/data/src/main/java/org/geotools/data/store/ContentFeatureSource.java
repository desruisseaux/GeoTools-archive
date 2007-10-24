/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.geotools.data.ContentFeatureCollection;
import org.geotools.data.FeatureListener;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.data.ReTypingFeatureCollection;
import org.geotools.data.Transaction;
import org.geotools.factory.Hints;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;

import com.vividsolutions.jts.geom.GeometryFactory;


/**
 * Abstract implementation of FeatureSource.
 * <p>
 * This feature source works off of operations provided by {@link FeatureCollection}.
 * Individual FeatureCollection implementations are provided by subclasses:
 * <ul>
 *   {@link #all(ContentState)}: Access to entire dataset
 *   {@link #filtered(ContentState, Filter)}: Access to filtered dataset
 * </ul>
 * </p>
 * <p>
 * Even though a feature source is read-only, this class is transaction aware.
 * (see {@link #setTransaction(Transaction)}. The transaction is taken into 
 * account during operations such as {@link #getCount()} and {@link #getBounds()}
 * since these values may be affected by another operation (like writing to 
 * a FeautreStore) working against the same transaction. 
 * </p>
 * <p>
 * Subclasses must also implement the {@link #buildFeatureType()} method which 
 * builds the schema for the feature source.
 * </p>
 *
 * @author Jody Garnett, Refractions Research Inc.
 * @author Justin Deoliveira, The Open Planning Project
 */
public abstract class ContentFeatureSource implements FeatureSource {
    /**
     * The entry for the feautre source.
     */
    protected ContentEntry entry;
    /**
     * The transaction to work from
     */
    protected Transaction transaction;
    /**
     * hints
     */
    protected Set<Hints.ClassKey> hints;
    
    /**
     * Creates the new feature source from an entry.
     */
    public ContentFeatureSource(ContentEntry entry) {
        this.entry = entry;
        
        //set up hints
        hints = new HashSet<Hints.ClassKey>();
        hints.add( Hints.JTS_GEOMETRY_FACTORY );
        hints.add( Hints.JTS_COORDINATE_SEQUENCE_FACTORY );
        
        //add subclass specific hints
        addHints( hints );
        
        //make hints unmodifiable
        hints = Collections.unmodifiableSet( hints );
        
    }

    /**
     * The entry for the feature source.
     */
    public ContentEntry getEntry() {
    	return entry;
    }
    
    /**
     * The current transaction the feature source is working against.
     * <p>
     * This transaction is used to derive the state for the feature source. A
     * <code>null</code> value for a transaction represents the auto commit
     * transaction: {@link Transaction#AUTO_COMMIT}.
     * </p>
     * @see {@link #getState()}.
     */
    public Transaction getTransaction() {
        return transaction;
    }
    
    /**
     * Sets the current transaction the feature source is working against.
     * <p>
     * <tt>transaction</tt> may be <code>null</code>. This signifies that the 
     * auto-commit transaction is used: {@link Transaction#AUTO_COMMIT}.
     * </p>
     * @param transaction The new transaction, or <code>null</code>.
     */
    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }
    /**
     * The current state for the feature source.
     * <p>
     * This value is derived from current transaction of the feature source.
     * </p>
     * 
     * @see {@link #setTransaction(Transaction)}.
     */
    public ContentState getState() {
        return entry.getState(transaction);
    }

    /**
     * The datastore that this feature source originated from.
     * <p>
     * Subclasses may wish to extend this method in order to type narrow its 
     * return type.
     * </p>
     */
    public ContentDataStore getDataStore() {
        return entry.getDataStore();
    }

    /**
     * Returns the feature type or the schema of the feature source.
     * <p>
     * This method delegates to {@link #buildFeatureType()}, which must be 
     * implemented by subclasses. The result is cached in 
     * {@link ContentState#getFeatureType()}.
     * </p>
     */
    public final SimpleFeatureType getSchema() {
        ContentState state = entry.getState(transaction);
        SimpleFeatureType featureType = state.getFeatureType();

        if (featureType == null) {
            //build and cache it
            synchronized (state) {
                if (featureType == null) {
                    try {
                        featureType = buildFeatureType();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    state.setFeatureType( featureType);
                }
            }
        }
        return featureType;
    }

    /**
     * Returns the bounds of the entire feature source.
     * <p>
     * This method delegates to the {@link FeatureCollection#getBounds()} method 
     * of the feature collection created by {@link #all(ContentState)}.
     * </p>
     */
    public final ReferencedEnvelope getBounds() throws IOException {
        return all(entry.getState(transaction)).getBounds();
    }

    /**
     * Returns the bounds of the results of the specified query against the 
     * feature source.
     * <p>
     * This method delegates to the {@link FeatureCollection#getBounds()} method 
     * of the feature collection created by {@link #filtered(ContentState, Filter)}.
     * </p>
     */
    public final ReferencedEnvelope getBounds(Query query) throws IOException {
        return filtered(entry.getState(transaction), query.getFilter()).getBounds();
    }

    /**
     * Returns the count of the number of features of the feature source.
     * <p>
     * This method delegates to the {@link FeatureCollection#size()} method of 
     * the feature collection created by {@link #filtered(ContentState, Filter)}.
     * </p>
     */
    public final int getCount(Query query) throws IOException {
        return filtered(entry.getState(transaction), query.getFilter()).size();
    }

    /**
     * Returns the feature collection of all the features of the feature source.
     * <p>
     * This method delegates to {@link #all(ContentState)} which must be 
     * implemented by subclasses.
     * </p>
     */
    public final ContentFeatureCollection getFeatures() throws IOException {
        return getFeatures(Query.ALL);
    }

    /**
     * Returns the feature collection if the features of the feature source which 
     * meet the specified query criteria.
     */
    public final ContentFeatureCollection getFeatures(Query query)
        throws IOException {
        ContentFeatureCollection features = getFeatures(query.getFilter());
        features.setHints( query.getHints() );
        
        if (query.getCoordinateSystemReproject() != null) {
            // features = features.reproject( query.getCoordinateSystemReproject() );
        }

        if (query.getCoordinateSystem() != null) {
            // features = features.toCRS( query.getCoordinateSystem() );
        }

        if (query.getMaxFeatures() != Integer.MAX_VALUE) {
            // features = (FeatureCollection) features.sort(
            //		SortBy.NATURAL_ORDER).subList(0, query.getMaxFeatures());
        }

        if (query.getNamespace() != null) {
            // features = features.toNamespace( query.getNamespace() );
        }

        if (query.getPropertyNames() != Query.ALL_NAMES) {
            SimpleFeatureType retyped = SimpleFeatureTypeBuilder.retype(getSchema(), query.getPropertyNames());
            features = new ReTypingFeatureCollection( features, retyped );
        }
        
        return features;
    }

    /**
     * Returns the feature collection if the features of the feature source which 
     * meet the specified filter criteria.
     * <p>
     * If <tt>filter</tt> is <code>null</code> or equal to {@link Filter#INCLUDE}
     * this method is reduced to {@link #all(ContentState)}, otherwise it is 
     * reduced to {@link #filtered(ContentState, Filter)}.
     * </p>
     */
    public final ContentFeatureCollection getFeatures(Filter filter)
        throws IOException {
        if ((filter == null) || (filter == Filter.INCLUDE)) {
            return all(entry.getState(transaction));
        }

        return filtered(entry.getState(transaction), filter);
    }

    /**
     * Adds an listener or observer to the feature source.
     * <p>
     * Listeners are stored on a per-transaction basis. 
     * </p>
     */
    public final void addFeatureListener(FeatureListener listener) {
        entry.getState(transaction).addListener(listener);
    }

    /**
     * Removes a listener from the feature source.
     */
    public final void removeFeatureListener(FeatureListener listener) {
        entry.getState(transaction).removeListener(listener);
    }

    /**
     * The hints provided by the feature store.
     * <p>
     * Subclasses should implement {@link #addHints(Set)} to provide additional
     * hints.
     * </p>
     * 
     * @see FeatureSource#getSupportedHints()
     */
    public final Set getSupportedHints() {
        return hints;
    }
    
    /**
     * Subclass hook too add additional hints.
     * <p>
     * By default, the followings are already present:
     * <ul>
     *   <li>{@link Hints#JTS_COORDINATE_SEQUENCE_FACTORY}
     *   <li>{@link Hints#JTS_GEOMETRY_FACTORY}
     * </ul>
     * 
     * </p>
     * @param hints The set of hints supported by the feature source.
     */
    protected void addHints( Set<Hints.ClassKey> hints ) {
        
    }
    //
    // Internal API
    //
    /**
     * Creates the feature type or schema for the feature source.
     * <p>
     * Implementations should use {@link SimpleFeatureTypeBuilder} to build the 
     * feature type. Also, the builder should be injected with the feature factory
     * which has been set on the datastore (see {@link ContentDataStore#getFeatureFactory()}.
     * Example:
     * <pre>
     *   SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();
     *   b.setFeatureTypeFactory( getDataStore().getFeatureTypeFactory() );
     *   
     *   //build the feature type
     *   ...
     * </pre>
     * </p>
     */
    protected abstract SimpleFeatureType buildFeatureType() throws IOException;

    /**
     * Returns a new feature collection containing all the features of the 
     * feature source.
     * <p>
     * Subclasses are encouraged to provide a feature collection implementation 
     * which provides optimized access to the underlying data format.
     * </p>
     *
     * @param state The state the feature collection must work from.
     */
    protected abstract ContentFeatureCollection all(ContentState state);

    /**
     * Returns a new feature collection containing all the features of the 
     * feature source which match the specified filter.
     * <p>
     * Subclasses are encouraged to provide a feature collection implementation 
     * which provides optimized access to the underlying data format.
     * </p>
     * @param state The state the feature collection must work from.
     * @param filter The constraint filtering the data to return.
     * 
     */
    protected abstract ContentFeatureCollection filtered(ContentState state, Filter filter);

    /**
     * FeatureList representing sorted content.
     * <p>
     * Available via getFeatureSource():
     * <ul>
     * <li>getFeatures().sort( sort )
     * <li>getFeatures( filter ).sort( sort )
     * <li>getFeatures( filter ).sort( sort ).sort( sort1 );
     * </ul>
     * @param state
     * @param filter
     * @param order List<SortBy> used to determine sort order
     * @return subset of content
     */

    //protected abstract FeatureList sorted(ContentState state, Filter filter, List order);

    /**
     * FeatureCollection optimized for read-only access.
     * <p>
     * Available via getView( filter ):
     * <ul>
     * <li>getFeatures().sort( sort )
     * <li>getFeatures( filter ).sort( sort )
     * </ul>
     * <p>
     * In particular this method of data access is intended for rendering and other high speed
     * operations; care should be taken to optimize the use of FeatureVisitor.
     * <p>
     * @param state
     * @param filter
     * @return readonly access
     */

    //protected abstract FeatureCollection readonly(ContentState state, Filter filter);
}
