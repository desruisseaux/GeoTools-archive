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
import com.vividsolutions.jts.geom.Envelope;
import org.opengis.feature.simple.SimpleTypeFactory;
import org.opengis.filter.Filter;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureListener;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.feature.simple.SimpleTypeBuilder;


/**
 * Abstract implementation of FeatureSource.
 * <p>
 * Implementations are based around feature collections. Subclasses must
 * implement the following methods to provide feature collection based data
 * access:
 * <ul>
 * <li>{@link #all(ContentState)}: Access to entire dataset
 * <li>{@link #filtered(ContentState, Filter)}: Access to filtered dataset
 * </ul>
 * In addition, subclasses must implement {@link #buildFeatureType(SimpleTypeFactory)}
 * in which constructs the feature type for the feature.
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

    public ContentFeatureSource(ContentEntry entry) {
        this.entry = entry;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public final DataStore getDataStore() {
        return entry.getDataStore();
    }

    public final FeatureType getSchema() {
        SimpleTypeFactory typeFactory = entry.getDataStore().getTypeFactory();
        ContentState state = entry.getState(transaction);
        FeatureType featureType = (FeatureType) state.get(typeFactory.getClass());

        if (featureType == null) {
            //build and cache it
            synchronized (state) {
                if (featureType == null) {
                    try {
                        featureType = buildFeatureType();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    state.put(typeFactory.getClass(), featureType);
                }
            }
        }

        return featureType;
    }

    public final Envelope getBounds() throws IOException {
        return all(entry.getState(transaction)).getBounds();
    }

    public final Envelope getBounds(Query query) throws IOException {
        return filtered(entry.getState(transaction), query.getFilter()).getBounds();
    }

    public final int getCount(Query query) throws IOException {
        return filtered(entry.getState(transaction), query.getFilter()).size();
    }

    public final FeatureCollection getFeatures() throws IOException {
        return getFeatures(Query.ALL);
    }

    public final FeatureCollection getFeatures(Query query)
        throws IOException {
        FeatureCollection features = getFeatures(query.getFilter());

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
            // features = features.reType( query.getPropertyNames() );
        }

        return features;
    }

    public final FeatureCollection getFeatures(Filter filter)
        throws IOException {
        if ((filter == null) || (filter == Filter.INCLUDE)) {
            return all(entry.getState(transaction));
        }

        return filtered(entry.getState(transaction), filter);
    }

    public final void addFeatureListener(FeatureListener listener) {
        entry.getState(transaction).addListener(listener);
    }

    public final void removeFeatureListener(FeatureListener listener) {
        entry.getState(transaction).removeListener(listener);
    }

    //
    // Internal API
    //
    /**
     * Creates a feature type for the entry.
     * <p>
     * An implementation of this method should create a new instance of
     * {@link SimpleTypeBuilder}, injecting it with the factory provided by
     * the datastore via. Example.
     * <pre>
     *   <code>
     *   SimpleTypeBuilder builder = new SimpleTypeBuilder( entry.getDataStore().getTypeFactory());
     *   ...
     *   </code>
     * </pre>
     */
    protected abstract FeatureType buildFeatureType() throws IOException;

    /**
     * FeatureCollection representing the entire contents.
     * <p>
     * Available via getFeatureSource():
     * <ul>
     * <li>getFeatures()
     * <li>getFeatures( Filter.INCLUDES )
     * </ul>
     *
     * @param state
     * @return all content
     */
    protected abstract FeatureCollection all(ContentState state);

    /**
     * FeatureCollection representing a subset of available content.
     * <p>
     * Available via getFeatureSource():
     * <ul>
     * <li>getFeatures().filter( filter )
     * <li>getFeatures( filter )
     * </ul>
     * @param state
     * @param filter
     * @return subset of content
     */
    protected abstract FeatureCollection filtered(ContentState state, Filter filter);

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
