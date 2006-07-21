/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.data.collection;

import java.io.IOException;

import org.geotools.data.AbstractDataStore;
import org.geotools.data.DataSourceException;
import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.SchemaNotFoundException;
import org.geotools.data.Transaction;
import org.geotools.feature.CollectionEvent;
import org.geotools.feature.CollectionListener;
import org.geotools.feature.DefaultFeatureType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureType;
import org.geotools.filter.Filter;

import com.vividsolutions.jts.geom.Envelope;


/**
 * Simple data store wrapper for feature collections. Allows to use feature collections in the user
 * interface layer and everything else where a data store or a feature source is needed.
 * @source $URL$
 */
public class CollectionDataStore extends AbstractDataStore {
    FeatureType featureType;
    FeatureCollection collection;

    /**
     * Builds a data store wrapper on top of a feature collection
     *
     * @param collection
     */
    public CollectionDataStore(FeatureCollection collection) {
        this.collection = collection;

        if (collection.size() == 0) {
            this.featureType = DefaultFeatureType.EMPTY;
        } else {
            this.featureType = ((Feature) collection.iterator().next()).getFeatureType();
        }

        collection.addListener(new FeatureCollectionListener());
    }

    /**
     * @see org.geotools.data.DataStore#getTypeNames()
     */
    public String[] getTypeNames() {
        return new String[] { featureType.getTypeName() };
    }

    /**
     * @see org.geotools.data.DataStore#getSchema(java.lang.String)
     */
    public FeatureType getSchema(String typeName) throws IOException {
        if ((typeName != null) && typeName.equals(featureType.getTypeName())) {
            return featureType;
        }

        throw new IOException(typeName + " not available");
    }

    /**
     * Provides FeatureReader over the entire contents of <code>typeName</code>.
     * 
     * <p>
     * Implements getFeatureReader contract for AbstractDataStore.
     * </p>
     *
     * @param typeName
     *
     * @return
     *
     * @throws IOException If typeName could not be found
     * @throws DataSourceException See IOException
     *
     * @see org.geotools.data.AbstractDataStore#getFeatureSource(java.lang.String)
     */
    public FeatureReader getFeatureReader(final String typeName)
        throws IOException {
    	return new DelegateFeatureReader( getSchema(typeName), collection.features() );
    }

    /**
     * Returns the feature collection held by this data store
     *
     * @return
     */
    public FeatureCollection getCollection() {
        return collection;
    }

    /**
     * @throws SchemaNotFoundException 
     * @see org.geotools.data.AbstractDataStore#getBounds(java.lang.String,
     *      org.geotools.data.Query)
     */
    protected Envelope getBounds(Query query) throws SchemaNotFoundException{
        String featureTypeName = query.getTypeName();
        if (!featureType.getTypeName().equals(featureTypeName)) {
            throw new SchemaNotFoundException(featureTypeName);
        }

        return getBoundsInternal(query);
    }

    /**
     * @param query
     */
    protected Envelope getBoundsInternal(Query query) {
        FeatureIterator iterator = collection.features();
        Envelope envelope = null;

        if (iterator.hasNext()) {
            int count = 1;
            Filter filter = query.getFilter();
            envelope = iterator.next().getDefaultGeometry().getEnvelopeInternal();

            while (iterator.hasNext() && (count < query.getMaxFeatures())) {
                Feature feature = iterator.next();

                if (filter.contains(feature)) {
                    count++;
                    envelope.expandToInclude(feature.getDefaultGeometry().getEnvelopeInternal());
                }
            }
        }

        return envelope;
        
    }

    /**
     * @see org.geotools.data.AbstractDataStore#getCount(java.lang.String, org.geotools.data.Query)
     */
    protected int getCount(Query query)
        throws IOException {
        String featureTypeName = query.getTypeName();
        if (!featureType.getTypeName().equals(featureTypeName)) {
            throw new SchemaNotFoundException(featureTypeName);
        }
            int count = 0;
            FeatureIterator iterator = collection.features();

            Filter filter = query.getFilter();

            while (iterator.hasNext() && (count < query.getMaxFeatures())) {
                if (filter.contains(iterator.next())) {
                    count++;
                }
            }

            return count;
    }

    /**
     * Simple listener that forwards collection events into data store events
     *
     * @author aaime
     */
    private class FeatureCollectionListener implements CollectionListener {
        public void collectionChanged(CollectionEvent tce) {
            String typeName = featureType.getTypeName();
            Envelope bounds = null;

            bounds = getBoundsInternal(Query.ALL);

            switch (tce.getEventType()) {
            case CollectionEvent.FEATURES_ADDED:
                listenerManager.fireFeaturesAdded(typeName, Transaction.AUTO_COMMIT, bounds, false);

                break;

            case CollectionEvent.FEATURES_CHANGED:
                listenerManager.fireFeaturesChanged(typeName, Transaction.AUTO_COMMIT, bounds, false);

                break;

            case CollectionEvent.FEATURES_REMOVED:
                listenerManager.fireFeaturesRemoved(typeName, Transaction.AUTO_COMMIT, bounds, false);

                break;
            }
        }
    }
}
